package amgen.ri.rdb.example.scott;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;

import amgen.ri.rdb.BlobData;
import amgen.ri.rdb.ClobData;
import amgen.ri.rdb.LobSaveable;
import amgen.ri.rdb.OraSequenceField;
import amgen.ri.rdb.RdbData;
import amgen.ri.rdb.SQLManagerIF;

/**
 *
 *   Example RdbData class which includes LOB columns.
 *   LOB columns are wrapped in the appropriate wrapper class, ClobData or BlobData.
 *   This class also allows the object to be committed to the database which
 *   requires implementing the LobSaveable interface-
 *   String getUpdateSQL()- Update SQL for the non-LOB columns. Returning null
 *       uses the default SQL which is usually fine.
 *   String getInsertSQL()- Insert SQL for the non-LOB columns. Returning null
 *       uses the default SQL which is usually fine.
 *   String getDeleteSQL()- Delete row SQL. Returning null uses the default
 *       SQL which is usually fine.
 *   String getSelectLobSQL(String fieldName)- SQL for selecting the LOB column
 *       indicated by the its field name (fieldName). Returning null
 *       uses the default SQL which is usually fine.
 *   Reader getClobReader(String fieldName)- Returns a Reader used to populate
 *       the CLOB. If the ClobData object is created elsewhere, such as in a
 *       constructor, simply return <ClobData>.getClobReader()
 *   InputStream getBlobStream(String fieldName)- Returns a InputStream used to populate
 *       the BLOB. If the BlobData object is created elsewhere, such as in a
 *       constructor, simply return <BlobData>.getBlobStream()
 *
 */
public class ExampleImage extends RdbData implements LobSaveable {
    protected OraSequenceField id;
    protected ClobData title;
    protected BlobData image;

    /**
     * Default Constructor
     */
    public ExampleImage() {
        super();
    }

    /**
     * RdbData Constructor
     */
    public ExampleImage(String id, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        this.id = new OraSequenceField(id);
    }

    /**
     * Constructor which sets the class variables
     */
    public ExampleImage(String sequenceName, String title, InputStream imageStream, SQLManagerIF sqlManager, String logonusername, String connectionPool) throws
        IOException {
        super(sqlManager, logonusername, connectionPool);
        setValues(new Object[] {
                  new OraSequenceField(sequenceName, this),
                  new ClobData(title),
                  new BlobData(imageStream),
        }
            );
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

    /**
     * Returns the table name of the mapping. This is needed if the table name does not
     * match the class name!! (I did this as an example)
     * @return
     */
    public String getTableName() {
        return "EXAMPLEIMAGES";
    }

    public String getUpdateSQL() {
        return null;
    }

    public String getInsertSQL() {
        return null;
    }

    public String getDeleteSQL() {
        return null;
    }

    /** Returns the SQL statement which selects for the LOB */
    public String getSelectLobSQL(String fieldName) {
        return null;
    }

    /** Returns a reader which will stream the Clob data */
    public Reader getClobReader(String fieldName) {
        return title.getClobReader();
    }

    /** Returns an inputstream which will stream the Blob data */
    public InputStream getBlobStream(String fieldName) {
        return image.getBlobStream();
    }

}
