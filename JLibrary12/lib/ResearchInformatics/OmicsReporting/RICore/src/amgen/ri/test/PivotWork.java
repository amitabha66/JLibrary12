package amgen.ri.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import oracle.jdbc.OraclePreparedStatement;
import amgen.ri.oracle.OraConnectionManager;
import amgen.ri.oracle.OraSQLManager;

/**
 * <p>Title: Dynamic Relation Db Mapping</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2010</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class PivotWork {
    String PARENT_COLUMNNAMES_SQL =
        "SELECT DISTINCT QRET.DISPLAY_ORDER, QRET.DISPLAY_ORDER || '.' || QRET.NAME  || '.' || " +
        "DECODE(QRET.CHILD_QUERY_RUN_ID, NULL, 'NO', 'YES') PARENT_COLUMN_NAME " +
        "FROM QUERY_RUN_ENTITY_TABLE_VW QRET ,QUERY_RUN_ENTITY_KEYS QREK ,QUERY_RUN_ENTITY_TABLE_VW QRET2 " +
        "WHERE QRET.QUERY_RUN_ID = :query_run_id " +
        "AND QRET.CHILD_QUERY_RUN_ID = QREK.QUERY_RUN_ID (+) " +
        "AND QRET.ENTITY_KEY = QREK.ENTITY_KEY1 (+) " +
        "AND QREK.ENTITY_KEY = QRET2.ENTITY_KEY (+) " +
        "AND QREK.QUERY_RUN_ID = QRET2.QUERY_RUN_ID (+) " +
        "ORDER BY  QRET.DISPLAY_ORDER ";

    String SUBTABLE_COLUMNNAMES_SQL =
        "SELECT DISTINCT QRET2.DISPLAY_ORDER DISPLAY_ORDER, QRET2.DISPLAY_ORDER || '.' || QRET2.NAME SUBTABLE_COLUMN_NAME " +
        "FROM QUERY_RUN_ENTITY_TABLE_VW QRET, QUERY_RUN_ENTITY_KEYS QREK, QUERY_RUN_ENTITY_TABLE_VW QRET2 " +
        "WHERE QRET.QUERY_RUN_ID = :query_run_id " +
        "AND QRET.CHILD_QUERY_RUN_ID = QREK.QUERY_RUN_ID (+) " +
        "AND QRET.ENTITY_KEY = QREK.ENTITY_KEY1 (+) " +
        "AND QREK.ENTITY_KEY = QRET2.ENTITY_KEY (+) " +
        "AND QREK.QUERY_RUN_ID = QRET2.QUERY_RUN_ID (+) " +
        "AND QRET.NAME || '$' || QRET.DISPLAY_ORDER = :parent_column_name " +
        "AND QRET2.NAME IS NOT NULL " +
        "ORDER BY   QRET2.DISPLAY_ORDER";

    String PARENT_PIVOT_SQL =
        "SELECT * FROM ( " +
        "  SELECT QRET.ENTITY_KEY ENTITY_KEY, QRET.DISPLAY_ORDER || QRET.NAME  PARENT_COLUMN_NAME, QRET.VALUE_DATA PARENT_COLUMN_VALUE " +
        "    FROM QUERY_RUN_ENTITY_TABLE_VW QRET ,QUERY_RUN_ENTITY_KEYS QREK ,QUERY_RUN_ENTITY_TABLE_VW QRET2 " +
        "    WHERE QRET.QUERY_RUN_ID = :query_run_id " +
        "    AND QRET.CHILD_QUERY_RUN_ID = QREK.QUERY_RUN_ID (+) " +
        "    AND QRET.ENTITY_KEY = QREK.ENTITY_KEY1 (+) " +
        "    AND QREK.ENTITY_KEY = QRET2.ENTITY_KEY (+) " +
        "    AND QREK.QUERY_RUN_ID = QRET2.QUERY_RUN_ID (+) " +
        "    ORDER BY " +
        "    QRET.DISPLAY_ORDER " +
        "    ,TO_NUMBER(QRET.ENTITY_KEY) " +
        "    ,QRET2.DISPLAY_ORDER " +
        "    ,QRET2.ENTITY_KEY) " +
        "    PIVOT (MAX(PARENT_COLUMN_VALUE) FOR  PARENT_COLUMN_NAME IN ([[COLUMNS]]) " +
        "  )";

    String SUBTABLE_TRANSPOSE_SQL =
        "SELECT ENTITY_KEY, [[SELECT_COLUMNS]] " +
        "FROM " +
        "  ( SELECT " +
        "      ENTITY_KEY , " +
        "      NVL( SUBTABLE_COLUMN_NAME, '%' ) AS SUBTABLE_COLUMN_NAME, " +
        "      ROW_NUMBER() " +
        "        OVER( PARTITION BY ENTITY_KEY, SUBTABLE_COLUMN_NAME ORDER BY SUBTABLE_COLUMN_VALUE ) " +
        "        AS ROW_NUM , " +
        "      SUBTABLE_COLUMN_VALUE " +
        "    FROM  " +
        "(SELECT QRET.ENTITY_KEY, QRET2.DISPLAY_ORDER DISPLAY_ORDER, QRET2.NAME SUBTABLE_COLUMN_NAME, " +
        "        QRET2.VALUE_DATA SUBTABLE_COLUMN_VALUE " +
        "FROM  " +
        "QUERY_RUN_ENTITY_TABLE_VW QRET " +
        ",QUERY_RUN_ENTITY_KEYS QREK " +
        ",QUERY_RUN_ENTITY_TABLE_VW QRET2 " +
        "WHERE QRET.QUERY_RUN_ID = :query_run_id " +
        "    AND QRET.CHILD_QUERY_RUN_ID = QREK.QUERY_RUN_ID (+) " +
        "    AND QRET.ENTITY_KEY = QREK.ENTITY_KEY1 (+)    " +
        "    AND QREK.ENTITY_KEY = QRET2.ENTITY_KEY (+) " +
        "    AND QREK.QUERY_RUN_ID = QRET2.QUERY_RUN_ID (+) " +
        "    AND QRET.NAME || '$' || QRET.DISPLAY_ORDER = :parent_column_name " +
        ")  " +
        "  ) T3A PIVOT " +
        "    ( MAX(SUBTABLE_COLUMN_VALUE) " +
        "      FOR SUBTABLE_COLUMN_NAME IN " +
        "        ([[COLUMNS]], '%'  AS UNASSIGNED " +
        "        ) " +
        "    ) " +
        "ORDER BY ENTITY_KEY, ROW_NUM";

    int queryRunID;
    String queryRunSubVar = "query_run_id";
    String parentColumnNameSubVar = "parent_column_name";

    public PivotWork(int queryRunID) throws Exception {
        this.queryRunID = queryRunID;
        Connection conn = new OraSQLManager().getConnection("vqt");

        List<VQTColumn> parentColumns = getParentColumns(conn, new ArrayList<VQTColumn> ());
        List<List<String>> values = parentPivot(conn, parentColumns);
        System.out.print("ENTITYKEY" + "\t");

        for (VQTColumn parentColumn : parentColumns) {
            System.out.print(parentColumn.name + "\t");
        }
        System.out.println();
        for (List<String> row : values) {
            for (int i = 0; i < row.size(); i++) {
                String cell = row.get(i);
                if (i > 0) {
                    VQTColumn parentColumn = parentColumns.get(i - 1);
                    if (parentColumn.hasSubtable) {
                        List<VQTColumn> subtableColumns = getSubtableColumns(conn, parentColumn, new ArrayList<VQTColumn> ());
                        List<List<String>> subtableValues = transposeSubtable(conn, parentColumn, subtableColumns);
                        System.out.print("[");
                        for (VQTColumn subtableColumn : subtableColumns) {
                            System.out.print(subtableColumn.name + ",");
                        }
                        for (List<String> subtableRow : subtableValues) {
                            System.out.print(";");
                            for (int j = 0; j < subtableRow.size(); j++) {
                                String subtableCell = subtableRow.get(j);
                                System.out.print(subtableCell + ",");
                            }
                        }
                    System.out.print("]\t");
                } else {
                    System.out.print(cell + "\t");
                }
            } else {
                System.out.print(cell + "\t");
            }
        }
        System.out.println();
    }
}

/**
 * getParentColumns
 *
 * @param columnNamesSQL String
 * @param string String
 * @param string1 String
 * @param parentColumnNames List
 */
private List<VQTColumn> getParentColumns(Connection conn, List<VQTColumn> results) throws SQLException {
    OraclePreparedStatement stmt = (OraclePreparedStatement) conn.prepareStatement(PARENT_COLUMNNAMES_SQL);
    stmt.setIntAtName(queryRunSubVar, queryRunID);
    ResultSet rset = stmt.executeQuery();
    while (rset.next()) {
        String column = rset.getString("PARENT_COLUMN_NAME");
        results.add(new VQTColumn(column));
    }
    stmt.close();
    return results;
}

/**
 * getParentColumns
 *
 * @param columnNamesSQL String
 * @param string String
 * @param string1 String
 * @param parentColumnNames List
 */
private List<VQTColumn> getSubtableColumns(Connection conn, VQTColumn parentColumn, List<VQTColumn> results) throws SQLException {
    OraclePreparedStatement stmt = (OraclePreparedStatement) conn.prepareStatement(SUBTABLE_COLUMNNAMES_SQL);

    stmt.setIntAtName(queryRunSubVar, queryRunID);
    stmt.setStringAtName(parentColumnNameSubVar, parentColumn.name + "$" + parentColumn.displayOrder);
    ResultSet rset = stmt.executeQuery();
    while (rset.next()) {
        String column = rset.getString("SUBTABLE_COLUMN_NAME");
        results.add(new VQTColumn(column));
    }
    stmt.close();
    return results;
}

private List<List<String>> parentPivot(Connection conn, List<VQTColumn> parentColumns) throws SQLException {
    List<List<String>> values = new ArrayList<List<String>> ();

    StringBuffer pivotColumns = new StringBuffer();
    for (VQTColumn parentColumn : parentColumns) {
        if (pivotColumns.length() > 0) {
            pivotColumns.append(",");
        }
        pivotColumns.append("'" + parentColumn.displayOrder + parentColumn.name + "' as " + parentColumn.validName + "$" + parentColumn.displayOrder);
    }
    String parentPivotSQL = PARENT_PIVOT_SQL.replace("[[COLUMNS]]", pivotColumns);
    OraclePreparedStatement parentPivotStmt = (OraclePreparedStatement) conn.prepareStatement(parentPivotSQL);
    parentPivotStmt.setIntAtName(queryRunSubVar, queryRunID);

    ResultSet parentPivotRset = parentPivotStmt.executeQuery();
    while (parentPivotRset.next()) {
        String entityID = parentPivotRset.getString("ENTITY_KEY");
        List<String> row = new ArrayList<String> ();
        values.add(row);
        row.add(entityID);
        for (VQTColumn parentColumn : parentColumns) {
            String value = parentPivotRset.getString(parentColumn.validName + "$" + parentColumn.displayOrder);
            row.add(value);
        }

    }
    parentPivotStmt.close();
    return values;

}

private List<List<String>> transposeSubtable(Connection conn, VQTColumn parentColumn, List<VQTColumn> subtableColumns) throws SQLException {
    List<List<String>> values = new ArrayList<List<String>> ();
    if (subtableColumns.size() == 0) {
        return values;
    }

    StringBuffer pivotColumns = new StringBuffer();
    StringBuffer pivotSelectColumns = new StringBuffer();
    for (VQTColumn subtableColumn : subtableColumns) {
        if (pivotColumns.length() > 0) {
            pivotColumns.append(",");
        }
        if (pivotSelectColumns.length() > 0) {
            pivotSelectColumns.append(",");
        }
        pivotSelectColumns.append("\"" + subtableColumn.name + "\"");
        pivotColumns.append("'" + subtableColumn.name + "' as \"" + subtableColumn.name + "\"");
    }

    String transposeSubtableSQL = SUBTABLE_TRANSPOSE_SQL.replace("[[COLUMNS]]", pivotColumns);
    transposeSubtableSQL = transposeSubtableSQL.replace("[[SELECT_COLUMNS]]", pivotSelectColumns);

    OraclePreparedStatement transposeSubtableStmt = (OraclePreparedStatement) conn.prepareStatement(transposeSubtableSQL);
    transposeSubtableStmt.setIntAtName(queryRunSubVar, queryRunID);
    transposeSubtableStmt.setStringAtName(parentColumnNameSubVar, parentColumn.name + "$" + parentColumn.displayOrder);

    ResultSet transposeSubtableRset = transposeSubtableStmt.executeQuery();
    while (transposeSubtableRset.next()) {
        String entityID = transposeSubtableRset.getString("ENTITY_KEY");
        List<String> row = new ArrayList<String> ();
        values.add(row);
        row.add(entityID);
        for (VQTColumn subtableColumn : subtableColumns) {
            String value = transposeSubtableRset.getString(subtableColumn.name);
            row.add(value);
        }

    }
    transposeSubtableStmt.close();
    return values;

}

public static void main(String[] args) throws Exception {
    OraConnectionManager.addConnectionPool("vqt",
                                                "jdbc:oracle:thin:VQT/!iMeQ7A1/V3Js1CzCEwjg7g==@Uswa-ddbx-ora10.amgen.com:1521:Wa0630d.amgen.com");

    PivotWork pivotwork = new PivotWork(21918);
}
}

    class VQTColumn {
    int displayOrder;
    String name;
    String validName;
    boolean hasSubtable;

    VQTColumn(String column) {
        String[] split = column.split("\\.");
        switch (split.length) {
            case 3:
                hasSubtable = split[2].equals("YES");
            case 2:
                name = split[1];
            case 1:
                displayOrder = Integer.valueOf(split[0]);
        }
        this.validName = name.replaceAll("\\W", "_");
    }

    public String toString() {
        return displayOrder + name;
    }
}
