package amgen.ri.rdb;

import java.util.HashMap;
import java.util.Map;

import amgen.ri.util.ExtString;

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
public class ContainsCompareTerm extends CompareTerm {

    private Map<String, String> withinSections;

    /**
     * Creates a CompareTerm assuming an equivalence operator
     * @param compareName
     * @param compareValue
     */
    public ContainsCompareTerm(String compareName, Object compareValue) {
        super(compareName, "=", compareValue);
        withinSections = new HashMap<String, String> ();
    }

    public String getCompareValue() {
        StringBuffer sb = new StringBuffer();
        if (compareValue != null && ExtString.hasLength(compareValue.toString())) {
            sb.append(compareValue);
        }
        if (withinSections.size() > 0) {
            for (String section : withinSections.keySet()) {
                if (sb.length() > 0) {
                    sb.append(" and ");
                }
                sb.append(section + " within " + withinSections.get(section));
            }
        }
        return sb.toString();
    }

    /**
     * Returns the compare term as a replacement term (e.g. <column>=?)
     * @return
     */
    public String asReplacement() {
        return "contains(" + getCompareName() + ", ?)>0";
    }

    /**
     * Returns the full compare term (e.g. <column>=<value>)
     * @return
     */
    public String toString() {
        return "contains(" + getCompareName() + ", '" + getCompareValue() + "')>0";
    }

}
