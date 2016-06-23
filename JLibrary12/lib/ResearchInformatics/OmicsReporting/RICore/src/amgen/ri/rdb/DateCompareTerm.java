package amgen.ri.rdb;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Creates a CompareTerm which allows comparing a java.sql.Date object with a
 * Date column
 */
public class DateCompareTerm extends CompareTerm {
    /**
     * Creates a DateCompareTerm assuming the operator is '='
     *
     * @param compareName String
     * @param compareValue Date
     */
    public DateCompareTerm(String compareName, Date compareValue) {
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
    public DateCompareTerm(String compareName, String operator, Date compareValue) {
        super(compareName, operator, compareValue);
    }

    /**
     * Returns the properly formatted compare value
     *
     * @return String
     */
    public String getCompareValue() {
        SimpleDateFormat defaultTimestampConversionFormat = new SimpleDateFormat("MM dd yyyy H:m:s");
        return defaultTimestampConversionFormat.format( (Date) compareValue);
    }

    /**
     * Returns the compare term as a replacement term (e.g. <column>=?) using the TO_DATE conversion
     * @return
     */
    public String asReplacement() {
        return compareName + operator + " TO_DATE(?, 'MM DD YYYY HH24:MI:SS', 'NLS_DATE_LANGUAGE = American')";
    }

    /**
     * Returns the full compare term (e.g. <column>=<value>)
     * @return
     */
    public String toString() {
        return compareName + operator + " TO_DATE('" + compareValue + "', 'MM DD YYYY HH24:MI:SS', 'NLS_DATE_LANGUAGE = American')";
    }

}
