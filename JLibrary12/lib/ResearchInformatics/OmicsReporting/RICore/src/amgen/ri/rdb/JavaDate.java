package amgen.ri.rdb;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Description: Used to wrap an Oracle persisted number as a JavaDate.
 * The date may be set by a String with a variety of acceptable formats including
 *     01/01/2007 3:30:05 PM
 *     01/01/2007 3:30 PM
 *     01/01/2007 15:30:05
 *     01/01/2007 15:30
 *     01/01/2007
 *     Jan 1, 2007 3:30:05 PM
 *     Jan 1, 2007 3:30 PM
 *     Jan 1, 2007 15:30:05
 *     Jan 1, 2007 15:30
 *     Jan 1, 2007
 *     Jan 1 2007 3:30:05 PM
 *     Jan 1 2007 3:30 PM
 *     Jan 1 2007 15:30:05
 *     Jan 1 2007 15:30
 *     Jan 1 2007
 * @author not attributable
 * @version 1.0
 */
public class JavaDate extends Number implements Comparable {
    private static String[] DATE_PATTERNS = {
        "MM/dd/yyyy hh:mm:ss a", // e.g. 01/01/2007 3:30:05 PM
        "MM/dd/yyyy hh:mm a", // e.g. 01/01/2007 3:30 PM
        "MM/dd/yyyy HH:mm:ss", // e.g. 01/01/2007 15:30:05
        "MM/dd/yyyy HH:mm", // e.g. 01/01/2007 15:30
        "MM/dd/yyyy", // e.g. 01/01/2007
        "MMM dd, yyyy hh:mm:ss a", // e.g. Jan 1, 2007 3:30:05 PM
        "MMM dd, yyyy hh:mm a", // e.g. Jan 1, 2007 3:30 PM
        "MMM dd, yyyy HH:mm:ss", // e.g. Jan 1, 2007 15:30:05
        "MMM dd, yyyy HH:mm", // e.g. Jan 1, 2007 15:30
        "MMM dd, yyyy", // e.g. Jan 1, 2007
        "MMM dd yyyy hh:mm:ss a", // e.g. Jan 1 2007 3:30:05 PM
        "MMM dd yyyy hh:mm a", // e.g. Jan 1 2007 3:30 PM
        "MMM dd yyyy HH:mm:ss", // e.g. Jan 1 2007 15:30:05
        "MMM dd yyyy HH:mm", // e.g. Jan 1 2007 15:30
        "MMM dd yyyy", // e.g. Jan 1 2007
    };
    protected long value;

    /**
     * Creates a populated field using the value as the epoch date value
     */
    public JavaDate() {
        this(new Date());
    }

    /**
     * Creates a populated field using the value as the epoch date value
     */
    public JavaDate(long value) {
        this.value = value;
    }

    /**
     * Creates a populated field attempting to parse the value. value must be one
     * of the acceptable patterns.
     */
    public JavaDate(String value) {
        this.value = parseDate(value);
    }

    /**
     * Creates a populated field using the give Date
     */
    public JavaDate(Date date) {
        this.value = date.getTime();
    }

    /**
     * Retrieves the epoch date value of the field.
     */
    public long getValue() {
        return value;
    }

    /**
     * Returns the Date as a Date object
     * @return Date
     */
    public Date getAsDate() {
        return new Date(value);
    }

    /**
     * Parses the date String by looping through the pattern list until the
     * first one which doesn't generate an exception is found
     * @param date String
     * @return long
     */
    private long parseDate(String date) {
        long value = 0;
        for (int i = 0; i < DATE_PATTERNS.length; i++) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERNS[i]);
                value = formatter.parse(date).getTime();
                if (value != 0) {
                    //System.out.println("Used: "+DATE_PATTERNS[i]);
                    break;
                }
            } catch (Exception e) {
            }
        }
        if (value == 0) {
            throw new IllegalArgumentException("Invalid date value");
        }
        return value;
    }

    //Implementations from Number

    /**
     * Returns the value of the specified number as an <code>int</code>.
     * This may involve rounding.
     */
    public int intValue() {
        return (int) getValue();
    }

    /**
     * Returns the value of the specified number as a <code>long</code>.
     */
    public long longValue() {
        return getValue();
    }

    /**
     * Returns the value of the specified number as a <code>float</code>.
     */
    public float floatValue() {
        return (float) getValue();
    }

    /**
     * Returns the value of the specified number as a <code>double</code>.
     */
    public double doubleValue() {
        return (double) getValue();
    }

    /**
     * Returns the compare terms which will enclose the full day reresented by this date
     * @param fieldName String
     * @return CompareTerm[]
     */
    public CompareTerm[] getFullDayCompareTerms(String fieldName) {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(getAsDate());
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(getAsDate());
        endCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endCalendar.set(Calendar.MINUTE, 59);

        return new CompareTerm[] {
            new CompareTerm(fieldName, ">=", startCalendar.getTimeInMillis() + ""),
            new CompareTerm(fieldName, "<=", endCalendar.getTimeInMillis() + "")
        };
    }

    /**
     * Returns the date using the given format
     * @param pattern String
     * @return String
     */
    public String toString(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(getAsDate());
    }

    /**
     * Returns the date using the given format OR null if the date is zero
     * @param pattern String
     * @return String
     */
    public String toStringOrNull(String pattern) {
        if (getValue() == 0) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(getAsDate());
    }

    public String toString() {
        return getValue() + "";
    }

    public int compareTo(Object obj) {
        JavaDate d1 = this;
        JavaDate d2 = (JavaDate) obj;
        return Double.compare(d1.getValue(), d2.getValue());
    }

    /**
     * Returns an instance of this class parsing the given String value. value must be
     * one of the acceptabel patterns
     */
    public static JavaDate valueOf(String value) {
        JavaDate javaDate = new JavaDate(value);
        return javaDate;
    }

    public static void main(String[] args) {
        try {
            String d = "Jan 1 2007 1:00:09 pm";

            JavaDate j = valueOf(d);
            System.out.println(d + "\n" + j.getAsDate());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
