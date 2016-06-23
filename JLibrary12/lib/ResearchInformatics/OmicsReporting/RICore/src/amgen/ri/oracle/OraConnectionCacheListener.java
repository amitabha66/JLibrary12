package amgen.ri.oracle;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleConnectionCacheCallback;

/**
 * <p>Title: Dynamic Relation Db Mapping</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2010</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class OraConnectionCacheListener implements OracleConnectionCacheCallback {
    public OraConnectionCacheListener() {
    }

    /**
     * When the release connection callback is registered on a connection, and the
     * cache from which the connection was retrieved is empty, then instead of
     * the default behavior to wait for connections to be returned to the cache,
     * this method is called by the cache.
     * @param conn - OracleConnection object
     * @param obj - Any Java object that needs to be processed in this method
     */
    public void releaseConnection(OracleConnection conn, Object obj) {
      try  {
         System.out.println("Releasing connection");
        // Release the connection
        if (conn != null)  {
          conn.close();
        }
      } catch (Exception ex)  {
        ex.printStackTrace();
      }
    }

    /**
     * When the handle abandoned connection callback is registered, the
     * connection cache calls this method when a connection needs to be
     * reclaimed.
     * @param conn - OracleConnection object
     * @param obj - Any Java object that needs to be processed in this method
     * @return - true of false
     */
    public boolean handleAbandonedConnection(OracleConnection conn, Object obj)  {
      try  {
         // Close the connection
         System.out.println("Releasing abandoned connection");
         if ( conn != null )  {
           conn.close();
         }
      } catch (Exception ex)  {
        ex.printStackTrace();
      }
      return true;
  }}
