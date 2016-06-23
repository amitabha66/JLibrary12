/*
 *   Removeable
 *   Interface which defines the SQL for deleting the entry
 *   $Revision: 1.1 $
 *   Created: Jeffrey McDowell, 18 Sep 2000
 *   Modified: $Author: cvs $
 *   $Log
 *
 */
package amgen.ri.rdb;

/**
 *   Interface which defines the SQL for deleting the entry
 *   @version $Revision: 1.1 $
 *   @author Jeffrey McDowell
 *   @author $Author: cvs $
 */
public interface Removeable {
    /**
     * Returns the DELETE statement for removing this entry from the database
     */
    public String getDeleteSQL();
}