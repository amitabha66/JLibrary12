package amgen.ri.rdb.example.scott;

import java.lang.reflect.Field;

import amgen.ri.rdb.RdbData;
import amgen.ri.rdb.SQLManagerIF;

/**
 * <p> Dynamic Relational Db Mapping</p>
 * <p>Description: This a simple implementation of the AbGenRdb wrapper classes
 * Provided are class variables with protected modifiers for each column using the appropriate data type.
 * This example contains an array providing a one-to-many relationship.
 * @author Jeffrey McDowell
 * @version 1.0
 */


public class Dept extends RdbData {
    protected int deptno;
    protected String dname;
    protected Emp[] employees;

    /**
     * Required default constructor which calls the super() constructor to properly register the class
     */
    public Dept() {
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
    public Dept(String deptno, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        this.deptno = Integer.parseInt(deptno);
    }

    /**
     * A required method which returns the primary key(s) of the table/RdbData class. If multi-column, use CSV format
     * @return
     */
    public String getIdentifier() {
        return deptno + "";
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

    public String getDepartmentName() {
        setData();
        return dname;
    }

    /**
     * An example of a get statement. This uses the get(String) method provided by the RdbData library. If the library
     * sees the class is not yet populated, RdbData calles Oracle to populate the class prior to retrieving the value
     * of the class variable.
     */
    public Emp[] getEmployees() {
        return (Emp[]) get("employees");
    }

    /**
     * Returns the foreign key (column name) to map a member RdbData array to this
     * class. This is used to create the SQL used to populate the member array if
     * the implementation does not override getMemberSQL(). If this returns null,
     * this RdbData class' primary key is used.
     * @param fieldName
     * @return
     */
    protected String getForeignKeyForMemberArray(String fieldName) {
        return "deptno";
    }

}
