package amgen.ri.rdb;

import java.util.regex.Pattern;

/**
 * <p>Title: Dynamic Relation Db Mapping</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class CatSearchCompareTerm extends CompareTerm {
    private Pattern orReplacePattern = Pattern.compile("\\sor\\s", Pattern.CASE_INSENSITIVE);
    private Pattern andReplacePattern = Pattern.compile("\\sand\\s", Pattern.CASE_INSENSITIVE);
    private Pattern notReplacePattern = Pattern.compile("\\snot\\s", Pattern.CASE_INSENSITIVE);
    /**
     * Creates a CompareTerm assuming an equivalence operator
     * @param compareName
     * @param compareValue
     */
    public CatSearchCompareTerm(String compareName, Object compareValue) {
        super(compareName, "=", compareValue);
    }

    /**
     * Returns the compare term as a replacement term (e.g. <column>=?)
     * @return
     */
    public String asReplacement() {
        return "CATSEARCH(" + getCompareName() + ", ?, '')>0";
    }

    /**
     * Returns the full compare term (e.g. <column>=<value>)
     * @return
     */
    public String toString() {
        return "CATSEARCH(" + getCompareName() + ", '" + getCompareValue() + "', '')>0";
    }

    public String getCompareValue() {
        String value = super.getCompareValue();
        //Replace the "OR", "AND", and "NOT" words with the proper syntax ("|", "", "-", resp.)
        value = orReplacePattern.matcher(value).replaceAll(" | ");
        value = andReplacePattern.matcher(value).replaceAll(" ");
        value = notReplacePattern.matcher(value).replaceAll(" - ");
        return value;
    }

}
