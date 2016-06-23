package amgen.ri.rdb;

import java.util.Arrays;
import java.util.List;

import amgen.ri.util.ExtString;

/**
 * <p>Title: Dynamic Relation Db Mapping</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2009</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class InCompareTerm extends CompareTerm {
    public InCompareTerm(String compareName, List<String> compareValues) {
        super(compareName, "IN", compareValues);
    }

    public InCompareTerm(String compareName, String[] compareValues) {
        this(compareName, Arrays.asList(compareValues));
    }

    /**
     * Returns the compare term as a replacement term (e.g. <column>=?)
     * @return
     */
    public String asReplacement() {
        List<String> values = (List<String>) compareValue;
        return compareName + " IN (" + ExtString.repeat("?", values.size(), ",") + ")";
    }

    public Object getCompareValues() {
        return (List<String>) compareValue;
    }

    /**
     * Returns the full compare term (e.g. <column>=<value>)
     * @return
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        List<String> values = (List<String>) compareValue;
        for (String value : values) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append("'" + value.replace('\'', ' ') + "'");
        }
        return compareName + " IN (" + sb + ")";
    }

}
