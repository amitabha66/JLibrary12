/*   Register
 *   Maintain datatypes used to make field assignments of registered classes
 *   $Revision: 1.1 $
 *   Created: Jeffrey McDowell, 18 Oct 2000
 *   Modified: $Author: cvs $
 *   $Log
 *
 */
package amgen.ri.rdb;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import amgen.ri.oracle.MetaData;
import amgen.ri.util.ExtString;
import amgen.ri.xml.GenericElement;
import amgen.ri.xml.XMLElement;

/**
 * This class creates a single, static register of all RdbData subclasses
 * These classes must be initially introspected for data types and registered prior to use.
 * Data types which are assigned by this class are
 *        String and String Array
 *        Integer and Integer Array
 *        Long and Long Array
 *        Double and Double Array
 *        SequenceField
 *        Hashtable
 *        RdbData type and RdbData Array
 *        Clob Data
 *        Blob Data
 *        SQLResultColumn
 *        OraSequenceField
 * Only fields which have the following attributes are registered as data fields:
 *        protected
 *        not static
 *        not final
 *        not transient
 * This class also allows default connection pools to be assigned to a class.
 *
 * This is done in 2 ways-
 * The application can set a ConnectionPoolRegister object which provides a lookup of the Class type and returns
 * the connection pool name. A ConnectionPoolRegister class must be created by implementing the ConnectionPoolRegisterIF
 * interface and registered with this class by calling the addConnectionPoolRegister() method prior to a class requiring the lookup
 * being instantiated.
 *
 * Alternatively, a more static method is to provide in the RdbData class the variable
 * public static final String DEFAULT_CONNECTION_POOL which is set the default connection pool for the class.
 *
 * The order of lookup of the connection pool is
 * RdbData constructor connection pool null?
 * Check all registered ConnectionPoolRegisters for a non-null connection pool for this RdbData class?
 * RdbData class contains DEFAULT_CONNECTION_POOL?
 *
 * This class provides a method for retreiving a handle to singleton instance:
 * Register.getRegister()
 *
 * Most RdbData subclasses get registered automatically when they are instantiated. If, however, some methods of RdbData are being
 * extended and a class description is needed before it is instantiated, it must be registered explicitly. The best way is in a
 * static block in the class extending the functionality. For example,
     <pre><blockquote>
     static {
        Register.getRegister().register(NewRdbClass.class);
     }
     </blockquote></pre>
 *
 * The data types registered by this class can be extended to allow additional data types not initialized by RdbData or
 * to initialize a field differently. To add additional data types, create a class which implements the
 * RegisterAdapterIF interface. This contains the method <pre>getFieldClassType(Field field)</pre>. This should return
 * a unique short which must be greater than 0. Add this adapter to the Register by calling
 * <pre>Register.getRegister().addAdapter(NewAdapterInstance)</pre>
 * before any classes with this data type are registered. Added adapters are check for assigning a field in the order they are
 * added until an adapter returns something other than UNKNOWN. Added adapter should return UNKNOWN
 * for any fields not assigned by it.
 * Those fields set a custom type will be populated by a custom setField method in RdbData. See RdbData
 * for more information.
 *
 * Use the getMetaData() methods to obtain MetaData information on the class.
 *
 *   @version $Revision: 1.1 $
 *   @author Jeffrey McDowell
 *   @author $Author: cvs $
 */
public class Register implements RegisterAdapterIF {
    public static final short SELECTALL = 0;
    public static final short SELECT = 1;
    public static final short UPDATE = 2;
    public static final short INSERT = 3;
    public static final short DELETE = 4;
    public static final short SELECTALLNOORDER = 5;

    public static final short UNKNOWN = -99;
    public static final short STRING = -100;
    public static final short DATE = -101;
    public static final short LONG = -102;
    public static final short INTEGER = -103;
    public static final short DOUBLE = -104;
    public static final short PRIMITIVEARRAY = -105;
    public static final short SEQUENCE = -106;
    public static final short HASHTABLE = -107;
    public static final short RDBDATA = -108;
    public static final short MEMBERARRAY = -109;
    public static final short CLOBDATA = -110;
    public static final short BLOBDATA = -111;
    public static final short SQLRESULTCOLUMN = -112;
    public static final short ORASEQUENCE = -113;
    public static final short TIMESTAMP = -114;
    public static final short TIME = -115;
    public static final short BOOLEAN = -116;
    public static final short XMLDATA = -117;
    public static final short JAVADATE = -118;

    public static final String DEFAULT_CONNECTIONPOOL = "DEFAULT_CONNECTION_POOL";

    //Table of data type for the registered RdbData classes
    //Structure: className => Field Table [fieldName => fieldType]
    private HashMap dataTypes = new HashMap();
    //Table of data names for the registered RdbData classes
    //Structure: className => Field Vector [fieldNames]
    private HashMap dataFields = new HashMap();
    //Table of data type for the registered RdbData classes for transient fields
    //Structure: className => Field Table [fieldName => fieldType]
    private HashMap transientDataTypes = new HashMap();
    //Table of data names for the registered RdbData classes for transient fields
    //Structure: className => Field Vector [fieldNames]
    private HashMap transientDataFields = new HashMap();
    //Table of connection pools. These connections pools are used to define a connection pool
    //for a particular class type when a connection pool is not defined
    private HashMap connectionPools = new HashMap();
    //Table of connection pools. These connections pools are used to define a connection pool
    //for a particular class type when a connection pool is not defined
    private ArrayList connectionPoolRegisters = new ArrayList();

    private StatementTable statementTable = new StatementTable();
    private Vector registerAdapters = new Vector();

    private static PrintWriter logWriter;

    private static Register register;

    static {
        register = new Register();
    }

    /**
     * Sets where any log messages will be written
     * @param logWriter a PrintWriter for the log
     */
    public void setLog(PrintWriter logWriter) {
        this.logWriter = logWriter;
    }

    private Register() {
        registerAdapters.add(this);
    }

    /**
     * Sets the data types for the class by introspection.
     */
    private void setClassFieldTypes(Class c) {
        if (c == null || c == RdbData.class) {
            return;
        }
        Hashtable fieldTypes = new Hashtable();
        Vector fieldNames = new Vector();
        Field[] fields = c.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            try {
                if (checkVisibilityIsProtected(field)) {
                    int type = checkRegisterAdapters(field);
                    if (type != UNKNOWN) {
                        fieldTypes.put(field.getName(), new Integer(type));
                        fieldNames.add(field.getName());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            dataTypes.put(c.getName(), fieldTypes);
            dataFields.put(c.getName(), fieldNames);
        }
    }

    /**
     * Sets the data types for the class by introspection.
     */
    private void setClassTransientFieldTypes(Class c) {
        if (c == null || c == RdbData.class) {
            return;
        }
        Hashtable fieldTypes = new Hashtable();
        Vector fieldNames = new Vector();
        Field[] fields = c.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            try {
                if (checkVisibilityIsTransientProtected(field)) {
                    int type = checkRegisterAdapters(field);
                    if (type != UNKNOWN) {
                        fieldTypes.put(field.getName(), new Integer(type));
                        fieldNames.add(field.getName());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            transientDataTypes.put(c.getName(), fieldTypes);
            transientDataFields.put(c.getName(), fieldNames);
        }
    }

    /**
     * Sets the default connection pool for the class by introspection.
     * This pool is used if the connection pool is not set by a ConnectionPoolRegisterIF implementation
     * of via the RdbData constructor.
     * This feature can be used to force a particular class to use one connection pool by
     * only having a constructor without a connectionPool field.
     * To use this feature, the class must have a public static final String class field with name
     * DEFAULT_CONNECTION_POOL.
     */
    private void setDefaultConnectionPool(Class c) {
        try {
            Field connectionPoolField = c.getField(DEFAULT_CONNECTIONPOOL);
            if (connectionPoolField.getType() == String.class &&
                Modifier.isPublic(connectionPoolField.getModifiers()) &&
                Modifier.isStatic(connectionPoolField.getModifiers()) &&
                Modifier.isFinal(connectionPoolField.getModifiers())) {
                connectionPools.put(c.getName(), connectionPoolField.get(null));
            }
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {}
    }

    /**
     * Adds a ConnectionPoolRegister object to the Register. ConnectionPoolRegisters are used to
     * lookup connection pools for objects which have their connection pool set to null
     * @param connectionPoolRegister
     */
    public void addConnectionPoolRegister(ConnectionPoolRegisterIF connectionPoolRegister) {
        addConnectionPoolRegister(connectionPoolRegister, false);
    }

    /**
     * Adds a ConnectionPoolRegister object to the Register. ConnectionPoolRegisters are used to
     * lookup connection pools for objects which have their connection pool set to null
     * @param connectionPoolRegister
     * @param replace whether this should replace a ConnectionPoolRegisterIF object which already exists. Note that the
     * lookup of previously added ConnectionPoolRegisterIF used the Object.equals() method.
     */
    public void addConnectionPoolRegister(ConnectionPoolRegisterIF connectionPoolRegister, boolean replace) {
        if (connectionPoolRegisters.contains(connectionPoolRegister)) {
            return;
        }
        connectionPoolRegisters.add(connectionPoolRegister);
    }

    /**
     * Returns the Connection Pool for the class from any registered
     * ConnectionPoolRegisterIF objects or null if none
     */
    public String getRegisteredConnectionPool(Class c) {
        for (int i = 0; i < connectionPoolRegisters.size(); i++) {
            ConnectionPoolRegisterIF connectionPoolRegister = (ConnectionPoolRegisterIF) connectionPoolRegisters.get(i);
            String connectionPool = connectionPoolRegister.getConnectionPoolForClass(c);
            if (connectionPool != null) {
                return connectionPool;
            }
        }
        return null;
    }

    /**
     * Returns the default connection pool as defined by a DEFUALT_CONNECTION_POOL
     * static member of the RdbData class
     * @param c
     * @return
     */
    public String getClassDefaultConnectionPool(Class c) {
        return (String) connectionPools.get(c.getName());
    }

    /**
     * Returns Field's class type as either DOUBLE, INTEGER, LONG, STRING, or SEQUENCE (SequenceField object)
     */
    public short getFieldClassType(Field field) {
        short type = getClassType(field.getType());
        if (type != UNKNOWN) {
            return type;
        }
        if (field.getType().isArray()) {
            switch (getClassType(field.getType().getComponentType())) {
                case (STRING):
                case (DOUBLE):
                case (LONG):
                case (INTEGER):
                    return PRIMITIVEARRAY;
                case (RDBDATA):
                    return MEMBERARRAY;
            }
        }
        return UNKNOWN;
    }

    /**
     * Returns whether the registered class contains a specific data type
     * @param _class
     * @param dataType
     * @return
     */
    public boolean containsDataType(Class _class, short dataType) {
        Iterator dataTypesKeys = dataTypes.keySet().iterator();
        Hashtable fieldTable = getFieldTable(_class.getName());
        Iterator fieldKeys = fieldTable.keySet().iterator();
        while (fieldKeys.hasNext()) {
            String fieldName = fieldKeys.next().toString();
            Integer fieldType = (Integer) fieldTable.get(fieldName);
            if (fieldType.shortValue() == dataType) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns Field's class type
     */
    public Class getFieldClass(Class _class, String fieldName) {
        Map fieldTypeTable = getFieldTable(_class.getName());
        if (fieldTypeTable == null) {
            return null;
        }
        Integer type = (Integer) fieldTypeTable.get(fieldName);

        switch (type.intValue()) {
            case (STRING):
                return String.class;
            case (DATE):
                return java.sql.Time.class;
            case (TIME):
                return java.sql.Time.class;
            case (TIMESTAMP):
                return java.sql.Timestamp.class;
            case (BOOLEAN):
                return Boolean.class;
            case (DOUBLE):
                return Double.class;
            case (LONG):
                return Long.class;
            case (INTEGER):
                return Integer.class;
            case (HASHTABLE):
                return Hashtable.class;
            case (SQLRESULTCOLUMN):
                return SQLResultColumn.class;
            case (SEQUENCE):
                return SequenceField.class;
            case (CLOBDATA):
                return ClobData.class;
            case (BLOBDATA):
                return BlobData.class;
            case (XMLDATA):
                return XMLData.class;
            case (RDBDATA):
                return RdbData.class;
            case (ORASEQUENCE):
                return OraSequenceField.class;
            case (JAVADATE):
                return JavaDate.class;
            default:
                throw new IllegalArgumentException("Unknown field type");
        }
    }

    public short getClassType(Class c) {
        if (c == String.class) {
            return STRING;
        }
        if (c == java.sql.Date.class) {
            return DATE;
        }
        if (c == java.sql.Time.class) {
            return TIME;
        }
        if (c == java.sql.Timestamp.class) {
            return TIMESTAMP;
        }
        if (c == Boolean.class || c == boolean.class) {
            return BOOLEAN;
        }
        if (c == Double.class || c == double.class ||
            c == Float.class || c == float.class) {
            return DOUBLE;
        }
        if (c == Long.class || c == long.class) {
            return LONG;
        }
        if (c == Integer.class || c == int.class ||
            c == Short.class || c == short.class) {
            return INTEGER;
        }
        if (c == Hashtable.class) {
            return HASHTABLE;
        }
        if (c == SQLResultColumn.class) {
            return SQLRESULTCOLUMN;
        }
        if (c == SequenceField.class) {
            return SEQUENCE;
        }
        if (c == ClobData.class) {
            return CLOBDATA;
        }
        if (c == BlobData.class) {
            return BLOBDATA;
        }
        if (c == XMLData.class) {
            return XMLDATA;
        }
        if (isBaseClass(c, RdbData.class)) {
            return RDBDATA;
        }
        if (isBaseClass(c, JavaDate.class)) {
            return JAVADATE;
        }
        if (isBaseClass(c, OraSequenceField.class)) {
            return ORASEQUENCE;
        }
        return UNKNOWN;
    }

    /**
     * Returns a SQL statement for the given registered RdbData class
     * @param regClass registered RdbData class for which to retrieve a SQL statement
     * @param fieldName the field on which the SQL statement pertains. If null, it returns the entire class version
     * For example, getRegisteredStatement(customer.class, "name", SELECT) returns the SQL select statement to return
     * name from the customer table. getRegisteredStatement(customer.class, null, SELECT) returns the SQL select statement
     * to populate the entire customer class
     * @param statementType the SQL statement type. Either SELECT,SELECTALL,INSERT,DELETE,UPDATE
     * SELECT- returns a statement which returns a member field by primary key
     * SELECTALL- returns a statement which returns either all primary keys in a table (if fieldName is null) or the primary keys
     *     with the fieldName as a replacement
     * INSERT- returns a statement to insert an entry into a table by primary key
     * UPDATE- returns a statement to update an entry into a table by primary key
     * DELETE- returns a statement to delete an entry into a table by primary key
     */
    public String getRegisteredStatement(Class regClass, String fieldName, String orderField, boolean ascending, short statementType) {
        String sql = statementTable.get(regClass, fieldName, orderField, ascending, statementType);
        if (statementType == SELECTALL && sql == null) {
            sql = setLookupByMemberSQL(regClass, fieldName, orderField, ascending);
        }
        //Debug.print(sql, 20);
        return sql;
    }

    /**
     * Returns a SQL statement for the given registered RdbData class
     * @param regClass registered RdbData class for which to retrieve a SQL statement
     * @param fieldName the field on which the SQL statement pertains. If null, it returns the entire class version
     * For example, getRegisteredStatement(customer.class, "name", SELECT) returns the SQL select statement to return
     * name from the customer table. getRegisteredStatement(customer.class, null, SELECT) returns the SQL select statement
     * to populate the entire customer class
     * @param statementType the SQL statement type. Either SELECT,SELECTALL,INSERT,DELETE,UPDATE
     * SELECT- returns a statement which returns a member field by primary key
     * SELECTALL- returns a statement which returns either all primary keys in a table (if fieldName is null) or the primary keys
     *     with the fieldName as a replacement
     * INSERT- returns a statement to insert an entry into a table by primary key
     * UPDATE- returns a statement to update an entry into a table by primary key
     * DELETE- returns a statement to delete an entry into a table by primary key
     */
    public String getRegisteredStatement(Class regClass, String fieldName, short statementType) {
        return getRegisteredStatement(regClass, fieldName, null, false, statementType);
    }

    /**
     * returns true if the field has protected visibility and is not static, final, or transient
     */
    private boolean checkVisibilityIsProtected(Field field) {
        if (!Modifier.isProtected(field.getModifiers()) ||
            Modifier.isStatic(field.getModifiers()) ||
            Modifier.isFinal(field.getModifiers()) ||
            Modifier.isTransient(field.getModifiers())) {
            return false;
        }
        return true;
    }

    /**
     * returns true if the field has protected visibility, is transient and is not static, or final
     */
    private boolean checkVisibilityIsTransientProtected(Field field) {
        if (!Modifier.isProtected(field.getModifiers()) ||
            Modifier.isStatic(field.getModifiers()) ||
            Modifier.isFinal(field.getModifiers())) {
            return false;
        }
        return Modifier.isTransient(field.getModifiers());
    }

    private boolean isBaseClass(Class classType, Class testBaseClass) {
        if (testBaseClass == Object.class && classType == Object.class) {
            return true;
        } else if (classType == Object.class) {
            return false;
        } else if (classType == testBaseClass) {
            return true;
        } else if (classType == null) {
            return false;
        }
        return isBaseClass(classType.getSuperclass(), testBaseClass);
    }

    /**
     * Checks all register adapters for assignment of the
     */
    protected short checkRegisterAdapters(Field field) {
        short type = UNKNOWN;
        for (int i = 0; i < registerAdapters.size(); i++) {
            RegisterAdapterIF adapter = (RegisterAdapterIF) registerAdapters.get(i);
            if ( (type = adapter.getFieldClassType(field)) != UNKNOWN) {
                break;
            }
        }
        return type;
    }

    /**
     * Returns the Register singleton instance
     * @return
     */
    public static Register getRegister() {
        return register;
    }

    /**
     * Returns the MetaData object for the given RdbData class
     * @param rdbData the RdbData class to return MetaData
     * @param computeStatistics whether to issue a ANALYZE statement to compute the statistics on the table prior to creating the TableInformation object
     * @param sqlManager a SQLManager object
     * @param logonusername the logonuser or null
     * @param connectionPool the connection pool to use
     * @return a MetaData object containing information regarding the table wrapped by the RdbData class. null if not available
     */
    public MetaData getMetaData(RdbData rdbData, boolean computeStatistics, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        return new MetaData(rdbData, computeStatistics, sqlManager, logonusername, connectionPool);
    }

    /**
     * Returns the MetaData object for the given RdbData class
     * @param className the RdbData class to return MetaData
     * @param computeStatistics whether to issue a ANALYZE statement to compute the statistics on the table prior to creating the TableInformation object
     * @param sqlManager a SQLManager object
     * @param logonusername the logonuser or null
     * @param connectionPool the connection pool to use
     * @return a MetaData object containing information regarding the table wrapped by the RdbData class. null if not available
     */
    public MetaData getMetaData(Class className, boolean computeStatistics, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        try {
            RdbData rdbData = (RdbData) className.getConstructor( (Class[])null).newInstance( (Object[])null);
            return getMetaData(rdbData, computeStatistics, sqlManager, logonusername, connectionPool);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the MetaData object for the given RdbData class
     * @param className the RdbData class to return MetaData
     * @param computeStatistics whether to issue a ANALYZE statement to compute the statistics on the table prior to creating the TableInformation object
     * @param sqlManager a SQLManager object
     * @param logonusername the logonuser or null
     * @param connectionPool the connection pool to use
     * @return a MetaData object containing information regarding the table wrapped by the RdbData class. null if not available
     */
    public MetaData getMetaData(String className, boolean computeStatistics, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        try {
            return getMetaData(Class.forName(className), computeStatistics, sqlManager, logonusername, connectionPool);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Adds an adapter to be checked when assigning class data types. These are used for
     * creating custom data types not already assignable by Register.
     * This method is checked BEFORE the field is assigned by Register.
     */
    public void addAdapter(RegisterAdapterIF adapter) {
        int position = registerAdapters.size() - 1;
        if (position < 0) {
            position = 0;
        }
        registerAdapters.add(position, adapter);
    }

    /**
     * Registers the class- set the member field types
     */
    public void register(Class c) {
        if (isRegistered(c.getName())) {
            return;
        }
        if (logWriter != null) {
            logWriter.println("Register: " + c.getName());
        }
        setClassFieldTypes(c);
        setClassTransientFieldTypes(c);
        setDefaultConnectionPool(c);
        setGeneratedStatements(c);
    }

    /**
     * Returns true if the class is registered
     */
    public boolean isRegistered(Class className) {
        return isRegistered(className.getName());
    }

    /**
     * Returns true if the class is registered
     */
    public boolean isRegistered(String className) {
        return dataTypes.containsKey(className);
    }

    /**
     * Returns the field table (fieldName => fieldType) for the registered class
     * null if the class is not registered
     */
    public Hashtable getFieldTable(String className) {
        return (Hashtable) dataTypes.get(className);
    }

    /**
     * Returns an iterator for the field names for the registered class
     * null if the class is not registered
     */
    public Iterator getFieldNames(String className) {
        Vector fieldNames = (Vector) dataFields.get(className);
        if (fieldNames == null) {
            return null;
        }
        return fieldNames.iterator();
    }

    /**
     * Returns an array of the field names for the registered class
     * null if the class is not registered
     */
    public String[] getFieldNameArray(String className) {
        Vector fieldNames = (Vector) dataFields.get(className);
        if (fieldNames == null) {
            return null;
        }
        String[] names = new String[fieldNames.size()];
        fieldNames.toArray(names);
        return names;
    }

    /**
     * Returns the transient field table (fieldName => fieldType) for the registered class
     * null if the class is not registered
     */
    public Hashtable getTransientFieldTable(String className) {
        return (Hashtable) transientDataTypes.get(className);
    }

    /**
     * Returns an iterator for the transient field names for the registered class
     * null if the class is not registered
     */
    public Iterator getTransientFieldNames(String className) {
        Vector fieldNames = (Vector) transientDataFields.get(className);
        if (fieldNames == null) {
            return null;
        }
        return fieldNames.iterator();
    }

    /**
     * Returns an array of the transient field names for the registered class
     * null if the class is not registered
     */
    public String[] getTransientFieldNameArray(String className) {
        Vector fieldNames = (Vector) transientDataFields.get(className);
        if (fieldNames == null) {
            return null;
        }
        String[] names = new String[fieldNames.size()];
        fieldNames.toArray(names);
        return names;
    }

    /**
     * Returns the number of assigned transient fields in the class
     * 0 if the class is not registered
     */
    public int getTransientFieldNameCount(String className) {
        Vector fieldNames = (Vector) transientDataFields.get(className);
        if (fieldNames == null) {
            return 0;
        }
        return fieldNames.size();
    }

    /**
     * Returns the transient field name for the registered class at the given index
     * null if the class is not registered
     */
    public String getTransientFieldName(String className, int index) {
        Vector fieldNames = (Vector) transientDataFields.get(className);
        if (fieldNames == null) {
            return null;
        }
        if (fieldNames.size() == 0) {
            return null;
        }
        return (String) fieldNames.get(index);
    }

    public XMLElement getFieldXML(String className) {
        String[] fieldNames = getFieldNameArray(className);
        Map fieldTable = getFieldTable(className);
        XMLElement fieldNamesElement = new GenericElement("FIELDNAMES");
        for (int i = 0; i < fieldNames.length; i++) {
            XMLElement fieldName = new GenericElement("FIELDNAME");
            fieldName.setData(fieldNames[i]);
            XMLElement fieldType = new GenericElement("FIELDTYPE");
            fieldType.setData(fieldTable.get(fieldNames[i]).toString());
            fieldName.addMemberElement(fieldType);
        }
        return fieldNamesElement;
    }

    /**
     * Returns the number of assigned fields in the class
     * 0 if the class is not registered
     */
    public int getFieldNameCount(String className) {
        Vector fieldNames = (Vector) dataFields.get(className);
        if (fieldNames == null) {
            return 0;
        }
        return fieldNames.size();
    }

    /**
     * Returns the field name for the registered class at the given index
     * null if the class is not registered
     */
    public String getFieldName(String className, int index) {
        Vector fieldNames = (Vector) dataFields.get(className);
        if (fieldNames == null) {
            return null;
        }
        if (fieldNames.size() == 0) {
            return null;
        }
        return (String) fieldNames.get(index);
    }

    /**
     * Prints all the registration info  for each class
     * Only for development
     */
    public void printEmAll() {
        Iterator dataTypesKeys = dataTypes.keySet().iterator();
        while (dataTypesKeys.hasNext()) {
            String className = dataTypesKeys.next().toString();
            Hashtable fieldTable = getFieldTable(className);
            Iterator fieldKeys = fieldTable.keySet().iterator();
            System.out.println(className + ": Data Types");
            while (fieldKeys.hasNext()) {
                String fieldName = fieldKeys.next().toString();
                Integer fieldType = (Integer) fieldTable.get(fieldName);
                System.out.println("\t" + fieldName + " => " + fieldType);
            }
        }

        dataTypesKeys = dataFields.keySet().iterator();
        while (dataTypesKeys.hasNext()) {
            String className = dataTypesKeys.next().toString();
            System.out.println(className + " Data Fields");
            for (int i = 0; i < getFieldNameCount(className); i++) {
                String fieldName = getFieldName(className, i);
                System.out.println("\t" + i + " => " + fieldName);
            }
        }

        statementTable.printAll();
    }

    /**
     * Called by register to enter the SQL statements into the StatementTable
     */
    private void setGeneratedStatements(Class regRdbClass) {
        if (regRdbClass == null || regRdbClass == RdbData.class) {
            return;
        }
        try {
            //System.out.println("Generating statements for "+regRdbClass.getName());
            RdbData regRdbObj = (RdbData) regRdbClass.getConstructor( (Class[])null).newInstance( (Object[])null);
            StringBuffer selectSQL = new StringBuffer("SELECT ");
            String selectLobField = null;
            StringBuffer sqlInsertFieldList = new StringBuffer();
            StringBuffer sqlInsertReplacementList = new StringBuffer();
            StringBuffer sqlUpdateFieldList = new StringBuffer();

            Iterator iterator = getFieldNames(regRdbClass.getName());
            Hashtable fieldTable = getFieldTable(regRdbClass.getName());
            if (iterator == null) {
                return;
            }
            StringBuffer primaryKeyClause = new StringBuffer();
            String primaryKeyList = null;
            String[] primaryKeys = regRdbObj.getPrimaryKeyFields();
            if (primaryKeys == null) {
                String primaryField = getFieldName(regRdbClass.getName(), 0);
                if (primaryField != null) {
                    primaryKeys = new String[] {
                        primaryField};
                }
            }
            if (primaryKeys != null) {
                for (int i = 0; i < primaryKeys.length; i++) {
                    if (primaryKeyClause.length() > 0) {
                        primaryKeyClause.append(" AND ");
                    }
                    primaryKeyClause.append(primaryKeys[i] + "=?");
                }
                primaryKeyList = ExtString.join(primaryKeys, ',');
            }
            if (primaryKeyClause.length() <= 0) {
                return;
            }

            while (iterator.hasNext()) {
                String fieldName = iterator.next().toString();
                Integer fieldType = (Integer) fieldTable.get(fieldName);
                switch (fieldType.intValue()) {
                    case (Register.STRING):
                    case (Register.DATE):
                    case (Register.TIME):
                    case (Register.TIMESTAMP):
                    case (Register.BOOLEAN):
                    case (Register.LONG):
                    case (Register.INTEGER):
                    case (Register.DOUBLE):
                    case (Register.SEQUENCE):
                    case (Register.ORASEQUENCE):
                    case (Register.JAVADATE):
                    case (Register.RDBDATA):
                        if (sqlInsertFieldList.length() > 0) {
                            selectSQL.append(",");
                            sqlInsertFieldList.append(",");
                            sqlUpdateFieldList.append(",");
                            sqlInsertReplacementList.append(",");
                        }
                        selectSQL.append(fieldName);
                        sqlInsertFieldList.append(fieldName);
                        sqlUpdateFieldList.append(fieldName + "=?");
                        sqlInsertReplacementList.append("?");
                        statementTable.put(regRdbClass, fieldName, SELECT,
                                           "SELECT " + fieldName + " FROM " + regRdbObj.getTableNameForSQL() + " WHERE " + primaryKeyClause);
                        break;
                    case (Register.BLOBDATA):
                        if (sqlInsertFieldList.length() > 0) {
                            sqlInsertFieldList.append(",");
                            sqlInsertReplacementList.append(",");
                        }
                        sqlInsertFieldList.append(fieldName);
                        sqlInsertReplacementList.append("EMPTY_BLOB()");
                        statementTable.put(regRdbClass, fieldName, SELECT,
                                           "SELECT " + fieldName + " FROM " + regRdbObj.getTableNameForSQL() + " WHERE " + primaryKeyClause);
                        break;
                    case (Register.CLOBDATA):
                        if (sqlInsertFieldList.length() > 0) {
                            sqlInsertFieldList.append(",");
                            sqlInsertReplacementList.append(",");
                        }
                        sqlInsertFieldList.append(fieldName);
                        sqlInsertReplacementList.append("EMPTY_CLOB()");
                        statementTable.put(regRdbClass, fieldName, SELECT,
                                           "SELECT " + fieldName + " FROM " + regRdbObj.getTableNameForSQL() + " WHERE " + primaryKeyClause);
                        break;
                    case (Register.XMLDATA):
                        if (sqlInsertFieldList.length() > 0) {
                            selectSQL.append(",");
                            sqlInsertFieldList.append(",");
                            sqlUpdateFieldList.append(",");
                            sqlInsertReplacementList.append(",");
                        }
                        selectSQL.append(fieldName);
                        sqlInsertFieldList.append(fieldName);

//                        sqlUpdateFieldList.append(fieldName+"=XMLTYPE(?)");
                        //                      sqlInsertReplacementList.append("XMLTYPE(?)");

                        sqlUpdateFieldList.append(fieldName + "=?");
                        sqlInsertReplacementList.append("?");

                        statementTable.put(regRdbClass, fieldName, SELECT,
                                           "SELECT " + fieldName + " FROM " + regRdbObj.getTableNameForSQL() + " WHERE " + primaryKeyClause);
                        break;
                }

            }
            selectSQL.append(" FROM " + regRdbObj.getTableNameForSQL() + " WHERE " + primaryKeyClause);
            statementTable.put(regRdbClass, SELECTALL, "SELECT " + primaryKeyList + " FROM " + regRdbObj.getTableNameForSQL() + " ORDER BY " + primaryKeyList);
            statementTable.put(regRdbClass, SELECTALLNOORDER, "SELECT " + primaryKeyList + " FROM " + regRdbObj.getTableNameForSQL());
            statementTable.put(regRdbClass, SELECT, selectSQL.toString());
            statementTable.put(regRdbClass, INSERT,
                               "INSERT INTO " + regRdbObj.getTableNameForSQL() + " (" + sqlInsertFieldList + ") VALUES (" + sqlInsertReplacementList + ")");
            statementTable.put(regRdbClass, UPDATE, "UPDATE " + regRdbObj.getTableNameForSQL() + " SET " + sqlUpdateFieldList + " WHERE " + primaryKeyClause);
            statementTable.put(regRdbClass, DELETE, "DELETE FROM " + regRdbObj.getTableNameForSQL() + " WHERE " + primaryKeyClause);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String setLookupByMemberSQL(Class regRdbClass, String fieldName, String orderField, boolean ascending) {
        try {
            Hashtable fieldTable = getFieldTable(regRdbClass.getName());
            if (fieldName != null && !fieldTable.containsKey(fieldName)) {
                return null;
            }
            if (orderField != null && !fieldTable.containsKey(orderField)) {
                return null;
            }
            RdbData regRdbObj = (RdbData) regRdbClass.getConstructor( (Class[])null).newInstance( (Object[])null);

            String primaryKeyList = null;
            String[] primaryKeys = regRdbObj.getPrimaryKeyFields();
            if (primaryKeys == null) {
                String primaryField = getFieldName(regRdbClass.getName(), 0);
                if (primaryField != null) {
                    primaryKeys = new String[] {
                        primaryField};
                }
            }
            if (primaryKeys == null || primaryKeys.length == 0) {
                throw new IllegalArgumentException("Unable to generate primary key list for SELECT statement");
            }
            primaryKeyList = ExtString.join(primaryKeys, ',');

            String selectSQL = "SELECT " + primaryKeyList + " FROM " + regRdbObj.getTableNameForSQL();
            if (fieldName != null) {
                selectSQL += " WHERE " + fieldName + "=?";
            }
            if (orderField != null) {
                selectSQL += " ORDER BY " + orderField + " " + (ascending ? "ASC" : "DESC");
            }
            statementTable.put(regRdbClass, fieldName, orderField, ascending, SELECTALL, selectSQL);
            return selectSQL;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

/**
 * Private Class which holds the generated SQL statements for registered classes
 */

class StatementTable {
    private Hashtable statements = new Hashtable();

    StatementTable() {}

    public Object put(Class classObj, short statementType, String statement) {
        return put(classObj, null, statementType, statement);
    }

    public Object put(Class classObj, String fieldName, short statementType, String statement) {
        return put(classObj.getName(), fieldName, statementType, statement);
    }

    public String get(Class classObj, short statementType) {
        return get(classObj, null, statementType);
    }

    public String get(Class classObj, String fieldName, short statementType) {
        return get(classObj.getName(), fieldName, statementType);
    }

    public Object put(Class classObj, String fieldName, String orderField, boolean ascending, short statementType, String statement) {
        return put(classObj.getName(), fieldName, orderField, ascending, statementType, statement);
    }

    public String get(Class classObj, String fieldName, String orderField, boolean ascending, short statementType) {
        return get(classObj.getName(), fieldName, orderField, ascending, statementType);
    }

    public Object put(String className, String fieldName, short statementType, String statement) {
        return put(className, fieldName, null, false, statementType, statement);
    }

    public Object put(String className, String fieldName, String orderField, boolean ascending, short statementType, String statement) {
        return statements.put(getStatementKey(className, fieldName, orderField, ascending, statementType), statement);
    }

    public String get(String className, String fieldName, short statementType) {
        return get(className, fieldName, null, false, statementType);
    }

    public String get(String className, String fieldName, String orderField, boolean ascending, short statementType) {
        return (String) statements.get(getStatementKey(className, fieldName, orderField, ascending, statementType));
    }

    private String getStatementKey(String className, String fieldName, String orderField, boolean ascending, short statementType) {
        String stmtKey = className + ".";
        if (fieldName != null) {
            stmtKey += fieldName + ".";
        }
        stmtKey += statementType;

        if (orderField != null) {
            stmtKey += "." + orderField + (ascending ? '+' : '-');
        }
        return stmtKey;
    }

    public void printAll() {
        Iterator statementIter = statements.keySet().iterator();
        while (statementIter.hasNext()) {
            Object key = statementIter.next();
            System.out.println(key + " => " + statements.get(key));
        }
    }

}
