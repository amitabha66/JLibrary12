package amgen.ri.sys;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Class:
 * SystemCommand
 * Description: 
 * Used to Execute a process on the system. Must provide the command and optionally any environment variables. 
 * The execute() method waits for the command to return. Standard out and error streams are retrieved into Strings
 * Optionally, 2 additional entry points are provided
 * 
 * @author jemcdowe
 * 
 * @version $Id 
 */
public class SystemCommand implements Runnable {
  private String command;
  private String[] environment;
  private File workingDir;
  private SystemCommandResults systemCommandResults;
  private String stdout;
  private String stderr;
  private Writer stdoutWriter;
  private Writer stderrWriter;
  private int exitValue;
  private Exception exception;
  private Thread thread = null;

  /**
   * Creates a SystemCommand object which is used to run a command on the system.
   * @param command the command to run on the System
   */
  public SystemCommand(String command) {
    this(command, null);
  }

  /**
   * Creates a SystemCommand object which is used to run a command on the system.
   * @param command the command to run on the System
   * @param environment any environment variables to add to the System. Null if none.
   */
  public SystemCommand(String command, Map environment) {
    this.command = command;
    if (environment != null) {
      setEnvironment(environment);
    } else {
      this.environment = null;
    }
    this.stdoutWriter = null;
    this.stderrWriter = null;
  }

  /**
   * Creates a SystemCommand object which is used to run a command on the system.
   * @param command the command to run on the System
   * @param environment any environment variables to add to the System. Null if none.
   * @param workingDir sets the working directory of the process
   */
  public SystemCommand(String command, Map environment, File workingDir) {
    this(command, environment);
    this.workingDir = workingDir;
  }

  /**
   * Creates a SystemCommand object which is used to run a command on the system.
   * @param command the command to run on the System
   * @param environment any environment variables to add to the System. Null if none.
   * @param workingDir sets the working directory of the process
   * @param stdoutWriter sets a writer for standard out
   * @param stderrWriter sets a writer for standard error
   */
  public SystemCommand(String command, Map environment, File workingDir, Writer stdoutWriter, Writer stderrWriter) {
    this(command, environment, workingDir);
    this.stdoutWriter = stdoutWriter;
    this.stderrWriter = stderrWriter;
  }

  /**
   * Creates a SystemCommand object which is used to run a command on the system.
   * @param command the command to run on the System
   * @param environment any environment variables to add to the System. Null if none.
   * @param stdoutFile sets a File for standard out
   * @param stderrFile sets a File for standard error     
   */
  public SystemCommand(String command, Map environment, File workingDir, File stdoutFile, File stderrFile) throws IOException {
    this(command, environment, workingDir);
    this.stdoutWriter = new FileWriter(stdoutFile);
    this.stderrWriter = new FileWriter(stderrFile);
  }

  /**
   * Sets any environment variables that should be set prior to executing the service
   * @param environment Map
   */
  public final void setEnvironment(Map environment) {
    if (environment != null) {
      this.environment = new String[environment.size()];
      int count = 0;
      for (Object name : environment.keySet()) {
        this.environment[count++] = name + "=" + environment.get(name);
      }
    } else {
      this.environment = null;
    }
  }

  /**
   * Run the command returning the exit value of the process.
   * This version has no timeout, so it depends on the Process to complete. 
   * Use <code>execute(long millisecondTimeout)</code> to ensure the Process will
   * not block indefinitely.
   * 
   * @return the exit value of the process
   * @throws IOException if any exception is throws from reading the stdout or stderr streams
   * @throws InterruptedException if any error is thrown while waiting for the process to complete
   */
  public int execute() throws IOException, InterruptedException {
    Runtime runtime = Runtime.getRuntime();
    Process process = runtime.exec(command, environment, workingDir);

    ThreadedStreamReader stdoutStreamReader = new ThreadedStreamReader(process.getInputStream(), stdoutWriter);
    ThreadedStreamReader stderrStreamReader = new ThreadedStreamReader(process.getErrorStream(), stderrWriter);
    try {
      stdoutStreamReader.start();
      stderrStreamReader.start();
      /** Wait for the end of the process */
      process.waitFor();
      /** Wait for streams to finish reading */
      while (stdoutStreamReader.isAlive()) {
      }
      while (stderrStreamReader.isAlive()) {
      }
      stdoutStreamReader.close();
      stderrStreamReader.close();
      stdout = stdoutStreamReader.getStreamValue();
      stderr = stderrStreamReader.getStreamValue();
      exitValue = process.exitValue();
      systemCommandResults = new SystemCommandResults(stdout, stderr, exitValue);
      return exitValue;
    } finally {
      stdoutStreamReader.close();
      stderrStreamReader.close();
      process.destroy();
    }
  }

  /**
   * Run the command returning the exit value of the process.
   * This method implements a timeout to force the Process to return after a
   * prescribed time
   *
   * @return the exit value of the process
   * @throws IOException if any exception is throws from reading the stdout
   *   or stderr streams
   * @throws InterruptedException if any error is thrown while waiting for
   *   the process to complete
   * @throws TimeoutException
   * @param millisecondTimout long
   */
  public int execute(long millisecondTimeout) throws IOException, InterruptedException, TimeoutException {
    ThreadPoolExecutor tpe = new ThreadPoolExecutor(2, 10, millisecondTimeout, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
    Runtime runtime = Runtime.getRuntime();
    Process process = runtime.exec(command, environment, workingDir);

    ThreadedStreamReader stdoutStreamReader = new ThreadedStreamReader(process.getInputStream(), stdoutWriter);
    ThreadedStreamReader stderrStreamReader = new ThreadedStreamReader(process.getErrorStream(), stderrWriter);

    tpe.execute(stdoutStreamReader);
    tpe.execute(stderrStreamReader);
    try {
      Worker worker = new Worker(process);
      worker.start();
      try {
        worker.join(millisecondTimeout);
        if (worker.getExit() == null) {
          throw new TimeoutException();
        }
      } catch (InterruptedException ex) {
        worker.interrupt();
        Thread.currentThread().interrupt();
        throw ex;
      }
      tpe.shutdown();
      /** Wait for streams to finish reading */
      while (!tpe.isTerminated()) {
      }

      stdoutStreamReader.close();
      stderrStreamReader.close();
      stdout = stdoutStreamReader.getStreamValue();
      stderr = stderrStreamReader.getStreamValue();
      exitValue = process.exitValue();
      systemCommandResults = new SystemCommandResults(stdout, stderr, exitValue);
      return exitValue;
    } finally {
      stdoutStreamReader.close();
      stderrStreamReader.close();
      process.destroy();
      tpe.shutdownNow();
    }
  }

  /**
   * Run the command returning the exit value of the process. This method
   * implements a timeout to force the Process to return after a prescribed
   * time.
   * This version DOES NOT capture any output from STDOUT/ERR
   *
   * @return the exit value of the process
   * @throws IOException if any exception is throws from reading the stdout
   *   or stderr streams
   * @throws InterruptedException if any error is thrown while waiting for
   *   the process to complete
   * @throws TimeoutException
   * @param millisecondTimeout long
   */
  public int executeNoStdOutputs(long millisecondTimeout) throws IOException, InterruptedException, TimeoutException {
    Runtime runtime = Runtime.getRuntime();
    Process process = runtime.exec(command, environment, workingDir);

    try {
      Worker worker = new Worker(process);
      worker.start();
      try {
        worker.join(millisecondTimeout);
        if (worker.getExit() == null) {
          throw new TimeoutException();
        }
      } catch (InterruptedException ex) {
        worker.interrupt();
        Thread.currentThread().interrupt();
        throw ex;
      }
      stdout = "No STDOUT captured";
      stderr = "No STDERR captured";
      exitValue = process.exitValue();
      systemCommandResults = new SystemCommandResults(stdout, stderr, exitValue);
      return exitValue;
    } finally {
      process.destroy();
    }
  }

  /**
   * The launch stub for the Threaded process. Generally this is not called directly.
   * Use execute() or executeInThread() to launch thread
   */
  public void run() {
    try {
      execute();
    } catch (Exception e) {
      exception = e;
    }
    thread = null;
  }

  /**
   * Run the command in a Thread. Returns immediately.
   * @throws IOException if any exception is throws from reading the stdout or stderr streams
   * @throws InterruptedException if any error is thrown while waiting for the process to complete
   * @throws IllegalArgumentException if the command is already running
   */
  public void executeInThread() throws IOException, InterruptedException, IllegalArgumentException {
    if (thread != null) {
      throw new IllegalArgumentException("Thread already running.");
    }
    thread = new Thread(this);
    thread.start();
  }

  /**
   * Runs the command waiting for it to complete. Used only if stdout and stderr is not required
   * @param command the System command to run
   * @param environment any environment variables to add to the environment
   * @return the exit value
   * @throws IOException if any exception is throws from reading the stdout or stderr streams
   * @throws InterruptedException if any error is thrown while waiting for the process to complete
   */
  public static int executeProcess(String command, HashMap environment) throws IOException, InterruptedException {
    SystemCommand executeCommand = new SystemCommand(command, environment);
    return executeCommand.execute();
  }

  /**
   * Runs the command waiting for it to complete. Used only if stdout and stderr is not required
   * @param command the System command to run
   * @param environment any environment variables to add to the environment
   * @return the exit value
   * @throws IOException if any exception is throws from reading the stdout or stderr streams
   * @throws InterruptedException if any error is thrown while waiting for the process to complete
   */
  public static int executeProcess(String command) throws IOException, InterruptedException {
    SystemCommand executeCommand = new SystemCommand(command, null);
    return executeCommand.execute();
  }

  /**
   * Returns the standard out of the completed process
   */
  public String getStdout() {
    return (stdout == null || stdout.length() == 0 ? null : stdout);
  }

  /**
   * Returns the standard error of the completed process
   */
  public String getStderr() {
    return (stderr == null || stderr.length() == 0 ? null : stderr);
  }

  /**
   * Returns if any standard output messages were generated
   */
  public boolean hasStdout() {
    return getStdout() != null;
  }

  /**
   * Returns if any standard error messages were generated
   */
  public boolean hasStderr() {
    return getStderr() != null;
  }

  /**
   * Returns the exit value of the completed process
   */
  public int getExitValue() {
    return exitValue;
  }

  /**
   * Returns the results of the SystemCommand as a SystemCommandResults object
   */
  public SystemCommandResults getResults() {
    return systemCommandResults;
  }

  /**
   * Returns the command SystemCommand
   *
   * @return String
   */
  public String getCommand() {
    return command;
  }

  /**
   * Returns any exception thrown during a Threaded call.
   */
  public Exception getException() {
    return exception;
  }

  /**
   * Returns whether the thread is running
   */
  public boolean isRunning() {
    return (thread != null);
  }

  /**
   * Encapsulate the standard output and error as well as the exit status
   */
  public class SystemCommandResults {
    protected String stdout;
    protected String stderr;
    protected int status;

    /**
     * Constructor which sets the class variables
     */
    public SystemCommandResults(String stdout, String stderr, int status) {
      this.stdout = stdout;
      this.stderr = stderr;
      this.status = status;
    }

    /** Get value for stdout */
    public String getStdout() {
      return stdout;
    }

    /** Set value for stdout */
    public void setStdout(String stdout) {
      this.stdout = stdout;
    }

    /** Get value for stderr */
    public String getStderr() {
      return stderr;
    }

    /** Set value for stderr */
    public void setStderr(String stderr) {
      this.stderr = stderr;
    }

    /** Get value for status */
    public int getStatus() {
      return status;
    }

    /** Set value for status */
    public void setStatus(int status) {
      this.status = status;
    }

    @Override
    public String toString() {
      return "Stdout: " + stdout + "\nStderr: " + stderr + "\nStatus: " + status;
    }
  }
}

/**
 * Private reader used to read a stream in its own thread
 */
class ThreadedStreamReader extends Thread {
  InputStream stream;
  BufferedReader in;
  Writer outputWriter;
  boolean running = false;

  public ThreadedStreamReader(InputStream in) {
    this(in, null);
  }

  public ThreadedStreamReader(InputStream in, Writer outputWriter) {
    stream = in;
    if (outputWriter == null) {
      this.outputWriter = new StringWriter();
    } else {
      this.outputWriter = outputWriter;
    }
  }

  @Override
  public void run() {
    running = true;
    try {
      String line;
      if (outputWriter instanceof StringWriter) {
        ((StringWriter) outputWriter).getBuffer().setLength(0);
      }
      PrintWriter writer = new PrintWriter(outputWriter);

      in = new BufferedReader(new InputStreamReader(stream));
      while ((line = in.readLine()) != null) {
        writer.println(line);
        writer.flush();
      }
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    running = false;

  }

  public String getStreamValue() {
    if (outputWriter instanceof StringWriter) {
      return ((StringWriter) outputWriter).toString();
    } else {
      return null;
    }
  }

  public boolean getRunning() {
    return running;
  }

  public void stopThread() {
    running = false;
  }

  public void close() {
    try {
      if (in != null) {
        in.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

/**
 * Encapsulates a Process in a Thread and persists its exit status value
 * @author jemcdowe
 */
class Worker extends Thread {
  private final Process process;
  private Integer exit;

  public Worker(Process process) {
    this.process = process;
  }

  @Override
  public void run() {
    try {
      exit = process.waitFor();
    } catch (InterruptedException ignore) {
      return;
    }
  }

  public Integer getExit() {
    return exit;
  }
}
