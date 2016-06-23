package amgen.ri.servlet;

import amgen.ri.crypt.StringEncrypter.EncryptionException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import amgen.ri.security.FASFEncrypter;
import amgen.ri.security.FASFIdentity;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Security filter which redirects the request to an authentication server if the auth. cookie is not set or
 * expired
 *
 * @author not attributable
 * @version 1.0
 */
public class FederatedAccessSecurityFilter implements Filter {

  public static String DEFAULT_FASF_COOKIE_NAME = "FASF_IDENTITY";
  public static String DEFAULT_FASF_SESSION_IDENTITY = "FASF_SESSION_IDENTITY";
  public static int FASF_COOKIE_NAME_EXPIRATION_HOURS = 4;
  private String fasfCookieKey;
  private String fasfSessionIdentityCookieKey;
  private String fasfAuthenticatorURL;
  private final String fasfCookieDomain = ".amgen.com";
  private boolean allowExplicitAuthentication = false;
  private boolean useTransientPostedParameters = false;
  private Set<String> trustedSites;
  private List<String> excludedFiles;
  private List<Pattern> excludedFileNamePatterns;
  private List<String> excludedPaths;
  private List<Pattern> includedFileNamePatterns;
  private Map<String, String> simulatedUsers = null;
  private FASFParameterEncoderIF parameterEncoder;

  public FederatedAccessSecurityFilter() {
    excludedFiles = new ArrayList<String>();
    excludedFileNamePatterns = new ArrayList<Pattern>();
    excludedPaths = new ArrayList<String>();
    includedFileNamePatterns = new ArrayList<Pattern>();
    trustedSites = new HashSet<String>();
    simulatedUsers = new HashMap<String, String>();
    allowExplicitAuthentication = false;
  }

  /**
   * Called by the web container to indicate to a filter that it is being placed into service.
   *
   * @param filterConfig FilterConfig
   * @throws ServletException
   * @todo Implement this javax.servlet.Filter method
   */
  public void init(FilterConfig filterConfig) throws ServletException {
    String contextName = filterConfig.getServletContext().getServletContextName();
    if (contextName == null || contextName.length() == 0) {
      contextName = UUID.randomUUID().toString();
    }
    contextName = contextName.replaceAll("\\W", "_");
    fasfAuthenticatorURL = getValueFromInitParameter(filterConfig, "FASF_AUTHENTICATOR_URL", "http://ussf-papp-dweb1.amgen.com/cgi-bin/fasf/fasf.pl");
    fasfCookieKey = getValueFromInitParameter(filterConfig, "FASF_COOKIE_KEY", contextName + "_IDENTITY");
    fasfSessionIdentityCookieKey = getValueFromInitParameter(filterConfig, "FASF_SESSION_IDENTITY_COOKIE_KEY", contextName + "_SESSIONIDENTITY");
    if (getValueFromInitParameter(filterConfig, "FASF_PARAMETER_ENCODER_CLASS", null) != null) {
      try {
        Class clazz = Class.forName(getValueFromInitParameter(filterConfig, "FASF_PARAMETER_ENCODER_CLASS", null));
        parameterEncoder = (FASFParameterEncoderIF) clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    String allowExplicitAuthentication = filterConfig.getInitParameter("FASF_ALLOW_EXPLICIT_AUTHENTICATION");
    this.allowExplicitAuthentication = equalsIgnoreCase(allowExplicitAuthentication, "true");

    String useTransientPostedParameters = filterConfig.getInitParameter("FASF_USE_TRANSIENT_POSTED_PARAMETERS");
    this.useTransientPostedParameters = equalsIgnoreCase(useTransientPostedParameters, "true");

    String allowExplicitAuthenticationTrustedSites = filterConfig.getInitParameter("FASF_EXPLICIT_AUTHENTICATION_TRUSTEDSITES");
    if (allowExplicitAuthenticationTrustedSites != null) {
      List<String> sites = Arrays.asList(allowExplicitAuthenticationTrustedSites.split("[,;:]+"));
      for (String site : sites) {
        String siteTrimmed = site.toLowerCase().trim();
        if (siteTrimmed.length() > 0) {
          this.trustedSites.add(siteTrimmed);
        }
      }
    }

    String filterFileExclusions = filterConfig.getInitParameter("FASF_AUTHENTICATOR_EXCLUDEDFILES");
    if (filterFileExclusions != null) {
      this.excludedFiles.clear();
      List<String> excludedFiles = Arrays.asList(filterFileExclusions.split("[,;:]+"));
      for (String excludedFile : excludedFiles) {
        String excludedFileTrimmed = excludedFile.trim();
        if (excludedFileTrimmed.length() > 0) {
          this.excludedFiles.add(excludedFileTrimmed);
        }
      }
    }

    String filterPathExclusions = filterConfig.getInitParameter("FASF_AUTHENTICATOR_EXCLUDEDPATHS");
    if (filterPathExclusions != null) {
      this.excludedPaths.clear();
      List<String> excludedPaths = Arrays.asList(filterPathExclusions.split("[,;:]+"));
      for (String excludedPath : excludedPaths) {
        String excludedPathTrimmed = excludedPath.trim();
        if (excludedPathTrimmed.length() > 0) {
          this.excludedPaths.add(excludedPathTrimmed);
        }
      }
    }

    String filterFileNamePatternExclusions = filterConfig.getInitParameter("FASF_AUTHENTICATOR_EXCLUDEDFILENAMEPATTERNS");
    if (filterFileNamePatternExclusions != null) {
      this.excludedFileNamePatterns.clear();
      List<String> excludedPatterns = Arrays.asList(filterFileNamePatternExclusions.split("[\\n]+"));
      for (String excludedPattern : excludedPatterns) {
        String excludedPatternTrimmed = excludedPattern.trim();
        if (excludedPatternTrimmed.length() > 0) {
          try {
            this.excludedFileNamePatterns.add(Pattern.compile(excludedPatternTrimmed));
          } catch (PatternSyntaxException e) {
            System.err.println("Warning: Unable to load exclude pattern " + excludedPatternTrimmed);
          }
        }
      }
    }

    String filterFileNamePatternInclusions = filterConfig.getInitParameter("FASF_AUTHENTICATOR_INCLUDEDFILENAMEPATTERNS");
    if (filterFileNamePatternInclusions != null) {
      this.includedFileNamePatterns.clear();
      List<String> includedPatterns = Arrays.asList(filterFileNamePatternInclusions.split("[\\n]+"));
      for (String includedPattern : includedPatterns) {
        String includedPatternTrimmed = includedPattern.trim();
        if (includedPatternTrimmed.length() > 0) {
          try {
            this.includedFileNamePatterns.add(Pattern.compile(includedPatternTrimmed));
          } catch (PatternSyntaxException e) {
            System.err.println("Warning: Unable to load exclude pattern " + includedPatternTrimmed);
          }
        }
      }
    }

    simulatedUsers.clear();
    String simulatedUserParam = getValueFromInitParameter(filterConfig, "FASF_SIMULATED_USER", null);
    if (hasTrimmedLength(simulatedUserParam)) {
      for (String simulatedUser : simulatedUserParam.split(",")) {
        if (simulatedUser.indexOf('=') > 0) {
          String[] simulatedUserSplit = simulatedUser.split("=");
          if (simulatedUserSplit.length == 2) {
            simulatedUsers.put(simulatedUserSplit[0].trim(), simulatedUserSplit[1].trim());
          }
        }
      }
    } else {
      simulatedUsers.clear();
    }
  }

  /**
   * Called by the web container to indicate to a filter that it is being taken out of service.
   *
   * @todo Implement this javax.servlet.Filter method
   */
  public void destroy() {
  }

  /**
   * The <code>doFilter</code> method of the Filter is called by the container each time a request/response
   * pair is passed through the chain due to a client request for a resource at the end of the chain.
   *
   * @param request ServletRequest
   * @param response ServletResponse
   * @param chain FilterChain
   * @throws IOException
   * @throws ServletException
   * @todo Implement this javax.servlet.Filter method
   */
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;
    //Logger.getLogger("FASF").debug("FASF: Started FASF filter");
    //Check if session login exists. If so, just continue chain.
    if (SessionLogin.doesSessionLoginExist(req)) {
      //Logger.getLogger("FASF").debug("FASF: Found session login for " + SessionLogin.getSessionLogin(req).getRemoteUser());
      FASFHttpServletRequest fasfHttpServletRequest = new FASFHttpServletRequest(req, new FASFIdentity(SessionLogin.getSessionLogin(req).getRemoteUser(), req), fasfAuthenticatorURL);
      chain.doFilter(fasfHttpServletRequest, resp);
      return;
    }
    //Get requested file
    String[] requestedPath = req.getRequestURI().split("\\/");
    String requestedFile = requestedPath[requestedPath.length - 1];
    //If there are included file name patterns (FASF_AUTHENTICATOR_INCLUDEDFILENAMEPATTERNS) only these get
    // protected
    if (!includedFileNamePatterns.isEmpty()) {
      //Loop through included pattern. If any match, procede to the security stuff.
      //Otherwise, continue chain
      boolean matchesIncludedPattern = false;
      for (Pattern includedFileNamePattern : includedFileNamePatterns) {
        if (includedFileNamePattern.matcher(requestedFile).matches()) {
          matchesIncludedPattern = true;
        }
      }
      if (!matchesIncludedPattern) {
        //Logger.getLogger("FASF").debug("FASF: File " + requestedFile + " not included in secure file list. Bypassing filter");
        chain.doFilter(request, response);
        return;
      }
    } else {
      //Check if the requested file is in the exclusion list. If so, just continue chain.
      if (excludedFiles.contains(requestedFile)) {
        //Logger.getLogger("FASF").debug("FASF: File " + requestedFile + " in excluded secure file list. Bypassing filter");
        chain.doFilter(request, response);
        return;
      }
      //Check if the requested file matches an exclusion Pattern. If so, just continue chain.
      for (Pattern excludedFileNamePattern : excludedFileNamePatterns) {
        if (excludedFileNamePattern.matcher(requestedFile).matches()) {
          //Logger.getLogger("FASF").debug("FASF: File " + requestedFile + " in excluded secure file pattern. Bypassing filter");
          chain.doFilter(request, response);
          return;
        }
      }

      //Check if the path is in the exclusion list. If so, just continue chain.
      for (String excludedPath : excludedPaths) {
        if (req.getRequestURI().startsWith(excludedPath)) {
          //Logger.getLogger("FASF").debug("FASF: File " + requestedFile + " in excluded secure file starts-with pattern. Bypassing filter");
          chain.doFilter(request, response);
          return;
        }
      }
    }

    //Check if it allows explicit authentication- i.e. it allows an encrypted username
    if (allowExplicitAuthentication) {
      String authenticatedUser = null;
      if (request.getParameter(fasfCookieKey) != null) {
        try {
          authenticatedUser = new FASFEncrypter().decrypt(request.getParameter(fasfCookieKey));
        } catch (Exception e) {
          System.err.println("Unable to decrypt authenticated user");
        }
      } else if (request.getParameter("SMIDENTITY") != null) {
        System.err.println("Explicit Siteminder authentication not supported");
      } else if (request.getParameter("SMSESSION") != null) {
        System.err.println("Explicit Siteminder authentication not supported");
      }
      if (isValidExplicitAuthenticationRequest(authenticatedUser, req)) {
        //Debug.print("Successful Explicit FASF Authentication");
        FASFIdentity fasfIdentity = new FASFIdentity(authenticatedUser, req);

        if (simulatedUsers.containsKey(fasfIdentity.getUsername())) {
          authenticatedUser = simulatedUsers.get(fasfIdentity.getUsername());
          fasfIdentity = new FASFIdentity(authenticatedUser, req);
        }

        FASFHttpServletRequest fasfHttpServletRequest = new FASFHttpServletRequest(req, fasfIdentity, fasfAuthenticatorURL);
        SessionLogin.getSessionLogin(fasfHttpServletRequest);
        try {
          Cookie fasfSessionIdentityCookie = null;
          fasfSessionIdentityCookie = new Cookie(fasfSessionIdentityCookieKey, new FASFEncrypter().encryptFASFdentityAsString(fasfIdentity));
          fasfSessionIdentityCookie.setDomain(fasfCookieDomain);
          fasfSessionIdentityCookie.setPath("/");
          fasfSessionIdentityCookie.setMaxAge(0);
          fasfSessionIdentityCookie.setSecure(false);
          resp.addCookie(fasfSessionIdentityCookie);
        } catch (Exception ex1) {
          ex1.printStackTrace();
        }
        //Logger.getLogger("FASF").debug("FASF: Explicit authentication request valid.");
        chain.doFilter(request, response);
        return;
      }
    }
    //Check we're at the right place
    // Cookies are tied to a specific to domain, so if the request doesn't have the correct domain, authentication
    // won't work. So, set the full cannonical name and reload if necessary
    URL requestURL = new URL(req.getRequestURL().toString());
    //System.out.println(requestURL);

    if (!checkCanonicalName(req, resp)) {
      //Logger.getLogger("FASF").debug("FASF: Cannonical name check failed. Reloading..");
      return;
    }

    //Create a CookieSourceIF to retrieve the Cookies from the HttpServletRequest
    CookieSourceIF cookieSource = new GenericCookieSource(req);

    //Check if there exists a valid Identity Cookie
    FASFIdentity validFASFIdentity = findValidIdentityFromCookieSource(cookieSource);
    if (validFASFIdentity != null) {
      if (validFASFIdentity != null && simulatedUsers.containsKey(validFASFIdentity.getUsername())) {
        String simulatedUser = simulatedUsers.get(validFASFIdentity.getUsername());      
        Cookie fasfSessionIdentityCookie = (Cookie) validFASFIdentity.getTransientAttributes("cookie");        
        validFASFIdentity = new FASFIdentity(simulatedUser, req);
        validFASFIdentity.setTransientAttributes("cookie", fasfSessionIdentityCookie);
        
      }

      FASFHttpServletRequest fasfHttpServletRequest = new FASFHttpServletRequest(req, validFASFIdentity, fasfAuthenticatorURL);
      SessionLogin.getSessionLogin(fasfHttpServletRequest);
      validFASFIdentity.pingLastAccess();
      try {
        Cookie fasfSessionIdentityCookie = (Cookie) validFASFIdentity.getTransientAttributes("cookie");

        //System.out.println(requestURL+"\t"+fasfSessionIdentityCookie.getName()+" "+validFASFIdentity.getTransientAttributes("fasfToken"));
        fasfSessionIdentityCookie.setValue(new FASFEncrypter().encryptFASFdentityAsString(validFASFIdentity));
        fasfSessionIdentityCookie.setDomain(fasfCookieDomain);
        fasfSessionIdentityCookie.setPath("/");
        fasfSessionIdentityCookie.setMaxAge(60 * 60 * FASF_COOKIE_NAME_EXPIRATION_HOURS);
        resp.addCookie(fasfSessionIdentityCookie);
      } catch (Exception ex1) {
        ex1.printStackTrace();
      }
      chain.doFilter(fasfHttpServletRequest, resp);
      return;
    }

    //Will accept either App Cookie or Default Cookie
    String cookieKey = fasfCookieKey;
    boolean foundAppCookie = (cookieSource.hasCookie(cookieKey) && hasLength(cookieSource.getCookie(cookieKey).getValue()));

    if (!foundAppCookie) {
      if (cookieSource.hasCookie(DEFAULT_FASF_COOKIE_NAME) && hasLength(cookieSource.getCookie(DEFAULT_FASF_COOKIE_NAME).getValue())) {
        cookieKey = DEFAULT_FASF_COOKIE_NAME;
      }
    }

    if (cookieSource.hasCookie(cookieKey) && hasLength(cookieSource.getCookie(cookieKey).getValue())
            && !cookieSource.getCookie(cookieKey).getValue().equals("invalidate")) {
//If the cookie exists, check for the auth key (cookieKey)
      req.getSession().removeAttribute("FASF_AUTH_STARTED");
      String authenticatedUser = null;
      try {
        String fasfCookieKeyValue = unescape(cookieSource.getCookie(cookieKey).getValue());
        if (!fasfCookieKeyValue.equals("unauthenticated")) {
          authenticatedUser = new FASFEncrypter().decrypt(fasfCookieKeyValue);
        }
      } catch (Exception ex) {
        //ex.printStackTrace();
      }

//This is part of the upcoming enhanced security. For now, if null, just create a FASFIdentity
      FASFIdentity fasfIdentity = null;
      try {
        fasfIdentity = new FASFEncrypter().decryptFASFIdentity(unescape(cookieSource.getCookie(fasfSessionIdentityCookieKey).getValue()));
      } catch (Exception ex) {
      }
      // If the fasfIdentity failed but we have an autheticated user, use that
      if ((fasfIdentity == null || fasfIdentity.getUsername() == null) && authenticatedUser != null) {
        fasfIdentity = new FASFIdentity(authenticatedUser, req);
      }
      if (fasfIdentity != null && simulatedUsers.containsKey(fasfIdentity.getUsername())) {
        authenticatedUser = simulatedUsers.get(fasfIdentity.getUsername());
        fasfIdentity = new FASFIdentity(authenticatedUser, req);
      }

      if (authenticatedUser != null) {
//We have an authenticated user and are ready to go
// Create a FASFHttpServletRequest Object and continue on the chain
        FASFHttpServletRequest fasfHttpServletRequest = new FASFHttpServletRequest(req, new FASFIdentity(authenticatedUser, req), fasfAuthenticatorURL);
//Setup the SessionLogin object
        try {
          SessionLogin.getSessionLogin(fasfHttpServletRequest);
        } catch (Exception e) {
//Problem getting user details- Ain't going nowhere from here!!
          e.printStackTrace();
          resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
          return;
        }
        chain.doFilter(fasfHttpServletRequest, resp);
      } else {
        //Logger.getLogger("FASF").debug("FASF: Authentication cookie found, but appears invalid. Reloading..");

//Hmm. We have an auth key (fasfCookieKey), but it could not be resolved to an authenticatedUser.
// Set it to "invalidate" to force a refresh and send an unhappy message
        Cookie authCookie = new Cookie(fasfCookieKey, "invalidate");
        authCookie.setDomain(fasfCookieDomain);
        authCookie.setPath("/");
        authCookie.setMaxAge(0);
        authCookie.setSecure(false);
        resp.addCookie(authCookie);
        resp.getWriter().write("Authentication problem. This may be due to a network problem. "
                + "Please refresh the browser to refresh and restart the authorization process. "
                + "If the problem continues, please contact Research Informatics.");
        if (req.getSession() != null) {
          req.getSession().invalidate();
        }
        return;
      }
    } else if (req.getSession() != null && req.getSession().getAttribute("FASF_AUTH_STARTED") != null
            && req.getSession().getAttribute("FASF_AUTH_STARTED").equals("true")) {
//If no cookie exists, check if the auth has started, if so, no negotiation occurred (cookie problem)
      req.getSession().removeAttribute("FASF_AUTH_STARTED");
      req.getSession().invalidate();
      resp.getWriter().write("Failed to negotiate authentication");
      return;
    } else {
//Begin the authentication
      Cookie fasfAuthCookie = new Cookie(fasfCookieKey, "unauthenticated");
      fasfAuthCookie.setDomain(fasfCookieDomain);
      fasfAuthCookie.setPath("/");
      resp.addCookie(fasfAuthCookie);
      String url = fasfAuthenticatorURL;
      Map<String, String[]> parameterMap = req.getParameterMap();
      List<String> queryTerms = new ArrayList<String>();
      queryTerms.add("FASF_CALLER_URL=" + escape(requestURL + ""));
      queryTerms.add("FASF_COOKIE_DOMAIN=" + escape(fasfCookieDomain));
      queryTerms.add("FASF_COOKIE_KEY=" + escape(fasfCookieKey + ""));
      queryTerms.add("FASF_SESSION_IDENTITY_KEY=" + escape(fasfSessionIdentityCookieKey + ""));

      if (this.useTransientPostedParameters && req.getMethod().equalsIgnoreCase("POST")) {
        Map<String, String> postedParameterMap = new HashMap<String, String>();
        for (String paramName : parameterMap.keySet()) {
          postedParameterMap.put(paramName, req.getParameter(paramName));
          queryTerms.add(paramName + "=" + escape(FASFHttpServletRequest.FASF_POSTEDPARAM_KEY));
        }
        if (!postedParameterMap.isEmpty()) {
          req.getSession().setAttribute(FASFHttpServletRequest.FASF_POSTEDPARAM_KEY, postedParameterMap);
        }
      } else {
        for (String paramName : parameterMap.keySet()) {
          String[] paramValues = parameterMap.get(paramName);
          for (String paramValue : paramValues) {
            if (parameterEncoder != null) {
              queryTerms.add(paramName + "=" + escape(parameterEncoder.encodeParameter(paramName, paramValue)));
            } else {
              queryTerms.add(paramName + "=" + escape(paramValue));
            }
          }
        }
      }
      String fullURL = url + "?" + join(queryTerms, '&');
      req.getSession(true).setAttribute("FASF_AUTH_STARTED", "true");

      //Logger.getLogger("FASF").debug("FASF: Authentication started..");
      resp.sendRedirect(fullURL);
    }
  }

  /**
   * Checks that an explicit authentication request is valid
   *
   * @param authenticatedUser String
   * @param req HttpServletRequest
   * @return boolean
   */
  private boolean isValidExplicitAuthenticationRequest(String authenticatedUser, HttpServletRequest req) {
    if (authenticatedUser == null) {
      return false;
    }
    if (trustedSites.isEmpty()) { //Means all are allowed
      return true;
    }
    String remoteHostAddr = req.getRemoteAddr();
    if (trustedSites.contains(remoteHostAddr)) {
      return true;
    }
    String remoteHostName = getCanonicalHostNamebyAddress(remoteHostAddr);
    if (remoteHostName != null && trustedSites.contains(remoteHostName.toLowerCase())) {
      return true;
    }
    return false;
  }

  /**
   * Returns whether the host in the request is a canonical name. It checks this by checking if the host ends
   * with '.amgen.com'. Otherwise, it looks up the canonical name and creates the canonical name. Depending on
   * the system naming service, the canonical name may not include domain. For this case, the canonical name
   * is assumed to be <name>.amgen.com. Having a name which ends in .amgen.com is required for the
   * authentication cookie sharing. If the request does not use the canonical name, the response is sent a
   * redirect to the proper canonical name.
   *
   * @param req HttpServletRequest
   * @param resp HttpServletResponse
   * @return boolean
   * @throws IOException
   */
  private boolean checkCanonicalName(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    URL requestURL = new URL(req.getRequestURL().toString());
    String requestHost = requestURL.getHost().toLowerCase();
    if (requestHost.endsWith(".amgen.com")) {
      return true;
    }
    req.getSession().removeAttribute("FASF_AUTH_STARTED");
    String[] serverName = getCanonicalHostName();
    String serverHost = serverName[0];
    String serverDomain = serverName[1];
    String serverCanonicalName = serverName[2];
    if (serverHost.equals(serverCanonicalName)) {
      String requestFile = requestURL.getFile();
      if (hasLength(req.getQueryString())) {
        requestFile = requestFile + "?" + req.getQueryString();
      }
      URL updatedRequest = new URL(req.getScheme(), serverHost + ".amgen.com", req.getServerPort(), requestFile);
      resp.sendRedirect(updatedRequest + "");
      return false;
    }
    String requestFile = requestURL.getFile();
    if (hasLength(req.getQueryString())) {
      requestFile = requestFile + "?" + req.getQueryString();
    }
    URL updatedRequest = new URL(req.getScheme(), serverCanonicalName, req.getServerPort(), requestFile);
    resp.sendRedirect(updatedRequest + "");
    return false;
  }

  /**
   * Returns a value from an InitParameter in the FilterConfig or a default value if not present.
   *
   * @param filterConfig FilterConfig
   * @param paramName String
   * @param defaultValue String
   * @return String
   */
  private String getValueFromInitParameter(FilterConfig filterConfig, String paramName, String defaultValue) {
    if (filterConfig.getInitParameter(paramName) == null) {
      return defaultValue;
    }
    return filterConfig.getInitParameter(paramName);
  }

  private FASFIdentity findValidIdentityFromCookieSource(CookieSourceIF cookieSource) {
    for (Cookie cookie : cookieSource) {
      if (!cookie.getName().equals("SMIDENTITY") && !cookie.getName().equals("SMSESSION") && !cookie.getName().equals("JSESSIONID")) {
        try {
          FASFIdentity fasfIdentity = new FASFEncrypter().decryptFASFIdentity(unescape(cookie.getValue()));
          if (fasfIdentity != null && hasLength(fasfIdentity.getUsername()) && fasfIdentity.getTimeSinceLastAccess(Calendar.HOUR) < FASF_COOKIE_NAME_EXPIRATION_HOURS) {
            fasfIdentity.setTransientAttributes("cookie", cookie);
            return fasfIdentity;
          }
        } catch (EncryptionException ex) {
        } catch (IOException ex) {
        } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
        } catch (Exception ex) {
        }
      }
    }
    return null;
  }

  //Imported methods to avoid additiona dependencies
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
   * Returns whether the 2 Strings are equivalent ignoring case checking that neither is null
   *
   * @param s String
   * @return boolean
   */
  public static boolean equalsIgnoreCase(String s, String v) {
    return (s != null && v != null && s.equalsIgnoreCase(v));
  }

  /**
   * Returns whether the String has length>0 first checking that it is not null
   *
   * @param s String
   * @return boolean
   */
  public static boolean hasLength(String s) {
    return (s != null && s.length() > 0);
  }

  /**
   * Returns whether the String has length>0 after trimming first checking that it is not null
   *
   * @param s String
   * @return boolean
   */
  public static boolean hasTrimmedLength(String s) {
    return (s != null && s.trim().length() > 0);
  }

  /**
   * Converts a String which has had its characters encoded with the %xx hexadecimal form back to its ASCII
   * character set equivalents. Useful to convert Strings encoded by the javascript escape function
   *
   * @param s %xx hexadecimal encoded String
   * @return String in ASCII form
   */
  public static String unescape(String s) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '%' && i < s.length() - 2) {
        String hex = s.substring(i + 1, i + 3);
        int ascii = Integer.parseInt(hex, 16);
        sb.append((char) ascii);
        i += 2;
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
   * Converts a String which has any URL characters that require encoding with hexadecimal %xx format.
   *
   * @param s String to encode
   * @return a String with hexadecimal encodings
   */
  public static String escape(String s) {
    try {
      return URLEncoder.encode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Error in urlDecode.", e);
    }
  }

  /**
   * Joins the items of a Set into one long String. Similar to concatArray, but offsets any concatenated array
   * which contains the delimiter in double-quotes. Any double quotes in array elements are replaced to single
   * quotes.
   *
   * @param array the original string array.
   * @param delim the delimiter to use between array elements
   * @return a <code>String</code> that contains each array element.
   */
  public static String join(Collection array, char delim) {
    if (array == null || array.isEmpty()) {
      return "";
    }
    if (array.size() == 1) {
      return array.iterator().next().toString();
    }
    StringBuilder sb = new StringBuilder();
    for (Object field : array) {
      if (sb.length() > 0) {
        sb.append(delim);
      }
      if (field == null) {
        sb.append("null");
      } else {
        sb.append(field.toString());
      }
    }
    return sb.toString();
  }
}
