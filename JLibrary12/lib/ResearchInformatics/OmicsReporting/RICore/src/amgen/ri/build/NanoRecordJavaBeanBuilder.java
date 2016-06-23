
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package amgen.ri.build;

import amgen.ri.csv.CSVReader;
import amgen.ri.util.ExtString;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author jemcdowe
 */
public class NanoRecordJavaBeanBuilder extends AbstractNanoBuilder {
  public NanoRecordJavaBeanBuilder() {
  }

  protected void doBuild(Reader inputReader) throws IOException {
    CSVReader reader = new CSVReader(inputReader, '\t');

    while (reader.readRecord()) {
      String[] values = reader.getValues();
      if (values.length >= 2 && ExtString.hasTrimmedLength(values[0]) && ExtString.hasTrimmedLength(values[1])) {
        String paramName = values[0];
        String paramType = values[1];
        String comments = (values.length >= 3 ? values[2] : null);
        String casedParamName = Character.toUpperCase(paramName.charAt(0)) + (paramName.length() > 1 ? paramName.substring(1) : "");

        if (processLine(paramName, paramType, comments)) {
          String getterTmpt = null;
          String setterTmpt = null;

          String typeName = null;

          switch (Types.getType(paramType)) {
            case BOOLEAN:
              typeName = "boolean";
              getterTmpt = "public %s get%s() {\n    return getBoolean(\"%s\");\n}";
              setterTmpt = "public void set%s(%s %s) {\n    add(\"%s\", %s);\n}";
              break;
            case INT:
              typeName = "int";
              getterTmpt = "public %s get%s() {\n    return getNumber(\"%s\").intValue();\n}";
              setterTmpt = "public void set%s(%s %s) {\n    add(\"%s\", %s);\n}";
              break;
            case DOUBLE:
              typeName = "double";
              getterTmpt = "public %s get%s() {\n    return getNumber(\"%s\").doubleValue();\n}";
              setterTmpt = "public void set%s(%s %s) {\n    add(\"%s\", %s);\n}";
              break;
            case STRING:
              typeName = "String";
              getterTmpt = "public %s get%s() {\n    return getString(\"%s\");\n}";
              setterTmpt = "public void set%s(%s %s) {\n    add(\"%s\", %s);\n}";
              break;
            case DATE:
              typeName = "Date";
              String imp = String.format(importTmpt, "java.util.Date");
              if (!imports.contains(imp)) {
                imports.add(imp);
              }
              getterTmpt = "public %s get%s() {\n    return getDate(\"%s\");\n}";
              setterTmpt = "public void set%s(%s %s) {\n    add(\"%s\", %s);\n}";
              break;
            default:
              typeName = paramType;
              getterTmpt = "public %s get%s() {\n    return get(\"%s\");\n}";
              setterTmpt = "public void set%s(%s %s) {\n    add(\"%s\", %s);\n}";
              break;
          }

          if (typeName != null) {
            declarations.add("//" + paramName + " [" + typeName + "] " + (values.length >= 3 ? values[2].replaceAll("[\\r\\n]+", " ") : ""));
            typeNames.put(paramName, typeName);
            getters.put(paramName, String.format(getterTmpt, typeName, casedParamName, paramName));
            setters.put(paramName, String.format(setterTmpt, casedParamName, typeName, paramName, paramName, paramName));
          }
        }
      }
    }
    baseClassName= "amgen.ri.json.record.AbstractRecord";

  }
}
