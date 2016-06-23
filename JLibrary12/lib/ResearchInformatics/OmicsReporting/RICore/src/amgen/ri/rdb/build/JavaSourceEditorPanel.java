package amgen.ri.rdb.build;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

/**
 * <p> </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class JavaSourceEditorPanel extends JPanel {
    JEditorPane editor;
    boolean modified;
    File lastSavedFile;
    RdbDataBuilder rdbDataBuilder;

    public JavaSourceEditorPanel() {
        super(new BorderLayout());
        editor = new JEditorPane();
        editor.setCaretColor(Color.white);
        JavaEditorKit kit = new JavaEditorKit();
        editor.setEditorKitForContentType("text/java", kit);
        editor.setContentType("text/java");
        editor.setBackground(Color.white);
        editor.setFont(new Font("Courier", 0, 12));
        editor.setEditable(true);

        // PENDING(prinz) This should have a customizer and
        // be serialized.  This is a bogus initialization.
        JavaContext styles = kit.getStylePreferences();
        Style s;
        s = styles.getStyleForScanValue(Token.COMMENT.getScanValue());
        StyleConstants.setForeground(s, Color.red);
        s = styles.getStyleForScanValue(Token.STRINGVAL.getScanValue());
        StyleConstants.setForeground(s, Color.blue.darker());
        Color keyword = new Color(102, 102, 255);
        for (int code = 70; code <= 130; code++) {
            s = styles.getStyleForScanValue(code);
            if (s != null) {
                StyleConstants.setForeground(s, keyword);
            }
        }
        add(editor, BorderLayout.CENTER);
    }

    public void read(File sourceFile) throws IOException {
        editor.read(new FileReader(sourceFile), sourceFile);

        BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
        StringBuffer sb = new StringBuffer();
        String line;
        while ( (line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        reader.close();
        setContent(sb.toString());
    }

    public void setCaretPosition(int position) {
        editor.setCaretPosition(position);
    }

    public String getContent() {
        return editor.getText();
    }

    public void setContent(String content) throws IOException {
        editor.setText(content);
        editor.setCaretColor(Color.red);
        modified = true;
    }

    public void setContentFromInput(InputControlsPanel input, boolean resetClassNameFromTable) throws Exception {
        try {
            String packageName = input.getPackageBox().getText();
            String className = input.getClassNameBox().getText();
            if (resetClassNameFromTable) {
                className = null;
            }
            String schema = input.getSchemaBox().getText();
            String table = input.getTableBoxValue();
            String author = input.getAuthorBox().getText();
            boolean saveable = input.getSaveableButton().isSelected();
            boolean removeable = input.getRemoveableButton().isSelected();
            boolean addGetters = input.getGettersButton().isSelected();
            boolean addSetters = input.getSettersButton().isSelected();
            String description = input.getDescriptionBox().getText();
            if (description == null || description.length() == 0) {
                input.getDescriptionBox().setText("RDBData wrapper class for " + table);
                description = input.getDescriptionBox().getText();
            }

            if (!checkInput(new String[] {packageName, schema, table, author, description})) {
                return;
            }
            RdbDataParameter[] parameters = input.getParameters();
            if (parameters == null || parameters.length == 0) {
                return;
            }
            rdbDataBuilder = new RdbDataBuilder(RdbDataClassBuilder.CONNECTION_POOL);
            String classContents = rdbDataBuilder.buildClass(className, packageName, parameters, schema,
                table, author, addGetters, addSetters, saveable, removeable, description);
            setContent("\n" + classContents);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Returns the relative path derived from the package for the last built source.
     * (e.g. amgen.ri.util.Data => amgen/ri/util
     * setContentFromInput() must be called previous to calling this or null will be returned
     * @return
     */
    public String getPackagePath() {
        if (rdbDataBuilder == null) {
            return null;
        }
        return rdbDataBuilder.getPackagePath();
    }

    /**
     * Returns the package for the last built source.
     * (e.g. amgen.ri.util.Data => amgen.ri.util
     * setContentFromInput() must be called previous to calling this or null will be returned
     * @return
     */
    public String getPackageName() {
        String packagePath = getPackagePath();
        if (packagePath == null) {
            return null;
        }
        return packagePath.replaceAll("\\/", ".");
    }

    /**
     * Returns the source File for the last built source.
     * (e.g. amgen.ri.util.Data => Data.java
     * setContentFromInput() must be called previous to calling this or null will be returned
     * @return
     */
    public String getSourceName() {
        if (rdbDataBuilder == null) {
            return null;
        }
        return rdbDataBuilder.getSourceName();
    }

    /**
     * Returns the class name for the last built source.
     * (e.g. amgen.ri.util.Data => Data.java
     * setContentFromInput() must be called previous to calling this or null will be returned
     * @return
     */
    public String getClassName() {
        if (rdbDataBuilder == null) {
            return null;
        }
        return rdbDataBuilder.getClassName();
    }

    public RdbDataParameter[] setParameters(InputControlsPanel input) throws Exception {
        try {
            String schema = input.getSchemaBox().getText();
            String table = input.getTableBoxValue();

            if (!checkInput(new String[] {schema, table})) {
                return null;
            }
            RdbDataBuilder rdbDataBuilder = new RdbDataBuilder(RdbDataClassBuilder.CONNECTION_POOL);
            return rdbDataBuilder.getParameters(schema, table);
        } catch (Exception e) {
            throw e;
        }
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public boolean isModified() {
        return modified;
    }

    public void setLastSavedFile(File lastSavedFile) {
        this.lastSavedFile = lastSavedFile;
    }

    public File getLastSavedFile() {
        return lastSavedFile;
    }

    public static boolean checkInput(String[] inputs) {
        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i] == null || inputs[i].trim().length() == 0) {
                return false;
            }
        }
        return true;
    }

}
