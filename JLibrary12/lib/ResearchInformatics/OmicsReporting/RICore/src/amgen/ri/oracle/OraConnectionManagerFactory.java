package amgen.ri.oracle;

import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Factory used to create an OracleConnectionManagerIF implementation. 
 * This is either the local Oracle cache OraConnectionManager or a container's JNDI
 * data source cache OracleDataSourceCache which is used if the Context "java:/comp/env"
 * exists.
 *
 * @version $Id
 */
public class OraConnectionManagerFactory {
  private static OraConnectionManagerFactory factory;
  private OracleConnectionManagerIF oracleConnectionManager;
  
  private OraConnectionManagerFactory(OracleConnectionManagerIF oracleConnectionManager) {
    this.oracleConnectionManager = oracleConnectionManager;
  }
  
  public static OraConnectionManagerFactory getFactory() {
    if (factory == null) {
      Context resourceContext = null;
      try {
        Context initContext = new InitialContext();
        resourceContext = (Context) initContext.lookup("java:/comp/env");
      } catch (Exception e) {
      }
      if (resourceContext == null) {
        setOracleConnectionManager(OraConnectionManager.getInstance());
      } else {
        setOracleConnectionManager(OracleDataSourceCache.getInstance());
      }
    }
    return factory;
  }
  
  public static void setOracleConnectionManager(OracleConnectionManagerIF oracleConnectionManager) {
    factory = new OraConnectionManagerFactory(oracleConnectionManager);
  }
  
  public OracleConnectionManagerIF getOracleConnectionManager() {
    return oracleConnectionManager;
  }
}
