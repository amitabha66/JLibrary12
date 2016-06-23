package amgen.ri.html;

/**
 * <p>Title: Dynamic Relation Db Mapping</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SelectOption extends GenericHTMLElement {

    protected SelectOption() {
        super();
    }

    public SelectOption(String text, boolean selected) {
        this(text, null, selected);
    }

    public SelectOption(String text) {
        this(text, null, false);
    }

    public SelectOption(String text, String value, boolean selected) {
        this();
        setText(text);
        if (value != null) {
            addAttribute("VALUE", value);
        } else {
            addAttribute("VALUE", text);

        }
        if (selected) {
            addAttribute("SELECTED", "T");
        }
    }

    public void setValue(String value) {
        addAttribute("VALUE", value);
    }

    public String getValue() {
        return getAttribute("VALUE");
    }

    public void setSelected(boolean selected) {
        if (selected) {
            addAttribute("SELECTED", "T");
        } else {
            clearAttribute("SELECTED");
        }
    }

    public boolean isSelected() {
        return (getAttribute("SELECTED") != null);
    }

    /**
     * getElementName
     *
     * @return String
     * @todo Implement this amgen.ri.xml.XMLElement method
     */
    public String getElementName() {
        return "OPTION";
    }

    /**
     * Returns the Value of the given OPTION element. Because an option element
     * can have its value as a "VALUE" attribute (any case) or in the Text of the
     * element, this is to help in finding what will actually be sent in a form
     * @param element HTMLElement
     * @return String
     */
    public static String findOptionValue(HTMLElement element) {
        String[] attrNames = element.getAttributeNames();
        for (int i = 0; i < attrNames.length; i++) {
            if (attrNames[i].equalsIgnoreCase("value")) {
                return element.getAttribute(attrNames[i]);
            }
        }
        return element.getText();
    }
}
