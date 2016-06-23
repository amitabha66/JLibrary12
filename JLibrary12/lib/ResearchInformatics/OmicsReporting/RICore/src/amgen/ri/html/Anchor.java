package amgen.ri.html;

/**
 * <p>Description: Anchor HTML element implementation </p>
 * @author not attributable
 * @version 1.0
 */
public class Anchor extends GenericHTMLElement {
    public Anchor() {
        super();
    }

    public Anchor(String name) {
        this();
        setName(name);
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
}
