package amgen.ri.html;

/**
 * <p>Description: Span HTML element implementation</p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Span extends GenericHTMLElement {
    public Span() {
        super();
    }

    public Span(String text, String className) {
        super();
        setText(text);
        setClassName(className);
    }

    /**
     * getElementName
     *
     * @return String
     * @todo Implement this amgen.ri.xml.XMLElement method
     */
    public String getElementName() {
        return "SPAN";
    }
}
