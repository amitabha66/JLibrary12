package amgen.ri.servlet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import amgen.ri.ldap.AmgenLDAPAuthProvider;
import amgen.ri.security.FASFIdentity;
import amgen.ri.util.ExtBase64;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class BasicAuthenticationFilter implements Filter {
    /**
     * HTTP Authorization header, equal to <code>Authorization</code>
     */
    protected static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * HTTP Authentication header, equal to <code>WWW-Authenticate</code>
     */
    protected static final String AUTHENTICATE_HEADER = "WWW-Authenticate";

    /**
     * The authcScheme to look for in the <code>Authorization</code> header, defaults to <code>BASIC</code>
     */
    private String authcScheme = HttpServletRequest.BASIC_AUTH;

    /**
     * The authzScheme value to look for in the <code>Authorization</code> header, defaults to <code>BASIC</code>
     */
    private String authzScheme = HttpServletRequest.BASIC_AUTH;

    private String fasfAuthenticatorURL;
    private final String fasfCookieDomain = ".amgen.com";
    private List<String> excludedFiles;
    private List<String> excludedPaths;

    public BasicAuthenticationFilter() {
        excludedFiles = new ArrayList<String> ();
        excludedPaths = new ArrayList<String> ();
    }

    /**
     * Called by the web container to indicate to a filter that it is being
     * placed into service.
     *
     * @param filterConfig FilterConfig
     * @throws ServletException
     * @todo Implement this javax.servlet.Filter method
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        String filterFileExclusions = filterConfig.getInitParameter("AUTHENTICATOR_EXCLUDEDFILES");
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
        String filterPathExclusions = filterConfig.getInitParameter("AUTHENTICATOR_EXCLUDEDPATHS");
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
    }

    /**
     * Called by the web container to indicate to a filter that it is being
     * taken out of service.
     *
     * @todo Implement this javax.servlet.Filter method
     */
    public void destroy() {
    }

    /**
     * The <code>doFilter</code> method of the Filter is called by the
     * container each time a request/response pair is passed through the
     * chain due to a client request for a resource at the end of the chain.
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
        //Check if session login exists. If so, just continue chain.
        if (SessionLogin.doesSessionLoginExist(req)) {
            chain.doFilter(new FASFHttpServletRequest(req, new FASFIdentity(SessionLogin.getSessionLogin(req).getRemoteUser(), req.getRemoteHost()), fasfAuthenticatorURL),
                           resp);
            return;
        }
        //Check if the requested file is in the exclusion list. If so, just continue chain.
        String[] requestedPath = req.getRequestURI().split("\\/");
        String requestedFile = requestedPath[requestedPath.length - 1];
        if (excludedFiles.contains(requestedFile)) {
            chain.doFilter(request, response);
            return;
        }
        //Check if the path is in the exclusion list. If so, just continue chain.
        for (String excludedPath : excludedPaths) {
            if (req.getRequestURI().startsWith(excludedPath)) {
                chain.doFilter(request, response);
                return;
            }
        }
        try {
            String username = onAccessDenied(req, resp);
            if (username != null) {
                FASFHttpServletRequest fasfHttpServletRequest = new FASFHttpServletRequest(req, new FASFIdentity(username, req.getRemoteHost()), fasfAuthenticatorURL);
                SessionLogin.getSessionLogin(fasfHttpServletRequest);
                chain.doFilter(request, response);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Returns the HTTP <b><code>WWW-Authenticate</code></b> header scheme that this filter will use when sending
     * the HTTP Basic challenge response.  The default value is <code>BASIC</code>.
     *
     * @return the HTTP <code>WWW-Authenticate</code> header scheme that this filter will use when sending the HTTP
     *         Basic challenge response.
     * @see #sendChallenge
     */
    public String getAuthcScheme() {
        return authcScheme;
    }

    /**
     * Processes unauthenticated requests. It handles the two-stage request/challenge authentication protocol.
     *
     * @param request  incoming ServletRequest
     * @param response outgoing ServletResponse
     * @return true if the request should be processed; false if the request should not continue to be processed
     */
    protected String onAccessDenied(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String loggedInUser = null; //null by default or we wouldn't be in this method
        if (isLoginAttempt(request, response)) {
            loggedInUser = executeLogin(request, response);
        }
        if (loggedInUser == null) {
            sendChallenge(request, response);
        }
        return loggedInUser;
    }

    protected String executeLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String[] userPass = getCredentials(request, response);
        new AmgenLDAPAuthProvider().authenticate(userPass[0], userPass[1]);
        return userPass[0];
    }

    /**
     * Determines whether the incoming request is an attempt to log in.
     * <p/>
     * The default implementation obtains the value of the request's
     * {@link #AUTHORIZATION_HEADER AUTHORIZATION_HEADER}, and if it is not <code>null</code>, delegates
     * to {@link #isLoginAttempt(String) isLoginAttempt(authzHeaderValue)}. If the header is <code>null</code>,
     * <code>false</code> is returned.
     *
     * @param request  incoming ServletRequest
     * @param response outgoing ServletResponse
     * @return true if the incoming request is an attempt to log in based, false otherwise
     */
    protected boolean isLoginAttempt(HttpServletRequest request, HttpServletResponse response) {
        String authzHeader = getAuthzHeader(request);
        return authzHeader != null && isLoginAttempt(authzHeader);
    }

    /**
     * Default implementation that returns <code>true</code> if the specified <code>authzHeader</code>
     * starts with the same (case-insensitive) characters specified by the
     * {@link #getAuthzScheme() authzScheme}, <code>false</code> otherwise.
     * <p/>
     * That is:
     * <p/>
     * <code>String authzScheme = getAuthzScheme().toLowerCase();<br/>
     * return authzHeader.toLowerCase().startsWith(authzScheme);</code>
     *
     * @param authzHeader the 'Authorization' header value (guaranteed to be non-null if the
     *                    {@link #isLoginAttempt(javax.servlet.ServletRequest, javax.servlet.ServletResponse)} method is not overriden).
     * @return <code>true</code> if the authzHeader value matches that configured as defined by
     *         the {@link #getAuthzScheme() authzScheme}.
     */
    protected boolean isLoginAttempt(String authzHeader) {
        String authzScheme = getAuthzScheme().toLowerCase();
        return authzHeader.toLowerCase().startsWith(authzScheme);
    }

    /**
     * Returns the HTTP <b><code>Authorization</code></b> header value that this filter will respond to as indicating
     * a login request.
     * <p/>
     * Unless overridden by the {@link #setAuthzScheme(String) setAuthzScheme(String)} method, the
     * default value is <code>BASIC</code>.
     *
     * @return the Http 'Authorization' header value that this filter will respond to as indicating a login request
     */
    public String getAuthzScheme() {
        return authzScheme;
    }

    /**
     * Returns the {@link #AUTHORIZATION_HEADER AUTHORIZATION_HEADER} from the specified ServletRequest.
     * <p/>
     * This implementation merely casts the request to an <code>HttpServletRequest</code> and returns the header:
     * <p/>
     * <code>HttpServletRequest httpRequest = {@link WebUtils#toHttp(javax.servlet.ServletRequest) toHttp(reaquest)};<br/>
     * return httpRequest.getHeader({@link #AUTHORIZATION_HEADER AUTHORIZATION_HEADER});</code>
     *
     * @param request the incoming <code>ServletRequest</code>
     * @return the <code>Authorization</code> header's value.
     */
    protected String getAuthzHeader(HttpServletRequest request) {
        return request.getHeader(AUTHORIZATION_HEADER);
    }

    protected void sendChallenge(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String authcHeader = getAuthcScheme() + " realm=\"" + "Application" + "\"";
        response.setHeader("WWW-Authenticate", authcHeader);
    }

    /**
     * Creates an AuthenticationToken for use during login attempt with the provided credentials in the http header.
     * <p/>
     * This implementation:
     * <ol><li>acquires the username and password based on the request's
     * {@link #getAuthzHeader(javax.servlet.ServletRequest) authorization header} via the
     * {@link #getPrincipalsAndCredentials(String, javax.servlet.ServletRequest) getPrincipalsAndCredentials} method</li>
     * <li>The return value of that method is converted to an <code>AuthenticationToken</code> via the
     * {@link #createToken(String, String, javax.servlet.ServletRequest, javax.servlet.ServletResponse) createToken} method</li>
     * <li>The created <code>AuthenticationToken</code> is returned.</li>
     * </ol>
     *
     * @param request  incoming ServletRequest
     * @param response outgoing ServletResponse
     * @return the AuthenticationToken used to execute the login attempt
     */
    protected String[] getCredentials(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = getAuthzHeader(request);
        if (authorizationHeader == null || authorizationHeader.length() == 0) {
            return null;
        }

        String[] prinCred = getPrincipalsAndCredentials(authorizationHeader, request);
        if (prinCred == null || prinCred.length < 2) {
            return null;
        }

        String username = prinCred[0];
        String password = prinCred[1];

        return new String[] {
            username, password};
    }

    /**
     * Returns the username obtained from the
     * {@link #getAuthzHeader(javax.servlet.ServletRequest) authorizationHeader}.
     * <p/>
     * Once the <code>authzHeader is split per the RFC (based on the space character, " "), the resulting split tokens
     * are translated into the username/password pair by the
     * {@link #getPrincipalsAndCredentials(String, String) getPrincipalsAndCredentials(scheme,encoded)} method.
     *
     * @param authorizationHeader the authorization header obtained from the request.
     * @param request             the incoming ServletRequest
     * @return the username (index 0)/password pair (index 1) submitted by the user for the given header value and request.
     * @see #getAuthzHeader(javax.servlet.ServletRequest)
     */
    protected String[] getPrincipalsAndCredentials(String authorizationHeader, ServletRequest request) throws IOException {
        if (authorizationHeader == null) {
            return null;
        }
        String[] authTokens = authorizationHeader.split(" ");
        if (authTokens == null || authTokens.length < 2) {
            return null;
        }
        return getPrincipalsAndCredentials(authTokens[0], authTokens[1]);
    }

    /**
     * Returns the username and password pair based on the specified <code>encoded</code> String obtained from
     * the request's authorization header.
     * <p/>
     * Per RFC 2617, the default implementation first Base64 decodes the string and then splits the resulting decoded
     * string into two based on the ":" character.  That is:
     * <p/>
     * <code>String decoded = Base64.decodeToString(encoded);<br/>
     * return decoded.split(":");</code>
     *
     * @param scheme  the {@link #getAuthcScheme() authcScheme} found in the request
     *                {@link #getAuthzHeader(javax.servlet.ServletRequest) authzHeader}.  It is ignored by this implementation,
     *                but available to overriding implementations should they find it useful.
     * @param encoded the Base64-encoded username:password value found after the scheme in the header
     * @return the username (index 0)/password (index 1) pair obtained from the encoded header data.
     */
    protected String[] getPrincipalsAndCredentials(String scheme, String encoded) throws IOException {
        String decoded = new String(ExtBase64.decode(encoded));
        return decoded.split(":");
    }

    /**
     * Returns whether the host in the request is a canonical name. It checks
     * this by checking if the host ends with '.amgen.com'. Otherwise, it looks
     * up the canonical name and creates the canonical name. Depending on the
     * system naming service, the canonical name may not include domain. For
     * this case, the canonical name is assumed to be <name>.amgen.com. Having a
     * name which ends in .amgen.com is required for the authentication cookie
     * sharing. If the request does not use the canonical name, the response is
     * sent a redirect to the proper canonical name.
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
            URL updatedRequest = new URL(req.getScheme(), serverHost + ".amgen.com", req.getServerPort(), requestURL.getFile());
            resp.sendRedirect(updatedRequest + "");
            return false;
        }
        URL updatedRequest = new URL(req.getScheme(), serverCanonicalName, req.getServerPort(), requestURL.getFile());
        resp.sendRedirect(updatedRequest + "");
        return false;
    }

    /**
     * Returns a value from an InitParameter in the FilterConfig or a default
     * value if not present.
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

    /**
     * Returns the canonical names for the localhost. The returned structure is
     * an array of {host name, domain, full name}. What gets returned is
     * dependent on the system naming system.
     *
     * @return String[]
     * @throws UnknownHostException
     */
    private String[] getCanonicalHostName() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        String hostName = addr.getHostName().toLowerCase();
        String canonicalName = addr.getCanonicalHostName().toLowerCase();
        String domain = ( (canonicalName.startsWith(hostName) && canonicalName.length() > hostName.length()) ? canonicalName.substring(hostName.length()) : "");
        return new String[] {
            hostName, domain, canonicalName};
    }
}
