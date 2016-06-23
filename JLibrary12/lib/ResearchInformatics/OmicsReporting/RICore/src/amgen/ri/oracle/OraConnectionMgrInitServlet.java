package amgen.ri.oracle;

import amgen.ri.json.ExtJSON;
import amgen.ri.json.JSONArray;
import amgen.ri.json.JSONObject;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import amgen.ri.util.Debug;
import amgen.ri.util.ExtString;

/**
 * Servlet which loads connection pools into the OracleConnectionManager. To
 * use, create a servlet which loads first. e.g.
 *   <servlet>
 *     <servlet-name>OracleInitializationServlet</servlet-name>
 *     <display-name>OracleInitialization Servlet</display-name>
 *     <servlet-class>amgen.ri.oracle.OraConnectionMgrInitServlet</servlet-class>
 *     <init-param>
 *       <param-name>VERSION</param-name>
 *       <param-value>AIG_VERSION</param-value>
 *     </init-param>
 *     <init-param>
 *       <param-name>AIG_JDBC.DEV</param-name>
 *       <param-value>jdbc:oracle:thin:aig/!umqLht7HxL37o8iC5YV8mA==@ussf-pdbx-ddb01:1521:SFDSC01P.amgen.com</param-value>
 *     </init-param>
 *     <init-param>
 *       <param-name>ASFRI_JDBC.DEV</param-name>
 *       <param-value>jdbc:oracle:thin:asfri/!54CEganeQfu548G+z4BsXA==@usto-pdbx-ora15:1521:SMCLPRD</param-value>
 *     </init-param>
 *     <load-on-startup>1</load-on-startup>
 *   </servlet>
 *
 * @author not attributable
 * @version 1.0
 */
public class OraConnectionMgrInitServlet extends HttpServlet {
  public OraConnectionMgrInitServlet() {
    super();
  }

  public void init() throws ServletException {
    String versionToken = getServletConfig().getInitParameter("VERSION");
    String version = null;
    if (versionToken != null) {
      version = getServletConfig().getServletContext().getInitParameter(versionToken);
    }
    String jConfig = getServletConfig().getInitParameter("Configurations");
    if (ExtJSON.isJSON(jConfig)) {
      loadJSONConfig(ExtJSON.toJSON(jConfig), version);
    }
    Enumeration<String> initParameterNames = getServletConfig().getInitParameterNames();
    while (initParameterNames.hasMoreElements()) {
      String initParameterName = initParameterNames.nextElement();
      String initParameterValue = getServletConfig().getInitParameter(initParameterName);

      if (initParameterValue.startsWith("jdbc:oracle:thin")) {
        if (version == null) {
          Properties cacheProperties = new Properties();
          String jdbcName = initParameterName;
          double maxConnections = ExtString.toDouble(getServletConfig().getInitParameter(jdbcName + ".MAXCONNECTIONS"));
          if (!Double.isNaN(maxConnections)) {
            cacheProperties.setProperty("MaxLimit", ((int) maxConnections) + "");
          }
          String properties = getServletConfig().getInitParameter(jdbcName + ".PROPERTIES");
          addCacheProperties(properties, cacheProperties);
          addConnectionDataSourceJDBC(jdbcName, initParameterValue, cacheProperties);
        } else if (initParameterName.endsWith("." + version)) {
          Properties cacheProperties = new Properties();
          String jdbcName = initParameterName.substring(0, initParameterName.length() - version.length() - 1);
          double maxConnections = ExtString.toDouble(getServletConfig().getInitParameter(jdbcName + ".MAXCONNECTIONS" + "." + version));
          if (!Double.isNaN(maxConnections)) {
            cacheProperties.setProperty("MaxLimit", ((int) maxConnections) + "");
          }
          String properties = getServletConfig().getInitParameter(jdbcName + ".PROPERTIES" + "." + version);
          addCacheProperties(properties, cacheProperties);
          addConnectionDataSourceJDBC(jdbcName, initParameterValue, cacheProperties);
        }
      }
    }
  }

  private void loadJSONConfig(Object jConfiguration, String version) {
    JSONArray jConfigurations = new JSONArray();
    if (jConfiguration instanceof JSONArray) {
      jConfigurations = (JSONArray) jConfiguration;
    } else if (jConfiguration instanceof JSONObject) {
      jConfigurations.put((JSONObject) jConfiguration);
    }
    for (int i = 0; i < jConfigurations.length(); i++) {
      try {
        JSONObject jConfig = jConfigurations.optJSONObject(i);
        String configVersion = jConfig.optString("Version", "-NOVERSION-");
        String jdbcName = jConfig.getString("Name");
        String jdbc = jConfig.getString("JDBC");
        String properties = jConfig.optString("Properties","MinLimit=1,MaxLimit=100,InitialLimit=3,InactivityTimeout=300,AbandonedConnectionTimeout=300,PropertyCheckInterval=60");
        if (configVersion.equals("-NOVERSION-") || ExtString.equalsIgnoreCase(version, configVersion)) {
          Properties cacheProperties = new Properties();
          addCacheProperties(properties, cacheProperties);
          addConnectionDataSourceJDBC(jdbcName, jdbc, cacheProperties);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * addCacheProperties
   *
   * @param properties String
   * @param cacheProperties Properties
   */
  private void addCacheProperties(String properties, Properties cacheProperties) {
    if (!ExtString.hasLength(properties)) {
      return;
    }
    String[] propertyFields = properties.split(",");
    for (String propertyField : propertyFields) {
      String[] nameValue = propertyField.split("=", 2);
      if (nameValue.length == 2) {
        cacheProperties.setProperty(nameValue[0].trim(), nameValue[1].trim());
      }
    }
  }

  private void addConnectionPool(String name, String jdbcURL, Properties cacheProperties) {
    try {
      OraConnectionManager oraConnectionManager= (OraConnectionManager)OraConnectionManager.getInstance();
      oraConnectionManager.addCacheEntry(name, jdbcURL, cacheProperties);
      StringBuffer sb = new StringBuffer();
      Properties p = oraConnectionManager.getCacheProperties(name);
      for (Object propertyName : p.keySet()) {
        if (sb.length() > 0) {
          sb.append(";");
        }
        sb.append(propertyName + "=" + p.get(propertyName));
      }
      Debug.print(name + "   " + jdbcURL + " (" + sb + ")");
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
  }

  private void addConnectionDataSourceJDBC(String name, String jdbcURL, Properties cacheProperties) {
    try {
      OraConnectionManager oraConnectionManager= (OraConnectionManager)OraConnectionManager.getInstance();
      oraConnectionManager.addCacheJDBC(name, jdbcURL, cacheProperties);
      StringBuffer sb = new StringBuffer();
      Properties p = oraConnectionManager.getCacheProperties(name);
      for (Object propertyName : p.keySet()) {
        if (sb.length() > 0) {
          sb.append(";");
        }
        sb.append(propertyName + "=" + p.get(propertyName));
      }
      Debug.print("Added: " + name + "   " + jdbcURL + " (" + sb + ")");
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
  }

  public void destroy() {
    try {
      OraConnectionManager oraConnectionManager= (OraConnectionManager)OraConnectionManager.getInstance();
      oraConnectionManager.clearCaches();
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
  }
}
