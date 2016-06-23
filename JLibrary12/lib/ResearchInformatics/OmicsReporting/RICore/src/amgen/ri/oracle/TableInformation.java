package amgen.ri.oracle;

import java.lang.reflect.Field;
import java.sql.Date;

import amgen.ri.rdb.RdbData;
import amgen.ri.rdb.SQLManagerIF;

/**
 * <p> Dynamic Relation Db Mapping</p>
 * <p>Description: </p>
 * @author Jeffrey McDowell
 * @version 1.0
 */

public class TableInformation extends RdbData {

    protected String table_name;
    protected String tablespace_name;
    protected String cluster_name;
    protected String iot_name;
    protected int pct_free;
    protected int pct_used;
    protected int ini_trans;
    protected int max_trans;
    protected int initial_extent;
    protected int next_extent;
    protected int min_extents;
    protected int max_extents;
    protected int pct_increase;
    protected int freelists;
    protected int freelist_groups;
    protected String logging;
    protected String backed_up;
    protected int num_rows;
    protected int blocks;
    protected int empty_blocks;
    protected int avg_space;
    protected int chain_cnt;
    protected int avg_row_len;
    protected int avg_space_freelist_blocks;
    protected int num_freelist_blocks;
    protected String degree;
    protected String instances;
    protected String cache;
    protected String table_lock;
    protected int sample_size;
    protected Date last_analyzed;
    protected String partitioned;
    protected String iot_type;
    protected String temporary;
    protected String secondary;
    protected String nested;
    protected String buffer_pool;
    protected String row_movement;
    protected String global_stats;
    protected String user_stats;
    protected String duration;
    protected String skip_corrupt;
    protected String monitoring;

    public TableInformation() {
        super();
    }

    public TableInformation(String table_name, SQLManagerIF sqlManager, String logonusername, String connectionPool) {
        super(sqlManager, logonusername, connectionPool);
        this.table_name = table_name;
    }

    public String getIdentifier() {
        return table_name;
    }

    public String getTableName() {
        return "USER_TABLES";
    }

    public boolean setData() {
        if (getSQLManager().getDatabaseProductName(getConnectionPool()).toUpperCase().indexOf("ORACLE") < 0) {
            throw new IllegalArgumentException("Table information only available on Oracle databases");
        }
        return super.setData();
    }

    protected void setFieldValue(Field field, Object value) throws IllegalAccessException {
        field.set(this, value);
    }

    protected Object getFieldValue(Field field) throws IllegalAccessException {
        return field.get(this);
    }

    public String getTablenameForInformation() {
        return table_name;
    }

    public String getTablespaceName() {
        return (String) get("tablespace_name");
    }

    public String getClusterName() {
        return (String) get("cluster_name");
    }

    public String getIotName() {
        return (String) get("iot_name");
    }

    public int getPctFree() {
        return getAsNumber("pct_free").intValue();
    }

    public int getPctUsed() {
        return getAsNumber("pct_used").intValue();
    }

    public int getIniTrans() {
        return getAsNumber("ini_trans").intValue();
    }

    public int getMaxTrans() {
        return getAsNumber("max_trans").intValue();
    }

    public int getInitialExtent() {
        return getAsNumber("initial_extent").intValue();
    }

    public int getNextExtent() {
        return getAsNumber("next_extent").intValue();
    }

    public int getMinExtents() {
        return getAsNumber("min_extents").intValue();
    }

    public int getMaxExtents() {
        return getAsNumber("max_extents").intValue();
    }

    public int getPctIncrease() {
        return getAsNumber("pct_increase").intValue();
    }

    public int getFreeLists() {
        return getAsNumber("freelists").intValue();
    }

    public int getFreelistGroups() {
        return getAsNumber("freelist_groups").intValue();
    }

    public String getLogging() {
        return (String) get("logging");
    }

    public String getBackedUp() {
        return (String) get("backed_up");
    }

    public int getNumRows() {
        return getAsNumber("num_rows").intValue();
    }

    public int getBlocks() {
        return getAsNumber("blocks").intValue();
    }

    public int getEmptyBlocks() {
        return getAsNumber("empty_blocks").intValue();
    }

    public int getAvgSpace() {
        return getAsNumber("avg_space").intValue();
    }

    public int getChainCnt() {
        return getAsNumber("chain_cnt").intValue();
    }

    public int getAvgRowLen() {
        return getAsNumber("avg_row_len").intValue();
    }

    public int getAvgSpaceFreelistBlocks() {
        return getAsNumber("avg_space_freelist_blocks").intValue();
    }

    public int getNumFreeListBlocks() {
        return getAsNumber("num_freelist_blocks").intValue();
    }

    public String getDegree() {
        return (String) get("degree");
    }

    public String getInstances() {
        return (String) get("instances");
    }

    public String getCache() {
        return (String) get("cache");
    }

    public String getTableLock() {
        return (String) get("table_lock");
    }

    public int getSampleSize() {
        return getAsNumber("sample_size").intValue();
    }

    public Date getLastAnalyzed() {
        return (Date) get("last_analyzed");
    }

    public String getPartitioned() {
        return (String) get("partitioned");
    }

    public String getIotType() {
        return (String) get("iot_type");
    }

    public String getTemporary() {
        return (String) get("temporary");
    }

    public String getSecondary() {
        return (String) get("secondary");
    }

    public String getNested() {
        return (String) get("nested");
    }

    public String getBufferPool() {
        return (String) get("buffer_pool");
    }

    public String getRowMovement() {
        return (String) get("row_movement");
    }

    public String getGlobalStats() {
        return (String) get("global_stats");
    }

    public String getUserStats() {
        return (String) get("user_stats");
    }

    public String getDuration() {
        return (String) get("duration");
    }

    public String getSkipCorrupt() {
        return (String) get("skip_corrupt");
    }

    public String getMonitoring() {
        return (String) get("monitoring");
    }

}
