package amgen.ri.rdb;

//import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import amgen.ri.oracle.ColumnData;
import amgen.ri.oracle.MetaData;
import amgen.ri.oracle.OraSQLManager;
import amgen.ri.rdb.listener.DataSetEvent;
import amgen.ri.rdb.listener.DataSetListener;
import amgen.ri.rdb.listener.SQLExceptionListener;
import amgen.ri.util.Debug;
import amgen.ri.util.ExtString;
import amgen.ri.xml.XMLElement;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RdbData
 * Description: Provides the mechanism for relation/object mapping largely by
 * introspection
 *
 * Usage:
 * To create a mapping class for a table, create a subclass of this base class.
 * This new class should contain
 * instance variables whose names directly correspond to the table columns, or
 * aliases (i.e. the same name, case-insensitive.)
 * Instance variables are assigned based on name, data type and visibility
 * modifier. Instance variables intended
 * for table column assignment should have protected visibility and not be
 * static, final, or transient (volatile is fine.)
 * All protected, non-static, non-final variables are assumed to be part of the
 * mapping. Public, private, and default/package
 * visibility variables are not considered. Inheritance is currently not
 * supported.
 *
 * Class mappings are accomplished through introspection & refection. All
 * RdbData subclasses must be registered in the Register
 * which is a single, static instance. This registration is done when the class
 * is first instantiated as part of the RdbData
 * default constructor. However, if you extend functionality of RdbData and use
 * the Register before the class may be instantiated
 * your code should register the class prior to this, perhaps in a static block.
 * See the Register class for information.
 *
 * A subclass' constructor(s) must call one of RdbData's constructors to ensure
 * proper registration. If the subclass is to be used as
 * an instance variable data type (singleton or array) in another RdbData
 * subclass, it should include the constructor:
 *
 * <p><blockquote><pre>
 * public SubClass(String primaryKey, SQLManager sqlManager, String username, String connectionPool) {
 * super(sqlManager, username, connectionPool);
 * ...
 * }
 * </pre></blockquote><p>
 *
 * which will be called via reflection for instantiation.
 *
 * Subclasses must provide implementations for methods or may override methods
 * which define how the subclass is populated.
 * Some of the implementations return SQL statements which are used to populate
 * the assigned instance variables.
 * They should be formatted with replacement tags ("?") as described in the JDBC
 * PreparedStatement class.
 * Most replacements are done by a setString() method which is exceptable for
 * all standard datatypes used.
 * <pre>getSQL()</pre> Returns the SELECT SQL statement used to populate the
 * class.
 * Each column in the SELECT statement must match the corresponding field name.
 * Use a column alias if the actual
 * column name is different from the field name. This may return null if
 * <pre>generateSQL()</pre> returns true.
 *
 * <pre>generateSQL()</pre> Returns whether SQL statements should be generated
 * by introspection of the class.
 * If this returns true and the corresponding
 * <pre>get<Statement>()</pre> returns null, a SQL statement will be created
 * based on
 * protected instance variable field names and the
 * <pre>getTableName()</pre> if it is not null. If
 * <pre>getTableName()</pre>
 * returns null, the table name is assumed ot be the class name. The first
 * protected instance variable is assumed
 * to be the primary key. If
 * <pre>generateSQL()</pre> returns true but a
 * <pre>get<Statement>()</pre> is not null, the
 * statement returned from the
 * <pre>get<Statement>()</pre> is always used.
 * Notes on Generated SQL: In order to properly dynamically generate the
 * required SQL, there are a couple requirements.
 * The SQL needed must contain only 1 primary key as returns by
 * <pre>getIdentifier()</pre>, and that primary key field
 * must be the first assigned instance variable in the class.
 * Each assigned instance variable must match the table column name when
 * generating SELECT, and UPDATE statements.
 * Arrays still require a member SQL returned by
 * <pre>getMemberSQL(String field)</pre>
 *
 * <pre>getIdentifier()</pre> Returns the identifier used as a replacement in
 * the populating SELECT statement
 * such as the primary key(s). If more than 1 element is required, use CSV.
 * Note: Generated SQL assumes a single primary
 * key. If a concatenated primary key or multiple identifiers, returned in CSV,
 * are required, return the correct SELECT
 * SQL using
 * <pre>getSQL()</pre>
 *
 * <pre>getTableName()</pre> Returns the table name used in generated SELECT SQL
 * statements. It is disregarded otherwise.
 * If this returns null, generated SQL assumes the table name is the class name.
 *
 * <pre>getMemberSQL(String field)</pre> If this class contains arrays,
 * SequenceField's, ClobData's, or BlobData's, this
 * method must return the SQL for populating the data.
 * For
 * SequenceField: return the sequence (character data- not CLOB)
 * ClobData: return the clob
 * BlobData: return the blob
 * Array: return either the element values (for primitive arrays or String
 * arrays) or primary key for RdbData subclass
 * arrays which are used as the first variable in the RdbData subclass
 * constructor:
 * SubClass(String primaryKey, SQLManager sqlManager, String username, String
 * connectionPool)
 * see above.
 * The SQL returned likely will contain a replacement tag which is replaced with
 * the value return by
 * <pre>getIdentifier()
 *     For RdbData arrays, the default implementation returns a SQL statement of the form:
 *     SELECT <MEMBER FIELD CLASS PRIMARY KEY(S)> FROM <MEMBER FIELD CLASS TABLE> WHERE <THIS CLASS PRIMARY KEY>=?
 *     So, this default implementation assumes the name of the field in this class corresponds to the primary key in the
 *     member field class which frequently is correct.</pre>
 *
 *
 * Because instance variables mapped to tables in the subclasses are set via
 * reflection, and java scoping and visibility
 * rules apply. In particular, a base class only has access to its subclass'
 * protected variables which are in the same
 * package. Therefore, two of the abstract methods which must be implemented are
 *
 * <p><blockquote><pre>
 * protected abstract void setFieldValue(Field field, Object value) throws IllegalAccessException;
 * protected abstract Object getFieldValue(Field field) throws IllegalAccessException;
 * </pre></blockquote><p>
 *
 * See below for more information. If an application's data package makes use of
 * this mechanism, an abstract class extending
 * RdbData can implement these methods. Each data class can extend this subclass
 * rather than RdbData. Since they are in the
 * same package, the new subclass can set the data classes mapped instance
 * variables.
 *
 * Modifications to the current values in an RdbData class can be made by either
 * creating your own setXXX methods or using
 * the methods:
 * set(String fieldName, Object value) which sets a specific field
 * setValues(Object[] values) which sets all registered fields in the object.
 *
 * A call to either of these methods sets a flag, modified, to true which can be
 * used to monitor the state. This flag is not affected
 * by a setData() call.
 *
 * Notes on field setting:
 * The object passed must be the same type as the field (or the appropriate
 * wrapper), or a String. If s String is passed on for
 * a field which is not a String, that object type's valueOf(String) method is
 * used for the conversion.
 * set() method: since this sets only 1 field, the dataset flags are not
 * changed. A call to setData() may therefore overwrite the
 * value.
 * setValues() method: since this sets all fields, the dataset flags are set to
 * true. Calls to setData() will not affect the values.
 * Both method set the modified flag to true.
 *
 *
 *
 * There are 3 additonal interfaces which control updating the database:
 * <pre>Saveable.java</pre>
 * Implements commiting data changes to the database. The
 * <pre>performCommit()</pre> commits the current state of the subclass
 * instance to the database by first trying an UPDATE. If this updates no rows,
 * it performs an INSERT.
 * <pre>getInsertSQL()</pre> Returns SQL statement to perform an INSERT. To
 * generate SQL, return null &
 * <pre>generateSQL()</pre>
 * should return true. See above.
 * <pre>getUpdateSQL()</pre> Returns SQL statement to perform an UPDATE. To
 * generate SQL, return null &
 * <pre>generateSQL()</pre>
 * should return true. See above.
 *
 * <pre>Removeable.java</pre>
 * Implements delete data from the database. The
 * <pre>performDelete()</pre> this subclass
 * instance from the database based on its
 * <pre>getIdentifier()</pre>
 * <pre>getDeleteSQL()</pre> Returns SQL statement to perform an DELETE. To
 * generate SQL, return null &
 * <pre>generateSQL()</pre>
 * should return true. See above.
 *
 * <pre>LobSaveable.java</pre>
 * Extension of Saveable and Removeable, this implements allowing the subclass
 * to save data in a Clob or Blob.
 * The
 * <pre>performCommit()</pre> method commits the current state of the subclass
 * instance to the database by first performing a DELETE, then performing an
 * INSERT followed by streaming the LOB.
 * <pre>getSelectLobSQL()</pre> Returns the SELECT SQL which selects the LOB.
 * Required for LOB updating. To generate SQL, return null &
 * <pre>generateSQL()</pre>
 * should return true. See above.
 * <pre>getClobReader()</pre> Returns a handle to a reader to stream character
 * data into a Clob. Not used for Blob data.
 * <pre>getBlobStream()</pre> Returns a handle to an InputStream to stream bytes
 * into a Blob. Not used for Clob data.
 *
 *
 * Allowed data types of the RdbData can be extended. By defining a
 * RegisterAdapterIF, custom data types-
 * either new class types or those whihc require special instantiation- can be
 * used. See Register for
 * information on creating a custom register adapter.
 *
 * Custom data types are set values by overriding the
 * setField(String fieldName, int fieldType, ResultSet rset)
 * method. Passed in are the field name, field type (your custom data type
 * enum), and the JDBC result set
 * which has the cursor moved to the next row.
 *
 * Additional, transient data may be included in this class by using the
 * setTransientData & getTransientData
 *
 * This class also implements the Comparable interface. The default
 * implementation orders by getIdentifier().
 * Subclasses which prefer a different ordering should override the compare
 * method or provide a custom Comparator.
 *
 * @author Jeffrey McDowell
 * @version 1.0
 */
public abstract class RdbData implements DeferredField, Comparable {
  private SimpleDateFormat defaultDateConversionFormat = new SimpleDateFormat("dd-MMM-yy");
  private SimpleDateFormat defaultTimestampConversionFormat = new SimpleDateFormat("dd-MMM-yy h:m:s a");
  private SQLManagerIF sqlManager; //SQLManager to use for populating classes
  private String connectionPool; //Connection pool identifier (provided through constructor)
  private String logonusername; //Virgo User logon name (provided through constructor)
  protected String sql; //Maintains the static SQL statement for this object (may be null id the SQL statement is not static) Used by methods in subclass, not directly by this class
  protected String lastSQL; //Maintains the last SQL statement
  protected List<String> lastReplacements = null; //Maintains the last SQL statement replacements
  protected boolean dataSet = false; //whether a data set call was made (regardless of success)
  protected boolean dataSetSuccess; //whether a data set call was successful
  //whether a data can be committed (i.e. saved to the database) or deleted. Generally true. Some instances may not be desirable to allow this
  protected boolean canCommit = true;
  //whether a sqlexception was thrown- stays set to true following an exception UNTIL the getLastSQLException
  //is called.
  protected boolean sqlExceptionThrown = false;
  protected SQLException sqlException = null; //holds the last sqlException. null until a sqlexception is thrown
  protected boolean modified = false; //sets whether the object has been changed- not by a database call but by a setXXX call
  private long dataSetMillis;
  private HashMap transientData; //Data which never gets saved to the database
  private List sqlExceptionListeners; //SQLException listener list
  private List dataSetListeners; //DataSetListener listener list

  protected RdbData() {
    Register.getRegister().register(this.getClass());
  }

  /**
   * Constructor used by sub classes- specifies SQLManager, logon username, and
   * connection pool. If the connection pool
   * is set to null, the default connection pool for the class is used.
   */
  public RdbData(SQLManagerIF sqlManager, String logonusername, String connectionPool) {
    this();
    this.sqlManager = sqlManager;
    this.logonusername = logonusername;
    setConnectionPool(connectionPool);
  }

  /**
   * Returns the value of the given field.
   * Returns null if the field does not exist.
   *
   * @param fieldName the field nam to return
   * @param performSetData whether to call setData() prior to the field call
   */
  public Object get(String fieldName, boolean performSetData) {
    try {
      if (!performSetData) {
        return getFieldValue(getClass().getDeclaredField(fieldName));
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    return get(fieldName);
  }

  /**
   * Returns the value of the given field.
   * Returns null if the field does not exist.
   * Equivalent to getFieldValue(String)
   */
  public Object get(String fieldName) {
    return getFieldValue(fieldName);
  }

  /**
   * Returns the value of the given field as a Number object. This is
   * appropriate for primitives.
   *
   * @param performSetData whether to call setData() prior to the field call
   * Returns null if the field does not exist.
   * @exception ClassCastException if the field is not a numerical field (int,
   * long, double, short)
   */
  public Number getAsNumber(String fieldName, boolean performSetData) {
    return (Number) get(fieldName, performSetData);
  }

  /**
   * Returns the value of the given field as a Number object. This is
   * appropriate for primitives.
   * Returns null if the field does not exist.
   *
   * @exception ClassCastException if the field is not a numerical field (int,
   * long, double, short)
   */
  public Number getAsNumber(String fieldName) {
    return (Number) getFieldValue(fieldName);
  }

  /**
   * Returns the value of the given field as a Date object.
   * If the field is a Long or Integer, the java.sql.Date(long) constructor is
   * called; otherwise, the field value is cast to java.sql.Date.
   * @para, fieldName the field name
   *
   * @param performSetData whether to call setData() prior to the field call
   * Returns null if the field does not exist.
   * @exception ClassCastException if the field is not a Long,Integer or Date
   */
  public java.sql.Date getAsDate(String fieldName, boolean performSetData) {
    Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
    Integer fieldType = (Integer) fieldTable.get(fieldName);
    switch (fieldType.intValue()) {
      case (Register.LONG):
      case (Register.INTEGER):
      case (Register.JAVADATE):
        Number value = getAsNumber(fieldName, performSetData);
        if (value == null) {
          return null;
        }
        return new java.sql.Date(value.longValue());
      default:
        return (java.sql.Date) get(fieldName, performSetData);
    }
  }

  /**
   * Returns the value of the given field as a Date object after performing a
   * setData, if necessary.
   * If the field is a Long or Integer, the java.sql.Date(long) constructor is
   * called; otherwise, the field value is cast to java.sql.Date.
   * @para, fieldName the field name
   * Returns null if the field does not exist.
   *
   * @exception ClassCastException if the field is not a Long,Integer or Date
   */
  public java.sql.Date getAsDate(String fieldName) {
    return getAsDate(fieldName, true);
  }

  /**
   * Returns the value of the given field as a boolean.
   *
   * @param performSetData whether to call setData() prior to the field call
   * Returns false if the field does not exist.
   * @exception ClassCastException if the field is not a boolean/Boolean field
   */
  public boolean getAsBoolean(String fieldName, boolean performSetData) {
    Boolean boolValue = (Boolean) get(fieldName, performSetData);
    if (boolValue == null) {
      return false;
    }
    return boolValue.booleanValue();
  }

  /**
   * Returns the value of the given field as a boolean.
   * Returns false if the field does not exist.
   *
   * @exception ClassCastException if the field is not a boolean/Boolean field
   */
  public boolean getAsBoolean(String fieldName) {
    Boolean boolValue = (Boolean) getFieldValue(fieldName);
    if (boolValue == null) {
      return false;
    }
    return boolValue.booleanValue();
  }

  /**
   * Returns the value of the given field.
   * Returns null if the field does not exist
   */
  public Object getFieldValue(String fieldName) {
    try {
      setData();
      return getFieldValue(getClass().getDeclaredField(fieldName));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Returns the format specifier used to do implicit date conversion
   *
   * @return
   */
  public SimpleDateFormat getDatabaseDateConversionFormat() {
    return defaultDateConversionFormat;
  }

  /**
   * Returns the maximum length of the given field.
   * If this field's String representation is greater than this value, it's
   * String representation
   * is truncated to this value. If this returns -1 as it is in the default
   * implementation, it is ignored.
   * Subclasses which need to impose such a length limit should override this
   * method. Note: This method
   * really is only appropriate for Strings.
   */
  public int getFieldMaxLength(String fieldName) {
    return -1;
  }

  /**
   * Returns the displayed name of the field. This may be overridden by the
   * subclass.
   * The default implementation returns fieldName.
   *
   * @param fieldName the name of the field to return a more informative title
   * for the field
   */
  public String getFieldDisplayName(String fieldName) {
    return fieldName;
  }

  /**
   * Returns whether the field is text searchable. For Oracle this means that
   * the
   * associated column has an interMedia (or ConText) index and therefore may
   * use the
   * <I>contains</I> operator. The default implementation returns false.
   *
   * @param fieldName the name of the field
   */
  public boolean isFieldTextSearchable(String fieldName) {
    return false;
  }

  /**
   * Set the connection pool for this object.
   * If the given connectionPool is null, this checks
   * if the Register has a connection pool for this class and uses this is
   * available.
   * Otherwise, the given connectionPool is used.
   *
   * @see Register
   */
  public String setConnectionPool(String connectionPool) {
    String registerConnectionPool = Register.getRegister().getRegisteredConnectionPool(this.getClass());
    String defaultConnectionPool = Register.getRegister().getClassDefaultConnectionPool(this.getClass());
    if (connectionPool != null && connectionPool.length() > 0) {
      this.connectionPool = connectionPool;
    } else if (registerConnectionPool != null) {
      this.connectionPool = registerConnectionPool;
    } else {
      this.connectionPool = Register.getRegister().getClassDefaultConnectionPool(this.getClass());
    }
    return this.connectionPool;
  }

  /**
   * Forces the connection pool for this object.
   */
  public String forceConnectionPool(String connectionPool) {
    return (this.connectionPool = connectionPool);
  }

  /**
   * Set the connection pool to the given class' default connection pool.
   *
   * @see Register
   */
  public void setConnectionPool(Class _class) {
    String registeredConnectionPool = Register.getRegister().getRegisteredConnectionPool(_class);
    if (registeredConnectionPool != null) {
      this.connectionPool = registeredConnectionPool;
    } else {
      this.connectionPool = Register.getRegister().getClassDefaultConnectionPool(_class);
    }
  }

  /**
   * Returns the connection pool for this object
   */
  public String getConnectionPool() {
    return connectionPool;
  }

  /**
   * Returns the logon username
   */
  public String getLogonUsername() {
    return logonusername;
  }

  /**
   * Returns the sqlManager
   */
  public SQLManagerIF getSQLManager() {
    return sqlManager;
  }

  /**
   * Sets the SQLManagerIF
   */
  public void setSQLManager(SQLManagerIF sqlManager) {
    this.sqlManager = sqlManager;
  }

  /**
   * Returns whether a sqlexception has been thrown
   */
  public boolean sqlExceptionThrown() {
    return sqlExceptionThrown;
  }

  /**
   * Returns the last sqlexception thrown. Resets the sqlexceptionthrown to
   * false
   */
  public SQLException getLastSQLException() {
    sqlExceptionThrown = false;
    return sqlException;
  }

  /**
   * Returns the elapsed time in milliseconds of the last successful setData
   *
   * @return long
   */
  public long getLastDataSetMillis() {
    return dataSetMillis;
  }

  /**
   * performs the query returning a ResultSet
   */
  protected ResultSet executeSQLQuery(String sql) throws SQLException {
    lastSQL = sql;
    lastReplacements = new ArrayList<String>();
    return sqlManager.executeQuery(sql, getConnectionPool());
  }

  /**
   * performs the query returning a ResultSet
   */
  protected ResultSet executeSQLQuery(String sql, String replacement) throws SQLException {
    lastSQL = sql;
    lastReplacements = new ArrayList<String>();
    if (replacement != null) {
      lastReplacements.add(replacement);
    }
    return sqlManager.executeQuery(sql, replacement, getConnectionPool());
  }

  /**
   * performs the query returning a ResultSet
   */
  protected ResultSet executeSQLQuery(String sql, String[] replacement) throws SQLException {
    lastSQL = sql;
    lastReplacements = new ArrayList<String>();
    if (replacement != null && replacement.length > 0) {
      lastReplacements.addAll(Arrays.asList(replacement));
    }
    return sqlManager.executeQuery(sql, replacement, getConnectionPool());
  }

  /**
   * performs the update returning the number of inserted rows
   */
  protected int executeSQLInsert(String sql, List replacementList) throws SQLException {
    lastSQL = sql;
    lastReplacements = new ArrayList<String>();
    if (replacementList != null && replacementList.size() > 0) {
      lastReplacements.addAll(replacementList);
    }
    if (sql == null) {
      return 0;
    }
    return sqlManager.executeUpdate(sql, replacementList.toArray(), getConnectionPool());
  }

  /**
   * performs the update returning the number of updated rows
   */
  protected int executeSQLUpdate(String sql, List replacementList, String key) throws SQLException {
    lastSQL = sql;
    lastReplacements = new ArrayList<String>();
    if (replacementList != null && replacementList.size() > 0) {
      lastReplacements.addAll(replacementList);
    }
    replacementList.add(key);
    return executeSQLInsert(sql, replacementList);
  }

  /**
   * performs the update returning the number of updated rows
   */
  protected int executeSQLClobInsert(String sql, String key, Reader clobReader) throws SQLException, IOException {
    lastSQL = sql;
    lastReplacements = new ArrayList<String>();
    return sqlManager.executeClobUpdate(sql, key, clobReader, getConnectionPool());
  }

  /**
   * performs the update returning the number of updated rows
   */
  protected int executeSQLBlobInsert(String sql, String key, InputStream blobStream) throws SQLException, IOException {
    lastSQL = sql;
    lastReplacements = new ArrayList<String>();
    return sqlManager.executeBlobUpdate(sql, key, blobStream, getConnectionPool());
  }

  /**
   * performs the delete returning the number of deleted rows
   */
  protected int executeSQLDelete(String sql, String key) throws SQLException {
    lastSQL = sql;
    lastReplacements = new ArrayList<String>();
    return executeSQLInsert(sql, Arrays.asList(new String[]{key}));
  }

  /**
   * Sets the field members for the object returning true if members were set
   * successfully, false otherwise
   * This gets called only once. Every subsequent call to this method returns
   * immediately the outcome of the first call
   * A few points about the returned boolean:
   * If
   * getSQL and getIdentifier are not null and no results return from the
   * database: return false
   * getSQL is not null and getIdentifier is null: return false
   * getSQL and getIdentifier are null: return true (used for those object which
   * are containers)
   */
  public boolean setData() {
    long startDataSetMillis = System.currentTimeMillis();
    ResultSet rset = null;
    if (dataSet) {
      return dataSetSuccess;
    }
    dataSet = true;
    String sqlExecuted = null;
    String fieldName = null;
    Integer fieldType = null;
    if (getSQLManager() == null) {
      System.err.println("WARNING: No SQLManager defined. (" + getClass().getName() + ")");
      fireDataSetEvent(false);
      return false;
    }
    try {
      if (getIdentifier() != null && getSQL() != null) {
        //* >>>>*/ System.out.println("setData "+getClass().getName().substring(getClass().getName().lastIndexOf('.')+1)+" "+this.getIdentifier());
        rset = executeSQLQuery(sqlExecuted = getSQL(), getIdentifier());
        if (!rset.next()) {
          fireDataSetEvent(false);
          closeResources(rset);
          return (dataSetSuccess = false);
        }
      }
      if (getSQL() != null && getIdentifier() == null) {
        fireDataSetEvent(false);
        closeResources(rset);
        return (dataSetSuccess = false);
      }
      if (getSQL() == null && generateSQL() && getIdentifier() != null) {
        rset = executeSQLQuery(sqlExecuted = getGeneratedSQL(Register.SELECT), getIdentifier());
        if (!rset.next()) {
          closeResources(rset);
          fireDataSetEvent(false);
          return (dataSetSuccess = false);
        }
      }
      //* >>>>*/ System.out.println("Loading "+getClass().getName().substring(getClass().getName().lastIndexOf('.')+1)+" "+this.getIdentifier());
      Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
      Iterator fieldNames = Register.getRegister().getFieldNames(getClass().getName());
      while (fieldNames.hasNext()) {
        fieldName = fieldNames.next().toString();
        fieldType = (Integer) fieldTable.get(fieldName);

        //* >>>>*/ System.out.println("FieldName Loop: "+(new Date())+" "+getClass().getName().substring(getClass().getName().lastIndexOf('.')+1)+"."+fieldName+" ("+fieldType+")");
        //* >>>>*/ System.out.println("FieldName Loop: "+getClass().getDeclaredField(fieldName));
        //* >>>>*/ System.out.println("FieldName Loop: "+(rset!= null ? rset.getString(fieldName) : ""));

        switch (fieldType.intValue()) {
          case (Register.STRING):
            if (rset == null) {
              fireDataSetEvent(false);
              return (dataSetSuccess = false);
            }
            setFieldValue(getClass().getDeclaredField(fieldName), rset.getString(fieldName));
            break;
          case (Register.DATE):
            if (rset == null) {
              fireDataSetEvent(false);
              return (dataSetSuccess = false);
            }
            setFieldValue(getClass().getDeclaredField(fieldName), rset.getDate(fieldName));
            break;
          case (Register.TIME):
            if (rset == null) {
              fireDataSetEvent(false);
              return (dataSetSuccess = false);
            }
            setFieldValue(getClass().getDeclaredField(fieldName), rset.getTime(fieldName));
            break;
          case (Register.TIMESTAMP):
            if (rset == null) {
              fireDataSetEvent(false);
              return (dataSetSuccess = false);
            }
            setFieldValue(getClass().getDeclaredField(fieldName), rset.getTimestamp(fieldName));
            break;
          case (Register.BOOLEAN):
            if (rset == null) {
              fireDataSetEvent(false);
              return (dataSetSuccess = false);
            }
            setFieldValue(getClass().getDeclaredField(fieldName), new Boolean(rset.getBoolean(fieldName)));
            break;
          case (Register.LONG):
            if (rset == null) {
              fireDataSetEvent(false);
              return (dataSetSuccess = false);
            }
            setFieldValue(getClass().getDeclaredField(fieldName), new Long(rset.getLong(fieldName)));
            break;
          case (Register.INTEGER):
            if (rset == null) {
              fireDataSetEvent(false);
              return (dataSetSuccess = false);
            }
            setFieldValue(getClass().getDeclaredField(fieldName), new Integer(rset.getInt(fieldName)));
            break;
          case (Register.DOUBLE):
            if (rset == null) {
              fireDataSetEvent(false);
              return (dataSetSuccess = false);
            }
            setFieldValue(getClass().getDeclaredField(fieldName), new Double(rset.getDouble(fieldName)));
            break;
          case (Register.ORASEQUENCE):
            if (rset == null) {
              fireDataSetEvent(false);
              return (dataSetSuccess = false);
            }
            setFieldValue(getClass().getDeclaredField(fieldName), new OraSequenceField(rset.getLong(fieldName)));
            break;
          case (Register.JAVADATE):
            if (rset == null) {
              fireDataSetEvent(false);
              return (dataSetSuccess = false);
            }
            setFieldValue(getClass().getDeclaredField(fieldName), new JavaDate(rset.getLong(fieldName)));
            break;
          case (Register.RDBDATA):
            Class fieldClass = getClass().getDeclaredField(fieldName).getType();
            String memberRdbDataIdent = rset.getString(fieldName);
            if (memberRdbDataIdent != null) {
              Object newObj = fieldClass.getConstructor(
                      new Class[]{
                        String.class, SQLManagerIF.class, String.class, String.class
                      }).newInstance(
                      new Object[]{
                        memberRdbDataIdent, getSQLManager(), getLogonUsername(), getConnectionPool()
                      });
              setFieldValue(getClass().getDeclaredField(fieldName), newObj);
            } else {
              setFieldValue(getClass().getDeclaredField(fieldName), null);
            }
            break;
          case (Register.SEQUENCE):
            setFieldValue(getClass().getDeclaredField(fieldName), new SequenceField(this, fieldName));
            break;
          case (Register.CLOBDATA):
            setFieldValue(getClass().getDeclaredField(fieldName), new ClobData(this, fieldName));
            break;
          case (Register.BLOBDATA):
            setFieldValue(getClass().getDeclaredField(fieldName), new BlobData(this, fieldName));
            break;
          case (Register.XMLDATA):
            setFieldValue(getClass().getDeclaredField(fieldName), new XMLData(this, fieldName));
            break;
          case (Register.HASHTABLE):
            if (rset == null) {
              fireDataSetEvent(false);
              return (dataSetSuccess = false);
            }
            setResultsHashtable(getClass().getDeclaredField(fieldName), rset);
            break;
          case (Register.PRIMITIVEARRAY):
            createPrimitiveArray(getClass().getDeclaredField(fieldName), getMemberSQL(fieldName), getIdentifier());
            break;
          case (Register.MEMBERARRAY):
            createArray(getClass().getDeclaredField(fieldName), getMemberSQL(fieldName), getMemberSQLValues(fieldName));
            break;
          case (Register.SQLRESULTCOLUMN):
            if (rset == null) {
              fireDataSetEvent(false);
              return (dataSetSuccess = false);
            }
            setSQLResults(getClass().getDeclaredField(fieldName), rset);
            break;
          default:
            if (rset == null) {
              fireDataSetEvent(false);
              return (dataSetSuccess = false);
            }
            setField(fieldName, fieldType.intValue(), rset);
            break;
        }
      }
      //System.out.println("Fields: "+fieldNames.size());
      //System.out.println("Total time: "+(((double)(System.currentTimeMillis()-start))/1000)+" secs");
    } catch (SQLException e) {
      handleSQLError(new PrintWriter(System.out), e, fieldName, fieldType);
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
      fireDataSetEvent(false);
      return (dataSetSuccess = false);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      fireDataSetEvent(false);
      return (dataSetSuccess = false);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
      fireDataSetEvent(false);
      return (dataSetSuccess = false);
    } catch (InvocationTargetException e) {
      e.printStackTrace();
      fireDataSetEvent(false);
      return (dataSetSuccess = false);
    } catch (InstantiationException e) {
      e.printStackTrace();
      fireDataSetEvent(false);
      return (dataSetSuccess = false);
    } catch (Exception e) {
      e.printStackTrace();
      fireDataSetEvent(false);
      return (dataSetSuccess = false);
    } finally {
      closeResources(rset);
    }
    fireDataSetEvent(true);
    dataSetMillis = System.currentTimeMillis() - startDataSetMillis;
    return (dataSetSuccess = true);
  }

  /**
   * Called after a setData() call has completed.
   *
   * @param dataSetSuccess whther the setData() successfully set the data
   * elements. If the data was previously set,
   * this is just the dataSetSuccess class variaible
   */
  protected void dataSet(boolean dataSetSuccess) {
  }

  /**
   * Forces all members of the class, including DeferredFields, to be set. This
   * includes all
   * deferred members of deferred fields. If additional members
   * are set which rely on members of this class, the subclass should override
   * this method, call
   * this method (super.setAllData()), then set those members.
   */
  public void setAllData() {
    setData();
    String fieldName = null;
    Integer fieldType = null;
    Field field;
    try {
      Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
      Iterator fieldNames = Register.getRegister().getFieldNames(getClass().getName());
      while (fieldNames.hasNext()) {
        fieldName = fieldNames.next().toString();
        fieldType = (Integer) fieldTable.get(fieldName);

        switch (fieldType.intValue()) {
          case (Register.RDBDATA):
          case (Register.SEQUENCE):
          case (Register.CLOBDATA):
          case (Register.BLOBDATA):
          case (Register.XMLDATA):
            field = getClass().getDeclaredField(fieldName);
            DeferredField deferredField = (DeferredField) getFieldValue(field);
            if (deferredField != null) {
              deferredField.setAllData();
            }
            break;
        }
      }
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Forces all members of the class, including DeferredFields, to be set- but
   * not the
   * deferred members of the deferred fields.
   */
  public void setDeferredData() {
    setData();
    String fieldName = null;
    Integer fieldType = null;
    Field field;
    try {
      Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
      Iterator fieldNames = Register.getRegister().getFieldNames(getClass().getName());
      while (fieldNames.hasNext()) {
        fieldName = fieldNames.next().toString();
        fieldType = (Integer) fieldTable.get(fieldName);

        switch (fieldType.intValue()) {
          case (Register.RDBDATA):
          case (Register.SEQUENCE):
          case (Register.CLOBDATA):
          case (Register.XMLDATA):
          case (Register.BLOBDATA):
            field = getClass().getDeclaredField(fieldName);
            DeferredField deferredField = (DeferredField) getFieldValue(field);
            if (deferredField != null) {
              deferredField.setData();
            }
            break;
        }
      }
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * This is called by setData for any fieldtypes unknown to RgbData. Override
   * this when added additional
   * field types other than the standard ones provided
   */
  protected void setField(String fieldName, int fieldType, ResultSet rset) throws Exception {
  }

  /**
   * Creates an array based on the getMemberSQL method
   */
  protected void createArray(Field arrayField, String sql, String[] sqlReplacement) throws SQLException, NoSuchMethodException, InvocationTargetException,
          IllegalAccessException, InstantiationException {
    HashMap usedIdentifierList = new HashMap();
    int arrayMemberCount = 0;
    if (sql == null) {
      return;
    }
    Class[] args = {
      String.class, SQLManagerIF.class, String.class, String.class};
    Vector members = new Vector();
    Class arrayFieldComponentClass = getArrayFieldComponentClass(arrayField.getName());
    if (arrayFieldComponentClass == null) {
      arrayFieldComponentClass = arrayField.getType().getComponentType();
    }
    ResultSet arrayRset = null;
    try {
      arrayRset = executeSQLQuery(sql, sqlReplacement);
      String className = arrayFieldComponentClass.getName();
      if (!Register.getRegister().isRegistered(className)) {
        arrayFieldComponentClass.getConstructor(new Class[0]).newInstance(new Object[0]);
      }
      //System.out.println("createArray: "+sql+" - "+sqlReplacement+" - "+" ("+className+")");
      while (arrayRset.next()) {
        if (getMaximumArraySize(arrayField.getName()) >= 0 && arrayMemberCount >= getMaximumArraySize(arrayField.getName())) {
          notifyMemberArrayLimitReached(this, arrayField.getName());
          break;
        }
        String identifier = getArrayIdentifier(arrayField.getName(), arrayRset);
        if (identifier == null) {
          continue;
        }
        if (getDistinctMemberArray(arrayField.getName()) && usedIdentifierList.containsKey(identifier)) {
          continue;
        } else if (getDistinctMemberArray(arrayField.getName())) {
          usedIdentifierList.put(identifier, "");
        }
        Object[] argVals = {
          identifier, getSQLManager(), getLogonUsername(), connectionPool};
        Object arrayElement = arrayFieldComponentClass.getConstructor(args).newInstance(argVals);
        members.add(arrayElement);
        arrayMemberCount++;
        if (arrayElement instanceof RdbData) {
          arrayElementSet(arrayField.getName(), (RdbData) arrayElement, arrayRset);
        }
      }
      Object[] data = (Object[]) Array.newInstance(arrayFieldComponentClass, members.size());
      members.copyInto(data);
      setFieldValue(arrayField, data);
    } finally {
      closeResources(arrayRset);
    }
  }

  /**
   * Returns the identifier for the array using the given ResultSet at the
   * current cursor position.
   * The default implementation returns all queried columns in the populating
   * SQL statement as
   * a CSV string. E.g. SELECT ID1,ID2 FROM TABLE => "ID1,ID2"
   * This may be overridden to use the columns returned from a SQL statement
   * which populates a member array
   * differently.
   *
   * @param rset the populating SQL statement's ResultSet at the current array's
   * cursor position (i.e. next() has been called
   * on the ResultSet
   * @return the value to be used as the primary key for the RdbData array
   * member
   * @throws SQLException if any problems occur while reading the ResultSet
   */
  protected String getArrayIdentifier(String fieldName, ResultSet rset) throws SQLException {
    String id = rset.getString(1);
    if (id == null) {
      return null;
    }
    StringBuffer identifier = new StringBuffer(id);
    int columnCount = rset.getMetaData().getColumnCount();
    for (int i = 2; i <= columnCount; i++) {
      identifier.append("," + rset.getString(i));
    }
    return identifier.toString();
  }

  /**
   * Called when an RdbData array element is set. Default implementation does
   * nothing.
   *
   * @param rdbData new array element
   */
  protected void arrayElementSet(String fieldName, RdbData rdbData, ResultSet arrayRset) {
  }

  /**
   * Creates an array based on the getMemberSQL method
   */
  protected void createPrimitiveArray(Field arrayField, String sql, String sqlReplacement) throws SQLException, NoSuchMethodException, InvocationTargetException,
          IllegalAccessException, InstantiationException {
    if (sql == null) {
      return;
    }
    Class[] args = {
      String.class};
    Vector members = new Vector();
    Class arrayFieldComponentClass = getPrimitiveArrayFieldComponentClass(arrayField.getType().getComponentType());
    ResultSet arrayRset = null;
    try {
      arrayRset = sqlManager.executeQuery(sql, sqlReplacement, getConnectionPool());
      //System.out.println("createArray: "+sql+" - "+sqlReplacement+" ("+arrayFieldComponentClass.getName()+")");
      while (arrayRset.next()) {
        members.add(arrayFieldComponentClass.getConstructor(
                new Class[]{String.class}).newInstance(
                new Object[]{arrayRset.getString(1)}));
      }
      Object[] data = (Object[]) Array.newInstance(arrayFieldComponentClass, members.size());
      members.copyInto(data);
      setFieldValue(arrayField, data);
    } finally {
      closeResources(arrayRset);
    }
  }

  /**
   * Sets the Oracle sequence field value using its defined Oracle sequence
   */
  public void setOraSequenceData(OraSequenceField sequenceField) {
    try {
      long nextVal = sqlManager.getNextSequenceValue(sequenceField.getSequenceName(), getConnectionPool());
      sequenceField.setValue(nextVal);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Sets the sequence field for the RgbData object.
   * Called by SequenceField. Generally there is no need to call this
   */
  public void setSequenceData(SequenceField sequenceField) {
    ResultSet rset = null;
    try {
      Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
      String fieldName = sequenceField.getFieldName();
      Integer fieldType = (Integer) fieldTable.get(fieldName);
      if (fieldType.intValue() == Register.SEQUENCE) {
        String sql = getMemberSQL(fieldName);
        if (sql == null && generateSQL()) {
          sql = Register.getRegister().getRegisteredStatement(getClass(), fieldName, Register.SELECT);
          if (sql == null) {
            return;
          }
        }
        rset = sqlManager.executeQuery(sql, getIdentifier(), getConnectionPool());
        if (rset.next()) {
          sequenceField.setSequence(rset.getBytes(fieldName));
        }
      }
    } catch (SQLException e) {
      handleSQLError(new PrintWriter(System.out), e);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      closeResources(rset);
    }
  }

  /**
   * Sets the CLOB data field for this object
   * Called by ClobField. Generally there is no need to call this
   */
  public void setClobData(ClobData clobField) {
    ResultSet rset = null;
    try {
      Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
      String fieldName = clobField.getFieldName();
      Integer fieldType = (Integer) fieldTable.get(fieldName);
      if (fieldType.intValue() == Register.CLOBDATA) {
        String sql = getMemberSQL(fieldName);
        if (sql == null && generateSQL()) {
          sql = Register.getRegister().getRegisteredStatement(getClass(), fieldName, Register.SELECT);
          if (sql == null) {
            return;
          }
        }
        rset = sqlManager.executeQuery(sql, getIdentifier(), getConnectionPool());
        if (rset.next()) {
          Clob clob = rset.getClob(fieldName);
          if (clob != null) {
            clobField.setSize(clob.length());
            char[] data = new char[(int) clob.length()];
            clob.getCharacterStream().read(data);
            clobField.setData(data);
          } else {
            clobField.setSize((long) 0);
            clobField.setData(new char[0]);
          }
        }
      }
    } catch (SQLException e) {
      handleSQLError(new PrintWriter(System.out), e);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      closeResources(rset);
    }

  }

  /**
   * Sets the XMLType data field for this object
   * Called by ClobField. Generally there is no need to call this
   */
  public void setXMLData(XMLData xmlField) {
    oracle.jdbc.OracleResultSet rset = null;
    try {
      Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
      String fieldName = xmlField.getFieldName();
      Integer fieldType = (Integer) fieldTable.get(fieldName);
      if (fieldType.intValue() == Register.XMLDATA) {
        String sql = getMemberSQL(fieldName);
        if (sql == null && generateSQL()) {
          sql = Register.getRegister().getRegisteredStatement(getClass(), fieldName, Register.SELECT);
          if (sql == null) {
            return;
          }
        }
        rset = (oracle.jdbc.OracleResultSet) sqlManager.executeQuery(sql, getIdentifier(), getConnectionPool());
        if (rset.next()) {
          oracle.sql.OPAQUE opaque = rset.getOPAQUE(fieldName);
          if (opaque != null) {
            xmlField.setData(opaque);
          } else {
            xmlField.setData(null);
          }
        }
      }
    } catch (SQLException e) {
      handleSQLError(new PrintWriter(System.out), e);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      closeResources(rset);
    }
  }

  /**
   * Sets the CLOB data field size without setting data for this object
   * Privides for fast access to the LOB size.
   * Called by ClobField. Generally there is no need to call this
   */
  public void setClobSize(ClobData clobField) {
    ResultSet rset = null;
    try {
      Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
      String fieldName = clobField.getFieldName();
      Integer fieldType = (Integer) fieldTable.get(fieldName);
      if (fieldType.intValue() == Register.CLOBDATA) {
        String sql = getMemberSQL(fieldName);
        if (sql == null && generateSQL()) {
          sql = Register.getRegister().getRegisteredStatement(getClass(), fieldName, Register.SELECT);
          if (sql == null) {
            return;
          }
        }
        rset = sqlManager.executeQuery(sql, getIdentifier(), getConnectionPool());
        if (rset.next()) {
          Clob clob = rset.getClob(fieldName);
          if (clob == null) {
            clobField.setSize(0);
          } else {
            clobField.setSize(clob.length());
          }
        }
      }
    } catch (SQLException e) {
      handleSQLError(new PrintWriter(System.out), e);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      closeResources(rset);
    }
  }

  /**
   * Sets the BLOB data field for this object
   * Called by BlobField. Generally there is no need to call this
   */
  public void setBlobData(BlobData blobField) {
    ResultSet rset = null;
    try {
      Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
      String fieldName = blobField.getFieldName();
      Integer fieldType = (Integer) fieldTable.get(fieldName);
      if (fieldType.intValue() == Register.BLOBDATA) {
        String sql = getMemberSQL(fieldName);
        if (sql == null && generateSQL()) {
          sql = Register.getRegister().getRegisteredStatement(getClass(), fieldName, Register.SELECT);
          if (sql == null) {
            return;
          }
        }

        rset = sqlManager.executeQuery(sql, getIdentifier(), getConnectionPool());
        if (rset.next()) {
          Blob blob = rset.getBlob(fieldName);
          if (blob == null) {
            blobField.setSize(0);
            blobField.setData(new byte[0]);
          } else {
            blobField.setSize(blob.length());
            byte[] data = new byte[(int) blob.length()];
            blob.getBinaryStream().read(data);
            blobField.setData(data);
          }
        }
      }
    } catch (SQLException e) {
      handleSQLError(new PrintWriter(System.out), e);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      closeResources(rset);
    }
  }

  /**
   * Sets the BLOB data field size without setting data for this object
   * Provides for fast access to the LOB size.
   * Called by BlobField. Generally there is no need to call this
   */
  public void setBlobSize(BlobData blobField) {
    ResultSet rset = null;
    try {
      Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
      String fieldName = blobField.getFieldName();
      Integer fieldType = (Integer) fieldTable.get(fieldName);
      if (fieldType.intValue() == Register.BLOBDATA) {
        String sql = getMemberSQL(fieldName);
        if (sql == null && generateSQL()) {
          sql = Register.getRegister().getRegisteredStatement(getClass(), fieldName, Register.SELECT);
          if (sql == null) {
            return;
          }
        }
        rset = sqlManager.executeQuery(sql, getIdentifier(), getConnectionPool());
        if (rset.next()) {
          Blob blob = rset.getBlob(fieldName);
          if (blob == null) {
            blobField.setSize(0);
          } else {
            blobField.setSize(blob.length());
          }
        }
      }
    } catch (SQLException e) {
      handleSQLError(new PrintWriter(System.out), e);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      closeResources(rset);
    }

  }

  /**
   * Sets the results hash for SQLResults objects
   */
  private void setSQLResults(Field field, ResultSet rset) throws SQLException, IllegalAccessException {
    setFieldValue(field, getResultsHashtable(rset));
  }

  /**
   * Sets the results hash for SQLResults objects
   */
  private void setResultsHashtable(Field field, ResultSet rset) throws SQLException, IllegalAccessException {
    setFieldValue(field, (Hashtable) getResultsHashtable(rset));
  }

  /**
   * Sets the SQLResultColumn SQLResults objects
   */
  protected SQLResultColumn getResultsHashtable(ResultSet rset) throws SQLException {
    ResultSetMetaData metaData = rset.getMetaData();
    SQLResultColumn results = new SQLResultColumn(metaData.getColumnCount());
    for (int i = 1; i <= metaData.getColumnCount(); i++) {
      String column = metaData.getColumnName(i).toUpperCase();
      switch (metaData.getColumnType(i)) {
        case (Types.BIT):
        case (Types.TINYINT):
        case (Types.SMALLINT):
        case (Types.INTEGER):
        case (Types.BIGINT):
        case (Types.FLOAT):
        case (Types.REAL):
        case (Types.DOUBLE):
        case (Types.NUMERIC):
        case (Types.DECIMAL):
          results.put(column, new Double(rset.getDouble(column)));
          break;
        case (Types.CHAR):
        case (Types.VARCHAR):
        case (Types.LONGVARCHAR):
          results.put(column, rset.getString(column));
          break;
        default:
          results.put(column, rset.getObject(column));
          break;
      }
    }
    return results;
  }

  /**
   * Sets the values of all fields in an RdbData class. This method is passed an
   * array of values
   * which are used to set each field. The order of the elements in the array
   * must correspond to
   * the order of the fields in the RdbData class.
   * The data types of the elements in the values arrays must conform to the
   * allowed values for the
   * field type. See setFieldValue(String, Object) for explanation of types.
   *
   * Note: If all fields are set successfully, will set the internal data set
   * variables.
   * Consequently, the class will act as if it successfully had its fields set
   * (like a call to the database).
   *
   */
  public boolean setValues(Object[] values) {
    try {
      int fieldCount = Register.getRegister().getFieldNameCount(getClass().getName());
      if (fieldCount != values.length) {
        return false;
      }
      Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
      for (int i = 0; i < fieldCount; i++) {
        String fieldName = Register.getRegister().getFieldName(getClass().getName(), i);
        setFieldValue(fieldName, values[i]);
      }
    } catch (Exception e) {
      System.err.println("Error setting field." + e);
      return false;
    }
    dataSet = true;
    dataSetSuccess = true;
    modified = true;
    return true;
  }

  /**
   * Sets the values of fields in an RdbData class using a Map of
   * fieldName => fieldValue
   * The data types of the field values must conform to the allowed values for
   * the
   * field type. See setFieldValue(String, Object) for explanation of types.
   *
   * Any key in the provided Map which is not a field name is disregarded.
   *
   * Note: If all fields are set successfully, will set the internal data set
   * variables.
   * Consequently, the class will act as if it successfully had its fields set
   * (like a call to the database).
   *
   * @return Returns a list of field names actually set
   */
  public List setValues(Map values) {
    Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
    int fieldCount = Register.getRegister().getFieldNameCount(getClass().getName());
    List setFieldList = new ArrayList();
    for (Iterator fieldNameIter = values.keySet().iterator(); fieldNameIter.hasNext();) {
      String fieldName = (String) fieldNameIter.next();
      if (!fieldTable.containsKey(fieldName)) {
        continue;
      }
      if (setFieldValue(fieldName, values.get(fieldName))) {
        setFieldList.add(fieldName);
      }
    }
    if (fieldCount == setFieldList.size()) {
      dataSet = true;
      dataSetSuccess = true;
    }
    modified = true;
    return setFieldList;
  }

  /**
   * Equivalent to setFieldValue(String, Object, int)
   */
  public boolean set(String fieldName, Object value, int maxLength) {
    modified = true;
    return setFieldValue(fieldName, value, maxLength);
  }

  /**
   * Equivalent to setFieldValue(String, Object)
   */
  public boolean set(String fieldName, Object value) {
    modified = true;
    return setFieldValue(fieldName, value);
  }

  /**
   * Sets the internal variables indicating that this is a new entity
   */
  public void setNewEntity() {
    this.dataSet = true;
    this.dataSetSuccess = true;
    this.modified = true;
  }

  /**
   * Returns the modified flag
   */
  public boolean isModified() {
    return modified;
  }

  /**
   * Returns the whether the RdbData object has been set- without setting the
   * data
   */
  public boolean isDataSet() {
    return dataSet;
  }

  /**
   * Forces the setting of the dataSet and dataSetSuccess.
   * Thsi affects whether the RdbData gets set later, so be careful!!
   */
  public void setIsDataSet(boolean dataSet, boolean dataSetSuccess) {
    this.dataSet = dataSet;
    this.dataSetSuccess = dataSetSuccess;
  }

  /**
   * Sets the modified flag
   *
   * @param modified the state of the RdbData object
   */
  public void setModified(boolean modified) {
    this.modified = modified;
  }

  /**
   * Returns whether this object is allowed to be committed to the database.
   * Generally, this is
   * true. There may be instances where this is not desired and this offers
   * protection. If this is
   * false, any performCommit returns 0
   *
   * @return the value of canCommit
   */
  public boolean canCommit() {
    return canCommit;
  }

  /**
   * Sets whether this object is allowed to be committed to the database.
   * Generally, this is
   * true. There may be instances where this is not desired and this offers
   * protection. If this is
   * false, any performCommit returns 0
   *
   * @param canCommit the value of canCommit
   */
  public void setCanCommit(boolean canCommit) {
    this.canCommit = canCommit;
  }

  /**
   * Used to set the value of a field of an RdbData class. This method uses the
   * setFieldValue(Field, Object) method which can be
   * used for some customization for cutsom field data types
   *
   * Specified are the fieldName and its corresponsing value.
   * The value must either be the same type as the field or may be a String for
   * certain field data types.
   *
   * The field data types which may be set by a String are
   * Date, Long, Integer, Double, Sequence, ClobData, BlobData, OraSequence,
   * RdbData. For all but an RdbData data type,
   * the <data type>.valueOf(String) method is called to set the object's value.
   * For an RdbData data type given a String, the
   * constructor <RdbData>(String, SQLManagerIF, String, String) is called with
   * value used as the first parameter.
   *
   * All other types, such as Arrays, Hashtable, ect, should match in data type
   * unless handled by implementation
   * of setFieldValue(Field, Object)
   *
   * Note: This method does not affect the internal data set variables.
   * Consequently, any call to setData() may reset this value by
   * a call to the database.
   *
   * @return whether the field was successfully set
   *
   */
  public boolean setFieldValue(String fieldName, Object value) {
    return setFieldValue(fieldName, value, -1);
  }

  /**
   * Used to set the value of a field of an RdbData class. This method uses the
   * setFieldValue(Field, Object) method which can be
   * used for some customization for cutsom field data types.
   *
   * This method optionally allows specification of the maximum length of the
   * field. This applies only to String data types. If
   * the parameter maxLength is >0, any String data type value with length
   * greater than maxLength is truncated to this length.
   *
   * Specified are the fieldName and its corresponsing value.
   * The value must either be the same type as the field or may be a String for
   * certain field data types.
   *
   * The field data types which may be set by a String are
   * Date, Long, Integer, Double, Sequence, ClobData, BlobData, OraSequence,
   * RdbData. For all but an RdbData data type,
   * the <data type>.valueOf(String) method is called to set the object's value.
   * For an RdbData data type given a String, the
   * constructor <RdbData>(String, SQLManagerIF, String, String) is called with
   * value used as the first parameter.
   *
   * All other types, such as Arrays, Hashtable, ect, should match in data type
   * unless handled by implementation
   * of setFieldValue(Field, Object)
   *
   * Note: This method does not affect the internal data set variables.
   * Consequently, any call to setData() may reset this value by
   * a call to the database.
   *
   * @return whether the field was successfully set
   *
   */
  public boolean setFieldValue(String fieldName, Object value, int maxLength) {
    try {
      Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
      Integer fieldType = (Integer) fieldTable.get(fieldName);
      Object fieldValue = value;
      Object currentValue = getFieldValue(getClass().getDeclaredField(fieldName));

      if (fieldValue instanceof String) {
        String stringValue = fieldValue.toString();
        switch (fieldType.intValue()) {
          case (Register.STRING):
            if (maxLength > 0 && stringValue.length() > maxLength) {
              stringValue = stringValue.substring(0, maxLength);
            }
            break;
          case (Register.DATE):
          case (Register.TIME):
          case (Register.TIMESTAMP):
            fieldValue = parseDateTime(fieldName, fieldType.intValue(), stringValue);
            break;
          case (Register.BOOLEAN):
            if (stringValue.equalsIgnoreCase("true")) {
              fieldValue = new Boolean(true);
            } else if (stringValue.equalsIgnoreCase("false")) {
              fieldValue = new Boolean(false);
            } else {
              fieldValue = new Boolean(!stringValue.equals("0"));
            }
            break;
          case (Register.LONG):
          case (Register.INTEGER):
          case (Register.DOUBLE):
            fieldValue = parseNumber(fieldName, fieldType.intValue(), stringValue);
            break;
          case (Register.JAVADATE):
            fieldValue = JavaDate.valueOf(stringValue);
            break;
          case (Register.ORASEQUENCE):
            fieldValue = OraSequenceField.valueOf(stringValue, fieldName);
            break;
          case (Register.RDBDATA):
            Class fieldClass = getClass().getDeclaredField(fieldName).getType();
            fieldValue = fieldClass.getConstructor(
                    new Class[]{String.class, SQLManagerIF.class, String.class, String.class}).newInstance(
                    new Object[]{fieldValue, getSQLManager(), getLogonUsername(), getConnectionPool()});
            break;
          case (Register.SEQUENCE):
            fieldValue = SequenceField.valueOf(stringValue, fieldName);
            break;
          case (Register.CLOBDATA):
            fieldValue = ClobData.valueOf(stringValue, fieldName);
            break;
          case (Register.BLOBDATA):
            fieldValue = BlobData.valueOf(stringValue, fieldName);
            break;
          case (Register.XMLDATA):
            fieldValue = XMLData.valueOf(stringValue, fieldName, getSQLManager(), getConnectionPool());
            break;
          case (Register.HASHTABLE):
          case (Register.PRIMITIVEARRAY):
          case (Register.MEMBERARRAY):
          case (Register.SQLRESULTCOLUMN):
            System.err.println("ERROR: Member type can not be set by String value.");
            break;
          default:
            System.err.println("ERROR: Unknown member type or member type can not be set by String value.");
            break;
        }
      }
      if (currentValue == null || (currentValue != fieldValue && !currentValue.equals(fieldValue))) {
        setFieldValue(getClass().getDeclaredField(fieldName), fieldValue);
      } else {
        return false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Returns a java.sql.Date, java.sql.Time, or java.sql.Timestamp object
   * (depending on the fieldType) from the String value using the parsing
   * rules returned by getDateFormatString() or, if this is null, the
   * default from the valueOf() method from the object.
   *
   * Returns null if value is null or zero-length
   *
   * @param fieldType
   * @param value
   * @return
   * @throws ParseException
   */
  private Object parseDateTime(String fieldName, int fieldType, String value) throws ParseException {
    if (value == null || value.length() == 0) {
      return null;
    }
    String dateFormatSpecifier = getDateFormatString(fieldName);
    if (dateFormatSpecifier == null) {
      switch (fieldType) {
        case (Register.DATE):
          return java.sql.Date.valueOf(value);
        case (Register.TIME):
          return java.sql.Time.valueOf(value);
        case (Register.TIMESTAMP):
          return java.sql.Timestamp.valueOf(value);
        default:
          throw new IllegalArgumentException("Not a time/date field");
      }
    }
    SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatSpecifier);
    long millis = dateFormat.parse(value).getTime();
    switch (fieldType) {
      case (Register.DATE):
        return new java.sql.Date(millis);
      case (Register.TIME):
        return new java.sql.Time(millis);
      case (Register.TIMESTAMP):
        return new java.sql.Timestamp(millis);
      default:
        throw new IllegalArgumentException("Not a time/date field");
    }
  }

  /**
   * Returns a java.lang.Long, java.lang.Integer, or java.lang.Double object
   * (depending on the fieldType) from the String value using the parsing
   * rules returned by getNumberFormatString() or, if this is null, the
   * default from the Double.valueOf() method from the object.
   *
   * Returns a "0" initialized number object if value is null or zero-length
   *
   * @param fieldType
   * @param value
   * @return
   * @throws ParseException
   */
  private Object parseNumber(String fieldName, int fieldType, String value) throws ParseException {
    Number number;
    String numberFormatSpecifier = getNumberFormatString(fieldName);
    if (value == null || value.length() == 0) {
      number = new Double(0);
    } else if (numberFormatSpecifier == null) {
      number = Double.valueOf(value);
    } else {
      DecimalFormat decimalFormat = new DecimalFormat(numberFormatSpecifier);
      number = decimalFormat.parse(value);
    }
    switch (fieldType) {
      case (Register.LONG):
        return new Long(number.longValue());
      case (Register.INTEGER):
        return new Integer(number.intValue());
      case (Register.DOUBLE):
        return new Double(number.doubleValue());
      default:
        throw new IllegalArgumentException("Not a numeric field");
    }
  }

  /**
   * Performs a commit on this RgbData object saving to the database the current
   * values of the fields
   * This class must have implemented LobSaveable or Saveable.
   * This object must have canCommit set to true which is the default. If
   * canCommit is false, this returns 0.
   * Thsi is the same as performCommit(true)
   */
  public int performCommit() {
    return performCommit(true);
  }

  /**
   * Performs a commit on this RgbData object saving to the database the current
   * values of the fields
   * This class must have implemented LobSaveable or Saveable.
   * This object must have canCommit set to true which is the default. If
   * canCommit is false, this returns 0.
   *
   * @param withLobCommit if the class is LobSaveable, the LOB's are saved if
   * this is true as well as the other fields
   */
  public int performCommit(boolean withLobCommit) {
    if (!canCommit) {
      return 0;
    }
    try {
      if (this instanceof LobSaveable && withLobCommit) {
        return performLobDataCommit();
      } else if (this instanceof Saveable) {
        return performDataCommit();
      }
    } catch (SQLException e) {
      handleSQLError(new PrintWriter(System.out), e);
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }

  /**
   * Mechanism which performs the commit on this RgbData object saving to the
   * database the current values of the fields
   * This class must have implemented ClobSaveable or Saveable
   */
  private int performDataCommit() throws SQLException, NoSuchFieldException, IllegalAccessException, Exception {
    Saveable saveable = (Saveable) this;
    String tableName = getTableNameForSQL();
    int updateCount = 0;
    ArrayList replacements = new ArrayList();

    Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
    Iterator iterator = Register.getRegister().getFieldNames(getClass().getName());

    SimpleDateFormat dateFormatter = getDatabaseDateConversionFormat();

    while (iterator.hasNext()) {
      String fieldName = iterator.next().toString();
      Integer fieldType = (Integer) fieldTable.get(fieldName);
      switch (fieldType.intValue()) {
        case (Register.STRING):
        case (Register.LONG):
        case (Register.INTEGER):
        case (Register.DOUBLE):
        case (Register.SEQUENCE):
        case (Register.ORASEQUENCE):
        case (Register.JAVADATE):
          Object fieldValue = getFieldValue(getClass().getDeclaredField(fieldName));
          String fieldValueString = (fieldValue == null ? "null" : fieldValue.toString());
          if (getFieldMaxLength(fieldName) > 0 && fieldValueString.length() > getFieldMaxLength(fieldName)) {
            fieldValueString = fieldValueString.substring(0, getFieldMaxLength(fieldName));
          }
          replacements.add(fieldValueString);
          break;
        case (Register.RDBDATA):
          RdbData rdbData = (RdbData) getFieldValue(getClass().getDeclaredField(fieldName));
          String rdbDataIdentifier = (rdbData == null ? "null" : rdbData.getIdentifier());
          if (getFieldMaxLength(fieldName) > 0 && rdbDataIdentifier.length() > getFieldMaxLength(fieldName)) {
            rdbDataIdentifier = rdbDataIdentifier.substring(0, getFieldMaxLength(fieldName));
          }
          replacements.add(rdbDataIdentifier);
          break;
        case (Register.DATE):
          Date date = (Date) getFieldValue(getClass().getDeclaredField(fieldName));
          replacements.add((date == null ? "null" : dateFormatter.format(date)));
          break;
        case (Register.TIME):
          Time time = (Time) getFieldValue(getClass().getDeclaredField(fieldName));
          replacements.add((time == null ? "null" : dateFormatter.format(time)));
          break;
        case (Register.TIMESTAMP):
          Timestamp timestamp = (Timestamp) getFieldValue(getClass().getDeclaredField(fieldName));
          replacements.add((timestamp == null ? "null" : defaultTimestampConversionFormat.format(timestamp)));
          break;
        case (Register.BOOLEAN):
          Boolean boolValues = (Boolean) getFieldValue(getClass().getDeclaredField(fieldName));
          replacements.add((boolValues == null ? "null" : boolValues.booleanValue() ? "1" : "0"));
          break;
        case (Register.XMLDATA):
          XMLData xmlData = (XMLData) getFieldValue(getClass().getDeclaredField(fieldName));
          replacements.add((xmlData == null ? null : xmlData.getXMLType()));
          break;
      }
    }
    String insertSQL = ((generateSQL() && saveable.getInsertSQL() == null) ? getGeneratedSQL(Register.INSERT) : saveable.getInsertSQL());
    String updateSQL = ((generateSQL() && saveable.getUpdateSQL() == null) ? getGeneratedSQL(Register.UPDATE) : saveable.getUpdateSQL());

    //System.out.println("performCommit: "+saveable.getUpdateSQL()+","+ replacements.toString()+","+getIdentifier());

    if ((updateCount = executeSQLUpdate(updateSQL, replacements, getIdentifier())) == 0) {
      return executeSQLInsert(insertSQL, replacements);
    }
    return updateCount;
  }

  /**
   * Mechanism which performs the commit on this RgbData object which contains a
   * Clob saving to the database the current values of the fields
   * This class must have implemented ClobSaveable or Saveable
   */
  private int performLobDataCommit() throws SQLException, NoSuchFieldException, IllegalAccessException, Exception {
    int countMods = 0;
    String selectLOBSQL;
    LobSaveable saveable = (LobSaveable) this;
    Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
    Iterator iterator = Register.getRegister().getFieldNames(getClass().getName());

    performDataDelete();
    countMods += performDataCommit();

    while (iterator.hasNext()) {
      String fieldName = iterator.next().toString();
      Integer fieldType = (Integer) fieldTable.get(fieldName);
      switch (fieldType.intValue()) {
        case (Register.BLOBDATA):
          selectLOBSQL = ((generateSQL() && saveable.getSelectLobSQL(fieldName) == null) ? getGeneratedSQL(Register.SELECT, fieldName)
                  : saveable.getSelectLobSQL(fieldName));
          if (saveable.getBlobStream(fieldName) != null) {
            countMods += executeSQLBlobInsert(selectLOBSQL, getIdentifier(), saveable.getBlobStream(fieldName));
          }
          break;
        case (Register.CLOBDATA):
          selectLOBSQL = ((generateSQL() && saveable.getSelectLobSQL(fieldName) == null) ? getGeneratedSQL(Register.SELECT, fieldName)
                  : saveable.getSelectLobSQL(fieldName));
          if (saveable.getClobReader(fieldName) != null) { //Register.getRegister().p();
            countMods += executeSQLClobInsert(selectLOBSQL, getIdentifier(), saveable.getClobReader(fieldName));
          }
          break;
        case (Register.XMLDATA):
          throw new SQLException("XML Data not saveable");
      }
    }
    return countMods;
  }

  /**
   * Performs a delete on this object removing it from the database. Must
   * implement Removeable
   * This object must have canCommit set to true which is the default. If
   * canCommit is false, this returns 0.
   */
  public int performDelete() {
    return performDataDelete();
  }

  /**
   * Performs the actual delete on this object removing it from the database.
   * This Object must implement Removeable.
   * This object must have canCommit set to true which is the default. If
   * canCommit is false, this returns 0.
   * This method is private as it gets called within this class
   */
  private int performDataDelete() {
    if (!canCommit) {
      return 0;
    }
    if (!(this instanceof Removeable)) {
      return 0;
    }
    Removeable removeable = (Removeable) this;
    String deleteSQL = ((generateSQL() && removeable.getDeleteSQL() == null) ? getGeneratedSQL(Register.DELETE) : removeable.getDeleteSQL());
    int updateCount = 0;
    try {
      updateCount = executeSQLDelete(deleteSQL, getIdentifier());
    } catch (SQLException e) {
      handleSQLError(new PrintWriter(System.out), e);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return updateCount;
  }

  /**
   * Returns the component class of an array to be populated. If null, it
   * determines by introspection
   */
  protected Class getArrayFieldComponentClass(String fieldName) {
    return null;
  }

  /**
   * Returns the component class of a primitive array to be populated.
   * If the type is a primitive, returns its wrapper class; otherwise, just
   * returns the given class.
   */
  private Class getPrimitiveArrayFieldComponentClass(Class fieldClass) {
    if (fieldClass == double.class || fieldClass == float.class) {
      return Double.class;
    }
    if (fieldClass == long.class) {
      return Long.class;
    }
    if (fieldClass == int.class || fieldClass == short.class) {
      return Integer.class;
    }
    return fieldClass;
  }

  /**
   * Generates the SQL used to populate the VirgoData class by introspection.
   * This is convenience method. Identical to getGeneratedSQL(requestedSQL,
   * null)
   *
   * @see getGeneratedSQL(short requestedSQL, String requestedFieldName)
   */
  private String getGeneratedSQL(short requestedSQL) {
    return getGeneratedSQL(requestedSQL, null);
  }

  /**
   * Generates the SQL used to populate the VirgoData class by introspection.
   * This is done by assuming
   * 1) The populating TABLE has a single primary key
   * 2) The first non-static,non-final protected class argument is the primary
   * key
   * 3) All non-static,non-final protected class arguments have the same names
   * as the corresponding TABLE columns.
   *
   * @param requestedSQL the sql requested
   * @param requestedFieldName for object types (ClobData, BlobData,
   * SequenceField), specifies which field. Not used otherwise, so may be null
   */
  private String getGeneratedSQL(short requestedSQL, String requestedFieldName) {
    return Register.getRegister().getRegisteredStatement(getClass(), requestedFieldName, requestedSQL);
  }

  /**
   * Returns the table name used for generated statements (either getTableName()
   * or class name)
   */
  public String getTableNameForSQL() {
    String tableName = getTableName();
    if (tableName == null) {
      int dot;
      tableName = getClass().getName();
      if ((dot = tableName.lastIndexOf('.')) >= 0) {
        tableName = tableName.substring(dot + 1);
      }
    }
    return tableName;
  }

  /**
   * Returns the registered fields as an array
   */
  public String[] getFieldNames() {
    List fieldNames = new ArrayList();
    Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().
            getName());
    int fieldCount = Register.getRegister().getFieldNameCount(getClass().
            getName());

    for (int i = 0; i < fieldCount; i++) {
      fieldNames.add(Register.getRegister().getFieldName(getClass().
              getName(), i));
    }
    return (String[]) fieldNames.toArray(new String[0]);
  }

  /**
   * Returns the RdbData Item Fields as a single line (CSV)
   *
   * @param delim specifies the delimiter to use between elements
   */
  public String getFieldsAsString(char delim) {
    return getFieldsAsString(delim, false);
  }

  /**
   * Returns the RdbData Item Fields as a single line (CSV)
   *
   * @param delim specifies the delimiter to use between elements
   * @param excelReady specifies whether to format the output so it will import
   * correctly in excel
   */
  public String getFieldsAsString(char delim, boolean excelReady) {
    StringBuffer sb = new StringBuffer();
    Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
    int fieldCount = Register.getRegister().getFieldNameCount(getClass().getName());

    for (int i = 0; i < fieldCount; i++) {
      String fieldName = Register.getRegister().getFieldName(getClass().getName(), i);
      Integer fieldType = (Integer) fieldTable.get(fieldName);
      switch (fieldType.intValue()) {
        case (Register.STRING):
        case (Register.DATE):
        case (Register.TIME):
        case (Register.TIMESTAMP):
        case (Register.BOOLEAN):
        case (Register.LONG):
        case (Register.INTEGER):
        case (Register.DOUBLE):
        case (Register.ORASEQUENCE):
        case (Register.JAVADATE):
        case (Register.SEQUENCE):
        case (Register.CLOBDATA):
          if (sb.length() > 0) {
            sb.append(delim);
          }
          if (excelReady) {
            sb.append(ExtString.getOutputExcelItem(fieldName.toUpperCase().replace('_', ' ')));
          } else {
            sb.append(fieldName.toUpperCase().replace('_', ' '));
          }
          break;
      }
    }
    return sb.toString();
  }

  /**
   * Returns the record as a Stirng in the format
   * name1 <tab> value1
   * name2 <tab> value2 ...
   *
   * Only returns STRING/DATE/LONG/DOUBLE/SEQUENCE types
   */
  public String getFieldNamesAndValues() {
    setData();
    StringBuffer sb = new StringBuffer();
    Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
    int fieldCount = Register.getRegister().getFieldNameCount(getClass().getName());

    try {
      for (int i = 0; i < fieldCount; i++) {
        String fieldName = Register.getRegister().getFieldName(getClass().getName(), i);
        String value = getAsString(fieldName);
        if (value == null || value == "null") {
          value = " ";
        }
        sb.append(fieldName + '\t' + value + '\n');
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return sb.toString();
  }

  /**
   * Returns the record as a single line (CSV)- only returns
   * STRING/LONG/DOUBLE/SEQUENCE types
   *
   * @param delim specifies the delimiter to use between elements
   */
  public String getAsString() {
    return getAsString(',', false);
  }

  /**
   * Returns the record as a single line (CSV)- only returns
   * STRING/LONG/DOUBLE/SEQUENCE types
   *
   * @param delim specifies the delimiter to use between elements
   */
  public String getAsString(char delim) {
    return getAsString(delim, false);
  }

  /**
   * Returns the record as a single line (CSV)- only returns
   * STRING/DATE/LONG/DOUBLE/SEQUENCE types
   *
   * @param delim specifies the delimiter to use between elements
   * @param excelReady specifies whether to format the output so it will import
   * correctly in excel
   */
  public String getAsString(char delim, boolean excelReady) {
    return getAsString(delim, false, excelReady);
  }

  /**
   * Returns the record as a single line (CSV)- only returns
   * STRING/DATE/LONG/DOUBLE/SEQUENCE types
   *
   * @param delim specifies the delimiter to use between elements
   * @param showFieldNames specifies whether to show the field names- useful for
   * debugging
   * @param excelReady specifies whether to format the output so it will import
   * correctly in excel
   */
  public String getAsString(char delim, boolean showFieldNames, boolean excelReady) {
    setData();
    StringBuffer sb = new StringBuffer();
    Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
    int fieldCount = Register.getRegister().getFieldNameCount(getClass().getName());

    try {
      for (int i = 0; i < fieldCount; i++) {
        String fieldName = Register.getRegister().getFieldName(getClass().getName(), i);
        String value = getAsString(fieldName);
        if (value == null || value == "null") {
          value = " ";
        }
        if (sb.length() > 0) {
          sb.append(delim);
        }
        if (showFieldNames) {
          sb.append(fieldName + ": ");
        }
        value = value.replace('"', '\'');
        if (excelReady) {
          sb.append(ExtString.getOutputExcelItem(value));
        } else {
          if (value.indexOf(delim) >= 0) {
            sb.append('"');
            sb.append(value);
            sb.append('"');
          } else {
            sb.append(value);
          }
        }
      }
    } catch (Exception e) {
    }
    return sb.toString();
  }

  /**
   * Returns the field's value as a String using any formatting necessary
   *
   * @param fieldName the name of the field to retrieve
   */
  public String getAsString(String fieldName) {
    Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
    String stringValue = null;
    try {
      Object value = get(fieldName);
      if (value == null) {
        return null;
      }
      Integer fieldType = (Integer) fieldTable.get(fieldName);
      switch (fieldType.intValue()) {
        case (Register.DATE):
        case (Register.TIME):
        case (Register.TIMESTAMP):
          String dateFormatSpecifier = getDateFormatString(fieldName);
          if (dateFormatSpecifier != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatSpecifier);
            stringValue = dateFormat.format((java.util.Date) value);
          } else {
            stringValue = value.toString();
          }
          break;
        case (Register.JAVADATE):
          dateFormatSpecifier = getDateFormatString(fieldName);
          if (dateFormatSpecifier != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatSpecifier);
            stringValue = dateFormat.format(((JavaDate) value).getAsDate());
          } else {
            stringValue = value.toString();
          }
          break;
        case (Register.LONG):
        case (Register.INTEGER):
        case (Register.DOUBLE):
          String numberFormatSpecifier = getNumberFormatString(fieldName);
          if (numberFormatSpecifier != null) {
            DecimalFormat decimalFormat = new DecimalFormat(numberFormatSpecifier);
            stringValue = decimalFormat.format((Number) value);
          } else {
            stringValue = value.toString();
          }
          break;
        case (Register.STRING):
        case (Register.BOOLEAN):
        case (Register.ORASEQUENCE):
        case (Register.SEQUENCE):
        case (Register.CLOBDATA):
          stringValue = value.toString();
          break;
        case (Register.RDBDATA):
          RdbData rdbDataField = (RdbData) value;
          stringValue = "[" + rdbDataField.getAsString() + "]";
          break;
      }
    } catch (Exception e) {
    }
    return stringValue;
  }

  /**
   * Returns the field's value as a String without using any formatting
   *
   * @param fieldName the name of the field to retrieve
   * @return
   */
  public String getUnFormattedString(String fieldName) {
    Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
    Hashtable transientFieldTable = Register.getRegister().getTransientFieldTable(getClass().getName());
    String stringValue = null;
    try {
      Object value = get(fieldName);
      if (value == null) {
        return null;
      }
      Integer fieldType = (Integer) fieldTable.get(fieldName);
      if (fieldType == null) {
        fieldType = (Integer) transientFieldTable.get(fieldName);
      }
      switch (fieldType.intValue()) {
        case (Register.DATE):
        case (Register.TIME):
        case (Register.TIMESTAMP):
        case (Register.LONG):
        case (Register.INTEGER):
        case (Register.DOUBLE):
        case (Register.STRING):
        case (Register.BOOLEAN):
        case (Register.JAVADATE):
        case (Register.ORASEQUENCE):
        case (Register.SEQUENCE):
        case (Register.CLOBDATA):
          stringValue = value.toString();
          break;
        case (Register.RDBDATA):
          stringValue = ((RdbData) value).getIdentifier();
          break;
      }
    } catch (Exception e) {
    }
    return stringValue;
  }

  /**
   * Writes this RdbData object in CSV format using getAsString
   *
   * @param writer a PrintWriter to write the RdbData as CSV
   */
  public void writeCSV(PrintWriter writer) {
    writer.println(getAsString(','));
  }

  /**
   * Writes this RdbData object in XML format. This RdbData object must
   * implement XMLWritable
   * otherwise this just returns. If this RdbData object is an RdbDataArray,
   * each
   * element will be written in XML format surrounded by the Array tag.
   *
   * @param writer a PrintWriter to write the XML
   */
  public void writeXML(PrintWriter writer) {
    if (!(this instanceof XMLWritable)) {
      return;
    }
    RdbXMLWriter xmlWriter = new RdbXMLWriter();
    XMLWritable xmlWritable = (XMLWritable) this;
    String version = xmlWritable.getXMLVersion();
    writer.println("<?xml version=\"" + (version == null ? "1.0" : version) + "\" ?>");
    xmlWriter.writeXML(writer, this);
  }

  /**
   * Returns this RdbData object in XML format. This RdbData object must
   * implement XMLWritable
   * otherwise this just an empty String. If this RdbData object is an
   * RdbDataArray, each
   * element will be written in XML format surrounded by the Array tag.
   * This method must create the String in memory, so for large objects use
   * writeXML to stream this
   * directly to the output.
   */
  public String getXMLString() {
    StringWriter writer = new StringWriter();
    writeXML(new PrintWriter(writer));
    return writer.toString();
  }

  /**
   * Returns this RdbData object as an XMLElement format. This RdbData object
   * must implement XMLWritable
   * otherwise this return null. If this RdbData object is an RdbDataArray, each
   * element will be written in XML format surrounded by the getArrayElement()
   * tag.
   */
  public XMLElement getAsXML() {
    RdbXMLWriter xmlWriter = new RdbXMLWriter();
    return xmlWriter.getXML(this);
  }

  /**
   * Prints the SQLException and details when a SQLException is thrown.
   * Also sets the sqlExceptionThrown and sqlException class variables
   */
  protected void handleSQLError(PrintWriter writer, SQLException e, String fieldName, Integer fieldType) {
    handleSQLError(writer, e, fieldName, (fieldType == null ? 0 : fieldType.intValue()));
  }

  /**
   * Prints the SQLException and details when a SQLException is thrown.
   * Also sets the sqlExceptionThrown and sqlException class variables
   */
  protected void handleSQLError(PrintWriter writer, SQLException e) {
    handleSQLError(writer, e, null, 0);
  }

  /**
   * Prints the SQLException and details when a SQLException is thrown.
   * Also sets the sqlExceptionThrown and sqlException class variables
   */
  protected void handleSQLError(PrintWriter writer, SQLException e, String fieldName, int fieldType) {
    sqlExceptionThrown = true;
    sqlException = e;

    if (sqlExceptionListeners != null) {
      boolean displayError = true;
      for (int i = 0; i < sqlExceptionListeners.size(); i++) {
        SQLExceptionListener listener = (SQLExceptionListener) sqlExceptionListeners.get(i);
        if (!listener.sqlExceptionThrown(this, sqlException)) {
          displayError = false;
        }
      }
      if (!displayError) {
        return;
      }
    }
    writer.println(e.toString());
    writer.println("SQL Error Code: " + e.getErrorCode());
    writer.println("Last SQL Stmt [" + getClass() + "]: (" + connectionPool + ") " + lastSQL);
    String cacheURL = getSQLManager().getCacheURL(connectionPool);
    writer.println("CacheURL: " + connectionPool + " => " + cacheURL);
    if (lastReplacements != null && lastReplacements.size() > 0) {
      writer.println("Last Replacements: ");
      for (Object lastReplacement : lastReplacements) {
        writer.println("Last Replacement: ");
        if (lastReplacement != null) {
          writer.println("[" + lastReplacement + "] Length=" + lastReplacement.toString().length());
        }
      }
    }
    if (fieldName != null) {
      writer.println("Field: " + getClass().getName() + "." + fieldName);
      writer.println("Field Type: " + fieldType);
    }
    e.printStackTrace(writer);
    writer.flush();
  }

  /**
   * Returns whether the given key is in the transient data table.
   */
  public boolean hasTransientData(Object key) {
    if (transientData == null) {
      transientData = new HashMap();
    }
    return transientData.containsKey(key);
  }

  /**
   * Sets a transient data elements. This is a convenience hashtable for holding
   * name/value data which is not
   * persisted in the database.
   */
  public void setTransientData(Object key, Object value) {
    if (transientData == null) {
      transientData = new HashMap();
    }
    transientData.put(key, value);
  }

  /**
   * Gets a transient data element. This is a convenience hashtable for holding
   * name/value data which is not
   * persisted in the database.
   */
  public Object getTransientData(Object key) {
    if (transientData == null) {
      transientData = new HashMap();
    }
    return transientData.get(key);
  }

  /**
   * Gets a transient data keys. This is a convenience hashtable for holding
   * name/value data which is not
   * persisted in the database.
   */
  public Set getTransientKeys() {
    if (transientData == null) {
      transientData = new HashMap();
    }
    return transientData.keySet();
  }

  /**
   * Get a foreign key data item. This is used when an RdbData class contains a
   * foreign key to another RdbData item
   * and the foreign key is maintained as a primitive or String in the class,
   * but it is necessary to instantiate
   * the foreign RdbData item. This method maintains the foreign RdbData object
   * as a transient.
   *
   * For example, AccessEntry contains the String, username, which is a foreign
   * key for User. AccessEntry.getUsername()
   * returns the username as a String. AccessEntry.getUser() returns the User
   * RdbData class for the username by calling
   * this method.
   */
  protected RdbData getForeignKeyData(Class className, String columnName) {
    return getForeignKeyData(className, columnName, "get" + Character.toUpperCase(columnName.charAt(0)) + columnName.substring(1));

  }

  /**
   * Get a foreign key data item. This is used when an RdbData class contains a
   * foreign key to another RdbData item
   * and the foreign key is maintained as a primitive or String in the class,
   * but it is necessary to instantiate
   * the foreign RdbData item. This method maintains the foreign RdbData object
   * as a transient.
   *
   * For example, AccessEntry contains the String, username, which is a foreign
   * key for User. AccessEntry.getUsername()
   * returns the username as a String. AccessEntry.getUser() returns the User
   * RdbData class for the username by calling
   * this method.
   * This method allows the specification of the getter to use to retreive the
   * foreign key
   */
  protected RdbData getForeignKeyData(Class className, String columnName, String fkGetMethod) {
    try {
      String key = className + "." + columnName;
      if (hasTransientData(key)) {
        return (RdbData) getTransientData(key);
      }
      if (!Register.getRegister().isRegistered(className)) {
        className.getConstructor(new Class[0]).newInstance(new Object[0]);
      }

      RdbData rdbData = null;
      Object foreignKey = getClass().getMethod(fkGetMethod, (Class[]) null).invoke(this, (Object[]) null);
      if (foreignKey != null) {
        Class[] args = {
          String.class, SQLManagerIF.class, String.class, String.class};
        rdbData = (RdbData) className.getConstructor(args).newInstance(new Object[]{foreignKey.toString(), getSQLManager(), getLogonUsername(),
                  getConnectionPool()});
      }
      setTransientData(key, rdbData);
      return rdbData;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private String toCSVString(String s) {
    if (s == null) {
      return "null";
    }
    int comma = s.indexOf(',');
    if (comma < 0) {
      return s;
    }
    return '"' + s.replace('"', '\'') + '"';
  }

  /**
   * Checks the RdbData class for errors in mapping (e.g. character length, data
   * precision, ...)
   *
   * @param writer where to write the analysis
   * @return true if the analysis reveals no errors, false otherwise
   */
  protected boolean analyzeForUpdate(PrintWriter writer) {
    boolean error = false;
    writer.println("--------------------------");
    writer.println("CLASS: " + getClass().getName());
    writer.println("\tIdentifier: " + getIdentifier());
    try {
      MetaData metaData = Register.getRegister().getMetaData(this, false, getSQLManager(), getLogonUsername(), getConnectionPool());
      Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
      Iterator fieldNames = Register.getRegister().getFieldNames(getClass().getName());
      while (fieldNames.hasNext()) {
        String fieldName = fieldNames.next().toString();
        Integer fieldType = (Integer) fieldTable.get(fieldName);
        writer.println("--------------------------");
        writer.println("FIELD: " + fieldName);

        ColumnData columnData = metaData.getColumnData(fieldName);
        if (columnData == null) {
          writer.println("\tNo information");
          continue;
        }

        String datatype = columnData.getDataType();
        int precision = columnData.getDataPrecision();
        int charDeclaredLength = columnData.getCharacterColumnDeclaredLength();

        Object fieldValue = getFieldValue(fieldName);
        writer.println("\tDB Data Type:" + datatype);
        writer.println("\tDB Char Length:" + charDeclaredLength);
        writer.println("\tDB Precision:" + precision);

        if (fieldValue == null) {
          writer.println("\tValue is null");
          continue;
        }
        switch (fieldType.intValue()) {
          case (Register.STRING):
            if (fieldValue.toString().length() > charDeclaredLength) {
              writer.println("\tVALUE TOO LARGE: " + fieldValue.toString().length());
              error = false;
            }
            break;
          case (Register.DATE):
            break;
          case (Register.TIME):
            break;
          case (Register.TIMESTAMP):
            break;
          case (Register.BOOLEAN):
            break;
          case (Register.LONG):
            break;
          case (Register.INTEGER):
            break;
          case (Register.DOUBLE):
            break;
          case (Register.JAVADATE):
            break;
          case (Register.ORASEQUENCE):
            break;
          case (Register.RDBDATA):
            break;
          case (Register.SEQUENCE):
            break;
          case (Register.CLOBDATA):
            break;
          case (Register.BLOBDATA):
            break;
          case (Register.XMLDATA):
            break;
          case (Register.HASHTABLE):
            break;
          case (Register.PRIMITIVEARRAY):
            break;
          case (Register.MEMBERARRAY):
            break;
          case (Register.SQLRESULTCOLUMN):
            break;
          default:
        }

      }
    } catch (Exception e) {
      e.printStackTrace();
      error = true;
    }
    writer.flush();
    return !error;
  }

  /**
   * Checks whether this RdbData class contains any RdbData class members
   * which exist in the database. Returned is an array of fieldnames which
   * are valid RdbData class members.
   *
   * This can be useful if you wish to check foreign key
   */
  public String[] hasValidForeignMembers() {
    setData();
    ArrayList validMembers = new ArrayList();
    Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
    int fieldCount = Register.getRegister().getFieldNameCount(getClass().getName());

    try {
      for (int i = 0; i < fieldCount; i++) {
        String fieldName = Register.getRegister().getFieldName(getClass().getName(), i);
        Integer fieldType = (Integer) fieldTable.get(fieldName);
        switch (fieldType.intValue()) {
          case (Register.STRING):
          case (Register.DATE):
          case (Register.TIME):
          case (Register.TIMESTAMP):
          case (Register.BOOLEAN):
          case (Register.LONG):
          case (Register.INTEGER):
          case (Register.DOUBLE):
          case (Register.JAVADATE):
          case (Register.ORASEQUENCE):
          case (Register.RDBDATA):
          case (Register.SEQUENCE):
          case (Register.CLOBDATA):
          case (Register.BLOBDATA):
          case (Register.XMLDATA):
          case (Register.HASHTABLE):
          case (Register.PRIMITIVEARRAY):
          case (Register.SQLRESULTCOLUMN):
            break;
          case (Register.MEMBERARRAY):
            RdbData[] members = (RdbData[]) get(fieldName);
            if (members != null && members.length > 0) {
              validMembers.add(fieldName);
            }
            break;
          default:
            break;
        }
      }
    } catch (Exception e) {
    }
    return (String[]) validMembers.toArray(new String[0]);
  }

  /*
   * Comparable interface implementations
   */
  public int compareTo(Object obj) {
    if (obj == null) {
      return 0;
    }
    return getIdentifier().compareTo(obj.toString());
  }

  /*
   * BELOW ARE THE METHODS WHICH MUST BE IMPLEMENTED OR CAN BE OVERRIDDEN
   */
  /**
   * Returns the primary key fields for the RdbData class which are used to
   * generate the SQL statement(s). If this returns null (the Default), the
   * first field of the class is assumed.
   * The getIdentifier() method must return, in CSV, the matching number of
   * elements as this array if generated SQL is used.
   */
  public String[] getPrimaryKeyFields() {
    return null;
  }

  /**
   * Sets the primary key of the RgbData object- Default implementation does
   * nothing;
   */
  public void setIdentifier(String id) {
  }

  /**
   * Returns whether the generating SQL command should be generated by
   * introspection (see above for details)
   * Default implementation returns true. This can be changed either by
   * overriding this method or by
   * returning a String in the getSQL() method.
   */
  protected boolean generateSQL() {
    return true;
  }

  /**
   * Returns the table name used when generating the SQL command. If this
   * returns null, the Class name
   * is assumed to be the table name. Not used if generateSQL() is false or
   * getSQL() is not null. Default implementation returns null.
   */
  protected String getTableName() {
    return null;
  }

  /**
   * Returns the SQL statement to be used to populate this RgbData object. If
   * null and generateSQL() is true,
   * the statement is generated using the getTableName() and introspection of
   * the class. Default
   * implementation returns null.
   */
  protected String getSQL() {
    return null;
  }

  /**
   * Returns a format String used either
   * To parse DateTime/Timestamp values when these fields get set by a call to
   * set(), setValues(), or setFieldValue() and the parameter passed into the
   * method is a String.
   * The default implementation returns null. When this returns null, the
   * default method for parsing
   * Date/Time/Timestamp values set using a String is by the appropriate
   * valueOf() method:
   * java.sql.Date.valueOf(<String>)
   * java.sql.Time.valueOf(<String>)
   * java.sql.Timestamp.valueOf(<String>)
   *
   * If the String format does not match the required, parsable format, the set
   * opertaion will fail
   * with a ParseException.
   * See the appropriate valueOf() method's documentation for the required
   * format.
   *
   * OR
   * To format the date in a call to getAsString()
   *
   * See java.text.DateFormat for the format specifiers which can be returned by
   * this method to customize
   * this parsing.
   *
   *
   */
  protected String getDateFormatString(String fieldName) {
    return null;
  }

  /**
   * Returns a format String used either
   * To parse Number values when these fields get set by a call to
   * set(), setValues(), or setFieldValue and the parameter passed into the
   * method is a String.
   * The default implementation returns null. When this returns null, the
   * default method for parsing
   * Number values set using a String is by the Double.valueOf() method.
   *
   * If the String format does not match the required, parsable format, the set
   * opertaion will fail
   * with a ParseException.
   *
   * OR
   * To format the number in a call to getAsString()
   *
   * See java.text.DecimalFormat for the format specifiers which can be returned
   * by this method to customize
   * this parsing.
   *
   *
   */
  protected String getNumberFormatString(String fieldName) {
    return null;
  }

  /**
   * Returns SQL statements used to populate Object members of the RgbData
   * object.
   * If the field is for an RdbData array, the default implementation returns a
   * SQL statement of the form:
   * SELECT <MEMBER FIELD CLASS PRIMARY KEY(S)> FROM <MEMBER FIELD CLASS TABLE>
   * WHERE <THIS CLASS PRIMARY KEY>=?
   * So, this default implementation assumes the name of the field in this class
   * corresponds to the primary key in the
   * member field class.
   *
   * If the field is not an RdbData array, this returns null by default.
   */
  protected String getMemberSQL(String fieldName) {
    try {
      Field field = getClass().getDeclaredField(fieldName);
      Class fieldClass = field.getType();
      Class rdbClass = fieldClass;
      if (fieldClass.isArray()) {
        rdbClass = fieldClass.getComponentType();
      }
      if (Register.getRegister().getClassType(rdbClass) != Register.RDBDATA) {
        return null;
      }
      RdbData rdbObj = (RdbData) rdbClass.getConstructor((Class[]) null).newInstance((Object[]) null);
      String[] fieldPrimaryKeys = rdbObj.getPrimaryKeyFields();
      if (fieldPrimaryKeys == null) {
        String primaryField = Register.getRegister().getFieldName(rdbClass.getName(), 0);
        if (primaryField != null) {
          fieldPrimaryKeys = new String[]{
            primaryField};
        }
      }

      String foreignKeyForMemberArray = getForeignKeyForMemberArray(fieldName);
      if (foreignKeyForMemberArray == null) {
        String[] primaryKeys = this.getPrimaryKeyFields();
        if (primaryKeys == null || primaryKeys.length == 0) {
          String primaryField = Register.getRegister().getFieldName(this.getClass().getName(), 0);
          if (primaryField != null) {
            primaryKeys = new String[]{
              primaryField};
          }
        }
        if (primaryKeys.length > 1) {
          throw new IllegalArgumentException(
                  "Can no generate member SQL for multi-column primary keys");
        }
        foreignKeyForMemberArray = primaryKeys[0];
      }
      StringBuffer selectSQL = new StringBuffer("SELECT ");
      selectSQL.append(ExtString.join(fieldPrimaryKeys, ','));
      selectSQL.append(" FROM " + rdbObj.getTableNameForSQL());
      selectSQL.append(" WHERE " + foreignKeyForMemberArray + "=?");
      return selectSQL.toString();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Returns the foreign key (column name) to map a member RdbData array to this
   * class. This is used to create the SQL used to populate the member array if
   * the implementation does not override getMemberSQL(). If this returns null,
   * this RdbData class' primary key is used.
   *
   * @param fieldName
   * @return
   */
  protected String[] getMemberSQLValues(String fieldName) {
    return new String[]{
              getIdentifier()};
  }

  /**
   * Returns the foreign key (column name) to map a member RdbData array to this
   * class. This is used to create the SQL used to populate the member array if
   * the implementation does not override getMemberSQL(). If this returns null,
   * this RdbData class' primary key is used.
   *
   * @param fieldName
   * @return
   */
  protected String getForeignKeyForMemberArray(String fieldName) {
    return null;
  }

  /**
   * Adds a SQLExceptionListener to the object.
   *
   * @param sqlExceptionListener
   */
  public void addSQLExceptionListener(SQLExceptionListener sqlExceptionListener) {
    if (sqlExceptionListeners == null) {
      sqlExceptionListeners = new ArrayList();
    }
    sqlExceptionListeners.add(sqlExceptionListener);
  }

  /**
   * Adds a DataSetListener to the object.
   *
   * @param listener
   */
  public void addDataSetListener(DataSetListener listener) {
    if (dataSetListeners == null) {
      dataSetListeners = new ArrayList();
    }
    dataSetListeners.add(listener);
  }

  protected void fireDataSetEvent(boolean success) {
    if (dataSetListeners != null) {
      DataSetEvent event = new DataSetEvent(this, success);
      for (int i = 0; i < dataSetListeners.size(); i++) {
        DataSetListener listener = (DataSetListener) dataSetListeners.get(i);
        listener.dataSet(event);
      }
    }
    dataSet(success);
  }

  /**
   * Removes a SQLExceptionListener to the object.
   *
   * @param sqlExceptionListener
   */
  public void removeSQLExceptionListener(SQLExceptionListener sqlExceptionListener) {
    if (sqlExceptionListeners == null) {
      return;
    }
    sqlExceptionListeners.remove(sqlExceptionListener);
  }

  public static void closeResources(ResultSet rset) {
    Connection conn = null;
    Statement stmt = null;
    try {
      stmt = rset.getStatement();
      conn = stmt.getConnection();
      rset.close();
    } catch (Exception e) {
      //e.printStackTrace();
    } finally {
      OraSQLManager.close(stmt);
      OraSQLManager.close(conn);
    }
  }

  /**
   * A synchronized method to return the current millis. The synchronized
   * keyword and the
   * Thread pause ensures that the timestamp is unique within the VM
   *
   * @return long
   */
  public static synchronized long getUniqueTimestamp() {
    try {
      Thread.currentThread().sleep(50);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return System.currentTimeMillis();
  }

  /**
   * Returns whether the member array should allow repeated members or if the
   * array should be only of distinct members
   * If this returns false (the default), all members returned by the
   * getMemberSQL(String) SQL statement are set for the array.
   * If this returns true, only members with unique identifiers are set in the
   * array.
   *
   * Note: This could be handled by a DISTINCT qualifier in the SQL statement.
   * This may be faster in some cases.
   */
  protected boolean getDistinctMemberArray(String field) {
    return false;
  }

  /**
   * Returns the maximum number of members the given array can contain
   * return -1 (the default) for unlimited.
   */
  protected int getMaximumArraySize(String field) {
    return -1;
  }

  /**
   * Called if the limit as set by getMaximumArraySize(String field) was reached
   * when populating a member arrays
   *
   * @param rdbData the RdbData class
   * @param arrayFieldName the array field name
   */
  protected void notifyMemberArrayLimitReached(RdbData rdbData, String arrayFieldName) {
  }

  /**
   * Returns the primary key of the RgbData object
   */
  public abstract String getIdentifier();

  /**
   * Defines the field setter for this class
   * For most objects, the following implementation is all that's required
   * field.set(this, value);
   *
   * Explanation:
   * Fields set by the RdbData class have protected access to limit scope. But
   * java access rules
   * for protected access class variables only permit base-class access of a
   * sub-class' class
   * variables within the same package. Therefore, this is necessary. However,
   * this class can be subclassed
   * in your data package with an abstract class with this method given an
   * implementation so all
   * rgb-data objects do not have to include the 1 line implementation above.
   */
  protected abstract void setFieldValue(Field field, Object value) throws IllegalAccessException;

  /**
   * Defines the field getter for this class
   * For most objects, the following implementation is all that's required
   * field.get(this);
   *
   * This is only used for Saveable objects.
   * Explanation:
   * Fields set by the RdbData class have protected access to limit scope. But
   * java access rules
   * for protected access class variables only permit base-class access of a
   * sub-class' class
   * variables within the same package. Therefore, this is necessary. However,
   * this class can be subclassed
   * in your data package with an abstract class with this method given an
   * implementation so all
   * rgb-data objects do not have to include the 1 line implementation above.
   */
  protected abstract Object getFieldValue(Field field) throws IllegalAccessException;

  /**
   * Prints the full RdbData object to the writer including all arrays-
   *
   */
  public void printDebugString(PrintWriter writer) {
    writer.println(getClass().getName());
    writer.println("=================================");
    printDebugString(writer, 0);
  }

  /**
   * Prints the full RdbData object to the writer including all arrays-
   *
   */
  private void printDebugString(PrintWriter writer, int level) {
    setData();
    Hashtable fieldTable = Register.getRegister().getFieldTable(getClass().getName());
    int fieldCount = Register.getRegister().getFieldNameCount(getClass().getName());
    StringBuffer sb = new StringBuffer(0);
    sb.setLength(1000);
    String padding = sb.toString().replaceAll("\0", " ");

    for (int i = 0; i < fieldCount; i++) {
      String fieldName = Register.getRegister().getFieldName(getClass().getName(), i);
      Integer fieldType = (Integer) fieldTable.get(fieldName);
      switch (fieldType.intValue()) {
        case (Register.DATE):
        case (Register.TIME):
        case (Register.TIMESTAMP):
        case (Register.LONG):
        case (Register.INTEGER):
        case (Register.DOUBLE):
        case (Register.STRING):
        case (Register.BOOLEAN):
        case (Register.JAVADATE):
        case (Register.ORASEQUENCE):
        case (Register.SEQUENCE):
        case (Register.CLOBDATA):
          writer.println(padding.substring(0, 4 * level) + fieldName + "=" + getAsString(fieldName));
          writer.println(padding.substring(0, 4 * level) + "=================================");
          break;
        case (Register.RDBDATA):
          RdbData member = (RdbData) get(fieldName);
          if (member == null) {
            writer.println(padding.substring(0, 4 * level) + fieldName);
            writer.println(padding.substring(0, 4 * level) + "---------------------------------");
          } else {
            writer.println(padding.substring(0, 4 * level) + fieldName + " (" + member.getClass().getName() + ")");
            writer.println(padding.substring(0, 4 * level) + "---------------------------------");
            member.printDebugString(writer, level + 1);
          }
          break;
        case (Register.BLOBDATA):
          writer.println(padding.substring(0, 4 * level) + fieldName + "=Blob Size: " + ((BlobData) get(fieldName)).getSize());
          writer.println(padding.substring(0, 4 * level) + "=================================");
          break;
        case (Register.MEMBERARRAY):
          writer.println(padding.substring(0, 4 * level) + fieldName);
          writer.println(padding.substring(0, 4 * level) + "---------------------------------");
          RdbData[] data = (RdbData[]) get(fieldName);
          if (data == null) {
            writer.println(padding.substring(0, 4 * (level + 1)) + "None");
          } else {
            for (int j = 0; j < data.length; j++) {
              writer.println(padding.substring(0, 4 * (level + 1)) + fieldName + "[" + j + "] (" + data[j].getClass().getName() + ")");
              writer.println(padding.substring(0, 4 * (level + 1)) + "---------------------------------");
              data[j].printDebugString(writer, level + 2);
            }
          }
          writer.println(padding.substring(0, 4 * level) + "=================================");
          break;
        case (Register.PRIMITIVEARRAY):
          writer.println(padding.substring(0, 4 * level) + fieldName);
          writer.println(padding.substring(0, 4 * level) + "---------------------------------");
          Object[] primitiveMembers = (Object[]) get(fieldName);
          if (primitiveMembers == null) {
            writer.println(padding.substring(0, 4 * (level + 1)) + "None");
          } else {
            for (int j = 0; j < primitiveMembers.length; j++) {
              writer.println(padding.substring(0, 4 * (level + 1)) + fieldName + "[" + j + "]");
              writer.println(padding.substring(0, 4 * (level + 1)) + "---------------------------------");
              writer.println(padding.substring(0, 4 * (level + 2)) + primitiveMembers[j]);
            }
          }
          writer.println(padding.substring(0, 4 * level) + "=================================");
          break;
      }
    }
  }
}
