package amgen.ri.thread;

// This class holds the statistics of the
// Thread Pool from which it was returned
public class ThreadPoolStats {
    public int maxThreads;
    public int minThreads;
    public int maxIdleTime;
    public int numThreads;
    public int pendingJobs;
    public int jobsInProgress;

    public String toString() {
        java.lang.StringBuffer sb =
            new java.lang.StringBuffer();
        String strMax = (maxThreads == -1)
            ? "No limit" : new Integer(maxThreads).toString();
        String strMin = (minThreads == -1)
            ? "No limit" : new Integer(minThreads).toString();
        sb.append("maxThreads = " + strMax + "\n");
        sb.append("minThreads = " + strMin + "\n");
        sb.append("maxIdleTime = " + maxIdleTime + "\n");
        sb.append("numThreads = " + numThreads + "\n");
        sb.append("pendingJobs = " + pendingJobs + "\n");
        sb.append("jobsInProgress = " + jobsInProgress + "\n");
        return (sb.toString());
    }
}
