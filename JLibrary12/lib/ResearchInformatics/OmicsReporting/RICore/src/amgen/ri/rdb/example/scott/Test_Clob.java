package amgen.ri.rdb.example.scott;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;

import amgen.ri.rdb.ClobData;
import amgen.ri.rdb.LobSaveable;
import amgen.ri.rdb.RdbData;
import amgen.ri.rdb.SQLManagerIF;

public class Test_Clob extends RdbData implements LobSaveable {
    protected int id;
    protected ClobData data;

    /**
     * Required default constructor which calls the super() constructor to properly register the class
     */
    public Test_Clob() {
        super();
    }

    /**
     * The required constructor for initializing a wrapper class. Provided is the primary key (which may be a CSV for
     * mulit-column primary keys), a SQLManagerIF implementation, logonusername (which is only for reference and may be null),
     * and the appropriate connection pool
     * @param deptno
     * @param sqlManager
     * @param logonusername
     * @param connectionPool
     */
    public Test_Clob(String id, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        this.id = Integer.parseInt(id);
    }

    /**
     * The required constructor for initializing a wrapper class. Provided is the primary key (which may be a CSV for
     * mulit-column primary keys), a SQLManagerIF implementation, logonusername (which is only for reference and may be null),
     * and the appropriate connection pool
     * @param deptno
     * @param sqlManager
     * @param logonusername
     * @param connectionPool
     */
    public Test_Clob(String id, String data, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        this.id = Integer.parseInt(id);
        this.data = null;
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
        return data.getClobReader();
    }

    /** Returns an inputstream which will stream the Blob data */
    public InputStream getBlobStream(String fieldName) {
        return null;
    }

}
