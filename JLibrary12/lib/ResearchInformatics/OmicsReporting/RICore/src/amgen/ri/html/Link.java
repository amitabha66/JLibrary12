package amgen.ri.html;

/**
 * <p>Title: Dynamic Relation Db Mapping</p>
 * <p>Description: HyperLink HTML element implementation </p>
 * @author not attributable
 * @version 1.0
 */
public class Link extends GenericHTMLElement {
    public Link() {
        super();
    }

    public Link(String text, String url, String className) {
        this(text, url, className, null);
    }

    public Link(String text, String url, String className, String onclick) {
        this();
        setClassName(className);
        setText(text);
        addAttribute("HREF", url);
        setOnclick(onclick);
    }

    /**
     * getElementName
     *
     * @return String
     * @todo Implement this amgen.ri.xml.XMLElement method
     */
    public String getElementName() {
        return "A";
    }

    /**
     * Sets the HREF attribute
     * Identical to addAttribute("HREF", href);
     * @param href String
     */
    public void setHRef(String href) {
        addAttribute("HREF", href);
    }

    /**
     * Returns the HREF attribute
     * Identical to getAttribute("HREF");
     */
    public String getHRef() {
        return getAttribute("HREF");
    }

    /**
     * Sets the TARGET attribute
     * Identical to addAttribute("TARGET", target);
     * @param href String
     */
    public void setTarget(String target) {
        addAttribute("TARGET", target);
    }

    /**
     * Returns the TARGET attribute
     * Identical to getAttribute("TARGET");
     */
    public String getTarget() {
        return getAttribute("TARGET");
    }
}
