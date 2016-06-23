package amgen.ri.rdb;

public class NullCompareTerm extends CompareTerm {
    public NullCompareTerm(String compareName, boolean isNull) {
        super(compareName, (isNull ? "is" : "is not"), "null");
    }

    /**
     * Returns the compare term as a replacement term (e.g. <column>=?)
     * @return
     */
    public String asReplacement() {
        throw new IllegalArgumentException("Null compare term not valid as a replacement");
    }

    /**
     * Returns the full compare term (e.g. <column>=<value>)
     * @return
     */
    public String toString() {
        return compareName + " " + operator + " null";
    }

}
