package amgen.ri.servlet;

import amgen.ri.json.ExtJSON;
import amgen.ri.json.JSONObject;
import amgen.ri.ldap.ActiveDirectoryLookup;
import amgen.ri.ldap.PersonRecordIF;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import amgen.ri.security.FASFEncrypter;
import amgen.ri.security.FASFIdentity;
import amgen.ri.util.Debug;
import amgen.ri.util.ExtFile;
import amgen.ri.util.ExtString;
import java.io.*;
import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Defines a base class for all servlet-related classes.
 */
public abstract class ServletBase extends HttpServlet {
  public enum ApplicationInstance {
    DEV, TEST, PROD, UNKNOWN
  };

  /**
   * @return the methodType
   */
  public MethodType getMethodType() {
    if (methodType == null) {
      methodType = MethodType.fromRequest(request);
    }
    return methodType;
  }

  /**
   * @param methodType the methodType to set
   */
  public void setMethodType(MethodType methodType) {
    this.methodType = methodType;
  }

  public enum MethodType {
    GET, POST;

    public static MethodType fromRequest(HttpServletRequest req) {
      try {
        return MethodType.valueOf(req.getMethod());
      } catch (Exception e) {
        return GET;
      }
    }
  };
  protected HttpServletRequest request;
  protected HttpServletResponse response;
  protected CookieSourceIF cookies;
  protected FASFIdentity fasfIdentity = null;
  protected HttpSession session;
  protected ServletContext context;
  private MethodType methodType;
  private String contentType;

  private boolean isMultipartContent;
  protected boolean alwaysUseServiceCache = false;
  protected Map<String, String> parameters;
  protected static Map<String, String> initParameters;
  protected Map<String, byte[]> inStreamParams;
  private JSONObject jsonBody;


  /**
   * Default constructor
   */
  public ServletBase() {
    super();
    this.parameters = new HashMap<String, String>();
    this.inStreamParams = new HashMap<String, byte[]>();
  }

  /**
   * Standard constructor which creates a new ServletBase
   *
   * @param req HttpServletRequest
   */
  public ServletBase(HttpServletRequest req, HttpServletResponse resp) {
    this(req, resp, null);
  }

  /**
   * Standard constructor which creates a new ServletBase and optionally sets
   * the response encoding
   *
   * @param req HttpServletRequest
   * @param resp HttpServletResponse
   * @param responseEncoding String
   */
  public ServletBase(HttpServletRequest req, HttpServletResponse resp, String responseEncoding) {
    this();
    this.request = req;
    this.response = resp;
    if (responseEncoding != null) {
      try {
        this.request.setCharacterEncoding(responseEncoding);
      } catch (UnsupportedEncodingException ex) {
        ex.printStackTrace();
      }
    }
    this.session = req.getSession();
    this.context = req.getSession().getServletContext();

    setParameters();
    setJAWRHandlerOverrides();
  }

  /**
   * Get the value of jsonBody
   *
   * @return the value of jsonBody
   */
  public JSONObject getJsonBody() {
    return jsonBody;
  }

  /**
   * Get the value of contentType
   *
   * @return the value of contentType
   */
  public String getContentType() {
    return contentType;
  }

  public void setJAWRHandlerOverrides() {
    ResourceBundlesHandler rsHandler = (ResourceBundlesHandler) context.getAttribute(JawrConstant.JS_CONTEXT_ATTRIBUTE);
    if (rsHandler != null) {
      if (getApplicationInstance().equals(ServletBase.ApplicationInstance.DEV)) {
        rsHandler.getConfig().setDebugModeOn(true);
      }
    }
  }

  public final void setParameters() {
    try {
      if (ServletFileUpload.isMultipartContent(request)) {
        isMultipartContent = true;
        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload();
        // Parse the request
        FileItemIterator iter = upload.getItemIterator(request);
        while (iter.hasNext()) {
          FileItemStream item = iter.next();
          String name = item.getFieldName();
          if (item.isFormField()) {
            parameters.put(name, Streams.asString(item.openStream()));
          } else {
            parameters.put(name, item.getName());
            InputStream in = item.openStream();
            inStreamParams.put(name, ExtFile.readStream(in));
          }
        }
      } else {
        isMultipartContent = false;    
        contentType= request.getContentType();
        
        if (request.getMethod().equalsIgnoreCase("post") && StringUtils.equalsIgnoreCase(contentType, "application/json")) {
          BufferedReader reader = request.getReader();
          String s;
          StringBuilder req_message = new StringBuilder();
          while ((s = reader.readLine()) != null) {
            req_message.append(s);
          }
          Object json= ExtJSON.toJSON(req_message.toString());        
          if (json!= null && json instanceof JSONObject) {
            jsonBody= (JSONObject)json;
          }          
        } else {
          Enumeration<String> paramNames = request.getParameterNames();
          while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            parameters.put(paramName, request.getParameter(paramName));
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns a Cookie of a given name
   *
   * @param cookieName String
   * @return Cookie
   */
  public Cookie getCookie(String cookieName) {
    if (cookies == null) {
      cookies = new GenericCookieSource(request);
    }
    return cookies.getCookie(cookieName);
  }

  /**
   * Returns a Cookie of a given name
   *
   * @param cookieName String
   * @return Cookie
   */
  public CookieSourceIF getCookies() {
    if (cookies == null) {
      cookies = new GenericCookieSource(request);
    }
    return cookies;
  }

  /**
   * Returns a FASFIdentity object if possible. null otherwise
   *
   * @return FASFIdentity
   */
  public FASFIdentity getFASFIdentity() {
    if (fasfIdentity == null) {
      try {
        fasfIdentity = new FASFEncrypter().decryptFASFIdentity(ExtString.unescape(getCookie(FederatedAccessSecurityFilter.DEFAULT_FASF_SESSION_IDENTITY).getValue()));
      } catch (Exception ex) {
        //ex.printStackTrace();
      }
    }
    if (fasfIdentity == null && ExtString.hasLength(request.getRemoteUser())) {
      fasfIdentity = new FASFIdentity(request.getRemoteUser(), request.getRemoteHost());
    }
    return fasfIdentity;
  }

  /**
   * Returns the user identity as a PersonRecord
   *
   * @return
   */
  public PersonRecordIF getPersonRecord() {
    return new ActiveDirectoryLookup().lookup(getFASFIdentity());
  }

  /**
   * Returns the parameter names as a set
   *
   * @return Set
   */
  public Set<String> getParameterNames() {
    return parameters.keySet();
  }

  /**
   * Returns the parameters as a Map
   *
   * @return Map
   */
  public Map<String, String> getParameterMap() {
    return new HashMap<String, String>(parameters);
  }

  /**
   * Returns the parameters as a Map excluding those keys provided
   *
   * @return Map
   */
  public Map<String, String> getParameterMapRemovingKeys(Collection<String> removeKeys) {
    Map<String, String> params = new HashMap<String, String>(parameters);
    params.keySet().removeAll(removeKeys);
    return params;
  }

  /**
   * Returns the parameters as a Map including only those keys provided
   *
   * @return Map
   */
  public Map<String, String> getParameterMapRetaingKeys(Collection<String> retainKeys) {
    Map<String, String> params = new HashMap<String, String>(parameters);
    if (retainKeys != null) {
      params.keySet().retainAll(retainKeys);
    }
    return params;
  }

  /**
   * Returns the HttpServletRequest object
   *
   * @return
   */
  public HttpServletRequest getHttpServletRequest() {
    return request;
  }

  /**
   * Returns the HttpServletRequest object
   *
   * @return
   */
  public HttpServletResponse getHttpServletResponse() {
    return response;
  }

  /**
   * Gets the request as a full url
   *
   * @return String
   */
  public String getRequestURL() {
    StringBuffer url = new StringBuffer(request.getRequestURI());
    if (getParameterNames().size() > 0) {
      StringBuffer params = new StringBuffer();
      for (String paramName : getParameterNames()) {
        if (params.length() > 0) {
          params.append("&");
        }
        params.append(paramName + "=" + getParameter(paramName));
      }
      url.append("?" + params);
    }
    return url.toString();
  }

  /**
   * Gets a request as a full url. also performs a decoding to UTF-8
   *
   * @return String
   */
  public static String getRequestURL(HttpServletRequest request) {
    StringBuffer url = new StringBuffer(request.getRequestURI());
    StringBuffer params = new StringBuffer();
    Enumeration paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements()) {
      String paramName = (String) paramNames.nextElement();
      String[] values = request.getParameterValues(paramName);
      for (String value : values) {
        if (params.length() > 0) {
          params.append("&");
        }
        params.append(paramName + "=" + value);
      }
    }
    if (params.length() > 0) {
      url.append("?" + params);
    }
    return url.toString();
  }

  /**
   * Returns whether the request is a Multipart request
   *
   * @return boolean
   */
  public boolean isMultipartContent() {
    return isMultipartContent;
  }

  /**
   * Returns the value of a parameter
   *
   * @param parameterName String
   * @return String
   */
  public String getParameter(String parameterName) {
    return parameters.get(parameterName);
  }

  /**
   * Returns the value of a parameter as a File object
   *
   * @param parameterName String
   * @return File
   */
  public File getFileParameter(String parameterName) {
    return new File(parameters.get(parameterName));
  }

  /**
   * Returns the value of a parameter or the default value
   *
   * @param parameterName String
   * @param defaultValue String
   * @return String
   */
  public String getParameter(String parameterName, String defaultValue) {
    return (parameters.get(parameterName) == null ? defaultValue : parameters.get(parameterName));
  }

  /**
   * Returns all parameter values with names that match the regex
   *
   * @param parameterNameRegEx String
   * @return List
   */
  public List<String> getParameters(String parameterNameRegEx) {
    List<String> paramValues = new ArrayList<String>();
    Pattern pattern = Pattern.compile(parameterNameRegEx);

    for (String paramName : parameters.keySet()) {
      if (pattern.matcher(paramName).matches()) {
        paramValues.add(getParameter(paramName));
      }
    }
    return paramValues;
  }

  /**
   * Returns all parameter values with names that match the regex which may be
   * delimited by a delimiter regex
   *
   * @param parameterNameRegEx String
   * @param delimiterRegEx String
   * @return List
   */
  public List<String> getParameters(String parameterNameRegEx, String delimiterRegEx) {
    List<String> paramValues = new ArrayList<String>();
    List<String> paramValuesBeforeSplit = getParameters(parameterNameRegEx);

    for (String value : paramValuesBeforeSplit) {
      String[] split = value.split(delimiterRegEx);
      paramValues.addAll(Arrays.asList(split));
    }
    return paramValues;
  }

  /**
   * Returns the value of a parameter as a Number or teh default value if not
   * possible
   *
   * @param parameterName String
   * @return String
   */
  public Number getParameterNumber(String parameterName, Number defaultValue) {
    Number val = getParameterNumber(parameterName);
    return (Double.isNaN(val.doubleValue()) ? defaultValue : val);
  }

  /**
   * Returns the value of a parameter as a Number or NaN if not possible
   *
   * @param parameterName String
   * @return String
   */
  public Number getParameterNumber(String parameterName) {
    String paramValue = getParameter(parameterName);
    if (paramValue != null) {
      try {
        return new Double(parameters.get(parameterName));
      } catch (Exception e) {
      }
    }
    return Double.NaN;
  }

  /**
   * Returns whether a parameter equals a given value
   *
   * @param parameterName String
   * @param testValue String
   * @return boolean
   */
  public boolean doesParameterEqual(String parameterName, String testValue) {
    return (getParameter(parameterName) != null && getParameter(parameterName).equals(testValue));
  }

  /**
   * Returns whether a parameter exists
   *
   * @param parameterName String
   * @return boolean
   */
  public boolean doesParameterExist(String parameterName) {
    return (getParameter(parameterName) != null);
  }

  /**
   * Returns whether a parameter exists and optionally have non-zero length
   * (excluding whitespace)
   *
   * @param parameterName String
   * @return boolean
   */
  public boolean doesParameterExist(String parameterName, boolean notZeroLength) {
    if (notZeroLength) {
      return (getParameter(parameterName) != null && getParameter(parameterName).trim().length() > 0);
    }
    return doesParameterExist(parameterName);
  }

  /**
   * Gets the InputStream for the parameter
   *
   * @param parameterName String
   * @return InputStream
   */
  public InputStream getParameterInputStream(String parameterName) {
    if (inStreamParams.containsKey(parameterName)) {
      return new ByteArrayInputStream(inStreamParams.get(parameterName));
    } else {
      return null;
    }
  }

  /**
   * Gets the byte array for the file contents of an input parameter
   *
   * @param parameterName String
   * @return InputStream
   */
  public byte[] getFileParameterByteArray(String parameterName) {
    if (inStreamParams.containsKey(parameterName)) {
      return inStreamParams.get(parameterName);
    } else {
      return new byte[0];
    }
  }

  /**
   * Returns whether an init parameter exists and optionally have non-zero
   * length (excluding whitespace)
   *
   * @param parameterName String
   * @return boolean
   */
  public boolean doesInitParameterExist(String parameterName, boolean notZeroLength) {
    String value = getInitParameter(parameterName);
    if (value == null) {
      return false;
    }
    return (notZeroLength && value.trim().length() == 0 ? false : true);
  }

  /**
   * Returns the init parameter for the servlet context
   *
   * @param parameterName String
   * @return String
   */
  public String getInitParameter(String parameterName) {
    return request.getSession(true).getServletContext().getInitParameter(parameterName);
  }

  /**
   * Returns the init parameter for the servlet context as a Number, null if it
   * does not exists, or NaN if it is not a number
   *
   * @param parameterName String
   * @return Number
   */
  public Number getInitParameterNumber(String parameterName) {
    String param = request.getSession(true).getServletContext().getInitParameter(parameterName);
    if (param == null) {
      return null;
    }
    try {
      return new Double(param);
    } catch (Exception e) {
      return new Double(Double.NaN);
    }
  }

  /**
   * Returns the real path using the ServletContext
   *
   * @param path String
   * @return File
   */
  public File getRealPath(String path) {
    return new File(context.getRealPath(path));
  }

  /**
   * Returns the real path using the ServletContext
   *
   * @param path String
   * @return File
   */
  public File getRealPathFromInitParameter(String parameterName) {
    if (doesInitParameterExist(parameterName, true)) {
      return new File(context.getRealPath(getInitParameter(parameterName)));
    }
    return null;
  }

  /**
   * Defines the ApplicationInstance (DEV, TEST, PROD) context parameter name.
   * This would appear in the web.xml as a context-param like this. *
   *
   * <context-param> <param-name>INSTANCE</param-name>
   * <param-value>DEV</param-value> </context-param>
   *
   * This should be overridden to use a different context parameter name. Use
   * the method getApplicationInstance() to return the actual
   * ApplicationInstance as defined by this context-param.
   *
   * Default name is INSTANCE
   *
   * @return
   */
  public String getInstanceInitParameter() {
    return "INSTANCE";
  }

  /**
   * Returns the ApplicationInstance as defined in the web.xml context-param
   * with name equal to that returned by getInstanceInitParameter(). If can not
   * be determines, this returns ApplicationInstance.UNKNOWN
   *
   * @return
   */
  public ApplicationInstance getApplicationInstance() {
    try {
      return ApplicationInstance.valueOf(getInitParameter(getInstanceInitParameter()).toUpperCase());
    } catch (Exception e) {
    }
    return ApplicationInstance.UNKNOWN;
  }

  /**
   * Overrides the GET method by simply calling doPost
   *
   * @param req HttpServletRequest
   * @param resp HttpServletResponse
   * @throws ServletException
   * @throws IOException
   */
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    setMethodType(MethodType.GET);
    doPost(req, resp);
  }

  /**
   * Overrides the POST method- instantiating the Servlet class and calling
   * performRequest
   *
   * @param req HttpServletRequest
   * @param resp HttpServletResponse
   * @throws ServletException
   * @throws IOException
   */
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (getMethodType() == null) {
      setMethodType(MethodType.POST);
    }
    ServletBase servlet = getServlet(req, resp);
    String servletMimeType = servlet.getServletMimeType();
    if (servletMimeType != null) {
      if (servlet.getServletMimeType().equals("text/html")) {
        servlet.setNoCache();
      }
      servlet.response.setContentType(servlet.getServletMimeType());
    }
    try {
      servlet.performRequest();
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }

  /**
   * Returns the getServlet implementation class for the request
   *
   * @param req HttpServletRequest
   * @param resp HttpServletResponse
   * @return TBXServlet
   */
  protected abstract ServletBase getServlet(HttpServletRequest req, HttpServletResponse resp);

  /**
   * Sets all the please do not cache directives
   */
  protected void setNoCache() {
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache");
    response.setDateHeader("Expires", 0);
  }

  /**
   * Actual work entry stub
   *
   * @throws ServletException
   * @throws IOException
   */
  protected abstract void performRequest() throws Exception;

  /**
   * Returns the mimetype of the servlet
   */
  protected abstract String getServletMimeType();

  /**
   * Returns the servlet parameters optionally excluding parameters
   *
   * @param excludeParameters List
   * @return Map
   */
  protected Map<String, List<String>> getParameters(String[] excludeParameters) {
    return getParameters(Arrays.asList(excludeParameters));
  }

  /**
   * Returns the servlet parameters as a List<String> splitting the parameter
   * value
   *
   * @param parameterName String
   * @param splitBy String
   * @return List
   */
  protected List<String> getParametersBySplit(String parameterName, String splitBy) {
    String value = getParameter(parameterName);
    if (value == null) {
      return new ArrayList<String>();
    }
    String[] values = value.split(splitBy);
    return Arrays.asList(values);
  }

  /**
   * Returns the servlet parameters optionally excluding parameters
   *
   * @param excludeParameters List
   * @return Map
   */
  protected Map<String, List<String>> getParameters(List<String> excludeParameters) {
    Map<String, List<String>> params = new HashMap<String, List<String>>();
    Map<String, String[]> requestParameters = request.getParameterMap();
    for (String paramName : requestParameters.keySet()) {
      if (excludeParameters == null || !excludeParameters.contains(paramName)) {
        if (!params.containsKey(paramName)) {
          params.put(paramName, new ArrayList<String>());
        }
        params.get(paramName).addAll(Arrays.asList(requestParameters.get(paramName)));
      }
    }
    return params;
  }

  /**
   * Returns the full path for the servlet given a relative path
   *
   * @param relativePath String
   * @return URL
   */
  protected URL createContextURL(String relativePath) {
    try {
      return new URL("http", request.getServerName(), request.getServerPort(), request.getContextPath() + "/" + relativePath);
    } catch (MalformedURLException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public void init() {
    initParameters = new HashMap<String, String>();
    String canonicalName = this.getClass().getCanonicalName();
    ServletConfig servletConfig = getServletConfig();
    Enumeration initParamNames = servletConfig.getServletContext().getInitParameterNames();
    while (initParamNames.hasMoreElements()) {
      String initParamName = initParamNames.nextElement().toString();
      if (initParamName.matches("^" + canonicalName.replaceAll("\\.", "\\.") + "\\.\\w+")) {
        String[] fields = initParamName.split("\\.");
        String field = fields[fields.length - 1];
        initParameters.put(field, servletConfig.getServletContext().getInitParameter(initParamName));
      }
    }
  }

  /**
   * Prints the request to stderr
   */
  public void printRequest() {
    Debug.print("REQUEST", true, false);
    Debug.print(request.getRequestURI());
    Debug.print(request.getQueryString());
    Debug.print(request.getMethod());
    Map<String, String[]> requestParameters = request.getParameterMap();
    for (String paramName : requestParameters.keySet()) {
      Debug.print(paramName + "=" + requestParameters.get(paramName)[0]);
    }
    Debug.print("---");
    for (String paramName : parameters.keySet()) {
      Debug.print(paramName + "=" + parameters.get(paramName));
    }
    Debug.print("---");
  }

  /**
   * Writes the request to the given PrintWriter
   */
  public void writeRequest(PrintWriter writer) {
    writer.println(new Date());
    writer.println("RequestURI: " + request.getRequestURI());
    writer.println("QueryString: " + request.getQueryString());
    writer.println("Method: " + request.getMethod());
    writer.println("Multipart: " + (isMultipartContent ? "Yes" : "No"));
    writer.println("Request Parameters:");
    Map<String, String[]> requestParameters = request.getParameterMap();
    for (String paramName : requestParameters.keySet()) {
      writer.println(paramName + "=" + requestParameters.get(paramName)[0]);
    }
    writer.println("---");
    writer.println("Servlet Parameters:");
    for (String paramName : parameters.keySet()) {
      writer.println(paramName + "=" + parameters.get(paramName));
    }
    writer.println("---");
  }

  /**
   * Prints the request to the given logger via debug()
   *
   * @param logger
   */
  public void printRequest(Logger logger) {
    StringWriter string = new StringWriter();
    PrintWriter writer = new PrintWriter(string);
    writeRequest(writer);
    writer.close();
    logger.debug(string.toString());
  }
}
