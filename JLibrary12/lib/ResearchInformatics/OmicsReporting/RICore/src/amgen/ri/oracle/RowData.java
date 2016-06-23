package amgen.ri.oracle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author jemcdowe
 */
public class RowData implements Iterable<Object> {
  private int columnCount;
  private List<Object> results;

  public RowData(int columnCount) {
    this.columnCount= columnCount;
    this.results = new ArrayList<Object>(columnCount);
  }
  
  public int getColumnCount() {
    return columnCount;
  }
  
  public void addColumnValue(int column, Object value) {
    if (column<0 || column>=columnCount) {
      throw new ArrayIndexOutOfBoundsException("Invalid column index");
    }
    this.results.add(column, value);
  }
  
  public Object getColumnValue(int column) {
    if (column<0 || column>=columnCount) {
      throw new ArrayIndexOutOfBoundsException("Invalid column index");
    }
    if (column>= results.size()) {
      return null;
    }
    return results.get(column);
  }

  public Iterator<Object> iterator() {
    return results.iterator();
  }  
  
  
  
}
