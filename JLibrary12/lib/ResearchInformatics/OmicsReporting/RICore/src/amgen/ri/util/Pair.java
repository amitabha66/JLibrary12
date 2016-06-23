/**
 * Simple name/value pair class
 *   @version $Revision: 1.1 $ $Date: 2011/10/26 04:13:32 $
 *   @author Jeffrey McDowell
 *   @author $Author $
 *
 */
package amgen.ri.util;

import java.io.Serializable;

public class Pair implements Serializable, Comparable {

    protected Object name;
    protected int intName;
    protected Object value;
    protected int intValue;

    /**
     * Default contructor
     */
    public Pair() {}

    /**
     * Populates name/value pairs
     */
    public Pair(String nameValuePair) {
        String[] nv = ExtString.split(nameValuePair, '=');
        if (nv.length >= 2) {
            name = nv[0];
            value = nv[1];
        } else if (nv.length == 1) {
            name = nv[0];
            value = "";
        }
    }

    /**
     * Populates name/value pairs
     */
    public Pair(Object _name, Object _value) {
        name = _name;
        value = _value;
    }

    /**
     * Populates name/int value pairs
     */
    public Pair(Object _name, int _value) {
        name = _name;
        intValue = _value;
        value = new Integer(_value);
    }

    /**
     * Populates name/value pairs
     */
    public Pair(int _name, Object _value) {
        intName = _name;
        value = _value;
        name = new Integer(_name);
    }

    /**
     * Populates name/int value pairs
     */
    public Pair(int _name, int _value) {
        intName = _name;
        intValue = _value;
        name = new Integer(_name);
        value = new Integer(_value);

    }

    /**
     * Returns name as an Object
     */
    public Object name() {
        return name;
    }

    /**
     * Returns value as an Object
     */
    public Object value() {
        return value;
    }

    /**
     * Returns int name
     */
    public int intName() {
        return intName;
    }

    /**
     * Returns int value
     */
    public int intValue() {
        return intValue;
    }

    /**
     * Return name as a String
     */
    public String getName() {
        return name.toString();
    }

    /**
     * Return value as a String
     */
    public String getValue() {
        return value.toString();
    }

    /**
     * Sets name
     */
    public void setName(Object name) {
        this.name = name;
    }

    /**
     * Sets value
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Sets name
     */
    public void setName(int intName) {
        this.intName = intName;
    }

    /**
     * Sets value
     */
    public void setValue(int intValue) {
        this.intValue = intValue;
    }

    /**
     * Returns the pair as a string in format name=value
     */
    public String toString() {
        return name + "=" + value;
    }

    public int compareTo(Object pairObj) {
        Pair pair = (Pair) pairObj;
        if (name != null && name instanceof String && pair.name != null && pair.name instanceof String) {
            return name.toString().compareTo(pair.name.toString());
        }
        return 0;
    }
}
