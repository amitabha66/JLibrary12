package amgen.ri.rdb.example.scott;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;

import amgen.ri.rdb.BlobData;
import amgen.ri.rdb.ClobData;
import amgen.ri.rdb.LobSaveable;
import amgen.ri.rdb.RdbData;
import amgen.ri.rdb.SQLManagerIF;

/**
 * Example RdbData class showing the use of Lobs. Lob columns in RdbData classes
 * use the ClobData and BlobData wrappers. To make an RdbData class containing
 * Lobs saveable, the LobSaveable interface must be implemented.
 *
 * This interface requires 3 methods to be implemented-
 *  String getSelectLobSQL(String fieldName) -Returns the SQL which selects
 * the clob identified by its field name (fieldName) from the table. null may be
 * returned to use the default SQL which is fine for most instances.
 *  Reader getClobReader(String fieldName) -Returns the reader used to populate a clob
 * identified by its field name (fieldName). This may be a simple as <ClobData>.getClobReader()
 * provided the ClobData object is set some other way such as a constructor.
 * InputStream getBlobStream(String fieldName) -Returns the stream used to populate a blob
 * identified by its field name (fieldName). This may be a simple as <BlobData>.getBlobStream();
 * provided the BlobData object is set some other way such as a constructor.
 *
 */

public class Lobs extends RdbData implements LobSaveable {

    protected int id;
    protected ClobData c;
    protected BlobData b;

    public Lobs() {
        super();
    }

    public Lobs(String id, SQLManagerIF sqlManager, String user, String connectionPool) {
        super(sqlManager, user, connectionPool);
        this.id = Integer.parseInt(id);
    }

    public Lobs(String id, String clobData, InputStream blobStream, SQLManagerIF sqlManager, String user, String connectionPool) throws IOException {
        super(sqlManager, user, connectionPool);
        setValues(new Object[] {id, new ClobData(clobData), new BlobData(blobStream)});
    }

    /**
     * A required method which returns the primary key(s) of the table/RdbData class. If multi-column, use CSV format
     * @return
     */
    public String getIdentifier() {
        return id + "";
    }

    /**
     * This method is required EXACTLY as written to allow the RdbData architecture access to the class variables.
     */
    protected void setFieldValue(Field field, Object value) throws IllegalAccessException {
        field.set(this, value);
    }

    /**
     * This method is required EXACTLY as written to allow the RdbData architecture access to the class variables.
     */
    protected Object getFieldValue(Field field) throws IllegalAccessException {
        return field.get(this);
    }

    /** Returns the SQL for INSERTing the object in the table */
    public String getInsertSQL() {
        return null;
    }

    /** Returns the SQL for UPDATing the object in the table */
    public String getUpdateSQL() {
        return null;
    }

    /**
     * Returns the DELETE statement for removing this entry from the database
     */
    public String getDeleteSQL() {
        return null;
    }

    /** Returns the SQL statement which selects for the LOB */
    public String getSelectLobSQL(String fieldName) {
        return null;
    }

    /** Returns a reader which will stream the Clob data */
    public Reader getClobReader(String fieldName) {
        return c.getClobReader();
    }

    /** Returns an inputstream which will stream the Blob data */
    public InputStream getBlobStream(String fieldName) {
        return b.getBlobStream();
    }

}
