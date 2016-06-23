package amgen.ri.servlet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Simple implementation of CookieSourceIF that grabs them from a HttpServletRequest
 *
 * @author J. McDowell
 * @version $Id
 */
public class GenericCookieSource implements CookieSourceIF {
    private Map<String, Cookie> cookies;

    public GenericCookieSource(HttpServletRequest request) {
        cookies = new HashMap<String, Cookie> ();
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                cookies.put(cookie.getName(), cookie);
            }
        }
    }

    /**
     *
     * @param cookieName String
     * @return Cookie
     */
    public Cookie getCookie(String cookieName) {
        return cookies.get(cookieName);
    }

    /**
     *
     * @param cookieName String
     * @return Cookie
     */
    public String getCookieValue(String cookieName) {
        return (hasCookie(cookieName) ? unescape(getCookie(cookieName).getValue()) : null);
    }

    /**
     *
     * @param cookieName String
     * @return boolean
     */
    public boolean hasCookie(String cookieName) {
        return (getCookie(cookieName) != null);
    }

    /**
     * Returns where the source has the Cookie given its name and its value is non-null and non-zero length
     *
     * @param cookieName String
     * @return boolean
     */
    public boolean hasCookieWithLength(String cookieName) {
        return (hasCookie(cookieName) && getCookie(cookieName).getValue() != null && getCookie(cookieName).getValue().length() > 0);
    }

    /**
     * Converts a String which has had its characters encoded with the
     * %xx hexadecimal form back to its ASCII character set equivalents.
     * Useful to convert Strings encoded by the javascript escape function
     * @param s %xx hexadecimal encoded String
     * @return String in ASCII form
     */
    private String unescape(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '%' && i < s.length() - 2) {
                String hex = s.substring(i + 1, i + 3);
                int ascii = Integer.parseInt(hex, 16);
                sb.append( (char) ascii);
                i += 2;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (String name : cookies.keySet()) {
            sb.append(name + "=" + unescape(cookies.get(name).getValue()));
            sb.append("\n");
        }
        return sb.toString();
    }

    public Cookie[] getCookies() {
        return (Cookie[]) cookies.values().toArray(new Cookie[0]);
    }

    public Iterator<Cookie> iterator() {
        return cookies.values().iterator();
    }

}
