/*
 *   Person
 *   RDBData wrapper class for PERSON
 *   $Revision: 1.1 $
 *   Created: Jeffrey McDowell, 19 Dec 2007
 *   Modified: $Author: cvs $
 *   $Log
 *
 */
package amgen.ri.user;

import java.lang.reflect.Field;
import java.util.Map;

import amgen.ri.rdb.RdbData;
import amgen.ri.rdb.SQLManagerIF;
import amgen.ri.rdb.XMLWritable;

/**
 *   RDBData wrapper class for PERSON
 *   @version $Revision: 1.1 $
 *   @author Jeffrey McDowell
 *   @author $Author: cvs $
 */
public class Person extends RdbData implements XMLWritable {
    protected int person_id;
    protected String last;
    protected String first;
    protected String department;
    protected String mailstop;
    protected String lab_phone;
    protected String office_phone;
    protected String fax;
    protected int amgen_staff_id;
    protected String building;
    protected String room;
    protected String amgen_login;
    protected java.sql.Date mod_date;
    protected java.sql.Date creation_date;
    protected String familiar_name;
    protected String title;
    protected int job_status;
    protected int emp_type;
    protected int supervisor;
    protected String location;
    protected String extension;
    protected String alt_ext;
    protected String alt_loc;
    protected String full_phone;
    protected java.sql.Date actual_contract_end_date;
    protected String alphapager;
    protected int alternate_contact_wkf_id;
    protected String careerpath_desc;
    protected String careerpath_id;
    protected String company_code;
    protected String company_code_desc;
    protected String costcenter_name;
    protected String costcenter_number;
    protected String discipline_desc;
    protected String discipline_id;
    protected String dropzone;
    protected String email;
    protected String employment_status;
    protected String employment_status_desc;
    protected int evp_wkf_id;
    protected java.sql.Date expected_contract_end_date;
    protected int firstvp_wkf_id;
    protected String floor_loc;
    protected int function_code;
    protected String function_long_desc;
    protected String function_short_desc;
    protected String emp_group;
    protected String emp_group_desc;
    protected String emp_subgroup;
    protected String emp_subgroup_desc;
    protected String high_level_mgnt_center;
    protected int hrbusiness_partner_wkf_id;
    protected String is_manager_flag;
    protected String is_org_unit_lead_flag;
    protected int job_code;
    protected String job_title;
    protected java.sql.Date last_updated;
    protected int last_updated_by_wkf_id;
    protected java.sql.Date loa_end_date;
    protected java.sql.Date loa_start_date;
    protected String location_name;
    protected String mgnt_center;
    protected String mgnt_center_desc;
    protected String middlename;
    protected String mobile_phone;
    protected String mobile_phone_paging_address;
    protected java.sql.Date most_recent_hire_date;
    protected String name_prefix;
    protected String name_suffix;
    protected String number_pager;
    protected int org_unit_lead_id;
    protected String org_unit_name;
    protected int org_unit_number;
    protected java.sql.Date original_hire_date;
    protected String pager;
    protected String payroll_area;
    protected String payroll_company_id;
    protected String persnl_area_code;
    protected String persnl_area_desc;
    protected String persnl_subarea_code;
    protected String persnl_subarea_desc;
    protected int position_code;
    protected String position_desc;
    protected java.sql.Date seniority_date;
    protected String signature_auth_level;
    protected String sub_discipline_desc;
    protected String sub_discipline_id;
    protected String supervisor_chain;
    protected String supervisor_fullname;
    protected String supervisor_loginname;
    protected java.sql.Date termination_date;
    protected String voicemail;

    /**
     * Default Constructor
     */
    public Person() {
        super();
    }

    /**
     * RdbData Constructor
     */
    public Person(String person_id, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        this.person_id = Integer.parseInt(person_id);
    }

    /** A required method which returns the primary key(s) of the table/RdbData class. */
    public String getIdentifier() {
        return person_id + "";
    }

    /** This method is required EXACTLY as written to allow the RdbData architecture access to the class variables. */
    protected void setFieldValue(Field field, Object value) throws IllegalAccessException {
        field.set(this, value);
    }

    /** This method is required EXACTLY as written to allow the RdbData architecture access to the class variables. */
    protected Object getFieldValue(Field field) throws IllegalAccessException {
        return field.get(this);
    }

    /**
     * Returns the primary key fields for the RdbData class which are used to
     * generate the SQL statement(s). If this returns null (the Default), the first field of the class is assumed.
     * The getIdentifier() method must return, in CSV, the matching number of elements as this array if generated SQL is used.
     */
    public String[] getPrimaryKeyFields() {
        return new String[] {
            "person_id"};
    }

    /** Get value for person_id */
    public int getPerson_id() {
        return getAsNumber("person_id", false).intValue();
    }

    /** Get value for last */
    public String getLast() {
        return (String) get("last");
    }

    /** Get value for first */
    public String getFirst() {
        return (String) get("first");
    }

    /** Get value for department */
    public String getDepartment() {
        return (String) get("department");
    }

    /** Get value for mailstop */
    public String getMailstop() {
        return (String) get("mailstop");
    }

    /** Get value for lab_phone */
    public String getLab_phone() {
        return (String) get("lab_phone");
    }

    /** Get value for office_phone */
    public String getOffice_phone() {
        return (String) get("office_phone");
    }

    /** Get value for fax */
    public String getFax() {
        return (String) get("fax");
    }

    /** Get value for amgen_staff_id */
    public int getAmgen_staff_id() {
        return getAsNumber("amgen_staff_id").intValue();
    }

    /** Get value for building */
    public String getBuilding() {
        return (String) get("building");
    }

    /** Get value for room */
    public String getRoom() {
        return (String) get("room");
    }

    /** Get value for amgen_login */
    public String getAmgen_login() {
        return (String) get("amgen_login");
    }

    /** Get value for mod_date */
    public java.sql.Date getMod_date() {
        return (java.sql.Date) get("mod_date");
    }

    /** Get value for creation_date */
    public java.sql.Date getCreation_date() {
        return (java.sql.Date) get("creation_date");
    }

    /** Get value for familiar_name */
    public String getFamiliar_name() {
        return (String) get("familiar_name");
    }

    /** Get value for title */
    public String getTitle() {
        return (String) get("title");
    }

    /** Get value for job_status */
    public int getJob_status() {
        return getAsNumber("job_status").intValue();
    }

    /** Get value for emp_type */
    public int getEmp_type() {
        return getAsNumber("emp_type").intValue();
    }

    /** Get value for supervisor */
    public int getSupervisor() {
        return getAsNumber("supervisor").intValue();
    }

    /** Get value for location */
    public String getLocation() {
        return (String) get("location");
    }

    /** Get value for extension */
    public String getExtension() {
        return (String) get("extension");
    }

    /** Get value for alt_ext */
    public String getAlt_ext() {
        return (String) get("alt_ext");
    }

    /** Get value for alt_loc */
    public String getAlt_loc() {
        return (String) get("alt_loc");
    }

    /** Get value for full_phone */
    public String getFull_phone() {
        return (String) get("full_phone");
    }

    /** Get value for actual_contract_end_date */
    public java.sql.Date getActual_contract_end_date() {
        return (java.sql.Date) get("actual_contract_end_date");
    }

    /** Get value for alphapager */
    public String getAlphapager() {
        return (String) get("alphapager");
    }

    /** Get value for alternate_contact_wkf_id */
    public int getAlternate_contact_wkf_id() {
        return getAsNumber("alternate_contact_wkf_id").intValue();
    }

    /** Get value for careerpath_desc */
    public String getCareerpath_desc() {
        return (String) get("careerpath_desc");
    }

    /** Get value for careerpath_id */
    public String getCareerpath_id() {
        return (String) get("careerpath_id");
    }

    /** Get value for company_code */
    public String getCompany_code() {
        return (String) get("company_code");
    }

    /** Get value for company_code_desc */
    public String getCompany_code_desc() {
        return (String) get("company_code_desc");
    }

    /** Get value for costcenter_name */
    public String getCostcenter_name() {
        return (String) get("costcenter_name");
    }

    /** Get value for costcenter_number */
    public String getCostcenter_number() {
        return (String) get("costcenter_number");
    }

    /** Get value for discipline_desc */
    public String getDiscipline_desc() {
        return (String) get("discipline_desc");
    }

    /** Get value for discipline_id */
    public String getDiscipline_id() {
        return (String) get("discipline_id");
    }

    /** Get value for dropzone */
    public String getDropzone() {
        return (String) get("dropzone");
    }

    /** Get value for email */
    public String getEmail() {
        return (String) get("email");
    }

    /** Get value for employment_status */
    public String getEmployment_status() {
        return (String) get("employment_status");
    }

    /** Get value for employment_status_desc */
    public String getEmployment_status_desc() {
        return (String) get("employment_status_desc");
    }

    /** Get value for evp_wkf_id */
    public int getEvp_wkf_id() {
        return getAsNumber("evp_wkf_id").intValue();
    }

    /** Get value for expected_contract_end_date */
    public java.sql.Date getExpected_contract_end_date() {
        return (java.sql.Date) get("expected_contract_end_date");
    }

    /** Get value for firstvp_wkf_id */
    public int getFirstvp_wkf_id() {
        return getAsNumber("firstvp_wkf_id").intValue();
    }

    /** Get value for floor_loc */
    public String getFloor_loc() {
        return (String) get("floor_loc");
    }

    /** Get value for function_code */
    public int getFunction_code() {
        return getAsNumber("function_code").intValue();
    }

    /** Get value for function_long_desc */
    public String getFunction_long_desc() {
        return (String) get("function_long_desc");
    }

    /** Get value for function_short_desc */
    public String getFunction_short_desc() {
        return (String) get("function_short_desc");
    }

    /** Get value for emp_group */
    public String getEmp_group() {
        return (String) get("emp_group");
    }

    /** Get value for emp_group_desc */
    public String getEmp_group_desc() {
        return (String) get("emp_group_desc");
    }

    /** Get value for emp_subgroup */
    public String getEmp_subgroup() {
        return (String) get("emp_subgroup");
    }

    /** Get value for emp_subgroup_desc */
    public String getEmp_subgroup_desc() {
        return (String) get("emp_subgroup_desc");
    }

    /** Get value for high_level_mgnt_center */
    public String getHigh_level_mgnt_center() {
        return (String) get("high_level_mgnt_center");
    }

    /** Get value for hrbusiness_partner_wkf_id */
    public int getHrbusiness_partner_wkf_id() {
        return getAsNumber("hrbusiness_partner_wkf_id").intValue();
    }

    /** Get value for is_manager_flag */
    public String getIs_manager_flag() {
        return (String) get("is_manager_flag");
    }

    /** Get value for is_org_unit_lead_flag */
    public String getIs_org_unit_lead_flag() {
        return (String) get("is_org_unit_lead_flag");
    }

    /** Get value for job_code */
    public int getJob_code() {
        return getAsNumber("job_code").intValue();
    }

    /** Get value for job_title */
    public String getJob_title() {
        return (String) get("job_title");
    }

    /** Get value for last_updated */
    public java.sql.Date getLast_updated() {
        return (java.sql.Date) get("last_updated");
    }

    /** Get value for last_updated_by_wkf_id */
    public int getLast_updated_by_wkf_id() {
        return getAsNumber("last_updated_by_wkf_id").intValue();
    }

    /** Get value for loa_end_date */
    public java.sql.Date getLoa_end_date() {
        return (java.sql.Date) get("loa_end_date");
    }

    /** Get value for loa_start_date */
    public java.sql.Date getLoa_start_date() {
        return (java.sql.Date) get("loa_start_date");
    }

    /** Get value for location_name */
    public String getLocation_name() {
        return (String) get("location_name");
    }

    /** Get value for mgnt_center */
    public String getMgnt_center() {
        return (String) get("mgnt_center");
    }

    /** Get value for mgnt_center_desc */
    public String getMgnt_center_desc() {
        return (String) get("mgnt_center_desc");
    }

    /** Get value for middlename */
    public String getMiddlename() {
        return (String) get("middlename");
    }

    /** Get value for mobile_phone */
    public String getMobile_phone() {
        return (String) get("mobile_phone");
    }

    /** Get value for mobile_phone_paging_address */
    public String getMobile_phone_paging_address() {
        return (String) get("mobile_phone_paging_address");
    }

    /** Get value for most_recent_hire_date */
    public java.sql.Date getMost_recent_hire_date() {
        return (java.sql.Date) get("most_recent_hire_date");
    }

    /** Get value for name_prefix */
    public String getName_prefix() {
        return (String) get("name_prefix");
    }

    /** Get value for name_suffix */
    public String getName_suffix() {
        return (String) get("name_suffix");
    }

    /** Get value for number_pager */
    public String getNumber_pager() {
        return (String) get("number_pager");
    }

    /** Get value for org_unit_lead_id */
    public int getOrg_unit_lead_id() {
        return getAsNumber("org_unit_lead_id").intValue();
    }

    /** Get value for org_unit_name */
    public String getOrg_unit_name() {
        return (String) get("org_unit_name");
    }

    /** Get value for org_unit_number */
    public int getOrg_unit_number() {
        return getAsNumber("org_unit_number").intValue();
    }

    /** Get value for original_hire_date */
    public java.sql.Date getOriginal_hire_date() {
        return (java.sql.Date) get("original_hire_date");
    }

    /** Get value for pager */
    public String getPager() {
        return (String) get("pager");
    }

    /** Get value for payroll_area */
    public String getPayroll_area() {
        return (String) get("payroll_area");
    }

    /** Get value for payroll_company_id */
    public String getPayroll_company_id() {
        return (String) get("payroll_company_id");
    }

    /** Get value for persnl_area_code */
    public String getPersnl_area_code() {
        return (String) get("persnl_area_code");
    }

    /** Get value for persnl_area_desc */
    public String getPersnl_area_desc() {
        return (String) get("persnl_area_desc");
    }

    /** Get value for persnl_subarea_code */
    public String getPersnl_subarea_code() {
        return (String) get("persnl_subarea_code");
    }

    /** Get value for persnl_subarea_desc */
    public String getPersnl_subarea_desc() {
        return (String) get("persnl_subarea_desc");
    }

    /** Get value for position_code */
    public int getPosition_code() {
        return getAsNumber("position_code").intValue();
    }

    /** Get value for position_desc */
    public String getPosition_desc() {
        return (String) get("position_desc");
    }

    /** Get value for seniority_date */
    public java.sql.Date getSeniority_date() {
        return (java.sql.Date) get("seniority_date");
    }

    /** Get value for signature_auth_level */
    public String getSignature_auth_level() {
        return (String) get("signature_auth_level");
    }

    /** Get value for sub_discipline_desc */
    public String getSub_discipline_desc() {
        return (String) get("sub_discipline_desc");
    }

    /** Get value for sub_discipline_id */
    public String getSub_discipline_id() {
        return (String) get("sub_discipline_id");
    }

    /** Get value for supervisor_chain */
    public String getSupervisor_chain() {
        return (String) get("supervisor_chain");
    }

    /** Get value for supervisor_fullname */
    public String getSupervisor_fullname() {
        return (String) get("supervisor_fullname");
    }

    /** Get value for supervisor_loginname */
    public String getSupervisor_loginname() {
        return (String) get("supervisor_loginname");
    }

    /** Get value for termination_date */
    public java.sql.Date getTermination_date() {
        return (java.sql.Date) get("termination_date");
    }

    /** Get value for voicemail */
    public String getVoicemail() {
        return (String) get("voicemail");
    }

    public String getXMLVersion() {
        return null;
    }

    public String getArrayElement() {
        return null;
    }

    public String getClassElement() {
        return null;
    }

    public boolean includeFieldInXML(String fieldName) {
        if (fieldName.equals("last") || fieldName.equals("first") || fieldName.equals("department") || fieldName.equals("amgen_login")) {
            return true;
        }
        return false;
    }

    public String getFieldElement(String fieldName) {
        return null;
    }

    public Map getArrayAttributes() {
        return null;
    }

    public Map getClassAttributes() {
        return null;
    }

    public Map getFieldAttributes(String fieldName) {
        return null;
    }

}
