package amgen.ri.thread;

/**
 *
 * This is the Test Program used to Test the
 * Thread pool implementation. this fires off n jobs w/ callbacks
 */
public class ThreadPoolExample {

    private final int maxThreads = 3;
    private final int minThreads = 0;
    private final int maxIdleTime = 2500;
    private final int nJobs = 10;
    private int cJobs = 0;
    private ThreadPool pool;

    public static void main(String[] args) {
        new ThreadPoolExample();
    }

    public ThreadPoolExample() {
        this.pool = new ThreadPoolImpl(minThreads, maxThreads, maxIdleTime);

        for (int i = 1; i <= nJobs; i++) {
            pool.addJob(new Job(i, this));
        }
    }

    public void callback(Job job) {
        System.out.println("Job: " + job.jobNumber + " Finished.");
        System.out.println("**********\n" +
                           pool.getStats() + "**********");
        //Count the number of returned jobs
        //Kill the ThreadPool if finished- not really needed if minThreads==0
        cJobs++;
        if (cJobs == nJobs) {
            pool.kill();
        }
    }

    // This class is passed in as the "job" to the addJob()
    // method on the thread pool. The only requirement for
    // this class is that it must implement java.lang.Runnable
    private class Job implements java.lang.Runnable {
        public int jobNumber;
        private ThreadPoolExample parent;

        public Job(int jobNumber, ThreadPoolExample parent) {
            this.jobNumber = jobNumber;
            this.parent = parent;
        }

        public void run() {
            System.out.println("This is job# " + jobNumber);
            try {
                java.lang.Thread.sleep(5000);
            } catch (java.lang.Exception e) {
            }
            parent.callback(this);
        }
    }
}
