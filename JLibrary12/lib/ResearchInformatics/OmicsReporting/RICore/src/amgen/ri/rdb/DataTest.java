package amgen.ri.rdb;

import amgen.ri.oracle.OraConnectionManager;
import amgen.ri.oracle.OraSQLManager;

public class DataTest {
    String jdbcURL = "jdbc:oracle:thin:aigdev/!potB9ytCgPE=@ussf-tdbx-ddb01:1521:SFDSC01T.amgen.com";

    public static void main(String[] args) {
        try {
            new DataTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DataTest() throws Exception {
        OraConnectionManager.addConnectionPool("jsbc", jdbcURL);
        OraSQLManager sqlManager = new OraSQLManager();

    }
}
