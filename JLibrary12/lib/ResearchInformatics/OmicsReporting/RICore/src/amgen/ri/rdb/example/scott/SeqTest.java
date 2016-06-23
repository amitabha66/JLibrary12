/*
 *   Seqtest
 *   Example of OraSequence
 *   $Revision: 1.1 $
 *   Created: Jeff, 12 May 2004
 *   Modified: $Author: cvs $
 *   $Log
 *
 */
package amgen.ri.rdb.example.scott;

import java.lang.reflect.Field;

import amgen.ri.rdb.OraSequenceField;
import amgen.ri.rdb.RdbData;
import amgen.ri.rdb.Removeable;
import amgen.ri.rdb.SQLManagerIF;
import amgen.ri.rdb.Saveable;

/**
 *   Example of OraSequenceField
 *   The OraSequenceField is a Number subclass which calls the Sequence nextval
 *   when the object is loaded from the database
 *
 *   @version $Revision: 1.1 $
 *   @author Jeff
 *   @author $Author: cvs $
 */
public class SeqTest extends RdbData implements Saveable, Removeable {
    protected OraSequenceField id;
    protected String data;

    /**
     * Default Constructor
     */
    public SeqTest() {
        super();
    }

    /**
     * RdbData Constructor
     */
    public SeqTest(String id, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        this.id = new OraSequenceField(id);
    }

    /**
     * Constructor which sets the class variables-
     * The sequence name is provided which is used to set the class variable
     * id.
     */
    public SeqTest(String sequenceName, String data, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        id = new OraSequenceField(sequenceName, this);
        this.data = data;
    }

    /** A required method which returns the primary key(s) of the table/RdbData class. */
    public String getIdentifier() {
        return id + "";
    }

    /** This method is required EXACTLY as written to allow the RdbData architecture access to the class variables. */
    protected void setFieldValue(Field field, Object value) throws IllegalAccessException {
        field.set(this, value);
    }

    /** This method is required EXACTLY as written to allow the RdbData architecture access to the class variables. */
    protected Object getFieldValue(Field field) throws IllegalAccessException {
        return field.get(this);
    }

    /** Returns the SQL for INSERTing the object/row in the table */
    public String getInsertSQL() {
        return null;
    }

    /** Returns the SQL for UPDATing the object/row in the table */
    public String getUpdateSQL() {
        return null;
    }

    /** Returns the SQL for DELETing the object/row in the table */
    public String getDeleteSQL() {
        return null;
    }

    /** Get value for id */
    public int getId() {
        return getAsNumber("id", false).intValue();
    }

    /** Get value for data */
    public String getData() {
        return (String) get("data");
    }

}
