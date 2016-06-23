package amgen.ri.rdb.listener;

import java.sql.SQLException;

import amgen.ri.rdb.RdbData;

/**
 * <p> Dynamic Relation Db Mapping</p>
 * <p>Description: SQLExceptionListener are listeners which are notified if a SQLException
 * is thrown during the execution of a SQL command in an RdbData class</p>
 * @author Jeffrey McDowell
 * @version 1.0
 */

public interface SQLExceptionListener {
    /**
     * Called if a SQLException is thrown. Return whether to display the
     * exception information to the log.
     * @param rdbData the RdbData object which the SQLException is thrown
     * @param e the SQLException which was thrown
     * @return if true, log data is displayed. if false, nothing is displayed
     */
    public boolean sqlExceptionThrown(RdbData rdbData, SQLException sqlException);
}
