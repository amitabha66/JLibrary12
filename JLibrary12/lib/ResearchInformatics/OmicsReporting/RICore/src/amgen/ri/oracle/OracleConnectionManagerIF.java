package amgen.ri.oracle;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: </p>
 *
 * <p>Company: </p>
 *
 * @author J. McDowell
 * @version $Id
 */
public interface OracleConnectionManagerIF {
    /**
     * Returns whether a cache exists in this ConnectionManager.
     *
     * @param cacheName The cache name to find
     * @return true if cache pool exists, false otherwise
     */
    public boolean cacheExist(String cacheName);

    /**
     * Returns the pools which exist in this ConnectionManager.
     */
    public String[] getCacheList();

    /**
     * Returns the cache for the pool.
     */
    public String getCacheURL(String name) throws SQLException;

    /**
     * Returns an open connection. If no one is available, and the max
     * number of connections has not been reached, a new connection is
     * created.
     *
     * @param name The cache name as defined in the properties file
     * @return Connection The connection or null
     */
    public Connection getConnection(String name) throws SQLException;

    /** Returns the DatabaseMetaData object for the connection cache. */
    public DatabaseMetaData getDatabaseMetaData(String connectionPoolName) throws SQLException;

  /**
   * @return the logWriter
   */
  public PrintWriter getLogWriter();
  public void setLogWriter(PrintWriter writer);

}
