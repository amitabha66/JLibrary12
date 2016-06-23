package amgen.ri.rdb.example.scott;

import java.lang.reflect.Field;

import amgen.ri.rdb.RdbData;
import amgen.ri.rdb.SQLManagerIF;

/**
 * This class wraps the SalGrade table.
 * It shows how to implement a custom SQL query to populate an internal array. When an array is to be populated, the
 * getMemberSQL(<class variable name>) is called which generally returns the default value of null forcing the default behavior
 * of a standrad lookup by foreign key. If the lookup is based on other variables, such as the case here, this method may be overridden.
 *
 *  The example here populates the array, Emp[], with employees at this salary grade- i.e. employees with salaries between the losal and
 *  hisal values. Note this will make use of the fact that the class variables are set in the order they appear in the class. See the
 *  getMemberSQL method below
 *   @version $Revision: 1.1 $
 *   @author Jeffrey McDowell
 *   @author $Author: cvs $
 */
public class SalGrade extends RdbData {
    protected int grade;
    protected double losal;
    protected double hisal;
    protected Emp[] emp;

    /**
     * Required default constructor which calls the super() constructor to properly register the class
     */
    public SalGrade() {
        super();
    }

    /**
     * The required constructor for initializing a wrapper class. Provided is the primary key (which may be a CSV for
     * mulit-column primary keys), a SQLManagerIF implementation, logonusername (which is only for reference and may be null),
     * and the appropriate connection pool
     */
    public SalGrade(String grade, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        this.grade = Integer.parseInt(grade);
    }

    /**
     * A required method which returns the primary key(s) of the table/RdbData class. If multi-column, use CSV format
     * @return
     */
    public String getIdentifier() {
        return getGrade() + "";
    }

    public String getMemberSQL(String fieldName) {
        if (fieldName.equals("emp")) {
            return "SELECT EMPNO FROM EMP WHERE SAL BETWEEN " + losal + " AND " + hisal;
        }
        //In the event (which actually can not happen), returns the default behavior
        return super.getMemberSQL(fieldName);
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

    /** Get value for grade */
    public int getGrade() {
        return grade;
    }

    /** Get value for losal
     *  This is an example of NOT using the get(<variable name>) RdbData method. It is fine to return the variable directly,
     *  but the RdbData method, setData(), must be called to be sure the class variables are set
     */
    public double getLosal() {
        setData();
        return losal;
    }

    /** Get value for hisal.
     *  This is an example of using the getAsNumber(<variable name>) RdbData method for numeric class variables. Numeric values
     *  are returned as the appropriate Number wrapper (Double, Integer)
     */
    public Number getHisal() {
        return getAsNumber("hisal");
    }

    /**
     * Returns the Employees which are in this salary grade
     */
    public Emp[] getEmployees() {
        return (Emp[]) get("emp");
    }

}
