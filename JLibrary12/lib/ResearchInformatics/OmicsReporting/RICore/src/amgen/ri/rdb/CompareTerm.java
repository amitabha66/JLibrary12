package amgen.ri.rdb;

/**
 * Class which encapsulates a column name, operator, and compare value for use in a where
 * clause
 * @author Jeffrey McDowell
 * @version 1.0
 */
public class CompareTerm {
    protected String compareName;
    protected String operator;
    protected Object compareValue;

    /**
     * Creates a CompareTerm assuming an equivalence operator
     * @param compareName
     * @param compareValue
     */
    public CompareTerm(String compareName, Object compareValue) {
        this(compareName, "=", compareValue);
    }

    /**
     * Creates a CompareTerm providing a name (column), the compare operator
     * (e.g. '=', '>', '>=', '<', '<=', '!=', 'LIKE'), and the value
     * @param compareName
     * @param operator
     * @param compareValue
     */
    public CompareTerm(String compareName, String operator, Object compareValue) {
        this.compareName = compareName;
        this.operator = operator;
        this.compareValue = compareValue;
    }

    public String getCompareName() {
        return compareName;
    }

    public String getOperator() {
        return operator;
    }

    public String getCompareValue() {
        if (compareValue instanceof JavaDate) {
            JavaDate date = (JavaDate) compareValue;
            return date.getValue() + "";
        } else {
            return compareValue.toString();
        }
    }

    public Object getCompareValues() {
        return getCompareValue();
    }

    /**
     * Returns the compare term as a replacement term (e.g. <column>=?)
     * @return
     */
    public String asReplacement() {
        return compareName + " " + operator + " ?";
    }

    /**
     * Returns the full compare term (e.g. <column>=<value>)
     * @return
     */
    public String toString() {
        return getCompareName() + " " + operator + " '" + getCompareValue() + "'";
    }

}
