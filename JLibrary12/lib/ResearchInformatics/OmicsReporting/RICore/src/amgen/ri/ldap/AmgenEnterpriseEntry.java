package amgen.ri.ldap;

import amgen.ri.util.HashCodeGenerator;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

/**
 * Encapsulates key attributes for an entry in Amgen's Enterprise LDAP instance
 *
 * @version $Id: AmgenEnterpriseEntry.java,v 1.4 2013/04/13 02:12:28 jemcdowe Exp $
 */
public class AmgenEnterpriseEntry implements Serializable {
  static final long serialVersionUID = -5130708628007307760L;
  private String distinguishedName;
  private String building;
  private String careerPathCode;
  private String careerPathValue;
  private String companyCode;
  private String companyID;
  private String companyValue;
  private String costCenterName;
  private String costCenterNumber;
  private String department;
  private String disciplineCode;
  private String disciplineValue;
  private String dropZone;
  private String employeeStatus;
  private String employeeStatusCode;
  private String employeeType;
  private String exemptStatus;
  private String extension;
  private String firstName;
  private String fullName;
  private String functionCode;
  private String functionName;
  private String functionShortName;
  private String gradeLevel;
  private String grpCode;
  private String grpName;
  private String hireDate;
  private String hostID;
  private String hostName;
  private String hrBusinessPartnerId;
  private String isManager;
  private String isOrgUnitLeader;
  private String lastName;
  private String ledgerName;
  private String locationCode;
  private String locationName;
  private String mailStop;
  private String managerUniqueIdentifier;
  private String mgrCtrCode;
  private String mgrCtrID;
  private String mgrCtrName;
  private String mgrCtrValue;
  private String mgrExtension;
  private String mgrName;
  private String orgUnitName;
  private String orgUnitNumber;
  private String persAreaCode;
  private String persAreaName;
  private String persSubAreaCode;
  private String persSubAreaName;
  private String positionCode;
  private String postalAddress;
  private String preferredName;
  private String prefix;
  private String roomNumber;
  private String sigAuth;
  private String subGrpCode;
  private String subGrpName;
  private String supervisorUniqueIdentifierChain;
  private String telephoneNumber;
  private String title;
  private String uid;
  private String uniqueIdentifier;
  private String updateDate;
  private String windowsLogin;
  private AmgenEnterpriseEntry manager;
  private List<AmgenEnterpriseEntry> supervisorChain;
  private List<AmgenEnterpriseEntry> directReports;
  private List<AmgenEnterpriseEntry> activeDirectReports;
  private int fHashCode;

  public AmgenEnterpriseEntry(String distinguishedName, Attributes entryAttributes) {
    this.distinguishedName = distinguishedName;
    this.building = getValue(entryAttributes, "amgen-comBuilding");
    this.careerPathCode = getValue(entryAttributes, "amgen-comCareerPathCode");
    this.careerPathValue = getValue(entryAttributes, "amgen-comCareerPathValue");
    this.companyCode = getValue(entryAttributes, "amgen-comCompanyCode");
    this.companyID = getValue(entryAttributes, "amgen-comCompanyID");
    this.companyValue = getValue(entryAttributes, "amgen-comCompanyValue");
    this.costCenterName = getValue(entryAttributes, "amgen-comCostCenterName");
    this.costCenterNumber = getValue(entryAttributes, "amgen-comCostCenterNumber");
    this.department = getValue(entryAttributes, "departmentNumber");
    this.disciplineCode = getValue(entryAttributes, "amgen-comDisciplineCode");
    this.disciplineValue = getValue(entryAttributes, "amgen-comDisciplineValue");
    this.dropZone = getValue(entryAttributes, "amgen-comDropZone");
    this.employeeStatus = getValue(entryAttributes, "amgen-comEmployeeStatus");
    this.employeeStatusCode = getValue(entryAttributes, "amgen-comEmployeeStatusCode");
    this.employeeType = getValue(entryAttributes, "employeeType");
    this.exemptStatus = getValue(entryAttributes, "amgen-comExemptStatus");
    this.extension = getValue(entryAttributes, "amgen-comExtension");
    this.firstName = getValue(entryAttributes, "givenName");
    this.fullName = getValue(entryAttributes, "amgen-comCN");
    this.functionCode = getValue(entryAttributes, "amgen-comFunctionCode");
    this.functionName = getValue(entryAttributes, "amgen-comFunctionName");
    this.functionShortName = getValue(entryAttributes, "amgen-comFunctionShortName");
    this.gradeLevel = getValue(entryAttributes, "amgen-comGradeLevel");
    this.grpCode = getValue(entryAttributes, "amgen-comGrpCode");
    this.grpName = getValue(entryAttributes, "amgen-comGrpName");
    this.hireDate = getValue(entryAttributes, "amgen-comHireDate");
    this.hostID = getValue(entryAttributes, "amgen-comHostID");
    this.hostName = getValue(entryAttributes, "amgen-comHostName");
    this.hrBusinessPartnerId = getValue(entryAttributes, "amgen-comHrBusinessPartnerId");
    this.isManager = getValue(entryAttributes, "amgen-comIsManager");
    this.isOrgUnitLeader = getValue(entryAttributes, "amgen-comIsOrgUnitLeader");
    this.lastName = getValue(entryAttributes, "sn");
    this.ledgerName = getValue(entryAttributes, "amgen-comFullName");
    this.locationCode = getValue(entryAttributes, "amgen-comLocationCode");
    this.locationName = getValue(entryAttributes, "amgen-comLocationName");
    this.mailStop = getValue(entryAttributes, "amgen-comMailStop");
    this.mgrCtrCode = getValue(entryAttributes, "amgen-comMgrCtrCode");
    this.mgrCtrID = getValue(entryAttributes, "amgen-comMgrCtrID");
    this.mgrCtrName = getValue(entryAttributes, "amgen-comMgrCtrName");
    this.mgrCtrValue = getValue(entryAttributes, "amgen-comMgrCtrValue");
    this.mgrExtension = getValue(entryAttributes, "amgen-comMgrExtension");
    this.mgrName = getValue(entryAttributes, "amgen-comMgrName");
    this.orgUnitName = getValue(entryAttributes, "amgen-comOrgUnitName");
    this.orgUnitNumber = getValue(entryAttributes, "amgen-comOrgUnitNumber");
    this.persAreaCode = getValue(entryAttributes, "amgen-comPersAreaCode");
    this.persAreaName = getValue(entryAttributes, "amgen-comPersAreaName");
    this.persSubAreaCode = getValue(entryAttributes, "amgen-comPersSubAreaCode");
    this.persSubAreaName = getValue(entryAttributes, "amgen-comPersSubAreaName");
    this.positionCode = getValue(entryAttributes, "amgen-comPositionCode");
    this.postalAddress = getValue(entryAttributes, "postalAddress");
    this.preferredName = getValue(entryAttributes, "amgen-comPreferredName");
    this.prefix = getValue(entryAttributes, "amgen-comPrefix");
    this.roomNumber = getValue(entryAttributes, "roomNumber");
    this.sigAuth = getValue(entryAttributes, "amgen-comSigAuth");
    this.subGrpCode = getValue(entryAttributes, "amgen-comSubGrpCode");
    this.subGrpName = getValue(entryAttributes, "amgen-comSubGrpName");
    this.supervisorUniqueIdentifierChain = getValue(entryAttributes, "amgen-comSupervisorChain");
    this.telephoneNumber = getValue(entryAttributes, "telephoneNumber");
    this.title = getValue(entryAttributes, "title");
    this.uid = getValue(entryAttributes, "uid");
    this.uniqueIdentifier = getValue(entryAttributes, "uniqueIdentifier");
    this.updateDate = getValue(entryAttributes, "amgen-comUpdateDate");
    this.windowsLogin = getValue(entryAttributes, "amgen-comWindowsId");
    setManagerUniqueIdentifier(getValue(entryAttributes, "manager"));
  }

  public AmgenEnterpriseEntry(String uid) {
    if (uid != null) {
      AmgenEnterpriseEntry entry = new AmgenEnterpriseLookup().lookupByUID(uid);
      if (entry != null) {
        Field[] fields = AmgenEnterpriseEntry.class.getDeclaredFields();
        for (Field field : fields) {
          if (!Modifier.isFinal(field.getModifiers()) && !field.getName().equals("fHashCode")) {
            Object value = null;
            try {
              value = field.get(entry);
              if (value != null) {
                field.set(this, value);
              }
            } catch (Exception ex) {
              System.err.println("Unable to set field " + field.getName());
              ex.printStackTrace();
            }
          }
        }
      }
    }
  }

  /*
   * Below are various getters for exposed LDAP Attributes
   */
  public String getBuilding() {
    return building;
  }

  public String getCareerPathCode() {
    return careerPathCode;
  }

  public String getCareerPathValue() {
    return careerPathValue;
  }

  public String getCompanyCode() {
    return companyCode;
  }

  public String getCompanyID() {
    return companyID;
  }

  public String getCompanyValue() {
    return companyValue;
  }

  public String getCostCenterName() {
    return costCenterName;
  }

  public String getCostCenterNumber() {
    return costCenterNumber;
  }

  public Integer getCostCenterNumberValue() {
    try {
      return new Integer(getCostCenterNumber());
    } catch (Exception e) {
      return -1;
    }
  }

  public String getDepartment() {
    return department;
  }

  public String getDisciplineCode() {
    return disciplineCode;
  }

  public String getDisciplineValue() {
    return disciplineValue;
  }

  public String getDropZone() {
    return dropZone;
  }

  public String getEmployeeStatus() {
    return employeeStatus;
  }

  public String getEmployeeStatusCode() {
    return employeeStatusCode;
  }

  public String getExemptStatus() {
    return exemptStatus;
  }

  public String getEmployeeType() {
    return employeeType;
  }

  public boolean isEmployeeOnStaff() {
    return "Staff".equalsIgnoreCase(getEmployeeType() + "");
  }

  public String getExtension() {
    return extension;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getFullName() {
    return fullName;
  }

  public String getFunctionCode() {
    return functionCode;
  }

  public String getFunctionName() {
    return functionName;
  }

  public String getFunctionShortName() {
    return functionShortName;
  }

  public String getGradeLevel() {
    return gradeLevel;
  }

  public String getGrpCode() {
    return grpCode;
  }

  public String getGrpName() {
    return grpName;
  }

  public String getHireDate() {
    return hireDate;
  }

  public String getHostID() {
    return hostID;
  }

  public String getHostName() {
    return hostName;
  }

  public String getHrBusinessPartnerId() {
    return hrBusinessPartnerId;
  }

  public String getIsManager() {
    return isManager;
  }

  public String getIsOrgUnitLeader() {
    return isOrgUnitLeader;
  }

  public String getLastName() {
    return lastName;
  }

  public String getLedgerName() {
    return ledgerName;
  }

  public String getLocationCode() {
    return locationCode;
  }

  public String getLocationName() {
    return locationName;
  }

  public String getMailStop() {
    return mailStop;
  }

  public String getManagerUniqueIdentifier() {
    return managerUniqueIdentifier;
  }

  public String getMgrCtrCode() {
    return mgrCtrCode;
  }

  public String getMgrCtrID() {
    return mgrCtrID;
  }

  public String getMgrCtrName() {
    return mgrCtrName;
  }

  public String getMgrCtrValue() {
    return mgrCtrValue;
  }

  public String getMgrExtension() {
    return mgrExtension;
  }

  public String getMgrName() {
    return mgrName;
  }

  public String getOrgUnitName() {
    return orgUnitName;
  }

  public String getOrgUnitNumber() {
    return orgUnitNumber;
  }

  public String getPersAreaCode() {
    return persAreaCode;
  }

  public String getPersAreaName() {
    return persAreaName;
  }

  public String getPersSubAreaCode() {
    return persSubAreaCode;
  }

  public String getPersSubAreaName() {
    return persSubAreaName;
  }

  public String getUid() {
    return uid;
  }

  public String getUsername() {
    return uid;
  }

  public String getTitle() {
    return title;
  }

  public String getTelephoneNumber() {
    return telephoneNumber;
  }

  public String getSupervisorUniqueIdentifierChain() {
    return supervisorUniqueIdentifierChain;
  }

  public String getSubGrpName() {
    return subGrpName;
  }

  public String getSubGrpCode() {
    return subGrpCode;
  }

  public String getSigAuth() {
    return sigAuth;
  }

  public String getRoomNumber() {
    return roomNumber;
  }

  public String getPrefix() {
    return prefix;
  }

  public String getPreferredName() {
    return preferredName;
  }

  public String getPostalAddress() {
    return postalAddress;
  }

  public String getPositionCode() {
    return positionCode;
  }

  public String getWindowsLogin() {
    return windowsLogin;
  }

  public String getUpdateDate() {
    return updateDate;
  }

  public String getUniqueIdentifier() {
    return uniqueIdentifier;
  }

  /**
   * Returns the location code as an AmgenLocationCode enum
   *
   * @return AmgenLocationCode
   */
  public AmgenLocationCode getAmgenLocationCode() {
    return AmgenLocationCode.getLocationCode(getLocationCode());
  }

  /**
   * Returns the entry's TimeZone based on AmgenLocationCode
   *
   * @return TimeZone
   */
  public TimeZone getTimeZone() {
    return AmgenLocationCode.getLocationTimeZone(getLocationCode());
  }

  /**
   * Manager as an AmgenEnterpriseEntry
   *
   * @return AmgenEnterpriseEntry
   */
  public AmgenEnterpriseEntry getManager() {
    if (managerUniqueIdentifier == null) {
      return null;
    }
    if (manager == null) {
      manager = new AmgenEnterpriseLookup().lookupByUniqueID(getManagerUniqueIdentifier());
    }

    return manager;
  }

  /**
   * The Supervisor Chain from low to high
   *
   * @return List
   */
  public List<AmgenEnterpriseEntry> getSupervisorChain() {
    if (supervisorChain == null) {
      supervisorChain = new AmgenEnterpriseLookup().lookupSupervisorChainByUniqueIDs(getSupervisorUniqueIdentifierChain());
    }
    return supervisorChain;
  }

  /**
   * The Supervisor Chain from low to high as Ledger names
   *
   * @return List
   */
  public List<String> getSupervisorNameChain() {
    List<String> chain = new ArrayList<String>();
    for (AmgenEnterpriseEntry supervisor : getSupervisorChain()) {
      chain.add(supervisor.getLedgerName());
    }
    return chain;
  }

  /**
   * All active and inactive direct reports
   *
   * @return List
   */
  public List<AmgenEnterpriseEntry> getDirectReports() {
    if (directReports == null) {
      directReports = new AmgenEnterpriseLookup().getDirectReports(this);
    }
    return directReports;
  }

  /**
   * Active direct reports
   *
   * @return List
   */
  public List<AmgenEnterpriseEntry> getActiveDirectReports() {
    if (activeDirectReports == null) {
      activeDirectReports = new AmgenEnterpriseLookup().getActiveDirectReports(this);
    }
    return activeDirectReports;
  }

  /**
   * Returns whether this entry is in the given OrgUnit provided as its number
   * by looking in the entry and each supervisor
   *
   * @param orgUnitNumber String
   * @return boolean
   */
  public boolean isInOrgUnit(String orgUnitNumber) {
    orgUnitNumber = orgUnitNumber.trim();
    if (getOrgUnitNumber().equals(orgUnitNumber)) {
      return true;
    }

    for (AmgenEnterpriseEntry supervisor : getSupervisorChain()) {
      if (supervisor.getOrgUnitNumber().equals(orgUnitNumber)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns whether this entry is in the given Cost Center provided as its number
   * by looking in the entry and each supervisor
   *
   * @param costCenterNumber int
   * @return boolean
   */
  public boolean isInCostCenter(int costCenterNumber) {
    int myCostCenter= getCostCenterNumberValue();
    if (myCostCenter== -1) {
      return false;
    }
    if (myCostCenter== costCenterNumber) {
      return true;
    }

    for (AmgenEnterpriseEntry supervisor : getSupervisorChain()) {
      int supervisorCostCenter= supervisor.getCostCenterNumberValue();
      if (supervisorCostCenter!= -1 &&  supervisorCostCenter== costCenterNumber) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns an ordered list of Org unit names
   *
   * @return
   */
  public List<String> getOrgUnitNameChain() {
    List<String> chain = new ArrayList<String>();
    chain.add(getOrgUnitName());
    for (AmgenEnterpriseEntry supervisor : getSupervisorChain()) {
      chain.add(supervisor.getOrgUnitName());
    }
    return chain;
  }

  /**
   * Returns an ordered list of Org unit numbers
   *
   * @return
   */
  public List<Integer> getOrgUnitNumberChain() {
    List<Integer> chain = new ArrayList<Integer>();
    chain.add(new Integer(getOrgUnitNumber()));
    for (AmgenEnterpriseEntry supervisor : getSupervisorChain()) {
      Integer supervisorOrgUnitNumber = new Integer(supervisor.getOrgUnitNumber());
      chain.add(supervisorOrgUnitNumber);
    }
    return chain;
  }

  /**
   * Returns an ordered list of Cost center names
   *
   * @return
   */
  public List<String> getCostCenterNameChain() {
    List<String> chain = new ArrayList<String>();
    chain.add(getCostCenterName());
    for (AmgenEnterpriseEntry supervisor : getSupervisorChain()) {
      chain.add(supervisor.getCostCenterName());
    }
    return chain;
  }

  /**
   * Returns an ordered list of Cost center numbers
   *
   * @return
   */
  public List<Integer> getCostCenterNumberChain() {
    List<Integer> chain = new ArrayList<Integer>();
    chain.add(new Integer(getCostCenterNumber()));
    for (AmgenEnterpriseEntry supervisor : getSupervisorChain()) {
      Integer supervisorCostCenterNumber = new Integer(supervisor.getCostCenterNumber());
      chain.add(supervisorCostCenterNumber);
    }
    return chain;
  }

  /**
   * Whether the employee is staff
   *
   * @return boolean
   */
  public boolean isStaff() {
    return equalsIgnoreCase(getEmployeeType(), "Staff");
  }

  /**
   * Whether the employee is active
   *
   * @return boolean
   */
  public boolean isActive() {
    return equalsIgnoreCase(getEmployeeStatus(), "Active");
  }

  /**
   * Sets the manager Unique Indentifier
   *
   * @param string String
   */
  private void setManagerUniqueIdentifier(String value) {
    if (hasLength(value)) {
      managerUniqueIdentifier = value.replaceFirst("uniqueIdentifier=", "").trim();
    }
  }

  /**
   * Retrieves a value from the Attributes assuming index=0
   *
   * @param attrs Attributes
   * @param id String
   * @return String
   */
  private String getValue(Attributes attrs, String id) {
    return getValue(attrs, id, 0);
  }

  /**
   * Returns a value from the attributes at an index
   *
   * @param attrs Attributes
   * @param id String
   * @param index int
   * @return String
   */
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
      result = HashCodeGenerator.hash(result, uid);
      fHashCode = result;
    }
    return fHashCode;
  }

  public boolean equals(Object obj) {
    if (obj instanceof AmgenEnterpriseEntry) {
      return (((AmgenEnterpriseEntry) obj).uid == this.uid);
    }
    return false;
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

  /**
   * Returns whether the 2 Strings are equivalent ignoring case checking that
   * neither is null
   *
   * @param s String
   * @return boolean
   */
  public static boolean equalsIgnoreCase(String s, String v) {
    return (s != null && v != null && s.equalsIgnoreCase(v));
  }
}
