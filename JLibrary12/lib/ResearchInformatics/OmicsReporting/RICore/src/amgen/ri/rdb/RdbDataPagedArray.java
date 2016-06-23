package amgen.ri.rdb;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import amgen.ri.oracle.OraConnectionManager;
import amgen.ri.oracle.OraSQLManager;
import amgen.ri.rdb.example.scott.Emp;

/**
 * <p>Dynamic Relation Db Mapping</p>
 * <p>Description: </p>
 * @author Jeffrey McDowell
 * @version 1.0
 */

public class RdbDataPagedArray {
    public static final short MEMBER_LOOKUP = 0;
    public static final short SQL = 1;
    public static final short ALL = 2;

    SQLManagerIF sqlManager;
    String connectionPool;
    private Class regRdbClass;
    private short type;
    private String sql;
    private CurrentPage currentPage;

    public RdbDataPagedArray(Class regRdbClass, String lookupToken, String lookupTokenKey, short lookupType, SQLManagerIF sqlManager, String logonusername,
                             String connectionPool) {
        this.sqlManager = sqlManager;
        this.connectionPool = connectionPool;
        this.regRdbClass = regRdbClass;
        this.type = ALL;
        Register.getRegister().register(regRdbClass);

        if (lookupType == MEMBER_LOOKUP) {
            this.sql = Register.getRegister().getRegisteredStatement(regRdbClass, lookupToken, null, true, Register.SELECTALL);
            if (this.sql == null) {
                throw new NullPointerException("Unable to find generated SQL for " + regRdbClass + " by " + lookupToken);
            }
            type = SQL;
        } else {
            this.sql = lookupToken;
            type = lookupType;
        }
    }

    public boolean hasPagingBegun() {
        return (currentPage != null);
    }

    public int beginPaging(String sessionToken, int pageSize) throws Exception {
        cleanUp(sessionToken);
        String paginatingTable = "PG_" + sessionToken;
        String tableSQL = "CREATE GLOBAL TEMPORARY TABLE " + paginatingTable + " ON COMMIT PRESERVE ROWS AS ( " + sql + " )";
        int ret = sqlManager.executeUpdate(tableSQL, connectionPool);

        currentPage = new
            CurrentPage(paginatingTable, pageSize, ret, regRdbClass, sqlManager, null, connectionPool);
        return ret;
    }

    public void cleanUp() {
        cleanUp(null);
    }

    public void cleanUp(String sessionToken) {
        String paginatingTable;
        if (currentPage == null && sessionToken == null) {
            return;
        } else if (currentPage != null) {
            paginatingTable = currentPage.getPaginatingTable();
        } else if (sessionToken != null) {
            paginatingTable = "PG_" + sessionToken;
        } else {
            return;
        }
        try {
            sqlManager.executeUpdate("TRUNCATE TABLE " + paginatingTable, connectionPool);
            sqlManager.executeUpdate("DROP TABLE " + paginatingTable, connectionPool);
        } catch (SQLException ex) {
            //ex.printStackTrace();
        }
    }

    public RdbDataArray nextPage() throws Exception {
        if (currentPage == null) {
            throw new SQLException("No paging started");
        }
        return currentPage.nextPage();
    }

    public RdbDataArray previousPage() throws Exception {
        if (currentPage == null) {
            throw new SQLException("No paging started");
        }
        return currentPage.previousPage();
    }

    public RdbDataArray currentPage() throws Exception {
        if (currentPage == null) {
            throw new SQLException("No paging started");
        }
        return currentPage.currentPage();
    }

    public int currentRowNumber() throws Exception {
        if (currentPage == null) {
            throw new SQLException("No paging started");
        }
        return currentPage.getCurrentRow();
    }

    public int currentPageNumber() throws Exception {
        if (currentPage == null) {
            throw new SQLException("No paging started");
        }
        return currentPage.getCurrentPage();
    }

    public int getNumberOfRows() throws Exception {
        if (currentPage == null) {
            throw new SQLException("No paging started");
        }
        return currentPage.getNumberOfRows();
    }

    public int getNumberOfPages() throws Exception {
        if (currentPage == null) {
            throw new SQLException("No paging started");
        }
        return currentPage.getNumberOfPages();
    }

    public void finalize() throws Throwable {
        try {
            cleanUp();
        } catch (Exception e) {
        } finally {
            super.finalize();
        }
    }

    private class CurrentPage {
        protected Class regRdbClass;
        protected String paginatingTable;
        protected String paginatingColumn;
        protected SQLManagerIF sqlManager;
        protected String logonusername;
        protected String connectionPool;
        protected int pageSize;
        protected int maxRows;
        protected int currentRow;
        protected int currentPage;

        protected RdbDataArray currentPageArray;

        CurrentPage(String paginatingTable, int pageSize, int maxRows, Class regRdbClass, SQLManagerIF sqlManager, String logonusername, String connectionPool) throws
            SQLException {
            this.regRdbClass = regRdbClass;
            this.paginatingTable = paginatingTable;
            this.sqlManager = sqlManager;
            this.logonusername = logonusername;
            this.connectionPool = connectionPool;
            this.pageSize = pageSize;
            this.currentRow = 1;
            this.currentPage = 0;
            this.maxRows = maxRows;
            setTableInfo();
        }

        RdbDataArray currentPage() {
            if (currentPageArray == null) {
                setPage( -1);
            }
            return currentPageArray;
        }

        String getPaginatingTable() {
            return paginatingTable;
        }

        int getPageSize() {
            return pageSize;
        }

        int getCurrentRow() {
            return currentRow;
        }

        int getCurrentPage() {
            return currentPage;
        }

        int getNumberOfRows() {
            return maxRows;
        }

        int getNumberOfPages() {
            return (int) Math.ceil( (double) maxRows / (double) pageSize);
        }

        RdbDataArray nextPage() {
            if (currentPageArray == null) {
                currentPage = 1;
                return currentPage();
            }
            int pageStart = currentRow + pageSize + 1;
            setPage(pageStart);
            currentPage++;
            return currentPageArray;
        }

        RdbDataArray previousPage() {
            if (currentRow == 1) {
                currentPage = 1;
                return currentPage();
            }
            int pageStart = currentRow - pageSize;
            if (pageStart < 1) {
                pageStart = 1;
            }
            setPage(pageStart);
            currentPage--;
            return currentPageArray;
        }

        private void setTableInfo() throws SQLException {
            Connection conn = sqlManager.getConnection(connectionPool);
            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet rs = dbmd.getColumns(null, dbmd.getUserName(), paginatingTable.toUpperCase(), "%");
            if (rs.next()) {
                String dbObjectCatalog = rs.getString(1);
                String dbObjectSchema = rs.getString(2);
                String dbObjectName = rs.getString(3);
                String dbColumnName = rs.getString(4);
                String dbColumnTypeName = rs.getString(6);
                paginatingColumn = dbColumnName;
            }
            rs.close();
            OraSQLManager.closeResources(rs);
        }

        private void setPage(int pageStart) {
            if (pageStart < 0) {
                pageStart = 1;
            } else if (currentRow == pageStart) {
                return;
            }
            String sql = "SELECT " + paginatingColumn + " FROM (" +
                " SELECT " + paginatingColumn + ", ROWNUM R_N FROM " + paginatingTable + ") " +
                " WHERE R_N BETWEEN " + pageStart + " AND " + (pageStart + pageSize - 1);
            currentPageArray = new RdbDataArray(regRdbClass, sql, new String[0], sqlManager, logonusername, connectionPool);
            currentRow = pageStart;
        }

    }
}
