package amgen.ri.rdb.example.scott;

import java.lang.reflect.Field;
import java.sql.Date;

import amgen.ri.rdb.RdbData;
import amgen.ri.rdb.Removeable;
import amgen.ri.rdb.SQLManagerIF;
import amgen.ri.rdb.Saveable;

/**
 * <p> Dynamic Relational Db Mapping</p>
 * <p>Description: This a simple implementation of the AbGenRdb wrapper classes
 * Provided are class variables with protected modifiers for each column using the appropriate data type.
 * Also, there is a class variable, deptno, which is another RdbData class defining a 'join' to another table.
 * There is also a transient modifier on score which signifies a class member which is NOT in the database.
 *
 * This class implements the Saveable interface which indicates new entries wrapped by this class may be committed.
 * This class implements the Removeable interface which indicates new entries wrapped by this class may be deleted.
 *
 * </p>
 * @author Jeffrey McDowell
 * @version 1.0
 */

public class Emp extends RdbData implements Saveable, Removeable {
    protected int empno;
    protected String ename;
    protected String job;
    protected Emp mgr;
    protected Date hiredate;
    protected double sal;
    protected double comm;
    protected Dept deptno;

    /**
     * Required default constructor which calls the super() constructor to properly register the class
     */
    public Emp() {
        super();
    }

    /**
     * The required constructor for initializing a wrapper class. Provided is the primary key (which may be a CSV for
     * mulit-column primary keys), a SQLManagerIF implementation, logonusername (which is only for reference and may be null),
     * and the appropriate connection pool
     * @param empno
     * @param sqlManager
     * @param logonusername
     * @param connectionPool
     */
    public Emp(String empno, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        this.empno = Integer.parseInt(empno);
    }

    /**
     * An example of a constructor which sets all the variable in the class by calling setValues().
     * The order of the values in the Object array should be the same as the order in the class.
     * Notice primitive types (int, double, ...) should use their wrapper classes. Any variable can be set with
     * a String as long as the datatype has a valueOf() method. For example, a double may be set with a String as
     * the class, Double, has a valueOf which returns a Double object from a String.
     * @param empno
     * @param ename
     * @param job
     * @param mgr
     * @param hiredate
     * @param sal
     * @param comm
     * @param deptno
     * @param sqlManager
     * @param logonusername
     * @param connectionPool
     */
    public Emp(String empno, String ename, String job, Emp mgr, Date hiredate, double sal,
               double comm, Dept deptno, boolean testbool, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        setValues(new Object[] {
                  new Integer(empno),
                  ename,
                  job,
                  mgr,
                  hiredate,
                  new Double(sal),
                  new Double(comm),
                  deptno
        }
            );
    }

    /**
     * A required method which returns the primary key(s) of the table/RdbData class. If multi-column, use CSV format
     * @return
     */
    public String getIdentifier() {
        return empno + "";
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

    public Dept getDept() {
        return (Dept) get("deptno");
    }

}
