package amgen.ri.rdb;

/**
 * OraSequenceField
 * Description: Used to represent an Oracle sequence. For read-only data, this operates as a Number which
 *   uses the OraSequenceField(long value) contructor.
 *   When the OraSequence(String sequenceName, RdbData parent) is used, the getValue() method will invoke
 *   the RdbData.setOraSequenceData(OraSequenceField sequenceField) to set the value. This method
 *   requests next next value from Oracle in the sequence.
 * @author
 * @version 1.0
 */

public class OraSequenceField extends Number {
    private boolean dataSet;
    private RdbData parent;
    private String sequenceName;
    protected long value;

    /**
     * Create an Oracle sequence field. The sequenceName corresponds to an Oracle sequence which is used
     * to populate this object when its getValue() method is called.
     * @param sequenceName an Oracle sequence
     * @param parent the RdbData data object for which this is a class variable
     */
    public OraSequenceField(String sequenceName, RdbData parent) {
        this.sequenceName = sequenceName;
        this.parent = parent;
        dataSet = false;
    }

    /**
     * Creates a populated field. Operates just like a Number.
     * @param value the numeric value of the field
     */
    public OraSequenceField(long value) {
        this.value = value;
        dataSet = true;
    }

    /**
     * Creates a populated field. Operates just like a Number.
     * @param value the numeric value of the field
     */
    public OraSequenceField(String value) {
        this(Long.parseLong(value));
    }

    /**
     * Sets the value of the field
     */
    public void setValue(long value) {
        this.value = value;
        dataSet = true;
    }

    /**
     * Retrieves the value of the field. If this object was created by the
     * OraSequenceField(String sequenceName, RdbData parent) constructor, the
     * Oracle sequence is used to populate this object.
     */
    public long getValue() {
        if (!dataSet) {
            parent.setOraSequenceData(this);
        }
        return value;
    }

    /**
     * Retrieves the value of the field as a String. If this object was created by the
     * OraSequenceField(String sequenceName, RdbData parent) constructor, the
     * Oracle sequence is used to populate this object.
     */
    public String getValueAsString() {
        return Long.toString(getValue());
    }

    /**
     * Returns the name of the Oracle sequence
     */
    public String getSequenceName() {
        return sequenceName;
    }

    //Implementations from Number

    /**
     * Returns the value of the specified number as an <code>int</code>.
     * This may involve rounding.
     */
    public int intValue() {
        return (int) getValue();
    }

    /**
     * Returns the value of the specified number as a <code>long</code>.
     */
    public long longValue() {
        return getValue();
    }

    /**
     * Returns the value of the specified number as a <code>float</code>.
     */
    public float floatValue() {
        return (float) getValue();
    }

    /**
     * Returns the value of the specified number as a <code>double</code>.
     */
    public double doubleValue() {
        return (double) getValue();
    }

    public String toString() {
        return getValue() + "";
    }

    /** Returns an instance of this class parsing the given String value */
    public static OraSequenceField valueOf(String value, String sequenceName) {
        OraSequenceField oraSequenceField = new OraSequenceField(Long.parseLong(value));
        oraSequenceField.sequenceName = sequenceName;
        return oraSequenceField;
    }
}
