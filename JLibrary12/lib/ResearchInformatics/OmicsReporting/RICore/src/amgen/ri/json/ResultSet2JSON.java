package amgen.ri.json;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 *
 * @author jemcdowe
 */
public class ResultSet2JSON {
  private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");

  public ResultSet2JSON() {
  }

  public ResultSet2JSON(SimpleDateFormat dateFormat) {
    this.dateFormat= dateFormat;
  }

  /**
   * This process the ResultSet appending the columns by column label to the
   * results JSONObject using the resultKey as an array key.
   *
   * @param rset
   * @param results
   * @param resultKey
   * @return
   * @throws SQLException
   * @throws JSONException
   */
  public JSONObject processResults(ResultSet rset, String resultKey) throws SQLException, JSONException {
    return processResults(rset, new JSONObject(), null, resultKey);
  }

  /**
   * This process the ResultSet appending the columns by column label to the
   * results JSONObject using the resultKey as an array key.
   *
   * @param rset
   * @param results
   * @param resultKey
   * @return
   * @throws SQLException
   * @throws JSONException
   */
  public JSONObject processResults(ResultSet rset, JSONObject results, String resultKey) throws SQLException, JSONException {
    return processResults(rset, results, null, resultKey);
  }

  /**
   * This process the ResultSet appending the columns by column label to the
   * results JSONObject using the resultKey as an array key.
   *
   * The columnName2FieldNameMap provides a means to map a reported column name
   * to the field in the result JSONObject. If it is null or the column name not
   * in the Map, the column name is used as the field name
   *
   * @param rset
   * @param results
   * @param resultKey
   * @return
   * @throws SQLException
   * @throws JSONException
   */
  public JSONObject processResults(ResultSet rset, JSONObject results, Map<String, String> columnName2FieldNameMap, String resultKey) throws SQLException, JSONException {
    ResultSetMetaData md = rset.getMetaData();
    while (rset.next()) {
      JSONObject record = new JSONObject();
      results.append(resultKey, record);
      for (int j = 1; j <= md.getColumnCount(); j++) {
        int columnType = md.getColumnType(j);
        String columnName = md.getColumnLabel(j);
        String fieldName = columnName;
        if (columnName2FieldNameMap != null && !columnName2FieldNameMap.isEmpty() && columnName2FieldNameMap.containsKey(columnName)) {
          fieldName = columnName2FieldNameMap.get(columnName);
        }
        String value = rset.getString(columnName);
        if (value != null) {
          switch (columnType) {
            //Integers
            case Types.BIGINT:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
              record.put(fieldName, rset.getInt(columnName));
              break;

            //Doubles
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.NUMERIC:
            case Types.REAL:
              record.put(fieldName, rset.getDouble(columnName));
              break;

            //Dates
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
              //record.put(fieldName, dateFormat.format(rset.getDate(columnName)));
              record.put(fieldName, dateFormat.format(rset.getDate(columnName)));
              break;

            //Boolean
            case Types.BIT:
            case Types.BOOLEAN:
              record.put(fieldName, rset.getBoolean(columnName));
              break;

            //Everything else- String
            default:
              record.put(fieldName, value);
              break;
          }
        }
      }
    }
    return results;
  }  
}
