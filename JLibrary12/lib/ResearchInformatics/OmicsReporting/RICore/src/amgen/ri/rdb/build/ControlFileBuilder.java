package amgen.ri.rdb.build;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;

import amgen.ri.rdb.Register;

/**
 *
 */
public class ControlFileBuilder extends JFrame implements ActionListener, ItemListener {
    private static final String TITLE = "ControlFile Builder";
    JScrollPane sourcePanelScroller;
    ControlFileViewer sourcePanel;
    File currentDirectory;
    InputControlFilePanel inputControlFilePanel;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {}
        ControlFileBuilder frame = new ControlFileBuilder();
        frame.pack();
        frame.setSize(1000, 800);
        frame.setVisible(true);
    }

    public ControlFileBuilder() {
        super(TITLE);
        setupUI();
    }

    public void setupUI() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitRequest();
            }
        });
        try {
            sourcePanel = new ControlFileViewer(this);
            sourcePanelScroller = new JScrollPane();
            JViewport vp = sourcePanelScroller.getViewport();
            vp.add(sourcePanel);
            vp.setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

            JSplitPane splitPane = new JSplitPane();
            splitPane.setDividerSize(2);

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add("Center", splitPane);
            splitPane.setRightComponent(sourcePanelScroller);
            splitPane.setLeftComponent(inputControlFilePanel = new InputControlFilePanel(this));
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void exitRequest() {
        if (sourcePanel.isModified()) {
            int results = JOptionPane.showConfirmDialog(null, "Save file?",
                "Source File Not Saved", JOptionPane.YES_NO_OPTION);
            switch (results) {
                case (JOptionPane.YES_OPTION):
                    saveFile();
                    break;
            }
        }
        System.exit(0);
    }

    /**
     * Invoked when an item has been selected or deselected.
     * The code written for this method performs the operations
     * that need to occur when an item is selected (or deselected).
     */
    public void itemStateChanged(ItemEvent event) {
        JComponent sourceComponent = (JComponent) event.getSource();
        InputControlFilePanel inputControlsPanel;
        if (sourceComponent instanceof InputControlFilePanel) {
            inputControlsPanel = (InputControlFilePanel) sourceComponent;
        } else if (sourceComponent.getParent() instanceof InputControlFilePanel) {
            inputControlsPanel = (InputControlFilePanel) sourceComponent.getParent();
        } else {
            return;
        }
        buildControlFile(inputControlsPanel, true);
    }

    public void actionPerformed(ActionEvent event) {
        InputControlFilePanel inputControlsPanel = null;
        if (! (event.getSource() instanceof JComponent)) {
            return;
        }
        JComponent sourceComponent = (JComponent) event.getSource();
        if (sourceComponent instanceof InputControlFilePanel) {
            inputControlsPanel = (InputControlFilePanel) sourceComponent;
        } else if (sourceComponent.getParent() instanceof InputControlFilePanel) {
            inputControlsPanel = (InputControlFilePanel) sourceComponent.getParent();
        } else {
            return;
        }
        if (event.getActionCommand().toLowerCase().startsWith("save")) {
            saveFile();
        } else if (event.getActionCommand().toLowerCase().startsWith("build")) {
            buildControlFile(inputControlsPanel, false);
        } else if (event.getActionCommand().toLowerCase().startsWith("tablechanged")) {
            inputControlsPanel.updateParameters();
            buildControlFile(inputControlsPanel, false);
        } else if (event.getActionCommand().toLowerCase().startsWith("exit")) {
            exitRequest();
        }
    }

    public void buildControlFile(InputControlFilePanel inputControlsPanel, boolean resetClassNameFromTable) {
        try {
            sourcePanel.build();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error Creating Class", JOptionPane.PLAIN_MESSAGE);
        }
        sourcePanel.setCaretPosition(0);
        JScrollBar bar = sourcePanelScroller.getVerticalScrollBar();
        bar.setValue(bar.getMinimum());
        resetTitleBar();
    }

    public void resetTitleBar() {
        boolean modified = sourcePanel.isModified();
        File lastSavedFile = sourcePanel.getLastSavedFile();

        if (modified) {
            if (lastSavedFile == null) {
                setTitle(TITLE + "- " + inputControlFilePanel.getTableBoxValue() + "*");
            } else {
                setTitle(TITLE + "- " + inputControlFilePanel.getTableBoxValue() + "* [" + lastSavedFile + "]");
            }
        } else {
            if (lastSavedFile == null) {
                setTitle(TITLE + "- " + inputControlFilePanel.getTableBoxValue());
            } else {
                setTitle(TITLE + "- " + inputControlFilePanel.getTableBoxValue() + " [" + lastSavedFile + "]");
            }
        }
    }

    public void saveFile() {
        try {
            File saveToFile = null;
            File lastSavedFile = sourcePanel.getLastSavedFile();
            JFileChooser chooser = new JFileChooser(currentDirectory);
            chooser.setDialogTitle("Save Control File");
            if (lastSavedFile == null) {
                lastSavedFile = new File(currentDirectory, inputControlFilePanel.getTableBoxValue() + ".ctl");
            }
            chooser.setSelectedFile(lastSavedFile);
            int returnVal = chooser.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                saveToFile = chooser.getSelectedFile();
                if (saveToFile == null) {
                    return;
                }
                if (saveToFile.exists()) {
                    int results = JOptionPane.showConfirmDialog(null, "Overwite file?", "File exists", JOptionPane.INFORMATION_MESSAGE);
                    if (results == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
            } else {
                return;
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(saveToFile));
            writer.write(sourcePanel.getContent());
            writer.close();
            currentDirectory = chooser.getSelectedFile();
            sourcePanel.setModified(false);
            sourcePanel.setLastSavedFile(saveToFile);
            resetTitleBar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unable to save file, " + e.getMessage(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
        }
    }

    class FileExtensionFilter extends javax.swing.filechooser.FileFilter {
        String[] extensions;

        public FileExtensionFilter(Object[] ext) {
            this.extensions = new String[ext.length];
            for (int i = 0; i < ext.length; i++) {
                extensions[i] = ext[i].toString();
            }
        }

        public FileExtensionFilter(String ext) {
            this(new String[] {ext});
        }

        public boolean accept(File file) {
            for (int i = 0; i < extensions.length; i++) {
                if (file.getName().endsWith(extensions[i])) {
                    return true;
                }
            }
            if (file.isDirectory()) {
                return true;
            }
            return false;
        }

        public String getDescription() {
            return "Java Source";
        }
    }
}

class ControlFileViewer extends JPanel {
    JEditorPane editor;
    boolean modified;
    File lastSavedFile;
    RdbDataBuilder rdbDataBuilder;
    ControlFileBuilder controlFileBuilder;

    public ControlFileViewer(ControlFileBuilder controlFileBuilder) {
        super(new BorderLayout());
        this.controlFileBuilder = controlFileBuilder;
        editor = new JEditorPane();
        editor.setCaretColor(Color.black);
        editor.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        DefaultEditorKit kit = new DefaultEditorKit();
        editor.setEditorKitForContentType("text/plain", kit);
        editor.setContentType("text/plain");
        editor.setBackground(Color.white);
        editor.setFont(new Font("Courier", 0, 12));
        editor.setEditable(true);
        add(editor, BorderLayout.CENTER);
    }

    public void setCaretPosition(int pos) {
        editor.setCaretPosition(pos);
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public boolean isModified() {
        return modified;
    }

    public File getLastSavedFile() {
        return lastSavedFile;
    }

    public void setLastSavedFile(File lastSavedFile) {
        this.lastSavedFile = lastSavedFile;
    }

    public static boolean checkInput(String[] inputs) {
        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i] == null || inputs[i].trim().length() == 0) {
                return false;
            }
        }
        return true;
    }

    public String getContent() {
        return editor.getText();
    }

    public void build() {
        RdbDataParameter[] parameters = controlFileBuilder.inputControlFilePanel.getParameters();
        if (parameters == null) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("LOAD DATA\n");
        sb.append("INFILE '-'\n");
        sb.append("APPEND\n");
        sb.append("INTO TABLE " + controlFileBuilder.inputControlFilePanel.getTableBoxValue());
        sb.append("\n");
        sb.append("FIELDS TERMINATED BY ");
        sb.append(controlFileBuilder.inputControlFilePanel.getDelimiterValue().startsWith("C") ? "','" : "X'9'");
        sb.append(" OPTIONALLY ENCLOSED BY '\"'\n");
        sb.append("TRAILING NULLCOLS\n");
        sb.append("(\n");
        boolean started = false;
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].includeParameter()) {
                int columnType = parameters[i].getColumnType();
                switch (columnType) {
                    case (Register.STRING):
                    case (Register.DOUBLE):
                    case (Register.LONG):
                    case (Register.INTEGER):
                        break;
                    case (Register.TIMESTAMP):
                    case (Register.CLOBDATA):
                    case (Register.BLOBDATA):
                        continue;
                }
                if (started) {
                    sb.append(",\n");
                }
                sb.append("\t" + parameters[i].getColumnName().toUpperCase() + "\t");
                sb.append(parameters[i].getControlFileType());
                if (columnType == Register.STRING) {
                    sb.append("(" + parameters[i].getColumnSize() + ")");
                }
                if (parameters[i].isNullable()) {
                    sb.append("\tNULLIF " + parameters[i].getColumnName().toUpperCase() + "=BLANKS");
                }
                started = true;
            }
        }
        sb.append("\n)\n");
        editor.setText(sb.toString());
        modified = true;
    }

}
