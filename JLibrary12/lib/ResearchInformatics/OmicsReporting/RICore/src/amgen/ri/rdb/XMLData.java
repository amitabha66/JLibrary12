package amgen.ri.rdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import oracle.sql.OPAQUE;
import oracle.xdb.XMLType;

import org.w3c.dom.Document;

/**
 *
 *  Encapsulates any data member which is an XMLType Oracle SQL type
 *
 * @author Jeffrey McDowell
 * @version 1.0
 */

public class XMLData implements DeferredField {
    private boolean dataSet;
    private RdbData parent;
    private String fieldName;
    protected XMLType data;

    /**
     * Default Constructor
     */
    public XMLData(RdbData parent, String fieldName) {
        this.parent = parent;
        this.fieldName = fieldName;
        dataSet = false;
    }

    public boolean setData() {
        if (!dataSet) {
            parent.setXMLData(this);
            dataSet = true;
        }
        return dataSet;
    }

    public void setData(String xml, SQLManagerIF sqlManager, String connectionPool) throws SQLException {
        Connection conn = sqlManager.getConnection(connectionPool);
        this.data = XMLType.createXML(conn, xml);
        this.dataSet = true;
        conn.close();
    }

    public void setData(InputStream xmlStream, SQLManagerIF sqlManager, String connectionPool) throws SQLException {
        Connection conn = sqlManager.getConnection(connectionPool);
        this.data = XMLType.createXML(conn, xmlStream);
        this.dataSet = true;
        conn.close();
    }

    public void setAllData() {
        setData();
    }

    /** Sets the XMLType field from the OPAQUE object */
    public void setData(OPAQUE opaque) throws SQLException {
        data = XMLType.createXML(opaque);
    }

    /** Returns the XML's fieldName */
    public String getFieldName() {
        return fieldName;
    }

    /** Gets the XML data as a Document */
    public Document getDocument() throws SQLException {
        setData();
        return data.getDOM();
    }

    /** Gets the XML data as an XMLType */
    public XMLType getXMLType() {
        setData();
        return data;
    }

    /**
     * Get the XMLType as a String
     * @return
     */
    public String getXMLString() {
        if (setData()) {
            try {
                return data.getStringVal();
            } catch (SQLException ex) {}
        }
        return null;
    }

    public String toString() {
        String s = getXMLString();
        return (s == null ? "" : s);
    }

    /**
     * Returns an instance of this class parsing the given String value as XML
     */
    public static XMLData valueOf(String value, String fieldName, SQLManagerIF sqlManager, String connectionPool) throws SQLException {
        if (value == null) {
            return null;
        }
        XMLData obj = new XMLData(null, fieldName);
        obj.setData(value, sqlManager, connectionPool);
        return obj;
    }

    /**
     * Returns an instance of this class parsing the given File as XML
     */
    public static XMLData valueOf(File sourceFile, String fieldName, SQLManagerIF sqlManager, String connectionPool) throws SQLException, IOException {
        if (sourceFile == null) {
            return null;
        }
        XMLData obj = new XMLData(null, fieldName);
        obj.setData(new FileInputStream(sourceFile), sqlManager, connectionPool);
        return obj;
    }

    /**
     * Returns an instance of this class parsing the given InputStream as XML
     */
    public static XMLData valueOf(InputStream sourceStream, String fieldName, SQLManagerIF sqlManager, String connectionPool) throws SQLException, IOException {
        if (sourceStream == null) {
            return null;
        }
        XMLData obj = new XMLData(null, fieldName);
        obj.setData(sourceStream, sqlManager, connectionPool);
        return obj;
    }

}
