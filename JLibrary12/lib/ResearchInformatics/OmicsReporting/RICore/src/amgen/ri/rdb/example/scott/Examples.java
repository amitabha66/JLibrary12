package amgen.ri.rdb.example.scott;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.jdom.Document;
import org.jdom.input.DOMBuilder;

import amgen.ri.oracle.OraConnectionManager;
import amgen.ri.oracle.OraSQLManager;
import amgen.ri.rdb.BlobData;
import amgen.ri.rdb.ClobData;
import amgen.ri.rdb.RdbDataArray;
import amgen.ri.rdb.SQLManagerIF;
import amgen.ri.rdb.XMLData;

/**
 * RdbData Examples
 */
public class Examples {
  public static final String POOL_NAME = "example";
  public static final String ORA_URL = "jdbc:oracle:thin:scott/tiger@localhost:1521:XE";
  public SQLManagerIF sqlManager;

  static {
    // Setup a OraConnectionManager using the hard coded URL given above setting the connection pool name to "example"
    // There are many ways to add connection pools to the system.
    // Pools are actually maintained in a singleton instance of OraConnectionManager.
    // OraConnectionManager.addCache(<pool name>, <connection URL>) is another way
    try {
      OraConnectionManager.addConnectionPool(POOL_NAME, ORA_URL);
    } catch (SQLException ex) {
    }
    OraConnectionManager oraConnectionManager = (OraConnectionManager) OraConnectionManager.getInstance();
    oraConnectionManager.logOraConnectionMgrStats(POOL_NAME);
  }

  public Examples() throws Exception {
    sqlManager = new OraSQLManager();

    /*
     ************************************************************
     * SIMPLE LOOKUPS FROM THE DATABASE USING RDBDATA OBJECTS
     * ***********************************************************
     */
// Retrieves a single employee entry and prints it in CSV to stdout
    getAnEmployeeByID("7566");
// Retrieves a single employee's manager and prints it in CSV to stdout
    getEmployeeMgr("7566");
// Retrieves a single employee's department prints is in CSV to stdout.
// The Dept object is retrieved using the RdbData method get(<variable name>) and casting to Dept
    getEmployeeDept("7566");

    /*
     ************************************************************************
     * ARRAY LOOKUPS FROM THE DATABASE USING RDBDATA & RDBADATARRAY OBJECTS
     * ***********************************************************************
     */

// Retrieves all Employees in a department and prints them
    getDeptEmployees("20");

// Retrieves all Employees which have the given job title and prints them as CSV
// This shows one use of the RdbDataArray class- retrieving a set of RdbData objects by querying a column in the
    getSalesEmployees("salesman");

// Retrieves all Employees with salaries greater than the given value and prints them as CSV
// This is an example of using the RdbDataArray class to retrieve a set of RdbData objects by a custom
// SQL query. Returned from the query must be the primary key(s) from the RdbData object
    getEmployeesWithSalary(2000);

// Retrieves all Employees with a specific salary grade
    getSalGrade("2");

//Retrieve all employee names from the table as a String array
    System.out.println("\nRetrieve all Employee names as ordered String array"
            + "\n===============================================");
    String[] enames = (String[]) getAllEmployeesField("ename");
    for (int i = 0; i < enames.length; i++) {
      System.out.println(enames[i]);
    }

    /*
     ************************************************************************
     * CREATE, UPDATE, DELETE OPERATIONS IN AN RDBDATA OBJECT
     * ***********************************************************************
     */
//Creates a new Employee
    String empNo = "1";
    String empName = "H.Simpson";
    String job = "Safety";
    int mgrEmpNo = 7698;
    Date hiredate = Date.valueOf("2002-10-18");
    double salary = 1000;
    double commission = 0;
    Dept dept = new Dept("20", sqlManager, null, POOL_NAME);
    addNewEmployee(empNo, empName, job, mgrEmpNo, hiredate, salary,
            commission, dept);
//Retrieves and prints the new employee as CSV
    Emp emp = getAnEmployeeByID(empNo);

//Updated the Employee Set a specific field
    Emp updatedEmp = setEmployeeField(getAnEmployeeByID(empNo), "ename",
            "N.Flanders");
    System.out.println(updatedEmp.getAsString());
//Deletes the new employee
    deleteEmployee(empNo);

//LOB Example
    System.out.println("\nLOB Example" + "\n===============================================");
    //Create a table containing an integer (as primary key), clob, & blob
    try {
      String createLobTable =
              "CREATE TABLE EXAMPLEIMAGES (ID INTEGER PRIMARY KEY, TITLE CLOB, IMAGE BLOB)";
      sqlManager.executeUpdate(createLobTable, POOL_NAME);
    } catch (Exception e) {
    } //just in case the table already exists and a SQLException is thrown

    //This example saves a title & an image to the database as a Clob & Blob, resp.
    //  It then will retrieve the image from the database and use a little Swing viewer
    // render it.
    String imageTitle = "Homer At Work";
    byte[] imageBytes = encode("/resources/homer.jpg");

    //Add an entry in the LOBS table with a primary key, String for the Clob, and
    // send the image bytes as an InputStream
    ExampleImage imageLobExample = new ExampleImage("SEQTEST_SEQUENCE", imageTitle,
            new ByteArrayInputStream(imageBytes),
            sqlManager, null, POOL_NAME);

    //Saves/commits the object to the database
    if (imageLobExample.performCommit() > 0) {
      System.out.println("\nCreated new LOB Example, "
              + imageLobExample.get("title"));
    } else {
      System.out.println("\nError saving LOB Example "
              + imageLobExample.getLastSQLException());
    }
    String homerID = imageLobExample.getIdentifier();

    //Create a Lob which pulls the Lob example from the database and display the image in a little viewer
    ExampleImage imageLobFromDatabase = new ExampleImage(homerID, sqlManager, null, POOL_NAME);
    imageLobFromDatabase.setAllData();
    ClobData title = (ClobData) imageLobFromDatabase.get("title");
    System.out.println(title);

    //Show the title & image in a little Swing viewer
    new ImageView(imageLobFromDatabase);

    //Delete the LOBS table
    String dropLobTable = "DROP TABLE EXAMPLEIMAGES";
    sqlManager.executeUpdate(dropLobTable, POOL_NAME);

    //OraSequenceField example
    SeqTest seqTest = createNewSeqTest("SEQTEST_SEQUENCE", "Just some data");

    //Create a new type which includes an XMLData member
    XMLTest xmlTest = createNewXMLTest("/resources/P51671.xml");
    System.out.println("Committed: " + xmlTest.performCommit());

    System.out.println("\nRetrieving XML Test Example ");
    XMLTest xmlTestRetrieval = new XMLTest("1", sqlManager, null, POOL_NAME);
    System.out.println(((XMLData) xmlTestRetrieval.get("xml_data")).getXMLString());

    DOMBuilder builder = new DOMBuilder();
    Document doc = builder.build(((XMLData) xmlTestRetrieval.get("xml_data")).getDocument());
    System.out.println(doc.getRootElement().getChildText("accession"));

    xmlTestRetrieval.performDelete();
  }

  /**
   * Here is an example of a class which uses a Sequence to set a column. The
   * class requires the Sequence name to be passed into the constructor. When
   * the class is instantiated and the data is retrieved from the database, the
   * Sequence is set by a call Sequence.nextval;
   *
   * @param sequenceName
   * @param data
   * @return
   */
  public SeqTest createNewSeqTest(String sequenceName, String data) {
    System.out.println("\nNew SeqTest object using sequence " + sequenceName
            + "\n===============================================");
    SeqTest seqtest = new SeqTest(sequenceName, data, sqlManager, null, POOL_NAME);
    System.out.println(seqtest.getAsString());
    return seqtest;
  }

  /**
   * Here is an example of a class which uses a Sequence to set a column. The
   * class requires the Sequence name to be passed into the constructor. When
   * the class is instantiated and the data is retrieved from the database, the
   * Sequence is set by a call Sequence.nextval;
   *
   * @param sequenceName
   * @param data
   * @return
   */
  public XMLTest createNewXMLTest(String xmlPath) throws SQLException, IOException {
    System.out.println("\nNew XMLTest object using xml "
            + "\n===============================================");
    InputStream in = Examples.class.getResourceAsStream(xmlPath);
    if (in == null) {
      throw new IOException("Unable to retrieve " + xmlPath);
    }
    XMLTest xmltest = new XMLTest("1", in, sqlManager, null, POOL_NAME);
    return xmltest;
  }

  /**
   * Retrieves a single employee entry and prints it in CSV to stdout
   *
   * @param empno
   */
  public Emp getAnEmployeeByID(String empno) {
    System.out.println("\nEmployee with ID:" + empno
            + "\n===============================================");
    Emp emp = new Emp(empno, sqlManager, null, POOL_NAME);
    System.out.println(emp.getAsString());
    return emp;
  }

  /**
   * Retrieves a single employee's manager prints it in CSV to stdout. The
   * manager Emp object is retrieved using the RdbData method get(<variable
   * name>) and casting to Emp
   *
   * @param empno
   */
  public Emp getEmployeeMgr(String empno) {
    System.out.println("\nEmployee " + empno
            + "'s Manager\n===============================================");
    Emp emp = new Emp(empno, sqlManager, null, POOL_NAME);
    Emp mgr = (Emp) emp.get("mgr");
    System.out.println(mgr.getAsString());
    return mgr;
  }

  /**
   * Retrieves a single employee's department prints is in CSV to stdout. The
   * Dept object is retrieved using a getter method in the Emp class
   *
   * @param empno
   */
  public Dept getEmployeeDept(String empno) {
    System.out.println("\nEmployee " + empno
            + "'s Department\n===============================================");
    Emp emp = new Emp(empno, sqlManager, null, POOL_NAME);
    Dept dept = emp.getDept();
    System.out.println(dept.getAsString());
    return dept;
  }

  /**
   * Retrieves all Employees in a department and prints them
   *
   * @param args
   */
  public Emp[] getDeptEmployees(String deptno) {
    System.out.println("\nEmployees in department " + deptno
            + "\n===============================================");
    Dept dept = new Dept(deptno, sqlManager, null, POOL_NAME);
    Emp[] emp = dept.getEmployees();
    System.out.println("Department Info: " + dept.getAsString());
    System.out.println("---------------------------");
    for (int i = 0; i < emp.length; i++) {
      System.out.println(emp[i].getAsString());
    }
    return emp;
  }

  /**
   * Retrieves all Employees which have the given job title and prints them as
   * CSV This shows one use of the RdbDataArray class- retrieving a set of
   * RdbData objects by querying a column in the RdbData class' table
   *
   * @param args
   */
  public Emp[] getSalesEmployees(String jobTitle) {
    System.out.println("\nEmployees with job title " + jobTitle
            + "\n===============================================");
    RdbDataArray empArray = new RdbDataArray(Emp.class, "job",
            jobTitle.toUpperCase(),
            sqlManager, null, POOL_NAME);
    for (int i = 0; i < empArray.size(); i++) {
      System.out.println(empArray.getItem(i).getAsString());
    }
    return (Emp[]) empArray.toArray(new Emp[0]);
  }

  /**
   * Retrieves all Employees with salaries greater than the given value and
   * prints them as CSV This is an example of using the RdbDataArray class to
   * retrieve a set of RdbData objects by a custom SQL query. Returned from the
   * query must be the primary key(s) from the RdbData object
   *
   * @param args
   */
  public Emp[] getEmployeesWithSalary(int minimumSal) {
    System.out.println("\nEmployees with salary > " + minimumSal
            + "\n===============================================");
    RdbDataArray empArray = new RdbDataArray(Emp.class,
            "SELECT EMPNO FROM EMP WHERE SAL>?", new String[]{
              minimumSal + ""
            },
            sqlManager, null, POOL_NAME);
    for (int i = 0; i < empArray.size(); i++) {
      System.out.println(empArray.getItem(i).getAsString());
    }
    return (Emp[]) empArray.toArray(new Emp[0]);
  }

  /**
   * Creates a new Emp object a commits (saves) it to the database
   *
   * @param empNo
   * @param empName
   * @param job
   * @param mgrEmpNo
   * @param hiredate
   * @param salary
   * @param commission
   * @param dept
   * @return
   */
  public Emp addNewEmployee(String empNo, String empName, String job,
          int mgrEmpNo, Date hiredate, double salary,
          double commission, Dept dept) {
    System.out.println("\nCreating new employee " + empName
            + "\n===============================================");
    Emp mgr = new Emp(mgrEmpNo + "", sqlManager, null, POOL_NAME);
    Emp newEmp = new Emp(empNo, empName, job, mgr, hiredate, salary,
            commission, dept, false, sqlManager, "", POOL_NAME);
    System.out.println(newEmp.getAsString());
    if (newEmp.performCommit() == 1) {
      System.out.println("New employee saved.");
    } else {
      System.out.println("Problem saving new employee.");
    }
    return newEmp;
  }

  /**
   * Sets a field in the Emp object to a new value and commits (saves) the
   * change to the database. The fieldName is the class variable of the field as
   * it appears in the Emp class and newValue is the updated value of the field.
   * The data type must either match (primitives use the appropriate wrapper
   * class, or if a String is provided, the data type's valueOf(<String>) is
   * used to convert.
   *
   * @param emp
   * @param fieldName
   * @param newValue
   * @return the updated Emp handle
   */
  public Emp setEmployeeField(Emp emp, String fieldName, Object newValue) {
    System.out.println("\nSetting employee " + emp.get("ename")
            + ": " + fieldName + " => " + newValue
            + " \n===============================================");
    emp.set(fieldName, newValue);
    if (emp.performCommit() > 0) {
      System.out.println("Employee, " + emp.get("ename") + ", updated.");
    } else {
      System.out.println("Problem saving employee.");
    }
    return emp;
  }

  /**
   * Use the RdbDataArray to retrieve all employees and return the given field
   * as an array. Also, this orders the array by the given field in ascending
   * order
   *
   * @param fieldName
   * @param _class
   * @return
   * @throws Exception
   */
  public Object[] getAllEmployeesField(String fieldName) throws Exception {
    RdbDataArray empArray = new RdbDataArray(Emp.class, fieldName, true,
            sqlManager, null, POOL_NAME);
    return empArray.getFieldArray(fieldName);
  }

  /**
   * Deletes an Emp from the database
   */
  public boolean deleteEmployee(String empNo) {
    System.out.println("\nDeleting employee " + empNo
            + "\n===============================================");
    Emp emp = new Emp(empNo, sqlManager, "", POOL_NAME);
    boolean ret = (emp.performDelete() > 0);
    System.out.println("Employee deleted.");
    return ret;
  }

  /**
   * Retrieves and prints in CSV all employees in the given salary grade
   *
   * @param grade
   */
  public void getSalGrade(String grade) {
    System.out.println("\nRetreiving employees in salary grade " + grade
            + "\n===============================================");
    SalGrade salGrade = new SalGrade(grade, sqlManager, null, POOL_NAME);
    System.out.println("Salary Grade " + grade + " Info: " + salGrade.getAsString());
    System.out.println("---------------------------");
    Emp[] emp = salGrade.getEmployees();
    for (int i = 0; i < emp.length; i++) {
      System.out.println(emp[i].getAsString());
    }
  }

  public static void main(String[] args) {
    try {
      new Examples();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Pulls an image resource from the ClassLoader and encode to a byte array
   * properly!! (getResourceAsStream fails for images in Jars frequently!!)
   *
   * @param imageName
   * @return
   * @throws IOException
   */
  public static byte[] encode(String imageName) throws IOException {
    URL imageURL = Examples.class.getResource(imageName);
    if (imageURL == null) {
      throw new IOException("Unable to load " + imageName);
    }
    BufferedImage image = javax.imageio.ImageIO.read(imageURL);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    javax.imageio.ImageIO.write(image, "jpg", out);
    return out.toByteArray();
  }
}

/**
 * Little Image viewer which takes the Lob example class and uses the Clob as
 * the title and the Blob as the Image
 */
class ImageView extends JFrame {
  ImageView(ExampleImage image) throws Exception {
    //Get the Clob ("title"). The toString() method of ClobData returns the Clob
    //  as a String
    super(image.get("title").toString());
    System.out.println("\nOpening LOB Example, " + image.get("title"));

    //Create an ImageIcon from the Blob ("image"). The BlobData.getData() returns
    //  the entire Blob as a byte array.
    ImageIcon icon = new ImageIcon(((BlobData) image.get("image")).getData());

    getContentPane().add(new JLabel(icon));
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        System.exit(0);
      }
    });
    pack();
    setVisible(true);
  }
}
