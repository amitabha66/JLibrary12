package amgen.ri.json;


import amgen.ri.util.ExtString;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @version $id$
 *
 */
public class JSONTable2Excel {
    public JSONTable2Excel() {
    }

    public Workbook createExcel(JSONObject table) throws JSONException {
        Workbook wb = new XSSFWorkbook();

        CellStyle columnHeaderXLStyle = wb.createCellStyle();
        Font columnHeaderXLFont = wb.createFont();
        columnHeaderXLFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        columnHeaderXLStyle.setFont(columnHeaderXLFont);

        Sheet sheet = wb.createSheet(ExtString.truncate(ExtString.getSafeFileName(table.optString("title", "Sheet 1")), 30, false));
        int rowCounter = 0;
        short cellCounter = 0;
        JSONArray columns = table.getJSONArray("columns");
        Row columnHeaderXLRow = sheet.createRow(rowCounter++);
        for (int i = 0; i < columns.length(); i++) {
            JSONObject column = columns.getJSONObject(i);
            String header = column.getString("header");
            Cell columnGroupXLCell = columnHeaderXLRow.createCell(cellCounter++);
            columnGroupXLCell.setCellStyle(columnHeaderXLStyle);
            columnGroupXLCell.setCellValue(StringEscapeUtils.unescapeXml(header));
        }
        JSONArray rows = table.getJSONArray("rows");

        for (int i = 0; i < rows.length(); i++) {
            JSONArray row = rows.getJSONArray(i);
            Row xlRow = sheet.createRow(rowCounter++);
            xlRow.setHeightInPoints((float) 12.75);
            cellCounter = 0;
            for (int j = 0; j < row.length(); j++) {
                Cell xlDataCell = xlRow.createCell(cellCounter++);
                Object cell = row.get(j);
                String value;
                if (cell== null || ExtString.equals(cell+"", "null")) {
                  value="";
                } else if (cell instanceof JSONObject) {
                    value= "";
                } else {
                    value = ExtString.toString(cell, "");
                }
                
                if (ExtString.isANumber(value)) {
                    xlDataCell.setCellValue(ExtString.toDouble(value));
                } else {
                    xlDataCell.setCellValue(StringEscapeUtils.unescapeXml(value.replaceAll("\\s+", " ")));
                }
            }
        }
        return wb;
    }
}
