package amgen.ri.rdb.listener;

import amgen.ri.rdb.RdbData;

/**
 * <p> Dynamic Relation Db Mapping</p>
 * <p>Description: Event wrapper for listeners monitoring data set events</p>
 * @author Jeffrey McDowell
 * @version 1.0
 */

public class DataSetEvent {
    private RdbData source;
    private boolean success;

    public DataSetEvent(RdbData source, boolean success) {
        this.source = source;
        this.success = success;
    }

    public RdbData getSource() {
        return source;
    }

    public boolean isSuccess() {
        return success;
    }
}
