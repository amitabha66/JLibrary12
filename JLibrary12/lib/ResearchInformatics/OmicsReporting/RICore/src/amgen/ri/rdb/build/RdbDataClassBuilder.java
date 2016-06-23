package amgen.ri.rdb.build;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.UIManager;

/**
 *
 */
public class RdbDataClassBuilder extends JFrame implements ActionListener, ItemListener {
    private static final String TITLE = "RdbData Class Builder";
    public static final String CONNECTION_POOL = "builder";
    JScrollPane sourcePanelScroller;
    JavaSourceEditorPanel sourcePanel;
    File currentDirectory;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {}
        RdbDataClassBuilder frame = new RdbDataClassBuilder();
        frame.pack();
        frame.setSize(900, 800);
        frame.setVisible(true);

    }

    public RdbDataClassBuilder() {
        super(TITLE);
        setupUI();
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setupUI() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitRequest();
            }
        });
        try {
            sourcePanel = new JavaSourceEditorPanel();
            sourcePanelScroller = new JScrollPane();
            JViewport vp = sourcePanelScroller.getViewport();
            vp.add(sourcePanel);
            vp.setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

            JSplitPane splitPane = new JSplitPane();
            splitPane.setDividerSize(2);

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add("Center", splitPane);
            splitPane.setRightComponent(sourcePanelScroller);
            splitPane.setLeftComponent(new InputControlsPanel(this));
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
        InputControlsPanel inputControlsPanel;
        if (sourceComponent instanceof InputControlsPanel) {
            inputControlsPanel = (InputControlsPanel) sourceComponent;
        } else if (sourceComponent.getParent() instanceof InputControlsPanel) {
            inputControlsPanel = (InputControlsPanel) sourceComponent.getParent();
        } else {
            return;
        }
        updateParameters(event.getSource());
        buildClassFile(inputControlsPanel, true);
        inputControlsPanel.setClassNameBox(sourcePanel.getClassName());
    }

    public void actionPerformed(ActionEvent event) {
        InputControlsPanel inputControlsPanel = null;
        if (! (event.getSource() instanceof JComponent)) {
            return;
        }
        JComponent sourceComponent = (JComponent) event.getSource();
        if (sourceComponent instanceof InputControlsPanel) {
            inputControlsPanel = (InputControlsPanel) sourceComponent;
        } else if (sourceComponent.getParent() instanceof InputControlsPanel) {
            inputControlsPanel = (InputControlsPanel) sourceComponent.getParent();
        } else {
            return;
        }
        if (event.getActionCommand().toLowerCase().startsWith("save")) {
            saveFile();
        } else if (event.getActionCommand().toLowerCase().startsWith("build")) {
            buildClassFile(inputControlsPanel, false);
        } else if (event.getActionCommand().toLowerCase().startsWith("tablechanged")) {
            buildClassFile(inputControlsPanel, false);
            Object ibj = event.getSource();
        } else if (event.getActionCommand().toLowerCase().startsWith("parameter")) {
            updateParameters(event.getSource());
        } else if (event.getActionCommand().toLowerCase().startsWith("exit")) {
            exitRequest();
        }
    }

    public void buildClassFile(InputControlsPanel inputControlsPanel, boolean resetClassNameFromTable) {
        try {
            sourcePanel.setContentFromInput(inputControlsPanel, resetClassNameFromTable);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error Creating Class", JOptionPane.PLAIN_MESSAGE);
        }
        sourcePanel.setCaretPosition(0);
        JScrollBar bar = sourcePanelScroller.getVerticalScrollBar();
        bar.setValue(bar.getMinimum());
        resetTitleBar();
    }

    public void resetTitleBar() {
        String fullClassName = sourcePanel.getPackageName() + "." + sourcePanel.getClassName();
        boolean modified = sourcePanel.isModified();
        File lastSavedFile = sourcePanel.getLastSavedFile();

        if (modified) {
            setTitle(TITLE + "- " + fullClassName + "*");
        } else {
            setTitle(TITLE + "- " + fullClassName + " [" + lastSavedFile + "]");
        }
    }

    public void updateParameters(Object source) {
        InputControlsPanel inputPanel = null;
        if (! (source instanceof JComponent)) {
            return;
        }
        JComponent sourceComponent = (JComponent) source;
        if (sourceComponent instanceof InputControlsPanel) {
            inputPanel = (InputControlsPanel) sourceComponent;
        } else if (sourceComponent.getParent() instanceof InputControlsPanel) {
            inputPanel = (InputControlsPanel) sourceComponent.getParent();
        } else {
            return;
        }
        try {
            RdbDataParameter[] parameters = sourcePanel.setParameters(inputPanel);
            if (parameters == null) {
                return;
            }
            inputPanel.updateParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error Creating Class", JOptionPane.PLAIN_MESSAGE);
        }
    }

    public void saveFile() {
        try {
            File saveToFile = null;
            JFileChooser chooser = new JFileChooser(currentDirectory);
            chooser.setDialogTitle("Select Source Directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = chooser.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File directory = chooser.getSelectedFile();
                currentDirectory = chooser.getSelectedFile();
                directory = new File(directory, sourcePanel.getPackagePath());
                if (!directory.exists()) {
                    if (!directory.mkdirs()) {
                        throw new IOException("Unable to make source file directory. " + directory);
                    }
                }
                saveToFile = new File(directory, sourcePanel.getSourceName());
                if (saveToFile.exists()) {
                    int results = JOptionPane.showConfirmDialog(null, "Overwite file?", "File exists", JOptionPane.INFORMATION_MESSAGE);
                    if (results == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
            }
            if (saveToFile == null) {
                return;
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(saveToFile));
            writer.write(sourcePanel.getContent());
            writer.close();
            sourcePanel.setModified(false);
            sourcePanel.setLastSavedFile(saveToFile);
            resetTitleBar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unable to save file, " + e.getMessage(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void jbInit() throws Exception {
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
