package amgen.ri.util;

import amgen.ri.crypt.StringEncrypter;
import amgen.ri.xml.ExtXMLElement;
import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.RequestTargetAuthentication;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.client.TargetAuthenticationStrategy;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom.Document;

/**
 * <p>
 * Title: Dynamic Relation Db Mapping</p>
 *
 * <p>
 * Description: </p>
 *
 * <p>
 * Copyright: Copyright (c) 2009</p>
 *
 * <p>
 * Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public final class ExtNet {

  private String domain;
  private String username;
  private String password;
  private String serviceAccountPropertiesFileName;
  private int socketTimeout = -1;
  private int connectionTimeout = -1;
  private StatusLine lastStatusLine;
  private List<Cookie> cookies;
  private List<Cookie> lastCookies;

  public ExtNet() {
    cookies = new ArrayList<Cookie>();
  }

  public ExtNet(String serviceAccountPropertiesFile) {
    this();
    this.serviceAccountPropertiesFileName = serviceAccountPropertiesFile;
  }

  public ExtNet(String domain, String username, String password) {
    this();
    this.domain = domain;
    this.username = username;
    this.password = password;
  }

  public void addCookie(Cookie cookie) {
    cookies.add(cookie);
  }

  public void setCookie(List<Cookie> cookies) {
    this.cookies = cookies;
  }

  public void setCookies(HttpServletRequest request) {
    javax.servlet.http.Cookie[] cookies = request.getCookies();
    for (javax.servlet.http.Cookie cookie : cookies) {
      BasicClientCookie c = new BasicClientCookie(cookie.getName(), cookie.getValue());
      if (cookie.getPath() != null) {
        c.setPath(cookie.getPath());
      }
      addCookie(c);
    }
  }

  public List<Cookie> getCookies() {
    return lastCookies;
  }

  /**
   * Returns cookies with the given name
   *
   * @param name
   * @return
   */
  public Set<Cookie> getCookies(String name) {
    Set<Cookie> matchCookies = new HashSet<Cookie>();
    if (lastCookies != null) {
      for (Cookie c : lastCookies) {
        if (c.getName().equals(name)) {
          matchCookies.add(c);
        }
      }
    }
    return matchCookies;
  }

  /**
   * Get the value of domain
   *
   * @return the value of domain
   */
  public String getDomain() {
    return domain;
  }

  /**
   * Set the value of domain
   *
   * @param domain new value of domain
   */
  public void setDomain(String domain) {
    this.domain = domain;
  }

  /**
   * Get the value of password
   *
   * @return the value of password
   */
  public String getPassword() {
    return password;
  }

  /**
   * Set the value of password
   *
   * @param password new value of password
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Get the value of username
   *
   * @return the value of username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Set the value of username
   *
   * @param username new value of username
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Get the value of serviceAccountPropertiesFile
   *
   * @return the value of serviceAccountPropertiesFile
   */
  public String getServiceAccountPropertiesFile() {
    return serviceAccountPropertiesFileName;
  }

  /**
   * Set the value of serviceAccountPropertiesFile
   *
   * @param serviceAccountPropertiesFileName new value of serviceAccountPropertiesFile
   */
  public void setServiceAccountPropertiesFile(String serviceAccountPropertiesFileName) {
    this.serviceAccountPropertiesFileName = serviceAccountPropertiesFileName;
  }

  /**
   * Get the value of connectionTimeout
   *
   * @return the value of connectionTimeout
   */
  public int getConnectionTimeout() {
    return connectionTimeout;
  }

  /**
   * Set the value of connectionTimeout
   *
   * @param connectionTimeout new value of connectionTimeout
   */
  public void setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  /**
   * Get the value of socketTimeout
   *
   * @return the value of socketTimeout
   */
  public int getSocketTimeout() {
    return socketTimeout;
  }

  /**
   * Set the value of socketTimeout
   *
   * @param socketTimeout new value of socketTimeout
   */
  public void setSocketTimeout(int socketTimeout) {
    this.socketTimeout = socketTimeout;
  }

  /**
   * Opens an HTTP connection and writes it to a String using the UTF-8 character set
   *
   * @param url
   * @return
   * @throws IOException
   */
  public String openHttp2String(URL url) throws IOException {
    return EntityUtils.toString(openHttpEntity(url), Charset.forName("UTF-8"));
  }

  /**
   * Opens an HTTP connection and writes it to a String using the UTF-8 character set
   *
   * @param url
   * @return
   * @throws IOException
   */
  public String openHttp2StringViaPost(URL url, Map<String, String> params) throws IOException {
    return EntityUtils.toString(openHttpEntityViaPost(url, params), Charset.forName("UTF-8"));
  }

  /**
   * Opens an HTTP connection and writes it to a byte array
   *
   * @param url
   * @return
   * @throws IOException
   */
  public byte[] openHttp2Bytes(URL url) throws IOException {
    return EntityUtils.toByteArray(openHttpEntity(url));
  }

  /**
   * Opens an HTTP connection and returns the InputStream character set
   *
   * @param url
   * @return
   * @throws IOException
   */
  public InputStream openHttp2Stream(URL url) throws IOException {
    return openHttpEntity(url).getContent();
  }

  /**
   * Opens an HTTP connection and returns the Reader
   *
   * @param url
   * @return
   * @throws IOException
   */
  public Reader openHttp2Reader(URL url) throws IOException {
    return new InputStreamReader(openHttpEntity(url).getContent());
  }

  /**
   * Opens an HTTP connection and parses it as a JDOM Document
   *
   * @param url
   * @return
   * @throws IOException
   */
  public Document openHttp2Document(URL url) throws IOException {
    InputStream in = null;
    try {
      in = openHttp2Stream(url);
      return ExtXMLElement.toDocument(in);
    } finally {
      in.close();
    }
  }

  /**
   * Opens an HTTP connection and returns an HttpEntity object
   *
   * @param url
   * @return
   * @throws IOException
   */
  public HttpEntity openHttpEntity(URL url) throws IOException {
    Logger.getLogger(TargetAuthenticationStrategy.class).setLevel(Level.ERROR);
    Logger.getLogger(RequestTargetAuthentication.class).setLevel(Level.ERROR);
    DefaultHttpClient httpclient = new DefaultHttpClient();

    for (Cookie cookie : cookies) {
      httpclient.getCookieStore().addCookie(cookie);
    }

    httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);

    if (socketTimeout > -1) {
      httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, socketTimeout);
    }
    if (connectionTimeout > -1) {
      httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeout);
    }

    String domain = this.domain;
    String username = this.username;
    String password = this.password;
    if (serviceAccountPropertiesFileName != null) {
      Properties serviceAccountProperties = new Properties();
      if (new File(serviceAccountPropertiesFileName).exists()) {
        serviceAccountProperties.load(new FileReader(serviceAccountPropertiesFileName));
      } else {
        try {
          serviceAccountProperties.load(this.getClass().getResourceAsStream(serviceAccountPropertiesFileName));
        } catch (Exception e) {
        }
      }
      domain = serviceAccountProperties.getProperty("http.serviceaccount.domain", domain);
      username = serviceAccountProperties.getProperty("http.serviceaccount.username", username);
      password = serviceAccountProperties.getProperty("http.serviceaccount.password", password);
    }

    if (username != null && password != null) {
      String decryptedPassword = password;
      try {
        decryptedPassword = new StringEncrypter().decrypt(password);
      } catch (Exception e) {
      }
      NTCredentials creds = new NTCredentials(username, decryptedPassword, getCanonicalHostName()[0], domain);
      httpclient.getCredentialsProvider().setCredentials(AuthScope.ANY, creds);
      httpclient.getAuthSchemes().unregister(AuthPolicy.SPNEGO);
      httpclient.getAuthSchemes().unregister(AuthPolicy.KERBEROS);
    }
    HttpHost target = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
    HttpContext localContext = new BasicHttpContext();
    HttpGet httpGet = new HttpGet(url.getFile());
    HttpResponse httpResponse = httpclient.execute(target, httpGet, localContext);
    this.lastStatusLine = httpResponse.getStatusLine();
    lastCookies = httpclient.getCookieStore().getCookies();

    return httpResponse.getEntity();
  }

  /**
   * Opens an HTTP connection and returns an HttpEntity object
   *
   * @param url
     * @param params
   * @return
   * @throws IOException
   */
  public HttpEntity openHttpEntityViaPost(URL url, Map<String, String> params) throws IOException {
    List<NameValuePair> nvps = new ArrayList<NameValuePair>();
    for (String pName : params.keySet()) {
      nvps.add(new BasicNameValuePair(pName, params.get(pName)));
    }
    return openHttpEntityViaPost(url, nvps);
  }
  

  /**
   * Opens an HTTP connection and returns an HttpEntity object
   *
   * @param url
     * @param nvps
   * @return
   * @throws IOException
   */
  public HttpEntity openHttpEntityViaPost(URL url, List<NameValuePair> nvps) throws IOException {
    Logger.getLogger(TargetAuthenticationStrategy.class).setLevel(Level.ERROR);
    Logger.getLogger(RequestTargetAuthentication.class).setLevel(Level.ERROR);
    DefaultHttpClient httpclient = new DefaultHttpClient();
    
    httpclient.setRedirectStrategy(new LaxRedirectStrategy());

    httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
    CookieStore store = new BasicCookieStore();
    for (Cookie cookie : cookies) {
      store.addCookie(cookie);
    }
    httpclient.setCookieStore(store);

    httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);

    if (socketTimeout > -1) {
      httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, socketTimeout);
    }
    if (connectionTimeout > -1) {
      httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeout);
    }

    String domain = this.domain;
    String username = this.username;
    String password = this.password;
    if (serviceAccountPropertiesFileName != null) {
      Properties serviceAccountProperties = new Properties();
      if (new File(serviceAccountPropertiesFileName).exists()) {
        serviceAccountProperties.load(new FileReader(serviceAccountPropertiesFileName));
      } else {
        try {
          serviceAccountProperties.load(this.getClass().getResourceAsStream(serviceAccountPropertiesFileName));
        } catch (Exception e) {
        }
      }
      domain = serviceAccountProperties.getProperty("http.serviceaccount.domain", domain);
      username = serviceAccountProperties.getProperty("http.serviceaccount.username", username);
      password = serviceAccountProperties.getProperty("http.serviceaccount.password", password);
    }

    if (username != null && password != null) {
      String decryptedPassword = password;
      try {
        decryptedPassword = new StringEncrypter().decrypt(password);
      } catch (Exception e) {
      }
      NTCredentials creds = new NTCredentials(username, decryptedPassword, getCanonicalHostName()[0], domain);
      httpclient.getCredentialsProvider().setCredentials(AuthScope.ANY, creds);
      httpclient.getAuthSchemes().unregister(AuthPolicy.SPNEGO);
      httpclient.getAuthSchemes().unregister(AuthPolicy.KERBEROS);
    }
    HttpHost target = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
    HttpContext localContext = new BasicHttpContext();
    HttpPost post = new HttpPost(url.getFile());
    
    post.setEntity(new UrlEncodedFormEntity(nvps, Charset.forName("UTF-8")));

    HttpResponse httpResponse = httpclient.execute(target, post, localContext);
    this.lastStatusLine = httpResponse.getStatusLine();
    lastCookies = httpclient.getCookieStore().getCookies();

    return httpResponse.getEntity();
  }  

  /**
   * Opens an HTTP connection and returns an HttpEntity object
   *
   * @param url
   * @return
   * @throws IOException
   */
  public StatusLine testHttpEntity(URL url) throws IOException {
    Logger.getLogger(TargetAuthenticationStrategy.class).setLevel(Level.ERROR);
    Logger.getLogger(RequestTargetAuthentication.class).setLevel(Level.ERROR);
    DefaultHttpClient httpclient = new DefaultHttpClient();

    httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);

    String domain = this.domain;
    String username = this.username;
    String password = this.password;
    if (serviceAccountPropertiesFileName != null) {
      Properties serviceAccountProperties = new Properties();
      if (new File(serviceAccountPropertiesFileName).exists()) {
        serviceAccountProperties.load(new FileReader(serviceAccountPropertiesFileName));
      } else {
        try {
          serviceAccountProperties.load(this.getClass().getResourceAsStream(serviceAccountPropertiesFileName));
        } catch (Exception e) {
        }
      }
      domain = serviceAccountProperties.getProperty("http.serviceaccount.domain", domain);
      username = serviceAccountProperties.getProperty("http.serviceaccount.username", username);
      password = serviceAccountProperties.getProperty("http.serviceaccount.password", password);
    }

    if (username != null && password != null) {
      String decryptedPassword = password;
      try {
        decryptedPassword = new StringEncrypter().decrypt(password);
      } catch (Exception e) {
      }
      NTCredentials creds = new NTCredentials(username, decryptedPassword, getCanonicalHostName()[0], domain);
      httpclient.getCredentialsProvider().setCredentials(AuthScope.ANY, creds);
      httpclient.getAuthSchemes().unregister(AuthPolicy.SPNEGO);
      httpclient.getAuthSchemes().unregister(AuthPolicy.KERBEROS);
    }
    HttpHost target = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
    HttpContext localContext = new BasicHttpContext();
    HttpGet httpGet = new HttpGet(url.getFile());
    HttpResponse httpResponse = httpclient.execute(target, httpGet, localContext);
    StatusLine lastStatusLine = httpResponse.getStatusLine();
    httpGet.releaseConnection();

    return (this.lastStatusLine = lastStatusLine);
  }

  /**
   * Get the value of lastStatus
   *
   * @return the value of lastStatus
   */
  public StatusLine getLastStatus() {
    return lastStatusLine;
  }

  /**
   * Returns the canonical names for the localhost. The returned structure is an array of {host name, domain,
   * full name}. What gets returned is dependent on the system naming system.
   *
   * @return String[]
   * @throws UnknownHostException
   */
  public static String[] getCanonicalHostName() throws UnknownHostException {
    InetAddress addr = InetAddress.getLocalHost();
    String hostName = addr.getHostName().toLowerCase();
    String canonicalName = addr.getCanonicalHostName().toLowerCase();
    String domain = ((canonicalName.startsWith(hostName) && canonicalName.length() > hostName.length()) ? canonicalName.substring(hostName.length()) : "");
    return new String[]{
      hostName, domain, canonicalName};
  }

  /**
   * Attempts to get the address of the given host name
   *
   * @param name String
   * @return String
   * @throws UnknownHostException
   */
  public static String getInetAddress(String hostName) throws UnknownHostException {
    InetAddress addr = InetAddress.getByName(hostName);
    return addr.getHostAddress();
  }

  /**
   * Attempts to get the canonical host name given its IP address as a string of
   *
   * @param name String
   * @return String
   * @throws UnknownHostException
   */
  public static String getCanonicalHostNamebyAddress(String address) {
    String[] octets = address.split("\\.");
    byte[] octetBytes = new byte[octets.length];
    for (int i = 0; i < octets.length; i++) {
      octetBytes[i] = Integer.valueOf(octets[i]).byteValue();
    }
    try {
      InetAddress addr = InetAddress.getByAddress(octetBytes);
      return addr.getCanonicalHostName();
    } catch (UnknownHostException ex) {
    }
    return null;
  }

}
