package amgen.ri.util;

/*
 * Class: ExtArray
 *
 * Created: Jeffrey McDowell
 *
 * Modified: $Author: cvs $
 *
 * Description:
 * Utilities to use with arrays (sorting, indexing, max, min, copying ect.)
 *
 /**
  * A bunch of utilities of manipulating array. Sorting, alphabetizing, copying, max, min
  *
  * @version $Revision: 1.1 $
  * @author Jeffrey McDowell
  */
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

public final class ExtArray {
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
     * Returns whether the Collection is not null and has size()>0
     *
     * @param c Collection
     * @return boolean
     */
    public static boolean hasLength(Collection c) {
        return (c != null && c.size() > 0);
    }

    /**
     * Returns whether the Array is not null and has size()>0
     *
     * @param c Object[]
     * @return boolean
     */
    public static boolean hasLength(Object[] arr) {
        return (arr != null && arr.length > 0);
    }

    /**
     * Return the maximum in an array in integers
     */
    public static int arrayMax(int arr[]) {
        int max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            max = Math.max(arr[i], max);
        }
        return max;
    }

    /**
     * Return the minimum in an array in integers
     */
    public static int arrayMin(int arr[]) {
        int min = arr[0];
        for (int i = 1; i < arr.length; i++) {
            min = Math.min(arr[i], min);
        }
        return min;
    }

    /**
     * Returns the minimum value in the List using the provided Comparator
     *
     * @param arr List
     * @param comparator Comparator
     * @return Object
     */
    public static Object min(List list, Comparator comparator) {
        int[] indxs = quicksort(list, comparator);
        if (indxs == null || indxs.length == 0) {
            return null;
        }
        return list.get(indxs[0]);
    }

    /**
     * Returns the maximum value in the List using the provided Comparator
     *
     * @param arr List
     * @param comparator Comparator
     * @return Object
     */
    public static Object max(List list, Comparator comparator) {
        int[] indxs = quicksort(list, comparator);
        if (indxs == null || indxs.length == 0) {
            return null;
        }
        return list.get(indxs[indxs.length - 1]);
    }

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
                        if (compare(theCollation, arr[indx[i]], a, ExtArray.LESSOREQUAL)) {
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
                if (compare(theCollation, arr[indx[l + 1]], arr[indx[ir]], ExtArray.GREATER)) {
                    SWAP(indx, l + 1, ir);
                }
                if (compare(theCollation, arr[indx[l]], arr[indx[ir]], ExtArray.GREATER)) {
                    SWAP(indx, l, ir);
                }
                if (compare(theCollation, arr[indx[l + 1]], arr[indx[l]], ExtArray.GREATER)) {
                    SWAP(indx, l + 1, l);
                }
                i = l + 1;
                j = ir;
                indxt = indx[l];
                a = arr[indxt];
                for (; ; ) {
                    do {
                        i++;
                    } while (compare(theCollation, arr[indx[i]], a, ExtArray.LESSTHAN));
                    do {
                        j--;
                    } while (compare(theCollation, arr[indx[j]], a, ExtArray.GREATER));
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
     * Sorts a List of Objects in the List according to a Comparator into ASCENDING order and returns an
     * updated, sorted List object
     * @param arr the List of objects
     * @param comparator Comparator to use
     * @return sorted List
     */
    public static List sort(List arr, Comparator comparator) {
        int[] sortedIndexes = quicksort(arr, comparator);
        List sortedList = new ArrayList(arr.size());
        for (int i = 0; i < sortedIndexes.length; i++) {
            sortedList.add(arr.get(sortedIndexes[i]));
        }
        return sortedList;
    }

    /**
     * Sorts a List of Objects in the List according to a Comparator into ASCENDING order and returns an
     * updated, sorted List object
     * @param arr the List of objects
     * @param comparator Comparator to use
     * @return sorted List
     */
    public static List sort(List arr) {
        int[] sortedIndexes = quicksort(arr, null);
        List sortedList = new ArrayList(arr.size());
        for (int i = 0; i < sortedIndexes.length; i++) {
            sortedList.add(arr.get(sortedIndexes[i]));
        }
        return sortedList;
    }

    /**
     * Sorts a List of Objects according to a Comparator into ASCENDING order
     * @param arr the List of objects
     * @param comparator Comparator to use
     * @return an integer array of the sorted indexes
     */
    public static int[] quicksort(List arr, Comparator comparator) {
        if (comparator == null) {
            comparator = new Comparator() {
                public int compare(Object obj1, Object obj2) {
                    Comparable c1 = (Comparable) obj1;
                    Comparable c2 = (Comparable) obj2;
                    return c1.compareTo(c2);
                }
            };
        }
        int n = arr.size();
        int[] indx = new int[n];
        int i, indxt, ir = n - 1, itemp, j, k, l = 0;
        int jstack = 0, istack[];
        Object a;

        istack = new int[n + 1];

        for (j = 0; j < n; j++) {
            indx[j] = j;
        }
        for (; ; ) {
            if (ir - l < 7) {
                for (j = l + 1; j <= ir; j++) {
                    indxt = indx[j];
                    a = arr.get(indxt);
                    for (i = j - 1; i >= 0; i--) {
                        if (comparator.compare(arr.get(indx[i]), a) <= 0) {
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
                if (comparator.compare(arr.get(indx[l + 1]), arr.get(indx[ir])) > 0) {
                    SWAP(indx, l + 1, ir);
                }
                if (comparator.compare(arr.get(indx[l]), arr.get(indx[ir])) > 0) {
                    SWAP(indx, l, ir);
                }
                if (comparator.compare(arr.get(indx[l + 1]), arr.get(indx[l])) > 0) {
                    SWAP(indx, l + 1, l);
                }
                i = l + 1;
                j = ir;
                indxt = indx[l];
                a = arr.get(indxt);
                for (; ; ) {
                    do {
                        i++;
                    } while (comparator.compare(arr.get(indx[i]), a) < 0);
                    do {
                        j--;
                    } while (comparator.compare(arr.get(indx[j]), a) > 0);
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
     *initializeVector
     * Initializes a Vector with the elements of a String array
     *
     * @param array the array of Strings
     *
     * @return the Vector of Strings
     */
    private static Vector initializeVector(String array[]) {
        Vector v = new Vector();
        for (int i = 0; i < array.length; i++) {
            v.addElement(array[i]);
        }
        return v;
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

    /**
     * Appends the data in source onto target. If target is null, it initializes
     * target.
     * @param target the target of the copy
     * @param source the source data for the copy
     */
    public static void append(Vector target, Vector source) {
        if (source == null || source.size() <= 0) {
            return;
        }
        if (target == null) {
            target = new Vector();
        }
        for (int i = 0; i < source.size(); i++) {
            target.addElement(source.elementAt(i));
        }
    }

    /**
     * Appends the data in source onto target. If target is null, it initializes
     * target.
     * @param target the target of the copy
     * @param source the source data for the copy
     */
    public static byte[] append(byte[] target, byte[] source) {
        if (target == null || source == null || source.length <= 0) {
            return target;
        }
        byte[] newArray = new byte[target.length + source.length];
        System.arraycopy(target, 0, newArray, 0, target.length);
        System.arraycopy(source, 0, newArray, target.length, source.length);
        target = newArray;
        return target;
    }

    /**
     * Appends a new item onto an existing array. The class type of newItem must be the same as that of target.
     * @param target the target of the copy
     * @param newItem the new item to append
     */
    public static Object[] append(Object[] target, Object newItem) {
        if (newItem == null) {
            return target;
        }
        if (target == null) {
            Object[] newArray = (Object[]) java.lang.reflect.Array.newInstance(
                newItem.getClass(), 1);
            newArray[0] = newItem;
            target = newArray;
            return target;
        } else {
            Object[] newArray = (Object[]) java.lang.reflect.Array.newInstance(
                newItem.getClass(), target.length + 1);
            System.arraycopy(target, 0, newArray, 0, target.length);
            newArray[target.length] = newItem;
            target = newArray;
            return target;
        }
    }

    /**
     * Appends a new item onto an existing array. The class type of newItem must be the same as that of target.
     * @param source the source of the copy
     * @param newItem the new item to append
     * @return a new array containing the source elements and the newItem prepended
     */
    public static Object prepend(Object[] source, Object newItem) {
        if (newItem == null) {
            return source;
        }
        if (source == null) {
            Object[] newArray = (Object[]) java.lang.reflect.Array.newInstance(
                newItem.getClass(), 1);
            newArray[0] = newItem;
            return newArray;
        } else {
            Object[] newArray = (Object[]) java.lang.reflect.Array.newInstance(
                newItem.getClass(), source.length + 1);
            System.arraycopy(source, 0, newArray, 1, source.length);
            newArray[0] = newItem;
            return newArray;
        }
    }

    /**
     * Appends all items of an array onto an existing array. The class type of each elements of newItems
     * must be the same as that of target.
     * @param target the target of the copy
     * @param newItems an array of items to append
     */
    public static Object[] append(Object[] target, Object[] newItems) {
        if (newItems != null) {
            for (int i = 0; i < newItems.length; i++) {
                target = append(target, newItems[i]);
            }
        }
        return target;
    }

    /**
     * Appends the data in source onto target. If target is null, it initializes
     * target.
     * @param target the target of the copy
     * @param source the source data for the copy
     */
    public static void append(Vector target, Object[] source) {
        if (source == null || source.length <= 0) {
            return;
        }
        if (target == null) {
            target = new Vector();
        }
        for (int i = 0; i < source.length; i++) {
            target.addElement(source[i]);
        }
    }

    /**
     * Returns the index of an object in the array using the equals method of the array objects. -1 if it does not exist
     * @param array array to look for item
     * @param item item to look for
     */
    public static int indexOf(Object[] array, Object item) {
        if (item == null || array == null) {
            return -1;
        }
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(item)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns as a integer array the values in the List, List values must
     * by Number subclasses
     * @param numberList
     * @return
     */
    public static int[] getIntegerArrayFromList(List numberList) {
        int[] arr = new int[numberList.size()];
        for (int i = 0; i < numberList.size(); i++) {
            arr[i] = ( (Number) numberList.get(i)).intValue();
        }
        return arr;
    }

    /**
     * Returns as a double array with all elements set the given initial value
     */
    public static double[] createFilledArray(int size, double initialValue) {
        double[] arr = new double[size];
        for (int i = 0; i < size; i++) {
            arr[i] = initialValue;
        }
        return arr;
    }

    /**
     * Appends a new member to the array
     * @return
     */
    public static Object[] push(Object[] array, Object newMember) {
        if (array == null) {
            return null;
        }
        return append(array, newMember);
    }

    /**
     * Pops the array- returns an array containing
     * Index 0- the popped value
     * Index 1- the new array minus the last value in the original array
     * @param array
     * @return
     */
    public static Object[] pop(Object[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        Object[] newArray = (Object[]) java.lang.reflect.Array.newInstance(
            array[0].getClass(), array.length - 1);
        System.arraycopy(array, 0, newArray, 0, array.length - 1);
        return new Object[] {
            array[array.length - 1], newArray};
    }

    /**
     * Shift the array- returns an array containing
     * Index 0- the shifted value
     * Index 1- the new array minus the first value in the original array
     * @param array
     * @return
     */
    public static Object[] shift(Object[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        Object[] newArray = (Object[]) java.lang.reflect.Array.newInstance(
            array[0].getClass(), array.length - 1);
        System.arraycopy(array, 1, newArray, 0, array.length - 1);
        return new Object[] {
            array[0], newArray};
    }

    /**
     * Determines if a value exists in a List using a Comparator
     *
     * @param arr the List of objects
     * @param key Object
     * @param comparator Comparator to use
     * @return index of the search key, if it is contained in the list;
     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *	       <i>insertion point</i> is defined as the point at which the
     *	       key would be inserted into the list: the index of the first
     *	       element greater than the key, or <tt>list.size()</tt>, if all
     *	       elements in the list are less than the specified key.  Note
     *	       that this guarantees that the return value will be &gt;= 0 if
     *	       and only if the key is found.
     */
    public static int match(List arr, Object key, Comparator comparator) {
        int[] sortedIdx = quicksort(arr, comparator);

        int low = 0;
        int high = arr.size() - 1;

        while (low <= high) {
            int mid = (low + high) >> 1;
            Object midVal = arr.get(sortedIdx[mid]);
            int cmp = comparator.compare(midVal, key);

            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return sortedIdx[mid]; // key found
            }
        }
        return - (low + 1); // key not found.
    }

    /**
     * Determines if a value exists in a List.
     * THIS ASSUMES THE LIST IS IN ITS NATURAL ORDER
     * @param validList
     * @param key
     * @return
     */
    public static boolean match(List validList, String key) {
        if (key == null || key.length() == 0) {
            return false;
        }
        //Perform a fast binary search- This assumes the validList is in its natural order!!
        int low = 0;
        int high = validList.size() - 1;

        while (low <= high) {
            int mid = (low + high) >> 1;
            Object midVal = validList.get(mid);
            int cmp = ( (Comparable) midVal).compareTo(key);

            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return true; // key found
            }
        }
        return false; // key not found
    }

    /**
     * Determines if a value exists in an array.
     * THIS ASSUMES THE LIST IS IN ITS NATURAL ORDER
     * @param validList
     * @param key
     * @return
     */
    public static boolean match(Object[] validArray, String key) {
        if (key == null || key.length() == 0) {
            return false;
        }
        //Perform a fast binary search- This assumes the validList is in its natural order!!
        int low = 0;
        int high = validArray.length - 1;

        while (low <= high) {
            int mid = (low + high) >> 1;
            Object midVal = validArray[mid];
            int cmp = ( (Comparable) midVal).compareTo(key);

            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return true; // key found
            }
        }
        return false; // key not found
    }

    /**
     * Used for List paging- returns the number of pages for the list given the
     * records per page
     *
     * @param list List
     * @param recordsPerPage int
     * @return int
     */
    public static int getPages(List list, int recordsPerPage) {
        double pages = Math.ceil( (double) list.size() / (double) recordsPerPage);
        return (int) Math.round(pages);
    }

    /**
     * Used for List paging- returns a sub List for the given page with the
     * given number of records per page. The last page may have few than the
     * records per page if the list size is not divisible by the records per
     * page.
     *
     * @param list List
     * @param page int
     * @param recordsPerPage int
     * @return List
     */
    public static List getPage(List list, int page, int recordsPerPage) {
        int start = page * recordsPerPage;
        int end = Math.min(list.size(), start + recordsPerPage);

        if (start >= list.size() || start >= end) {
            return null;
        }
        return list.subList(start, end);
    }

    /**
     * Converts a collection of Number objects to a double array
     *
     * @param values Collection
     * @return double[]
     */
    public static double[] toDoubleArray(Collection<Number> values) {
        double[] d = new double[values.size()];
        int count = 0;
        for (Number value : values) {
            d[count++] = value.doubleValue();
        }
        return d;
    }

    public static void main(String[] args) {
        int[] s = {
            1, 2, 3, 4};
        List<Integer> s2 = new ArrayList(Arrays.asList(s));

        System.out.println(s2.indexOf(3));

    }

}
