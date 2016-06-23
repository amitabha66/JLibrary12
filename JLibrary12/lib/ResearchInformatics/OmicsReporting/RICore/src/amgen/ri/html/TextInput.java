package amgen.ri.html;

/**
 * <p>Title: Dynamic Relation Db Mapping</p>
 *
 * <p>Description: Text Input HTML form element implementation</p>
 *
 * @author not attributable
 * @version 1.0
 */
public class TextInput extends GenericHTMLElement {

    public TextInput() {
        super();
    }

    public TextInput(String name) {
        this(name, null, null, null, null, null, false);
    }

    public TextInput(String name, String className) {
        this(name, null, null, className, null, null, false);
    }

    public TextInput(String name, String className, String defaultValue, boolean disabled) {
        this(name, null, null, className, null, defaultValue, disabled);
        addAttribute("VALUE", defaultValue);
        if (disabled) {
            addAttribute("DISABLED", "T");
        }
    }

    public TextInput(String name, String title, String className) {
        this(name, null, title, className, null, null, false);
    }

    public TextInput(String name, String id, String title, String className, String style) {
        this(name, id, title, className, style, null, false);
    }

    public TextInput(String name, String id, String title, String className, String style, String defaultValue) {
        this(name, id, title, className, style, defaultValue, false);
        addAttribute("VALUE", defaultValue);
    }

    public TextInput(String name, String id, String title, String className, String style, String defaultValue, boolean disabled) {
        this();
        setName(name);
        setId(id);
        setClassName(className);
        setStyle(style);
        addAttribute("TYPE", "TEXT");
        addAttribute("VALUE", defaultValue);
        if (disabled) {
            addAttribute("DISABLED", "T");
        }
    }

    /**
     * getElementName
     *
     * @return String
     * @todo Implement this amgen.ri.xml.XMLElement method
     */
    public String getElementName() {
        return "INPUT";
    }
}
