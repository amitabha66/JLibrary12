package amgen.ri.html;

/**
 * <p>Title: Dynamic Relation Db Mapping</p>
 * <p>Description: Generic HTML element implementation</p>
 * @author not attributable
 * @version 1.0
 */
public class GenericHTMLElement extends HTMLElement {
    private String tagName;

    public GenericHTMLElement() {
        super();
    }

    public GenericHTMLElement(String htmlTagName) {
        super();
        this.tagName = htmlTagName;
    }

    public GenericHTMLElement(String htmlTagName, HTMLElement member) {
        super();
        this.tagName = htmlTagName;
        addMemberElement(member);
    }

    public GenericHTMLElement(String htmlTagName, HTMLElement[] members) {
        super();
        this.tagName = htmlTagName;
        for (int i = 0; i < members.length; i++) {
            addMemberElement(members[i]);
        }
    }

    public GenericHTMLElement(String htmlTagName, String className) {
        super(null, className);
        this.tagName = htmlTagName;
    }

    public GenericHTMLElement(String htmlTagName, String title, String className) {
        super(null, title, className);
        this.tagName = htmlTagName;
    }

    public GenericHTMLElement(String htmlTagName, String id, String title, String className, String style) {
        super(null, id, title, className, style);
        this.tagName = htmlTagName;
    }

    public GenericHTMLElement(String htmlTagName, String id, String title, String className, String style, String text) {
        super(null, id, title, className, style);
        this.tagName = htmlTagName;
        setText(text);
    }

    public Object clone() {
        GenericHTMLElement newElement = new GenericHTMLElement();
        newElement.tagName = getElementName();
        return super.clone(newElement);
    }

    /**
     * getElementName
     *
     * @return String
     * @todo Implement this amgen.ri.xml.XMLElement method
     */
    public String getElementName() {
        return tagName;
    }

    public static void main(String[] args) {
        GenericHTMLElement element = new GenericHTMLElement("TEST");
        Link link = new Link("text", "url", "theclass");
        element.addMemberElement(link);
        link.addMemberElement(new Span("span", "spanclass"));
        GenericHTMLElement newElement = (GenericHTMLElement) element.clone();
        System.out.println(newElement.build(false, '"'));

        System.out.println(new org.jdom.output.XMLOutputter().outputString(newElement.getAsElement()));

    }

}
