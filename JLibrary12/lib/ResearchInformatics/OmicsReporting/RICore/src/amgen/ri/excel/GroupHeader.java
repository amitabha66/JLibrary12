package amgen.ri.excel;

import java.util.ArrayList;
import java.util.List;

import amgen.ri.util.Region;

/**
 * Encapsulates a GroupHeader- one which has parent and child headers.
 *
 * @author J. McDowell
 * @version $Id
 */
public class GroupHeader {
    private Region columnRange;
    private String header;
    private List<GroupHeader> children;

    /**
     * Creates a GroupHeader over the given Range and with the given text header
     *
     * @param range Region
     * @param header String
     */
    public GroupHeader(Region range, String header) {
        this.columnRange= range;
        this.header= header;
        this.children = new ArrayList<GroupHeader> ();
    }

    /**
     * Returns the ColumnRange
     *
     * @return Region
     */
    public Region getColumnRange() {
        return columnRange;
    }

    /**
     * Returns the text header
     *
     * @return String
     */
    public String getHeader() {
        return header;
    }

    /**
     * Returns the Group's children
     *
     * @return List
     */
    public List<GroupHeader> getChildren() {
        return children;
    }

    /**
     * Adds a Child header to the Group
     *
     * @param child GroupHeader
     */
    public void addChild(GroupHeader child) {
        children.add(child);
    }
}
