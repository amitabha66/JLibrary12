package amgen.ri.ldap;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import amgen.ri.crypt.StringEncrypter;

/**
 * Provides access to the Amgen Enterprise LDAP instance returns the results as
 * AmgenEnterpriseEntry
 * objects
 * <p>@version $Id: AmgenEnterpriseLookup.java,v 1.2 2012/07/30 22:58:46 cvs Exp
 * $</p>
 */
public class AmgenEnterpriseLookup {
  public static final int SEARCH_TIMEOUT_MILLIS = 20000;
  private Hashtable activeDirectoryEnv;
  private String[] baseDN;
  private String searchByUID = "(uid=%s)";
  private String lookupByUniqueIdentifier = "(uniqueIdentifier=%s)";
  private String lookupDirectReportsFilter = "(manager=uniqueIdentifier=%s)";
  private String lookupActiveDirectReportsFilter = "(&(manager=uniqueIdentifier=%s)(amgen-comEmployeeStatus=Active))";

  /**
   * Creates an anonymous AmgenEnterpriseLookup using default url and base DN
   */
  public AmgenEnterpriseLookup() {
    this("ldap://ldap.amgen.com:389", "ou=people,dc=Enterprise,dc=amgen,dc=com", null, null);
  }

  /**
   * Creates a AmgenEnterpriseLookup using the provided url, base DNs,
   * username and password.
   *
   * @param url String
   * @param baseDNs String
   * @param user String
   * @param encryptedPassword String
   */
  public AmgenEnterpriseLookup(String url, String baseDNs, String user, String encryptedPassword) {
    this.activeDirectoryEnv = new Hashtable();
    this.baseDN = baseDNs.split(";");
    for (int i = 0; i < this.baseDN.length; i++) {
      this.baseDN[i] = this.baseDN[i].trim();
    }

    activeDirectoryEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    activeDirectoryEnv.put("com.sun.jndi.ldap.read.timeout", "60000");
    activeDirectoryEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
    activeDirectoryEnv.put(Context.REFERRAL, "follow");

    activeDirectoryEnv.put(Context.PROVIDER_URL, url);
    if (user != null) {
      activeDirectoryEnv.put(Context.SECURITY_PRINCIPAL, user);
    }
    if (encryptedPassword != null) {
      try {
        String decryptedPasswd = new StringEncrypter().decrypt(encryptedPassword);
        activeDirectoryEnv.put(Context.SECURITY_CREDENTIALS, decryptedPasswd);
      } catch (Exception ex) {
        activeDirectoryEnv.put(Context.SECURITY_CREDENTIALS, encryptedPassword);
      }
    }
  }

  /**
   * Lookup a AmgenEnterpriseEntry by the UID attribute
   *
   * @param uid String
   * @return AmgenEnterpriseEntry
   */
  public AmgenEnterpriseEntry lookupByUID(String uid) {
    List<AmgenEnterpriseEntry> entries = new ArrayList<AmgenEnterpriseEntry>();
    DirContext dirContext = null;
    try {
      dirContext = new InitialDirContext(activeDirectoryEnv);
      String filter = String.format(searchByUID, uid);
      entries.addAll(searchDirectories(dirContext, filter, SearchControls.SUBTREE_SCOPE, 0, true));
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (dirContext != null) {
        try {
          dirContext.close();
        } catch (NamingException ex) {
        }
      }
    }
    return (entries.size() > 0 ? entries.get(0) : null);
  }

  /**
   * Lookup a AmgenEnterpriseEntry by is uniqueIdentifier attribute
   *
   * @param uniqueIdentifier String
   * @return AmgenEnterpriseEntry
   */
  public AmgenEnterpriseEntry lookupByUniqueID(String uniqueIdentifier) {
    List<AmgenEnterpriseEntry> entries = new ArrayList<AmgenEnterpriseEntry>();
    DirContext dirContext = null;
    try {
      dirContext = new InitialDirContext(activeDirectoryEnv);
      String filter = String.format(lookupByUniqueIdentifier, uniqueIdentifier);
      entries.addAll(searchDirectories(dirContext, filter, SearchControls.SUBTREE_SCOPE, 0, true));
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (dirContext != null) {
        try {
          dirContext.close();
        } catch (NamingException ex) {
        }
      }
    }
    return (entries.size() > 0 ? entries.get(0) : null);
  }

  /**
   * Retrieve a AmgenEnterpriseEntry by its full DN
   *
   * @param dn String
   * @return AmgenEnterpriseEntry
   */
  public AmgenEnterpriseEntry retrieve(String dn) {
    DirContext dirContext = null;
    try {
      dirContext = new InitialDirContext(activeDirectoryEnv);
      SearchControls searchControls = new SearchControls(SearchControls.OBJECT_SCOPE, 0, SEARCH_TIMEOUT_MILLIS, null, false, false);
      NamingEnumeration results = dirContext.search(dn, "(objectclass=*)", searchControls);
      if (results.hasMore()) {
        SearchResult sr = (SearchResult) results.next();
        Attributes attrs = sr.getAttributes();
        return new AmgenEnterpriseEntry(sr.getNameInNamespace(), attrs);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (dirContext != null) {
        try {
          dirContext.close();
        } catch (NamingException ex) {
        }
      }
    }
    return null;
  }

  /**
   * Retrieve a list of AmgenEnterpriseEntry by their full DNs
   *
   * @param dns String[]
   * @return List
   */
  public List<AmgenEnterpriseEntry> retrieve(String[] dns) {
    List<AmgenEnterpriseEntry> entries = new ArrayList<AmgenEnterpriseEntry>();
    DirContext dirContext = null;
    try {
      dirContext = new InitialDirContext(activeDirectoryEnv);
      SearchControls searchControls = new SearchControls(SearchControls.OBJECT_SCOPE, 0, SEARCH_TIMEOUT_MILLIS, null, false, false);
      for (String dn : dns) {
        NamingEnumeration results = dirContext.search(dn, "(objectclass=*)", searchControls);
        while (results.hasMore()) {
          SearchResult sr = (SearchResult) results.next();
          Attributes attrs = sr.getAttributes();
          entries.add(new AmgenEnterpriseEntry(sr.getNameInNamespace(), attrs));
        }
        results.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (dirContext != null) {
        try {
          dirContext.close();
        } catch (NamingException ex) {
        }
      }
    }
    return entries;
  }

  /**
   * Returns Active & Non-Active DirectReports for the given managerEntry
   *
   * @param managerEntry AmgenEnterpriseEntry
   * @return List
   */
  protected List<AmgenEnterpriseEntry> getDirectReports(AmgenEnterpriseEntry managerEntry) {
    List<AmgenEnterpriseEntry> reports = new ArrayList<AmgenEnterpriseEntry>();
    DirContext dirContext = null;
    try {
      dirContext = new InitialDirContext(activeDirectoryEnv);
      String filter = String.format(lookupDirectReportsFilter, managerEntry.getUniqueIdentifier());
      List<AmgenEnterpriseEntry> entries = searchDirectories(dirContext, filter, SearchControls.SUBTREE_SCOPE, 0, false);
      for (AmgenEnterpriseEntry reportEntry : entries) {
        reports.add(reportEntry);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (dirContext != null) {
        try {
          dirContext.close();
        } catch (NamingException ex) {
        }
      }
    }
    return reports;
  }

  /**
   * Returns ActiveDirectReports for the given managerEntry
   *
   * @param managerEntry AmgenEnterpriseEntry
   * @return List
   */
  protected List<AmgenEnterpriseEntry> getActiveDirectReports(AmgenEnterpriseEntry managerEntry) {
    List<AmgenEnterpriseEntry> reports = new ArrayList<AmgenEnterpriseEntry>();
    DirContext dirContext = null;
    try {
      dirContext = new InitialDirContext(activeDirectoryEnv);
      String filter = String.format(lookupActiveDirectReportsFilter, managerEntry.getUniqueIdentifier());
      List<AmgenEnterpriseEntry> entries = searchDirectories(dirContext, filter, SearchControls.SUBTREE_SCOPE, 0, false);
      for (AmgenEnterpriseEntry reportEntry : entries) {
        reports.add(reportEntry);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (dirContext != null) {
        try {
          dirContext.close();
        } catch (NamingException ex) {
        }
      }
    }
    return reports;
  }

  /**
   * Lookup the supervisor chain provided as a comma-delimited list
   *
   * @param string String
   * @return List
   */
  protected List<AmgenEnterpriseEntry> lookupSupervisorChainByUniqueIDs(String supervisorIDChain) {
    List<AmgenEnterpriseEntry> supervisors = new ArrayList<AmgenEnterpriseEntry>();
    if (hasLength(supervisorIDChain)) {
      String[] supervisorIDs = supervisorIDChain.split(",");
      for (String supervisorID : supervisorIDs) {
        AmgenEnterpriseEntry supervisor = lookupByUniqueID(supervisorID);
        if (supervisor != null) {
          supervisors.add(supervisor);
        }
      }
    }
    return supervisors;
  }

  private List<AmgenEnterpriseEntry> searchDirectories(DirContext dirContext, String filter, int searchControl, int resultLimit, boolean searchAllDNs) {
    return searchDirectories(dirContext, filter, searchControl, resultLimit, searchAllDNs, 1);

  }

  private List<AmgenEnterpriseEntry> searchDirectories(DirContext dirContext, String filter, int searchControl, int resultLimit, boolean searchAllDNs, int attemptCount) {
    List<AmgenEnterpriseEntry> entries = new ArrayList<AmgenEnterpriseEntry>();
    try {
      SearchControls searchControls = new SearchControls(searchControl, resultLimit, SEARCH_TIMEOUT_MILLIS, null, false, false);
      for (String dn : baseDN) {
        NamingEnumeration results = dirContext.search(dn, filter, searchControls);
        while (results.hasMore()) {
          SearchResult sr = (SearchResult) results.next();
          Attributes attrs = sr.getAttributes();
          entries.add(new AmgenEnterpriseEntry(sr.getNameInNamespace(), attrs));
        }
        results.close();
        if (!searchAllDNs && entries.size() > 0) {
          break;
        }
      }
    } catch (CommunicationException commExp) {
      if (attemptCount <= 5) {
        return searchDirectories(dirContext, filter, searchControl, resultLimit, searchAllDNs, attemptCount + 1);
      }
      //Eat the comm exceptions
      commExp.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return entries;

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
}
