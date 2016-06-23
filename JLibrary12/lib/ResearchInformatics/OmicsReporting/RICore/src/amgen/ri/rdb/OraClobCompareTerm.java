package amgen.ri.rdb;

/**
 * A CompareTerm used specifically for Oracle CLOB columns by implementing the
 *  DBMS_LOB.COMPARE function
 * @author Jeffrey McDowell
 * @version 1.0
 */

public class OraClobCompareTerm extends CompareTerm {
    /**
     * Creates a CompareTerm assuming an equivalence operator
     * @param compareName
     * @param compareValue
     */
    public OraClobCompareTerm(String compareName, Object compareValue) {
        super(compareName, compareValue);
    }

    /**
     * Returns the compare term as a replacement term (e.g. <column>=?)
     * @return
     */
    public String asReplacement() {
        return "DBMS_LOB.COMPARE(" + getCompareName() + ",?)=0";
    }

    /**
     * Returns the full compare term (e.g. <column>=<value>)
     * @return
     */
    public String toString() {
        return "DBMS_LOB.COMPARE(" + getCompareName() + ",'" + getCompareValue() + "')=0";
    }

}
