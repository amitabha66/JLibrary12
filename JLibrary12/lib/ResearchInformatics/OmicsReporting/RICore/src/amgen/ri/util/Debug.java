/*
 * Class: Debug
 *
 * Version: $Revision: 1.1 $
 *
 * Created: Jeffrey McDowell, 19 Nov 1998
 *
 * Modified: $Author: cvs $
 *
 * Description:
 * Stuff to assist debuging
 *
 * Revision Log:
 * $Log: Debug.java,v $
 * Revision 1.1  2011/10/26 04:13:32  cvs
 * no message
 *
 * Revision 1.9  2010/09/07 01:38:15  cvs
 * no message
 *
 * Revision 1.8  2010/08/03 23:35:33  cvs
 * no message
 *
 * Revision 1.7  2009/05/11 23:28:53  cvs
 * no message
 *
 * Revision 1.6  2009/04/14 22:59:23  cvs
 * no message
 *
 * Revision 1.5  2008/07/14 23:30:50  cvs
 * no message
 *
 * Revision 1.4  2008/06/02 23:29:39  cvs
 * no message
 *
 * Revision 1.3  2007/01/25 00:24:31  cvs
 * no message
 *
 * Revision 1.2  2007/01/13 00:10:58  cvs
 * no message
 *
 * Revision 1.1.1.1  2007/01/05 00:18:00  cvs
 * My new CVS module.
 *
 * Revision 1.19  2006/09/05 20:55:47  mcdowja
 * no message
 *
 * Revision 1.18  2005/03/08 21:58:26  mcdowelj
 * no message
 *
 * Revision 1.17  2004/08/18 02:52:52  mcdowelj
 * no message
 *
 * Revision 1.16  2004/08/16 04:21:46  mcdowelj
 * no message
 *
 * Revision 1.15  2004/03/16 22:19:27  mcdowelj
 * no message
 *
 * Revision 1.14  2003/09/24 20:56:34  mcdowelj
 * no message
 *
 * Revision 1.13  2003/07/25 21:20:25  mcdowelj
 * no message
 *
 * Revision 1.12  2003/07/15 16:23:35  mcdowelj
 * no message
 *
 * Revision 1.11  2003/07/14 04:52:26  mcdowelj
 * no message
 *
 * Revision 1.10  2002/11/14 22:12:52  mcdowelj
 * no message
 *
 * Revision 1.9  2002/03/11 22:13:08  mcdowelj
 * no message
 *
 * Revision 1.8  2002/01/14 02:00:00  mcdowelj
 * no message
 *
 * Revision 1.7  2001/10/22 19:57:21  mcdowelj
 * no message
 *
 * Revision 1.6  2001/08/15 04:42:25  mcdowelj
 * no message
 *
 * Revision 1.5  2001/08/06 03:01:39  mcdowelj
 * no message
 *
 * Revision 1.4  2001/07/30 02:29:07  mcdowelj
 * no message
 *
 * Revision 1.3  2001/07/23 21:02:25  mcdowelj
 * no message
 *
 * Revision 1.2  2000/05/31 22:11:19  mcdowelj
 * X
 *
 * Revision 1.1  2000/03/17 21:08:07  mcdowelj
 * Minor changes and Debug class
 *
 *
 *
 */

package amgen.ri.util;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import amgen.ri.time.ElapsedTime;
import amgen.ri.xml.ExtXMLElement;
import org.apache.log4j.Logger;

public class Debug {
    static PrintStream out = System.err;
    static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS MMM dd yyyy");

    private static boolean debug = true;

    private static ElapsedTime timer;

    /**
     * Sets the Global PrintStream for the Debug messages
     * @param _out
     */
    public static void setPrintStream(PrintStream _out) {
        out = _out;
    }

    public static void setDebug(boolean b) {
        debug = b;
    }

    public static String dumpStack() {
        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);
        Exception e = new Exception("Debug Stack trace");
        e.printStackTrace(writer);
        return sw.toString();
    }

    public static String getStack(int stackTraceLine) {
        String[] stack = getStack();
        if (stackTraceLine < 0 || stackTraceLine >= stack.length) {
            return null;
        }
        return stack[stackTraceLine];
    }

    public static void printSplitTime() {
        printSplitTime(null);
    }

    public static void printSplitTime(Object msg) {
        if (timer == null) {
            timer = new ElapsedTime(true);
        }
        print(timer.getSplit(msg), false, false);
    }

    public static void resetSplitTime() {
        if (timer != null) {
            print(timer.getElapsed(), false, false);
        }
        timer = null;
    }

    public static void print(PrintWriter writer) {
        print(writer, null);
    }

    public static void print(PrintWriter writer, Object s) {
        print(writer, s, false, false);
    }

    public static void print(PrintWriter writer, Object s, boolean showDate, boolean showStack) {
        print(writer, s, showDate, (showStack ? -1 : 0));
    }

    public static void print(PrintWriter writer, Object s, boolean showDate, int stackDepth) {
        if (!debug) {
            return;
        }
        String output = (showDate ? dateFormat.format(new Date()) + ": " : "");
        if (stackDepth < 0 || stackDepth > 0) {
            output += getMessageString(s);
            String[] stackArray = getStack();
            int depth = (stackDepth < 0 || stackDepth >= stackArray.length ? stackArray.length : stackDepth);
            output += (output.length() > 0 ? "\n" : "") + "Call Stack\n";
            for (int i = 0; i < depth; i++) {
                output += "\t+- " + stackArray[i] + "\n";
            }
        } else {
            output = output + getMethod();
            if (s != null) {
                output += ":" + getMessageString(s);
            }
        }
        writer.println(output);
        writer.flush();
    }

    private static String getMessageString(Object s) {
        StringBuffer sb = new StringBuffer();
        if (s != null) {
            if (s instanceof Collection) {
                sb.append("[");
                for (Object s1 : (Collection) s) {
                    if (sb.length() > 1) {
                        sb.append(",");
                    }
                    sb.append(s1);
                }
                sb.append("]");
            } else if (s instanceof Map) {
                sb.append("{");
                for (Object s1 : ( (Map) s).keySet()) {
                    if (sb.length() > 1) {
                        sb.append(",");
                    }
                    sb.append(s1 + ":" + ( (Map) s).get(s1));
                }
                sb.append("}");
            } else if (s instanceof Document) {
                sb.append("\n" + ExtXMLElement.toPrettyString( (Document) s));
            } else if (s instanceof Element) {
                sb.append("\n" + ExtXMLElement.toPrettyString( (Element) s));
            } else {
                sb.append(s);
            }
        }
        return sb.toString();
    }

    public static String[] getStack() {
        try {
            ArrayList stack = new ArrayList();
            String thisClass = "amgen.ri.util.Debug";
            String dumpStack = dumpStack();
            BufferedReader reader = new BufferedReader(new StringReader(dumpStack));
            String line = null;
            int lineCount = 0;
            while ( (line = reader.readLine()) != null) {
                line = line.trim();
                if (line.indexOf(thisClass) < 0 && lineCount > 0) {
                    stack.add(line.substring(3));
                }
                lineCount++;
            }
            String[] stackArray = new String[stack.size()];
            stack.toArray(stackArray);
            return stackArray;
        } catch (Exception e) {}
        return null;
    }

    public static String getCall() {
        try {
            String thisClass = "amgen.ri.util.Debug";
            String dumpStack = dumpStack();
            BufferedReader reader = new BufferedReader(new StringReader(dumpStack));
            String line = null;
            int lineCount = 0;
            while ( (line = reader.readLine()) != null) {
                line = line.trim();
                if (lineCount > 0 && line.indexOf(thisClass) < 0) {
                    return line.substring(3);
                }
                lineCount++;
            }
        } catch (Exception e) {}
        return "no line";
    }

    private static String getMethod() {
        String stackLine = getCall();
        int[] indexes = ExtString.indexesOf(stackLine, '.');
        int pos = 3;
        if (indexes.length > 3) {
            return stackLine.substring(indexes[indexes.length - 3] + 1);
        }
        return stackLine;
    }

    public static void print() {
        print(new PrintWriter(out), null);
    }

    public static void print(Object s) {
        print(new PrintWriter(out), s, false, false);
    }

    public static void print(boolean showDate) {
        print(new PrintWriter(out), null, showDate, false);
    }

    public static void print(boolean showDate, boolean showStack) {
        print(new PrintWriter(out), null, showDate, showStack);
    }

    public static void print(int stackDepth) {
        print(new PrintWriter(out), null, false, stackDepth);
    }

    public static void print(Object s, boolean showDate, boolean showStack) {
        print(new PrintWriter(out), s, showDate, showStack);
    }

    public static void print(Object s, int stackDepth) {
        print(new PrintWriter(out), s, false, stackDepth);
    }

    public static void print(boolean showDate, int stackDepth) {
        print(new PrintWriter(out), null, showDate, stackDepth);
    }

    public static void print(Object s, boolean showDate, int stackDepth) {
        print(new PrintWriter(out), s, showDate, stackDepth);
    }

    public static void dump(Map map) {
        if (map == null) {
            print("Null Map");
            return;
        }
        Iterator keyIter = map.keySet().iterator();
        while (keyIter.hasNext()) {
            Object key = keyIter.next();
            print(key + " => " + map.get(key));
        }
    }

    public static void dump(Object[] array) {
        if (array == null) {
            print("Null Array");
            return;
        }
        for (int i = 0; i < array.length; i++) {
            print(i + " => " + array[i]);
        }
    }

    public static void dump(double[] array) {
        if (array == null) {
            print("Null Array");
            return;
        }
        for (int i = 0; i < array.length; i++) {
            print(i + " => " + array[i]);
        }
    }

    public static void dump(int[] array) {
        if (array == null) {
            print("Null Array");
            return;
        }
        for (int i = 0; i < array.length; i++) {
            print(i + " => " + array[i]);
        }
    }

    public static void dump(List list) {
        if (list == null) {
            print("Null List");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            print(i + " => " + list.get(i));
        }
    }

    public static void main(String[] args) {
        Debug.print(true, 5);
        
;    }

}
