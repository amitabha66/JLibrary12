package amgen.ri.rdb;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SQLManagerIF
 * Description: Interface which defines lower level relational database operations.
 * This interface assumes the implementation support connection caching (or pooling).
 * Therefore most method include a connectionPoolName variable.
 *
 * @author Jeffrey A. McDowell
 * @version 1.0
 */
public interface SQLManagerIF {
    /** Retrieves the oracle JDBC-thin client connection for the session- logging in if not previously done so */
    public Connection getConnection(String connectionPoolName) throws SQLException;

    /** Closes the SQLManager returning the connection back to the connection cache */
    public void close() throws SQLException;

    /**
     * Returns whether a pool exists in the ConnectionManager.
     * @param name The pool name to find
     * @return true if the pool exists, false otherwise
     */
    public boolean cacheExist(String name);

    /** Returns the pools available in this ConnectionManager. */
    public String[] getCacheList();

    /**
     * Retrieves the prepared statement for the query. If already prepared, it returns the handle from the table. Otherwise, it
     * prepares the statement, enters it in the table, and returns the handle.
     */
    public PreparedStatement getStatement(String query, String connectionPoolName) throws SQLException;

    /** Excecutes the given query in the connection pool */
    public ResultSet executeQuery(String query, String connectionPoolName) throws SQLException;

    /**
     * Runs a query given by query which has replacement fields which is set to elements in replacementTag. The
     * query is used to create a PreparedStatement for efficient future use. When the query is no longer needed,
     * it should be removed by a call to removeUserQuery(query) to free the resources used by the PreparedStatement.
     * @param query the sql statement to run which includes a single replacement tag (?)
     */
    public ResultSet executeQuery(String query, String replacementTag, String connectionPoolName) throws SQLException;

    /**
     * Runs a query given by query which has replacement fields which is set to elements in replacementTag. The
     * query is used to create a PreparedStatement for efficient future use. When the query is no longer needed,
     * it should be removed by a call to removeUserQuery(query) to free the resources used by the PreparedStatement.
     * @param query the sql statement to run which includes a single replacement tag (?)
     */
    public ResultSet executeQuery(String query, String[] replacementTag, String connectionPoolName) throws SQLException;

    /**
     * Runs a query which returns the next value for the given sequence object
     */
    public long getNextSequenceValue(String sequenceName, String connectionPoolName) throws SQLException;

    /**
     * Runs an update DML given by query which has replacement fields which is set to elements in replacementTag. The
     * query is used to create a PreparedStatement for efficient future use. When the query is no longer needed,
     * it should be removed by a call to removeUserQuery(query) to free the resources used by the PreparedStatement.
     * @param query the sql statement to run which includes a single replacement tag (?)
     */
    public int executeUpdate(String query, String connectionPoolName) throws SQLException;

    /**
     * Runs an update DML given by query which has replacement fields which is set to elements in replacementTag. The
     * query is used to create a PreparedStatement for efficient future use. When the query is no longer needed,
     * it should be removed by a call to removeUserQuery(query) to free the resources used by the PreparedStatement.
     * @param query the sql statement to run which includes a single replacement tag (?)
     */
    public int executeUpdate(String query, String replacementTag, String connectionPoolName) throws SQLException;

    /**
     * Runs an update DML given by query which has replacement fields which is set to elements in replacementTag. The
     * query is used to create a PreparedStatement for efficient future use. When the query is no longer needed,
     * it should be removed by a call to removeUserQuery(query) to free the resources used by the PreparedStatement.
     * @param query the sql statement to run which includes a single replacement tag (?)
     */
    public int executeUpdate(String query, Object[] replacementTags, String connectionPoolName) throws SQLException;

    /**
     * Performs an INSERT into a table with a CLOB. Must provide a normal INSERT statement (with the EMPTY_CLOB() operator)
     * and a SELECT statement which selects the CLOB element which will be updated with data from the clobReader.
     * For example:
     * insertQuery: "INSERT INTO RESULTRECORDS (ENTRY_DATE,USERNAME,RESULT_COMMENT,RESULT) VALUES (?,?,?, EMPTY_CLOB())"
     * insertQueryReplacementTag: "101010,user,comment"
     * clobSelectQuery: "SELECT RESULT FROM RESULTRECORDS WHERE ENTRY_DATE=?";"
     * clobSelectReplacementTag: "101010"
     * @param insertQuery INSERT query used to insert non-LOB data and contains the EMPTY_CLOB() operator
     * @param insertQueryReplacementTag replacements for '?' in the insertQuery
     * @param clobSelectQuery a SELECT query to retrieve the newly created EMPTY_CLOB which will be updated with content from clobReader
     * @param clobSelectReplacementTag replacements for the '?' in the clobSelectQuery (e.g. the primary key)
     * @param clobReader the reader whose data will be inserted into the new CLOB
     */
    public int executeClobInsert(String insertQuery, String insertQueryReplacementTag, String clobSelectQuery, String clobSelectReplacementTag, Reader clobReader,
                                 String connectionPoolName) throws SQLException, IOException;

    /**
     * Performs an UPDATE into a table entry with a CLOB. Must provide a SELECT statement which selects the CLOB element
     * which will be updated with data from the clobReader.
     * For example:
     * clobSelectQuery: "SELECT RESULT FROM RESULTRECORDS WHERE ENTRY_DATE=?";"
     * clobSelectReplacementTag: "101010"
     * @param clobSelectQuery a SELECT query to retrieve the CLOB element which will be updated with content from clobReader
     * @param clobSelectReplacementTag replacements for the '?' in the clobSelectQuery (e.g. the primary key)
     * @param clobReader the reader whose data will be inserted into the new CLOB
     */
    public int executeClobUpdate(String clobSelectQuery, String clobSelectReplacementTag, Reader clobReader, String connectionPoolName) throws SQLException, IOException;

    /**
     * Appends data from the given Reader to a CLOB field. Must provide a SELECT statement which selects the CLOB element
     * which will be updated with data from the clobReader.
     * For example:
     * clobSelectQuery: "SELECT RESULT FROM RESULTRECORDS WHERE ENTRY_DATE=?";"
     * clobSelectReplacementTag: "101010"
     * @param clobSelectQuery a SELECT query to retrieve the CLOB element which will be updated with content from clobReader
     * @param clobSelectReplacementTag replacements for the '?' in the clobSelectQuery (e.g. the primary key)
     * @param appendDataReader Reader with the data to append to the CLOB
     * @param connectionPoolName the name fo the connection pool
     */
    public int executeClobAppend(String clobSelectQuery, String clobSelectReplacementTag, Reader appendDataReader, String connectionPoolName) throws SQLException,
        IOException;

    /**
     * Appends an array of characters to a CLOB field. Must provide a SELECT statement which selects the CLOB element
     * which will be updated with data from the clobReader.
     * For example:
     * clobSelectQuery: "SELECT RESULT FROM RESULTRECORDS WHERE ENTRY_DATE=?";"
     * clobSelectReplacementTag: "101010"
     * @param clobSelectQuery a SELECT query to retrieve the CLOB element which will be updated with content from clobReader
     * @param clobSelectReplacementTag replacements for the '?' in the clobSelectQuery (e.g. the primary key)
     * @param appendData the data to append to the CLOB
     * @param connectionPoolName the name fo the connection pool
     */
    public int executeClobAppend(String clobSelectQuery, String clobSelectReplacementTag, char[] appendData, String connectionPoolName) throws SQLException;

    /**
     * Performs an INSERT into a table with a BLOB. Must provide a normal INSERT statement (with the EMPTY_BLOB() operator)
     * and a SELECT statement which selects the BLOB element which will be updated with data from the blobStream.
     * For example:
     * insertQuery: "INSERT INTO RESULTRECORDS (ENTRY_DATE,USERNAME,RESULT_COMMENT,RESULT) VALUES (?,?,?, EMPTY_BLOB())"
     * insertQueryReplacementTag: "101010,user,comment"
     * blobSelectQuery: "SELECT RESULT FROM RESULTRECORDS WHERE ENTRY_DATE=?";"
     * blobSelectReplacementTag: "101010"
     * @param insertQuery INSERT query used to insert non-LOB data and contains the EMPTY_BLOB() operator
     * @param insertQueryReplacementTag replacements for '?' in the insertQuery
     * @param blobSelectQuery a SELECT query to retrieve the newly created EMPTY_CLOB which will be updated with content from clobReader
     * @param blobSelectReplacementTag replacements for the '?' in the blobSelectQuery (e.g. the primary key)
     * @param blobStream the reader whose data will be inserted into the new BLOB
     */
    public int executeBlobInsert(String insertQuery, String insertQueryReplacementTag, String blobSelectQuery, String blobSelectReplacementTag, InputStream blobStream,
                                 String connectionPoolName) throws SQLException, IOException;

    /**
     * Performs an UPDATE into a table entry with a BLOB. Must provide a SELECT statement which selects the BLOB element
     * which will be updated with data from the blobStream.
     * For example:
     * blobSelectQuery: "SELECT RESULT FROM RESULTRECORDS WHERE ENTRY_DATE=?";"
     * blobSelectReplacementTag: "101010"
     * @param blobSelectQuery a SELECT query to retrieve the BLOB element which will be updated with content from blobStream
     * @param blobSelectReplacementTag replacements for the '?' in the blobSelectQuery (e.g. the primary key)
     * @param blobStream the reader whose data will be inserted into the new BLOB
     */
    public int executeBlobUpdate(String blobSelectQuery, String blobSelectReplacementTag, InputStream blobStream, String connectionPoolName) throws SQLException,
        IOException;

    /* BELOW ARE STUBS FOR PROVIDING INFORMATION ABOUT THE DRIVER FOR A GIVEN CONNECTION CACHE */
    /** Returns the DatabaseMetaData object for the connection cache. */
    public DatabaseMetaData getDatabaseMetaData(String connectionPoolName);

    /** Returns the database name for the connection cache. (from the DatabaseMetaData) e.g. Oracle */
    public String getDatabaseProductName(String connectionPoolName);

    /** Returns the database version for the connection cache. (from the DatabaseMetaData) */
    public String getDatabaseProductVersion(String connectionPoolName);

    /** Returns the JDBC driver name for the connection cache. (from the DatabaseMetaData) */
    public String getDriverName(String connectionPoolName);

    /** Returns the JDBC driver version for the connection cache. (from the DatabaseMetaData) */
    public String getDriverVersion(String connectionPoolName);

    /** Returns the URL for a given cache */
    public String getCacheURL(String cache);

    /** Checks the status of the connection pool. true if the connection pool appears available; false otherwise */
    public boolean isAvailable(String connectionPoolName);

}
