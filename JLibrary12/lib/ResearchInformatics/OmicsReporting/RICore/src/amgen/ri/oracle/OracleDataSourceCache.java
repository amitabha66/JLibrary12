package amgen.ri.oracle;

import amgen.ri.util.Debug;
import amgen.ri.util.ExtString;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.sql.DataSource;
import oracle.jdbc.pool.OracleDataSource;

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
public class OracleDataSourceCache extends AbstractOraConnectionManager implements OracleConnectionManagerIF {
  private static OracleDataSourceCache instance;

  private OracleDataSourceCache() {
  }

  public static OracleDataSourceCache getInstance() {
    if (instance == null) {
      instance = new OracleDataSourceCache();
    }
    return instance;
  }

  public boolean cacheExist(String jndiName) {
    try {
      return (getContext().lookup(jndiName) != null);
    } catch (Exception ex) {
      return false;
    }
  }

  public String[] getCacheList() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public String getCacheURL(String name) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Connection getConnection(String name) throws SQLException {
    DataSource ds= null;
    try {
      ds = (DataSource) getContext().lookup(name);
      if (getLogWriter() != null) {
        ds.setLogWriter(getLogWriter());
      }
      Connection c = ds.getConnection();
      if (getLogWriter() != null) {
        getLogWriter().println("GetConnection- " + name + " " + c.getClass().getCanonicalName());
      }
      return c;
    } catch (Exception ex) {
      System.err.println(name);
      ex.printStackTrace();      
      throw new SQLException(ex);
    }
  }

  public DatabaseMetaData getDatabaseMetaData(String connectionPoolName) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  private void printProps(String jndiName, OracleDataSource ds) throws NamingException {
    Enumeration enu = ds.getReference().getAll();
    for (int i = 0; enu.hasMoreElements(); i++) {
      RefAddr addr = (RefAddr) enu.nextElement();
      System.err.printf("%20s:\t{%50s}= {%50s}\n", new Object[]{jndiName, addr.getType(), addr.getContent().toString()});
    }
  }

  private Context getContext() throws SQLException {
    try {
      Context initContext = new InitialContext();
      return (Context) initContext.lookup("java:/comp/env");
    } catch (NamingException ex) {
      throw new SQLException(ex);
    }
  }
}
