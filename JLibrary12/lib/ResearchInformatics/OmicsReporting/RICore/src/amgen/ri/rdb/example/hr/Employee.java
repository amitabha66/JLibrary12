/*
 *   Employees
 *   RDBData wrapper class for COUNTRIES
 *   $Revision: 1.1 $
 *   Created: Jeffrey McDowell, 17 Nov 2006
 *   Modified: $Author: cvs $
 *   $Log
 *
 */
package amgen.ri.rdb.example.hr;

import java.lang.reflect.Field;
import java.util.Map;

import amgen.ri.rdb.OraSequenceField;
import amgen.ri.rdb.RdbData;
import amgen.ri.rdb.SQLManagerIF;
import amgen.ri.rdb.Saveable;
import amgen.ri.rdb.XMLWritable;

/**
 *   RDBData wrapper class for COUNTRIES
 *   @version $Revision: 1.1 $
 *   @author Jeffrey McDowell
 *   @author $Author: cvs $
 */
public class Employee extends RdbData implements Saveable, XMLWritable {
    protected OraSequenceField employee_id;
    protected String first_name;
    protected String last_name;
    protected String email;
    protected String phone_number;
    protected String job_id;
    protected double salary;
    protected double commission_pct;
    protected int manager_id;
    protected int department_id;

    /**
     * Default Constructor
     */
    public Employee() {
        super();
    }

    /**
     * RdbData Constructor
     */
    public Employee(String employee_id, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        this.employee_id = new OraSequenceField(employee_id);
    }

    /**
     * Create a new RdbData Constructor- example
     */
    public Employee(SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        this.employee_id = new OraSequenceField("hr_seq", this);
        first_name = "Homer";
        last_name = "Simpson";
        email = "Homer.Simpson";
        phone_number = "123-555-1234";
        job_id = "IT_PROG";
        salary = 100000;
        commission_pct = .25;
        manager_id = 100;
        department_id = 80;
    }

    /** A required method which returns the primary key(s) of the table/RdbData class. */
    public String getIdentifier() {
        return employee_id + "";
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
            "employee_id"};
    }

    public String getTableName() {
        return "employees";
    }

    /** Returns the SQL for INSERTing the object in the table */
    public String getInsertSQL() {
        return null;
    }

    /** Returns the SQL for UPDATing the object in the table */
    public String getUpdateSQL() {
        return null;
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
        return true;
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
