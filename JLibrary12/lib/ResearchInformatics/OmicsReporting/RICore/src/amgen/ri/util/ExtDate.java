package amgen.ri.util;

import amgen.ri.ldap.AmgenEnterpriseLookup;
import amgen.ri.ldap.AmgenLocationCode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.TimeZone;
import javax.naming.NamingException;

public final class ExtDate {
    private static String[] DATE_PATTERNS = {
    	  "EEE MMM dd HH:mm:ss zzz yyyy", // e.g. Thu Jan 01 15:30:05 PDT 2007
        "MM/dd/yyyy hh:mm:ss a", // e.g. 01/01/2007 3:30:05 PM
        "MM/dd/yyyy hh:mm a", // e.g. 01/01/2007 3:30 PM
        "MM/dd/yyyy HH:mm:ss", // e.g. 01/01/2007 15:30:05
        "MM/dd/yyyy HH:mm", // e.g. 01/01/2007 15:30
        "MM/dd/yyyy", // e.g. 01/01/2007
        "yyyy-MM-dd", // e.g. 2007-01-31
        "yyyy-MMM-dd", // e.g. 2007-JAN-31

        "MMM dd, yyyy hh:mm:ss a", // e.g. Jan 1, 2007 3:30:05 PM
        "MMM dd, yyyy hh:mm a", // e.g. Jan 1, 2007 3:30 PM
        "MMM dd, yyyy HH:mm:ss", // e.g. Jan 1, 2007 15:30:05
        "MMM dd, yyyy HH:mm", // e.g. Jan 1, 2007 15:30
        "MMM dd, yyyy", // e.g. Jan 1, 2007

        "dd MMM yyyy hh:mm:ss a", // e.g. 1 Jan 2007 3:30:05 PM
        "dd MMM yyyy hh:mm a", // e.g. 1 Jan 2007 3:30 PM
        "dd MMM yyyy HH:mm:ss", // e.g. 1 Jan 2007 15:30:05
        "dd MMM yyyy HH:mm", // e.g. 1 Jan 2007 15:30
        "dd MMM yyyy", // e.g. 1 Jan 2007

        "MMM dd yyyy hh:mm:ss a", // e.g. Jan 1 2007 3:30:05 PM
        "MMM dd yyyy hh:mm a", // e.g. Jan 1 2007 3:30 PM
        "MMM dd yyyy HH:mm:ss", // e.g. Jan 1 2007 15:30:05
        "MMM dd yyyy HH:mm", // e.g. Jan 1 2007 15:30
        "MMM dd yyyy", // e.g. Jan 1 2007
    };
    private static LinkedHashMap<String, SimpleDateFormat> DATE_FORMATS;

    static {
        DATE_FORMATS = new LinkedHashMap<String, SimpleDateFormat> ();
        for (String pattern : DATE_PATTERNS) {
            try {
                DATE_FORMATS.put(pattern, new SimpleDateFormat(pattern));
            } catch (Exception e) {}
        }
    }

    /**
     * Returns a Date object parsed from the first successful parsing in DATE_PATTERNS. null otherwise
     * @param s String
     * @return Date
     */
    public static Date toDate(String s) {
        for (String pattern : DATE_FORMATS.keySet()) {
            try {
                return DATE_FORMATS.get(pattern).parse(s);
            } catch (Exception e) {}
        }
        return null;
    }

    /**
     * Returns the pattern from the first successful parsing in DATE_PATTERNS.
     * null otherwise
     *
     * @param s String
     * @return Date
     */
    public static String findPattern(String s) {
        for (String pattern : DATE_FORMATS.keySet()) {
            try {
                DATE_FORMATS.get(pattern).parse(s);
                return pattern;
            } catch (Exception e) {}
        }
        return null;
    }

    /**
     * Formats the date using the given date format string
     * @param format String
     * @param date Date
     * @return String
     */
    public static String getDateStringDate(String format, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * Formats the date from the given source AmgenLocationCode timezone to the
     * target AmgenLocationCode timezone using the given format string
     * @param format String
     * @param date Date
     * @param sourceCode AmgenLocationCode
     * @param targetCode AmgenLocationCode
     * @return String
     */
    public static String getDateStringDate(String format, Date date, AmgenLocationCode sourceCode, AmgenLocationCode targetCode) {
        Calendar cal = Calendar.getInstance(AmgenLocationCode.getLocationTimeZone(sourceCode));
        cal.setTime(date);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(AmgenLocationCode.getLocationTimeZone(targetCode));
        return sdf.format(cal.getTime());
    }

    /**
     * Formats the date from the given source AmgenLocationCode timezone to the
     * target user's timezone using the given format string
     * @param format String
     * @param date Date
     * @param sourceCode AmgenLocationCode
     * @param targetUser String
     * @return String
     * @throws NamingException
     */
    public static String getDateStringDate(String format, Date date, AmgenLocationCode sourceCode, String targetUser) throws NamingException {
        Calendar cal = Calendar.getInstance(AmgenLocationCode.getLocationTimeZone(sourceCode));
        cal.setTime(date);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            sdf.setTimeZone(new AmgenEnterpriseLookup().lookupByUID(targetUser).getTimeZone());
        } catch (Exception e) {}
        return sdf.format(cal.getTime());
    }

    /**
     * Formats the date from the given source timezone to the
     * target timezone using the given format string
     * @param format String
     * @param date Date
     * @param sourceTimeZone source timezone
     * @param targetTimeZone target timezone
     * @return String
     */
    public static String getDateStringDate(String format, Date date, TimeZone sourceTimeZone, TimeZone targetTimeZone) {
        Calendar cal = Calendar.getInstance(sourceTimeZone);
        cal.setTime(date);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(targetTimeZone);
        return sdf.format(cal.getTime());
    }


}
