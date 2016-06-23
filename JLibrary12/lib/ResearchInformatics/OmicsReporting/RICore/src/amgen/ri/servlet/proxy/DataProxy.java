package amgen.ri.servlet.proxy;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import amgen.ri.servlet.ServletBase;
import amgen.ri.util.ExtString;
import java.util.regex.Pattern;
import javax.servlet.http.Cookie;

/**
 * Simple proxy which forwards the all params to the URL in the param 'src' as
 * POST. Response is sent via a pass-through to the servlet OutputStream.
 * Nothing is set for the content type. If you need to set a content type, use
 * XMLDataProxy or subclass.
 *
 * @version $Id: DataProxy.java,v 1.3 2012/07/30 22:58:46 cvs Exp $
 *
 */
public class DataProxy extends ServletBase {
  private final String[] PROXIED_HEADERS = {
    "Accept", "Accept-Charset", "Accept-Encoding", "Accept-Language", "Content-Type", "Date", "Expect", "From", "Pragma", "Warning",};

  public DataProxy() {
  }

  /**
   * Standard constructor which creates a new ServletBase
   *
   * @param req HttpServletRequest
   * @param resp HttpServletResponse
   */
  public DataProxy(HttpServletRequest req, HttpServletResponse resp) {
    super(req, resp);
  }

  /**
   * Returns the getServlet implementation class for the request
   *
   * @param req HttpServletRequest
   * @param resp HttpServletResponse
   * @return ServletBase
   */
  protected ServletBase getServlet(HttpServletRequest req, HttpServletResponse resp) {
    return new DataProxy(req, resp);
  }

  /**
   * Returns the mimetype of the servlet used to setContentType
   *
   * @return String
   */
  protected String getServletMimeType() {
    if (doesParameterExist("contentType", true)) {
      return getParameter("contentType");
    } else {
      return "text/xml";
    }
  }

  /**
   * Handles the servlet request
   *
   * @throws Exception
   */
  protected void performRequest() throws Exception {
    if (!doesParameterExist("src", true)) {
      return;
    }
    URL srcURL = new URL(getParameter("src"));
    Map<String, String> parameters = new HashMap<String, String>();

    for (String paramName : getParameterNames()) {
      if (!paramName.equals("src") && !paramName.equals("contentType")) {
        parameters.put(paramName, getParameter(paramName));
      }
    }
    writeResponse(srcURL, parameters, response.getOutputStream());
  }

  /**
   * Handles the proxy request
   *
   * @param srcURL Source URL object
   * @param parameters HTTP decoded parameters
   * @param out OutputStream to send the response
   * @throws Exception
   */
  protected void writeResponse(URL srcURL, Map<String, String> parameters, OutputStream out) throws Exception {
    try {
      // Pull the parameters from the request and create POST-able set encoded in UTF8
      StringBuffer requestParameters = new StringBuffer();
      if (parameters != null) {
        for (String paramName : parameters.keySet()) {
          if (requestParameters.length() > 0) {
            requestParameters.append("&");
          }
          requestParameters.append(URLEncoder.encode(paramName, "UTF-8") + "=" + URLEncoder.encode(parameters.get(paramName), "UTF-8"));
        }
      }
      // Open the connection
      HttpURLConnection conn = null;

      switch (getMethodType()) {
        case GET:
          URL getURL = new URL(srcURL + (requestParameters.length() > 0 ? "?" + requestParameters : ""));
          conn = (HttpURLConnection) getURL.openConnection();
          break;
        case POST:
          conn = (HttpURLConnection) srcURL.openConnection();
          break;
      }

      List<Pattern> proxiedCookieNamePatterns = getProxiedCookies();
      if (!proxiedCookieNamePatterns.isEmpty()) {
        List<String> proxyCookieFields = new ArrayList<String>();
        for (Cookie cookie : getCookies()) {
          boolean match = false;
          for (Pattern proxiedCookieNamePattern : proxiedCookieNamePatterns) {
            if (proxiedCookieNamePattern.matcher(cookie.getName()).matches()) {
              match= true;
              break;
            }
          }
          if (match) {
            proxyCookieFields.add(cookie.getName() + "=" + cookie.getValue());
          }
        }
        if (!proxyCookieFields.isEmpty()) {
          conn.setRequestProperty("Cookie", ExtString.join(proxyCookieFields, ";"));
        }
      }

      for (String proxyHeader : PROXIED_HEADERS) {
        String headerValue = request.getHeader(proxyHeader);
        if (headerValue != null) {
          conn.setRequestProperty(proxyHeader, headerValue);
        }
      }

      if (getMethodType().equals(MethodType.POST)) {
        // If parameters exist, send via POST (by setting output on the connection)
        // This is needed since IIS throws a 405 when POSTing to a static file
        if (requestParameters.length() > 0) {
          conn.setDoOutput(true);
          OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
          wr.write(requestParameters.toString());
          wr.flush();
        }
      }
      // Pass-through the response from the connection InputStream to the servlet OutputStream
      // could be done by channels...
      byte[] buffer = new byte[1024];
      int bytesRead;
      InputStream in = conn.getInputStream();
      while ((bytesRead = in.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead);
      }
      in.close();
      //out.flush();
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("Message: " + e + " URL: " + srcURL);
    }
  }

  /**
   * List of cookie names to proxy from the request
   *
   * @return
   */
  protected List<Pattern> getProxiedCookies() {
    List<Pattern> cookieNamePatterns = new ArrayList<Pattern>();
    cookieNamePatterns.add(Pattern.compile("\\w+SESSION(_DEV|_TEST|_PROD){0,1}", Pattern.CASE_INSENSITIVE));
    cookieNamePatterns.add(Pattern.compile("\\w+IDENTITY(_DEV|_TEST|_PROD){0,1}", Pattern.CASE_INSENSITIVE));
    return cookieNamePatterns;
  }
}
