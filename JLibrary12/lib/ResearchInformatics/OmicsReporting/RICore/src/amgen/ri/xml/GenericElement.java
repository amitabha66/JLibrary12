package amgen.ri.xml;

public class GenericElement extends XMLElement {
    private String tagName;

    /**
     * Creates an XML element with the given name
     * @param tagName
     */
    public GenericElement(String tagName) {
        super();
        this.tagName = tagName;
    }

    /**
     * Creates an XML element with the given name
     * @param tagName
     * @param content the content of the XML Elementd
     */
    public GenericElement(String tagName, String content) {
        this(tagName);
        setData(content);
    }

    public String getElementName() {
        return tagName;
    }

}
