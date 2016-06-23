package amgen.ri.security;

import amgen.ri.ldap.AmgenEnterpriseEntry;
import amgen.ri.ldap.AmgenLocationCode;
import amgen.ri.ldap.PersonRecordIF;
import java.util.Date;
import java.util.TimeZone;

public interface IdentityIF {
	/**
	 * Returns the username for the identityIF object
	 * @return
	 */
	public String getUsername();	 
    /**
     * Returns the IdentityIF's TimeZone
     * @return
     */
    public TimeZone getUsersTimeZone();
    /**
     * Does a conversion of the given Date from the source TimeZone to the IdentityIF TimeZone
     * @param dateFormSpecifier
     * @param date
     * @param sourceTimeZone
     * @return
     */
    public String convertToUserTimeZone(String dateFormSpecifier, Date date, TimeZone sourceTimeZone);
	/**
	 * Returns the ledger style display name of the user
	 *
	 * @return String
	 */
	public String getUserLedgerDisplayName();
	/**
	 * Returns the standard/preferred style display name of the user
	 *
	 * @return String
	 */
	public String getUserPreferredDisplayName();
	/**
	 * Returns the ledger style display name of the given user
	 *
	 * @return String
	 * @param login String
	 */
	public String getUserLedgerDisplayName(String login);
	/**
	 * Returns the standard/preferred style display name of the given user
	 *
	 * @return String
	 * @param login String
	 */
	public String getUserPreferredDisplayName(String login);
	/**
	 * Returns the users Amgen location code
	 *
	 * @return AmgenLocationCode
	 */
	public AmgenLocationCode getUserAmgenLocationCode();

  /**
   * Returns the remote user as an ActiveDirectoryEntry
   *
   * @return String
   */
  public PersonRecordIF getActiveDirectoryEntry();
  
  /**
   * Returns the PersonRecord as a Record obj
   * @return 
   */
  //public JSONObject asPersonRecord();

  /**
   * Returns an AmgenEnterpriseEntry object which provides access to user
   * information in Amgen LDAP
   *
   * @return AmgenEnterpriseEntry
   */
  AmgenEnterpriseEntry getEntry();

  /**
   * Returns the created Date
   *
   * @return Date
   */
  Date getCreated();

}
