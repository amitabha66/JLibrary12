package amgen.ri.xml;

//import java.io.*;
//import java.util.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Element;

import sun.io.CharToByteConverter;
import amgen.ri.util.Pair;

/**
 * Title: XMLElement
 * Description: Abstract class which handles XML Element building
 * @author Jeffrey A. McDowell
 * @version 1.0
 */

public abstract class XMLElement implements Cloneable {
    protected final char DEFAULT_QUOTE = '\'';
    protected String data;
    private ArrayList memberElements = new ArrayList();
    protected HashMap attributes = new HashMap();
    protected ArrayList flagAttibutes = new ArrayList();

    public XMLElement() {}

    /** Get value for data */
    public String getData() {
        return data;
    }

    /** Get value for data */
    public String getText() {
        return getData();
    }

    /** Set value for data/text*/
    public void setData(String data) {
        this.data = data;
    }

    /** Set value for the text */
    public void setText(String text) {
        setData(text);
    }

    /**
     * Adds content to the tag
     * @param content
     */
    public void appendContent(String content) {
        String data = getData();
        setData( (data != null ? data + content : content));
    }

    /**
     * Adds content to the tag with an appended EOL
     * @param content
     */
    public void appendContentLine(String content) {
        appendContent(content + "\n");
    }

    /**
     * Clears all content from the tag
     */
    public void clearContent() {
        setData(null);
    }

    /**
     * Returns the Span content
     * @return
     */
    public String getContent() {
        return getData();
    }

    /**
     * Remove a member
     * @param member XMLElement
     */
    public void removeElement(XMLElement member) {
        memberElements.remove(member);
    }

    public XMLElement addMemberElement(XMLElement newMember) {
        if (newMember != null) {
            if (newMember == this) {
                throw new IllegalArgumentException("Can not add element as child");
            }
            memberElements.add(newMember);
        }
        return newMember;
    }

    public void addMemberElements(List newMembers) {
        for (int i = 0; i < newMembers.size(); i++) {
            if (newMembers.get(i) instanceof Element) {
                addMemberElement( (Element) newMembers.get(i));
            } else {
                addMemberElement( (XMLElement) newMembers.get(i));
            }
        }
    }

    public void addMemberElements(XMLElement[] newMembers) {
        for (int i = 0; i < newMembers.length; i++) {
            addMemberElement(newMembers[i]);
        }
    }

    public XMLElement addMemberElement(XMLElement newMember, int position) {
        if (newMember != null) {
            if (newMember == this) {
                throw new IllegalArgumentException("Can not add element as child");
            }
            if (position < 0) {
                position = 0;
            } else if (position > memberElements.size()) {
                position = memberElements.size();
            }
            memberElements.add(position, newMember);
        }
        return newMember;
    }

    public void addMemberElements(Element[] newMemberElement) {
        for (int i = 0; i < newMemberElement.length; i++) {
            addMemberElement(newMemberElement[i]);
        }
    }

    public XMLElement addMemberElement(Element newMemberElement) {
        if (newMemberElement != null) {
            String elementName = newMemberElement.getName();
            if (newMemberElement.getNamespacePrefix() != null && newMemberElement.getNamespacePrefix().length() > 0) {
                elementName = newMemberElement.getNamespacePrefix() + ":" + elementName;
            }
            XMLElement newMember = new GenericElement(elementName);
            List attributes = newMemberElement.getAttributes();
            for (int i = 0; i < attributes.size(); i++) {
                Attribute attribute = (Attribute) attributes.get(i);
                newMember.addAttribute(attribute.getName(), attribute.getValue());
            }
            if (newMemberElement.getText() != null) {
                newMember.setText(newMemberElement.getText());
            }
            memberElements.add(newMember);
            List childMemberElements = newMemberElement.getChildren();
            for (int i = 0; i < childMemberElements.size(); i++) {
                Element childMemberElement = (Element) childMemberElements.get(i);
                newMember.addMemberElement(childMemberElement);
            }
            return newMember;
        }
        return null;
    }

    public XMLElement addMemberElement(String newMemberElementName) {
        return addMemberElement(new GenericElement(newMemberElementName));
    }

    public int getMemberCount() {
        return memberElements.size();
    }

    public XMLElement getMember(int indx) {
        return (XMLElement) memberElements.get(indx);
    }

    public XMLElement[] getMembers() {
        return getMembers(null);
    }

    public XMLElement[] getMembers(String name) {
        ArrayList memberlist = new ArrayList();
        for (int i = 0; i < memberElements.size(); i++) {
            XMLElement element = (XMLElement) memberElements.get(i);
            if (name == null || element.getElementName().equalsIgnoreCase(name)) {
                memberlist.add(element);
            }
        }
        return (XMLElement[]) memberlist.toArray(new XMLElement[0]);
    }

    public void clearMemberElements() {
        memberElements.clear();
    }

    public void clearMemberElements(String elementName) {
        ArrayList newMemberlist = new ArrayList();
        for (int i = 0; i < memberElements.size(); i++) {
            XMLElement element = (XMLElement) memberElements.get(i);
            if (!element.getElementName().equalsIgnoreCase(elementName)) {
                newMemberlist.add(element);
            }
        }
        memberElements = newMemberlist;
    }

    /** Adds an attribute which has no value (flag) */
    public void addAttribute(String name) {
        flagAttibutes.add(name);
    }

    /** Adds an attribute name/value pair to the tag */
    public void addAttribute(String name, String value) {
        if (value == null || value.length() == 0) {
            clearAttribute(name);
        } else {
            attributes.put(name, value);
        }
    }

    /** Adds an attribute name/value pair to the tag */
    public void addAttribute(Pair attribute) {
        attributes.put(attribute.getName(), attribute.getValue());
    }

    /** Returns an attribute with the given name */
    public String getAttribute(String name) {
        return (String) attributes.get(name);
    }

    /** Returns the attributes as a Map */
    public Map getAttributes() {
        return attributes;
    }

    /** Returns an attribute names */
    public String[] getAttributeNames() {
        return (String[]) attributes.keySet().toArray(new String[0]);
    }

    /** Clears an atributes */
    public void clearAttribute(String name) {
        attributes.remove(name);
    }

    /** Clears all atributes */
    public void clearAttributes() {
        attributes.clear();
    }

    /**
     * Creates a new StringBuffer to begin the tag and adds the default tags.
     * e.g. "<[tagStart] [default tags] "
     * @param tagStart the starting elements of the tag
     */
    protected StringBuffer startTag(String tagStart, char quote) {
        StringBuffer tagBuffer = new StringBuffer("<" + tagStart);

        if (attributes.size() > 0 || flagAttibutes.size() > 0) {
            tagBuffer.append(" ");
        }
        Iterator attribIter = attributes.keySet().iterator();
        while (attribIter.hasNext()) {
            String attributeName = (String) attribIter.next();
            String attributeValue = (String) attributes.get(attributeName);
            addAttribute(tagBuffer, attributeName, attributeValue, quote);
        }
        for (int i = 0; i < flagAttibutes.size(); i++) {
            if (flagAttibutes.get(i) != null) {
                tagBuffer.append(flagAttibutes.get(i) + " ");
            }
        }
        return tagBuffer;
    }

    /**
     * Terminates a tagBuffer
     */
    protected String endTag(StringBuffer tagBuffer) {
        return endTag(tagBuffer, null);
    }

    /**
     * Terminates a tagBuffer optionally adding an element prior to the ">"
     * @param endStart optional final tag
     */
    protected String endTag(StringBuffer tagBuffer, String endStart) {
        if (endStart != null) {
            tagBuffer.append(endStart);
        }
        tagBuffer.append(">");
        return tagBuffer.toString();
    }

    /**
     * Adds a name/value tag to the tagbuffer if the value is not null
     * @param tagBuffer the StringBuffer the tag is built in
     * @param attribute the name
     * @param attributeValue the value
     */
    protected void addAttribute(StringBuffer tagBuffer, String attribute, String attributeValue, char quote) {
        if (attributeValue == null) {
            return;
        }
        if (attributeValue.indexOf(quote) > -1) {
            attributeValue = attributeValue.replace(quote, '`');
        }
        if (attribute != null) {
            tagBuffer.append(attribute + "=" + quote + attributeValue + quote);
        } else {
            tagBuffer.append(quote + attributeValue + quote);
        }
        tagBuffer.append(' ');
    }

    /**
     * Builds the complete tag
     */
    public String build() {
        return build(true, DEFAULT_QUOTE);
    }

    /**
     * Builds the complete tag
     */
    public String build(boolean includeLineBreaks, char quote) {
        StringBuffer tagBuffer = new StringBuffer(start(false, includeLineBreaks, quote));
        if (data != null) {
            tagBuffer.append(data);
        }
        for (int i = 0; i < memberElements.size(); i++) {
            XMLElement member = (XMLElement) memberElements.get(i);
            if (includeLineBreaks) {
                tagBuffer.append('\n');
            }
            tagBuffer.append(member.build(includeLineBreaks, quote));
        }
        tagBuffer.append(end());

        return tagBuffer.toString();
    }

    public abstract String getElementName();

    /**
     * Returns this XMLElement as a JDOM Element. Note: Flag attributes (attributes without a value which is
     * allowed in HTML) are not supported in XML and not included.
     * @return Element
     */
    public Element getAsElement() {
        Element element = new Element(getElementName());
        Iterator attribIter = attributes.keySet().iterator();
        while (attribIter.hasNext()) {
            String attributeName = (String) attribIter.next();
            String attributeValue = (String) attributes.get(attributeName);
            element.setAttribute(attributeName, attributeValue);
        }
        for (int i = 0; i < memberElements.size(); i++) {
            XMLElement member = (XMLElement) memberElements.get(i);
            element.addContent(member.getAsElement());
        }
        return element;
    }

    public String toString() {
        return build();
    }

    /**
     * Notification that the build of the element started. Does nothing in the
     * default implementation. May be overridden in child classes
     * @param includeMembers boolean
     */
    public void buildStarted(boolean includeMembers) {}

    /**
     * Builds the start of the tag- that is the <TAGNAME atttribute=value ...>
     */
    public String start() {
        return start(false, true, DEFAULT_QUOTE);
    }

    /**
     * Builds the start of the tag- that is the <TAGNAME atttribute=value ...> optionally
     * including the members
     */
    public String start(boolean includeMembers, boolean includeLineBreaks, char quote) {
        buildStarted(includeMembers);
        StringBuffer tagBuffer = startTag(getElementName(), quote);
        tagBuffer.append('>');
        if (includeMembers) {
            for (int i = 0; i < memberElements.size(); i++) {
                XMLElement member = (XMLElement) memberElements.get(i);
                if (includeLineBreaks) {
                    tagBuffer.append('\n');
                }
                tagBuffer.append(member.build(includeLineBreaks, quote));
            }
        }
        return tagBuffer.toString();
    }

    /**
     * Builds the end- - that is the </TAGNAME>
     */
    public String end() {
        return "</" + getElementName() + ">";
    }

    public void write(OutputStream out) {
        PrintWriter writer = new PrintWriter(out);
        write(writer);
        writer.flush();
    }

    public void write(Writer out) {
        PrintWriter writer = new PrintWriter(out);
        write(writer);
        writer.flush();
    }

    public void write(PrintWriter writer) {
        writer.println(build());
    }

    public void writeStart(PrintWriter writer) {
        writer.println(start(true, true, DEFAULT_QUOTE));
    }

    public void writeStart(PrintWriter writer, boolean includeMembers) {
        writer.println(start(includeMembers, true, DEFAULT_QUOTE));
    }

    public void writeStart(PrintWriter writer, boolean includeMembers, boolean includeLineBreaks) {
        writer.println(start(includeMembers, includeLineBreaks, DEFAULT_QUOTE));
    }

    public void writeStart(PrintWriter writer, boolean includeMembers, boolean includeLineBreaks, char quote) {
        writer.println(start(includeMembers, includeLineBreaks, quote));
    }

    public void writeEnd(PrintWriter writer) {
        writer.println(end());
    }

    protected String checkString(String s) {
        if (s == null) {
            return null;
        }
        return s.replace('\'', '`').replace('"', '`');
    }

    /**
     * Encode the character using escapes for reserved characters (<, >, ", &) and HEX
     * for non-printable characters and write
     */
    public void printEncodedString(Writer writer, String s, boolean keepQuot) throws IOException {
        if (s == null) {
            return;
        }
        writer.write(getEncodedString(s, keepQuot));
    }

    protected Object clone(XMLElement newElement) {
        newElement.data = this.data;
        newElement.memberElements = new ArrayList();
        for (int i = 0; i < this.memberElements.size(); i++) {
            XMLElement element = (XMLElement)this.memberElements.get(i);
            try {
                newElement.memberElements.add(element.clone());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        newElement.attributes = (HashMap)this.attributes.clone();
        newElement.flagAttibutes = (ArrayList)this.flagAttibutes.clone();
        return newElement;
    }

    /**
     * Encode the String using escapes for reserved characters (<, >, ", &) and HEX
     * for non-printable characters
     */
    public static String getEncodedString(String s) {
        return getEncodedString(s, false);
    }

    /**
     * Encode the String using escapes for reserved characters (<, >, ", &) and HEX
     * for non-printable characters
     */
    public static String getEncodedString(String s, boolean keepQuot) {
        if (s == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer(s.length());
        char[] c = s.toCharArray();
        for (int i = 0; i < c.length; i++) {
            sb.append(encodeChar(c[i], keepQuot));
        }
        return sb.toString();
    }

    /**
     * Encode the character using escapes for reserved characters (<, >, ", &) and HEX
     * for non-printable characters and write
     */
    public static void printEncodedChar(Writer writer, int ch, boolean keepQuot) throws IOException {
        writer.write(encodeChar(ch, keepQuot));
    }

    /**
     * Encode the character using escapes for reserved characters (<, >, ", &) and HEX
     * for non-printable characters
     */
    public static String encodeChar(int ch, boolean keepQuot) {
        if (ch == '<') {
            return "&lt;";
        } else if (ch == '&') {
            return "&amp;";
        } else if (ch == '>') {
            // character sequence "]]>" can't appear in content, therefore
            // we should escape '>'
            return "&gt;";
        } else if (ch == '"' && !keepQuot) {
            return "&quot;";
        } else if ( (ch >= ' ' && isPrintable( (char) ch)) ||
                   ch == '\n' || ch == '\r' || ch == '\t') {
            // REVISIT: new line characters must be escaped
            return "" + (char) ch;
        } else {
            // The character is not printable, return as character reference.
            return "&#x" + Integer.toHexString(ch) + ';';
        }
    }

    /**
     * Returns whether the character is printable using the default char/byte converter
     * @param ch
     * @return
     */
    public static boolean isPrintable(char ch) {
        try {
            return CharToByteConverter.getDefault().canConvert(ch);
        } catch (Exception e) {
            return false;
        }
    }

}
