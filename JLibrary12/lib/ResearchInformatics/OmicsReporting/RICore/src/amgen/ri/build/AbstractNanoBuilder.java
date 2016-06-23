/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package amgen.ri.build;

import amgen.ri.csv.CSVReader;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.*;
import java.util.*;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jemcdowe
 */
public abstract class AbstractNanoBuilder {

    enum Types {

        INT, DOUBLE, STRING, DATE, BOOLEAN, UNKNOWN;

        public static Types getType(String s) {
            try {
                if (s.equalsIgnoreCase("integer")) {
                    return INT;
                }
                return Types.valueOf(s.toUpperCase());
            } catch (Exception e) {
                return UNKNOWN;
            }
        }
    };
    protected String EDITOR_EXE = "C:/Program Files/NetBeans 7.4/bin/netbeans64.exe";
    protected String className;
    protected String baseClassName;
    protected String packageName;
    protected String localClassName;
    protected String classComments;
    protected List<String> imports;
    protected List<String> declarations;
    protected Map<String, String> getters;
    protected Map<String, String> setters;
    protected Map<String, String> typeNames;
    protected String getterTmpt = "public %s get%s() {\n    return %s;\n}";
    protected String setterTmpt = "public void set%s(%s %s) {\n    this.%s= %s;\n}";
    protected String contructorRecordSetterTmpt = "add(\"%s\", %s);\n";
    protected String contructorSetterTmpt = "this.%s= %s;\n";

    protected String getterCommentTmpl = "/**\n* Get value for %s\n*/";
    protected String setterCommentTmpl = "/**\n* Set value for %s\n*/";
    protected String importTmpt = "import %s;";
    protected String packageTmpl = "package %s;\n";
    protected String classTmpl = "public class %s {\n";
    protected String classExtendsTmpl = "public class %s extends %s {\n";
    protected String classConstructorDeclarationTmpl = "public %s() {\n}\n\n";

    protected AbstractNanoBuilder() {
    }

    public void build(File input, File output) throws IOException {
        className = null;
        packageName = null;
        localClassName = null;
        classComments = null;

        declarations = new ArrayList<String>();
        imports = new ArrayList<String>();
        typeNames = new LinkedHashMap<String, String>();
        getters = new LinkedHashMap<String, String>();
        setters = new LinkedHashMap<String, String>();

        Reader inputReader = null;

        if (input != null) {
            inputReader = new FileReader(input);
        } else {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable clipData = clipboard.getContents(this);
            String clipText = null;
            try {
                clipText = (String) (clipData.getTransferData(DataFlavor.stringFlavor));
                clipText = clipText.trim();
            } catch (Exception ee) {
                clipText = null;
            }
            if (clipText == null) {
                return;
            }
            inputReader = new StringReader(clipText);
        }
        try {
            doBuild(inputReader);

            packageName = className.substring(0, className.lastIndexOf('.'));
            localClassName = className.substring(className.lastIndexOf('.') + 1);

            StringBuilder sb = new StringBuilder();

            if (className != null) {
                sb.append(String.format(packageTmpl, packageName));
            }
            sb.append("\n");

            for (String imp : imports) {
                sb.append(imp);
                sb.append("\n");
            }
            if (baseClassName != null) {
                sb.append(String.format(importTmpt, baseClassName));
                sb.append("\n");
            }
            sb.append("\n");

            if (className != null) {
                if (baseClassName != null) {
                    String baseClassNamePackageName = baseClassName.substring(0, baseClassName.lastIndexOf('.'));
                    String baseClassNameLocalClassName = baseClassName.substring(baseClassName.lastIndexOf('.') + 1);
                    sb.append(String.format(classExtendsTmpl, localClassName, baseClassNameLocalClassName));
                } else {
                    sb.append(String.format(classTmpl, localClassName));
                }
            }

            for (String declaration : declarations) {
                sb.append(declaration);
                sb.append("\n");
            }
            sb.append("\n");

            if (className != null) {
                sb.append(String.format(classConstructorDeclarationTmpl, localClassName));

                sb.append("public " + localClassName + "(");
                StringBuilder pBuilder = new StringBuilder();
                for (String paramName : typeNames.keySet()) {
                    String typeName = typeNames.get(paramName);
                    if (pBuilder.length() > 0) {
                        pBuilder.append(", ");
                    }
                    pBuilder.append(typeName + " " + paramName);
                }
                sb.append(pBuilder + ") {\n");
                pBuilder = new StringBuilder();
                for (String paramName : typeNames.keySet()) {
                    if (this instanceof NanoRecordJavaBeanBuilder) {
                        pBuilder.append(String.format(contructorRecordSetterTmpt, paramName, paramName));
                    } else {
                        pBuilder.append(String.format(contructorSetterTmpt, paramName, paramName));
                    }
                }
                sb.append(pBuilder + "}\n");
            }

            for (String getterParam : getters.keySet()) {
                String getter = getters.get(getterParam);
                sb.append(String.format(getterCommentTmpl, getterParam));
                sb.append("\n");

                sb.append(getter);
                sb.append("\n");
            }
            sb.append("\n");

            for (String setterParam : setters.keySet()) {
                String setter = setters.get(setterParam);
                sb.append(String.format(setterCommentTmpl, setterParam));
                sb.append("\n");

                sb.append(setter);
                sb.append("\n");
            }
            if (className != null) {
                sb.append("}");
            }
            //System.out.println(sb);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection stringSelection = new StringSelection(sb.toString());
            clipboard.setContents(stringSelection, null);

            if (output == null && localClassName != null) {
                File f = File.createTempFile("temp", ".tmp");
                f.deleteOnExit();
                output = new File(f.getParentFile(), localClassName + ".java");
            }

            if (output != null) {
                PrintWriter writer = new PrintWriter(output);
                writer.print(sb);
                writer.close();
                Runtime.getRuntime().exec(EDITOR_EXE + " " + output);
            }
        } finally {
            try {
                inputReader.close();
            } catch (Exception e) {
            }
        }

    }

    protected boolean processLine(String nameField, String paramField, String commentField) {
        if (nameField.startsWith("#")) {
            return false;
        }
        if (nameField.equalsIgnoreCase("class")) {
            className = paramField;
            classComments = commentField;
            return false;
        } else if (nameField.equalsIgnoreCase("import")) {
            String imp = String.format(importTmpt, paramField);
            if (!imports.contains(imp)) {
                imports.add(imp);
            }
            return false;
        } else if (nameField.equalsIgnoreCase("build")) {
            if (StringUtils.endsWithIgnoreCase(paramField, "record")) {
                if (!(this instanceof NanoRecordJavaBeanBuilder)) {
                    throw new IllegalArgumentException("Wrong builder");
                }
            }
            return false;
        }
        return true;
    }

    protected abstract void doBuild(Reader inputReader) throws IOException;

    public static void main(String[] args) throws IOException {
        if ((args != null && args.length > 0) && args[0].toLowerCase().indexOf("record") > -1) {
            new NanoRecordJavaBeanBuilder().build(null, null);
        } else {
            try {
                new NanoJavaBeanBuilder().build(null, null);
            } catch (IllegalArgumentException e) {
                new NanoRecordJavaBeanBuilder().build(null, null);
            }
        }
    }
}
