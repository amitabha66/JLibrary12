package amgen.ri.rdb.build;

import java.io.BufferedWriter;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import amgen.ri.oracle.OraSQLManager;
import amgen.ri.util.ExtString;

public class RdbDataBuilder {
    private String connectionPool;
    private char dollar = '$';
    private BufferedWriter writer;
    private String packagePath;
    private String sourceName;
    private String className;

    public RdbDataBuilder(String connectionPool) {
        this.connectionPool = connectionPool;
    }

    public String buildClass(String className, String packageName, String schemaOwner, String tableName,
                             String author, boolean addGetters, boolean addSetters,
                             boolean makeSaveable, boolean makeRemoveable, String description) throws
        Exception {

        RdbDataParameter[] parameters = getParameters(schemaOwner, tableName);
        return buildClass(className, packageName, parameters, schemaOwner, tableName, author, addGetters, addSetters, makeSaveable, makeRemoveable, description);
    }

    public String buildClass(String _className, String packageName, RdbDataParameter[] rdbDataParameters,
                             String schemaOwner, String tableName,
                             String author,
                             boolean addGetters, boolean addSetters,
                             boolean makeSaveable, boolean makeRemoveable, String description) throws
        Exception {

        className = _className;
        ArrayList includedParameterList = new ArrayList();
        for (int i = 0; i < rdbDataParameters.length; i++) {
            if (rdbDataParameters[i].includeParameter()) {
                includedParameterList.add(rdbDataParameters[i]);
            }
        }
        RdbDataParameter[] parameters = (RdbDataParameter[]) includedParameterList.toArray(new RdbDataParameter[0]);
        RdbDataParameter primaryKeyParameter = null;
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isPrimaryKey()) {
                primaryKeyParameter = parameters[i];
            }
        }
        if (primaryKeyParameter == null) {
            primaryKeyParameter = parameters[0];
        }

        if (parameters.length == 0) {
            throw new IllegalArgumentException("No valid parameters");
        }
        boolean hasLob = false;
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isLOB()) {
                hasLob = true;
            }
        }
        if (className == null || className.trim().length() == 0) {
            className = getClassName(tableName);
        }

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        writer.print(getHeader(this.className, description, author));
        if (packageName != null) {
            writer.println("package " + packageName + ";\n");
        }
        writer.println("");
        writer.println("import java.lang.reflect.Field;");
        if (hasLob) {
            writer.println("import java.io.Reader;");
            writer.println("import java.io.InputStream;");

        }
        writer.println("import amgen.ri.rdb.*;");

        writer.println("");
        writer.println("/**\n *   " + description + "\n *   @version " + dollar +
                       "Revision$\n *   @author " + author + "\n *   @author " +
                       dollar + "Author$\n */");
        writer.print("public class " + className);
        writer.print(" extends RdbData");
        if (makeSaveable) {
            writer.print(" implements " + (hasLob ? "LobSaveable" : "Saveable"));
        }
        if (makeSaveable && makeRemoveable) {
            writer.print(", " + "Removeable");

        } else if (makeRemoveable) {
            writer.print(" implements " + "Removeable");
        }
        writer.println(" {");

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getDeclaredName().length() == 0) {
                break;
            }
            writer.println("    " + getProtection(parameters[i]) + " " +
                           parameters[i].getDeclaredType() + " " +
                           parameters[i].getDeclaredName() + ";");
        }
        writer.println("");

        writer.println("    /**\n     * Default Constructor\n     */");
        writer.println("    public " + className + "() {");
        writer.println("        super();");
        writer.println("    }");

        writer.println("    /**\n     * RdbData Constructor\n     */");
        String parameterName = primaryKeyParameter.getDeclaredName();
        writer.println("    public " + className + "(String " + parameterName +
                       ", SQLManagerIF sqlManager, String logonusername, String connectionPool) {");
        writer.println(
            "        super(sqlManager, logonusername, connectionPool);");
        writer.println("        this." + parameterName + "= " +
                       primaryKeyParameter.getFromStringFunction() + ";");
        writer.println("    }");

        if (makeSaveable) {
            writer.println(
                "    /**\n     * Constructor which sets the class variables\n     */");
            writer.print("    public " + className + "(");
            for (int i = 0; i < parameters.length; i++) {
                writer.print(parameters[i].getDeclaredType() + " " +
                             parameters[i].getDeclaredName());
                if (i < parameters.length - 1) {
                    writer.print(", ");
                }
            }
            writer.print(
                ", SQLManagerIF sqlManager, String logonusername, String connectionPool");

            writer.println(") {");
            writer.println(
                "        super(sqlManager, logonusername, connectionPool);");
            writer.println("        setValues(new Object[] {");
            for (int i = 0; i < parameters.length; i++) {
                writer.println("                  " +
                               parameters[i].getAsWrapper() + ",");
            }
            writer.println("            }");
            writer.println("        );");
            writer.println("    }");
        }
        writer.println("");

        writer.println("    /** A required method which returns the primary key(s) of the table/RdbData class. */");
        writer.println("    public String getIdentifier() {");
        writer.println("        return " + primaryKeyParameter.getToStringFunction() +
                       ";");
        writer.println("    }");
        writer.println("    /** This method is required EXACTLY as written to allow the RdbData architecture access to the class variables. */");
        writer.println("    protected void setFieldValue(Field field, Object value) throws IllegalAccessException {");
        writer.println("        field.set(this, value);");
        writer.println("    }");
        writer.println("    /** This method is required EXACTLY as written to allow the RdbData architecture access to the class variables. */");
        writer.println("    protected Object getFieldValue(Field field) throws IllegalAccessException {");
        writer.println("        return field.get(this);");
        writer.println("    }");

        if (!tableName.equalsIgnoreCase(className)) {
            writer.println("    /** This method returns the name of the table. */");
            writer.println("    protected String getTableName() {");
            writer.println("        return \"" + tableName.toUpperCase() + "\";");
            writer.println("    }");

        }

        writer.println("    /**");
        writer.println("     * Returns the primary key fields for the RdbData class which are used to");
        writer.println("     * generate the SQL statement(s). If this returns null (the Default), the first field of the class is assumed.");
        writer.println("     * The getIdentifier() method must return, in CSV, the matching number of elements as this array if generated SQL is used.");
        writer.println("     */");
        writer.println("    public String[] getPrimaryKeyFields() {");
        writer.println("        return new String[] {\"" + primaryKeyParameter.getDeclaredName() + "\"};");
        writer.println("    }");

        if (makeSaveable) {
            writer.println(
                "    /** Returns the SQL for INSERTing the object in the table */");
            writer.println("    public String getInsertSQL() {");
            writer.println("        return null;");
            writer.println("    }");
            writer.println(
                "    /** Returns the SQL for UPDATing the object in the table */");
            writer.println("    public String getUpdateSQL() {");
            writer.println("        return null;");
            writer.println("    }");
            if (hasLob) {
                writer.println(
                    "    /** Returns the SQL for DELTEing the object/row in the table */");
                writer.println("    public String getDeleteSQL() {");
                writer.println("        return null;");
                writer.println("    }");
                writer.println(
                    "    /** Returns the SQL statement which selects for the LOB */");
                writer.println(
                    "    public String getSelectLobSQL(String fieldName) {");
                writer.println("        return null;");
                writer.println("    }");
                writer.println(
                    "    /** Returns a reader which will stream the Clob data */");
                writer.println(
                    "    public Reader getClobReader(String fieldName) {");
                int countClobs = 0;
                for (int i = 0; i < parameters.length; i++) {
                    if (parameters[i].isCLOB()) {
                        if (countClobs == 0) {
                            writer.println("        if (fieldName.equals(\"" +
                                           parameters[i].getDeclaredName() + "\")) {");
                        } else {
                            writer.println(
                                "        } else if (fieldName.equals(\"" +
                                parameters[i].getDeclaredName() + "\")) {");
                        }
                        writer.println("            return " +
                                       parameters[i].getLobStreamer() + ";");
                        countClobs++;
                    }
                }
                if (countClobs > 0) {
                    writer.println("        }");
                }
                writer.println("        return null;");
                writer.println("    }");
                writer.println(
                    "    /** Returns an inputstream which will stream the Blob data */");
                writer.println(
                    "    public InputStream getBlobStream(String fieldName) {");
                int countBlobs = 0;
                for (int i = 0; i < parameters.length; i++) {
                    if (parameters[i].isBLOB()) {
                        if (countBlobs == 0) {
                            writer.println("        if (fieldName.equals(\"" +
                                           parameters[i].getDeclaredName() + "\")) {");
                        } else {
                            writer.println(
                                "        } else if (fieldName.equals(\"" +
                                parameters[i].getDeclaredName() + "\")) {");
                        }
                        writer.println("            return " +
                                       parameters[i].getLobStreamer() + ";");
                        countBlobs++;
                    }
                }
                if (countBlobs > 0) {
                    writer.println("        }");
                }
                writer.println("        return null;");
                writer.println("    }");

            } else {

            }
        }
        //Add the removable implementations. If saveable && hasLob, this is already done.
        if (makeRemoveable) {
            if (!makeSaveable || !hasLob) {
                writer.println(
                    "    /** Returns the SQL for DELTEing the object/row in the table */");
                writer.println("    public String getDeleteSQL() {");
                writer.println("        return null;");
                writer.println("    }");
            }
        }

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getDeclaredName().length() == 0) {
                break;
            }
            if (addGetters) {
                writer.println("    /** Get value for " +
                               parameters[i].getDeclaredName() + " */");
                writer.println("    public " + parameters[i].getDeclaredType() +
                               " get" +
                               Character.toUpperCase(parameters[i].getDeclaredName().
                    charAt(0)) + parameters[i].getDeclaredName().substring(1) + "() {");
                writer.println("        return " + parameters[i].getGetFuction() +
                               ";");
                writer.println("    }");
            }
            if (addSetters) {
                writer.println("    /** Set value for " +
                               parameters[i].getDeclaredName() + " */");
                writer.println("    public void set" +
                               Character.toUpperCase(parameters[i].getDeclaredName().
                    charAt(0)) + parameters[i].getDeclaredName().substring(1) + "(" +
                               parameters[i].getDeclaredType() + " " +
                               parameters[i].getDeclaredName() + ") {");
                writer.println("        set(\"" + parameters[i].getDeclaredName() +
                               "\", " + parameters[i].getAsWrapper() + ");");
                writer.println("    }");
            }

        }
        writer.println("");
        writer.println("}");
        writer.close();
        packagePath = packageName.replaceAll("\\.", "/");
        sourceName = className + ".java";

        return stringWriter.toString();
    }

    /**
     * Returns the relative path derived from the package for the last built source.
     * @return
     */
    public String getPackagePath() {
        return packagePath;
    }

    /**
     * Returns the source File for the last built source.
     * @return
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * Returns the class name for the last built source.
     * @return
     */
    public String getClassName() {
        return className;
    }

    protected String getProtection(RdbDataParameter parameter) {
        return "protected";
    }

    protected String getClassName(String tableName) {
        String[] parts = ExtString.split(tableName, '_');
        String fullName = Character.toUpperCase(parts[0].charAt(0)) +
            parts[0].substring(1).toLowerCase();
        for (int i = 1; i < parts.length; i++) {
            fullName += "_" + Character.toUpperCase(parts[i].charAt(0)) +
                parts[i].substring(1).toLowerCase();
        }
        return fullName;
    }

    public RdbDataParameter[] getParameters(String schemaName, String tableName) throws
        Exception {
        ArrayList parameterList = new ArrayList();
        OraSQLManager sqlManager = new OraSQLManager();
        Connection conn = sqlManager.getConnection(connectionPool);

        // Get DatabaseMetaData
        DatabaseMetaData dbmd = conn.getMetaData();

        ResultSet rs = dbmd.getColumns(null, schemaName.toUpperCase(),
                                       tableName.toUpperCase(), "%");
        int parameterCount = 0;
        // Printout table data
        while (rs.next()) {
            parameterCount++;
            // Get dbObject metadata
            String dbObjectCatalog = rs.getString(1);
            String dbObjectSchema = rs.getString(2);
            String dbObjectName = rs.getString(3);
            String dbColumnName = rs.getString(4);
            String dbColumnTypeName = rs.getString(6);
            int dbColumnSize = rs.getInt(7);
            int dbDecimalDigits = rs.getInt(9);
            String dbColumnDefault = rs.getString(13);
            int dbOrdinalPosition = rs.getInt(17);
            String dbColumnIsNullable = rs.getString(18);

            parameterList.add(new RdbDataParameter( (parameterCount == 1), dbColumnName, dbColumnTypeName,
                dbColumnSize, dbDecimalDigits, dbColumnIsNullable));
        }

        // Free database resources
        rs.getStatement().close();
        if (parameterList.size() == 0) {
            conn.close();
            throw new IllegalArgumentException("Unable to gather table information for " + schemaName + "." + tableName);
        }
        rs = dbmd.getPrimaryKeys(null, schemaName.toUpperCase(), tableName.toUpperCase());
        int primaryKeyCount = 0;
        while (rs.next()) {
            String dbObjectCatalog = rs.getString(1);
            String dbObjectSchema = rs.getString(2);
            String dbObjectName = rs.getString(3);
            String dbColumnName = rs.getString(4);
            int dbKeySequence = rs.getInt(5);
            String dbPKName = rs.getString(6);

            for (int i = 0; i < parameterList.size(); i++) {
                RdbDataParameter parameter = (RdbDataParameter) parameterList.get(i);
                if (parameter.getColumnName().equalsIgnoreCase(dbColumnName)) {
                    primaryKeyCount++;
                    parameter.setPrimaryKey(true);
                } else {
                    parameter.setPrimaryKey(false);
                }
            }
        }
        if (primaryKeyCount == 0) {
            RdbDataParameter parameter = (RdbDataParameter) parameterList.get(0);
            parameter.setPrimaryKey(true);
        }

        rs.close();
        conn.close();

        return (RdbDataParameter[]) parameterList.toArray(new RdbDataParameter[0]);
    }

    public String[] getTables(String schemaName) throws
        Exception {
        ArrayList tableList = new ArrayList();
        OraSQLManager sqlManager = new OraSQLManager();
        Connection conn = sqlManager.getConnection(connectionPool);

        // Get DatabaseMetaData
        DatabaseMetaData dbmd = conn.getMetaData();

        ResultSet rs = dbmd.getTables(null, schemaName.toUpperCase(), "%", new String[] {"TABLE", "VIEW"});

        while (rs.next()) {
            String dbObjectCatalog = rs.getString(1);
            String dbObjectSchema = rs.getString(2);
            String dbObjectName = rs.getString(3);
            String dbObjectType = rs.getString(4);
            String dbObjectRemarks = rs.getString(5);
            tableList.add(dbObjectName);
        }
        rs.getStatement().close();
        return (String[]) tableList.toArray(new String[0]);
    }

    public static File getClassSourceFile(String sourceDir,
                                          String fullClassName) {
        if (sourceDir.length() == 0) {
            return null;
        }
        int lastDot = fullClassName.lastIndexOf('.');
        String packageName = null;
        if (lastDot > 0) {
            packageName = fullClassName.substring(0, lastDot);
        }
        return new File(new File(sourceDir),
                        fullClassName.replace('.', File.separatorChar) +
                        ".java");
    }

    private String getHeader(String className, String description,
                             String author) {
        LineStringBuffer sb = new LineStringBuffer("/*");
        sb.add(" *   " + className);
        sb.add(" *   " + description);
        sb.add(" *   " + dollar + "Revision$");
        sb.add(" *   Created: " + author + ", " + getDate());
        sb.add(" *   Modified: " + dollar + "Author$");
        sb.add(" *   $Log");
        sb.add(" *");
        sb.add(" */");
        return sb.toString();
    }

    private String getDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
        return formatter.format(new java.util.Date());
    }

}

class LineStringBuffer {
    private StringBuffer sb;
    public LineStringBuffer() {
        sb = new StringBuffer();
    }

    public LineStringBuffer(String s) {
        this();
        add(s);
    }

    public void add(String s) {
        sb.append(s);
        sb.append('\n');
    }

    public String toString() {
        return sb.toString();
    }
}
