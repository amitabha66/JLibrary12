package amgen.ri.stat;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.RuleBasedCollator;
import java.util.Vector;

public class Sort {
    //Constants to define the order
    public static final short ASCENDING = 0;
    public static final short DESCENDING = 1;

    //Constants to defne the comparison
    public static final short GREATER = 0;
    public static final short LESSTHAN = 1;
    public static final short GREATEROREQUAL = 2;
    public static final short LESSOREQUAL = 3;

    //Constants to define the comparison type
    private static final short INT = 0;
    private static final short LONG = 1;
    private static final short DOUBLE = 2;
    private static final short STRING = 3;

    /**
     * Performs a quicksort or alphabetic sort of a Vector. The vector can be
     * Strings, Integers, or Longs
     * @param v the vector of data to sort
     * @param direction either ASCENDING or DESCENDING
     * @return array of ordered indexes
     */
    public static int[] quicksort(Vector v, short direction) {
        if (v == null || v.size() <= 0) {
            return new int[0];
        }
        if (v.elementAt(0) instanceof String) {
            String[] s = new String[v.size()];
            v.copyInto(s);
            return quicksort(s, direction);
        }
        if (v.elementAt(0) instanceof Integer) {
            int[] num = new int[v.size()];
            for (int i = 0; i < num.length; i++) {
                num[i] = ( (Integer) v.elementAt(i)).intValue();
            }
            return quicksort(num, direction);
        }
        if (v.elementAt(0) instanceof Long) {
            long[] num = new long[v.size()];
            for (int i = 0; i < num.length; i++) {
                num[i] = ( (Long) v.elementAt(i)).longValue();
            }
            return quicksort(num, direction);
        }
        return new int[0];
    }

    /**
     * Performs an alphabetic sort on the data
     * @param s array of strings to sort
     * @param direction either ASCENDING or DESCENDING
     * @return array of ordered indexes
     */
    public static int[] quicksort(String[] s, short direction) {
        if (direction == ASCENDING) {
            return quicksort(s);
        }
        int[] indx = quicksort(s);
        int[] temp = new int[indx.length];
        for (int i = 0; i < indx.length; i++) {
            temp[i] = indx[indx.length - 1 - i];
        }
        return temp;
    }

    /**
     * Performs a quicksort on the data
     * @param arr array of longs to sort
     * @param direction either ASCENDING or DESCENDING
     * @return array of ordered indexes
     */
    public static int[] quicksort(long arr[], short direction) {
        if (direction == ASCENDING) {
            return quicksort(arr);
        }
        int[] indx = quicksort(arr);
        int[] temp = new int[indx.length];
        for (int i = 0; i < indx.length; i++) {
            temp[i] = indx[indx.length - 1 - i];
        }
        return temp;
    }

    /**
     * Performs a quicksort on the data. Returns the data in ASCENDING order.
     * @param arr array of longs to sort
     * @return array of ordered indexes
     */
    public static int[] quicksort(long arr[]) {
        int n = arr.length;
        int[] indx = new int[arr.length];
        int i, indxt, ir = n - 1, itemp, j, k, l = 0;
        int jstack = 0, istack[];
        long a;

        istack = new int[arr.length + 1];
        for (j = 0; j < n; j++) {
            indx[j] = j;
        }
        for (; ; ) {
            if (ir - l < 7) {
                for (j = l + 1; j <= ir; j++) {
                    indxt = indx[j];
                    a = arr[indxt];
                    for (i = j - 1; i >= 0; i--) {
                        if (arr[indx[i]] <= a) {
                            break;
                        }
                        indx[i + 1] = indx[i];
                    }
                    indx[i + 1] = indxt;
                }
                if (jstack == 0) {
                    break;
                }
                ir = istack[jstack--];
                l = istack[jstack--];
            } else {
                k = (l + ir) >> 1;
                SWAP(indx, k, l + 1);
                if (arr[indx[l + 1]] > arr[indx[ir]]) {
                    SWAP(indx, l + 1, ir);
                }
                if (arr[indx[l]] > arr[indx[ir]]) {
                    SWAP(indx, l, ir);
                }
                if (arr[indx[l + 1]] > arr[indx[l]]) {
                    SWAP(indx, l + 1, l);
                }
                i = l + 1;
                j = ir;
                indxt = indx[l];
                a = arr[indxt];
                for (; ; ) {
                    do {
                        i++;
                    } while (arr[indx[i]] < a);
                    do {
                        j--;
                    } while (arr[indx[j]] > a);
                    if (j < i) {
                        break;
                    }
                    SWAP(indx, i, j);
                }
                indx[l] = indx[j];
                indx[j] = indxt;
                jstack += 2;
                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir;
                    istack[jstack - 1] = i;
                    ir = j - 1;
                } else {
                    istack[jstack] = j - 1;
                    istack[jstack - 1] = l;
                    l = i;
                }
            }
        }
        return indx;
    }

    /**
     * Performs a quicksort on the data.
     * @param arr array of integers to sort
     * @param direction either ASCENDING or DESCENDING
     * @return array of ordered indexes
     */
    public static int[] quicksort(int arr[], short direction) {
        if (direction == ASCENDING) {
            return quicksort(arr);
        }
        int[] indx = quicksort(arr);
        int[] temp = new int[indx.length];
        for (int i = 0; i < indx.length; i++) {
            temp[i] = indx[indx.length - 1 - i];
        }
        return temp;
    }

    /**
     * Performs a quicksort on the data.
     * @param arr array of integers to sort
     * @param direction either ASCENDING or DESCENDING
     * @return array of ordered indexes
     */
    public static int[] quicksort(Integer arr[], short direction) {
        int[] array = new int[arr.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = arr[i].intValue();
        }
        if (direction == ASCENDING) {
            return quicksort(array);
        }
        int[] indx = quicksort(array);
        int[] temp = new int[indx.length];
        for (int i = 0; i < indx.length; i++) {
            temp[i] = indx[indx.length - 1 - i];
        }
        return temp;
    }

    /**
     * Performs a quicksort on the data.
     * @param arr array of integers to sort
     * @param direction either ASCENDING or DESCENDING
     * @return array of ordered indexes
     */
    public static int[] quicksort(Long arr[], short direction) {
        long[] array = new long[arr.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = arr[i].intValue();
        }
        if (direction == ASCENDING) {
            return quicksort(array);
        }
        int[] indx = quicksort(array);
        int[] temp = new int[indx.length];
        for (int i = 0; i < indx.length; i++) {
            temp[i] = indx[indx.length - 1 - i];
        }
        return temp;
    }

    /**
     * Performs a quicksort on the data. Sorts on doubles use only 6 decimals of precision.
     * @param arr array of double to sort
     * @param direction either ASCENDING or DESCENDING
     * @return array of ordered indexes
     */
    public static int[] quicksort(Double arr[], short direction) {
        long[] array = new long[arr.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = (long) (1000000 * arr[i].doubleValue());
        }
        if (direction == ASCENDING) {
            return quicksort(array);
        }
        int[] indx = quicksort(array);
        int[] temp = new int[indx.length];
        for (int i = 0; i < indx.length; i++) {
            temp[i] = indx[indx.length - 1 - i];
        }
        return temp;
    }

    /**
     * Performs a quicksort on the data. Sorts on doubles use only 6 decimals of precision.
     * @param arr array of double to sort
     * @param direction either ASCENDING or DESCENDING
     * @return array of ordered indexes
     */
    public static int[] quicksort(double arr[], short direction) {
        long[] array = new long[arr.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = (long) (1000000 * arr[i]);
        }
        if (direction == ASCENDING) {
            return quicksort(array);
        }
        int[] indx = quicksort(array);
        int[] temp = new int[indx.length];
        for (int i = 0; i < indx.length; i++) {
            temp[i] = indx[indx.length - 1 - i];
        }
        return temp;
    }

    /**
     * Performs a quicksort on the data. Returns the data in ASCENDING order.
     * @param arr array of integers to sort
     * @return array of ordered indexes
     */
    public static int[] quicksort(int arr[]) {
        int n = arr.length;
        int[] indx = new int[arr.length];
        int i, indxt, ir = n - 1, itemp, j, k, l = 0;
        int jstack = 0, istack[];
        int a;

        istack = new int[arr.length + 1];
        for (j = 0; j < n; j++) {
            indx[j] = j;
        }
        for (; ; ) {
            if (ir - l < 7) {
                for (j = l + 1; j <= ir; j++) {
                    indxt = indx[j];
                    a = arr[indxt];
                    for (i = j - 1; i >= 0; i--) {
                        if (arr[indx[i]] <= a) {
                            break;
                        }
                        indx[i + 1] = indx[i];
                    }
                    indx[i + 1] = indxt;
                }
                if (jstack == 0) {
                    break;
                }
                ir = istack[jstack--];
                l = istack[jstack--];
            } else {
                k = (l + ir) >> 1;
                SWAP(indx, k, l + 1);
                if (arr[indx[l + 1]] > arr[indx[ir]]) {
                    SWAP(indx, l + 1, ir);
                }
                if (arr[indx[l]] > arr[indx[ir]]) {
                    SWAP(indx, l, ir);
                }
                if (arr[indx[l + 1]] > arr[indx[l]]) {
                    SWAP(indx, l + 1, l);
                }
                i = l + 1;
                j = ir;
                indxt = indx[l];
                a = arr[indxt];
                for (; ; ) {
                    do {
                        i++;
                    } while (arr[indx[i]] < a);
                    do {
                        j--;
                    } while (arr[indx[j]] > a);
                    if (j < i) {
                        break;
                    }
                    SWAP(indx, i, j);
                }
                indx[l] = indx[j];
                indx[j] = indxt;
                jstack += 2;
                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir;
                    istack[jstack - 1] = i;
                    ir = j - 1;
                } else {
                    istack[jstack] = j - 1;
                    istack[jstack - 1] = l;
                    l = i;
                }
            }
        }
        return indx;
    }

    /**
     * Sort an array of Strings into ASCENDING alphabetical order
     * @param arr the array of Strings to index
     * @return an integer array of the sorted indexes
     */
    public static int[] quicksort(String arr[]) {

        int n = arr.length;
        int[] indx = new int[arr.length];
        int i, indxt, ir = n - 1, itemp, j, k, l = 0;
        int jstack = 0, istack[];
        String a;

        istack = new int[arr.length + 1];

        String rules = "< 0< 1< 2< 3< 4< 5< 6< 7< 8< 9" +
            "< a,A< b,B< c,C< d,D< e,E< f,F< g,G< h,H< i,I< j,J" +
            "< k,K< l,L< m,M< n,N< o,O< p,P< q,Q< r,R< s,S< t,T" +
            "< u,U< v,V< w,W< x,X< y,Y< z,Z";

        RuleBasedCollator theCollation = null;
        try {
            theCollation = new RuleBasedCollator(rules);
        } catch (Exception e) {}

        for (j = 0; j < n; j++) {
            indx[j] = j;
        }
        for (; ; ) {
            if (ir - l < 7) {
                for (j = l + 1; j <= ir; j++) {
                    indxt = indx[j];
                    a = arr[indxt];
                    for (i = j - 1; i >= 0; i--) {
                        if (compare(theCollation, arr[indx[i]], a, LESSOREQUAL)) {
                            break;
                        }
                        indx[i + 1] = indx[i];
                    }
                    indx[i + 1] = indxt;
                }
                if (jstack == 0) {
                    break;
                }
                ir = istack[jstack--];
                l = istack[jstack--];
            } else {
                k = (l + ir) >> 1;
                SWAP(indx, k, l + 1);
                if (compare(theCollation, arr[indx[l + 1]], arr[indx[ir]], GREATER)) {
                    SWAP(indx, l + 1, ir);
                }
                if (compare(theCollation, arr[indx[l]], arr[indx[ir]], GREATER)) {
                    SWAP(indx, l, ir);
                }
                if (compare(theCollation, arr[indx[l + 1]], arr[indx[l]], GREATER)) {
                    SWAP(indx, l + 1, l);
                }
                i = l + 1;
                j = ir;
                indxt = indx[l];
                a = arr[indxt];
                for (; ; ) {
                    do {
                        i++;
                    } while (compare(theCollation, arr[indx[i]], a, LESSTHAN));
                    do {
                        j--;
                    } while (compare(theCollation, arr[indx[j]], a, GREATER));
                    if (j < i) {
                        break;
                    }
                    SWAP(indx, i, j);
                }
                indx[l] = indx[j];
                indx[j] = indxt;
                jstack += 2;
                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir;
                    istack[jstack - 1] = i;
                    ir = j - 1;
                } else {
                    istack[jstack] = j - 1;
                    istack[jstack - 1] = l;
                    l = i;
                }
            }
        }
        return indx;
    }

    /**
     * Sort an array of Strings into ASCENDING alphabetical order
     * @param arr the array of Strings to index
     * @return an integer array of the sorted indexes
     */
    public static int[] quicksort1(String arr[]) {

        int n = arr.length;
        int[] indx = new int[arr.length];
        int i, indxt, ir = n - 1, itemp, j, k, l = 0;
        int jstack = 0, istack[];
        String a;

        istack = new int[arr.length + 1];

        String rules = "< 0< 1< 2< 3< 4< 5< 6< 7< 8< 9" +
            "< a,A< b,B< c,C< d,D< e,E< f,F< g,G< h,H< i,I< j,J" +
            "< k,K< l,L< m,M< n,N< o,O< p,P< q,Q< r,R< s,S< t,T" +
            "< u,U< v,V< w,W< x,X< y,Y< z,Z";

        RuleBasedCollator theCollation = null;
        try {
            theCollation = new RuleBasedCollator(rules);
        } catch (Exception e) {}

        for (j = 0; j < n; j++) {
            indx[j] = j;
        } while (true) {
            if (ir - l < 7) {
                for (j = l + 1; j <= ir; j++) {
                    indxt = indx[j];
                    a = arr[indxt];
                    for (i = j - 1; i >= 0; i--) {
                        if (compare(theCollation, arr[indx[i]], a, LESSOREQUAL)) {
                            break;
                        }
                        indx[i + 1] = indx[i];
                    }
                    indx[i + 1] = indxt;
                }
                if (jstack == 0) {
                    break;
                }
                ir = istack[jstack--];
                l = istack[jstack--];
            } else {
                k = (l + ir) >> 1;
                SWAP(indx, k, l + 1);
                if (compare(theCollation, arr[indx[l + 1]], arr[indx[ir]], GREATER)) {
                    SWAP(indx, l + 1, ir);
                }
                if (compare(theCollation, arr[indx[l]], arr[indx[ir]], GREATER)) {
                    SWAP(indx, l, ir);
                }
                if (compare(theCollation, arr[indx[l + 1]], arr[indx[l]], GREATER)) {
                    SWAP(indx, l + 1, l);
                }
                i = l + 1;
                j = ir;
                indxt = indx[l];
                a = arr[indxt];
                while (true) {
                    do {
                        i++;
                    } while (compare(theCollation, arr[indx[i]], a, LESSTHAN));
                    do {
                        j--;
                    } while (compare(theCollation, arr[indx[j]], a, GREATER));
                    if (j < i) {
                        break;
                    }
                    SWAP(indx, i, j);
                }
                indx[l] = indx[j];
                indx[j] = indxt;
                jstack += 2;
                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir;
                    istack[jstack - 1] = i;
                    ir = j - 1;
                } else {
                    istack[jstack] = j - 1;
                    istack[jstack - 1] = l;
                    l = i;
                }
            }
        }
        return indx;
    }

    /**
     * Perfoms a quicksort on the objects by a class variable (instance or static) using the reflection API.
     * To use, each object in the array must have a declared public field with the name sortByField.
     * The first object in the array is used to determine the field
     * type, either integer, long, double, or String for all others (accessed by a toString() call on the class variable object).
     * As a convenience, if the first field requests throws an expection (which will be sure to either it not existing or not having
     * public access), the field name will be changed to a getField method name and the
     * quicksortByMethod(Object[] array, String methodName, short direction) will be invoked. The getField method is of the form
     * get[Uppercase first character e.g. F]ield. If an exception is thrown by quicksortByMethod, a NoSuchFieldException is thrown.
     * @param array the objects to sort
     * @param sortByField the declared name of the class variable by which the objects will be sorted
     * @param direction either ASCENDING or DESCENDING
     * @return an integer array of sorted indexes
     * @exception NoSuchFieldException if the sortByField does not exist in any of the array objects
     * @exception IllegalAccessException if there are insufficient privledges to access the sortByField
     * @exception NullPointerException if any object (array, sortByField) is null
     */
    public static int[] quicksort(Object[] array, String sortByField,
                                  short direction) throws NoSuchFieldException,
        IllegalAccessException {
        Field field;
        try {
            field = array[0].getClass().getField(sortByField);
        } catch (Exception e) {
            try {
                String getMethodName = "get" +
                    Character.toUpperCase(sortByField.charAt(0)) +
                    sortByField.substring(1);
                return quicksortByMethod(array, getMethodName, direction);
            } catch (Exception e1) {
                e1.printStackTrace();
                throw new NoSuchFieldException("Field, " + sortByField +
                                               ", does not exist and cannot access get method.");
            }
        }
        String fieldTypeString = field.getType().getName();
        Object sortFields = null;
        short fieldType;

        if (fieldTypeString.equals("int") ||
            fieldTypeString.equals("java.lang.Integer") ||
            fieldTypeString.equals("short") ||
            fieldTypeString.equals("java.lang.Short")) {
            fieldType = INT;
            sortFields = new Integer[array.length];
        } else if (fieldTypeString.equals("long") ||
                   fieldTypeString.equals("java.lang.Long")) {
            fieldType = LONG;
            sortFields = new Long[array.length];
        } else if (fieldTypeString.equals("double") ||
                   fieldTypeString.equals("float") ||
                   fieldTypeString.equals("java.lang.Float") ||
                   fieldTypeString.equals("java.lang.Double")) {
            fieldType = DOUBLE;
            sortFields = new Double[array.length];
        } else if (fieldTypeString.equals("java.lang.String")) {
            fieldType = STRING;
            sortFields = new String[array.length];
        } else {
            fieldType = STRING;
            sortFields = new String[array.length];
        }
        Object[] sortFieldArray = (Object[]) sortFields;
        for (int i = 0; i < array.length; i++) {
            switch (fieldType) {
                case (INT):
                    sortFieldArray[i] = new Integer(field.getInt(array[i]));
                    break;
                case (LONG):
                    sortFieldArray[i] = new Long(field.getLong(array[i]));
                    break;
                case (DOUBLE):
                    sortFieldArray[i] = new Double(field.getDouble(array[i]));
                    break;
                case (STRING):
                    sortFieldArray[i] = new String(field.get(array[i]).toString());
                    break;
            }
        }
        switch (fieldType) {
            case (INT):
                return quicksort( (Integer[]) sortFields, direction);
            case (LONG):
                return quicksort( (Long[]) sortFields, direction);
            case (DOUBLE):
                return quicksort( (Double[]) sortFields, direction);
            case (STRING):
                return quicksort( (String[]) sortFields, direction);
        }
        //Can't actually get here since fieldType must be INT, LONG, DOUBLE, or STRING!! (Just here to stop compiler error.)
        return null;

    }

    /**
     * Perfoms a quicksort on the objects by a class variable (instance or static) using the reflection API. To use, each object in the array must
     * have a declared public field with the name sortByField. The first object in the array is used to determine the field
     * type, either integer, long, double, or String for all others (accessed by a toString() call on the class variable object).
     * @param array the objects to sort
     * @param sortByField the declared name of the class variable by which the objects will be sorted
     * @param direction either ASCENDING or DESCENDING
     * @return an integer array of sorted indexes
     * @exception NoSuchFieldException if the sortByField does not exist in any of the array objects
     * @exception IllegalAccessException if there are insufficient privledges to access the sortByField
     * @exception NullPointerException if any object (array, sortByField) is null
     */
    public static int[] quicksortByMethod(Object[] array, String methodName,
                                          short direction) throws
        NoSuchMethodException, InvocationTargetException,
        IllegalAccessException {
        if (array == null || array.length == 0) {
            return new int[0];
        }
        Method method = array[0].getClass().getMethod(methodName, (Class[])null);
        String methodReturnTypeString = method.getReturnType().getName();
        Object sortFields = null;
        short methodReturnType;

        if (methodReturnTypeString.equals("int") ||
            methodReturnTypeString.equals("java.lang.Integer") ||
            methodReturnTypeString.equals("short") ||
            methodReturnTypeString.equals("java.lang.Short")) {
            methodReturnType = INT;
            sortFields = new Integer[array.length];
        } else if (methodReturnTypeString.equals("long") ||
                   methodReturnTypeString.equals("java.lang.Long")) {
            methodReturnType = LONG;
            sortFields = new Long[array.length];
        } else if (methodReturnTypeString.equals("double") ||
                   methodReturnTypeString.equals("float") ||
                   methodReturnTypeString.equals("java.lang.Float") ||
                   methodReturnTypeString.equals("java.lang.Double")) {
            methodReturnType = DOUBLE;
            sortFields = new Double[array.length];
        } else if (methodReturnTypeString.equals("java.lang.String")) {
            methodReturnType = STRING;
            sortFields = new String[array.length];
        } else {
            methodReturnType = STRING;
            sortFields = new String[array.length];
        }
        Object[] sortFieldArray = (Object[]) sortFields;
        for (int i = 0; i < array.length; i++) {
            switch (methodReturnType) {
                case (INT):
                    sortFieldArray[i] = (Integer) method.invoke(array[i], (Object[])null);
                    break;
                case (LONG):
                    sortFieldArray[i] = (Long) method.invoke(array[i], (Object[])null);
                    break;
                case (DOUBLE):
                    sortFieldArray[i] = (Double) method.invoke(array[i], (Object[])null);
                    break;
                case (STRING):
                    sortFieldArray[i] = method.invoke(array[i], (Object[])null).toString();
                    break;
            }
        }
        switch (methodReturnType) {
            case (INT):
                return quicksort( (Integer[]) sortFields, direction);
            case (LONG):
                return quicksort( (Long[]) sortFields, direction);
            case (DOUBLE):
                return quicksort( (Double[]) sortFields, direction);
            case (STRING):
                return quicksort( (String[]) sortFields, direction);
        }
        //Can't actually get here since fieldType must be INT, LONG, DOUBLE, or STRING!! (Just here to stop compiler error.)
        return null;

    }

    /**
     * Rule comparitor to compare 2 strings and return if it matches the comparison type
     * @param collator the rule collator for the comparison
     * @param arg1 string 1
     * @param arg2 string 2
     * @param type the type of comparison GREATER/LESSTHAN/GREATEROREQUAL/LESSOREQUAL
     */
    private static boolean compare(RuleBasedCollator collator, String arg1,
                                   String arg2, int type) {
        int compareResult = collator.compare(arg1, arg2);
        switch (type) {
            case (GREATER):
                if (compareResult > 0) {
                    return true;
                }
                break;
            case (LESSTHAN):
                if (compareResult < 0) {
                    return true;
                }
                break;
            case (GREATEROREQUAL):
                if (compareResult >= 0) {
                    return true;
                }
                break;

            case (LESSOREQUAL):
                if (compareResult <= 0) {
                    return true;
                }
                break;
        }
        return false;
    }

    /**
     * Swaps the positions of data in an array
     * @param array the array containg the data to swap
     * @param index1 position 1 to swap
     * @param index2 position 2 to swap
     */
    private static void SWAP(int[] array, int index1, int index2) {
        int temp = array[index1];
        array[index1] = array[index2];
        array[index2] = temp;
    }

}
