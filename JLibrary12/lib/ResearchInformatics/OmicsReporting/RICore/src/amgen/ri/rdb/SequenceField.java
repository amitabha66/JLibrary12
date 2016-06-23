/*
 *   AlignmentTableItem
 *   $Revision: 1.1 $
 *   Created: Jeffrey McDowell, 25 Apr 2000
 *   Modified: $Author: cvs $
 *   $Log: SequenceField.java,v $
 *   Revision 1.1  2011/09/14 23:09:57  cvs
 *   no message
 *
 *   Revision 1.2  2009/05/11 23:28:52  cvs
 *   no message
 *
 *   Revision 1.1.1.1  2007/01/05 00:18:00  cvs
 *   My new CVS module.
 *
 *   Revision 1.4  2001/09/25 21:06:31  mcdowelj
 *   no message
 *
 *   Revision 1.3  2001/06/14 20:54:59  mcdowelj
 *   no message
 *
 *   Revision 1.2  2001/05/02 21:34:00  mcdowelj
 *   Added security
 *
 *   Revision 1.1.1.1  2001/03/05 21:08:18  mcdowelj
 *   no message
 *
 *   Revision 1.2  2000/10/18 20:54:32  mcdowelj
 *   Added CLOB and results (still need work on QueryManager)
 *
 *   Revision 1.1  2000/09/19 05:18:23  mcdowelj
 *   updates and data
 *
 *   Revision 1.1  2000/04/28 21:46:00  mcdowelj
 *   More work to Admin
 *
 *   Revision 1.2  2000/04/26 03:53:56  mcdowelj
 *   Worked ob admin stuff
 *
 *   Revision 1.1  2000/04/25 21:39:58  mcdowelj
 *   Changes
 *
 *
 */

package amgen.ri.rdb;

/**
 * Encapsulates the element of a sequence
 *
 * @version $Revision: 1.1 $
 * @author Jeffrey McDowell
 */
public class SequenceField implements DeferredField {
    private boolean dataSet;
    private RdbData parent;
    private String fieldName;
    private byte[] sequence;

    /**
     * Creates a new SequenceField object- defining the parent object
     */
    public SequenceField(RdbData parent, String fieldName) {
        this.parent = parent;
        this.fieldName = fieldName;
        dataSet = false;
    }

    /**
     * Returns the sequence as a byte array
     */
    public byte[] getSequence() {
        setData();
        return sequence;
    }

    /** Sets the clob data
     *  Implmentation of DeferredField
     */
    public boolean setData() {
        if (!dataSet) {
            parent.setSequenceData(this);
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

    /**
     * Sets the sequence
     */
    public void setSequence(byte[] sequence) {
        this.sequence = sequence;
        dataSet = true;
    }

    /**
     * Sets the sequence
     */
    public void setSequence(String sequence) {
        setSequence(sequence.getBytes());
    }

    /**
     * Returns the number of residues in the sequence
     */
    public int length() {
        return getSequence().length;
    }

    /**
     * Returns the sequence as a String
     */
    public String toString() {
        return new String(getSequence());
    }

    /** Returns the BLOB's fieldName */
    public String getFieldName() {
        return fieldName;
    }

    /** Returns a SequenceField object using the String as the sequence data */
    public static SequenceField valueOf(String s, String fieldName) {
        SequenceField sequenceField = new SequenceField(null, fieldName);
        sequenceField.sequence = s.getBytes();
        sequenceField.dataSet = true;
        return sequenceField;
    }

}
