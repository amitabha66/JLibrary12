/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package amgen.ri.oracle;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 *
 * @author jemcdowe
 */
public abstract class AbstractOraConnectionManager implements OracleConnectionManagerIF {
  private PrintWriter logWriter;

  /**
   * Returns the DatabaseMetaData object for the connection cache.
   */
  public DatabaseMetaData getDatabaseMetaData(String connectionPoolName) throws SQLException {
    return getConnection(connectionPoolName).getMetaData();
  }

  /**
   * Returns the database name for the connection cache. e.g. Oracle
   */
  public String getDatabaseProductName(String connectionPoolName) throws SQLException {
    try {
      return getDatabaseMetaData(connectionPoolName).getDatabaseProductName();
    } catch (Exception e) {
    }
    return null;
  }

  /**
   * Returns the database version for the connection cache.
   */
  public String getDatabaseProductVersion(String connectionPoolName) throws SQLException {
    try {
      return getDatabaseMetaData(connectionPoolName).getDatabaseProductVersion();
    } catch (Exception e) {
    }
    return null;
  }

  /**
   * Returns the JDBC driver name for the connection cache.
   */
  public String getDriverName(String connectionPoolName) throws SQLException {
    try {
      return getDatabaseMetaData(connectionPoolName).getDriverName();
    } catch (Exception e) {
    }
    return null;
  }

  /**
   * Returns the JDBC driver version for the connection cache.
   */
  public String getDriverVersion(String connectionPoolName) throws SQLException {
    try {
      return getDatabaseMetaData(connectionPoolName).getDriverVersion();
    } catch (Exception e) {
    }
    return null;
  }

  /**
   * @return the logWriter
   */
  public PrintWriter getLogWriter() {
    return logWriter;
  }

  /**
   * @param logWriter the logWriter to set
   */
  public void setLogWriter(PrintWriter logWriter) {
    this.logWriter = logWriter;
  }
  
}
