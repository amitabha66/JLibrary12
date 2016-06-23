package amgen.ri.oracle;

import java.lang.reflect.Field;

import amgen.ri.rdb.RdbData;
import amgen.ri.rdb.SQLManagerIF;
import amgen.ri.util.ExtString;

/**
 * <p>Dynamic Relation Db Mapping</p>
 * <p>Description: Contains the column metadata</p>
 * @author Jeffrey McDowell
 * @version 1.0
 */

public class ColumnData extends RdbData {
    protected String table_name;
    protected String column_name;
    protected String owner;
    protected String data_type;
    protected String data_type_owner;
    protected int data_length;
    protected int data_precision;
    protected int data_scale;
    protected String nullable;
    protected String column_id;
    protected int default_length;
    protected int num_distinct;
    protected int char_col_decl_length;

    public ColumnData() {
        super();
    }

    public ColumnData(String table_name, String column_name, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        this.table_name = table_name;
        this.column_name = column_name;
    }

    public ColumnData(String table_nameANDcolumn_name, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        String[] elements = ExtString.splitCSV(table_nameANDcolumn_name);
        if (elements.length < 2) {
            throw new IllegalArgumentException("Insufficient arguments to instantiate class");
        }
        this.table_name = elements[0];
        this.column_name = elements[1];
    }

    public String getIdentifier() {
        return table_name + "," + column_name;
    }

    public String getTableName() {
        return "ALL_TAB_COLUMNS";
    }

    public boolean setData() {
        if (getSQLManager().getDatabaseProductName(getConnectionPool()).toUpperCase().indexOf("ORACLE") < 0) {
            throw new IllegalArgumentException("Table information only available on Oracle databases");
        }
        return super.setData();
    }

    public String[] getPrimaryKeyFields() {
        return new String[] {
            "table_name", "column_name"};
    }

    protected void setFieldValue(Field field, Object value) throws IllegalAccessException {
        field.set(this, value);
    }

    protected Object getFieldValue(Field field) throws IllegalAccessException {
        return field.get(this);
    }

    public String getTable_name() {
        return (String) get("table_name");
    }

    public String getColumn_name() {
        return (String) get("column_name");
    }

    public String getOwner() {
        return (String) get("owner");
    }

    public String getDataType() {
        return (String) get("data_type");
    }

    public String getDataTypeOwner() {
        return (String) get("data_type_owner");
    }

    public int getDataLength() {
        Number n = getAsNumber("data_length");
        return (n == null ? 0 : n.intValue());
    }

    public int getDataPrecision() {
        Number n = getAsNumber("data_precision");
        return (n == null ? 0 : n.intValue());
    }

    public int getCharacterColumnDeclaredLength() {
        Number n = getAsNumber("char_col_decl_length");
        return (n == null ? 0 : n.intValue());
    }

    public int getDataScale() {
        Number n = getAsNumber("data_scale");
        return (n == null ? 0 : n.intValue());
    }

    public String getNullable() {
        return (String) get("nullable");
    }

    public String getColumnID() {
        return (String) get("column_id");
    }

    public int getDefaultLength() {
        Number n = getAsNumber("default_length");
        return (n == null ? 0 : n.intValue());
    }

    public int getNumDistinct() {
        Number n = getAsNumber("num_distinct");
        return (n == null ? 0 : n.intValue());
    }
}
