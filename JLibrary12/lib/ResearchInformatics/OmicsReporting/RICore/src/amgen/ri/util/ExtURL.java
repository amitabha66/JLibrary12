package amgen.ri.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for manipulating URLs
 *
 * @version $Id: ExtURL.java,v 1.1 2011/10/26 04:13:32 cvs Exp $
 */
public final class ExtURL  {
    /**
     * Parses an URL query string and returns a map with the parameter values.
     * The URL query string is the part in the URL after the first '?' character up
     * to an optional '#' character. It has the format "name=value&name=value&...".
     * The map has the same structure as the one returned by
     * javax.servlet.ServletRequest.getParameterMap().
     * A parameter name may occur multiple times within the query string.
     * For each parameter name, the map contains a string array with the parameter values.
     * @param  s  an URL query string.
     * @return    a map containing parameter names as keys and parameter values as map values.
     * @author    Christian d'Heureuse, Inventec Informatik AG, Switzerland, www.source-code.biz.
     */
    public static Map<String, String[]> parseUrlQueryString(String s) {
        if (s == null) {
            return new HashMap<String, String[]> (0);
        }
        // In map1 we use strings and ArrayLists to collect the parameter values.
        HashMap<String, Object> map1 = new HashMap<String, Object> ();
        int p = 0;
        while (p < s.length()) {
            int p0 = p;
            while (p < s.length() && s.charAt(p) != '=' && s.charAt(p) != '&') {
                p++;
            }
            String name = urlDecode(s.substring(p0, p));
            if (p < s.length() && s.charAt(p) == '=') {
                p++;
            }
            p0 = p;
            while (p < s.length() && s.charAt(p) != '&') {
                p++;
            }
            String value = urlDecode(s.substring(p0, p));
            if (p < s.length() && s.charAt(p) == '&') {
                p++;
            }
            Object x = map1.get(name);
            if (x == null) {
                // The first value of each name is added directly as a string to the map.
                map1.put(name, value);
            } else if (x instanceof String) {
                // For multiple values, we use an ArrayList.
                ArrayList<String> a = new ArrayList<String> ();
                a.add( (String) x);
                a.add(value);
                map1.put(name, a);
            } else {
                @SuppressWarnings("unchecked")
                    ArrayList<String> a = (ArrayList<String>) x;

                a.add(value);
            }
        }
        // Copy map1 to map2. Map2 uses string arrays to store the parameter values.
        HashMap<String, String[]> map2 = new HashMap<String, String[]> (map1.size());
        for (Map.Entry<String, Object> e : map1.entrySet()) {
            String name = e.getKey();
            Object x = e.getValue();
            String[] v;
            if (x instanceof String) {
                v = new String[] {
                    (String) x};
            } else {
                @SuppressWarnings("unchecked")
                    ArrayList<String> a = (ArrayList<String>) x;

                v = new String[a.size()];
                v = a.toArray(v);
            }
            map2.put(name, v);
        }
        return map2;
    }

    /**
     * Decodes a <code>application/x-www-form-urlencoded</code> string using a
     * specific encoding scheme. The supplied encoding is used to determine what
     * characters are represented by any consecutive sequences of the form
     * "<code>%<i>xy</i></code>".
     *
     * <p> <em><strong>Note:</strong> The <a
     * href="http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars">
     * World Wide Web Consortium Recommendation</a> states that UTF-8 should be
     * used. Not doing so may introduce incompatibilites.</em>
     *
     * @param s the <code>String</code> to decode
     * @return the newly decoded <code>String</code>
     * @see URLEncoder#encode(java.lang.String, java.lang.String)
     * @since 1.4
     */
    public static String urlDecode(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error in urlDecode.", e);
        }
    }

    /**
     * Translates a string into <code>application/x-www-form-urlencoded</code>
     * format using a specific encoding scheme. This method uses the supplied
     * encoding scheme to obtain the bytes for unsafe characters.
     *
     * <p> <em><strong>Note:</strong> The <a
     * href="http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars">
     * World Wide Web Consortium Recommendation</a> states that UTF-8 should be
     * used. Not doing so may introduce incompatibilites.</em>
     *
     * @param s <code>String</code> to be translated.
     * @return the translated <code>String</code>.
     * @see URLDecoder#decode(java.lang.String, java.lang.String)
     * @since 1.4
     */
    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error in urlDecode.", e);
        }
    }

    /**
     * Returns a Map with the parts of the URI. The Map contains the following elements
     * source, protocol, authority, domain, port, path, directoryPath, fileName, query, anchor
     */
    public static Map<String, String> parseURI(String uri) {
        Map<String, String> uriParts = new HashMap<String, String> ();
        String[] uriPartNames = {
            "source", "protocol", "authority", "domain", "port", "path", "directoryPath", "fileName", "query", "anchor"};
        Pattern pattern = Pattern.compile(
            "^(?:([^:/?#.]+):)?(?://)?(([^:/?#]*)(?::(\\d*))?)?((/(?:[^?#](?![^?#/]*\\.[^?#/.]+(?:[\\?#]|$)))*/?)?([^?#/]*))?(?:\\?([^#]*))?(?:#(.*))?");

        Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            for (int i = 0; i < matcher.groupCount(); i++) {
                String uriPartName = uriPartNames[i];
                uriParts.put(uriPartName, matcher.group(i));
            }
        }
        // Always end directoryPath with a trailing backslash if a path was present in the source URI
        // Note that a trailing backslash is NOT automatically inserted within or appended to the "path" key
        if (uriParts.get("directoryPath") != null) {
            uriParts.put("directoryPath", uriParts.get("directoryPath").replaceAll("\\/?$", "/"));
        }
        return uriParts;
    }

    public static String createURL(URL url, Map<String, String[]> queryString) throws NumberFormatException, MalformedURLException {
        String urlStr = url.toString();
        if (queryString != null) {
            List<String> sb = new ArrayList<String> ();
            for (String name : queryString.keySet()) {
                String[] values = queryString.get(name);
                if (values == null || values.length == 0) {
                    sb.add(name);
                } else {
                    for (String value : values) {
                        sb.add(name + "=" + ExtString.escape(value));
                    }
                }
            }
            if (sb.size() > 0) {
                urlStr = urlStr + "?" + ExtString.join(sb, "&");
            }
        }
        return urlStr;
    }

}
