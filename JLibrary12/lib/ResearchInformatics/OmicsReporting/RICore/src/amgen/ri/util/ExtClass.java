package amgen.ri.util;

import java.io.ObjectStreamClass;
import java.net.URL;
import java.util.List;

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
public final class ExtClass {
    /**
     * Returns a class UID
     *
     * @param cls Class
     * @return long
     */
    public static long getClassUID(Class cls) {
        return ObjectStreamClass.lookup(cls).getSerialVersionUID();
    }

    /**
     * Returns the class source URL
     * @param cls Class
     * @return URL
     */
    public static URL getClassLocation(Class cls) {
        return cls.getProtectionDomain().getCodeSource().getLocation();
    }

    /**
     * Returns the class source URL
     * @param cls Class
     * @return URL
     */
    public static String getClassLocationAndName(Class cls) {
        return cls.getCanonicalName() + "=> " + cls.getProtectionDomain().getCodeSource().getLocation();
    }

    /**
     * Tries to load a Class given its name and a List of packages
     *
     * @param className String
     * @param packages List
     * @return Class
     */
    public static Class loadClass(String className, List<String> packages) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
        }
        if (packages != null) {
            for (String packageName : packages) {
                try {
                    return Class.forName(packageName + "." + className);
                } catch (ClassNotFoundException ex) {
                }
            }
        }
        return null;

    }
}
