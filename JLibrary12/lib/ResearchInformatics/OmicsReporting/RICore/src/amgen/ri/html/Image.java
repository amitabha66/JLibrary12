package amgen.ri.html;

import java.io.PrintWriter;

/**
 * Utility to help write HTML Image (IMG) elements
 *
 * @author Jeffrey McDowell
 */
public class Image extends GenericHTMLElement {

    public Image() {
        this(null, null, null, null, null, 0, UNSET, UNSET, UNSET, UNSET, null, null, UNSET);
    }

    public Image(String source) {
        this(null, null, source, null, null, 0, UNSET, UNSET, UNSET, UNSET, null, null, UNSET);
    }

    public Image(String source, String altText, String title) {
        this(null, null, source, altText, title, 0, UNSET, UNSET, UNSET, UNSET, null, null, UNSET);
    }

    public Image(String source, String title) {
        this(null, null, source, title, title, 0, UNSET, UNSET, UNSET, UNSET, null, null, UNSET);
    }

    public Image(String imageDirectory, String source, String altText, String title) {
        this(imageDirectory + "/" + source, altText, title);
    }

    public Image(String source, String title, int hSpace, int vSpace, short align) {
        this(null, null, source, null, title, UNSET, UNSET, UNSET, hSpace, vSpace, null, null, align);
    }

    public Image(String source, String title, int border, int hSpace, int vSpace, short align) {
        this(null, null, source, null, title, border, UNSET, UNSET, hSpace, vSpace, null, null, align);
    }

    public Image(String name, String id, String source, String altText, String title, int border, int height, int width,
                 int hSpace, int vSpace, String className, String style, short align) {
        super();
        setName(name);
        setId(id);
        setTitle(title);
        setClassName(className);
        setStyle(style);
        setSource(source);
        setAltText(altText);
        setBorder(border);
        setHeight(height);
        setWidth(width);
        setHSpace(hSpace);
        setVSpace(vSpace);
        setAlign(align);
        if (getId() == null) {
            setUniqueId();
        }
    }

    /** Get value for source */
    public String getSource() {
        return (String) getAttribute("SRC");
    }

    /** Set value for source */
    public void setSource(String source) {
        addAttribute("SRC", source);
    }

    /** Get value for altText */
    public String getAltText() {
        return (String) getAttribute("ALT");
    }

    /** Set value for altText */
    public void setAltText(String altText) {
        addAttribute("ALT", altText);
    }

    /** Get value for border */
    public String getBorder() {
        return (String) getAttribute("BORDER");
    }

    /** Set value for border */
    public void setBorder(int border) {
        if (border == UNSET) {
            setBorder(null);
            return;
        }
        setBorder(border + "");
    }

    /** Set value for border */
    public void setBorder(String border) {
        addAttribute("BORDER", border);
    }

    /** Get value for width */
    public String getWidth() {
        return (String) getAttribute("WIDTH");
    }

    /** Set value for width */
    public void setWidth(int width) {
        if (width == UNSET) {
            setWidth(null);
            return;
        }
        setWidth(width + "");
    }

    /** Set value for width */
    public void setWidth(String width) {
        addAttribute("WIDTH", width);
    }

    /** Get value for height */
    public String getHeight() {
        return (String) getAttribute("HEIGHT");
    }

    /** Set value for height */
    public void setHeight(int height) {
        if (height == UNSET) {
            setHeight(null);
            return;
        }
        setHeight(height + "");
    }

    /** Set value for height */
    public void setHeight(String height) {
        addAttribute("HEIGHT", height);
    }

    /** Set value for hSpace */
    public void setHSpace(int hSpace) {
        if (hSpace == UNSET) {
            setHSpace(null);
            return;
        }
        setHSpace(hSpace + "");
    }

    /** Set value for hSpace */
    public void setHSpace(String hSpace) {
        addAttribute("HSPACE", hSpace);
    }

    /** Set value for vSpace */
    public void setVSpace(int vSpace) {
        if (vSpace == UNSET) {
            setVSpace(null);
            return;
        }
        setVSpace(vSpace + "");
    }

    /** Set value for vSpace */
    public void setVSpace(String vSpace) {
        addAttribute("VSPACE", vSpace);
    }

    /** Set value for nSpace & vSpace */
    public void setSpace(int hSpace, int vSpace) {
        setHSpace(hSpace);
        setVSpace(vSpace);
    }

    public String getImageURL() {
        return getSource();
    }

    public String getImage(String altText, String title, short align, int hSpace) {
        setAltText(altText);
        setTitle(title);
        setAlign(align);
        setHSpace(hSpace);
        return build();
    }

    public String getImage(String altText, String title) {
        setAltText(altText);
        setTitle(title);
        return build();
    }

    public String getImage() {
        return build();
    }

    public void write(PrintWriter writer, String altText, String title) {
        setAltText(altText);
        setTitle(title);
        writer.println(build());
    }

    public void write(PrintWriter writer, String altText, String title, short align, int hSpace) {
        setAltText(altText);
        setTitle(title);
        setAlign(align);
        setHSpace(hSpace);
        writer.println(build());
    }

    public static String buildImage(String imageDirectory, String source, String altText, String title) {
        return buildImage(null, null, source, altText, title, UNSET, UNSET, UNSET, UNSET, UNSET, null, null, UNSET);
    }

    public static String buildImage(String source) {
        return buildImage(null, null, source, null, null, UNSET, UNSET, UNSET, UNSET, UNSET, null, null, UNSET);
    }

    public static String buildImage(String source, short align) {
        return buildImage(null, null, source, null, null, UNSET, UNSET, UNSET, UNSET, UNSET, null, null, align);
    }

    public static String buildImage(String source, int hSpace, int vSpace, short align) {
        return buildImage(null, null, source, null, null, UNSET, UNSET, UNSET, hSpace, vSpace, null, null, align);
    }

    public static String buildImage(String source, String altText, String title) {
        return buildImage(null, null, source, altText, title, UNSET, UNSET, UNSET, UNSET, UNSET, null, null, UNSET);
    }

    public static String buildImage(String source, String altText, String title, short align) {
        return buildImage(null, null, source, altText, title, UNSET, UNSET, UNSET, UNSET, UNSET, null, null, align);
    }

    public static String buildImage(String source, String altText, String title, int height, int width) {
        return buildImage(null, null, source, altText, title, UNSET, height, width, UNSET, UNSET, null, null, UNSET);
    }

    public static String buildImage(String source, String altText, String title, int border, int height, int width) {
        return buildImage(null, null, source, altText, title, border, height, width, UNSET, UNSET, null, null, UNSET);
    }

    public static String buildImage(String name, String id, String source, String altText, String title, int border, int height, int width, String className) {
        return buildImage(name, id, source, altText, title, border, height, width, UNSET, UNSET, null, null, UNSET);
    }

    public static String buildImage(String name, String id, String source, String altText, String title, int border, int height, int width,
                                    int hSpace, int vSpace, String className, String style, short align) {
        Image image = new Image(name, id, source, altText, title, border, height, width, hSpace, vSpace, className, style, align);
        return image.build();
    }

    public String getElementName() {
        return "IMG";
    }

    public String toString() {
        return getImage();
    }

}
