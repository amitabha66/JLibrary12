package amgen.ri.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class ExtObject {
    /**
     * Returns a copy of the object, or null if the object cannot
     * be serialized.
     */
    public static Object copy(Object orig) {
        try {
            return deserializeObject(serializeObject(orig));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Serializes an object and returns it as a byte array.
     *
     * @param obj Object
     * @throws IOException
     * @return byte[]
     */
    public static byte[] serializeObject(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(obj);
        out.flush();
        out.close();
        return bos.toByteArray();
    }

    /**
     * Deserializes a serialized object provided as a byte array to an Object
     *
     * @param serializedObjBytes byte[]
     * @throws IOException
     * @throws ClassNotFoundException
     * @return Object
     */
    public static Object deserializeObject(byte[] serializedObjBytes) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(serializedObjBytes));
        return in.readObject();
    }

    /**
     * Returns the class source (jar or folder) for the class
     *
     * @param cls Class
     * @return String
     */
    public static String showClassSource(Class cls) {
        String className = cls.getCanonicalName();
        if (!className.startsWith("/")) {
            className = "/" + className;
        }
        className = className.replace('.', '/');
        className = className + ".class";
        URL url = cls.getResource(className);
        return (url != null ? url.getFile() : null);
    }

    /**
     * Attempts to convert the Object to a double.
     * If the value is an instance of Number, it returns as a double. Otherwise,
     * is uses ExtString.toDouble after calling value's toString method.
     * Returns NaN if value is null or not convertable to a number.
     *
     * @param value Object
     * @return double
     */
    public static double toDouble(Object value) {
        if (value == null) {
            return Double.NaN;
        }
        if (value instanceof Number) {
            return ( (Number) value).doubleValue();
        }
        return ExtString.toDouble(value.toString());
    }

    /**
     * Attempts to convert the Object to a Date. If the value is an instance of
     * Date, it returns as a Date after casting. Otherwise, is uses
     * ExtDate.toDate after calling value's toString method. Returns null if
     * value is null or not convertable to a Date.
     *
     * @param value Object
     * @return Date
     */
    public static Date toDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return (Date) value;
        }
        return ExtDate.toDate(value.toString());
    }

}
