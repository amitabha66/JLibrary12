/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package amgen.ri.json.record;

import amgen.ri.json.JSONArray;
import amgen.ri.json.JSONException;
import amgen.ri.json.JSONObject;
import amgen.ri.json.ResultSet2JSON;
import amgen.ri.util.HashCodeGenerator;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jemcdowe
 */
public class AbstractRecord extends JSONObject implements Serializable {

    public SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
    private String recordID;
    private int fHashCode;

    protected AbstractRecord(String recordID) {
        this.recordID = recordID;
    }

    /**
     * Copies the contents of one AbstractRecord to another
     *
     * @param record
     * @throws JSONException
     */
    protected AbstractRecord(AbstractRecord record) throws JSONException {
        super(record, JSONObject.getNames(record));
    }

    /**
     * Copies the contents of one AbstractRecord to another
     *
     * @param record
     * @throws JSONException
     */
    protected AbstractRecord(JSONObject source, String idField) throws JSONException {
        super(source, JSONObject.getNames(source));
        this.recordID = source.getString(idField);
    }

    /**
     * Copies the contents of one AbstractRecord to another optionally skipping 
     * some keys in the source 
     *
     * @param source
     * @param idField
     * @param skipKeys
     * @throws JSONException
     */
    protected AbstractRecord(JSONObject source, String idField, String... skipKeys) throws JSONException {
        super();
        List<String> keys = new ArrayList<String>(Arrays.asList(JSONObject.getNames(source)));
        if (skipKeys != null) {
            keys.removeAll(Arrays.asList(skipKeys));
        }
        for(String key : keys) {
            putOnce(key, source.opt(key));
        }
        this.recordID = source.getString(idField);
    }

    /**
     * Tests of the value in the given key equals the value. If value is an
     * array, it test all values and returns true is any are equal
     *
     * @param key
     * @param value
     * @return
     */
    public boolean equals(String key, Object value) {
        if (has(key)) {
            if (value instanceof Object[]) {
                Object[] values = (Object[]) value;
                for (Object v : values) {
                    if (equals(key, v)) {
                        return true;
                    }
                }
            } else {
                return get(key).equals(value);
            }
        }
        return false;
    }

    /**
     * The equivalence here is done by changing values to String and doing an
     * String.equalsIgnoreCase test.
     *
     * Tests of the value in the given key equals the value. If value is an
     * array, it test all values and returns true is any are equal
     *
     * @param key
     * @param value
     * @return
     */
    public boolean equalsIgnoreCase(String key, Object value) {
        if (has(key)) {
            if (value instanceof Object[]) {
                Object[] values = (Object[]) value;
                for (Object v : values) {
                    if (equalsIgnoreCase(key, v)) {
                        return true;
                    }
                }
            } else {
                return getString(key).equalsIgnoreCase(value + "");
            }
        }
        return false;
    }

    /**
     * Adds an Object to the internal Map
     *
     * @param key
     * @param obj
     */
    public void add(String key, Object obj) {
        try {
            if (obj instanceof Date) {
                super.put(key, dateFormat.format((Date) obj));
            } else {
                super.put(key, obj);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Adds an Object to a JSONArray in the internal Map
     *
     * @param key
     * @param obj
     */
    public void addToArray(String key, Object obj) {
        try {
            if (obj instanceof Collection) {
                for (Object o : (Collection) obj) {
                    super.append(key, o);
                }
            } else {
                super.append(key, obj);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Adds an AbstractRecord to a JSONObject by a member value.
     * 
     * The result is-
     * {
     *  key: {
     *        <recordKey value>: record,
     *       ...
     *      }
     * }
     * 
     */
    public void addToMap(String key, String recordKey, AbstractRecord record) {
        try {
            if (!has(key)) {
                putOnce(key, new JSONObject());
            }
            String recordVal= record.getString(recordKey);
            getJSONObject(key).put(recordVal, record);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Returns a value as a Number if possible. null if the key does not exists
     * or NaN if it is not a number
     *
     * @param key
     * @return
     */
    public Number getNumber(String key) {
        try {
            if (!has(key)) {
                return null;
            }
            return super.getDouble(key);
        } catch (JSONException ex) {
            return Double.NaN;
        }
    }

    /**
     * Returns a value as a String. null if the key does not exist
     *
     * @param key
     * @return
     */
    public String getString(String key) {
        try {
            return super.getString(key);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Returns a value as a JSONArray. null if the key does not exist
     *
     * @param key
     * @return
     */
    public JSONArray getArray(String key) {
        try {
            return super.getJSONArray(key);
        } catch (JSONException ex) {
            return null;
        }
    }

    /**
     * Returns a JSONArray value as a List.
     *
     * @param key
     * @return
     */
    public List getList(String key) {
        try {
            return super.getJSONArray(key).asList();
        } catch (JSONException ex) {
            return new ArrayList();
        }
    }


    /**
     * Returns a JSONObject value as a Map.
     *
     * @param key
     * @return
     */
    public Map getMap(String key) {
        try {
            return super.getJSONObject(key).asMap();
        } catch (JSONException ex) {
            return new HashMap();
        }
    }

    /**
     * Returns a value as an AbstractRecord. null if the key does not exist
     *
     * @param key
     * @return
     */
    public AbstractRecord getRecord(String key) {
        try {
            return (AbstractRecord) super.get(key);
        } catch (JSONException ex) {
            return null;
        }
    }

    /**
     * Returns is the value is a Date
     *
     * @param key
     * @return
     */
    public boolean isDate(String key) {
        return false;
    }

    /**
     * Returns a value as a Date if the format is correct. null if the key does
     * not exist or can not be represented as a DATE according to the
     * DATE_FORMATTER
     *
     * @param key
     * @return
     */
    public Date getDate(String key) {
        try {
            if (has(key)) {
                return dateFormat.parse(getString(key));
            }
        } catch (Exception ex) {
            System.err.println("Unable to parse date: " + getString(key));
        }
        return null;
    }

    @Override
    public boolean getBoolean(String key) {
        try {
            return super.getBoolean(key);
        } catch (JSONException ex) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        if (fHashCode == 0) {
            int result = HashCodeGenerator.SEED;
            result = HashCodeGenerator.hash(result, getRecordID());
            fHashCode = result;
        }
        return fHashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractRecord && obj.getClass().equals(this.getClass())) {
            AbstractRecord g1 = this;
            AbstractRecord g2 = (AbstractRecord) obj;
            return (g1.getRecordID().equals(g2.getRecordID()));
        }
        return false;
    }

    /**
     * @return the recordID
     */
    public String getRecordID() {
        return recordID;
    }

    public Object get(String key) {
        try {
            return super.get(key);
        } catch (JSONException ex) {
            return null;
        }
    }

    public static List<AbstractRecord> fromOracle(Class abstractClass, ResultSet rset) throws SQLException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        List<AbstractRecord> results = new ArrayList<AbstractRecord>();
        ResultSet2JSON resultSet2JSON = new ResultSet2JSON();
        try {
            JSONObject jResults = resultSet2JSON.processResults(rset, "results");
            if (jResults.has("results")) {
                List<JSONObject> resultList = jResults.getJSONArray("results").asList();
                for (JSONObject result : resultList) {
                    results.add((AbstractRecord) abstractClass.getConstructor(new Class[]{JSONObject.class}).newInstance(new Object[]{result}));
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(AbstractRecord.class.getName()).log(Level.SEVERE, null, ex);
        }

        return results;

    }
}
