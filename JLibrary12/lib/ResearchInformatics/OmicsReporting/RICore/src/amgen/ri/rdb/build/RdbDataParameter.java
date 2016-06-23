package amgen.ri.rdb.build;

import amgen.ri.rdb.Register;

public class RdbDataParameter {
    private boolean includeParameter;
    private boolean isPrimaryKey;
    private String columnName;
    private String columnTypeName;
    private short columnType;
    private int columnSize;
    private int decimalDigits;
    private boolean addGet;
    private boolean addSet;
    private boolean isNullable;

    public RdbDataParameter(boolean isPrimaryKey, String columnName, String columnTypeName, int columnSize, int decimalDigits, String dbColumnIsNullable) {
        this.includeParameter = true;
        this.isPrimaryKey = isPrimaryKey;
        this.columnName = columnName;
        this.columnTypeName = columnTypeName;
        this.columnSize = columnSize;
        this.columnType = columnType;
        this.decimalDigits = decimalDigits;
        this.addGet = addGet;
        this.addSet = addSet;

        if (columnTypeName.equalsIgnoreCase("VARCHAR2")) {
            columnType = Register.STRING;
        } else if (columnTypeName.equalsIgnoreCase("VARCHAR")) {
            columnType = Register.STRING;
        } else if (columnTypeName.equalsIgnoreCase("FLOAT")) {
            columnType = Register.DOUBLE;
        } else if (columnTypeName.equalsIgnoreCase("NUMBER")) {
            if (decimalDigits > 0) {
                columnType = Register.DOUBLE;
            } else if (columnSize > 32) {
                columnType = Register.LONG;
            } else {
                columnType = Register.INTEGER;
            }
        } else if (columnTypeName.equalsIgnoreCase("DATE")) {
            columnType = Register.TIMESTAMP;
        } else if (columnTypeName.equalsIgnoreCase("CLOB")) {
            columnType = Register.CLOBDATA;
        } else if (columnTypeName.equalsIgnoreCase("BLOB")) {
            columnType = Register.BLOBDATA;
        } else if (columnTypeName.equalsIgnoreCase("XML")) {
            columnType = Register.XMLDATA;
        } else {
            columnType = Register.STRING;
        }
        if (dbColumnIsNullable != null) {
            this.isNullable = (dbColumnIsNullable.equalsIgnoreCase("YES"));
        } else {
            this.isNullable = true;
        }
    }

    public boolean isNumber() {
        switch (columnType) {
            case (Register.DOUBLE):
            case (Register.LONG):
            case (Register.INTEGER):
                return true;
            default:
                return false;
        }
    }

    public boolean isNullable() {
        return isNullable;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnTypeName() {
        return columnTypeName;
    }

    public String getColumnFullTypeName() {
        String columnTypeName = getColumnTypeName();
        if (columnTypeName.toUpperCase().startsWith("NUMBER")) {
            if (decimalDigits > 0) {
                return columnTypeName.toUpperCase() + "(" + columnSize + "," + decimalDigits + ")";
            } else {
                return columnTypeName.toUpperCase() + "(" + columnSize + ")";
            }
        } else if (columnTypeName.toUpperCase().startsWith("VARCHAR")) {
            return columnTypeName.toUpperCase() + "(" + columnSize + ")";
        }
        return columnTypeName;
    }

    public short getColumnType() {
        return columnType;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public int getDecimalDigits() {
        return decimalDigits;
    }

    public boolean isLOB() {
        return (isCLOB() || isBLOB());
    }

    public boolean isCLOB() {
        switch (columnType) {
            case (Register.CLOBDATA):
                return true;
            default:
                return false;
        }
    }

    public boolean isBLOB() {
        switch (columnType) {
            case (Register.BLOBDATA):
                return true;
            default:
                return false;
        }
    }

    public boolean isXML() {
        switch (columnType) {
            case (Register.XMLDATA):
                return true;
            default:
                return false;
        }
    }

    public String getDeclaredName() {
        return columnName.toLowerCase();
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    public boolean includeParameter() {
        return includeParameter;
    }

    public void setIncludeParameter(boolean includeParameter) {
        this.includeParameter = includeParameter;
    }

    public String getDeclaredType() {
        switch (columnType) {
            case (Register.INTEGER):
                return "int";
            case (Register.LONG):
                return "long";
            case (Register.DOUBLE):
                return "double";
            case (Register.TIMESTAMP):
                return "java.sql.Date";
            case (Register.CLOBDATA):
                return "ClobData";
            case (Register.BLOBDATA):
                return "BlobData";
            case (Register.XMLDATA):
                return "XMLData";
            default:
                return "String";
        }
    }

    public String getControlFileType() {
        switch (columnType) {
            case (Register.INTEGER):
                return "INTEGER EXTERNAL";
            case (Register.LONG):
                return "INTEGER EXTERNAL";
            case (Register.DOUBLE):
                return "DECIMAL EXTERNAL";
            case (Register.TIMESTAMP):
                return "DATE";
            default:
                return "CHAR";
        }
    }

    public String getFromStringFunction() {
        switch (columnType) {
            case (Register.INTEGER):
                return "Integer.parseInt(" + getDeclaredName() + ")";
            case (Register.LONG):
                return "Long.parseLong(" + getDeclaredName() + ")";
            case (Register.DOUBLE):
                return "Double.parseDouble(" + getDeclaredName() + ")";
            case (Register.TIMESTAMP):
                return "java.sql.Date.valueOf(" + getDeclaredName() + ")";
            case (Register.CLOBDATA):
                return "ClobData.parseDouble(" + getDeclaredName() + ")";
            case (Register.BLOBDATA):
                return "BlobData.parseDouble(" + getDeclaredName() + ")";
            default:
                return getDeclaredName();
        }
    }

    public String getToStringFunction() {
        switch (columnType) {
            case (Register.STRING):
                return getDeclaredName();
            default:
                return getDeclaredName() + "+\"\"";
        }
    }

    public String getAsWrapper() {
        switch (columnType) {
            case (Register.INTEGER):
                return "new Integer(" + getDeclaredName() + ")";
            case (Register.LONG):
                return "new Long(" + getDeclaredName() + ")";
            case (Register.DOUBLE):
                return "new Double(" + getDeclaredName() + ")";
            default:
                return getDeclaredName();
        }
    }

    public String getGetFuction() {
        switch (columnType) {
            case (Register.INTEGER):
                return "getAsNumber(\"" + getDeclaredName() + "\"" + (isPrimaryKey ? ", false" : "") + ").intValue()";
            case (Register.LONG):
                return "getAsNumber(\"" + getDeclaredName() + "\"" + (isPrimaryKey ? ", false" : "") + ").longValue()";
            case (Register.DOUBLE):
                return "getAsNumber(\"" + getDeclaredName() + "\"" + (isPrimaryKey ? ", false" : "") + ").doubleValue()";
            case (Register.CLOBDATA):
                return "(ClobData)get(\"" + getDeclaredName() + "\"" + (isPrimaryKey ? ", false" : "") + ")";
            case (Register.BLOBDATA):
                return "(BlobData)get(\"" + getDeclaredName() + "\"" + (isPrimaryKey ? ", false" : "") + ")";
            case (Register.TIMESTAMP):
                return "(java.sql.Date)get(\"" + getDeclaredName() + "\"" + (isPrimaryKey ? ", false" : "") + ")";
            default:
                return "(String)get(\"" + getDeclaredName() + "\"" + (isPrimaryKey ? ", false" : "") + ")";
        }
    }

    public String getLobStreamer() {
        switch (columnType) {
            case (Register.CLOBDATA):
                return getDeclaredName() + ".getClobReader()";
            case (Register.BLOBDATA):
                return getDeclaredName() + ".getBlobStream()";
            default:
                return null;
        }
    }

}
