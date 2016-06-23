package amgen.ri.thread;

// The interface to be implemented by the thread pool.
public interface ThreadPool {
    public void addJob(java.lang.Runnable job);

    public void kill();

    public ThreadPoolStats getStats();
}
