/*
 *   SQLResultColumn
 *   Encapsulates a result column from a generic SQL
 *   $Revision: 1.1 $
 *   Created: Jeffrey McDowell, 18 Sep 2000
 *   Modified: $Author: cvs $
 *   $Log
 *
 */
package amgen.ri.rdb;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Encapsulates a result column from a generic SQL
 *   @version $Revision: 1.1 $
 *   @author Jeffrey McDowell
 *   @author $Author: cvs $
 */
public class SQLResultColumn extends Hashtable {
    private List columns;

    /**
     * Default Constructor
     */
    public SQLResultColumn(int size) {
        super(size);
        columns = new ArrayList(size);
    }

    /**
     * Adds a column/value pair
     */
    public synchronized Object put(Object key, Object value) {
        columns.add(key);
        if (value == null) {
            return super.put(key, "");
        }
        return super.put(key, value);
    }

    /** Get the name of a column */
    public String getColumnName(int index) {
        if (index < 0 || index >= columns.size()) {
            return null;
        }
        return (String) columns.get(index);
    }

    /** Get the number of columns */
    public int getColumnCount() {
        return columns.size();
    }
}
