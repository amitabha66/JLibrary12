package amgen.ri.utils;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import oracle.sql.BLOB;

import org.jdom.Document;

/**
 * Utils for use in Java Stored Procedures
 * @version $Id
 */
public class ExtJProc {

    /**
     * Returns the default Connection for use in a JProc
     *
     * @return Connection
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:default:connection:");
    }

    /**
     * Returns Clob as a Java String
     *
     * @param clobInData Clob
     * @return String
     * @throws SQLException
     */
    public static String getString(Clob clobInData) throws SQLException {
        String stringClob = null;
        long i = 1;
        int clobLength = (int) clobInData.length();
        stringClob = clobInData.getSubString(i, clobLength);
        return stringClob;
    }

    /**
     * Creates a GZipped, serialized JDOM Document object as a Blob
     *
     * @param conn Connection
     * @param doc Document
     * @return BLOB
     * @throws Exception
     */
    public static Blob createGZippedSerializedDocumentBlob(Connection conn, Document doc) throws Exception {
        BLOB tempBlob = null;
        try {
            tempBlob = BLOB.createTemporary(conn, true, BLOB.DURATION_SESSION);
            OutputStream blobOutStream = tempBlob.setBinaryStream(0);
            ObjectOutputStream oop = new ObjectOutputStream(new GZIPOutputStream(blobOutStream));
            oop.writeObject(doc);
            oop.flush();
            oop.close();
            blobOutStream.close();
        } catch (Exception exp) {
            tempBlob.freeTemporary();
            throw exp;
        }
        return tempBlob;
    }

    /**
     * deserializeGZipBlobToDocument
     *
     * @param blob Blob
     * @return Document
     * @throws Exception
     */
    public static Document deserializeGZipBlobToDocument(Blob blob) throws Exception {
        InputStream is = null;
        try {
            is = new GZIPInputStream(blob.getBinaryStream());
            ObjectInputStream oip = new ObjectInputStream(is);
            return (Document) oip.readObject();
        } finally {
            if (is!= null) {
                is.close();
            }
        }
    }
}
