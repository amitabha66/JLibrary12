/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package amgen.ri.ldap;

import java.util.Collection;

/**
 *
 * @author jemcdowe
 */
public interface PersonRecordIF {
  public String getEmployeeID();

  public String getUsername();

  public String getFirstName();

  public String getLastName();

  public String getDisplayName();

  public String getLedgerName();

  public String getEmail();

  public String getTitle();

  public String getOrganizationalStatus();

  public String getDepartment();

  public String getBuilding();

  public String getCostCenter();

  public String getLocation();

  public String getPhone();

  public String getObjectClass();

  public PersonRecordIF getManager();

  public Collection<PersonRecordIF> getDirectReports();

  public Collection<PersonRecordIF> getSortedDirectReports();

  public Collection<PersonRecordIF> getSortedMembers();

  public String getDistinguishedName();

  public boolean isMemberOf(String groupName);

  /**
   * Returns whether this is a group of persons rather than an individual
   *
   * @return
   */
  public boolean isGroup();

  public boolean isMember(String username);

  public boolean isMemberByDN(String dn);

  PersonRecordIF getMember(String username);

  Collection<PersonRecordIF> getMembers();

  String getAttribute(String key);

  void setAttribute(String key, String value);
}
