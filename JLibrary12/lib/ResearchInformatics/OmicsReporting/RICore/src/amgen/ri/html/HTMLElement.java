package amgen.ri.html;

import amgen.ri.xml.XMLElement;

/**
 * Title: HTMLElement
 * Description: Abstract class which incorporates some commong attributes and methods for HTML tag building
 * @author Jeffrey A. McDowell
 * @version 1.0
 */

public abstract class HTMLElement extends XMLElement {
    public static long counter = 1;
    public static final short UNSET = -1;
    public static final short LEFT = 0;
    public static final short RIGHT = 1;
    public static final short TOP = 2;
    public static final short ABSMIDDLE = 3;
    public static final short ABSBOTTOM = 4;
    public static final short TEXTTOP = 5;
    public static final short MIDDLE = 6;
    public static final short BASELINE = 7;
    public static final short BOTTOM = 8;
    public static final short CENTER = 9;

    public static final short NONE = 0;
    public static final short NUMBER = 1;
    public static final short INTEGER = 2;
    public static final short DATE = 3;
    public static final short NUMERICRANGE = 4;
    public static final short INTEGRALRANGE = 5;
    public static final short REGEX = 6;

    public HTMLElement() {
        this(null, null, null, null, null);
    }

    public HTMLElement(String name) {
        this(name, null, null, null, null);
    }

    public HTMLElement(String name, String className) {
        this(name, null, null, className, null);
    }

    public HTMLElement(String name, String title, String className) {
        this(name, null, title, className, null);
    }

    public HTMLElement(String name, String id, String title, String className,
                       String style) {
        super();
        setName(name);
        setId(id);
        setTitle(title);
        setClassName(className);
        setStyle(style);
        if (getId() == null) {
            setUniqueId();
        }
        data = null;
    }

    /** Get value for name */
    public String getName() {
        return (String) getAttribute("NAME");
    }

    /** Set value for name */
    public void setName(String name) {
        addAttribute("NAME", name);
    }

    /** Get value for id */
    public String getId() {
        return (String) getAttribute("ID");
    }

    /** Set value for id */
    public void setId(String id) {
        addAttribute("ID", id);
    }

    /** Sets a unquie value for id */
    protected synchronized void setUniqueId() {
        setId("el_" + (counter++));
    }

    /** Get value for title */
    public String getTitle() {
        return (String) getAttribute("TITLE");
    }

    /** Set value for title */
    public void setTitle(String title) {
        addAttribute("TITLE", title);
    }

    public String getAlign() {
        return (String) getAttribute("ALIGN");
    }

    public void setAlign(short align) {
        switch (align) {
            case (ABSBOTTOM):
                addAttribute("ALIGN", "absbottom");
                break;
            case (ABSMIDDLE):
                addAttribute("ALIGN", "absmiddle");
                break;
            case (BASELINE):
                addAttribute("ALIGN", "baseline");
                break;
            case (BOTTOM):
                addAttribute("ALIGN", "bottom");
                break;
            case (MIDDLE):
                addAttribute("ALIGN", "middle");
                break;
            case (RIGHT):
                addAttribute("ALIGN", "right");
                break;
            case (TEXTTOP):
                addAttribute("ALIGN", "texttop");
                break;
            case (TOP):
                addAttribute("ALIGN", "top");
                break;
            case (LEFT):
                addAttribute("ALIGN", "left");
                break;
            default:
                clearAttribute("ALIGN");
                break;
        }
    }

    public void setAlign(String align) {
        addAttribute("ALIGN", align);
    }

    /** Get value for tooltip */
    public String getTooltip() {
        return (String) getAttribute("TOOLTIP");
    }

    /** Set value for tooltip */
    public void setTooltip(String tooltip) {
        addAttribute("TOOLTIP", checkString(tooltip));
    }

    /** Get value for tooltip style class */
    public String getTooltip_Styleclass() {
        return (String) getAttribute("TOOLTIP_STYLE");
    }

    /** Set value for tooltip style class */
    public void setTooltip_Styleclass(String tooltip_styleclass) {
        addAttribute("TOOLTIP_STYLE", tooltip_styleclass);
    }

    /** Get value for className */
    public String getClassName() {
        return (String) getAttribute("CLASS");
    }

    /** Set value for className */
    public void setClassName(String className) {
        addAttribute("CLASS", className);
    }

    /**
     * Set whether this tag is disabled
     */
    public void setEnabled(boolean isEnabled) {
        if (isEnabled) {
            flagAttibutes.remove("DISABLED");
        } else {
            flagAttibutes.add("DISABLED");
        }
    }

    /** Get value for style */
    public String getStyle() {
        return (String) getAttribute("STYLE");
    }

    /** Set value for style */
    public void setStyle(String style) {
        addAttribute("STYLE", style);
    }

    public void setOnMousedown(String onmousedown) {
        addAttribute("ONMOUSEDOWN", onmousedown);
    }

    public void setOnMouseenter(String onmouseenter) {
        addAttribute("ONMOUSEENTER", onmouseenter);
    }

    public void setOnMouseleave(String onmouseleave) {
        addAttribute("ONMOUSELEAVE", onmouseleave);
    }

    public void setOnMousemove(String onmousemove) {
        addAttribute("ONMOUSEMOVE", onmousemove);
    }

    public void setOnMouseout(String onmouseout) {
        addAttribute("ONMOUSEOUT", onmouseout);
    }

    public void setOnMouseover(String onmouseover) {
        addAttribute("ONMOUSEOVER", onmouseover);
    }

    public void setOnMouseup(String onmouseup) {
        addAttribute("ONMOUSEUP", onmouseup);
    }

    public void setOnMousewheel(String onmousewheel) {
        addAttribute("ONMOUSEWHEEL", onmousewheel);
    }

    public void setOnDragstart(String ondragstart) {
        addAttribute("ONDRAGSTART", ondragstart);
    }

    public void setOnDragend(String ondragend) {
        addAttribute("ONDRAGEND", ondragend);
    }

    public void setOnDrop(String ondrop) {
        addAttribute("ONDROP", ondrop);
    }

    public void setOnDragover(String ondragover) {
        addAttribute("ONDRAGOVER", ondragover);
    }

    public void setOnDragenter(String ondragenter) {
        addAttribute("ONDRAGENTER", ondragenter);
    }

    public void setOnDragleave(String ondragleave) {
        addAttribute("ONDRAGLEAVE", ondragleave);
    }

    public void setOnclick(String onclick) {
        addAttribute("ONCLICK", onclick);
    }

    public void setOnchange(String onchange) {
        addAttribute("ONCHANGE", onchange);
    }

    public void setOnload(String onload) {
        addAttribute("ONLOAD", onload);
    }

    public Object clone(HTMLElement newElement) {
        return super.clone(newElement);
    }

    public HTMLElement addMemberElement(String newMemberElementName) {
        return (HTMLElement) addMemberElement(new GenericHTMLElement(newMemberElementName));
    }

    /**
     * Creates a new StringBuffer to begin the tag and adds the default tags.
     * e.g. "<[tagStart] [default tags] "
     * @param tagStart the starting elements of the tag
     */
    protected StringBuffer startTag(String tagStart) {
        addToolTip();
        return super.startTag(tagStart, DEFAULT_QUOTE);
    }

    private void addToolTip() {
        String SHOW_TOOPTIP_EVENT = "_evt_show_Tootip()";
        String HIDE_TOOPTIP_EVENT = "_evt_hide_Tootip()";

        String tooltip = getAttribute("TOOLTIP");
        String tooltipstyle = getAttribute("TOOLTIP_STYLE");
        String onmouseover = getAttribute("ONMOUSEOVER");
        String onmouseout = getAttribute("ONMOUSEOUT");
        if (tooltip == null) {
            return;
        }
        if (onmouseover == null || onmouseover.indexOf(SHOW_TOOPTIP_EVENT) < 0) {
            setOnMouseover(onmouseover != null ?
                           onmouseover + ";" + SHOW_TOOPTIP_EVENT :
                           SHOW_TOOPTIP_EVENT);
        }
        if (onmouseout == null || onmouseout.indexOf(HIDE_TOOPTIP_EVENT) < 0) {
            setOnMouseout(onmouseout != null ?
                          onmouseout + ";" + HIDE_TOOPTIP_EVENT :
                          HIDE_TOOPTIP_EVENT);
        }
    }

    /**
     * Returns the String version of an element value
     */
    protected static String getString(short element) {
        switch (element) {
            case (LEFT):
                return "LEFT";
            case (RIGHT):
                return "RIGHT";
            case (TOP):
                return "TOP";
            case (BOTTOM):
                return "BOTTOM";
            case (CENTER):
                return "CENTER";
            case (MIDDLE):
                return "MIDDLE";
            case (BASELINE):
                return "BASELINE";
            case (ABSMIDDLE):
                return "ABSMIDDLE";
            case (ABSBOTTOM):
                return "ABSBOTTOM";
            case (TEXTTOP):
                return "TEXTTOP";
            default:
                return "";
        }
    }

    protected String checkString(String s) {
        if (s == null) {
            return null;
        }
        return s.replace('\'', '`').replace('"', '`');
    }

}
