package amgen.ri.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import amgen.ri.util.ExtArray;
import amgen.ri.util.ExtString;
import amgen.ri.util.HashCodeGenerator;
import java.io.Serializable;

/**
 * Encapsulates key attributes for an entry in ActiveDirectory
 *
 * @version $Id: ActiveDirectoryEntry.java,v 1.4 2014/04/03 23:42:43 jemcdowe Exp $
 */
public class ActiveDirectoryEntry implements PersonRecordIF, Serializable {
  static final long serialVersionUID = -452829307562651101L;
  private String distinguishedName;
  private String commonName;
  private String username;
  private String employeeID;
  private String objectClass;
  private String firstName;
  private String lastName;
  private String displayName;
  private String department;
  private String title;
  private String costCenter;
  private String organizationalStatus;
  private String email;
  private String location;
  private String phone;
  private String building;
  private String managerDN;
  private ActiveDirectoryEntry manager;
  private Set<String> memberDNs;
  private Map<String, PersonRecordIF> members;
  private Map<String, PersonRecordIF> reports;
  private Map<String, Boolean> isMemberOfGroup;
  
  private Map<String, String> attributes;
  private int fHashCode;

  public ActiveDirectoryEntry(String distinguishedName, Attributes entryAttributes) {
    setup(distinguishedName, entryAttributes);
  }

  private final void setup(String distinguishedName, Attributes entryAttributes) {
    this.distinguishedName = distinguishedName;
    commonName = getValue(entryAttributes, "cn", 0);
    username = getValue(entryAttributes, "sAMAccountName", 0);
    displayName = getValue(entryAttributes, "displayName", 0);
    employeeID = getValue(entryAttributes, "employeeID", 0);

    objectClass = getValue(entryAttributes, "objectClass", 1);
    firstName = getValue(entryAttributes, "givenName", 0);
    lastName = getValue(entryAttributes, "sn", 0);
    department = getValue(entryAttributes, "department", 0);
    title = getValue(entryAttributes, "title", 0);
    costCenter = getValue(entryAttributes, "amgen-comCostCenterName", 0);
    organizationalStatus = getValue(entryAttributes, "organizationalStatus", 0);
    email = getValue(entryAttributes, "mail", 0);

    location = getValue(entryAttributes, "l", 0);
    phone = getValue(entryAttributes, "telephoneNumber", 0);
    building = getValue(entryAttributes, "roomNumber", 0);

    managerDN = getValue(entryAttributes, "manager", 0);

    memberDNs = new LinkedHashSet<String>();
    Attribute memberAttr = entryAttributes.get("member");
    if (memberAttr != null) {
      try {
        NamingEnumeration memberEnum = memberAttr.getAll();
        while (memberEnum.hasMore()) {
          memberDNs.add((String) memberEnum.next());
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    this.isMemberOfGroup = new HashMap<String, Boolean>();
    this.attributes = new HashMap<String, String>();
  }

  /**
   * getCommonName
   *
   * @return String
   */
  public String getCommonName() {
    return commonName;
  }

  /**
   * getObjectClass
   *
   * @return String
   */
  public String getObjectClass() {
    return objectClass;
  }

  public boolean isGroup() {
    return ExtString.isEqualIgnoreCase(objectClass, "group");
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getEmail() {
    return email;
  }

  public String getDepartment() {
    return department;
  }

  public String getTitle() {
    return title;
  }

  public String getCostCenter() {
    return costCenter;
  }

  public String getOrganizationalStatus() {
    return organizationalStatus;
  }

  public String getUsername() {
    return username;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getLedgerName() {
    return getLastName()+", "+getFirstName();
  }

  public String getDistinguishedName() {
    return distinguishedName;
  }

  public Set<String> getMemberDNs() {
    return memberDNs;
  }

  public String getManagerDN() {
    return managerDN;
  }

  public ActiveDirectoryEntry getManager() {
    if (manager == null) {
      try {
        manager = new ActiveDirectoryLookup().retrieve(managerDN);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    return manager;
  }

  public String getLocation() {
    return location;
  }

  public String getPhone() {
    return phone;
  }

  public String getBuilding() {
    return building;
  }

  public String getEmployeeID() {
    return employeeID;
  }

  public void setMembersMap(Map<String, PersonRecordIF> members) {
    this.members = members;
  }

  public Map<String, PersonRecordIF> getMembersMap() {
    if (members == null) {
      try {
        members = new ActiveDirectoryLookup().getMembers(this);
      } catch (Exception ex) {
        members = new HashMap<String, PersonRecordIF>();
        ex.printStackTrace();
      }
    }
    return members;
  }

  public Map<String, PersonRecordIF> getReportsMap() {
    if (reports == null) {
      try {
        return getReportsMap(new ActiveDirectoryLookup());
      } catch (Exception ex) {
        reports = new HashMap<String, PersonRecordIF>();
        ex.printStackTrace();
      }
    }
    return reports;
  }

  public Map<String, PersonRecordIF> getReportsMap(ActiveDirectoryLookup activeDirectoryLookup) {
    if (reports == null) {
      try {
        reports = activeDirectoryLookup.getDirectReports(this);
      } catch (Exception ex) {
        reports = new HashMap<String, PersonRecordIF>();
        ex.printStackTrace();
      }
    }
    return reports;
  }

  /**
   * Returns whether this entry is a member of the group identified by its name
   *
   * @param groupName
   * @return
   */
  public boolean isMemberOf(String groupName) {
    if (!isMemberOfGroup.containsKey(groupName)) {
      isMemberOfGroup.put(groupName, new ActiveDirectoryLookup().isGroupMember(groupName, this));
    }
    return isMemberOfGroup.get(groupName);
  }

  public boolean isMember(String username) {
    return getMembersMap().containsKey(username);
  }

  public boolean isMemberByDN(String dn) {
    return memberDNs.contains(dn);
  }

  public PersonRecordIF getMember(String username) {
    return getMembersMap().get(username);
  }

  public Collection<PersonRecordIF> getMembers() {
    return getMembersMap().values();
  }

  public Collection<PersonRecordIF> getSortedMembers() {
    return ExtArray.sort(new ArrayList(getMembersMap().values()), new Comparator() {
      public int compare(Object obj1, Object obj2) {
        ActiveDirectoryEntry a1 = (ActiveDirectoryEntry) obj1;
        ActiveDirectoryEntry a2 = (ActiveDirectoryEntry) obj2;
        return a1.getDisplayName().compareTo(a2.getDisplayName());
      }
    });
  }

  public Collection<PersonRecordIF> getDirectReports() {
    return getReportsMap().values();
  }

  public Collection<PersonRecordIF> getSortedDirectReports() {
    return ExtArray.sort(new ArrayList(getReportsMap().values()), new Comparator() {
      public int compare(Object obj1, Object obj2) {
        ActiveDirectoryEntry a1 = (ActiveDirectoryEntry) obj1;
        ActiveDirectoryEntry a2 = (ActiveDirectoryEntry) obj2;
        return a1.getDisplayName().compareTo(a2.getDisplayName());
      }
    });
  }
  
  public void setAttribute(String key, String value) {
    attributes.put(key, value);
  }
  
  public String getAttribute(String key) {
    return attributes.get(key);
  }

  private String getValue(Attributes attrs, String id, int index) {
    try {
      Attribute attr = attrs.get(id);
      if (attr == null) {
        return null;
      }
      return (attr.get(index) == null ? null : attr.get(index).toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public int hashCode() {
    if (fHashCode == 0) {
      int result = HashCodeGenerator.SEED;
      result = HashCodeGenerator.hash(result, username);
      fHashCode = result;
    }
    return fHashCode;
  }

  public boolean equals(Object obj) {
    if (obj instanceof ActiveDirectoryEntry) {
      return (((ActiveDirectoryEntry) obj).username == this.username);
    }
    return false;
  }
}
