package amgen.ri.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import amgen.ri.util.ExtFile;
import amgen.ri.util.ExtString;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Set of utilities operating on JDOM XML objects
 */
public class ExtXMLElement {
  public static final String ISO8601_DATEPATTERN= "yyyy-MM-dd'T'HH:mm:ssZ";
  private static SimpleDateFormat ISO8601_DATEFORMATTER;
  
  static {
    ISO8601_DATEFORMATTER= new SimpleDateFormat(ISO8601_DATEPATTERN);
  }
  
  /**
   * Parses an ISO8601 formatted Date
   * @param dateString
   * @return
   * @throws ParseException 
   */
  public Date decodeDate(String dateString) throws ParseException {
    return ISO8601_DATEFORMATTER.parse(dateString);
  }
  /**
   * Formats a date to an ISO8601 formatted String
   * @param date
   * @return 
   */
  public String encodeDate(Date date) {
    return ISO8601_DATEFORMATTER.format(date);
  }  
  /**
   * Returns the attribute value or null.
   * @param element
   * @param attributeName
   * @return 
   */
  public static String getAttribute(Element element, String attributeName) {
    if (element != null) {
      try {
        return element.getAttributeValue(attributeName);
      } catch (Exception e) {
      }
    }
    return null;
  }
 
  /**
   * Returns the attribute value as a Date or null.
   * The Date format must be ISO8601
   * @param element
   * @param attributeName
   * @return 
   */
  public static Date getAttributeDate(Element element, String attributeName) {
    if (element != null) {
      try {
        return ISO8601_DATEFORMATTER.parse(element.getAttributeValue(attributeName));
      } catch (Exception e) {
      }
    }
    return null;
  }

  /**
   * Returns an attribute as a Number or NaN if not possible
   *
   * @param element Element
   * @param attributeName String
   * @return Number
   */
  public static Number getAttributeNumber(Element element, String attributeName) {
    Double value = new Double(Double.NaN);
    try {
      value = new Double(element.getAttributeValue(attributeName));
    } catch (Exception e) {
    }
    return value;
  }

  /**
   * Returns an attribute as a Number or defaultValue if not possible
   *
   * @param element Element
   * @param attributeName String
   * @param defaultValue double
   * @return Number
   */
  public static Number getAttributeNumber(Element element, String attributeName, double defaultValue) {
    Number value = getAttributeNumber(element, attributeName);
    return (Double.isNaN(value.doubleValue()) ? defaultValue : value);
  }

  /**
   * Returns an attribute as a Boolean or false if not possible
   *
   * @param element Element
   * @param attributeName String
   * @return Number
   */
  public static Boolean getAttributeBoolean(Element element, String attributeName) {
    boolean result = false;
    try {
      String value = element.getAttributeValue(attributeName);
      if (value != null && (value.toLowerCase().startsWith("t") || value.toLowerCase().startsWith("y") || value.equals("1"))) {
        result = true;
      }
    } catch (Exception e) {
    }
    return new Boolean(result);
  }

  /**
   * Returns an element text as a Number or NaN if not possible
   *
   * @param element Element
   * @param childElement String
   * @return Number
   */
  public static Number getChildNumber(Element element, String childElement) {
    Double value = new Double(Double.NaN);
    try {
      value = new Double(element.getChildText(childElement));
    } catch (Exception e) {
    }
    return value;
  }

  /**
   * Returns the text for an Element by searching 1. The Text content 2. An attribute with the same name as
   * this Element in lowercase
   *
   * @param el Element
   * @return String
   */
  public static String findTextInElement(Element el) {
    String value = el.getText();
    if (ExtString.hasTrimmedLength(value)) {
      return value;
    }
    return el.getAttributeValue(el.getName().toLowerCase());
  }

  /**
   * Returns the text for an Element by searching 1. The Text content of a Child Element of name 'name' 2. An
   * attribute of 'name' in lowercase
   *
   * @param el Element
   * @return String
   */
  public static String findTextInElement(Element el, String name) {
    String value = el.getChildText(name);
    if (ExtString.hasTrimmedLength(value)) {
      return value;
    }
    value = el.getAttributeValue(name);
    if (ExtString.hasTrimmedLength(value)) {
      return value;
    }
    return el.getAttributeValue(name.toLowerCase());
  }

  /**
   * Returns the numeric value for an Element by searching 1. The Text content of a Child Element of name
   * 'name' 2. An attribute of 'name' in lowercase
   *
   * @param el Element
   * @return double or NaN if something goes wrong
   */
  public static double findNumericValueInElement(Element el, String name) {
    String value = findTextInElement(el, name);
    return ExtString.toDouble(value);
  }

  /**
   * Get Number values from an xpath
   *
   * @param element Element
   * @param xpath String
   * @return List
   */
  public static List<Number> getXPathNumberValues(Element element, String xpath) {
    return getXPathNumberValues(element, xpath, null);
  }

  /**
   * Get Number values from an xpath
   *
   * @param element Element
   * @param xpath String
   * @return List
   */
  public static List<Number> getXPathNumberValues(Element element, String xpath, Map<String, String> ns) {
    List<Number> values = new ArrayList<Number>();
    try {
      XPath xpathInstance = XPath.newInstance(xpath);
      if (ns != null) {
        for (String name : ns.keySet()) {
          xpathInstance.addNamespace(name, ns.get(name));
        }
      }
      List contents = xpathInstance.selectNodes(element);
      for (int i = 0; i < contents.size(); i++) {
        Object content = contents.get(i);
        Double value = new Double(Double.NaN);
        try {
          if (content instanceof Attribute) {
            value = new Double(((Attribute) content).getDoubleValue());
          } else if (content instanceof Element) {
            String text = ((Element) content).getText();
            value = new Double(text).doubleValue();
          }
        } catch (Exception e) {
        }
        values.add(value);
      }
    } catch (JDOMException ex) {
    }
    return values;
  }

  /**
   * Get Number values from an xpath
   *
   * @param element Element
   * @param xpath String
   * @return List
   */
  public static List<Number> getXPathNumberValues(Document doc, String xpath, Map<String, String> ns) {
    List<Number> values = new ArrayList<Number>();
    try {
      XPath xpathInstance = XPath.newInstance(xpath);
      if (ns != null) {
        for (String name : ns.keySet()) {
          xpathInstance.addNamespace(name, ns.get(name));
        }
      }
      List contents = xpathInstance.selectNodes(doc);
      for (int i = 0; i < contents.size(); i++) {
        Object content = contents.get(i);
        Double value = new Double(Double.NaN);
        try {
          if (content instanceof Attribute) {
            value = new Double(((Attribute) content).getDoubleValue());
          } else if (content instanceof Element) {
            String text = ((Element) content).getText();
            value = new Double(text).doubleValue();
          }
        } catch (Exception e) {
        }
        values.add(value);
      }
    } catch (JDOMException ex) {
    }
    return values;
  }

  /**
   * Get Number values from an xpath
   *
   * @param element Element
   * @param xpath String
   * @return List
   */
  public static List<Number> getXPathNumberValues(Document doc, String xpath) {
    List<Number> values = new ArrayList<Number>();
    try {
      XPath xpathInstance = XPath.newInstance(xpath);
      List contents = xpathInstance.selectNodes(doc);
      for (int i = 0; i < contents.size(); i++) {
        Object content = contents.get(i);
        Double value = new Double(Double.NaN);
        try {
          if (content instanceof Attribute) {
            value = new Double(((Attribute) content).getDoubleValue());
          } else if (content instanceof Element) {
            String text = ((Element) content).getText();
            value = new Double(text).doubleValue();
          }
        } catch (Exception e) {
        }
        values.add(value);
      }
    } catch (JDOMException ex) {
    }
    return values;
  }

  /**
   * Get a Number value from an xpath
   *
   * @param element Element
   * @param xpath String
   * @return Number
   */
  public static Number getXPathNumberValue(Element element, String xpath) {
    List<Number> values = getXPathNumberValues(element, xpath);
    if (values.size() > 0) {
      return values.get(0);
    }
    return new Double(Double.NaN);
  }

  /**
   * Get a Number value from an xpath
   *
   * @param element Element
   * @param xpath String
   * @return Number
   */
  public static Number getXPathNumberValue(Element element, String xpath, Map<String, String> ns) {
    List<Number> values = getXPathNumberValues(element, xpath, ns);
    if (values.size() > 0) {
      return values.get(0);
    }
    return new Double(Double.NaN);
  }

  /**
   * Get a Number value from an xpath
   *
   * @param element Element
   * @param xpath String
   * @return Number
   */
  public static Number getXPathNumberValue(Document doc, String xpath, Map<String, String> ns) {
    List<Number> values = getXPathNumberValues(doc, xpath, ns);
    if (values.size() > 0) {
      return values.get(0);
    }
    return new Double(Double.NaN);
  }

  /**
   * Get a Number value from an xpath
   *
   * @param document Document
   * @param xpath String
   * @return Number
   */
  public static Number getXPathNumberValue(Document document, String xpath) {
    List<Number> values = getXPathNumberValues(document.getRootElement(), xpath);
    if (values.size() > 0) {
      return values.get(0);
    }
    return new Double(Double.NaN);
  }

  /**
   * Get a String values from an xpath
   *
   * @param element Element
   * @param xpath String
   * @return List
   */
  public static List<String> getXPathValues(Element element, String xpath) {
    return getXPathValues(element, xpath, null);
  }

  /**
   * Get a String values from an xpath
   *
   * @param element Element
   * @param xpath String
   * @return List
   */
  public static List<String> getXPathValues(Element element, String xpath, Map<String, String> ns) {
    List<String> values = new ArrayList<String>();
    try {
      XPath xpathInstance = XPath.newInstance(xpath);
      if (ns != null) {
        for (String name : ns.keySet()) {
          xpathInstance.addNamespace(name, ns.get(name));
        }
      }
      List contents = xpathInstance.selectNodes(element);
      for (int i = 0; i < contents.size(); i++) {
        Object content = contents.get(i);
        String value = null;
        try {
          if (content instanceof Attribute) {
            value = ((Attribute) content).getValue();
          } else if (content instanceof Element) {
            value = ((Element) content).getText();
          }
        } catch (Exception e) {
        }
        values.add(value);
      }
    } catch (JDOMException ex) {
    }
    return values;
  }

  /**
   * Get a String value from an xpath
   *
   * @param element Element
   * @param xpath String
   * @return String
   */
  public static String getXPathValue(Element element, String xpath) {
    List<String> values = getXPathValues(element, xpath);
    if (values.size() > 0) {
      return values.get(0);
    }
    return null;
  }

  /**
   * Get a String value from an xpath
   *
   * @param element Element
   * @param xpath String
   * @return String
   */
  public static String getXPathValue(Element element, String xpath, Map<String, String> ns) {
    List<String> values = getXPathValues(element, xpath, ns);
    if (values.size() > 0) {
      return values.get(0);
    }
    return null;
  }

  /**
   * Get Elements from an xpath
   *
   * @param element Element
   * @param xpath String
   * @return List
   */
  public static List<Element> getXPathElements(Element element, String xpath) {
    return getXPathElements(element, xpath, null);
  }

  /**
   * Get an Element from an xpath
   *
   * @param element Element
   * @param xpath String
   * @return Element
   */
  public static Element getXPathElement(Element element, String xpath) {
    return getXPathElement(element, xpath, null);
  }

  /**
   * Get a List of Elements from an xpath which may include a Map of XML namespaces
   *
   * @param element Element
   * @param xpath String
   * @param nsMap Map
   * @return List
   */
  public static List<Element> getXPathElements(Element element, String xpath, Map<String, String> nsMap) {
    try {
      XPath xpathInstance = XPath.newInstance(xpath);
      if (nsMap != null) {
        for (String prefix : nsMap.keySet()) {
          xpathInstance.addNamespace(prefix, nsMap.get(prefix));
        }
      }
      return xpathInstance.selectNodes(element);
    } catch (JDOMException ex) {
    }
    return new ArrayList<Element>();
  }

  /**
   * Get an Element from an xpath which may include a Map of XML namespaces
   *
   * @param element Element
   * @param xpath String
   * @param nsMap Map
   * @return Element
   */
  public static Element getXPathElement(Element element, String xpath, Map<String, String> nsMap) {
    try {
      XPath xpathInstance = XPath.newInstance(xpath);
      if (nsMap != null) {
        for (String prefix : nsMap.keySet()) {
          xpathInstance.addNamespace(prefix, nsMap.get(prefix));
        }
      }
      return (Element) xpathInstance.selectSingleNode(element);
    } catch (JDOMException ex) {
    }
    return null;
  }

  /**
   * Get Strings from an xpath
   *
   * @param document Document
   * @param xpath String
   * @return List
   */
  public static List<String> getXPathValues(Document document, String xpath) {
    return getXPathValues(document, xpath, null);
  }

  /**
   * Get a String from an xpath
   *
   * @param document Document
   * @param xpath String
   * @param nsMap Map
   * @return String
   */
  public static String getXPathValue(Document document, String xpath, Map<String, String> nsMap) {
    List<String> values = getXPathValues(document, xpath, nsMap);
    if (values.size() > 0) {
      return values.get(0);
    }
    return null;
  }

  /**
   * Gets Strings from an xpath which may include a Map of XML namespaces
   *
   * @param document Document
   * @param xpath String
   * @param nsMap Map
   * @return List
   */
  public static List<String> getXPathValues(Document document, String xpath, Map<String, String> nsMap) {
    List<String> values = new ArrayList<String>();
    try {
      XPath xpathInstance = XPath.newInstance(xpath);
      if (nsMap != null) {
        for (String prefix : nsMap.keySet()) {
          xpathInstance.addNamespace(prefix, nsMap.get(prefix));
        }
      }
      List contents = xpathInstance.selectNodes(document);
      for (int i = 0; i < contents.size(); i++) {
        Object content = contents.get(i);
        String value = null;
        try {
          if (content instanceof Attribute) {
            value = ((Attribute) content).getValue();
          } else if (content instanceof Element) {
            value = ((Element) content).getText();
          }
        } catch (Exception e) {
        }
        values.add(value);
      }
    } catch (JDOMException ex) {
    }
    return values;
  }

  /**
   * Get a String from an xpath
   *
   * @param document Document
   * @param xpath String
   * @return String
   */
  public static String getXPathValue(Document document, String xpath) {
    List<String> values = getXPathValues(document, xpath);
    if (values.size() > 0) {
      return values.get(0);
    }
    return null;
  }

  /**
   * Get Strings from an xpath
   *
   * @param document Document
   * @param xpath String
   * @return List
   */
  public static List<Element> getXPathElements(Document document, String xpath) {
    return getXPathElements(document, xpath, null);
  }

  /**
   * Get an Element from an xpath
   *
   * @param document Document
   * @param xpath String
   * @return Element
   */
  public static Element getXPathElement(Document document, String xpath) {
    return getXPathElement(document, xpath, null);
  }

  /**
   * Get Elements from an xpath which may include a Map of XML namespaces
   *
   * @param document Document
   * @param xpath String
   * @param nsMap Map
   * @return List
   */
  public static List<Element> getXPathElements(Document document, String xpath, Map<String, String> nsMap) {
    try {
      XPath xpathInstance = XPath.newInstance(xpath);
      if (nsMap != null) {
        for (String prefix : nsMap.keySet()) {
          xpathInstance.addNamespace(prefix, nsMap.get(prefix));
        }
      }
      return xpathInstance.selectNodes(document);
    } catch (JDOMException ex) {
    }
    return new ArrayList<Element>();
  }

  /**
   * Get an Element from an xpath which may include a Map of XML namespaces
   *
   * @param document Document
   * @param xpath String
   * @param nsMap Map
   * @return Element
   */
  public static Element getXPathElement(Document document, String xpath, Map<String, String> nsMap) {
    try {
      XPath xpathInstance = XPath.newInstance(xpath);
      if (nsMap != null) {
        for (String prefix : nsMap.keySet()) {
          xpathInstance.addNamespace(prefix, nsMap.get(prefix));
        }
      }
      return (Element) xpathInstance.selectSingleNode(document);
    } catch (JDOMException ex) {
    }
    return null;
  }

  /**
   * Adds a child element to the given parent element. The namespace of the parent is inherited by the child.
   *
   * @param parentEl Element
   * @param childElName String
   * @return Element
   */
  public static Element addElement(Element parentEl, String childElName) {
    return addTextElement(parentEl, childElName, null, parentEl.getNamespace());
  }

  /**
   * Adds a child element to the given parent element with the given namespace.
   *
   * @param parentEl Element
   * @param childElName String
   * @param ns Namespace
   * @return Element
   */
  public static Element addElement(Element parentEl, String childElName, Namespace ns) {
    return addTextElement(parentEl, childElName, null, ns);
  }

  /**
   * Adds a child element with text to the given parent element. The namespace of the parent is inherited by
   * the child.
   *
   * Identical to addTextElement(Element, String, String).
   *
   * @param parentEl Element
   * @param childElName String
   * @param text String
   * @return Element
   */
  public static Element addElement(Element parentEl, String childElName, String text) {
    return addTextElement(parentEl, childElName, text, parentEl.getNamespace());
  }

  /**
   * Adds a child element with text to the given parent element. The namespace of the parent is inherited by
   * the child.
   *
   * @param parentEl Element
   * @param childElName String
   * @param text String
   * @return Element
   */
  public static Element addTextElement(Element parentEl, String childElName, String text) {
    return addTextElement(parentEl, childElName, text, parentEl.getNamespace());
  }

  /**
   * Adds a child element with text to the given parent element if text is not null. The namespace of the
   * parent is inherited by the child.
   *
   * @param parentEl Element
   * @param childElName String
   * @param text String
   * @return Element
   */
  public static Element addTextElementIfNotNull(Element parentEl, String childElName, String text) {
    if (text == null) {
      return null;
    }
    return addTextElement(parentEl, childElName, text, parentEl.getNamespace());
  }

  /**
   * Adds a child element with text to the given parent element if text is not null.
   *
   * @param parentEl Element
   * @param childElName String
   * @param text String
   * @param ns Namespace
   * @return Element
   */
  public static Element addTextElementIfNotNull(Element parentEl, String childElName, String text, Namespace ns) {
    if (text == null) {
      return null;
    }
    return addTextElement(parentEl, childElName, text, ns);
  }

  /**
   * Adds a child element to the given parent element. The namespace of the parent is inherited by the child.
   *
   * @param parentEl Element
   * @param childElName String
   * @return Element
   */
  public static Element addTextElement(Element parentEl, String childElName) {
    return addTextElement(parentEl, childElName, null, parentEl.getNamespace());
  }

  /**
   * Sets the text for a child element. If the child element does not exist, it creates a new element. If it
   * does exist, it set the text values for the first element of the given name.
   *
   * @param parentEl Element
   * @param childElName String
   * @param text String
   * @param ns Namespace
   * @return Element
   */
  public static Element setChildElementValue(Element parentEl, String childElName, String text, Namespace ns) {
    if (text == null) {
      return null;
    }
    if (ns == null) {
      ns = Namespace.NO_NAMESPACE;
    }
    Element childEl = parentEl.getChild(childElName, ns);
    if (childEl == null) {
      childEl = addElement(parentEl, childElName, ns);
    }
    childEl.setText(text);
    return childEl;
  }

  /**
   * Sets the text for a child element. If the child element does not exist, it creates a new element. If it
   * does exist, it set the text values for the first element of the given name.
   *
   * @param parentEl Element
   * @param childElName String
   * @param text String
   * @param ns Namespace
   * @return Element
   */
  public static Element setChildElementValue(Element parentEl, String childElName, String text) {
    return setChildElementValue(parentEl, childElName, text, null);

  }

  /**
   * Adds an attribute to an element if attributeValue is not null
   *
   * @param parentEl Element
   * @param attrbuteName String
   * @param attrbuteValue String
   */
  public static void addAttribute(Element parentEl, String attributeName, String attributeValue) {
    if (attributeValue == null) {
      return;
    }
    parentEl.setAttribute(attributeName, attributeValue);
  }

  /**
   * Adds an attribute to an element if attributeValue is not null. The attributeValue
   * is converted to ISO8601
   *
   * @param parentEl Element
   * @param attrbuteName String
   * @param attrbuteValue Date
   */
  public static void addAttribute(Element parentEl, String attributeName, Date attributeValue) {
    if (attributeValue == null) {
      return;
    }
    parentEl.setAttribute(attributeName, ISO8601_DATEFORMATTER.format(attributeValue));
  }

  /**
   * Adds an attribute to an element if attributeValue is not null. If attributeValue is null, defaultValue is
   * added if that is not null
   *
   * @param parentEl Element
   * @param attributeName String
   * @param attributeValue String
   * @param defaultValue String
   */
  public static void addAttribute(Element parentEl, String attributeName, String attributeValue, String defaultValue) {
    if (attributeValue == null) {
      addAttribute(parentEl, attributeName, defaultValue);
    } else {
      parentEl.setAttribute(attributeName, attributeValue);
    }
  }

  /**
   * Adds a child element with text to the given parent element
   *
   * @param parentEl Element
   * @param childElName String
   * @param text String
   * @param ns Namespace
   * @return Element
   */
  public static Element addTextElement(Element parentEl, String childElName, String text, Namespace ns) {
    Element childEl = new Element(childElName, ns);
    parentEl.addContent(childEl);
    if (text != null) {
      childEl.setText(text);
    }
    return childEl;
  }

  /**
   * Adds a child element with text to the given parent element at the first position
   *
   * @param parentEl Element
   * @param childElName String
   * @param text String
   * @param ns Namespace
   * @return Element
   */
  public static Element prependTextElement(Element parentEl, String childElName, String text, Namespace ns) {
    Element childEl = new Element(childElName, ns);
    parentEl.addContent(0, childEl);
    if (text != null) {
      childEl.setText(text);
    }
    return childEl;
  }

  /**
   * Adds a child element with text to the given parent element at the first position The namespace of the
   * parent is inherited by the child.
   *
   * @param parentEl Element
   * @param childElName String
   * @param text String
   * @param ns Namespace
   * @return Element
   */
  public static Element prependTextElement(Element parentEl, String childElName, String text) {
    Element childEl = new Element(childElName, parentEl.getNamespace());
    parentEl.addContent(0, childEl);
    if (text != null) {
      childEl.setText(text);
    }
    return childEl;
  }

  /**
   * Adds a child element to the given parent element at the first position
   *
   * @param parentEl Element
   * @param childElName String
   * @param ns Namespace
   * @return Element
   */
  public static Element prependElement(Element parentEl, String childElName, Namespace ns) {
    return prependTextElement(parentEl, childElName, null, ns);
  }

  /**
   * Adds a child element to the given parent element at the first position The namespace of the parent is
   * inherited by the child.
   *
   * @param parentEl Element
   * @param childElName String
   * @return Element
   */
  public static Element prependElement(Element parentEl, String childElName) {
    return prependTextElement(parentEl, childElName, null, parentEl.getNamespace());
  }

  /**
   * Returns a top-level clone of the Element- i.e. attributes only
   *
   * @param el Element
   * @return Element
   */
  public static Element createShallowClone(Element el) {
    Element cloneEl = new Element(el.getName());
    if (el.getAttributes() != null) {
      List<Attribute> attributes = el.getAttributes();
      for (Attribute attr : attributes) {
        Attribute attribute = (Attribute) attr.clone();
        cloneEl.setAttribute(attribute);
      }
    }
    return cloneEl;
  }

  /**
   * Copies all attributes from the source Element to the target Element. Returns the target Element.
   *
   * @param elSource Element
   * @param elTarget Element
   * @return Element
   */
  public static Element copyAttributes(Element elSource, Element elTarget) {
    if (elSource.getAttributes() != null) {
      List<Attribute> attributes = elSource.getAttributes();
      for (Attribute attr : attributes) {
        Attribute attribute = (Attribute) attr.clone();
        elTarget.setAttribute(attribute);
      }
    }
    return elTarget;
  }

  /**
   * Copies all the child Elements from the Root Element of the source Document into the Root Element of the
   * target Document.
   *
   * @param sourceDoc Document
   * @param targetDoc Document
   * @return Document
   */
  public static Document copyContent(Document sourceDoc, Document targetDoc) {
    List<Element> els = sourceDoc.getRootElement().getChildren();
    for (Content el : els) {
      targetDoc.getRootElement().addContent((Element) el.clone());
    }
    return targetDoc;
  }

  /**
   * Returns the String-ified XML as a Document
   *
   * @param s String
   * @return Document
   */
  public static Document buildDocument(String s) {
    try {
      SAXBuilder builder = new SAXBuilder();
      builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      return builder.build(new StringReader(s));
    } catch (Exception ex) {
      return null;
    }
  }

  /**
   * Returns the String-ified XML byte array as a Document
   *
   * @param bytes byte[]
   * @return Document
   */
  public static Document buildDocument(byte[] bytes) {
    try {
      return new SAXBuilder().build(new ByteArrayInputStream(bytes));
    } catch (Exception ex) {
      return null;
    }
  }

  /**
   * Returns the String-ified XML as a Document
   *
   * @param s String
   * @return Document
   */
  public static Document toDocument(String s) {
    try {
      return buildDocument(s);
    } catch (Exception ex) {
      return null;
    }
  }

  /**
   * Builds a JDOM Document from a W3C Document representation
   *
   * @param w3cDoc String
   * @return Document
   */
  public static Document toDocument(org.w3c.dom.Document w3cDoc) {
    try {
      return new DOMBuilder().build(w3cDoc);
    } catch (Exception ex) {
      return null;
    }
  }

  /**
   * Returns the XML as a Document
   *
   * @param url URL
   * @return Document
   */
  public static Document toDocument(URL url) {
    try {
      return new SAXBuilder().build(url);
    } catch (Exception ex) {
      return null;
    }
  }

  /**
   * Returns the XML as a Document retrieved from a ClassLoader provided by the given Class
   *
   * @param clazz URL
   * @param fileName String
   * @return Document
   */
  public static Document toDocument(Class clazz, String fileName) {
    try {
      InputStream in = clazz.getClassLoader().getResourceAsStream(fileName);
      return new SAXBuilder().build(in);
    } catch (Exception ex) {
      return null;
    }
  }

  /**
   * Returns the XML as a Document
   *
   * @param url URL
   * @return Document
   */
  public static Document toDocument(String url, Map<String, ?> parameters) {
    try {
      InputStream in = ExtFile.postURLInputStream(url, parameters);
      Document doc = new SAXBuilder().build(in);
      in.close();
      return doc;
    } catch (Exception ex) {
      return null;
    }
  }

  /**
   * Returns the XML from a Reader as a Document
   *
   * @param r Reader
   * @return Document
   */
  public static Document toDocument(Reader r) {
    try {
      return new SAXBuilder().build(r);
    } catch (Exception ex) {
      return null;
    }
  }

  /**
   * Returns the XML from an InputStream as a Document
   *
   * @param r Reader
   * @return Document
   */
  public static Document toDocument(InputStream in) {
    try {
      return new SAXBuilder().build(in);
    } catch (Exception ex) {
      return null;
    }
  }

  /**
   * Returns the XML from a File as a Document
   *
   * @param f File
   * @return Document
   */
  public static Document toDocument(File f) {
    try {
      return new SAXBuilder().build(f);
    } catch (Exception ex) {
      return null;
    }
  }

  /**
   * Returns the XML from a File as a Document
   *
   * @param file File
   * @return Document
   */
  public static Document fromFile(File file) {
    return toDocument(file);
  }

  /**
   * Returns the XML from a File as a Document
   *
   * @param url URL
   * @return Document
   */
  public static Document fromFile(String fileName) {
    return fromFile(new File(fileName));
  }

  /**
   * Returns the XML as a String in compact format
   *
   * @param el Element
   * @return String
   */
  public static String toString(Element el) {
    return new XMLOutputter().outputString(el);
  }

  /**
   * Returns the XML as a String in pretty format
   *
   * @param el Element
   * @return String
   */
  public static String toPrettyString(Element el) {
    Format f = Format.getPrettyFormat();
    f.setEncoding("US-ASCII");
    return new XMLOutputter(f).outputString(el);
  }

  /**
   * Returns the XML as a String in compact format
   *
   * @param doc Document
   * @return String
   */
  public static String toString(Document doc) {
    return new XMLOutputter().outputString(doc);
  }

  /**
   * Returns the XML as a String in compact format
   *
   * @param doc Document
   * @return String
   */
  public static String toString(Document doc, Format f) {
    return new XMLOutputter(f).outputString(doc);
  }

  /**
   * Returns the XML as a String in compact format
   *
   * @param doc Document
   * @return String
   */
  public static String toString(Element el, Format f) {
    return new XMLOutputter(f).outputString(el);
  }

  /**
   * Returns the XML as a String in pretty format
   *
   * @param doc Document
   * @return String
   */
  public static String toPrettyString(Document doc) {
    return new XMLOutputter(Format.getPrettyFormat()).outputString(doc);
  }

  /**
   * Prints the XML as a String in pretty format to stdout
   *
   * @param el Document
   */
  public static void printPrettyString(Element el) {
    System.out.println(toPrettyString(el));
  }

  /**
   * Prints the XML as a String in pretty format to stdout
   *
   * @param doc Document
   * @return String
   */
  public static void printPrettyString(Document doc) {
    System.out.println(toPrettyString(doc));
  }

  /**
   * Writes the XML to the given Writer
   *
   * @param doc Document
   * @param writer Writer
   * @throws IOException
   * @return boolean
   */
  public static boolean write(Document doc, Writer writer) throws IOException {
    if (doc != null) {
      new XMLOutputter().output(doc, writer);
      return true;
    }
    return false;
  }

  /**
   * Writes the XML to the given Writer
   *
   * @param el Element
   * @param writer Writer
   * @throws IOException
   * @return boolean
   */
  public static boolean write(Element el, Writer writer) throws IOException {
    if (el != null) {
      new XMLOutputter().output(el, writer);
      return true;
    }
    return false;
  }

  /**
   * Writes the XML to the given Writer
   *
   * @param doc Document
   * @param writer Writer
   * @param format Format
   * @throws IOException
   * @return boolean
   */
  public static boolean write(Document doc, Writer writer, Format format) throws IOException {
    if (doc != null) {
      new XMLOutputter(format).output(doc, writer);
      return true;
    }
    return false;
  }

  /**
   * Writes the XML to the given OutputStream
   *
   * @param el Element
   * @param out OutputStream
   * @throws IOException
   * @return boolean
   */
  public static boolean write(Element el, OutputStream out) throws IOException {
    if (el != null) {
      new XMLOutputter().output(el, out);
      return true;
    }
    return false;
  }

  /**
   * Writes the XML to the given OutputStream
   *
   * @param el Element
   * @param out OutputStream
   * @param format Format
   * @throws IOException
   * @return boolean
   */
  public static boolean write(Element el, OutputStream out, Format format) throws IOException {
    if (el != null) {
      new XMLOutputter(format).output(el, out);
      return true;
    }
    return false;
  }

  /**
   * Writes the XML to the given OutputStream
   *
   * @param doc Document
   * @param out OutputStream
   * @param format Format
   * @throws IOException
   * @return boolean
   */
  public static boolean write(Document doc, OutputStream out, Format format) throws IOException {
    if (doc != null) {
      new XMLOutputter(format).output(doc, out);
      return true;
    }
    return false;
  }

  /**
   * Writes the XML to the given OutputStream
   *
   * @param doc Document
   * @param out OutputStream
   * @throws IOException
   * @return boolean
   */
  public static boolean write(Document doc, OutputStream out) throws IOException {
    if (doc != null) {
      new XMLOutputter().output(doc, out);
      return true;
    }
    return false;
  }

  /**
   * Writes the XML to the given Writer
   *
   * @param el Element
   * @param writer Writer
   * @param format Format
   * @throws IOException
   * @return boolean
   */
  public static boolean write(Element el, Writer writer, Format format) throws IOException {
    if (el != null) {
      new XMLOutputter(format).output(el, writer);
      return true;
    }
    return false;
  }

  /**
   * Writes the XML to the given Writer
   *
   * @param doc Document
   * @param fileName Writer
   * @throws IOException
   * @return boolean
   */
  public static boolean write(Document doc, String fileName) throws IOException {
    if (doc != null) {
      Writer f = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");
      new XMLOutputter().output(doc, f);
      f.close();
      return true;
    } else {
      return false;
    }
  }

  /**
   * Writes the XML to the given Writer
   *
   * @param el Element
   * @param fileName Writer
   * @throws IOException
   * @return boolean
   */
  public static boolean write(Element el, String fileName) throws IOException {
    if (el != null) {
      Writer f = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");
      new XMLOutputter().output(el, f);
      f.close();
      return true;
    } else {
      return false;
    }
  }

  /**
   * Writes the XML to the given Writer
   *
   * @param el Element
   * @param fileName Writer
   * @param format Format
   * @throws IOException
   * @return boolean
   */
  public static boolean write(Element el, String fileName, Format format) throws IOException {
    if (el != null) {
      FileWriter f = new FileWriter(fileName);
      new XMLOutputter(format).output(el, f);
      f.close();
      return true;
    } else {
      return false;
    }
  }

  /**
   * Writes the XML to the given Writer
   *
   * @param doc Document
   * @param file Writer
   * @throws IOException
   * @return boolean
   */
  public static boolean write(Document doc, File file) throws IOException {
    if (doc != null) {
      Writer f = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
      new XMLOutputter().output(doc, f);
      f.close();
      return true;
    } else {
      return false;
    }
  }

  /**
   * Writes the XML to the given Writer
   *
   * @param el Element
   * @param file Writer
   * @throws IOException
   * @return boolean
   */
  public static boolean write(Element el, File file) throws IOException {
    if (el != null) {
      Writer f = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
      new XMLOutputter().output(el, f);
      f.close();
      return true;
    } else {
      return false;
    }
  }

  public static OutputStream zipDocument(Document doc, OutputStream out) throws IOException {
    GZIPOutputStream gzipOut = new GZIPOutputStream(out);
    new XMLOutputter().output(doc, gzipOut);
    gzipOut.close();
    return out;
  }

  public static Document unzipDocument(InputStream in) {
    Document doc = null;
    try {
      GZIPInputStream gzipIn = new GZIPInputStream(in);
      doc = new SAXBuilder().build(gzipIn);
      gzipIn.close();
    } catch (Exception e) {
    }
    return doc;
  }
}
