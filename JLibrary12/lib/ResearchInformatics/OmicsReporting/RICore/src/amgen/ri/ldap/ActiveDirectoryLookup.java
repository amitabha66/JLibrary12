package amgen.ri.ldap;

import amgen.ri.crypt.StringEncrypter;
import amgen.ri.security.IdentityIF;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;

/**
 * Provides LDAP access to ActiveDirectory
 *
 * @version $Id: ActiveDirectoryLookup.java,v 1.6 2014/04/09 22:56:57 jemcdowe Exp $
 */
public class ActiveDirectoryLookup {
  private Map<String, ActiveDirectoryEnv> activeDirectoryEnvs = new HashMap<String, ActiveDirectoryEnv>();
  private final String userAndGroupsSearchByFilter = "(| (& (displayName=<1>*)(mail=*) (objectClass=user) )(& (cn=<1>*) (mail=*) (objectClass=group) )(sAMAccountName=<1>))";
  private final String userSearchByFilter = "(| (& (displayName=<1>*)(mail=*) (objectClass=user) ) (sAMAccountName=<1>))";
  private final String lookupFilter = "sAMAccountName=<1>";
  private final String lookupDirectReportsFilter = "manager=<1>";

  public ActiveDirectoryLookup() {
    try {
      Properties adAccountProperties = new Properties();
      adAccountProperties.load(getClass().getResourceAsStream("/ad.serviceaccount.properties"));
      addActiveDirectory("AM", "ldap://am.corp.amgen.com:389", "OU=Amgen,DC=am,DC=corp,DC=amgen,DC=com",
              adAccountProperties.getProperty("activedirectory.serviceaccount.dn"), adAccountProperties.getProperty("http.serviceaccount.password"));
      addActiveDirectory("EU", "ldap://eu.corp.amgen.com:389", "OU=Amgen,DC=eu,DC=corp,DC=amgen,DC=com",
              adAccountProperties.getProperty("activedirectory.serviceaccount.dn"), adAccountProperties.getProperty("http.serviceaccount.password"));
      addActiveDirectory("AP", "ldap://ap.corp.amgen.com:389", "DC=ap,DC=corp,DC=amgen,DC=com",
              adAccountProperties.getProperty("activedirectory.serviceaccount.dn"), adAccountProperties.getProperty("http.serviceaccount.password"));
    } catch (IOException ex) {
      Logger.getLogger(ActiveDirectoryLookup.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  public final void addActiveDirectory(String name, String url, String baseDNs, String user, String encryptedPassword) {
    this.activeDirectoryEnvs.put(name, new ActiveDirectoryEnv(url, user, encryptedPassword, baseDNs.split(";")));
  }

  public List<ActiveDirectoryEntry> searchBy(String query, boolean includeGroups) {
    List<ActiveDirectoryEntry> entries = new ArrayList<ActiveDirectoryEntry>();
    for (String name : activeDirectoryEnvs.keySet()) {
      ActiveDirectoryEnv adEnv = activeDirectoryEnvs.get(name);
      try {
        adEnv.open();
        String filter = (includeGroups ? userAndGroupsSearchByFilter : userSearchByFilter);
        filter = filter.replaceAll("<1>", query);
        entries.addAll(adEnv.searchDirectories(filter, SearchControls.SUBTREE_SCOPE, 0, true, 1));
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        adEnv.close();
      }
    }
    return entries;
  }

  /**
   * Returns a Map of <GroupName : Boolean> of whether this PersonRecordIF is a member of the groups provided
   * in the Set as their sAMAccountName's
   *
   * @param groupAccountNames Set
   * @param member PersonRecordIF
   * @return Map
   */
  public Map<String, Boolean> isGroupMember(Set<String> groupAccountNames, PersonRecordIF member) {
    Map<String, Boolean> isGroupMemberMap = new HashMap<String, Boolean>();
    List<PersonRecordIF> groups = lookup(groupAccountNames);
    for (PersonRecordIF group : groups) {
      isGroupMemberMap.put(group.getUsername(), group.isMemberByDN(member.getDistinguishedName()));
    }
    return isGroupMemberMap;
  }

  /**
   * Returns whether the given PersonRecordIF is a member of the group which is provided as its sAMAccountName
   *
   * @param groupAccountName String
   * @param member PersonRecordIF
   * @return boolean
   */
  public boolean isGroupMember(String groupAccountName, PersonRecordIF member) {
    Set<String> groupAccountNames = new HashSet<String>();
    groupAccountNames.add(groupAccountName);
    Map<String, Boolean> groupMembershipMap = isGroupMember(groupAccountNames, member);
    return (groupMembershipMap.containsKey(groupAccountName) ? groupMembershipMap.get(groupAccountName) : false);
  }

  /**
   * Returns the PersonRecordIF for the given IdentityIF
   *
   * @param identity
   * @return
   */
  public PersonRecordIF lookup(IdentityIF identity) {
    return lookup(identity.getUsername());
  }

  /**
   * Returns the PersonRecordIF for the given username
   *
   * @param username
   * @return
   */
  public PersonRecordIF lookup(String username) {
    for (String name : activeDirectoryEnvs.keySet()) {
      ActiveDirectoryEnv adEnv = activeDirectoryEnvs.get(name);
      try {
        adEnv.open();
        String filter = lookupFilter.replaceAll("<1>", username);
        List<ActiveDirectoryEntry> entries = adEnv.searchDirectories(filter, SearchControls.SUBTREE_SCOPE, 1, false, 1);
        if (entries.size() > 0) {
          return entries.get(0);
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        adEnv.close();
      }
    }
    return null;
  }

  public List<PersonRecordIF> lookup(String[] usernames) {
    return lookup(Arrays.asList(usernames));
  }

  public List<PersonRecordIF> lookup(Collection<String> usernames) {
    List<PersonRecordIF> entries = new ArrayList<PersonRecordIF>();
    for (String name : activeDirectoryEnvs.keySet()) {
      ActiveDirectoryEnv adEnv = activeDirectoryEnvs.get(name);
      try {
        adEnv.open();
        for (String username : usernames) {
          String filter = lookupFilter.replaceAll("<1>", username.trim());
          entries.addAll(adEnv.searchDirectories(filter, SearchControls.SUBTREE_SCOPE, 1, false, 1));
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        adEnv.close();
      }
    }
    return entries;
  }

  public ActiveDirectoryEntry retrieve(String dn) {
    for (String name : activeDirectoryEnvs.keySet()) {
      ActiveDirectoryEnv adEnv = activeDirectoryEnvs.get(name);
      try {
        adEnv.open();
        SearchControls searchControls = new SearchControls(SearchControls.OBJECT_SCOPE, 0, 0, null, false, false);
        NamingEnumeration results = adEnv.search(dn, "(objectclass=*)", searchControls);
        if (results.hasMore()) {
          SearchResult sr = (SearchResult) results.next();
          Attributes attrs = sr.getAttributes();
          return new ActiveDirectoryEntry(sr.getNameInNamespace(), attrs);
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        adEnv.close();
      }
    }
    return null;
  }

  public List<ActiveDirectoryEntry> retrieve(String[] dns) {
    List<ActiveDirectoryEntry> entries = new ArrayList<ActiveDirectoryEntry>();
    for (String name : activeDirectoryEnvs.keySet()) {
      ActiveDirectoryEnv adEnv = activeDirectoryEnvs.get(name);
      try {
        adEnv.open();
        SearchControls searchControls = new SearchControls(SearchControls.OBJECT_SCOPE, 0, 0, null, false, false);
        for (String dn : dns) {
          NamingEnumeration results = adEnv.search(dn, "(objectclass=*)", searchControls);
          while (results.hasMore()) {
            SearchResult sr = (SearchResult) results.next();
            Attributes attrs = sr.getAttributes();
            entries.add(new ActiveDirectoryEntry(sr.getNameInNamespace(), attrs));
          }
          results.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        adEnv.close();
      }
    }
    return entries;
  }

  public Map<String, PersonRecordIF> getMembers(ActiveDirectoryEntry groupEntry) {
    Map<String, PersonRecordIF> members = new HashMap<String, PersonRecordIF>();
    for (String name : activeDirectoryEnvs.keySet()) {
      ActiveDirectoryEnv adEnv = activeDirectoryEnvs.get(name);
      try {
        adEnv.open();
        SearchControls searchControls = new SearchControls(SearchControls.OBJECT_SCOPE, 0, 0, null, false, false);
        for (String memberDN : groupEntry.getMemberDNs()) {
          NamingEnumeration results = adEnv.search(memberDN, "(objectclass=*)", searchControls);
          if (results.hasMore()) {
            SearchResult sr = (SearchResult) results.next();
            Attributes attrs = sr.getAttributes();
            ActiveDirectoryEntry memberEntry = new ActiveDirectoryEntry(sr.getNameInNamespace(), attrs);
            members.put(memberEntry.getUsername(), memberEntry);
          }
          results.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        adEnv.close();
      }
    }
    return members;
  }

  public Set<String> getMemberUsernamesforGroups(Set<String> groupnames) {
    Set<String> usernames = new HashSet<String>();
    try {
      List<PersonRecordIF> groups = lookup(groupnames);
      for (PersonRecordIF group : groups) {
        Map<String, PersonRecordIF> members = getMembers((ActiveDirectoryEntry) group);
        usernames.addAll(members.keySet());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return usernames;
  }

  protected Map<String, PersonRecordIF> getDirectReports(PersonRecordIF managerEntry) {
    Map<String, PersonRecordIF> reportEntryMap = new HashMap<String, PersonRecordIF>();
    for (String name : activeDirectoryEnvs.keySet()) {
      ActiveDirectoryEnv adEnv = activeDirectoryEnvs.get(name);
      try {
        adEnv.open();
        String filter = lookupDirectReportsFilter.replaceAll("<1>", managerEntry.getDistinguishedName());
        List<ActiveDirectoryEntry> entries = adEnv.searchDirectories(filter, SearchControls.SUBTREE_SCOPE, 0, false, 1);
        for (ActiveDirectoryEntry reportEntry : entries) {
          reportEntryMap.put(reportEntry.getUsername(), reportEntry);
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        adEnv.close();
      }
    }
    return reportEntryMap;
  }

}

class ActiveDirectoryEnv {

  private String url;
  private String user;
  private String password;
  private List<String> baseDN;
  private DirContext dirContext;

  public ActiveDirectoryEnv(String url, String user, String password, String... baseDN) {
    this.url = url;
    this.user = user;
    this.password = password;
    this.baseDN = new ArrayList<String>();
    if (baseDN != null) {
      this.baseDN.addAll(Arrays.asList(baseDN));
    }
  }

  /**
   * Get the value of baseDN
   *
   * @return the value of baseDN
   */
  public List<String> getBaseDNs() {
    return baseDN;
  }

  /**
   * Get the value of baseDN
   *
   * @return the value of baseDN
   */
  public String getBaseDN() {
    return (baseDN.isEmpty() ? null : baseDN.get(0));
  }

  /**
   * Set the value of baseDN
   *
   * @param baseDN new value of baseDN
   */
  public void addBaseDN(String baseDN) {
    this.baseDN.add(baseDN);
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
   * Get the value of user
   *
   * @return the value of user
   */
  public String getUser() {
    return user;
  }

  /**
   * Set the value of user
   *
   * @param user new value of user
   */
  public void setUser(String user) {
    this.user = user;
  }

  /**
   * Get the value of url
   *
   * @return the value of url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Set the value of url
   *
   * @param url new value of url
   */
  public void setUrl(String url) {
    this.url = url;
  }

  public void open() throws NamingException {
    Hashtable<String, String> adEnv = new Hashtable<String, String>();
    adEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    adEnv.put("com.sun.jndi.ldap.read.timeout", "50000");
    adEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
    adEnv.put(Context.REFERRAL, "follow");

    adEnv.put(Context.PROVIDER_URL, getUrl());
    adEnv.put(Context.SECURITY_PRINCIPAL, getUser());
    String decryptedPasswd = getPassword();
    try {
      decryptedPasswd = new StringEncrypter().decrypt(decryptedPasswd);
    } catch (Exception ex) {
    }
    adEnv.put(Context.SECURITY_CREDENTIALS, decryptedPasswd);
    dirContext = new InitialDirContext(adEnv);
  }

  public void close() {
    if (dirContext != null) {
      try {
        dirContext.close();
      } catch (Exception e) {
      }
    }
  }

  public List<ActiveDirectoryEntry> searchDirectories(String filter, int searchControl, int resultLimit, boolean searchAllDNs, int attemptCount) {
    List<ActiveDirectoryEntry> entries = new ArrayList<ActiveDirectoryEntry>();
    try {
      SearchControls searchControls = new SearchControls(searchControl, resultLimit, 500, null, false, false);
      for (String dn : baseDN) {
        NamingEnumeration results = dirContext.search(dn, filter, searchControls);
        while (results.hasMore()) {
          SearchResult sr = (SearchResult) results.next();
          Attributes attrs = sr.getAttributes();
          ActiveDirectoryEntry entry = new ActiveDirectoryEntry(sr.getNameInNamespace(), attrs);
          if (!entry.getDistinguishedName().contains("OU=Exchange Resources")) {
            entries.add(entry);
          }
        }
        results.close();
        if (!searchAllDNs && entries.size() > 0) {
          break;
        }
      }
    } catch (CommunicationException ce) {
      if (attemptCount <= 5) {
        return searchDirectories(filter, searchControl, resultLimit, searchAllDNs, attemptCount + 1);
      }
      ce.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return entries;
  }

  public NamingEnumeration search(String dn, String objectclass, SearchControls searchControls) throws NamingException {
    return dirContext.search(dn, objectclass, searchControls);
  }

}
