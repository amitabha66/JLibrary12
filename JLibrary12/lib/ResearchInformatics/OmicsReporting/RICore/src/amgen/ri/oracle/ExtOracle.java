package amgen.ri.oracle;

import amgen.ri.json.JSONObject;
import java.io.*;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.zip.GZIPInputStream;
import oracle.sql.BLOB;

import oracle.sql.CLOB;

/**
 * Set of static utility methods for interacting with Oracle JDBC
 *
 * @version $Id: ExtOracle.java,v 1.4 2012/10/10 23:02:05 cvs Exp $
 */
public class ExtOracle {
  /**
   * Method used to write the contents (data) from an Oracle CLOB column to a
   * Writer. This method uses one of two ways to get data from the CLOB column -
   * namely the getChars() method. The other way to read data from an Oracle
   * CLOB column is to use Streams.
   *
   * @see readCLOBToWriter(CLOB, OutputStream). Returns the an array of 2
   * integers- [<total characters read>, <total characters written>]
   *
   * @throws IOException
   * @throws SQLException
   * @param clob CLOB
   * @param writer Writer
   * @return int[]
   */
  public static int[] readCLOBToWriter(CLOB clob, Writer writer) throws IOException, SQLException {
    int totCharsRead = 0;
    int totCharsWritten = 0;
    if (clob != null && writer != null) {
      long clobLength = clob.length();
      int chunkSize = clob.getChunkSize();
      char[] textBuffer = new char[chunkSize];
      int charsRead = 0;

      for (long position = 1; position <= clobLength; position += chunkSize) {
        charsRead = clob.getChars(position, chunkSize, textBuffer);
        writer.write(textBuffer, 0, charsRead);
        totCharsRead += charsRead;
        totCharsWritten += charsRead;
      }
      writer.flush();
    }
    return new int[]{totCharsRead, totCharsWritten};
  }

  /**
   * Method used to write the contents (data) from an Oracle CLOB column to a
   * String.
   *
   * @param clob
   * @return
   * @throws IOException
   * @throws SQLException
   */
  public static String readCLOBToString(CLOB clob) throws IOException, SQLException {
    StringWriter writer = new StringWriter();
    readCLOBToWriter(clob, writer);
    return writer.toString();
  }

  /**
   * Method used to write the contents (data) from an Oracle CLOB column to a
   * stream. This method uses one of two ways to get data from the CLOB column -
   * namely the getAsciiStream() method. The other way to read data from an
   * Oracle CLOB column is to use characters.
   *
   * @see readCLOBToWriter(CLOB, Writer). Returns the an array of 2 integers-
   * [<total bytes read>, <total bytes written>]
   *
   * @throws IOException
   * @throws SQLException
   * @param clob CLOB
   * @param writer Writer
   * @return int[]
   */
  public static int[] readCLOBToWriter(CLOB clob, OutputStream outStream) throws IOException, SQLException {
    int totBytesRead = 0;
    int totBytesWritten = 0;
    if (clob != null && outStream != null) {
      int chunkSize;
      byte[] textBuffer;
      int bytesRead = 0;
      InputStream clobInputStream = clob.getAsciiStream();
      chunkSize = clob.getChunkSize();
      textBuffer = new byte[chunkSize];
      while ((bytesRead = clobInputStream.read(textBuffer)) != -1) {
        outStream.write(textBuffer, 0, bytesRead);
        totBytesRead += bytesRead;
        totBytesWritten += bytesRead;
      }
      outStream.flush();
    }
    return new int[]{totBytesRead, totBytesWritten};
  }

  /**
   * Creates a Blob from the bytes from the serialized obj
   *
   * @param conn
   * @param obj the Object to serialize and add to the Blob
   * @return
   * @throws SQLException
   */
  public static Blob createBlob(Connection conn, Object obj) throws SQLException {
    BLOB tempBlob = null;
    try {
      tempBlob = BLOB.createTemporary(conn, true, BLOB.DURATION_SESSION);
      OutputStream blobOutStream = tempBlob.setBinaryStream(0);
      ObjectOutputStream oop = new ObjectOutputStream(blobOutStream);
      oop.writeObject(obj);
      oop.flush();
      oop.close();
      blobOutStream.close();
    } catch (Exception exp) {
      tempBlob.freeTemporary();
      throw new SQLException(exp);
    }
    return tempBlob;
  }

  /**
   * Reads a JSONObject from a given Blob
   *
   * @param b
   * @return
   * @throws SQLException
   */
  public static JSONObject readJSONObjectFromBlob(Blob b, boolean gzipped) throws SQLException {
    return (JSONObject)readObjectFromBlob(b, gzipped);
  }
  


  /**
   * Reads a JSONObject from a given Blob
   *
   * @param b
   * @return
   * @throws SQLException
   */
  public static Object readObjectFromBlob(Blob b, boolean gzipped) throws SQLException {
    InputStream in = null;
    ByteArrayOutputStream bytesOut = null;
    try {
      in = b.getBinaryStream();
      bytesOut = new ByteArrayOutputStream();
      byte[] bytes = new byte[1025];
      int byteCount = -1;
      while ((byteCount = in.read(bytes)) > 0) {
        bytesOut.write(bytes, 0, byteCount);
      }
      in.close();
      bytesOut.close();
      ObjectInputStream objIN = (gzipped ? 
              new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(bytesOut.toByteArray())))
              : new ObjectInputStream(new ByteArrayInputStream(bytesOut.toByteArray())));
      return objIN.readObject();
    } catch (Exception e) {
      throw new SQLException(e);
    } finally {
      try {
        in.close();
      } catch (Exception e) {
      }
      try {
        bytesOut.close();
      } catch (Exception e) {
      }
    }
  }  
}
