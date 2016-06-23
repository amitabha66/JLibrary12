package amgen.ri.rdb;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import amgen.ri.oracle.OraSQLManager;
import amgen.ri.util.ExtString;

@SuppressWarnings("unchecked")
public class RdbDataArrayCTXSearch extends RdbData {
    public static final short EXPAND_ALL_TEXT_LEFT = -1;
    public static final short EXPAND_ALL_TEXT_BOTH = 0;
    public static final short EXPAND_ALL_TEXT_RIGHT = 1;

    protected RdbData[] ctxDataItems;
    private RdbDataArrayCTXList dataArray;
    private Class regRdbClass;
    private String sectionsColumn;
    private Map fieldValues;
    private boolean useOracleSyntax = false;
    private int maxRowsReturned = 0;
    private String orderByScoreDirection = "DESC";

    public RdbDataArrayCTXSearch() {
        super();
    }

    public RdbDataArrayCTXSearch(Class regRdbClass, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        this(regRdbClass, null, null, null, sqlManager, logonusername, connectionPool);
    }

    public RdbDataArrayCTXSearch(Class regRdbClass, String sectionsColumn, Map fieldValues, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        this(regRdbClass, sectionsColumn, fieldValues, null, sqlManager, logonusername, connectionPool);
    }

    public RdbDataArrayCTXSearch(Class regRdbClass, Map fieldValues, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        this(regRdbClass, null, fieldValues, null, sqlManager, logonusername, connectionPool);
    }

    public RdbDataArrayCTXSearch(Class regRdbClass, String sectionsColumn, String allFieldSearchValue, SQLManagerIF sqlManager, String logonusername,
                                 String connectionPool) {
        this(regRdbClass, sectionsColumn, null, allFieldSearchValue, sqlManager, logonusername, connectionPool);
    }

    public RdbDataArrayCTXSearch(Class regRdbClass, String allFieldSearchValue, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        this(regRdbClass, null, null, allFieldSearchValue, sqlManager, logonusername, connectionPool);
    }

    public RdbDataArrayCTXSearch(Class regRdbClass, Map fieldValues, String allFieldSearchValue, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        this(regRdbClass, null, fieldValues, allFieldSearchValue, sqlManager, logonusername, connectionPool);
    }

    public RdbDataArrayCTXSearch(Class regRdbClass, String sectionsColumn, Map fieldValues, String allFieldSearchValue, SQLManagerIF sqlManager, String logonusername,
                                 String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        this.regRdbClass = regRdbClass;
        Register.getRegister().register(regRdbClass);
        this.sectionsColumn = sectionsColumn;
        if (sectionsColumn == null) {
            this.sectionsColumn = setSectionsColumn();
        }
        this.fieldValues = fieldValues;
        if (this.fieldValues == null) {
            this.fieldValues = new HashMap();
        }
        if (allFieldSearchValue != null) {
            this.fieldValues.put(null, allFieldSearchValue);
        }
    }

    /**
     * Returns the Sections for the Table/RdbData class
     * @return
     * @throws SQLException
     */
    public String[] getSections() throws SQLException {
        List sections = new ArrayList();
        try {
            RdbData regRdbObj = getInternalRdbDataObject();
            String tableName = regRdbObj.getTableNameForSQL();
            ResultSet rset = getSQLManager().executeQuery(
                "SELECT SEC_NAME FROM CTXSYS.DR$SECTION_GROUP GR,CTXSYS.DR$SECTION SEC " +
                "WHERE GR.SGP_ID=SEC.SEC_SGP_ID AND SGP_NAME=?", tableName.toUpperCase(), getConnectionPool());
            while (rset.next()) {
                sections.add(rset.getString(1));
            }
            OraSQLManager.closeResources(rset);
        } catch (Exception ex) {
        }
        return (String[]) sections.toArray(new String[0]);
    }

    /**
     * A required method which returns the primary key(s) of the table/RdbData class. If multi-column, use CSV format
     * @return
     */
    public String getIdentifier() {
        return null;
    }

    /**
     * This method is required EXACTLY as written to allow the RdbData architecture access to the class variables.
     */
    protected void setFieldValue(Field field, Object value) throws IllegalAccessException {
        field.set(this, value);
    }

    /**
     * This method is required EXACTLY as written to allow the RdbData architecture access to the class variables.
     */
    protected Object getFieldValue(Field field) throws IllegalAccessException {
        return field.get(this);
    }

    /**
     * Returns the component class of an array to be populated. If null, it determines by introspection
     */
    public Class getArrayFieldComponentClass(String fieldName) {
        return regRdbClass;
    }

    /**
     * Returns the identifier for the array using the given ResultSet at the current cursor position.
     * The default implementation returns all queried columns in the populating SQL statement as
     * a CSV string. E.g. SELECT ID1,ID2 FROM TABLE => "ID1,ID2"
     * This may be overridden to use the columns returned from a SQL statement which populates a member array
     * differently.
     * @param rset the populating SQL statement's ResultSet at the current array's cursor position (i.e. next() has been called
     * on the ResultSet
     * @return the value to be used as the primary key for the RdbData array member
     * @throws SQLException if any problems occur while reading the ResultSet
     */
    protected String getArrayIdentifier(ResultSet rset) throws SQLException {
        String id = rset.getString(1);
        if (id == null) {
            return null;
        }
        StringBuffer identifier = new StringBuffer(id);
        int columnCount = rset.getMetaData().getColumnCount();
        //The last column is the score, not a primary key!!
        columnCount--;

        for (int i = 2; i <= columnCount; i++) {
            identifier.append("," + rset.getString(i));
        }
        return identifier.toString();
    }

    protected void arrayElementSet(RdbData rdbData, ResultSet arrayRset) {
        try {
            double score = arrayRset.getDouble("SCORE");
            rdbData.setTransientData("score", new Double(score));
        } catch (Exception e) {}
    }

    /**
     * Sets the array item in this RdbData array class using the RdbData class machinery. The dataItems array is
     * then put into an ArrayList which is the actual container. The dataItems array is set back to null to allow the
     * garbage collector to remove it.
     */
    public boolean setData() {
        if (!dataSet) {
            dataSet = super.setData();
            if (!dataSet) {
                return false;
            }
            RdbData[] ctxDataItems = (RdbData[]) get("ctxDataItems");
            dataArray = new RdbDataArrayCTXList(ctxDataItems);
            set("ctxDataItems", null);
        }
        return dataSetSuccess;
    }

    public void useOracleSyntax(boolean useOracleSyntax) {
        this.useOracleSyntax = useOracleSyntax;
    }

    public void setMaxRowsReturned(int maxRowsReturned) {
        this.maxRowsReturned = maxRowsReturned;
    }

    public void setOrderByScoreAscending() {
        orderByScoreDirection = "ASC";
    }

    public void setOrderByScoreDescending() {
        orderByScoreDirection = "DESC";
    }

    public void setNoOrderByScore() {
        orderByScoreDirection = null;
    }

    public void setSectionsColumn(String sectionsColumn) {
        this.sectionsColumn = sectionsColumn;
    }

    protected String getMemberSQL(String fieldName) {
        return generateQuery();
    }

    public RdbData getInternalRdbDataObject() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        return (RdbData) regRdbClass.getConstructor( (Class[])null).newInstance( (Object[])null);
    }

    private String setSectionsColumn() {
        try {
            Hashtable fieldTable = Register.getRegister().getFieldTable(regRdbClass.getName());
            Iterator fieldNames = Register.getRegister().getFieldNames(regRdbClass.getName());
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next().toString();
                Integer fieldType = (Integer) fieldTable.get(fieldName);
                if (fieldType.intValue() == Register.CLOBDATA) {
                    return fieldName;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String generateQuery() {
        try {
            RdbData regRdbObj = getInternalRdbDataObject();

            String primaryKeyList = null;
            String[] primaryKeys = regRdbObj.getPrimaryKeyFields();
            if (primaryKeys == null) {
                String primaryField = Register.getRegister().getFieldName(regRdbClass.getName(), 0);
                if (primaryField != null) {
                    primaryKeys = new String[] {
                        primaryField};
                }
            }
            if (primaryKeys == null || primaryKeys.length == 0) {
                throw new IllegalArgumentException("Unable to generate primary key list for SELECT statement");
            }
            if (sectionsColumn == null) {
                throw new IllegalArgumentException("No sections column");
            }
            primaryKeyList = ExtString.join(primaryKeys, ',');

            StringBuffer sectionsContains = new StringBuffer();

            for (Iterator keyIter = fieldValues.keySet().iterator(); keyIter.hasNext(); ) {
                String fieldSectionName = (String) keyIter.next();
                String fieldSectionValue = (String) fieldValues.get(fieldSectionName);
                if (fieldSectionName == null || fieldSectionName.length() == 0 || fieldSectionValue == null || fieldSectionValue.length() == 0) {
                    continue;
                }
                if (sectionsContains.length() > 0) {
                    sectionsContains.append(" AND ");
                }
                if (!useOracleSyntax) {
                    fieldSectionValue = escapeTextQuery(fieldSectionValue);
                }
                sectionsContains.append("(" + fieldSectionValue + ") WITHIN " + fieldSectionName);
            }
            String allTextField = (String) fieldValues.get(null);
            if (allTextField != null) {
                if (sectionsContains.length() > 0) {
                    sectionsContains.append(" AND ");
                }
                if (!useOracleSyntax) {
                    allTextField = escapeTextQuery(allTextField);
                }
                sectionsContains.append("(" + allTextField + ")");
            }
            if (sectionsContains.length() == 0) {
                throw new IllegalArgumentException("No search fields provided");
            }
            String selectSQL = "SELECT " + primaryKeyList + ", SCORE(1) \"SCORE\"";
            selectSQL += " FROM " + regRdbObj.getTableNameForSQL() + " WHERE CONTAINS(" + sectionsColumn + ",'" + sectionsContains + "',1)>0 ";
            if (maxRowsReturned > 0) {
                selectSQL += " AND ROWNUM<=" + maxRowsReturned;
            }
            if (orderByScoreDirection != null) {
                selectSQL += " ORDER BY SCORE(1) " + orderByScoreDirection;
            }
            return selectSQL;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Escapes all Oracle Text query reserved characters in the raw query EXCEPT '*'
     * which is replaced by '%' to mimic usual wildcarding
     * @param raw
     * @return
     */
    private String escapeTextQuery(String raw) {
        if (raw == null) {
            return null;
        }
        Pattern[] escapePatterns = new Pattern[] {
            Pattern.compile("\\\\"),
            Pattern.compile(","),
            Pattern.compile("&"),
            Pattern.compile("\\?"),
            Pattern.compile("-"),
            Pattern.compile(";"),
            Pattern.compile("~"),
            Pattern.compile("\\|"),
            Pattern.compile("\\$"),
            Pattern.compile("!"),
            Pattern.compile(">"),
            Pattern.compile("%"),
            Pattern.compile("_"),
            Pattern.compile("\\{"),
            Pattern.compile("\\}"),
            Pattern.compile("\\("),
            Pattern.compile("\\)"),
            Pattern.compile("\\["),
            Pattern.compile("\\]"),
        };
        for (int i = 0; i < escapePatterns.length; i++) {
            Pattern p = escapePatterns[i];
            String regex = p.pattern();
            char character = regex.charAt(regex.length() - 1);
            raw = p.matcher(raw).replaceAll("\\\\" + character);
        }
        return Pattern.compile("\\*").matcher(raw).replaceAll("%");
    }

    private String getExpandedAllTextSearchTerm(String term, short expandType) {
        if (term == null || term.trim().length() == 0) {
            return "";
        }
        String nonExpandableRegex = "[(\\W|_)&&[^\\s]]";
        String nonExpandableWordRegex = "\\b(WITHIN|EQUIV|MINUS|AND|OR|NOT)\\b";
        Pattern nonExpandablePattern = Pattern.compile(nonExpandableRegex);
        Pattern nonExpandableWordPattern = Pattern.compile(nonExpandableWordRegex, Pattern.CASE_INSENSITIVE);

        if (nonExpandablePattern.matcher(term).find()) {
            return term;
        }
        StringTokenizer wordTokenizer = new StringTokenizer(term);
        StringBuffer updatedAllTextSearchTerm = new StringBuffer();
        int wordCount = wordTokenizer.countTokens();
        for (int i = 0; i < wordCount; i++) {
            String word = wordTokenizer.nextToken();
            if (nonExpandableWordPattern.matcher(word).find()) {
                updatedAllTextSearchTerm.append(word);
            } else {
                switch (expandType) {
                    case (EXPAND_ALL_TEXT_LEFT):
                        updatedAllTextSearchTerm.append("%" + word);
                        break;
                    case (EXPAND_ALL_TEXT_BOTH):
                        updatedAllTextSearchTerm.append("%" + word + "%");
                        break;
                    case (EXPAND_ALL_TEXT_RIGHT):
                        updatedAllTextSearchTerm.append(word + "%");
                        break;
                    default:
                        updatedAllTextSearchTerm.append(word);
                        break;
                }
            }
            updatedAllTextSearchTerm.append(" ");
        }
        return updatedAllTextSearchTerm.toString();
    }

    /**
     * Returns the CTX results as a RdbDataArray
     * @return
     */
    public RdbDataArray getAsRdbDataArray() {
        RdbData[] rdbData = (RdbData[]) getArrayList().toArray(new RdbData[0]);
        return new RdbDataArray(rdbData, getSQLManager(), getLogonUsername(), getConnectionPool());
    }

    /**
     * Returns the raw array list object. Only used internally in the object
     */
    private RdbDataArrayCTXList getArrayList() {
        setData();
        return dataArray;
    }

    /*
     * THE REMAINDER OF THE CODE INVOLVES IMPLEMENTING THE LIST INTERFACE
     */
    /**
     * Returns the number of elements in this list.  If this list contains
     * more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of elements in this list.
     */
    public int size() {
        return getArrayList().size();
    }

    /**
     * Returns <tt>true</tt> if this list contains no elements.
     *
     * @return <tt>true</tt> if this list contains no elements.
     */
    public boolean isEmpty() {
        return getArrayList().isEmpty();
    }

    /**
     *
     * Returns <tt>true</tt> if this list contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this list contains
     * at least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this list is to be tested.
     * @return <tt>true</tt> if this list contains the specified element.
     */
    public boolean contains(Object o) {
        return getArrayList().contains(o);
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     *
     * @return an iterator over the elements in this list in proper sequence.
     */
    public Iterator iterator() {
        return getArrayList().iterator();
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence.  Obeys the general contract of the
     * <tt>Collection.toArray</tt> method.
     *
     * @return an array containing all of the elements in this list in proper
     *	       sequence.
     * @see Arrays#asList(Object[])
     */
    public Object[] toArray() {
        return getArrayList().toArray();
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence; the runtime type of the returned array is that of the
     * specified array.  Obeys the general contract of the
     * <tt>Collection.toArray(Object[])</tt> method.
     *
     * @param a the array into which the elements of this list are to
     *		be stored, if it is big enough; otherwise, a new array of the
     * 		same runtime type is allocated for this purpose.
     * @return  an array containing the elements of this list.
     *
     * @throws ArrayStoreException if the runtime type of the specified array
     * 		  is not a supertype of the runtime type of every element in
     * 		  this list.
     */
    public Object[] toArray(Object a[]) {
        return getArrayList().toArray(a);
    }

    // Modification Operations

    /**
     * Appends the specified element to the end of this list <p>
     *
     * @param o element to be appended to this list.
     * @return <tt>true</tt> (as per the general contract of the
     *            <tt>Collection.add</tt> method).
     *
     * @throws ClassCastException if the class of the specified element
     * 		  prevents it from being added to this list.
     * @throws IllegalArgumentException if some aspect of this element
     *            prevents it from being added to this collection.
     */
    public boolean add(Object o) {
        return getArrayList().add(o);
    }

    /**
     * Removes the first occurrence in this list of the specified element
     * If this list does not contain the element, it is
     * unchanged.  More formally, removes the element with the lowest index i
     * such that <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt> (if
     * such an element exists).
     *
     * @param o element to be removed from this list, if present.
     * @return <tt>true</tt> if this list contained the specified element.
     *
     *
     */
    public boolean remove(Object o) {
        return getArrayList().remove(o);
    }

    // Bulk Modification Operations

    /**
     *
     * Returns <tt>true</tt> if this list contains all of the elements of the
     * specified collection.
     *
     * @param c collection to be checked for containment in this list.
     * @return <tt>true</tt> if this list contains all of the elements of the
     * 	       specified collection.
     *
     * @see #contains(Object)
     */
    public boolean containsAll(Collection c) {
        return getArrayList().containsAll(c);
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the specified
     * collection's iterator.  The behavior of this
     * operation is unspecified if the specified collection is modified while
     * the operation is in progress.  (Note that this will occur if the
     * specified collection is this list, and it's nonempty.)
     *
     * @param c collection whose elements are to be added to this list.
     * @return <tt>true</tt> if this list changed as a result of the call.
     *
     * @throws ClassCastException if the class of an element in the specified
     * 	       collection prevents it from being added to this list.
     *
     * @throws IllegalArgumentException if some aspect of an element in the
     *         specified collection prevents it from being added to this
     *         list.
     *
     * @see #add(Object)
     */
    public boolean addAll(Collection c) {
        return getArrayList().addAll(c);
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * list at the specified position.  Shifts the
     * element currently at that position (if any) and any subsequent
     * elements to the right (increases their indices).  The new elements
     * will appear in this list in the order that they are returned by the
     * specified collection's iterator.  The behavior of this operation is
     * unspecified if the specified collection is modified while the
     * operation is in progress.  (Note that this will occur if the specified
     * collection is this list, and it's nonempty.)
     *
     * @param index index at which to insert first element from the specified
     *	            collection.
     * @param c elements to be inserted into this list.
     * @return <tt>true</tt> if this list changed as a result of the call.
     *
     * @throws ClassCastException if the class of one of elements of the
     * 		  specified collection prevents it from being added to this
     * 		  list.
     * @throws IllegalArgumentException if some aspect of one of elements of
     *		  the specified collection prevents it from being added to
     *		  this list.
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *		  &lt; 0 || index &gt; size()).
     */
    public boolean addAll(int index, Collection c) {
        return getArrayList().addAll(index, c);
    }

    /**
     * Removes from this list all the elements that are contained in the
     * specified collection.
     *
     * @param c collection that defines which elements will be removed from
     *          this list.
     * @return <tt>true</tt> if this list changed as a result of the call.
     *
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean removeAll(Collection c) {
        return getArrayList().removeAll(c);
    }

    /**
     * Retains only the elements in this list that are contained in the
     * specified collection.  In other words, removes from this list all the
     * elements that are not contained in the specified collection.
     *
     * @param c collection that defines which elements this set will retain.
     *
     * @return <tt>true</tt> if this list changed as a result of the call.
     *
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean retainAll(Collection c) {
        return getArrayList().retainAll(c);
    }

    /**
     * Removes all of the elements from this list.  This
     * list will be empty after this call returns (unless it throws an
     * exception).
     */
    public void clear() {
        getArrayList().clear();
    }

    // Comparison and hashing

    /**
     * Compares the specified object with this list for equality.  Returns
     * <tt>true</tt> if and only if the specified object is also a list, both
     * lists have the same size, and all corresponding pairs of elements in
     * the two lists are <i>equal</i>.  (Two elements <tt>e1</tt> and
     * <tt>e2</tt> are <i>equal</i> if <tt>(e1==null ? e2==null :
     * e1.equals(e2))</tt>.)  In other words, two lists are defined to be
     * equal if they contain the same elements in the same order.  This
     * definition ensures that the equals method works properly across
     * different implementations of the <tt>List</tt> interface.
     *
     * @param o the object to be compared for equality with this list.
     * @return <tt>true</tt> if the specified object is equal to this list.
     */
    public boolean equals(Object o) {
        return getArrayList().equals(o);
    }

    /**
     * Returns the hash code value for this list.  The hash code of a list
     * is defined to be the result of the following calculation:
     * <pre>
     *  hashCode = 1;
     *  Iterator i = list.iterator();
     *  while (i.hasNext()) {
     *      Object obj = i.next();
     *      hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
     *  }
     * </pre>
     * This ensures that <tt>list1.equals(list2)</tt> implies that
     * <tt>list1.hashCode()==list2.hashCode()</tt> for any two lists,
     * <tt>list1</tt> and <tt>list2</tt>, as required by the general
     * contract of <tt>Object.hashCode</tt>.
     *
     * @return the hash code value for this list.
     * @see Object#hashCode()
     * @see Object#equals(Object)
     * @see #equals(Object)
     */
    public int hashCode() {
        return getArrayList().hashCode();
    }

    // Positional Access Operations

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of element to return.
     * @return the element at the specified position in this list.
     *
     * @throws IndexOutOfBoundsException if the index is out of range (index
     * 		  &lt; 0 || index &gt;= size()).
     */
    public Object get(int index) {
        return getArrayList().get(index);
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element.
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     *
     * @throws    ClassCastException if the class of the specified element
     * 		  prevents it from being added to this list.
     * @throws    IllegalArgumentException if some aspect of the specified
     *		  element prevents it from being added to this list.
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt;= size()).  */
    public Object set(int index, Object element) {
        return getArrayList().set(index, element);
    }

    /**
     * Inserts the specified element at the specified position in this list.
     * Shifts the element currently at that position
     * (if any) and any subsequent elements to the right (adds one to their
     * indices).
     *
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     *
     * @throws    ClassCastException if the class of the specified element
     * 		  prevents it from being added to this list.
     * @throws    IllegalArgumentException if some aspect of the specified
     *		  element prevents it from being added to this list.
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public void add(int index, Object element) {
        getArrayList().add(index, element);
    }

    /**
     * Removes the element at the specified position in this list. Shifts any
     * subsequent elements to the left (subtracts one
     * from their indices).  Returns the element that was removed from the
     * list.
     *
     * @param index the index of the element to removed.
     * @return the element previously at the specified position.
     *
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public Object remove(int index) {
        return getArrayList().remove(index);
    }

    // Search Operations

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     * More formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     *
     * @param o element to search for.
     * @return the index in this list of the first occurrence of the specified
     * 	       element, or -1 if this list does not contain this element.
     */
    public int indexOf(Object o) {
        return getArrayList().indexOf(o);
    }

    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element.
     * More formally, returns the highest index <tt>i</tt> such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     *
     * @param o element to search for.
     * @return the index in this list of the last occurrence of the specified
     * 	       element, or -1 if this list does not contain this element.
     */
    public int lastIndexOf(Object o) {
        return getArrayList().lastIndexOf(o);
    }

    // List Iterators

    /**
     * Returns a list iterator of the elements in this list (in proper
     * sequence).
     *
     * @return a list iterator of the elements in this list (in proper
     * 	       sequence).
     */
    public ListIterator listIterator() {
        return getArrayList().listIterator();
    }

    /**
     * Returns a list iterator of the elements in this list (in proper
     * sequence), starting at the specified position in this list.  The
     * specified index indicates the first element that would be returned by
     * an initial call to the <tt>next</tt> method.  An initial call to
     * the <tt>previous</tt> method would return the element with the
     * specified index minus one.
     *
     * @param index index of first element to be returned from the
     *		    list iterator (by a call to the <tt>next</tt> method).
     * @return a list iterator of the elements in this list (in proper
     * 	       sequence), starting at the specified position in this list.
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *         &lt; 0 || index &gt; size()).
     */
    public ListIterator listIterator(int index) {
        return getArrayList().listIterator(index);
    }

    // View

    /**
     * Returns a view of the portion of this list between the specified
     * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive.  (If
     * <tt>fromIndex</tt> and <tt>toIndex</tt> are equal, the returned list is
     * empty.)  The returned list is backed by this list, so changes in the
     * returned list are reflected in this list, and vice-versa.  The returned
     * list supports all of the optional list operations supported by this
     * list.<p>
     *
     * This method eliminates the need for explicit range operations (of
     * the sort that commonly exist for arrays).   Any operation that expects
     * a list can be used as a range operation by passing a subList view
     * instead of a whole list.  For example, the following idiom
     * removes a range of elements from a list:
     * <pre>
     *	    list.subList(from, to).clear();
     * </pre>
     * Similar idioms may be constructed for <tt>indexOf</tt> and
     * <tt>lastIndexOf</tt>, and all of the algorithms in the
     * <tt>Collections</tt> class can be applied to a subList.<p>
     *
     * The semantics of this list returned by this method become undefined if
     * the backing list (i.e., this list) is <i>structurally modified</i> in
     * any way other than via the returned list.  (Structural modifications are
     * those that change the size of this list, or otherwise perturb it in such
     * a fashion that iterations in progress may yield incorrect results.)
     *
     * @param fromIndex low endpoint (inclusive) of the subList.
     * @param toIndex high endpoint (exclusive) of the subList.
     * @return a view of the specified range within this list.
     *
     * @throws IndexOutOfBoundsException for an illegal endpoint index value
     *     (fromIndex &lt; 0 || toIndex &gt; size || fromIndex &gt; toIndex).
     */
    public List subList(int fromIndex, int toIndex) {
        return getArrayList().subList(fromIndex, toIndex);
    }

    /**
     * Returns a List of Strings of all field values for the given RdbData class.
     * This is done by a direct query to the database- not by instantiating
     * RdbData classes
     * @param regRdbClass
     * @param fieldName
     * @param sqlManager
     * @param logonusername
     * @param connectionPool
     * @return
     */
    public static List getFieldArray(Class regRdbClass, String fieldName, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        List fieldValues = new ArrayList();
        try {
            RdbData regRdbObj = (RdbData) regRdbClass.getConstructor( (Class[])null).newInstance( (Object[])null);
            String tableName = regRdbObj.getTableNameForSQL();
            String selectSQL = "SELECT " + fieldName + " FROM " + tableName;
            ResultSet rset = sqlManager.executeQuery(selectSQL, connectionPool);
            while (rset.next()) {
                fieldValues.add(rset.getString(1));
            }
            RdbData.closeResources(rset);
            return fieldValues;
        } catch (Exception e) {
            e.printStackTrace();
            fieldValues.clear();
        }
        return fieldValues;
    }

    /**
     * Returns a List of String arrays of all field values for the given RdbData class.
     * This is done by a direct query to the database- not by instantiating
     * RdbData classes
     * @param regRdbClass
     * @param fieldName
     * @param sqlManager
     * @param logonusername
     * @param connectionPool
     * @return
     */
    public static List getFieldArray(Class regRdbClass, String[] fieldNames, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        List fieldValues = new ArrayList();
        try {
            RdbData regRdbObj = (RdbData) regRdbClass.getConstructor( (Class[])null).newInstance( (Object[])null);
            String tableName = regRdbObj.getTableNameForSQL();
            StringBuffer selectSQL = new StringBuffer("SELECT ");
            for (int i = 0; i < fieldNames.length; i++) {
                if (i > 0) {
                    selectSQL.append(',');
                }
                selectSQL.append(fieldNames[i]);
            }
            selectSQL.append(" FROM " + tableName);
            ResultSet rset = sqlManager.executeQuery(selectSQL.toString(), connectionPool);
            while (rset.next()) {
                String[] values = new String[fieldNames.length];
                for (int i = 1; i <= fieldNames.length; i++) {
                    values[i - 1] = rset.getString(i);
                }
                fieldValues.add(values);
            }
            RdbData.closeResources(rset);
            return fieldValues;
        } catch (Exception e) {
            e.printStackTrace();
            fieldValues.clear();
        }
        return fieldValues;
    }

}

class RdbDataArrayCTXList extends AbstractList implements List, Cloneable {
    /**
     * The array buffer into which the elements of the ArrayList are stored.
     * The capacity of the ArrayList is the length of this array buffer.
     */
    private transient RdbData elementData[];

    /**
     * The size of the ArrayList (the number of elements it contains).
     */
    private int size;

    /**
     * Constructs an empty list with the default (10) initial capacity.
     */
    public RdbDataArrayCTXList() {
        super();
        this.elementData = new RdbData[10];
    }

    /**
     * Constructs a list with the specified objects.
     */
    public RdbDataArrayCTXList(RdbData[] rdbData) {
        super();
        if (rdbData == null) {
            size = 0;
            this.elementData = new RdbData[10];
        } else {
            size = rdbData.length;
            this.elementData = rdbData;
        }
    }

    /**
     * Trims the capacity of this <tt>ArrayList</tt> instance to be the
     * list's current size.  An application can use this operation to minimize
     * the storage of an <tt>ArrayList</tt> instance.
     */
    public void trimToSize() {
        modCount++;
        int oldCapacity = elementData.length;
        if (size < oldCapacity) {
            Object oldData[] = elementData;
            elementData = new RdbData[size];
            System.arraycopy(oldData, 0, elementData, 0, size);
        }
    }

    /**
     * Increases the capacity of this <tt>ArrayList</tt> instance, if
     * necessary, to ensure  that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param   minCapacity   the desired minimum capacity.
     */
    public void ensureCapacity(int minCapacity) {
        modCount++;
        int oldCapacity = elementData.length;
        if (minCapacity > oldCapacity) {
            Object oldData[] = elementData;
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            elementData = new RdbData[newCapacity];
            System.arraycopy(oldData, 0, elementData, 0, size);
        }
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return  the number of elements in this list.
     */
    public int size() {
        return size;
    }

    /**
     * Tests if this list has no elements.
     *
     * @return  <tt>true</tt> if this list has no elements;
     *          <tt>false</tt> otherwise.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     *
     * @param elem element whose presence in this List is to be tested.
     */
    public boolean contains(Object elem) {
        return indexOf(elem) >= 0;
    }

    /**
     * Searches for the first occurence of the given argument, testing
     * for equality using the <tt>equals</tt> method.
     *
     * @param   elem   an object.
     * @return  the index of the first occurrence of the argument in this
     *          list; returns <tt>-1</tt> if the object is not found.
     * @see     Object#equals(Object)
     */
    public int indexOf(Object elem) {
        if (elem == null) {
            for (int i = 0; i < size; i++) {
                if (elementData[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (elem.equals(elementData[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified object in
     * this list.
     *
     * @param   elem   the desired element.
     * @return  the index of the last occurrence of the specified object in
     *          this list; returns -1 if the object is not found.
     */
    public int lastIndexOf(Object elem) {
        if (elem == null) {
            for (int i = size - 1; i >= 0; i--) {
                if (elementData[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = size - 1; i >= 0; i--) {
                if (elem.equals(elementData[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Returns a shallow copy of this <tt>ArrayList</tt> instance.  (The
     * elements themselves are not copied.)
     *
     * @return  a clone of this <tt>ArrayList</tt> instance.
     */
    public Object clone() {
        try {
            RdbDataArrayCTXList v = (RdbDataArrayCTXList)super.clone();
            v.elementData = new RdbData[size];
            System.arraycopy(elementData, 0, v.elementData, 0, size);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    /**
     * Returns an array containing all of the elements in this list
     * in the correct order.
     *
     * @return an array containing all of the elements in this list
     * 	       in the correct order.
     */
    public Object[] toArray() {
        Object[] result = new Object[size];
        System.arraycopy(elementData, 0, result, 0, size);
        return result;
    }

    /**
     * Returns an array containing all of the elements in this list in the
     * correct order.  The runtime type of the returned array is that of the
     * specified array.  If the list fits in the specified array, it is
     * returned therein.  Otherwise, a new array is allocated with the runtime
     * type of the specified array and the size of this list.<p>
     *
     * If the list fits in the specified array with room to spare (i.e., the
     * array has more elements than the list), the element in the array
     * immediately following the end of the collection is set to
     * <tt>null</tt>.  This is useful in determining the length of the list
     * <i>only</i> if the caller knows that the list does not contain any
     * <tt>null</tt> elements.
     *
     * @param a the array into which the elements of the list are to
     *		be stored, if it is big enough; otherwise, a new array of the
     * 		same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list.
     * @throws ArrayStoreException if the runtime type of a is not a supertype
     *         of the runtime type of every element in this list.
     */
    public Object[] toArray(Object a[]) {
        if (a.length < size) {
            a = (Object[]) java.lang.reflect.Array.newInstance(
                a.getClass().getComponentType(), size);
        }

        System.arraycopy(elementData, 0, a, 0, size);

        if (a.length > size) {
            a[size] = null;
        }

        return a;
    }

    // Positional Access Operations

    /**
     * Returns the element at the specified position in this list.
     *
     * @param  index index of element to return.
     * @return the element at the specified position in this list.
     * @throws    IndexOutOfBoundsException if index is out of range <tt>(index
     * 		  &lt; 0 || index &gt;= size())</tt>.
     */
    public Object get(int index) {
        RangeCheck(index);

        return elementData[index];
    }

    /**
     * Returns all elements in this list.
     */
    public RdbData[] getAll() {
        return elementData;
    }

    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     * @throws    IndexOutOfBoundsException if index out of range
     *		  <tt>(index &lt; 0 || index &gt;= size())</tt>.
     */
    public Object set(int index, Object element) {
        RangeCheck(index);

        RdbData oldValue = elementData[index];
        elementData[index] = (RdbData) element;
        return oldValue;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param o element to be appended to this list.
     * @return <tt>true</tt> (as per the general contract of Collection.add).
     */
    public boolean add(Object o) {
        ensureCapacity(size + 1); // Increments modCount!!
        elementData[size++] = (RdbData) o;
        return true;
    }

    /**
     * Inserts the specified element at the specified position in this
     * list. Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     * @throws    IndexOutOfBoundsException if index is out of range
     *		  <tt>(index &lt; 0 || index &gt; size())</tt>.
     */
    public void add(int index, Object element) {
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException(
                "Index: " + index + ", Size: " + size);
        }

        ensureCapacity(size + 1); // Increments modCount!!
        System.arraycopy(elementData, index, elementData, index + 1,
                         size - index);
        elementData[index] = (RdbData) element;
        size++;
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).
     *
     * @param index the index of the element to removed.
     * @return the element that was removed from the list.
     * @throws    IndexOutOfBoundsException if index out of range <tt>(index
     * 		  &lt; 0 || index &gt;= size())</tt>.
     */
    public Object remove(int index) {
        RangeCheck(index);

        modCount++;
        Object oldValue = elementData[index];

        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elementData, index + 1, elementData, index,
                             numMoved);
        }
        elementData[--size] = null; // Let gc do its work

        return oldValue;
    }

    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns.
     */
    public void clear() {
        modCount++;

        // Let gc do its work
        for (int i = 0; i < size; i++) {
            elementData[i] = null;
        }

        size = 0;
    }

    /**
     * Appends all of the elements in the specified Collection to the end of
     * this list, in the order that they are returned by the
     * specified Collection's Iterator.  The behavior of this operation is
     * undefined if the specified Collection is modified while the operation
     * is in progress.  (This implies that the behavior of this call is
     * undefined if the specified Collection is this list, and this
     * list is nonempty.)
     *
     * @param c the elements to be inserted into this list.
     * @throws    IndexOutOfBoundsException if index out of range <tt>(index
     *		  &lt; 0 || index &gt; size())</tt>.
     */
    public boolean addAll(Collection c) {
        modCount++;
        int numNew = c.size();
        ensureCapacity(size + numNew);

        Iterator e = c.iterator();
        for (int i = 0; i < numNew; i++) {
            elementData[size++] = (RdbData) e.next();
        }

        return numNew != 0;
    }

    /**
     * Inserts all of the elements in the specified Collection into this
     * list, starting at the specified position.  Shifts the element
     * currently at that position (if any) and any subsequent elements to
     * the right (increases their indices).  The new elements will appear
     * in the list in the order that they are returned by the
     * specified Collection's iterator.
     *
     * @param index index at which to insert first element
     *		    from the specified collection.
     * @param c elements to be inserted into this list.
     * @throws    IndexOutOfBoundsException if index out of range <tt>(index
     *		  &lt; 0 || index &gt; size())</tt>.
     */
    public boolean addAll(int index, Collection c) {
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException(
                "Index: " + index + ", Size: " + size);
        }

        int numNew = c.size();
        ensureCapacity(size + numNew); // Increments modCount!!

        int numMoved = size - index;
        if (numMoved > 0) {
            System.arraycopy(elementData, index, elementData, index + numNew,
                             numMoved);
        }

        Iterator e = c.iterator();
        for (int i = 0; i < numNew; i++) {
            elementData[index++] = (RdbData) e.next();
        }

        size += numNew;
        return numNew != 0;
    }

    /**
     * Removes from this List all of the elements whose index is between
     * fromIndex, inclusive and toIndex, exclusive.  Shifts any succeeding
     * elements to the left (reduces their index).
     * This call shortens the list by <tt>(toIndex - fromIndex)</tt> elements.
     * (If <tt>toIndex==fromIndex</tt>, this operation has no effect.)
     *
     * @param fromIndex index of first element to be removed.
     * @param toIndex index after last element to be removed.
     */
    protected void removeRange(int fromIndex, int toIndex) {
        modCount++;
        int numMoved = size - toIndex;
        System.arraycopy(elementData, toIndex, elementData, fromIndex,
                         numMoved);

        // Let gc do its work
        int newSize = size - (toIndex - fromIndex);
        while (size != newSize) {
            elementData[--size] = null;
        }
    }

    /**
     * Check if the given index is in range.  If not, throw an appropriate
     * runtime exception.
     */
    private void RangeCheck(int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException(
                "Index: " + index + ", Size: " + size);
        }
    }

}
