/*
 *   LobSaveable
 *   Interface which defines method for inserting a Clob or Blob
 *   $Revision: 1.1 $
 *   Created: Jeffrey McDowell, 18 Sep 2000
 *   Modified: $Author: cvs $
 *   $Log
 *
 */
package amgen.ri.rdb;

import java.io.InputStream;
import java.io.Reader;

/**
 *   Interface which defines method for inserting a Clob or Blob
 *   Implementing class must define
 *   @version $Revision: 1.1 $
 *   @author Jeffrey McDowell
 *   @author $Author: cvs $
 */
public interface LobSaveable extends Saveable, Removeable {
    /** Returns the SQL statement which selects for the LOB */
    public String getSelectLobSQL(String fieldName);

    /** Returns a reader which will stream the Clob data */
    public Reader getClobReader(String fieldName);

    /** Returns an inputstream which will stream the Blob data */
    public InputStream getBlobStream(String fieldName);
}