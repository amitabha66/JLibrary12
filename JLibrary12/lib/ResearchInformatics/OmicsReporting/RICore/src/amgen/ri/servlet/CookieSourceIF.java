package amgen.ri.servlet;

import javax.servlet.http.Cookie;

/**
 * Source for HTTP Cookies
 *
 * @author J. McDowell
 * @version $Id
 */
public interface CookieSourceIF extends Iterable<Cookie> {
    /**
     * Returns a Cookie given a name or null if not present
     *
     * @param cookieName String
     * @return Cookie
     */
    public Cookie getCookie(String cookieName);

    /**
     * Returns a Cookie value given a name or null if not present. This performs an unescape on the value!!
     *
     * @param cookieName String
     * @return Cookie
     */
    public String getCookieValue(String cookieName);

    /**
     * Returns where the source has the Cookie given its name
     *
     * @param cookieName String
     * @return boolean
     */
    public boolean hasCookie(String cookieName);

    /**
     * Returns where the source has the Cookie given its name and its value is non-null and non-zero length
     *
     * @param cookieName String
     * @return boolean
     */
    public boolean hasCookieWithLength(String cookieName);

    public Cookie[] getCookies();
}
