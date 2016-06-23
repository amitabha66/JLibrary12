package amgen.ri.oracle;

import java.util.HashMap;

import amgen.ri.rdb.RdbData;
import amgen.ri.rdb.RdbDataArray;
import amgen.ri.rdb.SQLManagerIF;

/**
 * <p>Dynamic Relation Db Mapping</p>
 * <p>Description: Contains MetaData information regarding the given table or RdbData class </p>
 * @author Jeffrey McDowell
 * @version 1.0
 */

public class MetaData {
    protected RdbData rdbData;
    protected RdbDataArray columnData;
    protected HashMap columnDataMap;
    protected TableInformation tableInformation;

    public MetaData() {}

    /**
     * Creates a MetaData object for the RdbData class
     * @param rdbData
     * @param computeStatistics whether to issue a ANALYZE statement to compute the statistics on the table prior to creating the TableInformation object
     * @param sqlManager
     * @param logonusername
     * @param connectionPool
     */
    public MetaData(RdbData rdbData, boolean computeStatistics, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        this(rdbData.getTableNameForSQL(), computeStatistics, sqlManager, logonusername, connectionPool);
        this.rdbData = rdbData;
    }

    /**
     * Creates a MetaData object for the given table
     * @param table_name
     * @param computeStatistics whether to issue a ANALYZE statement to compute the statistics on the table prior to creating the TableInformation object
     * @param sqlManager
     * @param logonusername
     * @param connectionPool
     */
    public MetaData(String table_name, boolean computeStatistics, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        if (computeStatistics) {
            try {
                String sql = "ANALYZE TABLE " + table_name + " COMPUTE STATISTICS";
                sqlManager.executeUpdate(sql, connectionPool);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        columnData = new RdbDataArray(ColumnData.class, "table_name", table_name.toUpperCase(), sqlManager, logonusername, connectionPool);
        tableInformation = new TableInformation(table_name.toUpperCase(), sqlManager, logonusername, connectionPool);
        setColumnMap();
    }

    /**
     * Returns the RdbData object for this object, if available. null otherwise
     * @return
     */
    public RdbData getRdbData() {
        return rdbData;
    }

    /**
     * Returns the ColumnData for each column
     * @return
     */
    public ColumnData[] getColumnData() {
        return (ColumnData[]) columnData.getItems(ColumnData.class);
    }

    /**
     * Returns the ColumnData for a column
     * @return
     */
    public ColumnData getColumnData(String columnName) {
        return (ColumnData) columnDataMap.get(columnName.toUpperCase());
    }

    /**
     * Returns the TableInformation for the table
     * @return
     */
    public TableInformation getTableInformation() {
        return tableInformation;
    }

    private void setColumnMap() {
        columnDataMap = new HashMap();
        ColumnData[] columnData = getColumnData();
        for (int i = 0; i < columnData.length; i++) {
            columnDataMap.put(columnData[i].getColumn_name().toUpperCase(), columnData[i]);
        }
    }
}
