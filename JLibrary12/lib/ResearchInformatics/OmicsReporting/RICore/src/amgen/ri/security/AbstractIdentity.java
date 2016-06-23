package amgen.ri.security;

import amgen.ri.ldap.ActiveDirectoryLookup;
import amgen.ri.ldap.AmgenEnterpriseEntry;
import amgen.ri.ldap.AmgenEnterpriseLookup;
import amgen.ri.ldap.AmgenLocationCode;
import amgen.ri.ldap.PersonRecordIF;
import amgen.ri.util.ExtDate;
import java.io.Serializable;
import java.util.Date;
import java.util.TimeZone;

/**
 * Abstract IdentityIF object
 *
 * @author jemcdowe
 *
 */
public abstract class AbstractIdentity implements IdentityIF, Serializable {
  private String username;
  private AmgenEnterpriseEntry entry;
  private PersonRecordIF adEntry;
  private Date created;

  protected AbstractIdentity() {
    this.created= new Date();
  }

  public AbstractIdentity(String username) {
    this();
    this.username = username;
  }

  /**
   * Returns an AmgenEnterpriseEntry object which provides access to user
   * information in Amgen LDAP
   *
   * @return AmgenEnterpriseEntry
   */
  public AmgenEnterpriseEntry getEntry() {
    if (entry == null) {
      this.entry = new AmgenEnterpriseLookup().lookupByUID(this.username);
    }
    return entry;
  }

  /**
   * Returns the remote user as an ActiveDirectoryEntry
   *
   * @return String
   */
  public PersonRecordIF getActiveDirectoryEntry() {
    if (adEntry == null) {
      this.adEntry = new ActiveDirectoryLookup().lookup(username);
    }
    return adEntry;
  }
/*
  public JSONObject asPersonRecord() {
    JSONObject jPerson = new JSONObject();
    try {
      jPerson.put("username", getUsername());
      ActiveDirectoryEntry adEntry = (ActiveDirectoryEntry) getActiveDirectoryEntry();
      jPerson.put("CommonName", adEntry.getCommonName());
      jPerson.put("EmployeeID", adEntry.getEmployeeID());
      jPerson.put("FirstName", adEntry.getFirstName());
      jPerson.put("LastName", adEntry.getLastName());
      jPerson.put("DisplayName", adEntry.getDisplayName());
      jPerson.put("Department", adEntry.getDepartment());
      jPerson.put("Title", adEntry.getTitle());
      jPerson.put("CostCenter", adEntry.getCostCenter());
      jPerson.put("OrganizationalStatus", adEntry.getOrganizationalStatus());
      jPerson.put("Email", adEntry.getEmail());
      jPerson.put("Location", adEntry.getLocation());
      jPerson.put("Phone", adEntry.getPhone());
      jPerson.put("Building", adEntry.getBuilding());
      jPerson.put("ManagerDN", adEntry.getManagerDN());
    } catch (JSONException ex) {
    }
    return jPerson;
  }*/

  /**
   * Returns the users timezone based on the users site
   *
   * @return TimeZone
   */
  public TimeZone getUsersTimeZone() {
    return AmgenLocationCode.getLocationTimeZone(getUserAmgenLocationCode());
  }

  /**
   * Converts the given Date to the users TimeZone from the source TimeZone
   *
   * @param dateFormSpecifier String
   * @param date Date
   * @param sourceTimeZone TimeZone
   * @return String
   */
  public String convertToUserTimeZone(String dateFormSpecifier, Date date, TimeZone sourceTimeZone) {
    return ExtDate.getDateStringDate(dateFormSpecifier, date, sourceTimeZone, getUsersTimeZone());
  }

  /**
   * Returns the ledger style display name of the user
   *
   * @return String
   */
  public String getUserLedgerDisplayName() {
    return getActiveDirectoryEntry().getLedgerName();
  }

  /**
   * Returns the standard/preferred style display name of the user
   *
   * @return String
   */
  public String getUserPreferredDisplayName() {
    return getActiveDirectoryEntry().getLedgerName();
  }

  /**
   * Returns the ledger style display name of the given user
   *
   * @return String
   * @param login String
   */
  public String getUserLedgerDisplayName(String login) {
    if (login.equals(getUsername())) {
      return getUserLedgerDisplayName();
    }
    try {
      return new ActiveDirectoryLookup().lookup(login).getLedgerName();
    } catch (Exception e) {
    }
    return null;
  }

  /**
   * Returns the standard/preferred style display name of the given user
   *
   * @return String
   * @param login String
   */
  public String getUserPreferredDisplayName(String login) {
    if (login.equals(getUsername())) {
      return getUserPreferredDisplayName();
    }
    try {
      return new ActiveDirectoryLookup().lookup(login).getLedgerName();
    } catch (Exception e) {
    }
    return null;
  }

  /**
   * Returns the users Amgen location code
   *
   * @return AmgenLocationCode
   */
  public AmgenLocationCode getUserAmgenLocationCode() {
    return getEntry().getAmgenLocationCode();
  }

  public String getUsername() {
    return username;
  }

  protected void setUsername(String username) {
    if (username == null || username.length() == 0) {
      throw new IllegalArgumentException("Username can not be null or zero-length in an Identity object");
    }
    this.username = username;
  }

  /**
   * Returns the created Date
   *
   * @return Date
   */
  public Date getCreated() {
    return created;
  }

  /**
   * Sets the created Date
   *
   * @return Date
   */
  public void setCreated(long createdMillis) {
    this.created = new Date(createdMillis);
  }
}
