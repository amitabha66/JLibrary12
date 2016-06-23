/*
 *   ClobData
 *   Encapsulates any data member which is a Clob SQL type
 *   $Revision: 1.1 $
 *   Created: Jeffrey McDowell, 18 Oct 2000
 *   Modified: $Author: cvs $
 *   $Log
 *
 */
package amgen.ri.rdb;

import java.io.Reader;

/**
 * Encapsulates any data member which is a Clob SQL type
 *   @version $Revision: 1.1 $
 *   @author Jeffrey McDowell
 *   @author $Author: cvs $
 */
public class ClobData implements DeferredField {
    protected boolean dataSet;
    protected boolean sizeSet;
    protected RdbData parent;
    protected String fieldName;
    protected char[] data;
    protected long size = 0;
    protected Reader clobReader;

    /**
     * Default Constructor
     */
    public ClobData(RdbData parent, String fieldName) {
        this.parent = parent;
        this.fieldName = fieldName;
        dataSet = false;
        sizeSet = false;
    }

    /**
     * Default Constructor
     */
    public ClobData(Reader clobReader) {
        this.clobReader = clobReader;
        dataSet = true;
        sizeSet = true;
    }

    /**
     * Constructor which sets the content
     */
    public ClobData(String clobContent) {
        if (clobContent == null) {
            clobContent = "";
        }
        data = clobContent.toCharArray();
        size = data.length;
        dataSet = true;
        sizeSet = true;
    }

    /**
     * Constructor which sets the content to a zero-length char array
     */
    public ClobData() {
        data = new char[0];
        size = data.length;
        dataSet = true;
        sizeSet = true;
    }

    /** Get value for data */
    public char[] getData() {
        setData();
        return data;
    }

    /** Get value for data */
    public byte[] getDataAsBytes() {
        return toString().getBytes();
    }

    /** Sets the clob data
     *  Implmentation of DeferredField
     */
    public boolean setData() {
        if (!dataSet) {
            parent.setClobData(this);
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
            parent.setClobSize(this);
            sizeSet = true;
        }
        return size;
    }

    /** Set value for data */
    public void setData(char[] data) {
        dataSet = true;
        this.data = data;
    }

    /** Set value for data */
    public void setData(String data) {
        setData(data.toCharArray());
    }

    /** Set value for data size*/
    public void setSize(long dataSize) {
        sizeSet = true;
        this.size = dataSize;
    }

    /** Returns the CLOB as a string using a new String(byte[]) */
    public String toString() {
        return new String(getData());
    }

    /** Returns the CLOB's fieldName */
    public String getFieldName() {
        return fieldName;
    }

    /** Clears the field */
    public void clearField() {
        data = new char[0];
        size = data.length;
        dataSet = true;
        sizeSet = true;
    }

    public Reader getClobReader() {
        if (clobReader == null) {
            clobReader = new ClobReader(this);
        }
        return clobReader;
    }

    /** Returns an instance of this class parsing the given String value */
    public static ClobData valueOf(String value, String fieldName) {
        if (value == null) {
            return null;
        }
        ClobData obj = new ClobData(null, fieldName);
        obj.dataSet = true;
        obj.sizeSet = true;
        obj.data = value.toCharArray();
        obj.size = obj.data.length;
        return obj;
    }

}
