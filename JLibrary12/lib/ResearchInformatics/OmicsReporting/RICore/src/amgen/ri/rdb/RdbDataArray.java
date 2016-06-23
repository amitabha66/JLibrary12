/*
 *   RdbDataArray
 *   Used to generate an array of RdbData elements. This implementation retrieves all elements of the given
 *   class which exist in the database.
 *   See individual constructor comments for descriptions of array types.
 *   $Revision: 1.1 $
 *   Created: Jeffrey McDowell, 10 Jun 2001
 *   Modified: $Author: cvs $
 *   $Log
 *
 */
package amgen.ri.rdb;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import amgen.ri.util.ExtString;

/**
 *   Used to generate an array of RdbData elements. This implementation retrieves all elements of the given
 *   class which exist in the database.
 *   See individual constructor comments for descriptions of array types.
 *   @version $Revision: 1.1 $
 *   @author Jeffrey McDowell
 *   @author $Author: cvs $
 */
public class RdbDataArray extends RdbData implements XMLWritable, List {
    public static final short MEMBER_LOOKUP = 0;
    public static final short SQL = 1;
    public static final short ALL = 2;

    protected RdbData[] dataItems;
    private RdbArrayList dataArray;
    private Class regRdbClass;
    private short type;
    private String sql;
    private String[] sqlReplacement;
    private String[] transientMemberNames;
    private HashMap transientMemberValues;

    /** Creates a zero-order array & Used in reflection registration */
    public RdbDataArray() {
        super();
        dataArray = new RdbArrayList();
        this.dataSet = true;
        this.dataSetSuccess = true;
    }

    /** Creates a zero-order array */
    public RdbDataArray(SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        dataArray = new RdbArrayList();
        this.dataSet = true;
        this.dataSetSuccess = true;
    }

    /**
     * Creates an array of the given registered class which results in all elements
     * in the table populate this array
     * @param rdbDataClass the RdbData class with which this array will be populated
     * @param sqlManager the SQLManager object used for Oracle connections
     * @param connectionPool the name of the connection pool to use
     */
    public RdbDataArray(Class regRdbClass, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        this.regRdbClass = regRdbClass;
        this.type = ALL;
        Register.getRegister().register(regRdbClass);
        setArrayConnectionPool(connectionPool);
    }

    /**
     * Creates an array of the given registered class which results in all elements
     * in the table populate this array
     * @param rdbDataClass the RdbData class with which this array will be populated
     * @param orderField is the member field used in an ORDER BY clause. For example, if ENAME is the orderField,
     *     the generated SQL is 'SELECT EMPNO FROM EMP ORDER BY ENAME ASC'.
     * @param ascending if true, the ordering is ascending, otherwise descending
     * @param sqlManager the SQLManager object used for Oracle connections
     * @param connectionPool the name of the connection pool to use
     */
    public RdbDataArray(Class regRdbClass, String orderField, boolean ascending, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        this.regRdbClass = regRdbClass;
        Register.getRegister().register(regRdbClass);
        this.sql = Register.getRegister().getRegisteredStatement(regRdbClass, null, orderField, ascending, Register.SELECTALL);
        if (this.sql == null) {
            throw new NullPointerException("Unable to find generated SQL for " + regRdbClass);
        }
        type = SQL;
        setArrayConnectionPool(connectionPool);
    }

    /**
     * Creates a populated array of the given registered class
     * @param rdbData the RdbData array elements to which to populate
     * @param sqlManager the SQLManager object used for Oracle connections
     * @param connectionPool the name of the connection pool to use
     */
    public RdbDataArray(RdbData[] rdbData, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        dataArray = new RdbArrayList(rdbData);
        dataItems = null;
        if (rdbData != null && rdbData.length > 0) {
            this.regRdbClass = rdbData[0].getClass();
        } else {
            this.regRdbClass = rdbData.getClass().getDeclaringClass();
        }
        if (this.regRdbClass != null) {
            Register.getRegister().register(regRdbClass);
        }
        setArrayConnectionPool(connectionPool);
        dataSet = true;
        dataSetSuccess = true;
    }

    /**
     * Creates a populated array of the given registered class.
     * @param rdbData the RdbData array elements to which to populate
     * @exception ArrayIndexOutOfBoundsException if rdbData has zero-length
     */
    public RdbDataArray(RdbData[] rdbData) {
        super(rdbData[0].getSQLManager(), rdbData[0].getLogonUsername(), rdbData[0].getConnectionPool());

        dataArray = new RdbArrayList(rdbData);
        dataItems = null;
        this.regRdbClass = rdbData.getClass().getComponentType();
        Register.getRegister().register(regRdbClass);
        setArrayConnectionPool(rdbData[0].getConnectionPool());
        dataSet = true;
        dataSetSuccess = true;
    }

    /**
     * Creates an array of the given registered class using the given primary keys in the RdbData class' constructor
     * i.e. regRdbClass(String primaryKey, SQlManager sqlManager, String logonusername, String connectionPool)
     * @param rdbDataClass the RdbData class with which this array will be populated
     * @param keys array of primary keys used in the rdb
     * @param sqlManager the SQLManager object used for Oracle connections
     * @param connectionPool the name of the connection pool to use
     */
    public RdbDataArray(Class regRdbClass, String[] keys, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        this.regRdbClass = regRdbClass;
        Register.getRegister().register(regRdbClass);
        setArrayConnectionPool(connectionPool);
        setArrayData(keys);
        dataSet = true;
    }

    /**
     * Creates an array of the given registered class using either a member field for which a replacement token is provided or
     * a SQL statement for which a replacement token is optionally provided.
     * Which of these 2 options is used depends on the value of lookupType which is either MEMBER_LOOKUP of SQL.
     *
     * MEMBER_LOOKUP:
     * lookupToken is the member field name for which the SQL statement will be generated. For example, suppose "mgr" is the field
     *    name for the Emp class with primary key empno, the generated SQL is 'SELECT EMPNO FROM EMP WHERE MGR=?'
     * lookupToken Key is the replacement used in the generated SQL. For the example above, it would be the replacement for '?' in
     *    the WHERE MGR=? clause. The resulting array would contain all rows in the EMP table with MGR equal to the supplied replacement
     * If the memberField supplied in lookupToken does not exist in the given regRdbClass, a NullPointerException is thrown.
     *
     * SQL:
     * lookupToken is a complete SQL statement with or without 1 or more replacement tags ('?') which returns the regRdbClass primary
     *     key as the first column in the SELECT.
     * lookupTokenKey is the replacement(s) used in the SQL statement. There must be enough replacements as there are
     *     replacement tags. It may be null if there are no replacement tags.
     *
     * @param regRdbClass the RdbData class with which this array will be populated
     * @param lookupToken either a memeber field or SQL statement used to populate the array. See above.
     * @param lookupTokenKey replacement element(s) for either the member field of the SQL statement. See above.
     * @param sqlManager the SQLManager object used for Oracle connections
     * @param connectionPool the name of the connection pool to use
     */
    public RdbDataArray(Class regRdbClass, String lookupSQL, String[] lookupTokenKey, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        this.regRdbClass = regRdbClass;
        Register.getRegister().register(regRdbClass);
        this.sql = lookupSQL;
        type = SQL;
        setArrayConnectionPool(connectionPool);
        this.sqlReplacement = lookupTokenKey;
    }

    /**
     * Creates an array of the given registered class using a SQL statement for which a replacement token is optionally provided.
     * Optionally, a list of transient class members may be provided. These must
     * 1) Be a column in the SQL statement provided. Use a column alias is necessary.
     * 2) Be a protected,transient class variable in the member rdbData class
     *
     * @param regRdbClass the RdbData class with which this array will be populated
     * @param lookupSQL is a complete SQL statement with or without 1 or more replacement tags ('?') which returns the regRdbClass primary
     *     key as the first column in the SELECT.
     * @param lookupReplacements is the replacement(s) used in the SQL statement. There must be enough replacements as there are
     *     replacement tags. It may be null if there are no replacement tags.
     * @param transientMemberNames transient class variables which are to be set via columns on the SQL statement
     * @param sqlManager the SQLManager object used for Oracle connections
     * @param logonusername login user
     * @param connectionPool the name of the connection pool to use
     */
    public RdbDataArray(Class regRdbClass, String lookupSQL, String[] lookupReplacement, String[] transientMemberNames, SQLManagerIF sqlManager, String logonusername,
                        String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        this.regRdbClass = regRdbClass;
        this.transientMemberNames = transientMemberNames;
        Register.getRegister().register(regRdbClass);

        this.sql = lookupSQL;
        type = SQL;
        setArrayConnectionPool(connectionPool);
        this.sqlReplacement = lookupReplacement;
    }

    /**
     * Creates an array of the given registered class using a member field for which a replacement token is provided.
     *
     * @param regRdbClass the RdbData class with which this array will be populated
     * @param lookupMember is the member field name for which the SQL statement will be generated. For example, suppose "mgr" is the field
     *    name for the Emp class with primary key empno, the generated SQL is 'SELECT EMPNO FROM EMP WHERE MGR=?'
     *    If the lookupMember supplied in lookupToken does not exist in the given regRdbClass, a NullPointerException is thrown.
     * @param memberKey is the replacement used in the generated SQL. For the example above, it would be the replacement for '?' in
     *    the WHERE MGR=? clause. The resulting array would contain all rows in the EMP table with MGR equal to the supplied replacement
     * @param sqlManager the SQLManager object used for Oracle connections
     * @param connectionPool the name of the connection pool to use
     */
    public RdbDataArray(Class regRdbClass, String lookupMember, String memberKey, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        this(regRdbClass, lookupMember, memberKey, null, false, sqlManager, logonusername, connectionPool);
    }

    /**
     * Creates an array of the given registered class using a member field for which a replacement token is provided and the array
     * is ordered by the given member field in ascending order. This is a convenience method which is the same as
     * RdbDataArray(regRdbClass, lookupMember, memberKey, orderField, true, sqlManager, logonusername, connectionPool)
     *
     * @param regRdbClass the RdbData class with which this array will be populated
     * @param lookupMember is the member field name for which the SQL statement will be generated. For example, suppose "mgr" is the field
     *    name for the Emp class with primary key empno, the generated SQL is 'SELECT EMPNO FROM EMP WHERE MGR=?'
     *    If the lookupMember supplied in lookupToken does not exist in the given regRdbClass, a NullPointerException is thrown.
     * @param memberKey is the replacement used in the generated SQL. For the example above, it would be the replacement for '?' in
     *    the WHERE MGR=? clause. The resulting array would contain all rows in the EMP table with MGR equal to the supplied replacement
     * @param orderField is the member field used in an ORDER BY clause. For example, if ENAME is the orderField, using the above example,
     *     the generated SQL is 'SELECT EMPNO FROM EMP WHERE MGR=? ORDER BY ENAME ASC'.
     * @param sqlManager the SQLManager object used for Oracle connections
     * @param connectionPool the name of the connection pool to use
     */
    public RdbDataArray(Class regRdbClass, String lookupMember, String memberKey, String orderField, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        this(regRdbClass, lookupMember, memberKey, orderField, true, sqlManager, logonusername, connectionPool);
    }

    /**
     * Creates an array of the given registered class using a member field for which a replacement token is provided and the array
     * is ordered by the given member field in either ascending or descending order.
     *
     * @param regRdbClass the RdbData class with which this array will be populated
     * @param lookupMember is the member field name for which the SQL statement will be generated. For example, suppose "mgr" is the field
     *    name for the Emp class with primary key empno, the generated SQL is 'SELECT EMPNO FROM EMP WHERE MGR=?'
     *    If the lookupMember supplied in lookupToken does not exist in the given regRdbClass, a NullPointerException is thrown.
     * @param memberKey is the replacement used in the generated SQL. For the example above, it would be the replacement for '?' in
     *    the WHERE MGR=? clause. The resulting array would contain all rows in the EMP table with MGR equal to the supplied replacement
     * @param orderField is the member field used in an ORDER BY clause. For example, if ENAME is the orderField, using the above example,
     *     the generated SQL is 'SELECT EMPNO FROM EMP WHERE MGR=? ORDER BY ENAME ASC'.
     * @param ascending if true, the ordering is ascending, otherwise descending
     * @param sqlManager the SQLManager object used for Oracle connections
     * @param connectionPool the name of the connection pool to use
     */
    public RdbDataArray(Class regRdbClass, String lookupMember, String memberKey, String orderField, boolean ascending, SQLManagerIF sqlManager, String logonusername,
                        String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        this.regRdbClass = regRdbClass;
        setArrayConnectionPool(connectionPool);
        Register.getRegister().register(regRdbClass);
        this.sql = Register.getRegister().getRegisteredStatement(regRdbClass, lookupMember, orderField, ascending, Register.SELECTALL);
        if (this.sql == null) {
            throw new NullPointerException("Unable to find generated SQL for " + regRdbClass + " by " + lookupMember);
        }
        type = SQL;
        this.sqlReplacement = new String[] {
            memberKey};
    }

    /**
     * Creates an array of the given registered class using using an array of CompareTerms.
     * The array is optionally ordered by the given member field in either ascending or descending order.
     *
     * @param regRdbClass the RdbData class with which this array will be populated
     * @param queryTerms an array of CompareTerms. For example, if the terms are
     * ENAME=SMITH, JOB=CLERK, the SQL used would be 'SELECT empno FROM Emp WHERE JOB=? AND ENAME=?'
     * @param orderField is the member field used in an ORDER BY clause. For example, if ENAME is the orderField, using the above example,
     *     the generated SQL is 'ELECT empno FROM Emp WHERE JOB=? AND ENAME=? ORDER BY ENAME ASC'. Set to null if no ordering.
     * @param ascending if true, the ordering is ascending, otherwise descending
     * @param sqlManager the SQLManager object used for Oracle connections
     * @param connectionPool the name of the connection pool to use
     */
    public RdbDataArray(Class regRdbClass, CompareTerm[] queryTerms, String orderField, boolean ascending, SQLManagerIF sqlManager, String logonusername,
                        String connectionPool) {
        this(regRdbClass, queryTerms, "AND", orderField, ascending, sqlManager, logonusername, connectionPool);
    }

    /**
     * Creates an array of the given registered class using using an array of CompareTerms.
     * The array is optionally ordered by the given member field in either ascending or descending order.
     *
     * @param regRdbClass the RdbData class with which this array will be populated
     * @param queryTerms an array of CompareTerms. For example, if the terms are
     * ENAME=SMITH, JOB=CLERK, the SQL used would be 'SELECT empno FROM Emp WHERE JOB=? AND ENAME=?'
     * @param combineType either 'AND' or 'OR' defining the way the CompareTerms are combined
     * @param orderField is the member field used in an ORDER BY clause. For example, if ENAME is the orderField, using the above example,
     *     the generated SQL is 'ELECT empno FROM Emp WHERE JOB=? AND ENAME=? ORDER BY ENAME ASC'. Set to null if no ordering.
     * @param ascending if true, the ordering is ascending, otherwise descending
     * @param sqlManager the SQLManager object used for Oracle connections
     * @param connectionPool the name of the connection pool to use
     */
    public RdbDataArray(Class regRdbClass, CompareTerm[] queryTerms, String combineType, String orderField, boolean ascending, SQLManagerIF sqlManager,
                        String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        this.regRdbClass = regRdbClass;
        setArrayConnectionPool(connectionPool);
        Register.getRegister().register(regRdbClass);
        Object[] sqlAndReplacements = createSQLAndReplacements(queryTerms, combineType, orderField, ascending);
        this.sql = (String) sqlAndReplacements[0];
        if (this.sql == null) {
            throw new NullPointerException("Unable to find generated SQL for " + regRdbClass + " by query terms");
        }
        type = SQL;
        List sqlReplacementList = (List) sqlAndReplacements[1];
        if (sqlReplacementList == null) {
            this.sqlReplacement = null;
        } else {
            this.sqlReplacement = (String[]) sqlReplacementList.toArray(new String[0]);
        }
    }

    /**
     * Creates an array of the given registered class using using an array of CompareTerms.
     * The array is optionally ordered by the given member field in either ascending or descending order.
     *
     * @param regRdbClass the RdbData class with which this array will be populated
     * @param queryTerms an array of CompareTerms. For example, if the terms are
     * ENAME=SMITH, JOB=CLERK, the SQL used would be 'SELECT empno FROM Emp WHERE JOB=? AND ENAME=?'
     * @param sqlManager the SQLManager object used for Oracle connections
     * @param connectionPool the name of the connection pool to use
     */
    public RdbDataArray(Class regRdbClass, CompareTerm[] queryTerms, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        this(regRdbClass, queryTerms, null, true, sqlManager, logonusername, connectionPool);
    }
    /**
     * Creates an array of the given registered class using using an array of CompareTerms.
     * The array is optionally ordered by the given member field in either ascending or descending order.
     *
     * @param regRdbClass the RdbData class with which this array will be populated
     * @param queryTerm a CompareTerm. For example, if the terms are
     * ENAME=SMITH, JOB=CLERK, the SQL used would be 'SELECT empno FROM Emp WHERE JOB=? AND ENAME=?'
     * @param sqlManager the SQLManager object used for Oracle connections
     * @param connectionPool the name of the connection pool to use
     */
    public RdbDataArray(Class regRdbClass, CompareTerm queryTerm, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        this(regRdbClass, new CompareTerm[] {queryTerm}, null, true, sqlManager, logonusername, connectionPool);
    }

    /**
     * Creates an array of the given registered class using using an array of CompareTerms.
     * The array is optionally ordered by the given member field in either ascending or descending order.
     *
     * @param regRdbClass the RdbData class with which this array will be populated
     * @param queryTerms an array of CompareTerms. For example, if the terms are
     * ENAME=SMITH, JOB=CLERK, the SQL used would be 'SELECT empno FROM Emp WHERE JOB=? AND ENAME=?'
     * @param combineType either 'AND' or 'OR' defining the way the CompareTerms are combined
     * @param orderField is the member field used in an ORDER BY clause. For example, if ENAME is the orderField, using the above example,
     * @param sqlManager the SQLManager object used for Oracle connections
     * @param connectionPool the name of the connection pool to use
     */
    public RdbDataArray(Class regRdbClass, CompareTerm[] queryTerms, String combineType, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        this(regRdbClass, queryTerms, combineType, null, true, sqlManager, logonusername, connectionPool);
    }

    protected Object[] createSQLAndReplacements(CompareTerm[] queryTerms, String combineType, String orderField, boolean ascending) {
        if (regRdbClass == null) {
            throw new NullPointerException("Unable to generate SQL for  " + regRdbClass + " by query terms");
        }
        if (queryTerms == null || queryTerms.length == 0) {
            return new String[] {
                Register.getRegister().getRegisteredStatement(regRdbClass, null, orderField, ascending, Register.SELECTALL),
                null
            };
        }

        try {
            Hashtable fieldTable = Register.getRegister().getFieldTable(regRdbClass.getName());
            RdbData regRdbObj = (RdbData) regRdbClass.getConstructor( (Class[])null).newInstance( (Object[])null);

            String primaryKeyList = null;
            String[] primaryKeys = regRdbObj.getPrimaryKeyFields();
            if (primaryKeys == null) {
                String primaryField = Register.getRegister().getFieldName(regRdbClass.getName(), 0);
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
            ArrayList replacementTerms = new ArrayList();
            if (queryTerms != null) {
                StringBuffer where = new StringBuffer();
                for (int i = 0; i < queryTerms.length; i++) {
                    if (where.length() > 0) {
                        where.append(" " + combineType + " ");
                    }
                    if (queryTerms[i] instanceof NullCompareTerm) {
                        where.append(queryTerms[i].toString());
                    } else {
                        where.append(queryTerms[i].asReplacement());
                        if (queryTerms[i].getCompareValues() instanceof Collection) {
                            Collection<String> values = (Collection<String>) queryTerms[i].getCompareValues();
                            replacementTerms.addAll(values);
                        } else {
                            replacementTerms.add(queryTerms[i].getCompareValue());
                        }
                    }
                }
                selectSQL += " WHERE " + where;
            }
            if (orderField != null) {
                selectSQL += " ORDER BY " + orderField + " " + (ascending ? "ASC" : "DESC");
            }
            return new Object[] {
                selectSQL,
                replacementTerms
            };
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException("Unable to generate SQL for  " + regRdbClass + " by query terms");
        }
    }

    protected void setArrayConnectionPool(String providedConnectionPool) {
        if (providedConnectionPool == null && regRdbClass != null) {
            super.setConnectionPool(regRdbClass);
        }
    }

    /**
     * Returns the identifier for the array using the given ResultSet at the current cursor position.
     * The default implementation returns all queried columns in the populating SQL statement as
     * a CSV string. E.g. SELECT ID1,ID2 FROM TABLE => "ID1,ID2"
     * If any transient fields are defined via the constructor(s) above, this is used to
     * exact these columns from the ResultSet
     * @param rset the populating SQL statement's ResultSet at the current array's cursor position (i.e. next() has been called
     * on the ResultSet
     * @return the value to be used as the primary key for the RdbData array member
     * @throws SQLException if any problems occur while reading the ResultSet
     */
    protected String getArrayIdentifier(String fieldName, ResultSet rset) throws SQLException {
        if (transientMemberNames == null) {
            return super.getArrayIdentifier(fieldName, rset);
        }
        transientMemberValues = new HashMap();
        StringBuffer identifier = new StringBuffer();
        int columnCount = rset.getMetaData().getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = rset.getMetaData().getColumnName(i);
            String columnValue = rset.getString(i);
            int transientListPos = inTransientList(columnName);
            //NOT IN TRANSIENT FIELD LIST, SO ASSUME PART OF PRIMARY KEY
            if (transientListPos < 0) {
                if (identifier.length() > 0) {
                    identifier.append(",");
                }
                identifier.append(columnValue);
            } else {
                transientMemberValues.put(transientMemberNames[transientListPos], columnValue);
            }
        }
        return identifier.toString();
    }

    /**
     * Called when an RdbData array element is set.
     * If any transient members are defined and available, this method sets these fields
     * @param rdbData new array element
     */
    protected void arrayElementSet(RdbData rdbData) {
        if (transientMemberNames == null) {
            return;
        }
        Iterator transientMemberNamesIter = transientMemberValues.keySet().iterator();
        while (transientMemberNamesIter.hasNext()) {
            String fieldName = (String) transientMemberNamesIter.next();
            String stringValue = (String) transientMemberValues.get(fieldName);
            if (stringValue == null) {
                continue;
            }
            try {
                Field field = rdbData.getClass().getDeclaredField(fieldName);
                Object fieldValue = stringValue;
                switch (Register.getRegister().getClassType(field.getType())) {
                    case (Register.STRING):
                        break;
                    case (Register.DATE):
                        fieldValue = java.sql.Date.valueOf(stringValue);
                        break;
                    case (Register.LONG):
                        fieldValue = Long.valueOf(stringValue);
                        break;
                    case (Register.INTEGER):
                        fieldValue = Integer.valueOf(stringValue);
                        break;
                    case (Register.DOUBLE):
                        fieldValue = Double.valueOf(stringValue);
                        break;
                    case (Register.JAVADATE):
                        fieldValue = JavaDate.valueOf(stringValue);
                        break;
                    case (Register.ORASEQUENCE):
                        fieldValue = OraSequenceField.valueOf(stringValue, fieldName);
                        break;
                    case (Register.RDBDATA):
                        Class fieldClass = getClass().getDeclaredField(fieldName).getType();
                        fieldValue = fieldClass.getConstructor(
                            new Class[] {String.class, SQLManagerIF.class, String.class, String.class}).newInstance(
                                new Object[] {fieldValue, getSQLManager(), getLogonUsername(), getConnectionPool()});
                        break;
                    case (Register.SEQUENCE):
                        fieldValue = SequenceField.valueOf(stringValue, fieldName);
                        break;
                    case (Register.CLOBDATA):
                        fieldValue = ClobData.valueOf(stringValue, fieldName);
                        break;
                    case (Register.BLOBDATA):
                        fieldValue = BlobData.valueOf(stringValue, fieldName);
                        break;
                    case (Register.XMLDATA):
                        fieldValue = XMLData.valueOf(stringValue, fieldName, getSQLManager(), getConnectionPool());
                        break;
                    case (Register.HASHTABLE):
                    case (Register.PRIMITIVEARRAY):
                    case (Register.MEMBERARRAY):
                    case (Register.SQLRESULTCOLUMN):
                        System.err.println("ERROR: Member type can not be set by String value.");
                        break;
                    default:
                        System.err.println("ERROR: Unknown member type or member type can not be set by String value.");
                        break;
                }
                int fieldModifiers = field.getModifiers();
                if (Modifier.isPrivate(fieldModifiers)) {
                    continue;
                }
                if (!Modifier.isTransient(fieldModifiers)) {
                    System.err.println("WARNING: Setting a non-transient class variable");
                }
                rdbData.setFieldValue(field, fieldValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns if the given column name is in the transient column list
     * @return the position in the transient array or -1 if not present
     */
    private int inTransientList(String columnName) {
        for (int i = 0; i < transientMemberNames.length; i++) {
            if (columnName.equalsIgnoreCase(transientMemberNames[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Populates the array with the given list of keys used as the primary for the regRdbClass' constructor:
     * regRdbClass(String primaryKey, SQlManager sqlManager, String logonusername, String connectionPool)
     */
    private void setArrayData(String[] keys) {
        try {
            Class[] args = {
                String.class, SQLManagerIF.class, String.class, String.class};
            dataArray = new RdbArrayList();
            for (int i = 0; i < keys.length; i++) {
                dataArray.add( (RdbData) regRdbClass.getConstructor(args).newInstance(
                    new Object[] {keys[i], getSQLManager(), getLogonUsername(), getConnectionPool()}));
            }
            dataSetSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            dataArray = new RdbArrayList();
            dataSetSuccess = false;
        }
    }

    protected String getMemberSQL(String fieldName) {
        if (!fieldName.equals("dataItems")) {
            return null;
        }
        switch (type) {
            case (SQL):
                return sql;
        }
        return Register.getRegister().getRegisteredStatement(regRdbClass, null, Register.SELECTALL);
    }

    public String getTheSQL() {
        return sql;
    }

    /**
     * Sets the array item in this RdbData array class using the RdbData class machinery. The dataItems array is
     * then put into an ArrayList which is the actual container. The dataItems array is set back to null to allow the
     * garbage collector to remove it.
     */
    public boolean setData() {
        if (!dataSet) {
            dataSet = super.setData();
            dataArray = new RdbArrayList(dataItems);
            dataItems = null;
        }
        return dataSetSuccess;
    }

    /**
     * Used by subclasses to set the data members
     * @param rdbData the members to populate the array
     */
    protected void setData(RdbData[] rdbData) {
        dataArray = new RdbArrayList(rdbData);
        dataItems = null;
        this.regRdbClass = rdbData.getClass().getComponentType();
        Register.getRegister().register(regRdbClass);
        setArrayConnectionPool(getConnectionPool());
        dataSet = true;
        dataSetSuccess = true;
    }

    /** Get value for id */
    public String getIdentifier() {
        return null;
    }

    protected String[] getMemberSQLValues(String fieldName) {
        switch (type) {
            case (SQL):
                return sqlReplacement;
        }
        return super.getMemberSQLValues(fieldName);
    }

    protected void setFieldValue(Field field, Object value) throws IllegalAccessException {
        field.set(this, value);
    }

    protected Object getFieldValue(Field field) throws IllegalAccessException {
        return field.get(this);
    }

    /**
     * Returns whether the generating SQL command should be generated by introspection (see above for details)
     */
    protected boolean generateSQL() {
        return false;
    }

    /** Returns the items as an array of RdbData objects */
    public RdbData[] getItems() {
        RdbData[] dataItems = new RdbData[getCount()];
        getArrayList().toArray(dataItems);
        return dataItems;
    }

    /**
     * Returns the items as an array of the given class type. The RdbData objects must be subclasses of the given class type.
     * @param classType the class type to cast all the item to
     * @return an array Object of the data items in this RdbDataArray
     */
    public Object getItems(Class classType) {
        RdbData[] items = getItems();
        Object array = Array.newInstance(classType, items.length);
        System.arraycopy(items, 0, array, 0, items.length);
        return array;
    }

    /**
     * Returns an array of fields from the RdbData array elements.
     * @param fieldName the class variable name
     * @return an array of Objects
     * @exception NoSuchFieldException if the fieldName does not exist in the Class
     */
    public Object[] getFieldArray(String fieldName) {
        Class fieldClass = Register.getRegister().getFieldClass(regRdbClass, fieldName);

        RdbData[] items = getItems();
        Object[] fieldValues = (Object[]) Array.newInstance(fieldClass, items.length);
        for (int i = 0; i < items.length; i++) {
            fieldValues[i] = items[i].get(fieldName);
        }
        return fieldValues;
    }

    /**
     * Returns an array of values in the given column/class variable in this array.
     * Optionally, only unique values can be returned. This has slightly more overhead.
     * @param fieldName the class variable name
     * @param unique whether to include only unique values as tested by the class variable's equals()
     * method. If this is false, getFieldArray(String fieldName) is actually used.
     * @return
     */
    public Object[] getFieldArray(String fieldName, boolean unique) {
        Class fieldClass = Register.getRegister().getFieldClass(regRdbClass, fieldName);
        if (!unique) {
            return getFieldArray(fieldName);
        }
        ArrayList values = new ArrayList();
        RdbData[] items = getItems();
        for (int i = 0; i < items.length; i++) {
            Object value = items[i].get(fieldName);
            if (!values.contains(value)) {
                values.add(value);
            }
        }
        Object[] fieldValues = (Object[]) Array.newInstance(fieldClass, values.size());
        return values.toArray(fieldValues);
    }

    /**
     * Returns an array of fields from the RdbData array elements.
     * @param fieldName the class variable name
     * @return an array of Objects
     * @exception NoSuchFieldException if the fieldName does not exist in the Class
     */
    public Object[] getFieldArray(String fieldName, Class _class) {
        RdbData[] items = getItems();
        Object[] fieldValues = (Object[]) Array.newInstance(_class, items.length);
        for (int i = 0; i < items.length; i++) {
            fieldValues[i] = items[i].get(fieldName);
        }
        return fieldValues;
    }

    /**
     * Returns a List of fields from the RdbData array elements.
     * @param fieldName the class variable name
     * @return an array of Objects
     * @exception NoSuchFieldException if the fieldName does not exist in the Class
     */
    public List getFieldList(String fieldName) {
        List list = new ArrayList();
        list.addAll(Arrays.asList(getFieldArray(fieldName)));
        return list;
    }

    /**
     * Returns a List of values in the given column/class variable in this array.
     * Optionally, only unique values can be returned. This has slightly more overhead.
     * @param fieldName the class variable name
     * @param unique whether to include only unique values as tested by the class variable's equals()
     * method. If this is false, getFieldArray(String fieldName) is actually used.
     * @return
     */
    public List getFieldList(String fieldName, boolean unique) {
        List list = new ArrayList();
        list.addAll(Arrays.asList(getFieldArray(fieldName, unique)));
        return list;
    }

    /**
     * Returns a List of fields from the RdbData array elements.
     * @param fieldName the class variable name
     * @return an array of Objects
     * @exception NoSuchFieldException if the fieldName does not exist in the Class
     */
    public List getFieldList(String fieldName, Class _class) {
        List list = new ArrayList();
        list.addAll(Arrays.asList(getFieldArray(fieldName, _class)));
        return list;
    }

    /**
     * Returns the array members sorted by the given fieldName values using
     * the field's default Comparator is used. The field class variable must
     * implement Comparable.
     * The sorting done using the Arrays.sort() method.
     * @param fieldName the class variable name
     * @exception NoSuchFieldException if the fieldName does not exist in the Class
     */
    public RdbData[] getSortedItems(String fieldName) {
        return getSortedItems(fieldName, null);
    }

    /**
     * Returns the array members sorted by the given fieldName values. A Comparator
     * can be provided or the default Comparator is used. In this case, the fieldName
     * class variable must implement Comparable.
     * The sorting done using the Arrays.sort() method.
     * @param fieldName the class variable name
     * @param comparator the comparator to use. If null, the default Comparator is used if
     * the class variable implements Comparable
     * @exception NoSuchFieldException if the fieldName does not exist in the Class
     */
    public RdbData[] getSortedItems(String fieldName, Comparator comparator) {
        RdbData[] items = getItems();
        RdbData[] newItems = new RdbData[items.length];
        System.arraycopy(items, 0, newItems, 0, items.length);
        RdbDataByFieldComparator rdbDataByFieldComparator = new RdbDataByFieldComparator(fieldName, comparator);
        Arrays.sort(newItems, rdbDataByFieldComparator);
        return newItems;
    }

    /**
     * Returns an array of Number objects which have been converted from the RdbData array elements.
     * The method does a toString() on the field value and uses Double.valueOf() to convert.
     * If the conversion fails or the field value is null, a 0 is added.
     * @param fieldName the class variable name
     * @param unique whether the returned list of numbers should be unique
     * @return an array of Number objects
     * @exception NoSuchFieldException if the fieldName does not exist in the Class
     */
    public Number[] getNumberFromStringFieldArray(String fieldName, boolean unique) {
        RdbData[] items = getItems();
        ArrayList fieldValueList = new ArrayList();
        for (int i = 0; i < items.length; i++) {
            Double value;
            try {
                value = Double.valueOf(items[i].get(fieldName).toString());
            } catch (Exception e) {
                value = new Double(0);
            }
            if (!unique || !fieldValueList.contains(value)) {
                fieldValueList.add(value);
            }
        }
        return (Number[]) fieldValueList.toArray(new Number[0]);
    }

    /**
     * Returns an array of Long objects which have been converted from the RdbData array elements.
     * The method does a toString() on the field value and uses Long.valueOf() to convert.
     * If the conversion fails or the field value is null, a 0 is added.
     * @param fieldName the class variable name
     * @param unique whether the returned list of numbers should be unique
     * @return an array of Number objects
     * @exception NoSuchFieldException if the fieldName does not exist in the Class
     */
    public Long[] getLongFromStringFieldArray(String fieldName, boolean unique) {
        RdbData[] items = getItems();
        ArrayList fieldValueList = new ArrayList();
        for (int i = 0; i < items.length; i++) {
            Long value;
            try {
                value = Long.valueOf(items[i].get(fieldName).toString());
            } catch (Exception e) {
                value = new Long(0);
            }
            if (!unique || !fieldValueList.contains(value)) {
                fieldValueList.add(value);
            }
        }
        return (Long[]) fieldValueList.toArray(new Long[0]);
    }

    /**
     * Returns an array of the identifiers of the items
     * @return
     */
    public String[] getIdentifierArray() {
        String[] values = new String[size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = getItem(i).getIdentifier();
        }
        return values;
    }

    /**
     * Returns an array of values in the given column/class variable in this array.
     * Optionally, only unique values can be returned. This has slightly more overhead.
     * @param fieldName the class variable name
     * @param _class the class variable's Class type
     * @param unique whether to include only unique values as tested by the class variable's equals()
     * method. If this is false, getFieldArray(String fieldName, Class _class) is actually used.
     * @return
     */
    public Object[] getFieldArray(String fieldName, Class _class, boolean unique) {
        if (!unique) {
            return getFieldArray(fieldName, _class);
        }
        ArrayList values = new ArrayList();
        RdbData[] items = getItems();
        for (int i = 0; i < items.length; i++) {
            Object value = items[i].get(fieldName);
            if (!values.contains(value)) {
                values.add(value);
            }
        }
        Object[] fieldValues = (Object[]) Array.newInstance(_class, values.size());
        return values.toArray(fieldValues);
    }

    /**
     * Returns an array identifiers of an RdbData field from the RdbData array elements.
     * The field specified by the fieldName parameter must be an RdbData class or
     * an exception will be thrown
     * @param fieldName the class variable name of the RdbData field
     * @return an array of identifiers of the RdbData field objects
     * @exception NoSuchFieldException if the fieldName does not exist in the Class
     */
    public String[] getRdbDataFieldIdentifierArray(String fieldName) {
        RdbData[] items = getItems();
        RdbData[] rdbDataFields = (RdbData[]) getFieldArray(fieldName, RdbData.class);
        String[] identifierArray = new String[rdbDataFields.length];
        for (int i = 0; i < rdbDataFields.length; i++) {
            identifierArray[i] = rdbDataFields[i].getIdentifier();
        }
        return identifierArray;
    }

    /**
     * Returns an array identifiers of an RdbData field from the RdbData array elements.
     * The field specified by the fieldName parameter must be an RdbData class or
     * an exception will be thrown
     * @param fieldName the class variable name of the RdbData field
     * @param unique whether the returned array must be unique
     * @return an array of identifiers of the RdbData field objects
     * @exception NoSuchFieldException if the fieldName does not exist in the Class
     */
    public String[] getRdbDataFieldIdentifierArray(String fieldName, boolean unique) {
        if (!unique) {
            return getRdbDataFieldIdentifierArray(fieldName);
        }
        ArrayList identifiers = new ArrayList();
        RdbData[] rdbDataFields = (RdbData[]) getFieldArray(fieldName, RdbData.class);
        for (int i = 0; i < rdbDataFields.length; i++) {
            String identifier = rdbDataFields[i].getIdentifier();
            if (!identifiers.contains(identifier)) {
                identifiers.add(identifier);
            }
        }
        return (String[]) identifiers.toArray(new String[0]);
    }

    /**
     * Sets the key/value transient data pair for all members of the array
     * @param key
     * @param value
     */
    public void setTransientDataForMembers(Object key, Object value) {
        RdbData[] items = getItems();
        if (items == null) {
            return;
        }
        for (int i = 0; i < items.length; i++) {
            items[i].setTransientData(key, value);
        }
    }

    /**
     * Returns any transient values
     * @return Map
     */
    public Map getTransientValues() {
        return transientMemberValues;
    }

    /**
     * Returns a subset of this RdbDataArray which contains RdbData objects
     * which have fields with the given value
     * @param fieldName
     * @param value
     * @return
     */
    public RdbDataArray getSubArray(String fieldName, Object value) {
        ArrayList subArray = new ArrayList();
        RdbData[] items = getItems();
        Object checkValue;
        if (value instanceof Object[]) {
            checkValue = Arrays.asList( (Object[]) value);
        } else {
            checkValue = value;
        }
        for (int i = 0; i < items.length; i++) {
            Object fieldValue = items[i].get(fieldName);
            if (fieldValue == null) {
                continue;
            }
            if (checkValue instanceof List) {
                List checkValues = (List) checkValue;
                if (checkValues.contains(fieldValue)) {
                    subArray.add(items[i]);
                }
            } else if (fieldValue.equals(value)) {
                subArray.add(items[i]);
            }
        }
        return new RdbDataArray( (RdbData[]) subArray.toArray(new RdbData[0]),
                                getSQLManager(), getLogonUsername(), getConnectionPool());
    }

    /**
     * Returns a subset of this RdbDataArray which contains RdbData objects using
     * an RdbDataArray's values
     * @param fieldName field name of this RdbDataArray to test
     * @param testArray another RdbDataArray which to pull a field value to use as a test
     * @param testArrayFieldName the field in the test RdbDataArray chose value is used as the test
     * @return
     */
    public RdbDataArray getSubArray(String fieldName, RdbDataArray testArray, String testArrayFieldName) {
        Object[] testValues = testArray.getFieldArray(testArrayFieldName);
        return getSubArray(fieldName, testValues);
    }

    /**
     * Returns a subset of this RdbDataArray which contains RdbData objects that have returned
     * a successful setData()
     */
    public RdbDataArray validEntryArray() {
        if (size() == 0) {
            return new RdbDataArray(getSQLManager(), getLogonUsername(), getConnectionPool());
        }
        ArrayList validEntries = new ArrayList();
        for (int i = 0; i < size(); i++) {
            if (getItem(i).setData()) {
                validEntries.add(getItem(i));
            }
        }
        if (validEntries.size() == 0) {
            return new RdbDataArray(getSQLManager(), getLogonUsername(), getConnectionPool());
        }
        return new RdbDataArray( (RdbData[]) validEntries.toArray(new RdbData[0]));
    }

    /**
     * Returns the raw array list object. Only used internally in the object
     */
    private RdbArrayList getArrayList() {
        setData();
        return dataArray;
    }

    /** Get count for the items */
    public int getCount() {
        return getArrayList().size();
    }

    /** Get an item */
    public RdbData getItem(int i) {
        setData();
        if (dataArray == null || i < 0 || i >= dataArray.size()) {
            return null;
        }
        return (RdbData) dataArray.get(i);
    }

    /** Removes the item at the given position by setting it to null and attempts a garbage collection */
    public RdbData removeItem(int i) {
        if (i > 0 && i < getCount()) {
            return (RdbData) getArrayList().remove(i);
        }
        return null;
    }

    /** Get an item based on its identifier.
     *  Returns null if not present or initialized
     */
    public RdbData getItemByIdentifier(String ident) {
        if (getCount() == 0) {
            return null;
        }
        for (int i = 0; i < getCount(); i++) {
            if (getItem(i).getIdentifier().equals(ident)) {
                return getItem(i);
            }
        }
        return null;
    }

    /** Get an item based a member.
     *  This uses the member's equals() method to determine equivalence
     */
    public RdbData[] getItemByMember(String member, Object memberValue) {
        if (getCount() == 0) {
            return new RdbData[0];
        }
        ArrayList members = new ArrayList();
        for (int i = 0; i < getCount(); i++) {
            if (getItem(i).get(member).equals(memberValue)) {
                members.add(getItem(i));
            }
        }
        return (RdbData[]) members.toArray(new RdbData[0]);
    }

    /** Get an item based a member.
     *  This uses the member's equals() method to determine equivalence
     * Returns the item or null if it doesn't exist
     */
    public RdbData getFirstItemByMember(String member, Object memberValue) {
        if (getCount() > 0) {
            ArrayList members = new ArrayList();
            for (int i = 0; i < getCount(); i++) {
                if (getItem(i).get(member).equals(memberValue)) {
                    return getItem(i);
                }
            }
        }
        return null;
    }

    public boolean containsID(RdbData dataObj) {
        if (dataObj == null) {
            return false;
        }
        return (getItemByIdentifier(dataObj.getIdentifier()) != null);
    }

    /**
     * Returns the component class of this array
     */
    public Class getComponentClass() {
        return regRdbClass;
    }

    /**
     * Returns the component class of an array to be populated. If null, it determines by introspection
     */
    public Class getArrayFieldComponentClass(String fieldName) {
        return regRdbClass;
    }

    /*
     * XMLWRITABLE IMPLEMENTATIONS
     */
    public String getXMLVersion() {
        return null;
    }

    public String getArrayElement() {
        return null;
    }

    public String getClassElement() {
        return null;
    }

    public boolean includeFieldInXML(String fieldName) {
        return true;
    }

    public String getFieldElement(String fieldName) {
        return null;
    }

    public Map getArrayAttributes() {
        return null;
    }

    public Map getClassAttributes() {
        return null;
    }

    public Map getFieldAttributes(String fieldName) {
        return null;
    }

    /*
     * THE REMAINDER OF THE CODE INVOLVES IMPLEMENTING THE LIST INTERFACE
     */
    /**
     * Returns the number of elements in this list.  If this list contains
     * more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of elements in this list.
     */
    public int size() {
        return getArrayList().size();
    }

    /**
     * Returns <tt>true</tt> if this list contains no elements.
     *
     * @return <tt>true</tt> if this list contains no elements.
     */
    public boolean isEmpty() {
        return getArrayList().isEmpty();
    }

    /**
     *
     * Returns <tt>true</tt> if this list contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this list contains
     * at least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this list is to be tested.
     * @return <tt>true</tt> if this list contains the specified element.
     */
    public boolean contains(Object o) {
        return getArrayList().contains(o);
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     *
     * @return an iterator over the elements in this list in proper sequence.
     */
    public Iterator iterator() {
        return getArrayList().iterator();
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence.  Obeys the general contract of the
     * <tt>Collection.toArray</tt> method.
     *
     * @return an array containing all of the elements in this list in proper
     *	       sequence.
     * @see Arrays#asList(Object[])
     */
    public Object[] toArray() {
        return getArrayList().toArray();
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence; the runtime type of the returned array is that of the
     * specified array.  Obeys the general contract of the
     * <tt>Collection.toArray(Object[])</tt> method.
     *
     * @param a the array into which the elements of this list are to
     *		be stored, if it is big enough; otherwise, a new array of the
     * 		same runtime type is allocated for this purpose.
     * @return  an array containing the elements of this list.
     *
     * @throws ArrayStoreException if the runtime type of the specified array
     * 		  is not a supertype of the runtime type of every element in
     * 		  this list.
     */
    public Object[] toArray(Object a[]) {
        return getArrayList().toArray(a);
    }

    // Modification Operations

    /**
     * Appends the specified element to the end of this list <p>
     *
     * @param o element to be appended to this list.
     * @return <tt>true</tt> (as per the general contract of the
     *            <tt>Collection.add</tt> method).
     *
     * @throws ClassCastException if the class of the specified element
     * 		  prevents it from being added to this list.
     * @throws IllegalArgumentException if some aspect of this element
     *            prevents it from being added to this collection.
     */
    public boolean add(Object o) {
        return getArrayList().add(o);
    }

    /**
     * Removes the first occurrence in this list of the specified element
     * If this list does not contain the element, it is
     * unchanged.  More formally, removes the element with the lowest index i
     * such that <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt> (if
     * such an element exists).
     *
     * @param o element to be removed from this list, if present.
     * @return <tt>true</tt> if this list contained the specified element.
     *
     *
     */
    public boolean remove(Object o) {
        return getArrayList().remove(o);
    }

    // Bulk Modification Operations

    /**
     *
     * Returns <tt>true</tt> if this list contains all of the elements of the
     * specified collection.
     *
     * @param c collection to be checked for containment in this list.
     * @return <tt>true</tt> if this list contains all of the elements of the
     * 	       specified collection.
     *
     * @see #contains(Object)
     */
    public boolean containsAll(Collection c) {
        return getArrayList().containsAll(c);
    }

    /**
     * Appends all of the elements in the RdbDataArray to the end of
     * this list, in the order that they are returned by the specified
     * collection's iterator.  The behavior of this
     * operation is unspecified if the specified collection is modified while
     * the operation is in progress.  (Note that this will occur if the
     * specified collection is this list, and it's nonempty.)
     *
     * @param RdbDataArray array whose elements are to be added to this list.
     * @return <tt>true</tt> if this list changed as a result of the call.
     *
     * @throws ClassCastException if the class of an element in the specified
     * 	       collection prevents it from being added to this list.
     *
     * @throws IllegalArgumentException if some aspect of an element in the
     *         specified collection prevents it from being added to this
     *         list.
     *
     * @see #add(Object)
     */
    public void addAll(RdbDataArray rdbDataArray) {
        addAll(rdbDataArray.getArrayList());
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the specified
     * collection's iterator.  The behavior of this
     * operation is unspecified if the specified collection is modified while
     * the operation is in progress.  (Note that this will occur if the
     * specified collection is this list, and it's nonempty.)
     *
     * @param c collection whose elements are to be added to this list.
     * @return <tt>true</tt> if this list changed as a result of the call.
     *
     * @throws ClassCastException if the class of an element in the specified
     * 	       collection prevents it from being added to this list.
     *
     * @throws IllegalArgumentException if some aspect of an element in the
     *         specified collection prevents it from being added to this
     *         list.
     *
     * @see #add(Object)
     */
    public boolean addAll(Collection c) {
        return getArrayList().addAll(c);
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * list at the specified position.  Shifts the
     * element currently at that position (if any) and any subsequent
     * elements to the right (increases their indices).  The new elements
     * will appear in this list in the order that they are returned by the
     * specified collection's iterator.  The behavior of this operation is
     * unspecified if the specified collection is modified while the
     * operation is in progress.  (Note that this will occur if the specified
     * collection is this list, and it's nonempty.)
     *
     * @param index index at which to insert first element from the specified
     *	            collection.
     * @param c elements to be inserted into this list.
     * @return <tt>true</tt> if this list changed as a result of the call.
     *
     * @throws ClassCastException if the class of one of elements of the
     * 		  specified collection prevents it from being added to this
     * 		  list.
     * @throws IllegalArgumentException if some aspect of one of elements of
     *		  the specified collection prevents it from being added to
     *		  this list.
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *		  &lt; 0 || index &gt; size()).
     */
    public boolean addAll(int index, Collection c) {
        return getArrayList().addAll(index, c);
    }

    /**
     * Removes from this list all the elements that are contained in the
     * specified collection.
     *
     * @param c collection that defines which elements will be removed from
     *          this list.
     * @return <tt>true</tt> if this list changed as a result of the call.
     *
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean removeAll(Collection c) {
        return getArrayList().removeAll(c);
    }

    /**
     * Retains only the elements in this list that are contained in the
     * specified collection.  In other words, removes from this list all the
     * elements that are not contained in the specified collection.
     *
     * @param c collection that defines which elements this set will retain.
     *
     * @return <tt>true</tt> if this list changed as a result of the call.
     *
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean retainAll(Collection c) {
        return getArrayList().retainAll(c);
    }

    /**
     * Removes all of the elements from this list.  This
     * list will be empty after this call returns (unless it throws an
     * exception).
     */
    public void clear() {
        getArrayList().clear();
    }

    // Comparison and hashing

    /**
     * Compares the specified object with this list for equality.  Returns
     * <tt>true</tt> if and only if the specified object is also a list, both
     * lists have the same size, and all corresponding pairs of elements in
     * the two lists are <i>equal</i>.  (Two elements <tt>e1</tt> and
     * <tt>e2</tt> are <i>equal</i> if <tt>(e1==null ? e2==null :
     * e1.equals(e2))</tt>.)  In other words, two lists are defined to be
     * equal if they contain the same elements in the same order.  This
     * definition ensures that the equals method works properly across
     * different implementations of the <tt>List</tt> interface.
     *
     * @param o the object to be compared for equality with this list.
     * @return <tt>true</tt> if the specified object is equal to this list.
     */
    public boolean equals(Object o) {
        return getArrayList().equals(o);
    }

    /**
     * Returns the hash code value for this list.  The hash code of a list
     * is defined to be the result of the following calculation:
     * <pre>
     *  hashCode = 1;
     *  Iterator i = list.iterator();
     *  while (i.hasNext()) {
     *      Object obj = i.next();
     *      hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
     *  }
     * </pre>
     * This ensures that <tt>list1.equals(list2)</tt> implies that
     * <tt>list1.hashCode()==list2.hashCode()</tt> for any two lists,
     * <tt>list1</tt> and <tt>list2</tt>, as required by the general
     * contract of <tt>Object.hashCode</tt>.
     *
     * @return the hash code value for this list.
     * @see Object#hashCode()
     * @see Object#equals(Object)
     * @see #equals(Object)
     */
    public int hashCode() {
        return getArrayList().hashCode();
    }

    // Positional Access Operations

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of element to return.
     * @return the element at the specified position in this list.
     *
     * @throws IndexOutOfBoundsException if the index is out of range (index
     * 		  &lt; 0 || index &gt;= size()).
     */
    public Object get(int index) {
        return getArrayList().get(index);
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element.
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     *
     * @throws    ClassCastException if the class of the specified element
     * 		  prevents it from being added to this list.
     * @throws    IllegalArgumentException if some aspect of the specified
     *		  element prevents it from being added to this list.
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt;= size()).  */
    public Object set(int index, Object element) {
        return getArrayList().set(index, element);
    }

    /**
     * Inserts the specified element at the specified position in this list.
     * Shifts the element currently at that position
     * (if any) and any subsequent elements to the right (adds one to their
     * indices).
     *
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     *
     * @throws    ClassCastException if the class of the specified element
     * 		  prevents it from being added to this list.
     * @throws    IllegalArgumentException if some aspect of the specified
     *		  element prevents it from being added to this list.
     * @throws    IndexOutOfBoundsException if the index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public void add(int index, Object element) {
        getArrayList().add(index, element);
    }

    /**
     * Removes the element at the specified position in this list. Shifts any
     * subsequent elements to the left (subtracts one
     * from their indices).  Returns the element that was removed from the
     * list.
     *
     * @param index the index of the element to removed.
     * @return the element previously at the specified position.
     *
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public Object remove(int index) {
        return getArrayList().remove(index);
    }

    // Search Operations

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     * More formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     *
     * @param o element to search for.
     * @return the index in this list of the first occurrence of the specified
     * 	       element, or -1 if this list does not contain this element.
     */
    public int indexOf(Object o) {
        return getArrayList().indexOf(o);
    }

    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element.
     * More formally, returns the highest index <tt>i</tt> such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     *
     * @param o element to search for.
     * @return the index in this list of the last occurrence of the specified
     * 	       element, or -1 if this list does not contain this element.
     */
    public int lastIndexOf(Object o) {
        return getArrayList().lastIndexOf(o);
    }

    // List Iterators

    /**
     * Returns a list iterator of the elements in this list (in proper
     * sequence).
     *
     * @return a list iterator of the elements in this list (in proper
     * 	       sequence).
     */
    public ListIterator listIterator() {
        return getArrayList().listIterator();
    }

    /**
     * Returns a list iterator of the elements in this list (in proper
     * sequence), starting at the specified position in this list.  The
     * specified index indicates the first element that would be returned by
     * an initial call to the <tt>next</tt> method.  An initial call to
     * the <tt>previous</tt> method would return the element with the
     * specified index minus one.
     *
     * @param index index of first element to be returned from the
     *		    list iterator (by a call to the <tt>next</tt> method).
     * @return a list iterator of the elements in this list (in proper
     * 	       sequence), starting at the specified position in this list.
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *         &lt; 0 || index &gt; size()).
     */
    public ListIterator listIterator(int index) {
        return getArrayList().listIterator(index);
    }

    // View

    /**
     * Returns a view of the portion of this list between the specified
     * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive.  (If
     * <tt>fromIndex</tt> and <tt>toIndex</tt> are equal, the returned list is
     * empty.)  The returned list is backed by this list, so changes in the
     * returned list are reflected in this list, and vice-versa.  The returned
     * list supports all of the optional list operations supported by this
     * list.<p>
     *
     * This method eliminates the need for explicit range operations (of
     * the sort that commonly exist for arrays).   Any operation that expects
     * a list can be used as a range operation by passing a subList view
     * instead of a whole list.  For example, the following idiom
     * removes a range of elements from a list:
     * <pre>
     *	    list.subList(from, to).clear();
     * </pre>
     * Similar idioms may be constructed for <tt>indexOf</tt> and
     * <tt>lastIndexOf</tt>, and all of the algorithms in the
     * <tt>Collections</tt> class can be applied to a subList.<p>
     *
     * The semantics of this list returned by this method become undefined if
     * the backing list (i.e., this list) is <i>structurally modified</i> in
     * any way other than via the returned list.  (Structural modifications are
     * those that change the size of this list, or otherwise perturb it in such
     * a fashion that iterations in progress may yield incorrect results.)
     *
     * @param fromIndex low endpoint (inclusive) of the subList.
     * @param toIndex high endpoint (exclusive) of the subList.
     * @return a view of the specified range within this list.
     *
     * @throws IndexOutOfBoundsException for an illegal endpoint index value
     *     (fromIndex &lt; 0 || toIndex &gt; size || fromIndex &gt; toIndex).
     */
    public List subList(int fromIndex, int toIndex) {
        return getArrayList().subList(fromIndex, toIndex);
    }

    /**
     * Returns a List of Strings of all field values for the given RdbData class.
     * This is done by a direct query to the database- not by instantiating
     * RdbData classes
     * @param regRdbClass
     * @param fieldName
     * @param sqlManager
     * @param logonusername
     * @param connectionPool
     * @return
     */
    public static List getFieldArray(Class regRdbClass, String fieldName, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        List fieldValues = new ArrayList();
        try {
            RdbData regRdbObj = (RdbData) regRdbClass.getConstructor( (Class[])null).newInstance( (Object[])null);
            String tableName = regRdbObj.getTableNameForSQL();
            String selectSQL = "SELECT " + fieldName + " FROM " + tableName;
            ResultSet rset = sqlManager.executeQuery(selectSQL, connectionPool);
            while (rset.next()) {
                fieldValues.add(rset.getString(1));
            }
            RdbData.closeResources(rset);
            return fieldValues;
        } catch (Exception e) {
            e.printStackTrace();
            fieldValues.clear();
        }
        return fieldValues;
    }

    /**
     * Returns a List of String arrays of all field values for the given RdbData class.
     * This is done by a direct query to the database- not by instantiating
     * RdbData classes
     * @param regRdbClass
     * @param fieldName
     * @param sqlManager
     * @param logonusername
     * @param connectionPool
     * @return
     */
    public static List getFieldArray(Class regRdbClass, String[] fieldNames, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        List fieldValues = new ArrayList();
        try {
            RdbData regRdbObj = (RdbData) regRdbClass.getConstructor( (Class[])null).newInstance( (Object[])null);
            String tableName = regRdbObj.getTableNameForSQL();
            StringBuffer selectSQL = new StringBuffer("SELECT ");
            for (int i = 0; i < fieldNames.length; i++) {
                if (i > 0) {
                    selectSQL.append(',');
                }
                selectSQL.append(fieldNames[i]);
            }
            selectSQL.append(" FROM " + tableName);
            ResultSet rset = sqlManager.executeQuery(selectSQL.toString(), connectionPool);
            while (rset.next()) {
                String[] values = new String[fieldNames.length];
                for (int i = 1; i <= fieldNames.length; i++) {
                    values[i - 1] = rset.getString(i);
                }
                fieldValues.add(values);
            }
            RdbData.closeResources(rset);
            return fieldValues;
        } catch (Exception e) {
            e.printStackTrace();
            fieldValues.clear();
        }
        return fieldValues;
    }
}

class RdbArrayList extends AbstractList implements List, Cloneable {
    /**
     * The array buffer into which the elements of the ArrayList are stored.
     * The capacity of the ArrayList is the length of this array buffer.
     */
    private transient RdbData elementData[];

    /**
     * The size of the ArrayList (the number of elements it contains).
     */
    private int size;

    /**
     * Constructs an empty list with the default (10) initial capacity.
     */
    public RdbArrayList() {
        super();
        this.elementData = new RdbData[10];
    }

    /**
     * Constructs a list with the specified objects.
     */
    public RdbArrayList(RdbData[] rdbData) {
        super();
        if (rdbData == null) {
            size = 0;
            this.elementData = new RdbData[10];
        } else {
            size = rdbData.length;
            this.elementData = rdbData;
        }
    }

    /**
     * Trims the capacity of this <tt>ArrayList</tt> instance to be the
     * list's current size.  An application can use this operation to minimize
     * the storage of an <tt>ArrayList</tt> instance.
     */
    public void trimToSize() {
        modCount++;
        int oldCapacity = elementData.length;
        if (size < oldCapacity) {
            Object oldData[] = elementData;
            elementData = new RdbData[size];
            System.arraycopy(oldData, 0, elementData, 0, size);
        }
    }

    /**
     * Increases the capacity of this <tt>ArrayList</tt> instance, if
     * necessary, to ensure  that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param   minCapacity   the desired minimum capacity.
     */
    public void ensureCapacity(int minCapacity) {
        modCount++;
        int oldCapacity = elementData.length;
        if (minCapacity > oldCapacity) {
            Object oldData[] = elementData;
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            elementData = new RdbData[newCapacity];
            System.arraycopy(oldData, 0, elementData, 0, size);
        }
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return  the number of elements in this list.
     */
    public int size() {
        return size;
    }

    /**
     * Tests if this list has no elements.
     *
     * @return  <tt>true</tt> if this list has no elements;
     *          <tt>false</tt> otherwise.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     *
     * @param elem element whose presence in this List is to be tested.
     */
    public boolean contains(Object elem) {
        return indexOf(elem) >= 0;
    }

    /**
     * Searches for the first occurence of the given argument, testing
     * for equality using the <tt>equals</tt> method.
     *
     * @param   elem   an object.
     * @return  the index of the first occurrence of the argument in this
     *          list; returns <tt>-1</tt> if the object is not found.
     * @see     Object#equals(Object)
     */
    public int indexOf(Object elem) {
        if (elem == null) {
            for (int i = 0; i < size; i++) {
                if (elementData[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (elem.equals(elementData[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified object in
     * this list.
     *
     * @param   elem   the desired element.
     * @return  the index of the last occurrence of the specified object in
     *          this list; returns -1 if the object is not found.
     */
    public int lastIndexOf(Object elem) {
        if (elem == null) {
            for (int i = size - 1; i >= 0; i--) {
                if (elementData[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = size - 1; i >= 0; i--) {
                if (elem.equals(elementData[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Returns a shallow copy of this <tt>ArrayList</tt> instance.  (The
     * elements themselves are not copied.)
     *
     * @return  a clone of this <tt>ArrayList</tt> instance.
     */
    public Object clone() {
        try {
            RdbArrayList v = (RdbArrayList)super.clone();
            v.elementData = new RdbData[size];
            System.arraycopy(elementData, 0, v.elementData, 0, size);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    /**
     * Returns an array containing all of the elements in this list
     * in the correct order.
     *
     * @return an array containing all of the elements in this list
     * 	       in the correct order.
     */
    public Object[] toArray() {
        Object[] result = new Object[size];
        System.arraycopy(elementData, 0, result, 0, size);
        return result;
    }

    /**
     * Returns an array containing all of the elements in this list in the
     * correct order.  The runtime type of the returned array is that of the
     * specified array.  If the list fits in the specified array, it is
     * returned therein.  Otherwise, a new array is allocated with the runtime
     * type of the specified array and the size of this list.<p>
     *
     * If the list fits in the specified array with room to spare (i.e., the
     * array has more elements than the list), the element in the array
     * immediately following the end of the collection is set to
     * <tt>null</tt>.  This is useful in determining the length of the list
     * <i>only</i> if the caller knows that the list does not contain any
     * <tt>null</tt> elements.
     *
     * @param a the array into which the elements of the list are to
     *		be stored, if it is big enough; otherwise, a new array of the
     * 		same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list.
     * @throws ArrayStoreException if the runtime type of a is not a supertype
     *         of the runtime type of every element in this list.
     */
    public Object[] toArray(Object a[]) {
        if (a.length < size) {
            a = (Object[]) java.lang.reflect.Array.newInstance(
                a.getClass().getComponentType(), size);
        }

        System.arraycopy(elementData, 0, a, 0, size);

        if (a.length > size) {
            a[size] = null;
        }

        return a;
    }

    // Positional Access Operations

    /**
     * Returns the element at the specified position in this list.
     *
     * @param  index index of element to return.
     * @return the element at the specified position in this list.
     * @throws    IndexOutOfBoundsException if index is out of range <tt>(index
     * 		  &lt; 0 || index &gt;= size())</tt>.
     */
    public Object get(int index) {
        RangeCheck(index);

        return elementData[index];
    }

    /**
     * Returns all elements in this list.
     */
    public RdbData[] getAll() {
        return elementData;
    }

    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     * @throws    IndexOutOfBoundsException if index out of range
     *		  <tt>(index &lt; 0 || index &gt;= size())</tt>.
     */
    public Object set(int index, Object element) {
        RangeCheck(index);

        RdbData oldValue = elementData[index];
        elementData[index] = (RdbData) element;
        return oldValue;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param o element to be appended to this list.
     * @return <tt>true</tt> (as per the general contract of Collection.add).
     */
    public boolean add(Object o) {
        ensureCapacity(size + 1); // Increments modCount!!
        elementData[size++] = (RdbData) o;
        return true;
    }

    /**
     * Inserts the specified element at the specified position in this
     * list. Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     * @throws    IndexOutOfBoundsException if index is out of range
     *		  <tt>(index &lt; 0 || index &gt; size())</tt>.
     */
    public void add(int index, Object element) {
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException(
                "Index: " + index + ", Size: " + size);
        }

        ensureCapacity(size + 1); // Increments modCount!!
        System.arraycopy(elementData, index, elementData, index + 1,
                         size - index);
        elementData[index] = (RdbData) element;
        size++;
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).
     *
     * @param index the index of the element to removed.
     * @return the element that was removed from the list.
     * @throws    IndexOutOfBoundsException if index out of range <tt>(index
     * 		  &lt; 0 || index &gt;= size())</tt>.
     */
    public Object remove(int index) {
        RangeCheck(index);

        modCount++;
        Object oldValue = elementData[index];

        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elementData, index + 1, elementData, index,
                             numMoved);
        }
        elementData[--size] = null; // Let gc do its work

        return oldValue;
    }

    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns.
     */
    public void clear() {
        modCount++;

        // Let gc do its work
        for (int i = 0; i < size; i++) {
            elementData[i] = null;
        }

        size = 0;
    }

    /**
     * Appends all of the elements in the specified Collection to the end of
     * this list, in the order that they are returned by the
     * specified Collection's Iterator.  The behavior of this operation is
     * undefined if the specified Collection is modified while the operation
     * is in progress.  (This implies that the behavior of this call is
     * undefined if the specified Collection is this list, and this
     * list is nonempty.)
     *
     * @param c the elements to be inserted into this list.
     * @throws    IndexOutOfBoundsException if index out of range <tt>(index
     *		  &lt; 0 || index &gt; size())</tt>.
     */
    public boolean addAll(Collection c) {
        modCount++;
        int numNew = c.size();
        ensureCapacity(size + numNew);

        Iterator e = c.iterator();
        for (int i = 0; i < numNew; i++) {
            elementData[size++] = (RdbData) e.next();
        }

        return numNew != 0;
    }

    /**
     * Inserts all of the elements in the specified Collection into this
     * list, starting at the specified position.  Shifts the element
     * currently at that position (if any) and any subsequent elements to
     * the right (increases their indices).  The new elements will appear
     * in the list in the order that they are returned by the
     * specified Collection's iterator.
     *
     * @param index index at which to insert first element
     *		    from the specified collection.
     * @param c elements to be inserted into this list.
     * @throws    IndexOutOfBoundsException if index out of range <tt>(index
     *		  &lt; 0 || index &gt; size())</tt>.
     */
    public boolean addAll(int index, Collection c) {
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException(
                "Index: " + index + ", Size: " + size);
        }

        int numNew = c.size();
        ensureCapacity(size + numNew); // Increments modCount!!

        int numMoved = size - index;
        if (numMoved > 0) {
            System.arraycopy(elementData, index, elementData, index + numNew,
                             numMoved);
        }

        Iterator e = c.iterator();
        for (int i = 0; i < numNew; i++) {
            elementData[index++] = (RdbData) e.next();
        }

        size += numNew;
        return numNew != 0;
    }

    /**
     * Removes from this List all of the elements whose index is between
     * fromIndex, inclusive and toIndex, exclusive.  Shifts any succeeding
     * elements to the left (reduces their index).
     * This call shortens the list by <tt>(toIndex - fromIndex)</tt> elements.
     * (If <tt>toIndex==fromIndex</tt>, this operation has no effect.)
     *
     * @param fromIndex index of first element to be removed.
     * @param toIndex index after last element to be removed.
     */
    protected void removeRange(int fromIndex, int toIndex) {
        modCount++;
        int numMoved = size - toIndex;
        System.arraycopy(elementData, toIndex, elementData, fromIndex,
                         numMoved);

        // Let gc do its work
        int newSize = size - (toIndex - fromIndex);
        while (size != newSize) {
            elementData[--size] = null;
        }
    }

    /**
     * Check if the given index is in range.  If not, throw an appropriate
     * runtime exception.
     */
    private void RangeCheck(int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException(
                "Index: " + index + ", Size: " + size);
        }
    }

}
