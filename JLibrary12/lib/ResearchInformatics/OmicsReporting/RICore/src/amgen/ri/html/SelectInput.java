package amgen.ri.html;

//import java.util.*;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import amgen.ri.xml.XMLElement;

/**
 * <p>Title: Dynamic Relation Db Mapping</p>
 *
 * <p>Description: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SelectInput extends GenericHTMLElement {
    private String selectedValue;
    private List<SelectOption> selectOptions = new ArrayList<SelectOption> ();

    public SelectInput() {
        super();
    }

    public SelectInput(String name) {
        this(name, null, null, null, null);
    }

    public SelectInput(String name, String className) {
        this(name, null, null, className, null);

    }

    public SelectInput(String name, String title, String className) {
        this(name, null, title, className, null);
    }

    public SelectInput(String name, String id, String title, String className, String style) {
        this();
        setName(name);
        setId(id);
        setTitle(title);
        setClassName(className);
        setStyle(style);
    }

    public void bindDictionaryXML(String url) throws JDOMException, IOException {
        bindDictionaryXML(url, null);
    }

    public void bindDictionaryXML(String dictionaryXMLURL, String dictionaryName) throws JDOMException, IOException {
        bindDictionaryXML(dictionaryXMLURL, null, dictionaryName);
    }

    public void bindDictionaryXML(String dictionaryXMLURL, String dictionaryXSLTURL, String dictionaryName) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        Document dictionaryDocument = builder.build(dictionaryXMLURL);

        if (dictionaryXSLTURL != null) {
            try {
                dictionaryDocument = transform(dictionaryDocument, new URL(dictionaryXSLTURL));
            } catch (Exception e) {
                throw new JDOMException(e + "");
            }
        }
        bindDictionaryXML(dictionaryDocument, dictionaryName);
    }

    public void bindDictionaryXML(Object dictionaryXML, String dictionaryEntry) throws JDOMException {
        List selectOptionsList = new ArrayList();
        XPath optionXPath = XPath.newInstance("./OPTIONS[@name='" + dictionaryEntry + "']");
        Element optionElement = (Element) optionXPath.selectSingleNode(dictionaryXML);
        if (optionElement == null) {
            return;
        }
        String order = optionElement.getAttributeValue("order");
        List optionsElementList = optionElement.getChildren("OPTION");
        for (int i = 0; i < optionsElementList.size(); i++) {
            String label = null;
            String value = null;
            String position = null;
            String selected = null;
            Element option = (Element) optionsElementList.get(i);
            label = option.getAttributeValue("label");
            value = option.getAttributeValue("value");
            position = option.getAttributeValue("position");
            selected = option.getAttributeValue("selected");

            if (label == null && option.getChild("Label") != null) {
                label = option.getChildText("Label");
            }
            if (value == null && option.getChild("Value") != null) {
                value = option.getChildText("Value");
            }
            if (value != null) {
                SelectOption selectOption = new SelectOption();
                selectOptions.add(selectOption);
                selectOption.setValue(value);
                selectOptionsList.add(selectOption);
                if (label != null) {
                    selectOption.setText(label);
                } else {
                    selectOption.setText(value);
                }
                if (selected != null && Boolean.valueOf(selected).booleanValue()) {
                    selectOption.setSelected(true);
                }
                if (position != null) {
                    selectOption.addAttribute("position", position);
                }
            }
        }
        SelectOption[] selectOptions = (SelectOption[]) selectOptionsList.toArray(new SelectOption[0]);
        if (order != null) {
            if (order.equalsIgnoreCase("alphabetical")) {
                Arrays.sort(selectOptions, new Comparator() {
                    public int compare(Object obj1, Object obj2) {
                        SelectOption option1 = (SelectOption) obj1;
                        SelectOption option2 = (SelectOption) obj2;
                        String value1 = option1.getAttribute("VALUE");
                        if (value1 == null) {
                            value1 = option1.getText();
                        }
                        String position1 = option1.getAttribute("position");
                        String value2 = option2.getAttribute("VALUE");
                        if (value2 == null) {
                            value2 = option1.getText();
                        }
                        String position2 = option2.getAttribute("position");
                        if (position1 != null && position1.equalsIgnoreCase("start")) {
                            return -1;
                        }
                        if (position1 != null && position1.equalsIgnoreCase("end")) {
                            return 1;
                        }
                        if (position2 != null && position2.equalsIgnoreCase("start")) {
                            return 1;
                        }
                        if (position2 != null && position2.equalsIgnoreCase("end")) {
                            return -1;
                        }
                        return value1.compareTo(value2);
                    }
                }
                );
            } else if (order.equalsIgnoreCase("Numeric")) {
                Arrays.sort(selectOptions, new Comparator() {
                    public int compare(Object obj1, Object obj2) {
                        SelectOption option1 = (SelectOption) obj1;
                        SelectOption option2 = (SelectOption) obj2;
                        try {
                            String value1 = option1.getAttribute("VALUE");
                            String position1 = option1.getAttribute("position");
                            if (value1 == null) {
                                value1 = option1.getText();
                            }
                            String value2 = option2.getAttribute("VALUE");
                            String position2 = option2.getAttribute("position");
                            if (value2 == null) {
                                value2 = option1.getText();
                            }
                            if (position1 != null && position1.equalsIgnoreCase("start")) {
                                return -1;
                            }
                            if (position1 != null && position1.equalsIgnoreCase("end")) {
                                return 1;
                            }
                            if (position2 != null && position2.equalsIgnoreCase("start")) {
                                return 1;
                            }
                            if (position2 != null && position2.equalsIgnoreCase("end")) {
                                return -1;
                            }
                            Double d1 = new Double(value1);
                            Double d2 = new Double(value2);
                            return d1.compareTo(d2);
                        } catch (Exception e) {}
                        return 0;
                    }
                }
                );
            }
        }
        addMemberElements(selectOptions);
    }

    public void setSelectedValue(String selectedValue) {
        for (int i = 0; i < getMemberCount(); i++) {
            XMLElement member = getMember(i);
            if (member instanceof HTMLElement && member.getElementName().equalsIgnoreCase("option")) {
                String value = SelectOption.findOptionValue( (HTMLElement) member);
                if (value != null && value.equals(selectedValue)) {
                    member.addAttribute("SELECTED", "T");
                } else {
                    member.clearAttribute("SELECTED");
                }
            }
        }
    }

    public boolean containsOptionWithValue(String value) {
        for (int i = 0; i < getMemberCount(); i++) {
            XMLElement member = getMember(i);
            if (member instanceof HTMLElement && member.getElementName().equalsIgnoreCase("option")) {
                String optionValue = SelectOption.findOptionValue( (HTMLElement) member);
                if (optionValue != null && optionValue.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * getElementName
     *
     * @return String
     * @todo Implement this amgen.ri.xml.XMLElement method
     */
    public String getElementName() {
        return "SELECT";
    }

    public Document transform(Document sourceDoc, URL stylesheetURL) throws IOException, JDOMException, TransformerConfigurationException, TransformerException {
        // Set up the XSLT stylesheet for use with Xalan-J 2
        TransformerFactory transformerFactory =
            TransformerFactory.newInstance();
        Templates stylesheet = transformerFactory.newTemplates(new StreamSource(stylesheetURL.openStream()));
        Transformer processor = stylesheet.newTransformer();
        // Use I/O streams for source files
        PipedInputStream sourceIn = new PipedInputStream();
        PipedOutputStream sourceOut = new PipedOutputStream(sourceIn);
        StreamSource source = new StreamSource(sourceIn);
        // Use I/O streams for output files
        PipedInputStream resultIn = new PipedInputStream();
        PipedOutputStream resultOut = new PipedOutputStream(resultIn);
        // Convert the output target for use in Xalan-J 2
        StreamResult result = new StreamResult(resultOut);
        // Get a means for output of the JDOM Document
        XMLOutputter xmlOutputter = new XMLOutputter();
        // Output to the I/O stream
        xmlOutputter.output(sourceDoc, sourceOut);
        sourceOut.close();
        // Feed the resultant I/O stream into the XSLT processor
        processor.transform(source, result);
        resultOut.close();
        // Convert the resultant transformed document back to JDOM
        SAXBuilder builder = new SAXBuilder();
        Document resultDoc = builder.build(resultIn);
        return resultDoc;
    }

    public HTMLElement addOptionGroup(String label) {
        HTMLElement optionGroup = (HTMLElement) addMemberElement(new GenericHTMLElement("OPTGROUP"));
        optionGroup.addAttribute("LABEL", label);
        return optionGroup;
    }

    public HTMLElement addOption(String text, String value, boolean selected) {
        SelectOption option;
        addMemberElement(option = new SelectOption(text, value, selected));
        selectOptions.add(option);
        return option;
    }

    public HTMLElement addOption(String text, String value) {
        return addOption(text, value, false);
    }

    public HTMLElement addOption(String text, boolean selected) {
        return addOption(text, null, selected);
    }

    public HTMLElement addOption(String text) {
        return addOption(text, null, false);
    }

    public HTMLElement[] addOptions(String[] text, String[] values) {
        HTMLElement[] options = new HTMLElement[values.length];
        for (int i = 0; i < values.length; i++) {
            options[i] = addOption(text[i], values[i], false);
        }
        return options;
    }

    public HTMLElement[] addOptions(Collection values) {
        HTMLElement[] options = new HTMLElement[values.size()];
        int count = 0;
        for (Object value : values) {
            options[count++] = addOption(value.toString(), value.toString(), false);
        }
        return options;
    }

    /**
     * Returns Options add using the addOption, addOptions, bindDictionaryXML methods
     * @return List
     */
    public List<SelectOption> getSelectOptions() {
        return selectOptions;
    }
}
