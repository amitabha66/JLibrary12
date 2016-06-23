package amgen.ri.html;

import java.util.ArrayList;
import java.util.List;

public class Table extends GenericHTMLElement {

    private int rowCount = 0;
    private List rows;
    /**
     * Creates a TABLE element with default attributes
     */
    public Table() {
        this(null, "0", "0", "0");
    }

    /**
     * Creates a TABLE element with the given CSS class
     */
    public Table(String className) {
        this(className, "0", "0", "0");
    }

    /**
     * Creates a TABLE element with the given cell spacing & padding
     */
    public Table(String cellSpacing, String cellPadding) {
        this(null, "0", cellSpacing, cellPadding);
    }

    /**
     * Creates a TABLE element with the given CSS class and cell spacing & padding
     */
    public Table(String className, String cellSpacing, String cellPadding) {
        this(className, "0", cellSpacing, cellPadding);
    }

    /**
     * Creates a TABLE element with the given CSS class, border, and cell spacing & padding
     */
    public Table(String className, String border, String cellSpacing, String cellPadding) {
        super("TABLE");
        rows = new ArrayList();
        setClassName(className);
        addAttribute("BORDER", border);
        addAttribute("CELLSPACING", cellSpacing);
        addAttribute("CELLPADDING", cellPadding);
    }

    /**
     * Sets the TABLE width
     * @param width String
     */
    public void setWidth(String width) {
        addAttribute("WIDTH", width);
    }

    /**
     * Adds a TableRow to the TABLE
     * @return TableRow
     */
    public TableRow addRow() {
        return addRow(null);
    }

    /**
     * Adds a TableRow to the TABLE
     * @return TableRow
     */
    public TableRow addRow(String id) {
        rowCount++;
        TableRow row = (TableRow) addMemberElement(new TableRow());
        if (id != null) {
            row.setId(id);
        }
        rows.add(row);
        return row;
    }

    /**
     * Adds a TableRow as the first row of the TABLE enclosed in a THEAD tag
     * @return TableRow
     */
    public TableRow addHeaderRow() {
        rowCount++;
        HTMLElement thead = (HTMLElement) addMemberElement(new GenericHTMLElement("THEAD"), 0);
        TableRow row = (TableRow) thead.addMemberElement(new TableRow());
        rows.add(row);
        return row;
    }

    /**
     * Adds a TableRow as the first row of the TABLE enclosed in a THEAD tag
     * @return TableRow
     */
    public TableRow addHeaderRow(String tHeadClassName, String rowClassName) {
        rowCount++;
        HTMLElement thead = (HTMLElement) addMemberElement(new GenericHTMLElement("THEAD", tHeadClassName), 0);
        TableRow row = (TableRow) thead.addMemberElement(new TableRow(rowClassName));
        rows.add(row);
        return row;
    }

    /**
     * Returns the current row
     * @return TableRow
     */
    public TableRow getCurrentRow() {
        if (rows.size() > 0) {
            return (TableRow) rows.get(rows.size() - 1);
        }
        return null;
    }

    /**
     * Returns a specific TableRow
     * @param row int
     * @return TableRow
     */
    public TableRow getRow(int row) {
        if (row >= 0 && row < rows.size()) {
            return (TableRow) rows.get(row);
        }
        return null;
    }

    /**
     * Returns the current row count
     * @return int
     */
    public int getRowCount() {
        return rowCount;
    }

    /**
     * A TABLE ROW (TR) element
     */
    public static class TableRow extends GenericHTMLElement {
        private int cellCount = 0;
        private List cells;

        /**
         * Create a new TableRow
         */
        public TableRow() {
            this(null);
        }

        /**
         * Create a new TableRow
         */
        public TableRow(String className) {
            super("TR", className);
            cells = new ArrayList();
        }

        /**
         * Adds a cell to the row with default attributes
         * @return TableCell
         */
        public TableCell addCell() {
            return addCell(null, null, null, null, false, 0, 0);
        }

        /**
         * Adds a cell to the row with default attributes
         * @return TableCell
         */
        public TableCell addCell(int colSpan) {
            return addCell(null, null, null, null, false, colSpan, 0);
        }

        /**
         * Adds a cell to the row with default attributes
         * @return TableCell
         */
        public TableCell addCell(int colSpan, boolean noWrap) {
            return addCell(null, null, null, null, noWrap, colSpan, 0);
        }

        /**
         * Adds a cell to the row with default attributes
         * @return TableCell
         */
        public TableCell addCell(String className, int colSpan, boolean noWrap) {
            return addCell(className, null, null, null, noWrap, colSpan, 0);
        }

        /**
         * Adds a cell to the row with default attributes
         * @return TableCell
         */
        public TableCell addCell(boolean noWrap) {
            return addCell(null, null, null, null, noWrap, 0, 0);
        }

        /**
         * Adds a cell to the row with default attributes
         * @return TableCell
         */
        public TableCell addCell(int colSpan, int rowSpan) {
            return addCell(null, null, null, null, false, colSpan, rowSpan);
        }

        /**
         * Adds a cell to the row with the given CSS class
         * @return TableCell
         */
        public TableCell addCell(String className) {
            return addCell(className, null, null, null, false, 0, 0);
        }

        /**
         * Adds a cell to the row with the given CSS class
         * @return TableCell
         */
        public TableCell addCell(String className, int colSpan) {
            return addCell(className, null, null, null, false, colSpan, 0);
        }

        /**
         * Adds a cell to the row with the given CSS class and align and vertical align attributes
         * @return TableCell
         */
        public TableCell addCell(String className, String align, String vAlign) {
            return addCell(className, null, align, vAlign, false, 0, 0);
        }

        /**
         * Adds a cell to the row with the given CSS class and nowrap flag
         * @return TableCell
         */
        public TableCell addCell(String className, boolean noWrap) {
            return addCell(className, null, null, null, noWrap, 0, 0);
        }

        /**
         * Adds a cell to the row with the given CSS class, align, vertical align attributes, & nowrap flag
         * @return TableCell
         */
        public TableCell addCell(String className, String style, String align, String vAlign, boolean noWrap) {
            return addCell(className, style, align, vAlign, noWrap, 0, 0);
        }

        /**
         * Adds a cell to the row with the given CSS class, align, vertical align attributes, nowrap flag, column span
         * @return TableCell
         */
        public TableCell addCell(String className, String style, String align, String vAlign, boolean noWrap, int colSpan) {
            return addCell(className, style, align, vAlign, noWrap, colSpan, 0);
        }

        /**
         * Adds a cell to the row with the given CSS class, align, vertical align attributes, nowrap flag, column span & row span
         * @return TableCell
         */
        public TableCell addCell(String className, String style, String align, String vAlign, boolean noWrap, int colSpan, int rowSpan) {
            cellCount++;
            TableCell cell = (TableCell) addMemberElement(new TableCell());
            cell.setClassName(className);
            cell.setStyle(style);
            cell.addAttribute("ALIGN", align);
            cell.addAttribute("VALIGN", vAlign);
            if (colSpan != 0) {
                cell.addAttribute("COLSPAN", colSpan + "");
            }
            if (rowSpan != 0) {
                cell.addAttribute("ROWSPAN", rowSpan + "");
            }
            if (noWrap) {
                cell.addAttribute("NOWRAP", "T");
            }
            cells.add(cell);
            return cell;
        }

        /**
         * Return the current cell count
         * @return int
         */
        public int getCellCount() {
            return cellCount;
        }

        /**
         * Get the current cell
         * @return TableCell
         */
        public TableCell getCurrentCell() {
            if (cells.size() > 0) {
                return (TableCell) cells.get(cells.size() - 1);
            }
            return null;
        }

        /**
         * Get a specific cell
         */
        public TableCell getCell(int cell) {
            if (cell >= 0 && cell < cells.size()) {
                return (TableCell) cells.get(cell);
            }
            return null;
        }

    }

    /**
     * TableCell (TD) element
     */
    public static class TableCell extends GenericHTMLElement {
        public TableCell() {
            super("TD");
        }
    }

}
