/*
 *   Saveable
 *   Interface which defines 2 methods for inserting/updating data
 *   $Revision: 1.1 $
 *   Created: Jeffrey McDowell, 18 Sep 2000
 *   Modified: $Author: cvs $
 *   $Log
 *
 */
package amgen.ri.rdb;

/**
 * Interface which defines 2 methods for inserting/updating data
 * The performCommit() funtion will first attempt an update using the getUpdateSQL() then if that does not update
 * any rowsm it attempt an insert using the getInsertSQL()
 *   @version $Revision: 1.1 $
 *   @author Jeffrey McDowell
 *   @author $Author: cvs $
 */
public interface Saveable {
    /** Returns the SQL for INSERTing the object in the table */
    public String getInsertSQL();

    /** Returns the SQL for UPDATing the object in the table */
    public String getUpdateSQL();
}
