package amgen.ri.servlet;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import amgen.ri.security.FASFIdentity;
import amgen.ri.util.Debug;
import amgen.ri.util.ExtString;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.UnavailableException;

public class FASFHttpServletRequest extends HttpServletRequestWrapper implements CookieSourceIF {
    protected static final String FASF_POSTEDPARAM_KEY= "[FASF POSTED]";
    private FASFIdentity fasfIdentity;
    private URL fasfAuthenticatorURL;
    private Map<String, Cookie> cookies;

    public FASFHttpServletRequest(HttpServletRequest req, FASFIdentity fasfIdentity, String fasfAuthenticatorURL) {
        super(req);
        this.fasfIdentity = fasfIdentity;
        if (fasfAuthenticatorURL != null) {
            try {
                this.fasfAuthenticatorURL = new URL(fasfAuthenticatorURL);
            } catch (Exception ex) {
                this.fasfAuthenticatorURL = null;
            }
        }
    }

    public Cookie getCookie(String cookieName) {
        if (cookies == null) {
            cookies = new HashMap<String, Cookie> ();
            if (getCookies() != null) {
                for (Cookie cookie : getCookies()) {
                    cookies.put(cookie.getName(), cookie);
                }
            }
        }
        return cookies.get(cookieName);
    }

    public boolean hasCookie(String cookieName) {
        return (getCookie(cookieName) != null);
    }

    public String getCookieValue(String cookieName) {
        return (hasCookie(cookieName) ? ExtString.unescape(getCookie(cookieName).getValue()) : null);
    }

    /**
     * Returns where the source has the Cookie given its name and its value is non-null and non-zero length
     *
     * @param cookieName String
     * @return boolean
     */
    public boolean hasCookieWithLength(String cookieName) {
        return (hasCookie(cookieName) && ExtString.hasLength(getCookie(cookieName).getValue()));
    }

    public String getRemoteUser() {
        return fasfIdentity.getUsername();
    }

    public FASFIdentity getFASFIdentity() {
        return fasfIdentity;
    }

    public URL getFasfAuthenticatorURL() {
        return fasfAuthenticatorURL;
    }

    public Iterator<Cookie> iterator() {
        return cookies.values().iterator();
    }
    
    @Override
    public String getParameter(String name) {
      String value= super.getParameter(name);
      if (FASF_POSTEDPARAM_KEY.equals(value)) {
        try {
          return getPostedParameterValue(name);
        } catch (UnavailableException ex) {
          return null;
        }
      }
      return value;
    }
    @Override
    public Map<String,String[]> getParameterMap() {
      return super.getParameterMap();
    }
    
    @Override
    public String[] getParameterValues(String name) {
      return super.getParameterValues(name);
    }

  private String getPostedParameterValue(String name) throws UnavailableException {
    Map<String, String> postedParameterMap= (Map)getSession().getAttribute(FASF_POSTEDPARAM_KEY);
    if (postedParameterMap!= null && postedParameterMap.containsKey(name)) {
      return postedParameterMap.get(name);
    }
    throw new javax.servlet.UnavailableException("Parameter not available");
  }
    
    
    

}
