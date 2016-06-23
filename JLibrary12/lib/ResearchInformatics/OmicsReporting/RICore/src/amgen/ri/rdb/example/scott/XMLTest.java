package amgen.ri.rdb.example.scott;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.SQLException;

import amgen.ri.rdb.RdbData;
import amgen.ri.rdb.Removeable;
import amgen.ri.rdb.SQLManagerIF;
import amgen.ri.rdb.Saveable;
import amgen.ri.rdb.XMLData;

public class XMLTest extends RdbData implements Saveable, Removeable {
    protected int id;
    protected XMLData xml_data;

    /**
     * Default Constructor
     */
    public XMLTest() {
        super();
    }

    /**
     * RdbData Constructor
     */
    public XMLTest(String id, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        this.id = Integer.parseInt(id);
    }

    /**
     * Constructor which sets the class variables-
     * @param id id of the entry
     * @param xmlData XML as a String
     * @param sqlManager
     * @param logonusername
     * @param connectionPool
     * @throws SQLException
     */
    public XMLTest(String id, String xmlData, SQLManagerIF sqlManager, String logonusername, String connectionPool) throws SQLException {
        super(sqlManager, logonusername, connectionPool);
        this.id = Integer.parseInt(id);
        this.xml_data = XMLData.valueOf(xmlData, "xml_data", sqlManager, connectionPool);
    }

    /**
     * Constructor which sets the class variables-
     * @param id id of the entry
     * @param xmlFile XML File
     * @param sqlManager
     * @param logonusername
     * @param connectionPool
     * @throws SQLException
     */
    public XMLTest(String id, File xmlFile, SQLManagerIF sqlManager, String logonusername, String connectionPool) throws SQLException, IOException {
        super(sqlManager, logonusername, connectionPool);
        this.id = Integer.parseInt(id);
        this.xml_data = XMLData.valueOf(xmlFile, "xml_data", sqlManager, connectionPool);
    }

    /**
     * Constructor which sets the class variables-
     * @param id id of the entry
     * @param xmlStreamSource XML to be retrieved from an InputStream
     * @param sqlManager
     * @param logonusername
     * @param connectionPool
     * @throws SQLException
     */
    public XMLTest(String id, InputStream xmlStreamSource, SQLManagerIF sqlManager, String logonusername, String connectionPool) throws SQLException, IOException {
        super(sqlManager, logonusername, connectionPool);
        this.id = Integer.parseInt(id);
        this.xml_data = XMLData.valueOf(xmlStreamSource, "xml_data", sqlManager, connectionPool);
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
    public XMLData getData() {
        return (XMLData) get("xml_data");
    }
}
