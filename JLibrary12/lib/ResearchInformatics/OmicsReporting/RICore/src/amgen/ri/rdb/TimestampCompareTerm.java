package amgen.ri.rdb;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Creates a CompareTerm which allows comparing a java.sql.Timestamp object with a
 * Timestamp column
 */
public class TimestampCompareTerm extends CompareTerm {
    /**
     * Creates a TimestampCompareTerm assuming the operator is '='
     *
     * @param compareName String
     * @param compareValue Date
     */
    public TimestampCompareTerm(String compareName, Timestamp compareValue) {
        super(compareName, compareValue);
    }

    /**
     * Creates a DateCompareTerm for comparing a java.sql.Date object with a
     * Date column. Operator can be '=', '>', '>=', '<', '<=', '!='
     *
     * @param compareName String
     * @param operator String
     * @param compareValue Date
     */
    public TimestampCompareTerm(String compareName, String operator, Timestamp compareValue) {
        super(compareName, operator, compareValue);
    }

    /**
     * Returns the properly formatted compare value
     *
     * @return String
     */
    public String getCompareValue() {
        SimpleDateFormat defaultTimestampConversionFormat = new SimpleDateFormat("MM dd yyyy H:m:s");
        return defaultTimestampConversionFormat.format( (Timestamp) compareValue);
    }

    /**
     * Returns the compare term as a replacement term (e.g. <column>=?) using the TO_TIMESTAMP conversion
     * @return
     */
    public String asReplacement() {
        return compareName + operator + " TO_TIMESTAMP (?, 'MM DD YYYY HH24:MI:SS', 'NLS_DATE_LANGUAGE = American')";
    }

    /**
     * Returns the full compare term (e.g. <column>=<value>)
     * @return
     */
    public String toString() {
        return compareName + operator + " TO_DATE('" + compareValue + "', 'MM DD YYYY HH24:MI:SS', 'NLS_DATE_LANGUAGE = American')";
    }

}
