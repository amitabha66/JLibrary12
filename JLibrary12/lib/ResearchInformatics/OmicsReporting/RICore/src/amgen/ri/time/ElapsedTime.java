package amgen.ri.time;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import amgen.ri.util.Debug;
import amgen.ri.util.Pair;
import amgen.ri.util.PrintfFormat;

/*
 * Class: ElapsedTime
 *
 * Version: $Revision: 1.1 $
 *
 * Created: Jeffrey McDowell, 05 Aug 2003
 *
 * Modified: $Author: cvs $, $Date: 2011/10/26 04:13:33 $
 *
 * Description:
 * Simple class for handling elapsed time
 *
 * Revision Log:
 * $Log: ElapsedTime.java,v $
 * Revision 1.1  2011/10/26 04:13:33  cvs
 * no message
 *
 * Revision 1.7  2009/05/11 23:28:53  cvs
 * no message
 *
 * Revision 1.6  2008/10/10 23:28:20  cvs
 * no message
 *
 * Revision 1.5  2008/05/30 23:10:11  cvs
 * no message
 *
 * Revision 1.4  2008/02/21 00:37:47  cvs
 * no message
 *
 * Revision 1.3  2007/08/24 23:25:31  cvs
 * no message
 *
 * Revision 1.2  2007/01/13 00:10:58  cvs
 * no message
 *
 * Revision 1.1.1.1  2007/01/05 00:18:00  cvs
 * My new CVS module.
 *
 * Revision 1.12  2006/08/25 17:33:31  mcdowja
 * no message
 *
 * Revision 1.11  2006/01/23 22:25:18  mcdowja
 * no message
 *
 * Revision 1.10  2005/02/10 22:13:23  mcdowelj
 * no message
 *
 * Revision 1.9  2004/08/05 20:56:15  mcdowelj
 * no message
 *
 * Revision 1.8  2004/07/06 20:59:02  mcdowelj
 * no message
 *
 * Revision 1.7  2004/04/30 21:17:08  mcdowelj
 * no message
 *
 * Revision 1.6  2004/04/08 20:55:35  mcdowelj
 * no message
 *
 * Revision 1.5  2004/03/12 22:02:45  mcdowelj
 * no message
 *
 * Revision 1.4  2004/03/11 22:14:11  mcdowelj
 * no message
 *
 * Revision 1.3  2003/11/10 22:15:26  mcdowelj
 * no message
 *
 * Revision 1.2  2003/10/14 21:20:22  mcdowelj
 * no message
 *
 * Revision 1.1  2003/08/05 21:03:48  mcdowelj
 * no message
 *
 */

public class ElapsedTime {
    private double start = 0;
    private double end = 0;
    private double currSplit = 0;

    private Map monitors;

    public ElapsedTime() {}

    public ElapsedTime(boolean start) {
        this();
        if (start) {
            start();
        }
    }

    public void startMonitor(String monitor) {
        if (monitors == null) {
            monitors = new HashMap();
        }
        if (!monitors.containsKey(monitor)) {
            monitors.put(monitor, new ArrayList());
        }
        Pair p = new Pair();
        p.setName(new Double(System.currentTimeMillis()));
        p.setValue(new Double( -1));
        ( (List) monitors.get(monitor)).add(p);
    }

    public void splitMonitor(String monitor) {
        if (monitors == null) {
            monitors = new HashMap();
        }
        if (!monitors.containsKey(monitor)) {
            throw new IllegalArgumentException("No monitor defined " + monitor);
        }
        List monitorList = (List) monitors.get(monitor);
        Pair lastPair = (Pair) monitorList.get(monitorList.size() - 1);
        lastPair.setValue(new Double(System.currentTimeMillis()));
    }

    public void printMonitors() {
        PrintWriter writer = new PrintWriter(System.out);
        printMonitors(writer);
        writer.flush();
    }

    public void printMonitors(PrintWriter writer) {
        PrintfFormat monitorSPrint = new PrintfFormat("\t\t%-15s %-7.2f sec");
        writer.println(monitors.size() + " MONITORS:");
        for (Iterator monitorIterator = monitors.keySet().iterator(); monitorIterator.hasNext(); ) {
            String monitor = (String) monitorIterator.next();
            List monitorPairs = (List) monitors.get(monitor);
            double avg = 0;
            int longestIndex = -1;
            int shortestIndex = -1;
            double longest = 0;
            double shortest = 0;
            for (int i = 0; i < monitorPairs.size(); i++) {
                Pair monitorPair = (Pair) monitorPairs.get(i);
                Double start = (Double) monitorPair.name();
                Double end = (Double) monitorPair.value();
                double diff = end.doubleValue() - start.doubleValue();
                avg += diff / monitorPairs.size();
                if (longestIndex == -1 || diff > longest) {
                    longest = diff;
                    longestIndex = i;
                }
                if (shortestIndex == -1 || diff < shortest) {
                    shortest = diff;
                    shortestIndex = i;
                }
            }
            Pair first = (Pair) monitorPairs.get(0);
            Pair last = (Pair) monitorPairs.get(monitorPairs.size() - 1);
            double monitorStart = ( (Double) first.name()).doubleValue();
            double monitorEnd = ( (Double) last.value()).doubleValue();

            writer.println("\t" + monitor + ": " + monitorPairs.size() + " split(s)");
            writer.println(monitorSPrint.sprintf(new Object[] {"Average:", new Double(avg / 1000)}));
            writer.println(monitorSPrint.sprintf(new Object[] {"Longest:", new Double(longest / 1000)}));
            writer.println(monitorSPrint.sprintf(new Object[] {"Shortest:", new Double(shortest / 1000)}));
            writer.println(monitorSPrint.sprintf(new Object[] {"Total:", new Double( (monitorEnd - monitorStart) / 1000)}));

        }
    }

    /**
     * Sets the start time to now
     * @return
     */
    public double start() {
        start = System.currentTimeMillis();
        return start;
    }

    public String startAsString() {
        double start = start();
        SimpleDateFormat format = new SimpleDateFormat(
            "dd-MMM-yyyy hh:mm:ss a");
        return format.format(new Date( (long) start));
    }

    /**
     * Sets the end time to now
     * @return
     */
    public double end() {
        end = System.currentTimeMillis();
        return end;
    }

    public String endAsString() {
        double end = end();
        SimpleDateFormat format = new SimpleDateFormat(
            "dd-MMM-yyyy hh:mm:ss a");
        return format.format(new Date( (long) end));
    }

    /**
     * Returns the full elapsed time as h hr m min s sec
     * from the start to the end. start() should be called. If end() is not
     * called before this method is, it is called first.
     * @return
     */
    public String getElapsed() {
        return getElapsed(null);
    }

    /**
     * Returns the elapsed time in milliseconds
     */
    public long getElapsedMillis() {
        if (end == 0) {
            end();
        }
        return (long) (end - start);
    }

    /**
     * Returns the elapsed time as h hr m min s sec
     * from the start to the current time & the last split
     * @return
     */
    public String getElapsed(Object msg) {
        if (end == 0) {
            end();
        }
        double totalTimeDiff = (end - start) / 1000;
        return (msg != null ? "[" + msg + "] " : "") + "Start Diff: " + getFormatedTime(totalTimeDiff, true);
    }

    /**
     * Returns the elapsed time as h hr m min s sec
     * from the start to the current time & the last split
     * @return
     */
    public String getElapsed(boolean showPosition) {
        return getElapsed(showPosition, 0);
    }

    /**
     * Returns the elapsed time as h hr m min s sec
     * from the start to the current time & the last split
     * @return
     */
    public String getElapsed(boolean showPosition, int stackPosition) {
        String msg = null;
        if (showPosition) {
            msg = getStackPosition(stackPosition);
        }
        return getElapsed(msg);
    }

    /**
     * Returns the elapsed time as h hr m min s sec
     * from the start to the current time & the last split
     * @return
     */
    public String getElapsed(Object msg, boolean showPosition, int stackPosition) {
        String stackmsg = null;
        if (showPosition) {
            stackmsg = getStackPosition(stackPosition);
            return getSplit( (msg == null ? "" : msg + "; ") + stackmsg);
        }
        return getElapsed(msg);
    }

    /**
     * Writes the full elapsed time as h hr m min s sec
     * from the start to the end. start() should be called. If end() is not
     * called before this method is, it is called first.
     * @return
     */
    public void writeElapsed() {
        PrintWriter writer = new PrintWriter(System.out);
        writer.println(getElapsed());
        writer.flush();
    }

    /**
     * Writes the full elapsed time as h hr m min s sec
     * from the start to the end. start() should be called. If end() is not
     * called before this method is, it is called first.
     * @return
     */
    public void writeElapsed(Object msg) {
        PrintWriter writer = new PrintWriter(System.out);
        writer.println(getElapsed(msg));
        writer.flush();
    }

    /**
     * Writes the full elapsed time as h hr m min s sec
     * from the start to the end. start() should be called. If end() is not
     * called before this method is, it is called first.
     * @return
     */
    public void writeElapsed(boolean showPosition, int stackPosition) {
        PrintWriter writer = new PrintWriter(System.out);
        writer.println(getElapsed(showPosition, stackPosition));
        writer.flush();
    }

    /**
     * Writes the full elapsed time as h hr m min s sec
     * from the start to the end. start() should be called. If end() is not
     * called before this method is, it is called first.
     * @return
     */
    public void writeElapsed(boolean showPosition) {
        writeElapsed(showPosition, 0);
    }

    /**
     * Writes the full elapsed time as h hr m min s sec
     * from the start to the end. start() should be called. If end() is not
     * called before this method is, it is called first.
     * @return
     */
    public void writeElapsed(Object msg, boolean showPosition) {
        PrintWriter writer = new PrintWriter(System.out);
        writer.println(getElapsed(msg, showPosition, 0));
        writer.flush();
    }

    /**
     * Returns the remaining time as h hr m min s sec by averaging the
     * previous split-times.
     * @param currentSplitNumber current split number
     * @param totalSplit total number of splits
     * @return
     */
    public String getRemaining(int currentSplitNumber, int totalSplit) {
        return getFormatedTime(getRemainingTime(currentSplitNumber, totalSplit), false);
    }

    /**
     * Returns the elapsed time as h hr m min s sec
     * from the start to the current time & the last split
     * @return
     */
    public String getSplit() {
        return getSplit(null);
    }

    /**
     * Returns the elapsed time as h hr m min s sec
     * from the start to the current time & the last split
     * @return
     */
    public String getSplit(boolean showPosition) {
        return getSplit(showPosition, 0);
    }

    /**
     * Returns the elapsed time as h hr m min s sec
     * from the start to the current time & the last split
     * @return
     */
    public String getSplit(boolean showPosition, int stackPosition) {
        String msg = null;
        if (showPosition) {
            msg = getStackPosition(stackPosition);
        }
        return getSplit(msg);
    }

    /**
     * Returns the elapsed time as h hr m min s sec
     * from the start to the current time & the last split
     * @return
     */
    public String getSplit(Object msg, boolean showPosition, int stackPosition) {
        String stackmsg = null;
        if (showPosition) {
            stackmsg = getStackPosition(stackPosition);
            return getSplit( (msg == null ? "" : msg + "; ") + stackmsg);
        }
        return getSplit(msg);
    }

    /**
     * Returns the elapsed time as h hr m min s sec
     * from the start to the current time & the last split
     * @return
     */
    public String getSplit(Object msg) {
        long split = System.currentTimeMillis();
        double totalTimeDiff = (split - start) / 1000;
        double splitTimeDiff = (split - currSplit) / 1000;
        if (currSplit == 0) {
            splitTimeDiff = 0;
        }
        currSplit = split;
        return (msg != null ? "[" + msg + "] " : "") + "Start Diff: " + getFormatedTime(totalTimeDiff, true) +
            "; Split Diff: " + getFormatedTime(splitTimeDiff, true);
    }

    public void writeSplit(PrintWriter writer) {
        writer.println(getSplit());
    }

    public void writeSplit() {
        PrintWriter writer = new PrintWriter(System.out);
        writeSplit(writer);
        writer.flush();
    }

    public void writeSplit(PrintWriter writer, Object msg) {
        writer.println(getSplit(msg));
    }

    public void writeSplit(Object msg) {
        PrintWriter writer = new PrintWriter(System.out);
        writeSplit(writer, msg);
        writer.flush();
    }

    public void writeSplit(boolean showPosition) {
        PrintWriter writer = new PrintWriter(System.out);
        writeSplit(writer, showPosition);
        writer.flush();
    }

    public void writeSplit(PrintWriter writer, boolean showPosition) {
        writer.println(getSplit(showPosition));
    }

    public void writeSplit(PrintWriter writer, Object msg, boolean showPosition) {
        writer.println(getSplit(msg, showPosition, 0));
    }

    public void writeSplit(Object msg, boolean showPosition) {
        PrintWriter writer = new PrintWriter(System.out);
        writeSplit(writer, msg, showPosition);
        writer.flush();
    }

    private String getFormatedTime(double timeDiff, boolean fraction) {
        Integer hours = new Integer( (int) (timeDiff / 3600));
        timeDiff = timeDiff - hours.intValue() * 3600;
        Integer minutes = new Integer( (int) (timeDiff / 60));
        timeDiff = timeDiff - minutes.intValue() * 60;
        Double seconds = new Double(timeDiff);
        PrintfFormat format;
        if (fraction) {
            format = new PrintfFormat("%i hr %i min %.2f sec");
        } else {
            format = new PrintfFormat("%i hr %i min %.0f sec");
            seconds = new Double(Math.round(timeDiff));
        }
        Object[] times = {
            hours, minutes, seconds};
        return format.sprintf(times);
    }

    /**
     * Same as getSplit()
     * @return
     */
    public String toString() {
        return getSplit();
    }

    /**
     * Returns the remaining time in seconds calculated by averaging the
     * previous split-times.
     * @param currentSplitNumber current split number
     * @param totalSplit total number of splits
     * @return
     */
    public double getRemainingTime(int currentSplitNumber, int totalSplit) {
        getSplit();
        if (currSplit == 0 || currentSplitNumber == 0) {
            return 0;
        }
        double timeDiff = (currSplit - start) / 1000;

        double perSplitTime = timeDiff / currentSplitNumber;
        return perSplitTime * (totalSplit - currentSplitNumber);
    }

    private String getStackPosition(int position) {
        String[] traces = Debug.getStack();
        List traceList = new ArrayList(traces.length);
        for (int i = 0; i < traces.length; i++) {
            if (traces[i].startsWith("a.lang.Exception") ||
                traces[i].startsWith("amgen.ri.time.ElapsedTime") || traces[i].startsWith("amgen.ri.util.Debug")) {
                continue;
            }
            traceList.add(traces[i]);
        }
        return (position < traceList.size() ? (String) traceList.get(position) : "Unknown");
    }

    public static void main(String[] a) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(
                "dd-MMM-yyyy");
            Date d = format.parse("01-JUL-2006");
            System.out.println(d + "\t" + d.getTime());

            d = format.parse("01-AUG-2006");
            System.out.println(d + "\t" + d.getTime());

            Debug.print();

        } catch (Exception e) {
        }

    }

}
