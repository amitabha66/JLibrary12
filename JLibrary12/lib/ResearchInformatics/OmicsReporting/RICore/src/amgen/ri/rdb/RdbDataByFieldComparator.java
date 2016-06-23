package amgen.ri.rdb;

import java.util.Comparator;

/**
 * Compares 2 RdbData items using the given fieldName
 * <p> Dynamic Relation Db Mapping</p>
 * <p>Description: </p>
 * @author Jeffrey McDowell
 * @version 1.0
 */
public class RdbDataByFieldComparator implements Comparator {
    Comparator comparator;
    String fieldName;

    public RdbDataByFieldComparator(String fieldName, Comparator comparator) {
        this(fieldName);
        this.comparator = comparator;
    }

    public RdbDataByFieldComparator(String fieldName) {
        this.fieldName = fieldName;
        this.comparator = null;
    }

    public int compare(Object obj1, Object obj2) {
        RdbData item1 = (RdbData) obj1;
        RdbData item2 = (RdbData) obj2;
        Object item1Value = item1.getFieldValue(fieldName);
        Object item2Value = item2.getFieldValue(fieldName);
        if (comparator != null) {
            return comparator.compare(item1Value, item2Value);
        }
        Comparable item1Comp = (Comparable) item1Value;
        Comparable item2Comp = (Comparable) item2Value;
        return item1Comp.compareTo(item2Comp);
    }
}
