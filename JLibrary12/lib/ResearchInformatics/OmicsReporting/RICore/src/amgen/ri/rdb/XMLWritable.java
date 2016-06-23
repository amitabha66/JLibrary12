package amgen.ri.rdb;

import java.util.Map;

/**
 * <p>Description: Allows an RdbClass to be writable in simple XML format</p>
 * There are 8 methods which require implementation
 * String getXMLVersion()- The XML version number. If this returns null, 1.0 is used
 * String getArrayElement()- The top level element tag used for an array of multiple RdbData objects of this type
 * String getClassElement()- The enclosing tag for the RdbData object field
 * boolean includeFieldInXML()- Whether the field should be included in the output
 * String getFieldElement()- the tag used to enclose the field of the RdbData class. If null, the field name is used
 * String getArrayAttributes()- Any additional attributes to include in the array element tag
 * String getClassAttributes()- Any additional attributes to include in the class element tag
 * String getFieldAttributes(String fieldName)- Any additional attributes to include in the field element tag
 * @author Jeffrey McDowell
 * @version 1.0
 */

public interface XMLWritable {
    public String getXMLVersion();

    public String getArrayElement();

    public String getClassElement();

    public boolean includeFieldInXML(String fieldName);

    public String getFieldElement(String fieldName);

    public Map getArrayAttributes();

    public Map getClassAttributes();

    public Map getFieldAttributes(String fieldName);
}
