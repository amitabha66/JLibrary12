package amgen.ri.rdb;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import amgen.ri.util.ExtString;
import amgen.ri.xml.GenericElement;
import amgen.ri.xml.XMLElement;

/**
 * <p>Dynamic Relation Db Mapping</p>
 * <p>Description: Called by RdbData to write an XML version of the RdbData Class</p>
 * @author Jeffrey McDowell
 * @version 1.0
 */

public class RdbXMLWriter {
    RdbXMLWriter() {}

    /**
     * Writes an individual RdbData object in XML format. This RdbData object must implement XMLWritable
     * otherwise this just returns. If the RdbData object is an RdbDataArray, this simply calls
     * writeArrayXML and the component class of RdbDataArray must implement XMLWritable
     * This method is called from RdbData. Not called directly.
     * @param writer a PrintWriter to write the XML
     * @param rdbData the RdbDataObject
     */
    public void writeXML(PrintWriter writer, RdbData rdbData) {
        XMLElement xml = getXML(rdbData);
        if (xml != null) {
            xml.write(writer);
        }
    }

    public XMLElement getXML(RdbData rdbData) {
        if (rdbData instanceof RdbDataArray) {
            return getArrayXML( (RdbDataArray) rdbData);
        }
        if (! (rdbData instanceof XMLWritable)) {
            return null;
        }
        if (!rdbData.setData()) {
            return null;
        }
        XMLWritable xmlWritable = (XMLWritable) rdbData;
        String className = rdbData.getClass().getName();
        Hashtable fieldTable = Register.getRegister().getFieldTable(className);
        int fieldCount = Register.getRegister().getFieldNameCount(className);

        String classTag = xmlWritable.getClassElement();
        classTag = (classTag == null ? getClassName(rdbData) : classTag);
        Map classAttributes = xmlWritable.getClassAttributes();

        GenericElement topElement = new GenericElement(classTag);
        if (classAttributes != null) {
            for (Iterator attr = classAttributes.keySet().iterator(); attr.hasNext(); ) {
                String attribute = attr.next().toString();
                topElement.addAttribute(attribute, ExtString.getValidXML(classAttributes.get(attribute).toString()));
            }
        }
        for (int i = 0; i < fieldCount; i++) {
            String fieldName = Register.getRegister().getFieldName(className, i);
            Integer fieldType = (Integer) fieldTable.get(fieldName);
            if (!xmlWritable.includeFieldInXML(fieldName)) {
                continue;
            }
            if (fieldType.shortValue() == Register.MEMBERARRAY) {
                RdbData[] members = (RdbData[]) rdbData.get(fieldName);
                if (members != null && members.length > 0) {
                    for (int j = 0; j < members.length; j++) {
                        RdbData member = members[j];
                        XMLElement memberElement = member.getAsXML();
                        if (memberElement != null) {
                            topElement.addMemberElement(memberElement);
                        }
                    }
                }

            } else {
                String item = rdbData.getUnFormattedString(fieldName);
                if (item == null) {
                    continue;
                }
                item = ExtString.getValidXML(item);
                String fieldTag = xmlWritable.getFieldElement(fieldName);
                fieldTag = (fieldTag == null ? fieldName : fieldTag);
                GenericElement fieldElement = new GenericElement(fieldTag);
                topElement.addMemberElement(fieldElement);
                fieldElement.appendContent(item);
                Map fieldAttributes = xmlWritable.getFieldAttributes(fieldName);
                if (fieldAttributes != null) {
                    for (Iterator attr = fieldAttributes.keySet().iterator(); attr.hasNext(); ) {
                        String attribute = attr.next().toString();
                        fieldElement.addAttribute(attribute, ExtString.getValidXML(fieldAttributes.get(attribute).toString()));
                    }
                }
            }
        }

        Hashtable transientFieldTable = Register.getRegister().getTransientFieldTable(className);
        int transientFieldCount = Register.getRegister().getTransientFieldNameCount(className);

        for (int i = 0; i < transientFieldCount; i++) {
            String fieldName = Register.getRegister().getTransientFieldName(className, i);
            Integer fieldType = (Integer) transientFieldTable.get(fieldName);
            if (!xmlWritable.includeFieldInXML(fieldName)) {
                continue;
            }
            if (fieldType.shortValue() == Register.MEMBERARRAY) {
                RdbData[] members = (RdbData[]) rdbData.get(fieldName);
                if (members != null && members.length > 0) {
                    for (int j = 0; j < members.length; j++) {
                        RdbData member = members[j];
                        XMLElement memberElement = member.getAsXML();
                        if (memberElement != null) {
                            topElement.addMemberElement(memberElement);
                        }
                    }
                }

            } else {
                String item = rdbData.getUnFormattedString(fieldName);
                if (item == null) {
                    continue;
                }
                item = ExtString.getValidXML(item);
                String fieldTag = xmlWritable.getFieldElement(fieldName);
                fieldTag = (fieldTag == null ? fieldName : fieldTag);
                GenericElement fieldElement = new GenericElement(fieldTag);
                topElement.addMemberElement(fieldElement);
                fieldElement.appendContent(item);
                Map fieldAttributes = xmlWritable.getFieldAttributes(fieldName);
                if (fieldAttributes != null) {
                    for (Iterator attr = fieldAttributes.keySet().iterator(); attr.hasNext(); ) {
                        String attribute = attr.next().toString();
                        fieldElement.addAttribute(attribute, ExtString.getValidXML(fieldAttributes.get(attribute).toString()));
                    }
                }
            }
        }
        return topElement;
    }

    /**
     * Writes XML for an array of XMLWritable RdbData objects. Called by writeXML, The RdbData object compnents must implement
     * XMLWritable otherwise this simply returns. This method obtains the Array element tags and attributes by instantiating
     * the component class default (no argument constructor.) As such in th unlikely event the there are class variable
     * dependencies of the Array elements or attributes, these should be set in the default constructor.
     * @param writer the PrintWriter to write the XML
     * @param rdbDataArray an RdbDataArray of XMLWritable RdbData objects
     */
    private void writeArrayXML(PrintWriter writer, RdbDataArray rdbDataArray) {
        XMLElement xml = getArrayXML(rdbDataArray);
        if (xml != null) {
            xml.write(writer);
        }
    }

    public XMLElement getArrayXML(RdbDataArray rdbDataArray) {
        try {
            if (!rdbDataArray.setData()) {
                return null;
            }
            if (rdbDataArray.size() == 0) {
                return null;
            }
            RdbData regRdbObj = (RdbData) rdbDataArray.getComponentClass().getConstructor( (Class[])null).newInstance( (Object[])null);
            if (! (regRdbObj instanceof XMLWritable)) {
                return null;
            }
            XMLWritable xmlWritable = (XMLWritable) regRdbObj;
            String className = getClassName(regRdbObj);
            String arrayElement = xmlWritable.getArrayElement();
            String classElement = xmlWritable.getClassElement();
            String arrayTag;
            if (arrayElement != null) {
                arrayTag = arrayElement;
            } else if (classElement != null) {
                arrayTag = classElement;
            } else {
                arrayTag = className + "S";
            }

            GenericElement xml = new GenericElement(arrayTag);
            Map arrayAttributes = xmlWritable.getClassAttributes();
            if (arrayAttributes != null) {
                for (Iterator attr = arrayAttributes.keySet().iterator(); attr.hasNext(); ) {
                    String attribute = attr.next().toString();
                    if (arrayAttributes.get(attribute) != null) {
                        xml.addAttribute(attribute,
                                         ExtString.getValidXML(arrayAttributes.get(attribute).toString()));
                    }
                }
            }
            for (int i = 0; i < rdbDataArray.size(); i++) {
                XMLElement memberXML = getXML(rdbDataArray.getItem(i));
                xml.addMemberElement(memberXML);
            }
            return xml;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the class name without the package
     */
    private String getClassName(Object obj) {
        String className = obj.getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < className.length() - 1) {
            return className.substring(lastDot + 1);
        }
        return className;
    }

}
