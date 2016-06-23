package amgen.ri.ldap;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * Look up user information via the Amgen LDAP Directory
 */
public class AmgenLDAPAuthProvider {

  public static final String LDAP_URL = "ldap://ldap.amgen.com:389";
  public static final String BASE_PEOPLE_DN = "ou=people,dc=enterprise,dc=amgen,dc=com";
  public static final String BASE_SERVICEACCOUNTS_DN = "ou=service accounts,dc=enterprise,dc=amgen,dc=com";
  public static final String BASE_DN = "dc=enterprise,dc=amgen,dc=com";
  public static final String BASE_GROUPS_DN = "ou=groups,dc=enterprise,dc=amgen,dc=com";
  public static final String BIND_DN = "uniqueIdentifier=%s,ou=people,dc=enterprise,dc=amgen,dc=com";

  private String ldapURL;
  private DirContext dirContext;
  private String baseDN;
  private String bindDN;
  private int searchScope;

  public AmgenLDAPAuthProvider() {
    this(null, null, null);
  }

  public AmgenLDAPAuthProvider(String ldapURL) {
    this(ldapURL, null, null);
  }

  public AmgenLDAPAuthProvider(String ldapURL, String baseDN) {
    this(ldapURL, baseDN, null);
  }

  public AmgenLDAPAuthProvider(String ldap_url, String baseDN, String bindDN) {
    this.ldapURL = (ldap_url == null ? LDAP_URL : ldap_url);
    this.baseDN = (baseDN == null ? BASE_PEOPLE_DN : baseDN);
    this.bindDN = (bindDN == null ? BIND_DN : bindDN);
    this.searchScope = SearchControls.ONELEVEL_SCOPE;
  }

  public String getLdapURL() {
    return ldapURL;
  }

  public String getBaseDN() {
    return baseDN;
  }

  public String getBindDN() {
    return bindDN;
  }

  public void setLdapURL(String ldapURL) {
    this.ldapURL = ldapURL;
  }

  public void setBaseDN(String baseDN) {
    this.baseDN = baseDN;
  }

  public void setBindDN(String bindDN) {
    this.bindDN = bindDN;
  }

  public void setSearchScope(int searchScope) {
    this.searchScope = searchScope;
  }

  public Attribute getEmail(String uid) throws NamingException {
    return getAttributeForUID(uid, "mail");
  }

  public Attribute getUniqueIdentifier(String uid) throws NamingException {
    return getAttributeForUID(uid, "uniqueIdentifier");
  }

  public String getPreferredName(String uid) throws NamingException {
    uid = removeUserDomain(uid);
    Attribute firstName = getAttributeForUID(uid, "amgen-comPreferredName");
    Attribute lastName = getAttributeForUID(uid, "sn");
    if (firstName == null || lastName == null) {
      return null;
    }
    return firstName.get() + " " + lastName.get();
  }

  public String getLedgerPreferredName(String uid) throws NamingException {
    uid = removeUserDomain(uid);
    Attribute firstName = getAttributeForUID(uid, "amgen-comPreferredName");
    Attribute lastName = getAttributeForUID(uid, "sn");
    if (firstName == null || lastName == null) {
      return null;
    }
    return lastName.get() + ", " + firstName.get();
  }

  public String getLocation(String uid) throws NamingException {
    uid = removeUserDomain(uid);
    Attribute location = getAttributeForUID(uid, "amgen-comLocationCode");
    return location.get() + "";
  }

  public AmgenLocationCode getLocationCode(String uid) throws NamingException {
    uid = removeUserDomain(uid);
    Attribute location = getAttributeForUID(uid, "amgen-comLocationCode");
    return AmgenLocationCode.valueOf(location.get() + "");
  }

  public TimeZone getTimeZone(String uid) throws NamingException {
    return AmgenLocationCode.getLocationTimeZone(getLocation(uid));
  }

  public boolean authenticate(String uid, String password) throws NamingException {
    uid = removeUserDomain(uid);
    try {
      StringBuffer sb = new StringBuffer();
      Attribute uniqueIdentifier = getUniqueIdentifier(uid);
      new Formatter(sb).format(this.bindDN, new Object[]{uniqueIdentifier.get() + ""});
      Hashtable env = new Hashtable();
      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
      env.put(Context.PROVIDER_URL, ldapURL);
      env.put(Context.SECURITY_AUTHENTICATION, "simple");
      env.put(Context.SECURITY_PRINCIPAL, sb.toString());
      env.put(Context.SECURITY_CREDENTIALS, password);
      new InitialDirContext(env);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public Map<String, Attribute> getAttributesForUID(String uid) throws NamingException {
    List<SearchResult> searchResults = searchByUID(uid);
    Map attributeMap = new HashMap();
    for (SearchResult searchResult : searchResults) {
      Attributes resultAttributes = searchResult.getAttributes();
      NamingEnumeration ids = resultAttributes.getIDs();
      while (ids.hasMore()) {
        String id = (String) ids.next();
        attributeMap.put(id, resultAttributes.get(id));
      }
    }
    return attributeMap;
  }

  public Map<String, Attribute> getAttributesForName(String name) throws NamingException {
    List<SearchResult> searchResults = searchByCNName(name);
    Map attributeMap = new HashMap();
    for (SearchResult searchResult : searchResults) {
      Attributes resultAttributes = searchResult.getAttributes();
      NamingEnumeration ids = resultAttributes.getIDs();
      while (ids.hasMore()) {
        String id = (String) ids.next();
        attributeMap.put(id, resultAttributes.get(id));
      }
    }
    return attributeMap;
  }

  public List<String> getUIDForMgrName(String name) throws NamingException {
    List<SearchResult> searchResults = searchByMgrName(name);
    List<String> uidList = new ArrayList<String>();
    for (SearchResult searchResult : searchResults) {
      Attributes resultAttributes = searchResult.getAttributes();
      NamingEnumeration ids = resultAttributes.getIDs();
      while (ids.hasMore()) {
        String id = (String) ids.next();
        if (id.equals("uid")) {
          uidList.add(resultAttributes.get(id) + "");
        }
      }
    }
    return uidList;
  }

  public Attribute getAttributeForUID(String uid, String attributeName) throws NamingException {
    List<SearchResult> searchResults = searchByUID(uid);
    for (SearchResult searchResult : searchResults) {
      Attributes resultAttributes = searchResult.getAttributes();
      NamingEnumeration ids = resultAttributes.getIDs();
      while (ids.hasMore()) {
        String id = (String) ids.next();
        if (id.equalsIgnoreCase(attributeName)) {
          return resultAttributes.get(id.toString());
        }
      }
    }
    return null;
  }

  public Attribute getAttribute(String queryAttributeName, String queryAttributeValue, String attributeName) throws NamingException {
    Attributes matchAttrs = new BasicAttributes(true);
    matchAttrs.put(new BasicAttribute(queryAttributeName, queryAttributeValue));
    NamingEnumeration answer = getDirContext().search(baseDN, matchAttrs);
    while (answer.hasMore()) {
      SearchResult sr = (SearchResult) answer.next();
      Attributes resultAttributes = sr.getAttributes();
      NamingEnumeration ids = resultAttributes.getIDs();
      while (ids.hasMore()) {
        String id = (String) ids.next();
        if (id.equalsIgnoreCase(attributeName)) {
          return resultAttributes.get(id.toString());
        }
      }
    }
    return null;
  }

  public List<SearchResult> search(String filter) {
    List<SearchResult> results = new ArrayList<SearchResult>();
    try {
      SearchControls searchControls = new SearchControls();
      searchControls.setSearchScope(searchScope);
      NamingEnumeration answer = getDirContext().search(baseDN, filter, searchControls);
      while (answer.hasMore()) {
        SearchResult sr = (SearchResult) answer.next();
        results.add(sr);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }

  public List<SearchResult> searchByUID(String uid) {
    return search("uid=" + removeUserDomain(uid));
  }

  public List<SearchResult> searchByCNName(String name) {
    return search("cn=" + name);
  }

  public List<SearchResult> searchByMgrName(String name) {
    return search("amgen-comMgrName=" + name);
  }

  private int showAttributes(String uid) throws AccessControlException, NamingException {
    Map<String, Attribute> attributeMap = getAttributesForUID(uid);
    int answerCount = 0;
    for (Attribute atttibute : attributeMap.values()) {
      System.out.println(atttibute);
      answerCount++;
    }
    return answerCount;
  }

  private DirContext getDirContext() throws AccessControlException, NamingException {
    if (dirContext == null) {
      Hashtable env = new Hashtable();
      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
      env.put(Context.PROVIDER_URL, ldapURL);
      dirContext = new InitialDirContext(env);
    }
    return dirContext;
  }

  public int getSearchScope() {
    return searchScope;
  }

  private String removeUserDomain(String username) {
    if (username.indexOf('\\') == -1) {
      return username;
    }
    return username.substring(username.lastIndexOf('\\') + 1);
  }

  public Map<String, Attribute> getAttributes2(String query) throws NamingException {
        // Set up how we want to search - in this case the entire subtree
    // under the specified directory root.
    SearchControls ctrl = new SearchControls();
    ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);

    Map attributeMap = new HashMap();
    NamingEnumeration answer = getDirContext().search(baseDN, query, ctrl);

    int answerCount = 0;
    while (answer.hasMore()) {
      SearchResult sr = (SearchResult) answer.next();
      Attributes resultAttributes = sr.getAttributes();
      NamingEnumeration ids = resultAttributes.getIDs();
      while (ids.hasMore()) {
        String id = (String) ids.next();
        attributeMap.put(id, resultAttributes.get(id));
      }
    }
    return attributeMap;
  }

  public Map<String, Attribute> getAttributes() throws NamingException {
        // Set up how we want to search - in this case the entire subtree
    // under the specified directory root.
    SearchControls ctrl = new SearchControls();
    ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);

    Map attributeMap = new HashMap();
    Attributes attributes = getDirContext().getAttributes(baseDN);
    NamingEnumeration answer = attributes.getAll();

    int answerCount = 0;

    while (answer.hasMore()) {
      Attribute attribute = (Attribute) answer.next();
      NamingEnumeration attributes2 = attribute.getAll();
      while (attributes2.hasMore()) {
        String a = (String) attributes2.next();
        System.out.println(a);
      }
    }
    return attributeMap;
  }

  public static String getLedgerDisplayName(String uid) {
    try {
      return new AmgenLDAPAuthProvider().getLedgerPreferredName(uid);
    } catch (Exception e) {
      return uid;
    }
  }

  public static String getPreferredDisplayName(String uid) {
    try {
      return new AmgenLDAPAuthProvider().getPreferredName(uid);
    } catch (Exception e) {
      return uid;
    }
  }

  public Map<String, Attribute> getAttributes3(String baseDN, String query) throws NamingException {
        // Set up how we want to search - in this case the entire subtree
    // under the specified directory root.
    SearchControls ctrl = new SearchControls();
    ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);

    Map attributeMap = new HashMap();
    NamingEnumeration answer = getDirContext().search(baseDN, null); //, query, ctrl);

    int answerCount = 0;
    while (answer.hasMore()) {
      SearchResult sr = (SearchResult) answer.next();
      Attributes resultAttributes = sr.getAttributes();
      NamingEnumeration ids = resultAttributes.getIDs();
      while (ids.hasMore()) {
        String id = (String) ids.next();
        attributeMap.put(id, resultAttributes.get(id));
        if (id.equals("cn")) {
          System.out.println(resultAttributes.get(id));
        }
      }
    }
    return attributeMap;
  }

  public List<SearchResult> search2() {
    List<SearchResult> results = new ArrayList<SearchResult>();
    try {
      SearchControls searchControls = new SearchControls();
      searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
      NamingEnumeration answer = getDirContext().search("CN=RG Core Team,OU=Exchange Distribution Lists,OU=AM,OU=Amgen,DC=am,DC=corp,DC=amgen,DC=com",
              "objectclass=*", searchControls);
      while (answer.hasMore()) {
        SearchResult sr = (SearchResult) answer.next();
        results.add(sr);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return results;
  }

}
