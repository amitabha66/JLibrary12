package amgen.ri.oracle;

import amgen.ri.rdb.SQLManagerIF;
import amgen.ri.sql.SQLProvider;
import amgen.ri.sql.SQLQuery;
import amgen.ri.util.ExtArray;
import amgen.ri.util.ExtString;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.sql.BLOB;
import oracle.sql.CLOB;
import oracle.xdb.XMLType;

/**
 * OraSQLManager Description: Oracle implementation of the SQLManagerIF
 * Interface. Handles low level JDBC calls and interaction with the
 * OraConnectionManager (the connection caching system). Also handles
 * PreparedStatement caching
 *
 * @author Jeffrey A. McDowell
 * @version 1.0
 */
public final class OraSQLManager implements SQLManagerIF {
  //DBConnection Manager
  protected static OracleConnectionManagerIF oraConnectionManager;
  protected static Map dataSourceMap = new HashMap();
  //Map of types to pools
  protected HashMap poolTypeMap = new HashMap();
  private int queryTimeoutSecs = 1800;
  private short debug = 0;

  /**
   * Creates a SQLManager instance using the singleton instance of
   * OraConnectionManager.
   */
  public OraSQLManager() {
    this(OraConnectionManagerFactory.getFactory().getOracleConnectionManager());
  }

  /**
   * Creates a SQL session passing in the OraConnectionManager
   *
   * @param oraConnectionManager the OraConnectionManager to use in the session
   */
  private OraSQLManager(OracleConnectionManagerIF oraConnectionManager) {
    this.oraConnectionManager = oraConnectionManager;
  }

  /**
   * Creates a SQL session passing in the OraConnectionManager
   *
   * @param oraConnectionManager the OraConnectionManager to use in the session
   */
  public OraSQLManager(DataSource ds, String dataSourceName) {
    dataSourceMap.put(dataSourceName, ds);
  }

  /**
   * Creates a SQL session passing in the OraConnectionManager
   *
   * @param oraConnectionManager the OraConnectionManager to use in the session
   */
  public OraSQLManager(Map dataSourceMap) {
    this.dataSourceMap.putAll(dataSourceMap);
  }

  /**
   * Checks the status of the connection pool. true if the connection pool
   * appears available; false otherwise
   */
  public boolean isAvailable(String connectionPoolName) {
    Connection connection = null;
    try {
      connection = getConnection(connectionPoolName);
      connection.getCatalog();
    } catch (Exception e) {
      return false;
    } finally {
      closeResources(connection);
    }
    return true;
  }

  /**
   * Closes the session
   */
  public void close() {
  }

  /**
   * Sets the debug setting
   *
   * @param b
   */
  public void setDebug(int b) {
    debug = (short) b;
  }

  /**
   * Retrieves the oracle JDBC-thin client connection for the session- logging
   * in if not previously done so
   */
  public Connection getConnection(String connectionPoolName) throws SQLException {
    Connection connection = oraConnectionManager.getConnection(connectionPoolName);
    if (connection == null) {
      throw new SQLException("Unable to create/retrieve connection for connection pool " + connectionPoolName);
    }
    return connection;
  }

  /**
   * Returns whether a pool exists in the ConnectionManager.
   *
   * @param name The pool name to find
   * @return true if the pool exists, false otherwise
   */
  public boolean cacheExist(String name) {
    if (oraConnectionManager != null) {
      return oraConnectionManager.cacheExist(name);
    } else {
      return dataSourceMap.containsKey(name);
    }
  }

  /**
   * Returns the pools available in this ConnectionManager.
   */
  public String[] getCacheList() {
    if (oraConnectionManager != null) {
      return oraConnectionManager.getCacheList();
    } else {
      return (String[]) dataSourceMap.keySet().toArray(new String[0]);
    }
  }

  /**
   * Returns the URL for a given cache
   */
  public String getCacheURL(String cache) {
    String url = null;
    try {
      if (oraConnectionManager != null) {
        url = oraConnectionManager.getCacheURL(cache);
      } else {
        DataSource ds = (DataSource) dataSourceMap.get(cache);
        if (ds != null) {
          Connection conn = ds.getConnection();
          url = conn.getMetaData().getURL();
          conn.close();
        }
      }
    } catch (Exception e) {
    }
    return url;
  }

  /**
   * Retrieves the prepared statement for the query. If already prepared, it
   * returns the handle from the table. Otherwise, it prepares the statement,
   * enters it in the table, and returns the handle.
   */
  public PreparedStatement getStatement(String query, String connectionPoolName) throws SQLException {
    Connection connection = getConnection(connectionPoolName);
    try {
      return connection.prepareStatement(query);
    } catch (SQLException sqle) {
      closeResources(connection);
      throw sqle;
    }
  }

  /**
   * Runs a query given by query which has replacement fields which is set to
   * elements in replacementTag. The query is used to create a PreparedStatement
   * for efficient future use. When the query is no longer needed, it should be
   * removed by a call to removeUserQuery(query) to free the resources used by
   * the PreparedStatement.
   *
   * @param query the sql statement to run which includes a single replacement
   * tag (?)
   */
  public ResultSet executeQuery(String query, String replacementTag, String connectionPoolName) throws SQLException {
    Connection connection = getConnection(connectionPoolName);
    try {
      PreparedStatement stmt = getStatement(query, connection);
      if (replacementTag != null) {
        int numTags = ExtString.countChar(query, '?');
        String[] st = ExtString.splitCSV(replacementTag);
        if (st.length < numTags) {
          throw new SQLException("Insufficient number of replacements");
        }
        for (int i = 1; i <= numTags; i++) {
          stmt.setString(i, st[i - 1]);
        }
      }
      return stmt.executeQuery();
    } catch (SQLException sqle) {
      closeResources(connection);
      throw sqle;
    }
  }

  /**
   * Runs a query given by query which has replacement fields which is set to
   * elements in replacementTag. The query is used to create a PreparedStatement
   * for efficient future use. When the query is no longer needed, it should be
   * removed by a call to removeUserQuery(query) to free the resources used by
   * the PreparedStatement.
   *
   * @param query the sql statement to run which includes a replacement tags (?)
   * @param replacementTag array of replacements which correspond to the
   * replacement tags (?) in the SQL statement
   * @param connectionPoolName
   */
  public ResultSet executeQuery(String query, String[] replacementTag, String connectionPoolName) throws SQLException {
    Connection connection = getConnection(connectionPoolName);
    try {
      PreparedStatement stmt = getStatement(query, connection);
      if (replacementTag != null) {
        int numTags = Math.min(replacementTag.length, ExtString.countChar(query, '?'));
        for (int i = 1; i <= numTags; i++) {
          stmt.setString(i, replacementTag[i - 1]);
        }
      }
      return stmt.executeQuery();
    } catch (SQLException sqle) {
      closeResources(connection);
      throw sqle;
    }
  }

  /**
   * Runs a query given by query which has no replacement variables.
   *
   * @param query SQLQuery
   * @param connectionPoolName String
   * @throws SQLException
   * @return ResultSet
   */
  public ResultSet executeQuery(SQLQuery query, String connectionPoolName) throws SQLException {
    return executeQuery(query.getSql(), connectionPoolName);
  }

  /**
   * Runs a query which has replacement variables with values defined in
   * replacements
   *
   * @param query SQLQuery
   * @param replacements Map
   * @param connectionPoolName String
   * @return ResultSet
   * @throws SQLException
   */
  public ResultSet executeQuery(SQLQuery query, Map<String, Object> replacements, String connectionPoolName) throws SQLException {
    if (query.getVariables().size() == 0) {
      return executeQuery(query, connectionPoolName);
    }
    Connection connection = getConnection(connectionPoolName);
    try {
      OraclePreparedStatement stmt = (OraclePreparedStatement) getStatement(query.getSql(), connection);
      for (String variableName : query.getVariables()) {
        if (!replacements.containsKey(variableName) || replacements.get(variableName) == null) {
          throw new SQLException("Missing replacement " + variableName);
        }
        Object value = replacements.get(variableName);
        if (value instanceof Integer) {
          stmt.setIntAtName(variableName, ((Number) value).intValue());
        } else if (value instanceof Double) {
          stmt.setDoubleAtName(variableName, ((Number) value).doubleValue());
        } else if (value instanceof CLOB) {
          stmt.setCLOBAtName(variableName, (CLOB) value);
        } else if (value instanceof BLOB) {
          stmt.setBLOBAtName(variableName, (BLOB) value);
        } else {
          stmt.setStringAtName(variableName, value.toString());
        }
      }
      return stmt.executeQuery();
    } catch (SQLException sqle) {
      closeResources(connection);
      throw sqle;
    }
  }

  /**
   * Runs a query which has replacement variables with values defined in
   * replacements
   *
   * @param sqlProvider SQLQuery
   * @param sqlQueryName String
   * @param replacements Map
   * @param connectionPoolName String
   * @return ResultSet
   * @throws SQLException
   */
  public ResultSet executeQuery(SQLProvider sqlProvider, String sqlQueryName, Map<String, Object> replacements, String connectionPoolName) throws SQLException {
    SQLQuery query = sqlProvider.getSQLQuery(sqlQueryName);
    if (query == null) {
      throw new NullPointerException(sqlQueryName + " not found");
    }
    return executeQuery(query, replacements, connectionPoolName);

  }

  /**
   * Runs a query which has replacement variables with values defined in
   * replacements. The replacements are defined by an array with members of the
   * form <replacement name>:<value>. The replacement is always added as a
   * String.
   *
   * @param query SQLQuery
   * @param replacements Map
   * @param connectionPoolName String
   * @return ResultSet
   * @throws SQLException
   */
  public ResultSet executeQuery(SQLQuery query, String[] replacements, String connectionPoolName) throws SQLException {
    Map<String, Object> replacementMap = new HashMap<String, Object>();
    if (ExtArray.hasLength(replacements)) {
      for (String replacement : replacements) {
        String[] keyValue = replacement.split("[:=]", 2);
        if (keyValue.length == 2) {
          replacementMap.put(keyValue[0], keyValue[1]);
        }
      }
    }
    return executeQuery(query, replacementMap, connectionPoolName);
  }

  /**
   * Runs a query which has replacement variables with values defined in
   * replacements. The replacements are defined by an array with members of the
   * form <replacement name>:<value>. The replacement is always added as a
   * String.
   *
   * @param sqlProvider SQLQuery
   * @param sqlQueryName String
   * @param replacements Map
   * @param connectionPoolName String
   * @return ResultSet
   * @throws SQLException
   */
  public ResultSet executeQuery(SQLProvider sqlProvider, String sqlQueryName, String[] replacements, String connectionPoolName) throws SQLException {
    SQLQuery query = sqlProvider.getSQLQuery(sqlQueryName);
    if (query == null) {
      throw new NullPointerException(sqlQueryName + " not found");
    }
    return executeQuery(query, replacements, connectionPoolName);
  }

  /**
   * Runs a query given by query which has replacement fields which is set to
   * elements in replacementTag. The query is used to create a PreparedStatement
   * for efficient future use. When the query is no longer needed, it should be
   * removed by a call to removeUserQuery(query) to free the resources used by
   * the PreparedStatement.
   *
   * @param query the sql statement to run which includes a replacement tags (?)
   * @param replacementTag array of replacements which correspond to the
   * replacement tags (?) in the SQL statement
   * @param connectionPoolName
   */
  public ResultSet executeQueryTrailingNulls(String query, String[] replacementTag, String connectionPoolName) throws SQLException {
    Connection connection = getConnection(connectionPoolName);
    try {
      PreparedStatement stmt = getStatement(query, connection);
      if (replacementTag != null) {
        int numTags = ExtString.countChar(query, '?');
        for (int i = 1; i <= numTags; i++) {
          stmt.setString(i, replacementTag[i - 1]);
        }
      }
      return stmt.executeQuery();
    } catch (SQLException sqle) {
      closeResources(connection);
      throw sqle;
    }
  }

  /**
   * Runs a given query which contains no replacement tags
   *
   * @param query SELECT SQL without replacement tags (?)
   * @param connectionPoolName
   */
  public ResultSet executeQuery(String query, String connectionPoolName) throws SQLException {
    Connection connection = getConnection(connectionPoolName);
    try {
      ResultSet rset = getStatement(query, connection).executeQuery();
      return rset;
    } catch (SQLException sqle) {
      closeResources(connection);
      throw sqle;
    }
  }

  /**
   * Runs an update DML given by query which has replacement fields which is set
   * to elements in replacementTag. The query is used to create a
   * PreparedStatement for efficient future use. When the query is no longer
   * needed, it should be removed by a call to removeUserQuery(query) to free
   * the resources used by the PreparedStatement.
   *
   * @param query the sql statement to run which includes a single replacement
   * tag (?)
   */
  public int executeUpdate(String query, String connectionPoolName) throws SQLException {
    return executeUpdate(query, new Object[0], connectionPoolName);
  }

  /**
   * Runs an update DML given by query which has replacement fields which is set
   * to elements in replacementTag. The query is used to create a
   * PreparedStatement for efficient future use. When the query is no longer
   * needed, it should be removed by a call to removeUserQuery(query) to free
   * the resources used by the PreparedStatement.
   *
   * @param query the sql statement to run which includes a single replacement
   * tag (?)
   */
  public int executeUpdate(String query, String replacementTag, String connectionPoolName) throws SQLException {
    if (replacementTag != null) {
      String[] st = ExtString.splitCSV(replacementTag);
      return executeUpdate(query, st, connectionPoolName);
    }
    return executeUpdate(query, new Object[0], connectionPoolName);

  }

  /**
   * Runs an update DML given by query which has replacement fields which is set
   * to elements in replacementTag. The query is used to create a
   * PreparedStatement for efficient future use. When the query is no longer
   * needed, it should be removed by a call to removeUserQuery(query) to free
   * the resources used by the PreparedStatement.
   *
   * @param query the sql statement to run which includes a single replacement
   * tag (?)
   */
  public int executeUpdate(String query, Object[] replacementTags, String connectionPoolName) throws SQLException {
    Connection connection = getConnection(connectionPoolName);
    try {
      PreparedStatement stmt = getStatement(query, connection);
      if (replacementTags != null) {
        int numTags = ExtString.countChar(query, '?');
        if (replacementTags.length < numTags) {
          throw new SQLException("Insufficient number of replacements");
        }
        for (int i = 1; i <= numTags; i++) {
          Object replacementTag = replacementTags[i - 1];
          if (replacementTag == null) {
            replacementTag = "null";
          }
          if (replacementTag instanceof String) {
            String st = (String) replacementTag;
            if (st.equals("null")) {
              stmt.setNull(i, Types.VARCHAR);
            } else {
              stmt.setString(i, st);
            }
          } else if (replacementTag instanceof XMLType) {
            stmt.setObject(i, replacementTag);
          } else if (replacementTag instanceof java.sql.Date) {
            stmt.setDate(i, (java.sql.Date) replacementTag);
          } else if (replacementTag instanceof java.sql.Timestamp) {
            stmt.setTimestamp(i, (java.sql.Timestamp) replacementTag);
          }
        }
      }
      int ret = stmt.executeUpdate();
      stmt.close();
      return ret;
    } catch (SQLException sqle) {
      throw sqle;
    } finally {
      closeResources(connection);
    }
  }

  /**
   * Runs a query which returns the next value for the given Oracle sequence
   * object
   */
  public long getNextSequenceValue(String sequenceName, String connectionPoolName) throws SQLException {
    String sql = "SELECT " + sequenceName + ".NEXTVAL FROM DUAL";
    Connection connection = getConnection(connectionPoolName);
    PreparedStatement stmt = null;
    try {
      stmt = getStatement(sql, connection);
      ResultSet rset = stmt.executeQuery();
      if (!rset.next()) {
        throw new SQLException("No sequence value returned.");
      }
      long val = rset.getLong(1);
      close(rset);
      return val;
    } catch (SQLException sqle) {
      throw sqle;
    } finally {
      close(stmt);
      close(connection);
    }
  }

  /**
   * Performs an INSERT into a table with a CLOB. Must provide a normal INSERT
   * statement (with the EMPTY_CLOB() operator) and a SELECT statement which
   * selects the CLOB element which will be updated with data from the
   * clobReader. For example: insertQuery: "INSERT INTO RESULTRECORDS
   * (ENTRY_DATE,USERNAME,RESULT_COMMENT,RESULT) VALUES (?,?,?, EMPTY_CLOB())"
   * insertQueryReplacementTag: "101010,user,comment" clobSelectQuery: "SELECT
   * RESULT FROM RESULTRECORDS WHERE ENTRY_DATE=?";" clobSelectReplacementTag:
   * "101010"
   *
   * @param insertQuery INSERT query used to insert non-LOB data and contains
   * the EMPTY_CLOB() operator
   * @param insertQueryReplacementTag replacements for '?' in the insertQuery
   * @param clobSelectQuery a SELECT query to retrieve the newly created
   * EMPTY_CLOB which will be updated with content from clobReader
   * @param clobSelectReplacementTag replacements for the '?' in the
   * clobSelectQuery (e.g. the primary key)
   * @param clobReader the reader whose data will be inserted into the new CLOB
   */
  public int executeClobInsert(String insertQuery, String insertQueryReplacementTag, String clobSelectQuery, String clobSelectReplacementTag, Reader clobReader,
          String connectionPoolName) throws SQLException, IOException {
    if (executeUpdate(insertQuery, insertQueryReplacementTag, connectionPoolName) == 0) {
      return 0;
    }
    return executeClobUpdate(clobSelectQuery, clobSelectReplacementTag, clobReader, connectionPoolName);
  }

  /**
   * Performs an UPDATE into a table entry with a CLOB. Must provide a SELECT
   * statement which selects the CLOB element which will be updated with data
   * from the clobReader. For example: clobSelectQuery: "SELECT RESULT FROM
   * RESULTRECORDS WHERE ENTRY_DATE=?";" clobSelectReplacementTag: "101010"
   *
   * @param clobSelectQuery a SELECT query to retrieve the CLOB element which
   * will be updated with content from clobReader
   * @param clobSelectReplacementTag replacements for the '?' in the
   * clobSelectQuery (e.g. the primary key)
   * @param clobReader the reader whose data will be inserted into the new CLOB
   */
  public int executeClobUpdate(String clobSelectQuery, String clobSelectReplacementTag, Reader clobReader, String connectionPoolName) throws SQLException, IOException {
    Connection connection = getConnection(connectionPoolName);
    try {
      char c;
      connection.setAutoCommit(false);
      String clobSelect = clobSelectQuery.toUpperCase().trim();
      if (!clobSelect.endsWith("FOR UPDATE")) {
        clobSelect = clobSelect.concat(" FOR UPDATE");
      }
      OracleResultSet rset = (OracleResultSet) executeQuery(clobSelect, clobSelectReplacementTag, connection);
      if (!rset.next()) {
        return 0;
      }
      CLOB clob = (CLOB) rset.getClob(1);

      BufferedWriter writer = new BufferedWriter(clob.setCharacterStream(0L), clob.getBufferSize());
      BufferedReader reader = new BufferedReader(clobReader, clob.getBufferSize());

      StringBuffer sb = new StringBuffer();
      while ((c = (char) reader.read()) != (char) - 1) {
        writer.write(c);
        sb.append(c);
      }
      writer.close();
      clobReader.close();
      connection.commit();
      connection.setAutoCommit(true);
    } catch (SQLException sqle) {
      throw sqle;
    } catch (IOException ioe) {
      throw ioe;
    } finally {
      closeResources(connection);
    }
    return 1;
  }

  /**
   * Appends data from the given Reader to a CLOB field. Must provide a SELECT
   * statement which selects the CLOB element which will be updated with data
   * from the clobReader. For example: clobSelectQuery: "SELECT RESULT FROM
   * RESULTRECORDS WHERE ENTRY_DATE=?";" clobSelectReplacementTag: "101010" This
   * method just uses a buffer to read all character from the Reader and calls
   * executeClobAppend(String, String, char[], String)
   *
   * @param clobSelectQuery a SELECT query to retrieve the CLOB element which
   * will be updated with content from clobReader
   * @param clobSelectReplacementTag replacements for the '?' in the
   * clobSelectQuery (e.g. the primary key)
   * @param appendDataReader Reader with the data to append to the CLOB
   * @param connectionPoolName the name fo the connection pool
   */
  public int executeClobAppend(String clobSelectQuery, String clobSelectReplacementTag, Reader appendDataReader, String connectionPoolName) throws SQLException,
          IOException {
    StringBuffer sb = new StringBuffer();
    BufferedReader buffReader = new BufferedReader(appendDataReader);
    char c;
    while ((c = (char) buffReader.read()) != (char) - 1) {
      sb.append(c);
    }
    buffReader.close();
    char[] appendData = new char[sb.length()];
    sb.getChars(0, sb.length(), appendData, 0);
    return executeClobAppend(clobSelectQuery, clobSelectReplacementTag, appendData, connectionPoolName);
  }

  /**
   * Appends an array of characters to a CLOB field. Must provide a SELECT
   * statement which selects the CLOB element which will be updated with data
   * from the clobReader. For example: clobSelectQuery: "SELECT RESULT FROM
   * RESULTRECORDS WHERE ENTRY_DATE=?";" clobSelectReplacementTag: "101010"
   *
   * @param clobSelectQuery a SELECT query to retrieve the CLOB element which
   * will be updated with content from clobReader
   * @param clobSelectReplacementTag replacements for the '?' in the
   * clobSelectQuery (e.g. the primary key)
   * @param appendData the data to append to the CLOB
   * @param connectionPoolName the name fo the connection pool
   */
  public int executeClobAppend(String clobSelectQuery, String clobSelectReplacementTag, char[] appendData, String connectionPoolName) throws SQLException {
    Connection connection = getConnection(connectionPoolName);
    try {
      char c;
      connection.setAutoCommit(false);
      String clobSelect = clobSelectQuery.toUpperCase().trim();
      if (!clobSelect.endsWith("FOR UPDATE")) {
        clobSelect = clobSelect.concat(" FOR UPDATE");
      }
      OracleResultSet rset = (OracleResultSet) executeQuery(clobSelect, clobSelectReplacementTag, connection);
      if (!rset.next()) {
        closeResources(rset);
        return 0;
      }
      CLOB clob = (CLOB) rset.getClob(1);
      long length = clob.length();
      clob.putChars(length + 1, appendData);
      connection.commit();
      connection.setAutoCommit(true);
    } catch (SQLException e) {
      throw e;
    } finally {
      closeResources(connection);
    }
    return 1;
  }

  /**
   * Performs an INSERT into a table with a BLOB. Must provide a normal INSERT
   * statement (with the EMPTY_BLOB() operator) and a SELECT statement which
   * selects the BLOB element which will be updated with data from the
   * blobStream. For example: insertQuery: "INSERT INTO RESULTRECORDS
   * (ENTRY_DATE,USERNAME,RESULT_COMMENT,RESULT) VALUES (?,?,?, EMPTY_BLOB())"
   * insertQueryReplacementTag: "101010,user,comment" blobSelectQuery: "SELECT
   * RESULT FROM RESULTRECORDS WHERE ENTRY_DATE=?";" blobSelectReplacementTag:
   * "101010"
   *
   * @param insertQuery INSERT query used to insert non-LOB data and contains
   * the EMPTY_BLOB() operator
   * @param insertQueryReplacementTag replacements for '?' in the insertQuery
   * @param blobSelectQuery a SELECT query to retrieve the newly created
   * EMPTY_CLOB which will be updated with content from clobReader
   * @param blobSelectReplacementTag replacements for the '?' in the
   * blobSelectQuery (e.g. the primary key)
   * @param blobStream the reader whose data will be inserted into the new BLOB
   */
  public int executeBlobInsert(String insertQuery, String insertQueryReplacementTag, String blobSelectQuery, String blobSelectReplacementTag, InputStream blobStream,
          String connectionPoolName) throws SQLException, IOException {
    if (executeUpdate(insertQuery, insertQueryReplacementTag, connectionPoolName) == 0) {
      return 0;
    }
    return executeBlobUpdate(blobSelectQuery, blobSelectReplacementTag, blobStream, connectionPoolName);
  }

  /**
   * Performs an UPDATE into a table entry with a BLOB. Must provide a SELECT
   * statement which selects the BLOB element which will be updated with data
   * from the blobStream. For example: blobSelectQuery: "SELECT RESULT FROM
   * RESULTRECORDS WHERE ENTRY_DATE=?";" blobSelectReplacementTag: "101010"
   *
   * @param blobSelectQuery a SELECT query to retrieve the BLOB element which
   * will be updated with content from blobStream
   * @param blobSelectReplacementTag replacements for the '?' in the
   * blobSelectQuery (e.g. the primary key)
   * @param blobStream the reader whose data will be inserted into the new BLOB
   */
  public int executeBlobUpdate(String blobSelectQuery, String blobSelectReplacementTag, InputStream blobStream, String connectionPoolName) throws SQLException,
          IOException {
    Connection connection = getConnection(connectionPoolName);
    try {
      connection.setAutoCommit(false);
      String blobSelect = blobSelectQuery.toUpperCase().trim();
      if (!blobSelect.endsWith("FOR UPDATE")) {
        blobSelect = blobSelect.concat(" FOR UPDATE");
      }
      OracleResultSet rset = (OracleResultSet) executeQuery(blobSelect, blobSelectReplacementTag, connection);
      if (!rset.next()) {
        closeResources(rset);
        return 0;
      }
      BLOB blob = (BLOB) rset.getBlob(1);
      OutputStream outstream = blob.setBinaryStream(0L);
      int size = blob.getBufferSize();
      byte[] buffer = new byte[size];
      int length = -1;
      while ((length = blobStream.read(buffer)) != -1) {
        outstream.write(buffer, 0, length);
      }
      blobStream.close();
      outstream.close();
      connection.commit();
      connection.setAutoCommit(true);
    } catch (SQLException sqle) {
      throw sqle;
    } catch (IOException ioe) {
      throw ioe;
    } finally {
      closeResources(connection);
    }
    return 1;
  }

  /**
   * Returns the DatabaseMetaData object for the connection cache.
   */
  public DatabaseMetaData getDatabaseMetaData(String connectionPoolName) {
    try {
      DatabaseMetaData metaData = null;
      if (oraConnectionManager != null) {
        metaData = oraConnectionManager.getDatabaseMetaData(connectionPoolName);
      } else {
        DataSource ds = (DataSource) dataSourceMap.get(connectionPoolName);
        if (ds != null) {
          Connection conn = ds.getConnection();
          metaData = conn.getMetaData();
          conn.close();
        }
      }
      return metaData;
    } catch (SQLException e) {
      return null;
    }
  }

  /**
   * Returns the database name for the connection cache. e.g. Oracle
   */
  public String getDatabaseProductName(String connectionPoolName) {
    try {
      return getDatabaseMetaData(connectionPoolName).getDatabaseProductName();
    } catch (SQLException e) {
      return null;
    }
  }

  /**
   * Returns the database version for the connection cache.
   */
  public String getDatabaseProductVersion(String connectionPoolName) {
    try {
      return getDatabaseMetaData(connectionPoolName).getDatabaseProductVersion();
    } catch (SQLException e) {
      return null;
    }
  }

  /**
   * Returns the JDBC driver name for the connection cache.
   */
  public String getDriverName(String connectionPoolName) {
    try {
      return getDatabaseMetaData(connectionPoolName).getDriverName();
    } catch (SQLException e) {
      return null;
    }
  }

  /**
   * Returns the JDBC driver version for the connection cache.
   */
  public String getDriverVersion(String connectionPoolName) {
    try {
      return getDatabaseMetaData(connectionPoolName).getDriverVersion();
    } catch (SQLException e) {
      return null;
    }
  }

  public static String[] ArrayList(ResultSet rset, String columnName) throws SQLException {
    ArrayList v = new ArrayList();
    while (rset.next()) {
      v.add(rset.getString(columnName));
    }
    if (v.size() == 0) {
      return new String[0];
    }
    return (String[]) v.toArray(new String[0]);
  }

  /**
   * Runs a query which has replacement variables with values defined in
   * replacements
   *
   * @param query SQLQuery
   * @param replacements Map
   * @param connectionPoolName String
   * @return ResultSet
   * @throws SQLException
   */
  public static ResultSet executeQuery(Connection conn, SQLQuery query, Map<String, Object> replacements) throws SQLException {
    OraSQLManager oraSQLManager = new OraSQLManager();
    OraclePreparedStatement stmt = (OraclePreparedStatement) oraSQLManager.getStatement(query.getSql(), conn);
    if (query.getVariables().size() > 0 && replacements != null) {
      for (String variableName : query.getVariables()) {
        if (!replacements.containsKey(variableName) || replacements.get(variableName) == null) {
          throw new SQLException("Missing replacement " + variableName);
        }
        Object value = replacements.get(variableName);
        if (value instanceof Integer) {
          stmt.setIntAtName(variableName, ((Number) value).intValue());
        } else if (value instanceof Double) {
          stmt.setDoubleAtName(variableName, ((Number) value).doubleValue());
        } else if (value instanceof CLOB) {
          stmt.setCLOBAtName(variableName, (CLOB) value);
        } else if (value instanceof BLOB) {
          stmt.setBLOBAtName(variableName, (BLOB) value);
        } else {
          stmt.setStringAtName(variableName, value.toString());
        }
      }
    }
    return stmt.executeQuery();
  }

  /**
   * Runs a query which has replacement variables with values defined in
   * replacements. The replacements are defined by an array with members of the
   * form <replacement name>:<value>. The replacement is always added as a
   * String.
   *
   * @param conn Connection
   * @param query SQLQuery
   * @param replacements Map
   * @return ResultSet
   * @throws SQLException
   */
  public static ResultSet executeQuery(Connection conn, SQLQuery query, String[] replacements) throws SQLException {
    Map<String, Object> replacementMap = new HashMap<String, Object>();
    if (ExtArray.hasLength(replacements)) {
      for (String replacement : replacements) {
        String[] keyValue = replacement.split(":", 2);
        if (keyValue.length == 2) {
          replacementMap.put(keyValue[0], keyValue[1]);
        }
      }
    }
    return executeQuery(conn, query, replacementMap);
  }

  /*
   * Runs a query which has replacement variables with values defined in
   * replacements. The replacements are defined by an array with members of the
   * form <replacement name>:<value>. The replacement is always added as a
   * String.
   */
  public static ResultSet executeQuery(Connection conn, SQLProvider sqlProvider, String sqlQueryName, String[] replacements) throws SQLException {
    SQLQuery query = sqlProvider.getSQLQuery(sqlQueryName);
    if (query == null) {
      throw new NullPointerException(sqlQueryName + " not found");
    }
    return executeQuery(conn, query, replacements);
  }

  /**
   * Runs a query which contains an IN condition. This will page the IN
   * replacements to 1000 at a time and replace the others as usual.
   *
   * @param conn Oracle connection
   * @param sqlProvider SQLProvider implementation
   * @param sqlQueryName The name of the query accessible from the SQLProvider
   * @param inSQLParameter The name of SQL parameter which is the IN condition
   * replacements
   * @param inValues The IN replacement values
   * @param replacements Other values
   * @return
   * @throws SQLException
   */
  public static List<RowData> executeInQuery(Connection conn, SQLProvider sqlProvider, String sqlQueryName, String inSQLParameter, List<String> inValues, Map<String, Object> replacements) throws SQLException {
    List<RowData> results = new ArrayList<RowData>();
    if (inSQLParameter == null) {
      return results;
    }
    SQLQuery query = sqlProvider.getSQLQuery(sqlQueryName);
    int pageCount = ExtArray.getPages(inValues, 1000);
    for (int i = 0; i < pageCount; i++) {
      List<String> replacementsPage = ExtArray.getPage(inValues, i, 1000);
      StringBuffer inRepNames= new StringBuffer();
      for(int j=0; j< replacementsPage.size(); j++) {
        if (inRepNames.length()> 0) {
          inRepNames.append(",");
        }
        inRepNames.append(":"+inSQLParameter).append(j);        
      }
      String processedSQL = query.getSql().replace(":"+inSQLParameter, inRepNames);      
      OraclePreparedStatement stmt = (OraclePreparedStatement) conn.prepareStatement(processedSQL);
      for (int j = 0; j < replacementsPage.size(); j++) {
        stmt.setStringAtName(inSQLParameter+j, replacementsPage.get(j));
      }
      if (query.getVariables().size() > 0 && replacements != null) {
        for (String variableName : query.getVariables()) {
          if (!variableName.equals(inSQLParameter)) {
            if (!replacements.containsKey(variableName) || replacements.get(variableName) == null) {
              throw new SQLException("Missing replacement " + variableName);
            }
            Object value = replacements.get(variableName);
            if (value instanceof Integer) {
              stmt.setIntAtName(variableName, ((Number) value).intValue());
            } else if (value instanceof Double) {
              stmt.setDoubleAtName(variableName, ((Number) value).doubleValue());
            } else if (value instanceof CLOB) {
              stmt.setCLOBAtName(variableName, (CLOB) value);
            } else if (value instanceof BLOB) {
              stmt.setBLOBAtName(variableName, (BLOB) value);
            } else {
              stmt.setStringAtName(variableName, value.toString());
            }
          }
        }
      }
      ResultSet rset = stmt.executeQuery();
      ResultSetMetaData md = rset.getMetaData();
      while (rset.next()) {
        RowData rowData = new RowData(md.getColumnCount());
        results.add(rowData);
        for (int j = 1; j <= md.getColumnCount(); j++) {
          int column = j - 1;
          rowData.addColumnValue(column, rset.getObject(j));
        }
      }
      stmt.close();
    }
    return results;
  }

  /**
   * Runs a query which has replacement variables with values defined in
   * replacements
   *
   * @param query SQLQuery
   * @param replacements Map
   * @param connectionPoolName String
   * @return ResultSet
   * @throws SQLException
   */
  public static ResultSet executeQuery(Connection conn, SQLQuery query) throws SQLException {
    return executeQuery(conn, query, new String[0]);
  }

  public static void closeResources(ResultSet rset) {
    Connection conn= null;
    Statement stmt= null;
    try {
      if (rset != null) {
        stmt= rset.getStatement();
        conn= rset.getStatement().getConnection();                
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      close(rset);
      close(stmt);
      close(conn);
    }
  }

  public static void closeResources(Statement stmt) {
    Connection conn= null;
    try {
      conn= stmt.getConnection();
    } catch (Exception e) {
    } finally {
      close(stmt);
      close(conn);
    }
  }

  public static void closeResources(Connection conn) {
    close(conn);
  }

  public static void close(ResultSet rset) {
    try {
      rset.close();      
    } catch (Exception e) {}
  }

  public static void close(Statement stmt) {
    try {
      stmt.close();
    } catch (Exception e) {}
  }

  public static void close(Connection conn) {
    try {
      conn.close();
    } catch (Exception e) {}
  }

  static void run(OraSQLManager manager, String sql) throws Exception {
    ResultSet rset = manager.executeQuery(sql, "abgen");
    int countRows = 0;
    ResultSetMetaData metaData = rset.getMetaData();
    while (rset.next()) {
      countRows++;
    }
    System.out.println("Rows/Cols: " + countRows + "/" + metaData.getColumnCount());
  }

  /**
   * Retrieves the prepared statement for the query. If already prepared, it
   * returns the handle from the table. Otherwise, it prepares the statement,
   * enters it in the table, and returns the handle.
   */
  private PreparedStatement getStatement(String query, Connection connection) throws SQLException {
    PreparedStatement stmt = connection.prepareStatement(query);
    stmt.setQueryTimeout(getQueryTimeoutSecs());
    return stmt;
  }

  /**
   * Runs a query given by query which has replacement fields which is set to
   * elements in replacementTag. The query is used to create a PreparedStatement
   * for efficient future use. When the query is no longer needed, it should be
   * removed by a call to removeUserQuery(query) to free the resources used by
   * the PreparedStatement.
   *
   * @param query the sql statement to run which includes a single replacement
   * tag (?)
   */
  private ResultSet executeQuery(String query, String replacementTag, Connection connection) throws SQLException {
    PreparedStatement stmt = getStatement(query, connection);
    if (replacementTag != null) {
      int numTags = ExtString.countChar(query, '?');
      String[] st = ExtString.splitCSV(replacementTag);
      if (st.length < numTags) {
        throw new SQLException("Insufficient number of replacements");
      }
      for (int i = 1; i <= numTags; i++) {
        stmt.setString(i, st[i - 1]);
      }
    }
    return stmt.executeQuery();
  }

  public void setQueryTimeoutSecs(int queryTimeoutSecs) {
    this.queryTimeoutSecs = queryTimeoutSecs;
  }

  public int getQueryTimeoutSecs() {
    return queryTimeoutSecs;
  }
}
