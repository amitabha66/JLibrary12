package amgen.ri.oracle;

import amgen.ri.crypt.StringEncrypter;
import amgen.ri.util.ExtString;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleConnectionCacheManager;
import oracle.jdbc.pool.OracleDataSource;

/**
 * OraConnectionManager Description: Implementation of a connection caching
 * system which uses Oracle cache manager
 *
 * @author Jeffrey A. McDowell
 * @version 1.0
 */
public class OraConnectionManager extends AbstractOraConnectionManager implements OracleConnectionManagerIF {
  static public String SERVLET_MANAGER_PROPERTIES = "OraConnectionManager.properties";
  static private OraConnectionManager instance; // The singleton instance
  private static boolean logToStdOut = true;
  private Properties defaultCacheProperties;
  private Map<String, OracleDataSource> cacheTable = new Hashtable();
  private Map<String, String> cacheJDBCMap = new Hashtable();
  private Map<String, Properties> cacheJPropertiesMap = new Hashtable();
  private String[] cacheArray;
  private String dateMask = null;

  private OraConnectionManager() throws SQLException {
    defaultCacheProperties = new Properties();
    defaultCacheProperties.setProperty("MinLimit", "1"); // the cache size is 1 at least
    defaultCacheProperties.setProperty("MaxLimit", "10");
    defaultCacheProperties.setProperty("InitialLimit", "3"); // create 3 connections at startup
    defaultCacheProperties.setProperty("InactivityTimeout", "1800"); //  seconds
    defaultCacheProperties.setProperty("AbandonedConnectionTimeout", "900"); //  seconds
    defaultCacheProperties.setProperty("PropertyCheckInterval", "60"); // seconds
  }

  /**
   * Returns the single instance, creating one if it's the first time this
   * method is called.
   *
   * @return the single instance of OraConnectionManager
   */
  static synchronized public OracleConnectionManagerIF getInstance() {
    if (instance == null) {
      try {
        instance = new OraConnectionManager();
      } catch (SQLException ex) {
      }
    }
    return instance;
  }

  /**
   * Returns whether the single instance has been created.
   */
  static public boolean isAvailable() {
    return (instance != null);
  }

  /**
   * Sets the value of a default connection cache property
   *
   * @param name
   * @param value
   */
  public void setDefaultCacheProperty(String name, String value) {
    this.defaultCacheProperties.put(name, value);
  }

  /**
   * Gets the value of a default connection cache property
   *
   * @param name   
   */
  public String getDefaultCacheProperty(String name) {
    return this.defaultCacheProperties.getProperty(name);
  }

  /**
   * Adds an entry to the connection cache. Rather than initialize the Data
   * Source immediately, this method only adds the JDBC URL to the cache and
   * defers initialization until needed.
   *
   * @param cacheName the name of the new cache
   * @param jdbcURL the JDBC url
   * @param cacheProperties
   * @throws SQLException
   */
  public void addCacheJDBC(String cacheName, String jdbcURL, Properties cacheProperties) throws SQLException {
    if (cacheProperties == null) {
      cacheProperties = new Properties();
    }
    cacheJDBCMap.put(cacheName, jdbcURL);
    cacheJPropertiesMap.put(cacheName, cacheProperties);
  }

  /**
   * Adds an entry to the connection cache
   *
   * @param cacheName the name of the new cache
   * @param jdbcURL the JDBC url
   * @param maxConnections the maximum number of connections
   * @param cacheScheme how the cache behaves with the connections- either
   * DYNAMIC_SCHEME, FIXED_WAIT_SCHEME, or FIXED_RETURN_NULL_SCHEME
   * @param type the type of connection
   * @throws SQLException
   */
  public void addCacheEntry(String cacheName, String jdbcURL, Properties cacheProperties) throws SQLException {
    Map jdbcMap = parseJDBCURL(jdbcURL);

    addCacheEntry(cacheName, (String) jdbcMap.get("HOST"), (String) jdbcMap.get("PORT"),
            (String) jdbcMap.get("SID"), (String) jdbcMap.get("USER"), (String) jdbcMap.get("PASSWD"),
            cacheProperties);
  }

  public void addCacheEntry(String cacheName, String server, String port, String serviceName, String username, String password,
          int maxConnections) throws SQLException {
    /*
     * This object holds the properties of the cache and is passed to the
     * ConnectionCacheManager while creating the cache. Based on these
     * properties the connection cache manager created the connection cache.
     */
    Properties cacheProperties = new Properties(defaultCacheProperties);
    /*
     * Set Max Limit for the Cache. This sets the maximum number of
     * PooledConnections the cache can hold. There is no default MaxLimit
     * assumed meaning connections in the cache could reach as many as the
     * database allows.
     */
    cacheProperties.setProperty("MaxLimit", maxConnections + "");
    addCacheEntry(cacheName, server, port, serviceName, username, password, cacheProperties);
  }

  public void addCacheEntry(String cacheName, String server, String port, String serviceName, String username, String password,
          Properties cacheProperties) throws SQLException {

    /*
     * Initialize the Datasource
     */
    OracleDataSource ods = new OracleDataSource();
    /*
     * Enable caching
     */
    ods.setConnectionCachingEnabled(true);

    /*
     * Set the cache name
     */
    ods.setConnectionCacheName(cacheName);
    ods.setUser(username);
    ods.setPassword(decryptPassword(password));
    if (cacheProperties != null && ExtString.equalsIgnoreCase(cacheProperties.getProperty("SERVER"), "shared")) {
      ods.setDriverType("thin");
      ods.setServerName(server);
      ods.setPortNumber(Integer.parseInt(port));
      ods.setServiceName(serviceName);
      log("Initializing cache " + cacheName + ". Using Shared connections.");
    } else {
      String url = "jdbc:oracle:thin:@(description=(address=(host=" + server
              + ")(protocol=tcp)(port=" + port + "))(connect_data=(SERVICE_NAME=" + serviceName + ")(SERVER=DEDICATED)))";
      ods.setURL(url);
      log("Initializing cache " + cacheName + ". Using Dedicated connection: " + url);
    }
    OracleConnectionCacheManager connMgr = OracleConnectionCacheManager.getConnectionCacheManagerInstance();

    Properties enforcedCacheProperties = new Properties();
    enforcedCacheProperties.putAll(defaultCacheProperties);

    for (Object propertyName : cacheProperties.keySet()) {
      String propertyValue = cacheProperties.get(propertyName) + "";
      if (!ExtString.hasLength(propertyValue) || ExtString.equals(propertyValue, "-1")) {
        enforcedCacheProperties.remove(propertyName);
      } else {
        enforcedCacheProperties.put(propertyName, propertyValue);
      }
    }

    /*
     * Create the cache by passing the cache name, data source and the cache
     * properties
     */
    if (connMgr.existsCache(cacheName)) {
      connMgr.removeCache(cacheName, 0);
    }
    connMgr.createCache(cacheName, ods, enforcedCacheProperties);
    //Add cache to cache table
    cacheTable.put(cacheName, ods);
    cacheJDBCMap.put(cacheName, "jdbc:oracle:thin:" + username + "/" + password + "@" + server + ":" + port + ":" + serviceName);
    cacheJPropertiesMap.put(cacheName, enforcedCacheProperties);
    log("Initialized cache " + cacheName + " Maximum connections: " + ods.getConnectionCacheProperties().getProperty("MaxLimit"));
  }

  /**
   * Adds a connection pool to the connection manager giving the pool a unique
   * name As with all methods which set up the OraConnectionManager, this MUST
   * be called prior to the initial call to getInstance()
   *
   * @param jdbcURL the jdbc URL for the connection. For example,
   * jdbc:oracle:thin:scott/tiger@localhost:1521:TEST
   * @return the connection pool name
   */
  public static String addConnectionPool(String jdbcURL) throws SQLException {
    return addConnectionPool(null, jdbcURL);
  }

  /**
   * Adds a connection pool to the connection manager. As with all methods which
   * set up the OraConnectionManager, this MUST be called prior to the initial
   * call to getInstance()
   *
   * @param connectionPool unique name of the new connection pool. If this is
   * null, a system pool name is given
   * @param jdbcURL the jdbc URL for the connection. For example,
   * jdbc:oracle:thin:scott/tiger@localhost:1521:TEST
   * @return the connection pool name
   */
  public static String addConnectionPool(String connectionPool, String jdbcURL) throws SQLException {
    if (connectionPool == null) {
      connectionPool = "pool" + System.currentTimeMillis();
    }
    OraConnectionManager oraConnectionManager = (OraConnectionManager) OraConnectionManager.getInstance();
    oraConnectionManager.addCache(connectionPool, jdbcURL);
    return connectionPool;
  }

  /**
   * Sets the NLS_Date_Format mask used by Oracle to implicitly convert Date
   * fields.
   *
   * @param oraNLS_Date_Format
   */
  public void setDateFormatMask(String oraNLS_Date_Format) {
    this.dateMask = oraNLS_Date_Format;
  }

  /**
   * Gets the NLS_Date_Format mask used by Oracle to implicitly convert Date
   * fields.
   */
  public String getDateFormatMask() {
    return dateMask;
  }

  /**
   * Adds a dynamic entry to the connection cache
   *
   * @param cacheName the name of the new cache
   * @param jdbcURL the JDBC url
   */
  public String addCache(String cacheName, String jdbcURL) throws SQLException {
    addCacheEntry(cacheName, jdbcURL, new Properties());
    return cacheName;
  }

  /**
   * Adds a dynamic entry to the connection cache. If a cache of the same name
   * already exists, it is replaced only if replace is true.
   *
   * @param cacheName the name of the new cache
   * @param jdbcURL the JDBC url
   * @param replace whether to replace the cache if a cahc ewith the same name
   * already exists
   */
  public String addCache(String cacheName, String jdbcURL, boolean replace) throws SQLException {
    if (cacheExist(cacheName) && !replace) {
      return cacheName;
    } else if (cacheExist(cacheName)) {
      OracleConnectionCacheManager connMgr = OracleConnectionCacheManager.getConnectionCacheManagerInstance();
      connMgr.removeCache(cacheName, 0L);
    }
    addCacheEntry(cacheName, jdbcURL, new Properties());
    return cacheName;
  }

  /**
   * Sets whether the log should go to stdout
   */
  static public void setLogOnNoFile(boolean b) {
    setLogToStdout(b);
  }

  /**
   * Sets whether the log should go to stdout
   */
  static public void setLogToStdout(boolean b) {
    logToStdOut = b;
  }

  /**
   * Writes a message to the log file.
   */
  private void log(Object msg) {
    if (logToStdOut) {
      System.out.println(new Date() + ": " + msg);
      System.out.flush();
    }
  }

  /**
   * Writes a message with an Exception to the log file.
   */
  private void log(Throwable e, String msg) {
    if (logToStdOut) {
      System.out.println(new Date() + ": " + msg);
      e.printStackTrace(System.out);
      System.out.flush();
    }
  }

  /**
   * Returns an open connection. If no one is available, and the max number of
   * connections has not been reached, a new connection is created.
   *
   * @param name The cache name as defined in the properties file
   * @return Connection The connection or null
   */
  public Connection getConnection(String name) throws SQLException {
    OracleDataSource ods = null;
    try {
      ods = getOraDataSource(name);
    } catch (Exception e) {
    }
    if (ods != null) {
      if (getLogWriter() != null) {
        ods.setLogWriter(getLogWriter());
      }
      OracleConnection conn = null;
      try {
        conn = (OracleConnection) ods.getConnection();
        if (conn == null) {
          System.err.println("Warning: Connection Pool " + name + " did not return a connection. Refreshing...");
          OracleConnectionCacheManager connMgr = OracleConnectionCacheManager.getConnectionCacheManagerInstance();
          connMgr.refreshCache(name, OracleConnectionCacheManager.REFRESH_INVALID_CONNECTIONS);
          try {
            conn = (OracleConnection) ods.getConnection();
            if (conn == null) {
              System.err.println("Warning: Unable to refresh connection pool " + name + ". Recreating...");
              String jdbcURL = cacheJDBCMap.get(name);
              removeCache(name);
              addCache(name, jdbcURL);
              ods = getOraDataSource(name);
              conn = (OracleConnection) ods.getConnection();
            }
          } catch (SQLException e2) {
            if (e2.getErrorCode() == 17142) {
              System.err.println("Warning: Unable to refresh connection pool " + name + ". Recreating...");
              String jdbcURL = cacheJDBCMap.get(name);
              removeCache(name);
              addCache(name, jdbcURL);
              ods = getOraDataSource(name);
              conn = (OracleConnection) ods.getConnection();
            }
          }
        }
      } catch (SQLException e1) {
        e1.printStackTrace();
        if (e1.getErrorCode() == 17142) {
          System.err.println("Warning: Connection Pool " + name + " was disabled. Refreshing...");
          OracleConnectionCacheManager connMgr = OracleConnectionCacheManager.getConnectionCacheManagerInstance();
          connMgr.refreshCache(name, OracleConnectionCacheManager.REFRESH_INVALID_CONNECTIONS);
          try {
            conn = (OracleConnection) ods.getConnection();
          } catch (SQLException e2) {
            if (e2.getErrorCode() == 17142) {
              System.err.println("Warning: Unable to refresh disabled connection pool " + name + ". Recreating...");
              String jdbcURL = cacheJDBCMap.get(name);
              Properties cacheProperties = cacheJPropertiesMap.get(name);

              removeCache(name);
              addCacheEntry(name, jdbcURL, cacheProperties);
              ods = getOraDataSource(name);
              conn = (OracleConnection) ods.getConnection();
            }
          }
        } else {
          throw e1;
        }
      }
      conn.setImplicitCachingEnabled(true);
      if (conn.pingDatabase(10) != OracleConnection.DATABASE_OK) {
        OracleConnectionCacheManager connMgr = OracleConnectionCacheManager.getConnectionCacheManagerInstance();
        connMgr.refreshCache(name, OracleConnectionCacheManager.REFRESH_INVALID_CONNECTIONS);
        conn = (OracleConnection) ods.getConnection();
      }
      if (dateMask != null) {
        Statement stmt = conn.createStatement();
        stmt.execute("ALTER SESSION SET NLS_DATE_FORMAT='" + dateMask + "'");
      }
      //conn.registerConnectionCacheCallback(new OraConnectionCacheListener(), null, OracleConnection.ALL_CONNECTION_CALLBACKS);

      if (getLogWriter() != null) {
        getLogWriter().println("GetConnection- " + name + " " + conn.getClass().getCanonicalName());
      }

      return conn;
    }
    System.err.println("Warning: No Connection Pool " + name + ".");
    return null;
  }

  /**
   * Send to log details about all the connection caches
   */
  public void logOraConnectionMgrStats() {
    try {
      OracleConnectionCacheManager connMgr = OracleConnectionCacheManager.getConnectionCacheManagerInstance();
      for (String cacheName : cacheJDBCMap.keySet()) {
        OracleDataSource ods = getOraDataSource(cacheName);
        log("-----------------------------------------------------------------");
        log("Cache " + cacheName + " active size: " + connMgr.getNumberOfActiveConnections(cacheName) + "");
        log("Cache " + cacheName + " size: " + connMgr.getNumberOfAvailableConnections(cacheName) + "");
        log("Cache " + cacheName + " maxsize: " + connMgr.getCacheProperties(cacheName).getProperty("MaxLimit"));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Send to log details about the given connection cache
   */
  public void logOraConnectionMgrStats(String cacheName) {
    Map cacheInfo = getConnectionInformation(cacheName);
    log("-----------------------------------------------------------------");
    for (Iterator it = cacheInfo.keySet().iterator(); it.hasNext();) {
      Object cacheInfoName = it.next();
      log("Cache " + cacheName + ": " + cacheInfoName + "=" + cacheInfo.get(cacheInfoName));
    }
  }

  /**
   * Returns whether a cache exists in this ConnectionManager.
   *
   * @param cacheName The cache name to find
   * @return true if cache pool exists, false otherwise
   */
  public boolean cacheExist(String cacheName) {
    return cacheJDBCMap.containsKey(cacheName);
  }

  /**
   * Refreshes all connections in a cache.
   *
   * @param cacheName The cache name to close
   * @throws SQLException
   */
  public void refreshCache(String cacheName) throws SQLException {
    System.err.println("Warning: Refreshing connection pool " + cacheName + ".");
    OracleConnectionCacheManager.getConnectionCacheManagerInstance().refreshCache(cacheName, OracleConnectionCacheManager.REFRESH_ALL_CONNECTIONS);
  }

  /**
   * Closes a cache and removes it from this ConnectionManager.
   *
   * @param cacheName The cache name to close
   */
  public void removeCache(String cacheName) throws SQLException {
    if (cacheTable.containsKey(cacheName)) {
      try {
        OracleConnectionCacheManager.getConnectionCacheManagerInstance().purgeCache(cacheName, true);
      } catch (Exception e) {
      }
      OracleConnectionCacheManager.getConnectionCacheManagerInstance().removeCache(cacheName, 0);
      cacheTable.remove(cacheName);
      cacheJDBCMap.remove(cacheName);
    }
  }

  /**
   * Removes all caches and removes them from this ConnectionManager.
   *
   * @param cacheName The cache name to close
   */
  public void clearCaches() throws SQLException {
    String[] cacheNames = getCacheList();
    for (String cacheName : cacheNames) {
      try {
        removeCache(cacheName);
      } catch (Exception e) {
      }
    }
  }

  /**
   * Returns the pools which exist in this ConnectionManager.
   */
  public String[] getCacheList() {
    if (cacheArray == null) {
      cacheArray = new String[cacheJDBCMap.size()];
      cacheJDBCMap.keySet().toArray(cacheArray);
    }
    return cacheArray;
  }

  /**
   * Returns the Servername for the JDBC connection cache.
   */
  public String getServerName(String connectionPoolName) throws SQLException {
    Map infoTable = getConnectionInformation(connectionPoolName);
    if (infoTable == null) {
      return null;
    }
    return (String) infoTable.get("SERVER");
  }

  /**
   * Returns the Servername for the JDBC connection cache.
   */
  public String getUsername(String connectionPoolName) throws SQLException {
    Map infoTable = getConnectionInformation(connectionPoolName);
    if (infoTable == null) {
      return null;
    }
    return (String) infoTable.get("USERNAME");
  }

  /**
   * Returns the cache for the pool.
   */
  public String getCacheURL(String name) throws SQLException {
    if (name == null) {
      return name;
    }
    return getOraDataSource(name).getURL();
  }

  public void writeConnectionInformation() {
    for (String cacheName : cacheJDBCMap.keySet()) {
      System.out.println(cacheName);
      Map information = getConnectionInformation(cacheName);
      System.out.println("ACTIVESIZE/CACHESIZE/CACHESCHEME: " + information.get("ACTIVESIZE") + "/" + information.get("CACHESIZE") + "/"
              + information.get("CACHESCHEME"));
    }
  }

  public HashMap getConnectionInformation(String cacheName) {
    try {
      OracleConnectionCacheManager cacheMgr = OracleConnectionCacheManager.getConnectionCacheManagerInstance();
      OracleDataSource ods = getOraDataSource(cacheName);
      String jdbcURL = ods.getConnection().getMetaData().getURL();
      if (jdbcURL == null) {
        return null;
      }
      String[] elements = jdbcURL.split(":");
      if (elements.length != 6) {
        return null;
      }
      String protocol = elements[0];
      String subprotocol = elements[1];
      String drivertype = elements[2];
      String host = elements[3];
      String server;
      String username = "";
      String password = "";
      String port = elements[4];
      String sid = elements[5];
      if (host.startsWith("@")) {
        server = host.substring(1);
      } else {
        String[] hostElements = host.split("@");
        server = hostElements[1];
        if (hostElements[0].indexOf('/') > 0) {
          String[] userElements = hostElements[0].split("/", 2);
          username = userElements[0];
          password = userElements[1];
        } else {
          username = hostElements[0];
        }
      }

      HashMap infoMap = new HashMap();
      infoMap.put("PROTOCOL", protocol);
      infoMap.put("SUBPROTOCOL", subprotocol);
      infoMap.put("DRIVERTYPE", drivertype);
      infoMap.put("HOST", host);
      infoMap.put("SERVER", server);
      infoMap.put("USERNAME", username);
      infoMap.put("PASSWORD", password);
      infoMap.put("PORT", port);
      infoMap.put("SID", sid);
      infoMap.put("ACTIVESIZE", cacheMgr.getNumberOfActiveConnections(cacheName) + "");
      infoMap.put("CACHESIZE", cacheMgr.getNumberOfAvailableConnections(cacheName) + "");

      return infoMap;
    } catch (Exception e) {
      return null;
    }
  }

  public Properties getCacheProperties(String cacheName) {
    return cacheJPropertiesMap.get(cacheName);
  }

  protected OracleDataSource getOraDataSource(String cacheName) throws SQLException {
    if (cacheTable.containsKey(cacheName)) {
      return cacheTable.get(cacheName);
    } else if (cacheJDBCMap.containsKey(cacheName)) {
      addCacheEntry(cacheName, cacheJDBCMap.get(cacheName), cacheJPropertiesMap.get(cacheName));
      return cacheTable.get(cacheName);
    }
    return null;
  }

  /**
   * Remove all caches from the OracleConnectionCacheManager
   */
  protected void finalize() {
    /*
     * OracleConnectionCacheManager cacheMgr = null; try { cacheMgr =
     * OracleConnectionCacheManager.getConnectionCacheManagerInstance(); } catch
     * (SQLException ex) { } String[] cacheNames = getCacheList(); for (String
     * cacheName : cacheNames) { try { cacheMgr.refreshCache(cacheName, 0); }
     * catch (Exception e) {} }
     */
  }

  //jdbc:oracle:<drivertype>:@<hostname>:<port>:<database_sid>
//jdbc:oracle:thin:@(description=(address=(host=usto-pdbx-ora15)(protocol=tcp)(port=1521))(connect_data=(SERVICE_NAME=smclprd)(SERVER=DEDICATED)))
  private Map parseJDBCURL(String jdbcURL) {

    String[] urlParts = jdbcURL.split(":");
    String jdbc = urlParts[0];
    String database = urlParts[1];
    String type = urlParts[2];
    String host = urlParts[3];
    String port = urlParts[4];
    String sid = urlParts[5];
    String username = null;
    String password = null;

    String[] userid_parts = host.split("@");
    host = userid_parts[1];
    if (userid_parts[0].length() > 0) {
      String[] username_passwd = userid_parts[0].split("\\/", 2);
      username = username_passwd[0];
      if (username_passwd.length == 2) {
        password = username_passwd[1];
      }
    }
    HashMap connectionMap = new HashMap();
    connectionMap.put("TYPE", type);
    connectionMap.put("HOST", host);
    connectionMap.put("PORT", port);
    connectionMap.put("SID", sid);
    connectionMap.put("USER", username);
    connectionMap.put("PASSWD", password);
    return connectionMap;
  }

  private String decryptPassword(String password) {
    //Debug.print(password);
    if (password == null) {
      return null;
    }
    if (!password.startsWith("!")) {
      return password;
    }
    try {
      //Debug.print(password);
      //Debug.print(password.substring(1));
      //Debug.print(new StringEncrypter().decrypt(password.substring(1)));
      return new StringEncrypter().decrypt(password.substring(1));
    } catch (Exception ex) {
      return password;
    }
  }
}
