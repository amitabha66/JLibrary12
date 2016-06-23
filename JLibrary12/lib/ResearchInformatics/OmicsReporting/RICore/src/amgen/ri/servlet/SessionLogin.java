package amgen.ri.servlet;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import amgen.ri.ldap.AmgenEnterpriseEntry;
import amgen.ri.ldap.AmgenEnterpriseLookup;
import amgen.ri.security.AbstractIdentity;
import amgen.ri.security.FASFEncrypter;
import amgen.ri.security.FASFIdentity;
import amgen.ri.security.IdentityIF;
import amgen.ri.security.SessionIdentityIF;

/**
 * Class which maintains the session user information.
 */
public class SessionLogin extends AbstractIdentity implements IdentityIF {
    private Date authDate;
    private Date lastAccess;
    private SessionIdentityIF sessionIdentity;

    private String sessionID;
    private String remoteMachineIP; //User's machine IP

    private String smSessionToken;
    private String smIdentityToken;

    /**
     * Creates a new SessionLogin object. Only called by the getSessionLogin
     * method
     *
     * @param request HttpServletRequest
     */
    private SessionLogin(final HttpServletRequest request) throws ServletException {
        String username = request.getRemoteUser();
        if (username == null) {
            throw new ServletException("General Error: No user defined in session");
        }
        setUsername(username);
        this.sessionIdentity = new FASFIdentity(username, request.getRemoteHost());
        
        if (getActiveDirectoryEntry()== null) {
            throw new SecurityException("LDAP Error: Unable to retrieve user from Active directory");
        }
        this.sessionID = request.getSession().getId();
        this.remoteMachineIP = request.getRemoteHost();
        this.authDate = new Date();
        updateSMTokens(request);

        request.getSession().setAttribute("SESSION_LOGIN_USERNAME", getActiveDirectoryEntry().getLedgerName()+ " (" + sessionIdentity.getUsername() + ")");
        //Debug.print("SessionLogin- " + sessionIdentity.getUsername());
    }

    /**
     * Creates a new SessionLogin object. Only called by the getSessionLogin
     * method
     *
     * @param request HttpServletRequest
     */
    protected SessionLogin(SessionLogin source) throws ServletException {
    	super(source.getUsername());
        this.authDate = (Date) source.authDate.clone();
        this.sessionIdentity = (SessionIdentityIF) source.sessionIdentity.clone();
        this.sessionID = source.sessionID;
        this.remoteMachineIP = source.remoteMachineIP;
        this.smIdentityToken = source.smIdentityToken;
        this.smSessionToken = source.smSessionToken;
    }

    /**
     * Updates the Siteminder tokens using the given HttpServletRequest object.
     *
     *
     * @param request HttpServletRequest
     */
    public void updateSMTokens(final HttpServletRequest request) {
        CookieSourceIF cookieSource = getCookieSource(request);
        String smSessionToken = (cookieSource.hasCookie("SMSESSION") ? cookieSource.getCookie("SMSESSION").getValue() : null);
        String smIdentityToken = (cookieSource.hasCookie("SMIDENTITY") ? cookieSource.getCookie("SMIDENTITY").getValue() : null);
        if (hasLength(smSessionToken) && !isEqual(smSessionToken, this.smSessionToken)) {
            //Debug.print("Updating smSessionToken "+smSessionToken.substring(0, 30));
            this.smSessionToken = smSessionToken;
        }
        if (hasLength(smIdentityToken) && !isEqual(smIdentityToken, this.smIdentityToken)) {
            //Debug.print("Updating smIdentityToken");
            this.smIdentityToken = smIdentityToken;
        }
    }

    /**
     * Updates the Siteminder tokens with the given tokens if they are non-null.
     *
     * @param smSessionToken HttpServletRequest
     * @param smIdentityToken String
     */
    public void setSMTokens(String smSessionToken, String smIdentityToken) {
        if (smSessionToken != null) {
            this.smSessionToken = smSessionToken.trim();
            //Debug.print("Setting smSessionToken "+smSessionToken.substring(0, 30));
        }
        if (smIdentityToken != null) {
            this.smIdentityToken = smIdentityToken.trim();
        }
    }

    /**
     * Returns a CookieSource for the HttpServletRequest
     *
     * @param request HttpServletRequest
     * @return CookieSourceIF
     */
    public CookieSourceIF getCookieSource(HttpServletRequest request) {
        return (request instanceof CookieSourceIF ? (CookieSourceIF) request : new GenericCookieSource(request));
    }

    /**
     * Retrieves or creates a new SessionLogin from the request
     *
     * @param request HttpServletRequest
     * @return SessionLogin
     * @throws AIGException
     */
    public static SessionLogin getSessionLogin(HttpServletRequest request) throws ServletException, SecurityException {
        if (request.getSession().getAttribute("SESSION_LOGIN") == null) {
            request.getSession().setAttribute("SESSION_LOGIN", new SessionLogin(request));
        }
        return (SessionLogin) request.getSession().getAttribute("SESSION_LOGIN");
    }

    /**
     * Returns the SessionLogin for the session. If none exists, returns null.
     *
     * @param session HttpSession
     * @return SessionLogin
     */
    public static SessionLogin getSessionLogin(HttpSession session) {
        return (SessionLogin) session.getAttribute("SESSION_LOGIN");
    }

    /**
     * Takes the current SessionLogin object and wraps it in a custom
     * SessionLogin child class. The child class MUST have a constructor of the
     * format <E>(SessionLogin, HttpServletRequest) or <E>(SessionLogin).
     * If not or if anything fails in the instantiation, this returns null
     *
     * @param request HttpServletRequest
     * @param clazz Class
     * @return SessionLogin
     */
    public static SessionLogin updateSessionLogin(HttpServletRequest request, Class clazz) {
        try {
            SessionLogin sessionLogin = getSessionLogin(request);
            Constructor cnstr = clazz.getConstructor(new Class[] {SessionLogin.class, HttpServletRequest.class});
            SessionLogin updatedSessionLogin = (SessionLogin) cnstr.newInstance(new Object[] {sessionLogin, request});
            request.getSession().setAttribute("SESSION_LOGIN", updatedSessionLogin);
            return updatedSessionLogin;
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
        try {
            SessionLogin sessionLogin = getSessionLogin(request);
            Constructor cnstr = clazz.getConstructor(new Class[] {SessionLogin.class});
            SessionLogin updatedSessionLogin = (SessionLogin) cnstr.newInstance(new Object[] {sessionLogin});
            request.getSession().setAttribute("SESSION_LOGIN", updatedSessionLogin);
            return updatedSessionLogin;
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
        return null;
    }

    /**
     * doesSessionLoginExist
     *
     * @param sessionObj Object
     */
    public static boolean doesSessionLoginExist(Object sessionObj) {
        HttpSession session;
        if (sessionObj instanceof HttpSession) {
            session = (HttpSession) sessionObj;
        } else if (sessionObj instanceof HttpServletRequest) {
            session = ( (HttpServletRequest) sessionObj).getSession(true);
        } else {
            throw new IllegalArgumentException("Argument not a session-containing object");
        }
        return (session.getAttribute("SESSION_LOGIN") != null);
    }

    /**
     * Returns the remote user
     *
     * @return String
     */
    public String getRemoteUser() {
        return getUsername();
    }

    /**
     * Returns the remote user as an AmgenEnterpriseEntry with attributes sets
     * from the Amgen Enterprise LDAP instance
     *
     * @return String
     */
    public AmgenEnterpriseEntry getAmgenEnterpriseEntry() {
        return new AmgenEnterpriseLookup().lookupByUID(getUsername());
    }

    /**
     * Returns the remote user FASF encrypted
     *
     * @return String
     */
    public String getRemoteUserFASFEncrypted() {
        try {
            return new FASFEncrypter().encrypt(getRemoteUser());
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Returns the AIG timezone based on the users site
     *
     * @return TimeZone
     */
    public TimeZone getPrimaryTimeZone() {
        return TimeZone.getTimeZone("America/Los_Angeles");
    }

    /**
     * Returns the session ID that this login is registered
     *
     * @return String
     */
    public String getSessionID() {
        return sessionID;
    }

    /**
     * Returns the remote machine's IP
     *
     * @return String
     */
    public String getRemoteMachineIP() {
        return remoteMachineIP;
    }

    /**
     * Returns the Date that this login was created
     *
     * @return Date
     */
    public Date getAuthDate() {
        return authDate;
    }

    /**
     * Returns the last access Date. Only gets set after a call to
     * updateLastAccess.
     *
     * @return Date
     */
    public Date getLastAccess() {
        return lastAccess;
    }

    /**
     * Returns the current SM session token
     *
     * @return String
     */
    public String getSmSessionToken() {
        return smSessionToken;
    }

    /**
     * Returns the current SM identity token
     *
     * @return String
     */
    public String getSmIdentityToken() {
        return smIdentityToken;
    }

    /**
     * Returns the SessionIdentity object defined in the request
     *
     * @return SessionIdentityIF
     */
    public SessionIdentityIF getSessionIdentity() {
        return sessionIdentity;
    }

    /**
     * Gets the encrypter user name if the session was initiated by the FASF
     * system
     *
     * @return String
     */
    public String getCypherUser() {
        try {
            return new FASFEncrypter().encrypt(getRemoteUser());
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Updates the last access date returning the previous last access or null
     * if there was not one
     *
     * @return Date
     */
    public Date updateLastAccess() {
        Date prevLastAccess = getLastAccess();
        this.lastAccess = new Date();
        return prevLastAccess;
    }

    /**
     * Utility returning whether it has been more than the given minutes since
     * the last access. Returns false is last access is null.
     *
     * @param mins long
     * @return boolean
     */
    public boolean isLastAccessGreater(long mins) {
        if (lastAccess == null) {
            return false;
        }
        double millisDiff = (new Date()).getTime() - lastAccess.getTime();
        double minsDiff = millisDiff / 60000;
        return (minsDiff > mins);
    }

    /**
     * Utility method
     *
     * @param s String
     * @return boolean
     */
    private boolean hasLength(String s) {
        return (s!= null && s.length()>0);
    }

    /**
     * Utility method
     *
     * @param s1 String
     * @param s2 String
     * @return boolean
     */
    private boolean isEqual(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.equals(s2);
    }

}
