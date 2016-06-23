package amgen.ri.excel;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import amgen.ri.util.ExtString;
import amgen.ri.util.Region;
import java.util.HashMap;

/**
 * <p>@version $id$</p>
 *
 * <p> </p>
 *
 * <p> </p>
 *
 * <p> </p> not attributable
 */
public abstract class ExcelUtils {
    private boolean includeComments;
    private SpreadsheetVersion spreadsheetVersion;
    public static String EXCEL2003_MIMETYPE = "application/vnd.ms-excel";
    public static String EXCEL2007_MIMETYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public static String EXCEL2003_EXTENSION = "xls";
    public static String EXCEL2007_EXTENSION = "xlsx";

    public ExcelUtils() {
        super();
        includeComments = true;
    }

    public ExcelUtils(SpreadsheetVersion spreadsheetVersion) {
        this();
        this.spreadsheetVersion = spreadsheetVersion;
    }

    public static Workbook readWorkbook(String excelFileName) throws IOException {
        return readWorkbook(new File(excelFileName));
    }

    public static Workbook readWorkbook(File excelFileName) throws IOException {
        Workbook wb = null;
        try {
            wb = new HSSFWorkbook(new FileInputStream(excelFileName));
        } catch (Exception e) {}
        if (wb == null) {
            try {
                wb = new XSSFWorkbook(new FileInputStream(excelFileName));
            } catch (Exception e) {}
        }
        if (wb == null) {
            throw new IOException("Unable to open file " + excelFileName);
        }
        return wb;
    }

    public static Workbook readWorkbook(byte[] in) throws IOException {
        Workbook wb = null;
        try {
            wb = new HSSFWorkbook(new ByteArrayInputStream(in));
        } catch (Exception e) {}
        if (wb == null) {
            try {
                wb = new XSSFWorkbook(new ByteArrayInputStream(in));
            } catch (Exception e) {}
        }
        if (wb == null) {
            throw new IOException("Unable to open file");
        }
        return wb;
    }

    public static Workbook readWorkbook(InputStream in) throws IOException {
        Workbook wb = null;
        try {
            wb = new HSSFWorkbook(in);
        } catch (Exception e) {}
        if (wb == null) {
            try {
                wb = new XSSFWorkbook(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (wb == null) {
            throw new IOException("Unable to open workbook");
        }
        return wb;
    }

    public static File writeWorkbook(Workbook wb, File outputFolderFile, String outputFileBaseName, boolean launchExcel) throws IOException {
        SpreadsheetVersion version;
        if (wb instanceof XSSFWorkbook) {
            version = SpreadsheetVersion.EXCEL2007;
        } else {
            version = SpreadsheetVersion.EXCEL97;
        }

        File outputFile;
        switch (version) {
            case EXCEL97:
                outputFile = new File(outputFolderFile, outputFileBaseName + ".xls");
                break;
            default:
                outputFile = new File(outputFolderFile, outputFileBaseName + ".xlsx");
                break;
        }
        FileOutputStream out = new FileOutputStream(outputFile);
        wb.write(out);
        out.close();
        if (launchExcel) {
            launchExcel(outputFile);
        }
        return outputFile;
    }

    public static File writeWorkbook(Workbook wb, File outputFolderFile, String outputFileBaseName) throws IOException {
        return writeWorkbook(wb, outputFolderFile, outputFileBaseName, false);
    }

    public Row getRow(Sheet sheet, int rowNum) {
        Row row = sheet.getRow(rowNum);
        return (row == null ? sheet.createRow(rowNum) : row);
    }

    public static String getCellStringValue(Map<String, Integer> columnHeaderMap, String header, Row row) {
        header = header.toUpperCase().replaceAll("\\s", "_");
        if (columnHeaderMap.get(header) == null) {
            return null;
        }
        return getCellStringValue(row, columnHeaderMap.get(header));
    }

    public static String getCellStringValue(Row row, int colNum) {
        try {
            if (row == null) {
                return null;
            }
            Cell cell = row.getCell(colNum);
            if (cell == null) {
                return null;
            }
            Object value;
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC:
                    value = cell.getNumericCellValue();
                    break;
                case Cell.CELL_TYPE_ERROR:
                    return null;
                case Cell.CELL_TYPE_STRING:
                default:
                    value = cell.getStringCellValue();
            }
            return (value == null ? null : value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    public static Double getCellNumericValue(Map<String, Integer> columnHeaderMap, String header, Row row) {
        header = header.toUpperCase().replaceAll("\\s", "_");
        if (columnHeaderMap.get(header) == null) {
            return Double.NaN;
        }
        return getCellNumericValue(row, columnHeaderMap.get(header));
    }

    public static Double getCellNumericValue(Row row, int colNum) {
        try {
            Cell cell = row.getCell(colNum);
            if (cell == null) {
                return Double.NaN;
            }
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC:
                    return cell.getNumericCellValue();
                case Cell.CELL_TYPE_ERROR:
                    return Double.NaN;
                case Cell.CELL_TYPE_STRING:
                default:
                    return ExtString.toDouble(cell.getStringCellValue());
            }
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    public static Map<String, Integer> getColumnHeaderMap(Sheet sheet) {
        Map<String, Integer> columnHeaderMap = new LinkedHashMap<String, Integer> ();
        for (Cell cell : sheet.getRow(0)) {
            String header = cell.getStringCellValue().toUpperCase().replaceAll("\\s", "_");
            columnHeaderMap.put(header, cell.getColumnIndex());
        }
        return columnHeaderMap;
    }

    public static int getColumnIndex(Map<String, Integer> columnHeaderMap, String columnName) {
        String header = columnName.toUpperCase().replaceAll("\\s", "_");
        if (columnHeaderMap.containsKey(header)) {
            return columnHeaderMap.get(header);
        }
        return -1;
    }

    /**
     * Returns the column header (headers from row #1) for the given Sheet
     *
     * @param sheet Sheet
     * @return List
     */
    public static List<String> getColumnHeaders(Sheet sheet) {
        List<String> headers = new ArrayList<String> ();
        if (sheet.getRow(0) != null) {
            for (Cell cell : sheet.getRow(0)) {
              cell.getColumnIndex();
                headers.add(cell.getStringCellValue());
            }
        }
        return headers;
    }


    /**
     * Returns a Map of column index : the column header for the given Sheet
     *
     * @param sheet Sheet
     * @return List
     */
    public static Map<Integer, String> getColumnHeaderIndexMap(Sheet sheet) {
        Map<Integer, String> headers = new LinkedHashMap<Integer, String> ();
        if (sheet.getRow(0) != null) {
            for (Cell cell : sheet.getRow(0)) {
                headers.put(cell.getColumnIndex(), cell.getStringCellValue());
            }
        }
        return headers;
    }

    /**
     * Returns the multiple headers (parent headers from row #1 and children in row #2) for the given Sheet
     *
     *
     * @param sheet Sheet
     * @return List
     */
    public static List<GroupHeader> getGroupedColumnHeaders(Sheet sheet) {
        Map<Integer, GroupHeader> headers = new LinkedHashMap<Integer, GroupHeader> ();
        Row parentHeaderRow = sheet.getRow(0);
        Row headerRow = sheet.getRow(1);
        if (sheet.getRow(0) == null || sheet.getRow(1) == null) {
            return null;
        }

        List<CellRangeAddress> mergedCells = new ArrayList<CellRangeAddress> ();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            try {
                CellRangeAddress region = sheet.getMergedRegion(i);
                if (region.getFirstRow() == 0 && region.getLastRow() == 0) {
                    mergedCells.add(region);
                }
            } catch (Exception e) {}
        }

        for (Cell cell : headerRow) {
            int colIndex = cell.getColumnIndex();
            Cell parentCell = parentHeaderRow.getCell(cell.getColumnIndex());
            int parentCellStartCol = cell.getColumnIndex();
            int parentCellEndCol = cell.getColumnIndex();
            for (CellRangeAddress mergedCell : mergedCells) {
                int startCol = mergedCell.getFirstColumn();
                int endCol = mergedCell.getLastColumn();
                if (colIndex >= startCol && colIndex <= endCol) {
                    parentCell = parentHeaderRow.getCell(startCol);
                    parentCellStartCol = startCol;
                    parentCellEndCol = endCol;
                }
            }
            if (!headers.containsKey(parentCellStartCol)) {
                headers.put(parentCellStartCol, new GroupHeader(new Region(parentCellStartCol, parentCellEndCol), parentCell.getStringCellValue()));
            }
            headers.get(parentCellStartCol).addChild(new GroupHeader(new Region(colIndex, colIndex), cell.getStringCellValue()));
        }
        return new ArrayList<GroupHeader> (headers.values());
    }

    /**
     * Returns the cell types for the given row
     *
     * @param row Row
     * @return List
     */
    public static List<Integer> getRowCellTypes(Row row) {
        List<Integer> cellTypes = new ArrayList<Integer> ();
        if (row != null) {
            for (Cell cell : row) {
                cellTypes.add(cell.getCellType());
            }
        }
        return cellTypes;
    }

    public String getColumnAddress(Map<String, Integer> columnHeaderMap, String columnName) {
        return CellReference.convertNumToColString(columnHeaderMap.get(columnName));
    }

    public Cell getCellValue(Map<String, Integer> columnHeaderMap, Row row, String columnName) {
        Integer columnIndex = columnHeaderMap.get(columnName);
        if (columnIndex == null) {
            return null;
        }
        return row.getCell(columnIndex);
    }

    public void autoSizeAllColumns(Sheet sheet) {
        for (Cell cell : sheet.getRow(0)) {
            sheet.autoSizeColumn(cell.getColumnIndex());
        }
    }

    public Cell setCellFormula(Map<String, Integer> columnHeaderMap, Row row, String columnName, String formula) {
        Integer columnIndex = columnHeaderMap.get(columnName);
        if (columnIndex == null) {
            return null;
        }
        Cell cell = row.createCell(columnIndex);
        cell.setCellFormula(formula);
        return cell;
    }

    /**
     *
     * @param row Row
     * @param columnIndex int
     * @param value Object
     */
    public Cell setCellValue(Sheet sheet, int rowNum, int columnIndex, Object value) {
        Row row = getRow(sheet, rowNum);
        return setCellValue(row, columnIndex, value, null, null, null);
    }

    /**
     *
     * @param row Row
     * @param columnIndex int
     * @param value Object
     */
    public Cell setCellValue(Row row, int columnIndex, Object value) {
        return setCellValue(row, columnIndex, value, null, null, null);
    }

    /**
     *
     * @param row Row
     * @param columnIndex int
     * @param value Object
     * @param style CellStyle
     */
    public Cell setCellValue(Row row, int columnIndex, Object value, CellStyle style) {
        return setCellValue(row, columnIndex, value, null, null, style);
    }

    /**
     * setCellValue
     *
     * @param row Row
     * @param i int
     * @param d double
     */
    public Cell setCellValue(Row row, int columnIndex, Object value, String cellComments, Drawing drawing, CellStyle style) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            cell = row.createCell(columnIndex);
        }
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            if (ExtString.isANumber(value.toString())) {
                double d = ExtString.toDouble(value.toString());
                if (Double.isNaN(d)) {
                    cell.setCellValue("NA");
                } else {
                    cell.setCellValue(d);
                }
            } else {
                cell.setCellValue( (String) value);
            }
        } else if (value instanceof Character) {
            cell.setCellValue(value.toString());
        } else if (value instanceof Enum) {
            cell.setCellValue(value.toString());
        } else if (value instanceof Number) {
            double d = ( (Number) value).doubleValue();
            if (Double.isNaN(d)) {
                cell.setCellValue("NA");
            } else {
                cell.setCellValue(d);
            }
        }
        if (cellComments != null && ExtString.hasLength(cellComments.toString()) && includeComments()) {
            Workbook wb = row.getSheet().getWorkbook();
            CreationHelper factory = wb.getCreationHelper();
            ClientAnchor anchor = factory.createClientAnchor();
            anchor.setCol1(cell.getColumnIndex());
            anchor.setRow1(row.getRowNum());
            anchor.setCol2(cell.getColumnIndex() + 3);
            anchor.setRow2(row.getRowNum() + 5);
            Comment comment = drawing.createCellComment(anchor);
            RichTextString str = factory.createRichTextString(cellComments.toString());
            comment.setString(str);
            comment.setAuthor("Jeff");
            cell.setCellComment(comment);
        }

        if (style != null) {
            cell.setCellStyle(style);
        }
        return cell;
    }

    public Cell setCellValue(Row row, Map<String, Integer> columnHeaderMap, String columnName, Object value, Object cellComments, Drawing drawing) {
        return setCellValue(row, columnHeaderMap, columnName, value, cellComments, drawing, null);
    }

    public Cell setCellValue(Row row, Map<String, Integer> columnHeaderMap, String columnName, Object value, Object cellComments, Drawing drawing,
                             CellStyle style) {
        Integer columnIndex = columnHeaderMap.get(normalizeColumnHeader(columnName));
        if (columnIndex == null) {
            return null;
        }
        Cell cell = null;
        if (value == null) {
            cell = row.createCell(columnIndex);
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell = row.createCell(columnIndex);
            cell.setCellValue( (String) value);
        } else if (value instanceof Character) {
            cell = row.createCell(columnIndex);
            cell.setCellValue(value.toString());
        } else if (value instanceof Enum) {
            cell = row.createCell(columnIndex);
            cell.setCellValue(value.toString());
        } else if (value instanceof Number) {
            cell = row.createCell(columnIndex);
            double d = ( (Number) value).doubleValue();
            if (Double.isNaN(d)) {
                cell.setCellValue("NA");
            } else {
                cell.setCellValue(d);
            }
        } else if (value instanceof Cell) {
            cell = row.createCell(columnIndex);
            Cell sourceCell = (Cell) value;
            switch (sourceCell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC:
                    double numVal = sourceCell.getNumericCellValue();
                    cell.setCellValue(numVal);
                    break;
                case Cell.CELL_TYPE_ERROR:
                    return null;
                case Cell.CELL_TYPE_STRING:
                default:
                    String strVal = sourceCell.getStringCellValue();
                    cell.setCellValue(strVal);
            }
        }
        if (style != null) {
            cell.setCellStyle(style);
        }

        if (cellComments != null && ExtString.hasLength(cellComments.toString()) && includeComments()) {
            Workbook wb = row.getSheet().getWorkbook();
            CreationHelper factory = wb.getCreationHelper();
            ClientAnchor anchor = factory.createClientAnchor();
            anchor.setCol1(cell.getColumnIndex());
            anchor.setRow1(row.getRowNum());
            anchor.setCol2(cell.getColumnIndex() + 3);
            anchor.setRow2(row.getRowNum() + 5);
            Comment comment = drawing.createCellComment(anchor);
            RichTextString str = factory.createRichTextString(cellComments.toString());
            comment.setString(str);
            comment.setAuthor("Jeff");
            cell.setCellComment(comment);
        }
        return cell;
    }

    public Cell copyCell(Cell sourceCell, Cell targetCell) {
        switch (sourceCell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                double numVal = sourceCell.getNumericCellValue();
                targetCell.setCellValue(numVal);
                break;
            case Cell.CELL_TYPE_ERROR:
                return null;
            case Cell.CELL_TYPE_STRING:
            default:
                String strVal = sourceCell.getStringCellValue();
                targetCell.setCellValue(strVal);
        }
        return targetCell;
    }

    public Row copyRow(Row sourceRow, Row targetRow) {
        for (Cell sourceCell : sourceRow) {
            if (sourceCell != null &&
                (sourceCell.getCellType() == Cell.CELL_TYPE_NUMERIC || sourceCell.getCellType() == Cell.CELL_TYPE_STRING)) {
                Cell targetCell = targetRow.getCell(sourceCell.getColumnIndex());
                if (targetCell == null) {
                    targetCell = targetRow.createCell(sourceCell.getColumnIndex());
                }
                copyCell(sourceCell, targetCell);
            }
        }
        return targetRow;
    }

    public String normalizeColumnHeader(String header) {
        if (header == null) {
            return null;
        }
        return header.toUpperCase().replaceAll("\\s", "_");
    }

    public String getSheetName(String prefix, String base, String suffix) {
        if (!ExtString.hasLength(base)) {
            throw new IllegalArgumentException("Illegal Sheet Name");
        }
        StringBuffer name = new StringBuffer();
        if (ExtString.hasLength(prefix)) {
            name.append(prefix + " ");
        }
        name.append(base);
        if (ExtString.hasLength(suffix)) {
            name.append("-");
            name.append(suffix);
        }

        if (name.length() > 30) {
            int overflow = name.length() - 30;
            if (overflow > base.length()) {
                throw new IllegalArgumentException("Illegal Sheet Name: Name too long");
            }
            return getSheetName(prefix, base.substring(0, base.length() - overflow), suffix);
        }

        return name.toString();

    }

    public void setIncludeComments(boolean includeComments) {
        this.includeComments = includeComments;
    }

    public void setSpreadsheetVersion(SpreadsheetVersion spreadsheetVersion) {
        this.spreadsheetVersion = spreadsheetVersion;
    }

    public boolean includeComments() {
        return includeComments;
    }

    public static void launchExcel(File excelFile) throws IOException {
        if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") > -1) {
            Runtime.getRuntime().exec("cmd /c start excel.exe /e " + excelFile);
        }
    }

    public static List<String> getSheetNames(File file) throws IOException {
        class OpenExcel extends ExcelUtils {
            OpenExcel() {
                super();
            }
        };
        Workbook wb = new OpenExcel().readWorkbook(file);
        List<String> sheetNames = new ArrayList<String> ();
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            sheetNames.add(wb.getSheetName(i));
        }
        return sheetNames;
    }

    public static SpreadsheetVersion getSpreadsheetVersion(File file) throws IOException {
        class OpenExcel extends ExcelUtils {
            OpenExcel() {
                super();
            }
        };
        Workbook wb = new OpenExcel().readWorkbook(file);
        if (wb instanceof XSSFWorkbook) {
            return SpreadsheetVersion.EXCEL2007;
        } else {
            return SpreadsheetVersion.EXCEL97;
        }
    }

    /**
     * Returns which version is the Spreadsheet provided by a byte array
     *
     * @param excelBytes byte[]
     * @return SpreadsheetVersion
     * @throws IOException
     */
    public static SpreadsheetVersion getSpreadsheetVersion(byte[] excelBytes) throws IOException {
        class OpenExcel extends ExcelUtils {
            OpenExcel() {
                super();
            }
        };
        Workbook wb = new OpenExcel().readWorkbook(excelBytes);
        if (wb instanceof XSSFWorkbook) {
            return SpreadsheetVersion.EXCEL2007;
        } else {
            return SpreadsheetVersion.EXCEL97;
        }
    }

    /**
     * Returns the mimetype for a version
     *
     */
    public static String getMimetype(SpreadsheetVersion version) {
        switch (version) {
            case EXCEL2007:
                return EXCEL2007_MIMETYPE;
            default:
                return EXCEL2003_MIMETYPE;
        }
    }

    /**
     * Returns the extension for a version
     *
     */
    public static String getExtension(SpreadsheetVersion version) {
        switch (version) {
            case EXCEL2007:
                return EXCEL2007_EXTENSION;
            default:
                return EXCEL2003_EXTENSION;
        }
    }

    public SpreadsheetVersion getSpreadsheetVersion() {
        return spreadsheetVersion;
    }

    public static void main(String[] args) throws Exception {

    }

}
