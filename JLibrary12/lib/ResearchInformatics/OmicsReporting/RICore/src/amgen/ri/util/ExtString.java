package amgen.ri.util;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains a set of static method utilities for operating on Strings
 *
 * @version $Id: ExtString.java,v 1.13 2014/04/03 23:42:42 jemcdowe Exp $
 */
public final class ExtString implements java.io.Serializable {

  /*
   *
   * BELOW ARE STRING UTILITIES
   *
   */
  /**
   * Returns the given String, s, if not null. defaultValue otherwise.
   *
   * @param s String
   * @param defaultValue String
   * @return String
   */
  public static String applyIf(String s, String defaultValue) {
    return (s != null ? s : defaultValue);
  }

  /**
   * Returns the given String, s, has length using hasLength(s). defaultValue
   * otherwise.
   *
   * @param s String
   * @param defaultValue String
   * @return String
   */
  public static String applyIfLength(String s, String defaultValue) {
    return (hasLength(s) ? s : defaultValue);
  }

  /**
   * Returns whether the 2 Strings are equivalent checking that neither is null
   *
   * @param s String
   * @param v String
   * @return boolean
   */
  public static boolean equals(String s, String v) {
    return (s != null && v != null && s.equals(v));
  }

  /**
   * Returns whether the 2 Strings are equivalent ignoring case checking that
   * neither is null
   *
   * @param s String
   * @return boolean
   */
  public static boolean equalsIgnoreCase(String s, String v) {
    return (s != null && v != null && s.equalsIgnoreCase(v));
  }

  /**
   * Returns whether the String has length>0 first checking that it is not null
   *
   * @param s String
   * @return boolean
   */
  public static boolean hasLength(String s) {
    return (s != null && s.length() > 0);
  }

  /**
   * Returns whether every String in the arrays has length>0 first checking each
   * is not. If the array is null, this returns false. If the array is
   * zero-length, this returns true
   *
   */
  public static boolean haveLength(String[] strs) {
    if (strs == null) {
      return false;
    }
    for (String s : strs) {
      if (!ExtString.hasLength(s)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the String as upper case if it is not null; otherwise, returns null
   *
   * @param s String
   * @return String
   */
  public static String toUpperCase(String s) {
    return (s != null ? s.toUpperCase() : null);
  }

  /**
   * Returns the String as lower case if it is not null; otherwise, returns null
   *
   * @param s String
   * @return String
   */
  public static String toLowerCase(String s) {
    return (s != null ? s.toLowerCase() : null);
  }

  /**
   * Returns whether the String has length>0 after trimming first checking that
   * it is not null
   *
   * @param s String
   * @return boolean
   */
  public static boolean hasTrimmedLength(String s) {
    return (s != null && s.trim().length() > 0);
  }

  /**
   * Returns the index within this string of the first occurrence of the
   * specified character.
   *
   * @param ch a character.
   * @return the index of the first occurrence of the character in the character
   * sequence represented by this object, or
   * <code>-1</code> if the character does not occur.
   */
  public static int[] indexesOf(String s, char ch) {
    int[] idx = new int[countChar(s, ch)];
    if (idx.length > 0) {
      char[] array = s.toCharArray();
      int count = 0;
      for (int i = 0; i < array.length; i++) {
        if (array[i] == ch) {
          idx[count++] = i;
        }
      }
    }
    return idx;
  }

  /**
   * Returns the number of times a character appears in a String.
   *
   * @param s
   * <code>String</code> to search.
   * @return c Character to count
   */
  public static int countChar(String s, char c) {
    int num = 0;
    char[] string = s.toCharArray();
    for (int i = 0; i < string.length; i++) {
      if (string[i] == c) {
        num++;
      }
    }
    return num;
  }

  /**
   * Returns the number of times a character type (see Character) appears in a
   * String as a byte array.
   *
   * @param s
   * <code>String</code> as byte array to search.
   * @return c Character to count
   */
  public static int countChar(byte[] s, int type) {
    int num = 0;
    for (int i = 0; i < s.length; i++) {
      if (Character.getType((char) s[i]) == type) {
        num++;
      }
    }
    return num;
  }

  /**
   * Returns the number of times a word occurs in a String. Just a wrapper for
   * StringUtils.countMatches
   *
   * @param s
   * @param word
   * @return
   */
  public static int countOccurrences(String s, String word) {
    if (!hasLength(s) || !hasLength(word)) {
      return 0;
    }
    int count = 0;
    int idx = 0;
    while ((idx = s.indexOf(word, idx)) != -1) {
      count++;
      idx += s.length();
    }
    return count;
  }

  /**
   * Does a replacement of a given term with a replacement starting from a given
   * index
   *
   * @param s
   * @param startIndex
   * @param replacementTerm
   * @param replacementValue
   * @return
   */
  public static String replaceFrom(String s, int startIndex, String replacementTerm, String replacementValue) {
    String prefix = s.substring(0, startIndex);
    String rest = s.substring(startIndex);
    rest = rest.replace(replacementTerm, replacementValue);
    return prefix + rest;
  }

  /**
   * Returns the length os a String including coe points above the base set-
   * includes supplementary characters represented as a surrogate pair
   *
   * @param s String
   * @return int
   */
  public static int length(String s) {
    int charLen = s.length();
    return s.codePointCount(0, charLen);
  }

  /**
   * Replaces all substrings in a String with another string. The search for the
   * replace substring continues until one of the following: 1) The replaceWith
   * array runs out of elements 2) The entire source runs out of substrings
   * matching replace 3) The current replaceWith element contains replace as a
   * substring. If this were allowed an infinite loop would result. The replace
   * and replaceWith substrings do not have to be the same length.
   *
   * @param target the original string.
   * @param replace the string to replace.
   * @param replaceWith the strings which are inserted in place of replace.
   * @return a
   * <code>String</code> that contains the replaced string.
   */
  public static String replace(String source, String replace, String[] replaceWith) {
    int indx;
    int count = 0;
    while ((indx = source.indexOf(replace)) >= 0 && count < replaceWith.length) {
      if (replaceWith[count].indexOf(replace) >= 0) {
        break;
      }
      source = replace(source, replace, replaceWith[count]);
      count++;
    }
    return source;
  }

  /**
   * Replaces a substring in a String with another string. The search for the
   * replace string is from the left. The replace and replaceWith substrings do
   * not have to be the same length.
   *
   * @param target the original string.
   * @param replace the string to replace.
   * @param replaceWith the string which is inserted in place of replace.
   * @return a
   * <code>String</code> that contains the replaced string.
   */
  public static String replace(String source, String replace, String replaceWith) {
    int indx = source.indexOf(replace);
    if (indx < 0) {
      return source;
    }
    StringBuffer sb = new StringBuffer(source.substring(0, indx));
    sb.append(replaceWith);
    sb.append(source.substring(indx + replace.length()));
    return sb.toString();
  }

  /**
   * Replaces all occurences of a char with a String.
   *
   * @param source the original string.
   * @param replace the char to replace.
   * @param replaceWith the string which is inserted in place of replace.
   * @return a
   * <code>String</code> that contains the replaced string.
   */
  public static String replaceAll(String source, char replace, String replaceWith) {
    int indx = source.indexOf(replace);
    if (indx < 0) {
      return source;
    }
    StringBuffer sb = new StringBuffer();
    char[] stringChars = source.toCharArray();
    for (int i = 0; i < stringChars.length; i++) {
      if (stringChars[i] == replace) {
        sb.append(replaceWith);
      } else {
        sb.append(stringChars[i]);
      }
    }
    return sb.toString();
  }

  /**
   * Replaces all occurences of a char with the corresponding member of the
   * String array String. If there are more String[] members than replacement
   * char's, the String[] members are used until all char's are replaced If
   * there are more replacement char's than String[] members, the last String[]
   * member is repeated.
   *
   * @param source the original string.
   * @param replace the char to replace.
   * @param replaceWith the string array which is inserted in place of replace.
   * @return a
   * <code>String</code> that contains the replaced string.
   */
  public static String replaceAll(String source, char replace, String[] replaceWith) {
    int indx = source.indexOf(replace);
    if (indx < 0 || replaceWith == null || replaceWith.length == 0) {
      return source;
    }
    StringBuffer sb = new StringBuffer();
    char[] stringChars = source.toCharArray();
    int count = 0;
    for (int i = 0; i < stringChars.length; i++) {
      if (stringChars[i] == replace) {
        sb.append(replaceWith[count]);
        if (count < replaceWith.length - 1) {
          count++;
        }
      } else {
        sb.append(stringChars[i]);
      }
    }
    return sb.toString();
  }

  /**
   * Replaces all occurences of a char with the corresponding member of the
   * String CSV String. If there are more CSV members than replacement char's,
   * the CSV members are used until all char's are replaced If there are more
   * replacement char's than CSV members, the last CSV member is repeated.
   *
   * @param source the original string.
   * @param replace the char to replace.
   * @param replaceWith the CSV string which is inserted in place of replace.
   * @return a
   * <code>String</code> that contains the replaced string.
   */
  public static String replaceAllCSV(String source, char replace, String replaceWith) {
    return replaceAll(source, replace, splitCSV(replaceWith));
  }

  /**
   *
   * @param source
   * @param replace
   * @param replaceWith
   * @return
   */
  public static String replaceAll(String source, char[] replace, String replaceWith) {
    if (replace == null) {
      return source;
    }
    String target = source;
    for (int i = 0; i < replace.length; i++) {
      target = replace(target, replace[i] + "", replaceWith);

    }
    return target;
  }

  /**
   * Replaces a substring in a String with another string. The search for the
   * replace string is from the right. The replace and replaceWith substrings do
   * not have to be the same length.
   *
   * @param target the original string.
   * @param replace the string to replace.
   * @param replaceWith the string which is inserted in place of replace.
   * @return a
   * <code>String</code> that contains the replaced string.
   */
  public static String replaceLast(String target, String replace, String replaceWith) {
    int indx = target.lastIndexOf(replace);
    if (indx < 0) {
      return target;
    }
    StringBuffer sb = new StringBuffer(target.substring(0, indx));
    sb.append(replaceWith);
    sb.append(target.substring(indx + replace.length()));
    return sb.toString();
  }

  /**
   * Joins the items of a String array into one long String. Similar to
   * concatArray, but offsets any concatenated array which contains the
   * delimiter in double-quotes. Any double quotes in array elements are
   * replaced to single quotes.
   *
   * @param array the original string array.
   * @param delim the delimiter to use between array elements
   * @return a
   * <code>String</code> that contains each array element.
   */
  public static String join(Object[] array, char delim) {
    if (array == null || array.length == 0) {
      return "";
    }
    if (array.length == 1) {
      if (array[0] == null) {
        return "null";
      } else {
        return array[0].toString();
      }
    }
    StringBuffer sb = new StringBuffer();

    for (int i = 0; i < array.length; i++) {
      if (i > 0) {
        sb.append(delim);
      }
      if (array[i] == null) {
        sb.append("null");
      } else {
        sb.append((array[i].toString().indexOf(delim) < 0 ? array[i].toString() : '"' + array[i].toString().replace('"', '\'') + '"'));
      }
    }
    return sb.toString();
  }

  /**
   * Joins the items of a String array into one long String. Similar to
   * concatArray, but offsets any concatenated array which contains the
   * delimiter in double-quotes. Any double quotes in array elements are
   * replaced to single quotes.
   *
   * @param array the original string array.
   * @param delim the delimiter to use between array elements
   * @return a
   * <code>String</code> that contains each array element.
   */
  public static String join(Object[] array, String delim) {
    if (array == null || array.length == 0) {
      return "";
    }
    if (array.length == 1) {
      if (array[0] == null) {
        return "null";
      } else {
        return array[0].toString();
      }
    }
    StringBuffer sb = new StringBuffer();

    for (int i = 0; i < array.length; i++) {
      if (i > 0) {
        sb.append(delim);
      }
      if (array[i] == null) {
        sb.append("null");
      } else {
        sb.append((array[i].toString().indexOf(delim) < 0 ? array[i].toString() : '"' + array[i].toString().replace('"', '\'') + '"'));
      }
    }
    return sb.toString();
  }

  /**
   * Joins the items of a String array into one long String. Similar to
   * concatArray, but offsets any concatenated array which contains the
   * delimiter in double-quotes. Any double quotes in array elements are
   * replaced to sinigle quotes.
   *
   * @param array the original string array.
   * @param delim the delimiter to use between array elements
   * @return a
   * <code>String</code> that contains each array element.
   */
  public static String join(Object[] array, char delim, int start, int end) {
    if (array == null || array.length == 0) {
      return "";
    }
    if (array.length == 1) {
      if (array[0] == null) {
        return "null";
      } else {
        return array[0].toString();
      }
    }
    StringBuffer sb = new StringBuffer();

    for (int i = start; i < end; i++) {
      if (i > start) {
        sb.append(delim);
      }
      if (array[i] == null) {
        sb.append("null");
      } else {
        sb.append((array[i].toString().indexOf(delim) < 0 ? array[i].toString() : '"' + array[i].toString().replace('"', '\'') + '"'));
      }
    }
    return sb.toString();
  }

  /**
   * Joins the items of a String array into one long String. Similar to
   * concatArray, but offsets any concatenated array which contains the
   * delimiter in double-quotes. Any double quotes in array elements are
   * replaced to sinigle quotes.
   *
   * @param array the original string array.
   * @param delim the delimiter to use between array elements
   * @return a
   * <code>String</code> that contains each array element.
   */
  public static String join(List array, char delim) {
    if (array == null || array.size() == 0) {
      return "";
    }
    if (array.size() == 1) {
      return array.get(0).toString();
    }
    StringBuffer sb = new StringBuffer((array.get(0).toString().indexOf(delim) < 0 ? array.get(0).toString()
            : '"' + array.get(0).toString().replace('"', '\'') + '"'));
    for (int i = 1; i < array.size(); i++) {
      sb.append(delim);
      if (array == null) {
        sb.append("null");
      } else {
        sb.append((array.get(i).toString().indexOf(delim) < 0 ? array.get(i).toString() : '"' + array.get(i).toString().replace('"', '\'') + '"'));
      }
    }
    return sb.toString();
  }

  /**
   * Joins the items of a List into one long String. Similar to concatArray, but
   * offsets any concatenated array which contains the delimiter in
   * double-quotes. Any double quotes in array elements are replaced to sinigle
   * quotes.
   *
   * @param array the original string array.
   * @param delim the delimiter to use between array elements
   * @return a
   * <code>String</code> that contains each array element.
   */
  public static String join(List array, String delim) {
    if (array == null || array.size() == 0) {
      return "";
    }
    if (array.size() == 1) {
      return array.get(0).toString();
    }
    StringBuffer sb = new StringBuffer(array.get(0).toString());
    for (int i = 1; i < array.size(); i++) {
      sb.append(delim);
      if (array.get(i) == null) {
        sb.append("null");
      } else {
        sb.append(array.get(i).toString());
      }
    }
    return sb.toString();
  }

  /**
   * Joins the items of a Set into one long String. Similar to concatArray, but
   * offsets any concatenated array which contains the delimiter in
   * double-quotes. Any double quotes in array elements are replaced to single
   * quotes.
   *
   * @param array the original string array.
   * @param delim the delimiter to use between array elements
   * @return a
   * <code>String</code> that contains each array element.
   */
  public static String join(Set array, String delim) {
    if (array == null || array.size() == 0) {
      return "";
    }
    if (array.size() == 1) {
      return array.iterator().next().toString();
    }
    StringBuffer sb = new StringBuffer();
    for (Object field : array) {
      if (sb.length() > 0) {
        sb.append(delim);
      }
      if (field == null) {
        sb.append("null");
      } else {
        sb.append(field.toString());
      }
    }
    return sb.toString();
  }

  /**
   * Joins the items of a Set into one long String. Similar to concatArray, but
   * offsets any concatenated array which contains the delimiter in
   * double-quotes. Any double quotes in array elements are replaced to single
   * quotes.
   *
   * @param array the original string array.
   * @param delim the delimiter to use between array elements
   * @return a
   * <code>String</code> that contains each array element.
   */
  public static String join(Set array, char delim) {
    if (array == null || array.size() == 0) {
      return "";
    }
    if (array.size() == 1) {
      return array.iterator().next().toString();
    }
    StringBuffer sb = new StringBuffer();
    for (Object field : array) {
      if (sb.length() > 0) {
        sb.append(delim);
      }
      if (field == null) {
        sb.append("null");
      } else {
        sb.append(field.toString());
      }
    }
    return sb.toString();
  }

  /**
   * Concatenates the items of a String array into one long String
   *
   * @param array the original string array.
   * @return a
   * <code>String</code> that contains each array element.
   */
  public static String concatArray(Object[] array) {
    if (array.length == 1) {
      return array[0].toString();
    }
    StringBuffer sb = new StringBuffer(array[0].toString());
    for (int i = 1; i < array.length; i++) {
      sb.append(array[i]);
    }
    return sb.toString();
  }

  /**
   * Concatenates the items of a String array into one long,delimited String
   *
   * @param array the original string array.
   * @param delim character to put between each array element
   * @return a
   * <code>String</code> that contains each array element.
   */
  public static String concat(Object[] array, char delim) {
    return concatArray(array, delim);
  }

  /**
   * Concatenates the items of a String array into one long,delimited String
   *
   * @param array the original string array.
   * @param delim character to put between each array element
   * @return a
   * <code>String</code> that contains each array element.
   */
  public static String concatArray(Object[] array, char delim) {
    if (array.length == 1) {
      return array[0].toString();
    }
    StringBuffer sb = new StringBuffer(array[0].toString());
    for (int i = 1; i < array.length; i++) {
      sb.append(delim);
      sb.append(array[i]);
    }
    return sb.toString();
  }

  /**
   * Concatenates the items of a String array into one long,delimited String
   *
   * @param array the original string array.
   * @param delim character to put between each array element
   * @return a
   * <code>String</code> that contains each array element.
   */
  public static String concat(Object[] array, String delim) {
    return concatArray(array, delim);
  }

  /**
   * Concatenates the items of a String array into one long,delimited String
   *
   * @param array the original string array.
   * @param delim character to put between each array element
   * @return a
   * <code>String</code> that contains each array element.
   */
  public static String concatArray(Object[] array, String delim) {
    if (array.length == 1) {
      return array[0].toString();
    }
    StringBuffer sb = new StringBuffer(array[0].toString());
    for (int i = 1; i < array.length; i++) {
      sb.append(delim);
      sb.append(array[i]);
    }
    return sb.toString();
  }

  /**
   * Concatenates the items of a String array into one long,delimited String
   *
   * @param array the original string array.
   * @param delim character to put between each array element
   * @return a
   * <code>String</code> that contains each array element.
   */
  public static String concatArray(String[] array, int start, int end) {
    if (array.length == 1) {
      return array[0];
    }
    StringBuffer sb = new StringBuffer(array[0]);
    for (int i = start; i < end; i++) {
      sb.append(array[i]);
    }
    return sb.toString();
  }

  /**
   * Concatenates the items of a String array into one long,delimited String
   *
   * @param array the original string array.
   * @param delim character to put between each array element
   * @return a
   * <code>String</code> that contains each array element.
   */
  public static String concatArray(Object[] array, int start, int end, char delim) {
    if (array.length == 1) {
      return array[0].toString();
    }
    StringBuffer sb = new StringBuffer(array[0].toString());
    for (int i = start; i < end; i++) {
      sb.append(delim);
      sb.append(array[i]);
    }
    return sb.toString();
  }

  /**
   * Concatenates the items of a String array into one long,delimited String
   *
   * @param array the original string array.
   * @param delim character to put between each array element
   * @return a
   * <code>String</code> that contains each array element.
   */
  public static String concatArray(Object[] array, int start) {
    if (array.length == 0) {
      return "";
    }
    if (array.length == 1) {
      return array[0].toString();
    }
    if (start >= array.length) {
      start = array.length - 1;
    }
    StringBuffer sb = new StringBuffer(array[start].toString());
    for (int i = start + 1; i < array.length; i++) {
      sb.append(array[i]);
    }
    return sb.toString();
  }

  /**
   * Concatenates the items of a String array into one long,delimited String
   *
   * @param array the original string array.
   * @param delim character to put between each array element
   * @return a
   * <code>String</code> that contains each array element.
   */
  public static String concatArray(Object[] array, int start, char delim) {
    if (array.length == 1) {
      return array[0].toString();
    }
    StringBuffer sb = new StringBuffer(array[start].toString());
    for (int i = start + 1; i < array.length; i++) {
      sb.append(delim);
      sb.append(array[i]);
    }
    return sb.toString();
  }

  /**
   * Adds a field to an emerging delimited list. If field is null, it returns
   * the list. If list is null or length zero, it returns the field. Otherwise,
   * it returns the concatenation of list+delimiter+field.
   *
   * @param list String
   * @param field String
   * @param delimiter String
   * @return String
   */
  public static String addToDelimitedList(String list, String field, String delimiter) {
    if (field == null) {
      return list;
    }
    if (list == null || list.length() == 0) {
      return field;
    }
    return list + delimiter + field;
  }

  /**
   * Removes all of the given character from the given string
   *
   * @param s the original string
   * @param s character to remove
   * @return a
   * <code>String</code> with the character removed.
   */
  public static String remove(String s, char c) {
    StringBuffer sb = new StringBuffer();
    char[] chars = s.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if (chars[i] != c) {
        sb.append(chars[i]);
      }
    }
    return sb.toString();
  }

  /**
   * Removes all of the given character from the given string
   *
   * @param s the original string
   * @param s character to remove
   * @return a
   * <code>String</code> with the character removed.
   */
  public static String remove(String s, char[] c) {
    StringBuffer sb = new StringBuffer();
    char[] chars = s.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      boolean ok = true;
      for (int j = 0; j < c.length; j++) {
        if (chars[i] == c[j]) {
          ok = false;
          break;
        }
      }
      if (ok) {
        sb.append(chars[i]);
      }
    }
    return sb.toString();
  }

  /**
   * Removes all of the given string from the given string
   *
   * @param s the original string
   * @param removeItem string to remove
   * @return a
   * <code>String</code> with the string removed.
   */
  public static String remove(String s, String removeItem) {
    StringBuffer sb = new StringBuffer();
    String s1;
    String s2 = s;
    int indx;
    while ((indx = s2.indexOf(removeItem)) >= 0) {
      if (indx > 0) {
        s1 = s2.substring(0, indx);
        sb.append(s1);
      }
      if (indx >= s2.length() - removeItem.length()) {
        s2 = null;
        break;
      }
      s2 = s2.substring(indx + removeItem.length());
    }
    if (s2 != null) {
      sb.append(s2);
    }
    return sb.toString();
  }

  /**
   * Repeats a String num times with delim delimiting
   *
   * @param repeatStr String
   * @param num int
   * @param delim String
   * @return String
   */
  public static String repeat(String repeatStr, int num, String delim) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < num; i++) {
      if (sb.length() > 0 && delim != null) {
        sb.append(delim);
      }
      sb.append(repeatStr);
    }
    return sb.toString();
  }

  /**
   * Repeats a String num times into a List
   *
   * @param repeatStr String
   * @param num int
   * @return List<String>
   */
  public static List<String> repeat(String repeatStr, int num) {
    List<String> list = new ArrayList<String>();
    for (int i = 0; i < num; i++) {
      list.add(repeatStr);
    }
    return list;
  }

  /**
   * Returns whether the given string is a parsable number
   *
   * @param s string
   * @return true if the string is a parsable number, false otherwise
   */
  public static boolean isANumber(String s) {
    try {
      Double.valueOf(s);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  /**
   * Returns whether the given string is a parsable integer
   *
   * @param s string
   * @return true if the string is a parsable integer, false otherwise
   */
  public static boolean isAInteger(String s) {
    double val = toDouble(s);
    if (Double.isNaN(val)) {
      return false;
    }
    return (val == (int) val);
  }

  /**
   * Returns whether the given string is equal to one of the given members of
   * the string array
   *
   * @param s string to test
   * @param array strings to look for s
   */
  public static boolean in(String s, String[] array) {
    for (int i = 0; i < array.length; i++) {
      if (s.equals(array[i])) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns whether the given string is equal to one of the given members of
   * the string array
   *
   * @param s string to test
   * @param array strings to look for s
   */
  public static String removeTrailing(String s, char c) {
    char[] chars = s.toCharArray();
    int index;
    for (index = chars.length - 1; index >= 0; index--) {
      if (chars[index] != c) {
        break;
      }
    }
    return s.substring(0, index + 1);
  }

  /**
   * Returns whether the given string is equal to one of the given members of
   * the string array, ignoring the case
   *
   * @param s string to test
   * @param array strings to look for s
   */
  public static boolean inIgnoreCase(String s, String[] array) {
    for (int i = 0; i < array.length; i++) {
      if (s.equalsIgnoreCase(array[i])) {
        return true;
      }
    }
    return false;
  }

  /**
   * Compares String s1 to String s2. The result is true if and only if both
   * Strings are not null and both Strings are the same sequence of characters.
   */
  public static boolean isEqual(String s1, String s2) {
    if (s1 == null || s2 == null) {
      return false;
    }
    return s1.equals(s2);
  }

  /**
   * Performs a case-insensitive comparison of String s1 to String s2. The
   * result is true if and only if both Strings are not null and both Strings
   * are the same sequence of characters ignoring case.
   */
  public static boolean isEqualIgnoreCase(String s1, String s2) {
    if (s1 == null || s2 == null) {
      return false;
    }
    return s1.equalsIgnoreCase(s2);
  }

  /**
   * Compares all s1 elements to String s2. The result is true any matches s2
   */
  public static boolean anyIsEqual(String[] s1, String s2) {
    if (s1 == null || s2 == null) {
      return false;
    }
    for (String s : s1) {
      if (isEqual(s, s2)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Compares all s1 elements to String s2. The result is true any matches s2,
   * ignoring case
   */
  public static boolean anyIsEqualIgnoreCase(String[] s1, String s2) {
    if (s1 == null || s2 == null) {
      return false;
    }
    for (String s : s1) {
      if (isEqualIgnoreCase(s, s2)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Compares all s1 elements to String s2. The result is true all match s2
   */
  public static boolean allIsEqual(String[] s1, String s2) {
    if (s1 == null || s2 == null) {
      return false;
    }
    for (String s : s1) {
      if (!isEqual(s, s2)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compares all s1 elements to String s2. The result is true all match s2,
   * ignoring case
   */
  public static boolean allIsEqualIgnoreCase(String[] s1, String s2) {
    if (s1 == null || s2 == null) {
      return false;
    }
    for (String s : s1) {
      if (!isEqualIgnoreCase(s, s2)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compares all s1 elements to String s2 ignoring any non-alphanumerics. The
   * result is true all match s2
   */
  public static boolean allAlphaNumericsEqual(String[] s1, String s2) {
    if (s1 == null || s2 == null) {
      return false;
    }
    for (String s : s1) {
      if (s == null) {
        return false;
      }
      String t1 = s.replaceAll("[^\\p{Alnum}]", "");
      String t2 = s2.replaceAll("[^\\p{Alnum}]", "");

      if (!isEqual(t1, t2)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compares all s1 elements to String s2 ignoring any non-alphanumerics. The
   * result is true all match s2, ignoring case
   */
  public static boolean allAlphaNumericsEqualIgnoreCase(String[] s1, String s2) {
    if (s1 == null || s2 == null) {
      return false;
    }
    for (String s : s1) {
      if (s == null) {
        return false;
      }
      String t1 = s.replaceAll("[^\\p{Alnum}]", "");
      String t2 = s2.replaceAll("[^\\p{Alnum}]", "");
      if (!isEqualIgnoreCase(t1, t2)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compares s1 to s2 ignoring any non-alphanumerics.
   */
  public static boolean alphaNumericsEqual(String s1, String s2) {
    if (s1 == null || s2 == null) {
      return false;
    }
    String t1 = s1.replaceAll("[^\\p{Alnum}]", "");
    String t2 = s2.replaceAll("[^\\p{Alnum}]", "");
    return t1.equals(t2);
  }

  /**
   * Compares s1 to s2 ignoring any non-alphanumerics and ignoring case
   */
  public static boolean alphaNumericsEqualIgnoreCase(String s1, String s2) {
    if (s1 == null || s2 == null) {
      return false;
    }
    return alphaNumericsEqual(s1.toUpperCase(), s2.toUpperCase());
  }

  /**
   * Compares all s1 elements to String s2 ignoring any non-alphanumerics. The
   * result is true all match s2
   */
  public static boolean anyAlphaNumericsEqual(String[] s1, String s2) {
    if (s1 == null || s2 == null) {
      return false;
    }
    for (String s : s1) {
      if (s != null) {
        String t1 = s.replaceAll("[^\\p{Alnum}]", "");
        String t2 = s2.replaceAll("[^\\p{Alnum}]", "");

        if (isEqual(t1, t2)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Compares all s1 elements to String s2 ignoring any non-alphanumerics. The
   * result is true all match s2, ignoring case
   */
  public static boolean anyAlphaNumericsEqualIgnoreCase(String[] s1, String s2) {
    if (s1 == null || s2 == null) {
      return false;
    }
    for (String s : s1) {
      if (s != null) {
        String t1 = s.replaceAll("[^\\p{Alnum}]", "");
        String t2 = s2.replaceAll("[^\\p{Alnum}]", "");
        if (isEqualIgnoreCase(t1, t2)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Check if the given String contains any non-whitespace characters The result
   * is true if and only if the Strings is not null and it has at least 1
   * non-whitespace character.
   */
  public static boolean containsString(String s1) {
    if (s1 == null) {
      return false;
    }
    return s1.trim().length() > 0;
  }

  /**
   * Check if the given String contains another string
   */
  public static boolean contains(String s1, String s2) {
    if (s1 == null || s2 == null) {
      return false;
    }
    return s1.indexOf(s2) >= 0;
  }

  /**
   * Check if the given String contains another string ignoring case. If either
   * is null, this returns false
   */
  public static boolean containsIgnoreCase(String s1, String s2) {
    if (s1 == null || s2 == null) {
      return false;
    }
    return contains(s1.toUpperCase(), s2.toUpperCase());
  }

  /**
   * Splits a CommaSeparatedValues formatted string into contituents. CSV
   * formatting has fields separated by commas. Fields containing commas must be
   * enclosed in double-quotes ("). The starting quote must follow the previous
   * field's comma. The terminating quote can appear anywhere following the
   * start. i.e. the following are equivalent: 0.1,3.0,"hello to you, bob",5.3
   * 0.1,3.0,"hello to you," bob,5.3 Note: End of line characters are treated as
   * normal characters
   *
   * @param s the CSV formatted string to split
   * @return an array of strings of split fields
   */
  public static String[] splitCSV(String s) {
    List splits = new ArrayList();
    char[] c = s.toCharArray();
    StringBuffer sb = new StringBuffer();
    boolean inQuotes = false;
    for (int i = 0; i < c.length; i++) {
      switch (c[i]) {
        case (','): //Possible field delimiter unless in quotes
          if (!inQuotes) {
            splits.add(sb.toString());
            sb.setLength(0);
            break;
          } else {
            sb.append(c[i]);
            break;
          }
        case ('"'): //Possible start or terminating quote
          if (inQuotes) {
            inQuotes = false;
            break;
          }
          if (sb.length() == 0) {
            inQuotes = true;
            break;
          }
        default: //All other characters AND fall through for non-start or terminating quotes
          sb.append(c[i]);
      }
    }
    splits.add(sb.toString());
    return (String[]) splits.toArray(new String[0]);
  }

  /**
   * Splits the given string into an array of strings delmited by the given
   * character
   *
   * @param s the string to split
   * @param delim the field delimiter
   * @return an array of strings of split fields
   */
  public static String[] split(String s, char delim) {
    return split(s, delim, 0);
  }

  /**
   * Splits the given string into an array of strings delmited by the given
   * character
   *
   * @param s the string to split
   * @param delim the field delimiter
   * @return an array of strings of split fields
   */
  public static String[] split(String s, char delim, int num) {
    List splits = new ArrayList();
    String s1;
    String s2 = s;
    int indx;
    int count = 0;
    while ((indx = s2.indexOf(delim)) >= 0) {
      if (indx > 0) {
        s1 = s2.substring(0, indx);
        splits.add(s1);
      }
      if (indx >= s2.length() - 1) {
        s2 = null;
        break;
      }
      s2 = s2.substring(indx + 1);
      count++;
      if (num > 0 && count + 1 >= num) {
        break;
      }
    }
    if (s2 != null) {
      splits.add(s2);
    }
    return (String[]) splits.toArray(new String[0]);
  }

  /**
   * Splits the given string into an array of strings delmited by the given
   * string
   *
   * @param s the string to split
   * @param delim the field delimiter
   * @return an array of strings of split fields
   */
  public static String[] split(String s, String delim) {
    if (delim == null) {
      return new String[]{
                s};
    }
    if (s == null) {
      return null;
    }
    List splits = new ArrayList();
    String s1;
    String s2 = s;
    int indx;
    while ((indx = s2.indexOf(delim)) >= 0) {
      if (indx > 0) {
        s1 = s2.substring(0, indx);
        splits.add(s1);
      }
      if (indx >= s2.length() - delim.length()) {
        s2 = null;
        break;
      }
      s2 = s2.substring(indx + delim.length());
    }
    if (s2 != null) {
      splits.add(s2);
    }
    return (String[]) splits.toArray(new String[0]);
  }

  /**
   * Splits a String at the indexes defined in the Region objects
   *
   * @param s the source String
   * @param r regions objects defining start and end positions in the String
   * @return array of trimmed substrings from s defined by r elements
   */
  public static String[] split(String s, Region[] r) {
    return split(s, r, true);
  }

  /**
   * Splits a String at the indexes defined in the Region objects
   *
   * @param s the source String
   * @param r regions objects defining start and end positions in the String
   * @param trim whether to trim the substrings
   * @return array of substrings from s defined by r elements
   */
  public static String[] split(String s, Region[] r, boolean trim) {
    ArrayList list = new ArrayList();
    for (int i = 0; i < r.length; i++) {
      int start = r[i].getStart();
      int end = r[i].getEnd();

      if (start < 0) {
        start = 0;
      }
      if (end > s.length()) {
        end = s.length();
      }
      if (end < start) {
        continue;
      }
      String substring = s.substring(start, end);
      if (trim) {
        list.add(substring.trim());
      } else {
        list.add(substring);
      }
    }
    return (String[]) list.toArray(new String[0]);
  }

  public static String[] split(String s, int[] startCoords) {
    Region[] r = new Region[startCoords.length];
    for (int i = 0; i < startCoords.length - 1; i++) {
      r[i] = new Region(startCoords[i], startCoords[i + 1]);
    }
    r[startCoords.length - 1] = new Region(startCoords[startCoords.length - 1], s.length());
    return split(s, r, false);
  }

  public static String[] split(String s, Integer[] startCoords) {
    Region[] r = new Region[startCoords.length];
    for (int i = 0; i < startCoords.length - 1; i++) {
      int startCoord1 = startCoords[i].intValue();
      int startCoord2 = startCoords[i + 1].intValue();

      r[i] = new Region(startCoord1, startCoord2);
    }
    r[startCoords.length - 1] = new Region(startCoords[startCoords.length - 1].intValue(), s.length());
    return split(s, r, false);
  }

  /**
   * Identical to String.split, but returns a List<String>
   *
   * @param s String
   * @param regex String
   * @return List
   */
  public static List<String> splitToList(String s, String regex) {
    if (s == null) {
      return new ArrayList<String>();
    }
    return Arrays.asList(s.split(regex));
  }

  /**
   * Identical to String.split, but 1. Optionally only adds an element if it has
   * non-zero, trimmed length 2. Returns a List<String>
   *
   * @param s String
   * @param regex String
   * @param nonZeroLengthOny boolean
   * @return List
   */
  public static List<String> splitToList(String s, String regex, boolean nonZeroLengthOnly) {
    if (s == null) {
      return new ArrayList<String>();
    }
    List<String> list = new ArrayList<String>();

    for (String f : s.split(regex)) {
      if (!nonZeroLengthOnly || hasTrimmedLength(s)) {
        list.add(s);
      }
    }
    return list;
  }

  /**
   * Identical to String.split, but returns a List<String>
   *
   * @param s String
   * @param regex String
   * @param limit int
   * @return List
   */
  public static List<String> splitToList(String s, String regex, int limit) {
    if (s == null) {
      return new ArrayList<String>();
    }
    return Arrays.asList(s.split(regex, limit));
  }

  /**
   * Returns a String with length less than or equal to length optionally
   * appending an ellipse (...)
   *
   * @param s String
   * @param length int
   * @param ellipse boolean
   * @return String
   */
  public static String truncate(String s, int length, boolean ellipse) {
    if (s == null) {
      return null;
    }
    if (s.length() < length) {
      return s;
    }
    if (ellipse) {
      return s.substring(0, length - 3) + "...";
    }
    return s.substring(0, length);
  }

  /**
   * Splits the source String at the any of the given characters provided in the
   * chars String
   *
   * @param s the source String
   * @param chars list of characters used as delimiters to split the source
   * String
   * @return array of Strings split from s
   */
  public static String[] splitByChars(String s, String chars) {
    ArrayList list = new ArrayList();
    StringTokenizer st = new StringTokenizer(s, chars);
    while (st.hasMoreTokens()) {
      list.add(st.nextToken());
    }
    return (String[]) list.toArray(new String[0]);
  }

  /**
   * Returns a byte array of the given String <I>without</I> correct char->byte
   * conversion. String.getBytes() takes about ten times as long as
   * String.getBytes(int srcBegin, int srcEnd, byte dst[], int dstBegin). This
   * is because the former does correct byte-to-char conversion, which involves
   * a function call per character. The latter is deprecated, but you can get
   * 10% faster than it without any deprecated methods using this method.
   */
  public static byte[] getBytesFast(String s) {
    int length = s.length();
    char[] buffer = new char[length];
    s.getChars(0, length, buffer, 0);
    byte b[] = new byte[length];
    for (int j = 0; j < length; j++) {
      b[j] = (byte) buffer[j];
    }
    return b;
  }

  /**
   * Formats the sequence with the given width . Any non-digit or non-character
   * is removed
   *
   * @param sequence the sequence as a byte array
   * @param lineLength the length of each line
   */
  public static String getSequenceFormattedString(String sequence, int lineLength) {
    return getSequenceFormattedString(sequence.getBytes(), lineLength, 0);
  }

  /**
   * Formats the sequence with the given width and block length. Any non-digit
   * or non-character is removed
   *
   * @param sequence the sequence as a byte array
   * @param lineLength the length of each line
   * @param block the block size- each block is appended by a space
   */
  public static String getSequenceFormattedString(String sequence, int lineLength, int block) {
    return getSequenceFormattedString(sequence, lineLength, block, "\n");
  }

  /**
   * Formats the sequence with the given width and block length. Any non-digit
   * or non-character is removed
   *
   * @param sequence the sequence as a byte array
   * @param lineLength the length of each line
   * @param block the block size- each block is appended by a space
   * @param lineDelim string used to delimit lines
   */
  public static String getSequenceFormattedString(String sequence, int lineLength, int block, String lineDelim) {
    return getSequenceFormattedString(sequence.getBytes(), lineLength, block, lineDelim);
  }

  /**
   * Formats the sequence in the byte array with the given width and block
   * length. Any non-digit or non-character is removed
   *
   * @param sequence the sequence as a byte array
   * @param lineLength the length of each line
   * @param block the block size- each block is appended by a space
   */
  public static String getSequenceFormattedString(byte[] sequence, int lineLength, int block) {
    return getSequenceFormattedString(sequence, lineLength, block, "\n");
  }

  /**
   * Formats the sequence in the byte array with the given width and block
   * length. Any non-digit or non-character is removed
   *
   * @param sequence the sequence as a byte array
   * @param lineLength the length of each line
   * @param block the block size- each block is appended by a space
   * @param lineDelim string used to delimit lines
   */
  public static String getSequenceFormattedString(byte[] sequence, int lineLength, int block, String lineDelim) {
    StringBuffer sb = new StringBuffer();
    if (lineDelim == null) {
      lineDelim = "\n";
    }
    int count = 0;
    for (int i = 0; i < sequence.length; i++) {
      if (Character.isLetterOrDigit((char) sequence[i])) {
        if (lineLength > 0 && count % lineLength == 0 && count > 0) {
          sb.append(lineDelim);
        } else if (count > 0 && block > 0 && count % block == 0) {
          sb.append(' ');
        }
        sb.append((char) sequence[i]);
        count++;
      }
    }
    return sb.toString();
  }

  /**
   * Checks the String and returns one that has reserved XML characters replaed
   * with the equivalent character entity
   *
   * @param source
   * @return
   */
  public static String getValidXML(String source) {
    if (source == null) {
      return null;
    }
    String newString = replaceAll(source, '<', "&lt;");
    newString = replaceAll(newString, '>', "&gt;");
    newString = replaceAll(newString, '&', "&amp;");
    return newString;
  }

  /**
   * Returns the first non-null String in the array
   *
   * @param s String[]
   * @return String
   */
  public static String findFirstNotNull(String[] s) {
    for (String ss : s) {
      if (ss != null) {
        return ss;
      }
    }
    return null;
  }


  /**
   * Returns the first null String in the array, returning its index.
   * -1 if all non-null
   *
   * @param s String[]
   * @return index of first null String, -1 otherwise
   */
  public static int findFirstNull(String[] s) {
    for (int i=0; i< s.length; i++) {
      if (s[i] == null) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns a CSV String with any Excel importing issues worked-around
   *
   * @param cells the items to be in teh cells. A toString() is performed on
   * each
   * @return
   */
  public static String getOutputExcelCSV(Object[] cells) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < cells.length; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(getOutputExcelItem((cells[i] == null ? "" : cells[i].toString())));
    }
    return sb.toString();
  }

  /**
   * Any formatting which must be done to ensure no improper formatting is done
   * when importing into excel.
   */
  public static String getOutputExcelItem(String value) {
    StringBuffer sb = new StringBuffer();
    if (value != null) {
      char[] array = value.toCharArray();
      for (int i = 0; i < array.length; i++) {
        switch (array[i]) {
          case (','): //Ensure any numeric values separated by a comma has a space intervening
            if (i == 0 || i == array.length - 1) {
              sb.append(array[i]);
            } else {
              if (Character.isDigit(array[i - 1]) && Character.isDigit(array[i + 1])) {
                sb.append(array[i]);
                sb.append(' ');
              } else {
                sb.append(array[i]);
              }
            }
            break;
          case ('"'):
            sb.append('\'');
            break;
          default:
            sb.append(array[i]);
            break;
        }
      }
    } else {
      sb.append(' ');
    }
    sb.insert(0, '"');
    sb.append('"');
    return sb.toString();
  }

  /**
   * A null-safe trim that returns a trimmed String or null if s is null
   *
   * @param s String
   * @return String
   */
  public static String trim(String s) {
    return (s == null ? null : s.trim());
  }

  /**
   * Removes white space from the left side of the String. This method is used
   * to trim
   * {@link Character#isSpace(char) whitespace} from the beginning of a string;
   * in fact, it trims all ASCII control characters as well.
   *
   * @return s string, with white space removed from the front.
   */
  public static String trimLeft(String s) {
    int len = s.length();
    int st = 0;
    int off = 0; /*
     * avoid getfield opcode
     */
    char[] val = s.toCharArray(); /*
     * avoid getfield opcode
     */

    while ((st < len) && (val[off + st] <= ' ')) {
      st++;
    }
    return ((st > 0) || (len < s.length())) ? s.substring(st, len) : s;
  }

  /**
   * Removes white space from the right side of the String. This method is used
   * to trim
   * {@link Character#isSpace(char) whitespace} from the end of a string; in
   * fact, it trims all ASCII control characters as well.
   *
   * @return s string, with white space removed from the end.
   */
  public static String trimRight(String s) {
    int len = s.length();
    int st = 0;
    int off = 0; /*
     * avoid getfield opcode
     */
    char[] val = s.toCharArray(); /*
     * avoid getfield opcode
     */

    while ((st < len) && (val[off + len - 1] <= ' ')) {
      len--;
    }
    return ((st > 0) || (len < s.length())) ? s.substring(st, len) : s;
  }

  /**
   * Sets the length of the String either by truncating or adding characters by
   * using StringBuffer.setLength() The difference with the method and
   * StringBuffer.setLength() is this method allows you to choose the appending
   * character. If newLength<String.length, this operates exactly as
   * StringBuffer.setLength()
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *

   *
   * @see java.lang.StringBuffer
   * @param source the String to modify
   * @param newLength the new length of the String
   * @param appendChar the character used to pad the String if
   * newLength>String.length
   */
  public static String setLength(String source, int newLength, char appendChar) {
    StringBuffer sb = new StringBuffer(source);
    sb.setLength(newLength);
    String s = sb.toString();
    return s.replace('\0', appendChar);
  }

  /**
   * Converts a String to Title Case- all words begin with an upper case letter
   * and the rest lower case
   *
   * @param s
   * @return
   */
  public static String toTitleCase(String s) {
    StringTokenizer st = new StringTokenizer(s);
    StringBuffer target = new StringBuffer();
    while (st.hasMoreTokens()) {
      String token = st.nextToken();
      if (token.length() == 0) {
        target.append(token.toUpperCase());
      } else {
        target.append(token.substring(0, 1).toUpperCase());
        target.append(token.substring(1).toLowerCase());
      }
      target.append(" ");
    }
    return target.toString();
  }

  /**
   * Splits the given String into pieces of the given length.
   *
   * @param s
   * @param len
   * @return
   */
  public static String[] splitByLength(String s, int len) {
    ArrayList splits = new ArrayList();
    String currS = s;
    while (currS.length() > len) {
      splits.add(currS.substring(0, len));
      currS = currS.substring(len);
    }
    if (currS.length() > 0) {
      splits.add(currS);
    }
    return (String[]) splits.toArray(new String[0]);
  }

  /**
   * Adds a delimiter to a String at specific intervals
   *
   * @param s
   * @param length
   * @param delim
   * @return
   */
  public static String wrap(String s, int length, String delim) {
    StringBuffer sb = new StringBuffer();
    char[] chars = s.toCharArray();
    int lineCount = 0;
    for (int i = 0; i < chars.length; i++) {
      if (lineCount >= length && Character.isSpaceChar(chars[i])) {
        sb.append(delim);
        lineCount = 0;
      } else {
        sb.append(chars[i]);
        lineCount++;
      }
    }
    return sb.toString();
  }

  /**
   * Performs word wrapping. Returns the input string with long lines of text
   * cut (between words) for readability.
   *
   * @param s text to be word-wrapped
   * @param length number of characters in a line
   */
  public static String wordWrap(String s, int length, boolean splitOnHyphen) {
    s = s.trim();
    if (splitOnHyphen) {
      s = s.replaceFirst("^-", "");
    }
    String newline = "\n";

    //If Small Enough Already, Return Original
    if (s.length() < length) {
      return s;
    }
    //If Next length Contains Newline, Split There
    if (s.substring(0, length).contains(newline)) {
      return s.substring(0, s.indexOf(newline)).trim() + newline
              + wordWrap(s.substring(s.indexOf("\n") + 1), length, splitOnHyphen);
    }
    //Otherwise, Split Along Nearest Previous Space/Tab/Dash

    int spaceIndex = Math.max(s.lastIndexOf(" ", length), s.lastIndexOf("\t", length));

    if (splitOnHyphen) {
      spaceIndex = Math.max(spaceIndex, s.lastIndexOf("-", length));
    }

    //If No Nearest Space, Split At length
    if (spaceIndex == -1) {
      spaceIndex = length;
    }
    //Split
    return s.substring(0, spaceIndex).trim() + newline + wordWrap(s.substring(spaceIndex), length, splitOnHyphen);
  }

  /**
   * Returned a time difference as <hours> hr <minutes> min <seconds> s
   *
   * @param timeDiff time in seconds
   * @return
   */
  public static String getElapsedTimeString(double timeSecondsDiff) {
    return getElapsedTimeString(timeSecondsDiff, "%i hr %i m %.1f s");
  }

  /**
   * Returns the elapsed time in seconds as a String using the given format
   * specifier String. The format specifier String is define in PrintfFormat.
   * The number of format specifiers in the format specifier String dictates the
   * output content: 1) Only the total seconds 2) Total minutes, seconds 3)
   * Total hours, minutes, seconds
   *
   * @param timeSecondsDiff
   * @param formatSpec
   * @return
   */
  public static String getElapsedTimeString(double timeSecondsDiff, String formatSpec) {
    double timeDiff = timeSecondsDiff;
    PrintfFormat format = new PrintfFormat(formatSpec);
    char[] formatSpecifiers = format.getFormatSpecifiers();
    Integer hours;
    Integer minutes;
    Double seconds;

    String[] times;
    switch (formatSpecifiers.length) {
      case (1):
        times = new String[]{
          new Double(timeDiff) + ""
        };
        break;
      case (2):
        minutes = new Integer((int) (timeDiff / 60));
        timeDiff = timeDiff - minutes.intValue() * 60;
        seconds = new Double(timeDiff);
        times = new String[]{
          new Double(minutes.doubleValue()) + "",
          new Double(timeDiff) + ""
        };
        break;
      case (3):
        hours = new Integer((int) (timeDiff / 3600));
        timeDiff = timeDiff - hours.intValue() * 3600;
        minutes = new Integer((int) (timeDiff / 60));
        timeDiff = timeDiff - minutes.intValue() * 60;
        seconds = new Double(timeDiff);
        times = new String[]{
          new Double(hours.doubleValue()) + "",
          new Double(minutes.doubleValue()) + "",
          new Double(timeDiff) + ""
        };
        break;
      default:
        throw new IllegalArgumentException("Invalid number of format specifiers");
    }
    return format.cprintf(times);
  }

  /**
   * Returns a properly encoded GET URI for the give URL and parameters
   *
   * @param url String
   * @param queryParams Map
   * @return String
   */
  public static String toURI(String url, Map<String, String> queryParams) {
    if (queryParams == null || queryParams.size() == 0) {
      return url;
    }
    StringBuffer queryString = new StringBuffer();
    for (String paramName : queryParams.keySet()) {
      if (queryString.length() > 0) {
        queryString.append("&");
      }
      if (hasLength(queryParams.get(paramName))) {
        queryString.append(paramName + "=" + escape(queryParams.get(paramName)));
      } else {
        queryString.append(paramName);
      }
    }
    if (queryString.length() == 0) {
      return url;
    }
    return url + "?" + queryString;
  }

  /**
   * Determines if the provided String is true. If s is null, it returns false
   *
   * @param s
   * @return
   */
  public static boolean toBoolean(String s) {
    if (s == null) {
      return false;
    }
    return Boolean.valueOf(s.toLowerCase()).booleanValue();
  }

  /**
   * Converts a value to a double or NaN if not possible
   *
   * @param value String
   * @return double
   */
  public static double toDouble(String value) {
    return toDouble(value, Double.NaN);
  }

  /**
   * Converts a value to an Integer. Throws a NumberFormatException if not
   * possible.
   *
   * @param value String
   * @return double
   */
  public static int toInteger(String value) {
    double val = toDouble(value, Double.NaN);
    if (Double.isNaN(val)) {
      throw new NumberFormatException("Value is not a number. " + value);
    }
    return (int) val;
  }

  /**
   * Converts a value to an Integer. Throws a NumberFormatException if not
   * possible.
   *
   * @param value String
   * @return double
   */
  public static int toInteger(String value, int defaultValue) {
    if (!isAInteger(value)) {
      return defaultValue;
    }
    double val = toDouble(value, Double.NaN);
    if (Double.isNaN(val)) {
      return defaultValue;
    }
    return (int) val;
  }

  /**
   * Converts a value to a double or the defautl value if not possible
   *
   * @param value String
   * @param defaultValue double
   * @return double
   */
  public static double toDouble(String value, double defaultValue) {
    try {
      return new Double(value.toString()).doubleValue();
    } catch (Exception e) {
    }
    return defaultValue;
  }

  /**
   * Returns a Number if the given string is a parsable number otherwise,
   * returns the String. Try a new Integer, then new Double Returns null if s is
   * null;
   *
   * @param s string
   * @return Number if the string is a parsable number, s otherwise
   */
  public static Object toNumber(String s) {
    try {
      return new Double(s);
    } catch (NumberFormatException e) {
    }

    return s;
  }

  /**
   * Returns a List of Integers given a String in the format x-y, including x
   * and y
   *
   * @param s string
   */
  public static List<Integer> toIntegersFromRange(String s) {
    List<Integer> values = new ArrayList<Integer>();
    try {
      String[] ends = s.split("-");
      int start = new Integer(ends[0].trim());
      int finish = new Integer(ends[1].trim());
      if (start > finish) {
        int start1 = start;
        start = finish;
        finish = start1;
      }
      for (int i = start; i <= finish; i++) {
        values.add(i);
      }
    } catch (Exception e) {
      values.clear();
    }
    return values;
  }

  public static String getValidANumber(String aNumber) {
    Pattern aNumberPattern = Pattern.compile("^A-?[0-9]+(\\.[0-9]+)?");
    Pattern aNumberStartPattern = Pattern.compile("^A-");
    Pattern aNumberEndSaltPattern = Pattern.compile(".*(\\.[0-9]+)$");
    Pattern aNumberEndNoSaltPattern = Pattern.compile("\\.$/");

    if (aNumber.length() == 0 || !aNumberPattern.matcher(aNumber).matches()) {
      return null;
    }

    if (!aNumberStartPattern.matcher(aNumber).lookingAt()) {
      aNumber = aNumber.replaceAll("^A", "A-");
    }
    if (!aNumberEndSaltPattern.matcher(aNumber).lookingAt()) {
      if (aNumberEndNoSaltPattern.matcher(aNumber).lookingAt()) {
        aNumber = aNumber + "0";
      } else {
        aNumber = aNumber + ".0";
      }
    }
    return aNumber;
  }

  /**
   * Copies the contents of a file to a String
   */
  public static String readToString(File input) throws IOException {
    FileReader reader = null;
    StringWriter writer = new StringWriter();
    try {
      reader = new FileReader(input);
      final int BUFSIZ = 1024;
      char buf[] = new char[BUFSIZ];
      int len = 0;
      while ((len = reader.read(buf)) > 0) {
        writer.write(buf, 0, len);
      }
    } finally {
      if (reader != null) {
        reader.close();
      }
      if (writer != null) {
        writer.close();
      }
    }
    return writer.toString();
  }

  /**
   * Copies the contents of a Reader to a String
   */
  public static String readToString(Reader reader) throws IOException {
    StringWriter writer = new StringWriter();
    try {
      final int BUFSIZ = 1024;
      char buf[] = new char[BUFSIZ];
      int len = 0;
      while ((len = reader.read(buf)) > 0) {
        writer.write(buf, 0, len);
      }
    } finally {
      if (reader != null) {
        reader.close();
      }
      if (writer != null) {
        writer.close();
      }
    }
    return writer.toString();
  }

  /**
   * Copies the contents of a file to a String using the given character set
   *
   * @param input File
   * @param charset The name of a supported
   *         {@link java.nio.charset.Charset </code>charset<code>}
   * @throws IOException
   * @return String
   */
  public static String readToString(File input, String charset) throws IOException {
    Reader reader = null;
    StringWriter writer = new StringWriter();
    try {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), charset));
      final int BUFSIZ = 1024;
      char buf[] = new char[BUFSIZ];
      int len = 0;
      while ((len = reader.read(buf)) > 0) {
        writer.write(buf, 0, len);
      }
    } finally {
      if (reader != null) {
        reader.close();
      }
      if (writer != null) {
        writer.close();
      }
    }
    return writer.toString();
  }

  /**
   * Copies the contents of a Reader to a String
   */
  public static String readToString(InputStream in) throws IOException {
    StringWriter writer = new StringWriter();
    try {
      int c;
      while ((c = in.read()) > 0) {
        writer.write(c);
      }
    } finally {
      if (in != null) {
        in.close();
      }
      if (writer != null) {
        writer.close();
      }
    }
    return writer.toString();
  }

  /**
   * Copies the contents of a URL to a String
   */
  public static String readToString(URL url) throws IOException {
    return readToString(url.openStream());
  }

  /**
   * Returns the Objects toString if not null, ifNullValue otherwise.
   *
   * @param obj Object
   * @param ifNullValue String
   * @return String
   */
  public static String toString(Object obj, String ifNullValue) {
    return (obj == null ? ifNullValue : obj.toString());
  }

  /**
   * Converts a String which has had its characters encoded with the %xx
   * hexadecimal form back to its ASCII character set equivalents. Useful to
   * convert Strings encoded by the javascript escape function
   *
   * @param s %xx hexadecimal encoded String
   * @return String in ASCII form
   */
  public static String unescape(String s) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '%' && i < s.length() - 2) {
        String hex = s.substring(i + 1, i + 3);
        int ascii = Integer.parseInt(hex, 16);
        sb.append((char) ascii);
        i += 2;
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
   * Converts a String which has any URL characters that require encoding with
   * hexadecimal %xx format.
   *
   * @param s String to encode
   * @return a String with hexadecimal encodings
   */
  public static String escape(String s) {
    return ExtURL.urlEncode(s);
  }

  public String safeString(String s) {
    StringBuffer sb = new StringBuffer();
    for (char ch : s.toCharArray()) {
      if ((int) ch > 127) {
        switch ((int) ch) {
          case 181: //micro
            sb.append("u");
            break;
          default:
            sb.append("-");
            break;
        }
      } else {
        sb.append(ch);
      }
    }
    return sb.toString();
  }

  /**
   * Removes unwanted characters from a potential filename including replacing
   * spaces with underscores
   *
   * @param s String
   * @return String
   */
  public static String getSafeFileName(String s) {
    String safe = s.trim();
    // replace spaces with underscore
    safe = safe.replaceAll(" ", "_");

    // replace any 'double spaces' with singles
    safe = safe.replaceAll("_{2,}", "_");

    // trim out illegal characters
    safe = safe.replaceAll("^[\\W]+", "");
    safe = safe.replaceAll("[\\W]+$", "");
    safe = safe.replaceAll("[\\/\\?<>\\\\:\\*\\|\"']", "");

    return safe;
  }

  /**
   * Removes unwanted characters from a potential filename including replacing
   * spaces with underscores
   *
   * @param s String
   * @return String
   */
  public static String getSafeFileNameAllowSpaces(String s) {
    String safe = s.trim();

    // replace any 'double spaces' with singles
    safe = safe.replaceAll("\\s{2,}", " ");
    // trim out illegal characters
    safe = safe.replaceAll("^[\\W]+", "");
    safe = safe.replaceAll("[\\W]+$", "");
    safe = safe.replaceAll("[\\/\\?<>\\\\:\\*\\|\"']", "");

    return safe;
  }

  /**
   * Returns a Map with the parts of the URI. The Map contains the following
   * elements source, protocol, authority, domain, port, path, directoryPath,
   * fileName, query, anchor
   */
  public static Map<String, String> parseURI(String uri) {
    return ExtURL.parseURI(uri);
  }

  /**
   * Creates a String based on a tokenized template by passing in an arbitrary
   * number of arguments to replace the tokens. Each token must be unique, and
   * must increment in the format {0}, {1}, which correspond to elements in the
   * replacements array
   *
   * @param template String
   * @param replacements String[]
   * @return String
   */
  public static String applyTemplate(String template, String[] replacements) {
    if (template == null) {
      return null;
    }
    if (replacements == null) {
      return template;
    }
    return applyTemplate(template, Arrays.asList(replacements));
  }

  /**
   * Creates a String based on a tokenized template by passing in an arbitrary
   * number of arguments to replace the tokens. Each token must be unique, and
   * must increment in the format {0}, {1}, which correspond to elements in the
   * replacements List
   *
   * @param template String
   * @param replacements List<String>
   * @return String
   */
  public static String applyTemplate(String template, List<String> replacements) {
    if (template == null) {
      return null;
    }
    if (replacements == null) {
      return template;
    }
    for (int i = 0; i < Math.min(replacements.size(), 100); i++) {
      int indx = i + 1;
      Pattern p = Pattern.compile("\\{" + indx + "\\}");
      Matcher m = p.matcher(template);
      if (m.find()) {
        template = m.replaceAll(replacements.get(i));
      }
    }
    return template;
  }

  public static void main(String[] args) throws Exception {


  }
}
