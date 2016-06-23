/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package amgen.ri.build;

import amgen.ri.csv.CSVReader;
import amgen.ri.util.ExtString;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 *
 * @author jemcdowe
 */
public class NanoJavaBeanBuilder extends AbstractNanoBuilder {
  public NanoJavaBeanBuilder() {
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
          if (values.length >= 3) {
            declarations.add("// " + values[2].replaceAll("[\\r\\n]+", " "));
          }

          String typeName = null;

          switch (Types.getType(paramType)) {
            case BOOLEAN:
              typeName = "boolean";
              break;
            case INT:
              typeName = "int";
              break;
            case DOUBLE:
              typeName = "double";
              break;
            case STRING:
              typeName = "String";
              break;
            case DATE:
              typeName = "Date";
              String imp = String.format(importTmpt, "java.util.Date");
              if (!imports.contains(imp)) {
                imports.add(imp);
              }
              break;
            default:
              typeName = paramType;
              break;
          }

          if (typeName != null) {
            declarations.add("private " + typeName + " " + paramName + ";");
            typeNames.put(paramName, typeName);
            getters.put(paramName, String.format(getterTmpt, typeName, casedParamName, paramName));
            setters.put(paramName, String.format(setterTmpt, casedParamName, typeName, paramName, paramName, paramName));
          }
        }
      }
    }
  }
}
