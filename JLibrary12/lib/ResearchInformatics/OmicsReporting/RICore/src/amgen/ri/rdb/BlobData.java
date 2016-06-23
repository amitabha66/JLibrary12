/*
 *   BlobData
 *   Encapsulates any data member which is a Blob SQL type
 *   $Revision: 1.1 $
 *   Created: Jeffrey McDowell, 18 Oct 2000
 *   Modified: $Author: cvs $
 *   $Log
 *
 */
package amgen.ri.rdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Encapsulates any data member which is a Blob SQL type
 *   @version $Revision: 1.1 $
 *   @author Jeffrey McDowell
 *   @author $Author: cvs $
 */
public class BlobData implements DeferredField {
    private boolean dataSet;
    private boolean sizeSet;
    private RdbData parent;
    private String fieldName;
    protected byte[] data;
    protected long size;

    /**
     * Default Constructor
     */
    public BlobData(RdbData parent, String fieldName) {
        this.parent = parent;
        this.fieldName = fieldName;
        dataSet = false;
        sizeSet = false;
    }

    /**
     * Constructor which sets the content to none (i.e. new byte[0])
     */
    public BlobData() {
        this(new byte[0]);
    }

    /**
     * Constructor which sets the content
     */
    public BlobData(byte[] blobContent) {
        if (blobContent == null) {
            blobContent = new byte[0];
        }
        data = blobContent;
        size = data.length;
        dataSet = true;
        sizeSet = true;
    }

    /**
     * Constructor which sets the content
     */
    public BlobData(String blobContent) {
        if (blobContent == null) {
            blobContent = "";
        }
        data = blobContent.getBytes();
        size = data.length;
        dataSet = true;
        sizeSet = true;

    }

    /**
     * Constructor which sets the content from the given stream
     *
     * @param blobStream the data stream
     * @throws IOException if there are any problems with the stream
     */
    public BlobData(InputStream blobStream) throws IOException {
        data = loadInputStream(blobStream);
        size = data.length;
        dataSet = true;
        sizeSet = true;
    }

    /**
     * Constructor which sets the content from the given File
     *
     * @param file the file to load
     * @throws IOException if there are any problems with the stream
     */
    public BlobData(File file) throws IOException {
        if (file != null) {
            data = loadInputStream(new FileInputStream(file));
        } else {
            data = new byte[0];
        }
        size = data.length;
        dataSet = true;
        sizeSet = true;
    }

    private byte[] loadInputStream(InputStream blobStream) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int read;
        while ( (read = blobStream.read(b)) > -1) {
            byteOut.write(b);
        }
        blobStream.close();
        return byteOut.toByteArray();
    }

    /** Get value for data */
    public byte[] getData() {
        setData();
        return data;
    }

    /** Writes the data to the given OutputStream */
    public long writeData(OutputStream stream) throws IOException {
        stream.write(getData());
        return getData().length;
    }

    /** Sets the clob data
     *  Implmentation of DeferredField
     */
    public boolean setData() {
        if (!dataSet) {
            parent.setBlobData(this);
            dataSet = true;
        }
        return dataSet;
    }

    /** Sets the clob data- same as setData()
     *  Implmentation of DeferredField
     */
    public void setAllData() {
        setData();
    }

    /** Return the length of the clob */
    public long getSize() {
        if (!sizeSet) {
            parent.setBlobSize(this);
            sizeSet = true;
        }
        return size;
    }

    /** Set value for data */
    public void setData(byte[] data) {
        dataSet = true;
        this.data = data;
    }

    /** Set value for data */
    public void setData(String data) {
        setData(data.getBytes());
    }

    /** Set value for data size*/
    public void setSize(long dataSize) {
        sizeSet = true;
        this.size = dataSize;
    }

    /** Returns the BLOB as a string using a new String(byte[]) */
    public String toString() {
        return new String(getData());
    }

    /** Returns the BLOB's fieldName */
    public String getFieldName() {
        return fieldName;
    }

    /** Returns an InputStream for reading the data in this BlobData item */
    public InputStream getBlobStream() {
        return new ByteArrayInputStream(getData());
    }

    /** Clears the field */
    public void clearField() {
        data = new byte[0];
        size = data.length;
        dataSet = true;
        sizeSet = true;
    }

    /** Returns an instance of this class parsing the given String value. The data is set using the String.getBytes() method */
    public static BlobData valueOf(String value, String fieldName) {
        BlobData obj = new BlobData(null, fieldName);
        obj.dataSet = true;
        obj.sizeSet = true;
        obj.data = value.getBytes();
        obj.size = obj.data.length;
        return obj;
    }

}
