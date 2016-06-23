package amgen.ri.rdb.build;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import amgen.ri.oracle.OraConnectionManager;

import com.borland.jbcl.layout.XYConstraints;
import com.borland.jbcl.layout.XYLayout;

public class InputControlsPanel extends JPanel implements TableModelListener {
    XYLayout xYLayout1 = new XYLayout();
    JLabel jLabel2 = new JLabel();
    JLabel jLabel3 = new JLabel();
    JLabel jLabel4 = new JLabel();
    JLabel jLabel5 = new JLabel();
    JLabel jLabel6 = new JLabel();
    JLabel jLabel7 = new JLabel();
    JLabel jLabel8 = new JLabel();
    JLabel jLabel9 = new JLabel();
    JTextField jdbcURLBox = new JTextField();
    JTextField authorBox = new JTextField();
    JTextArea descriptionBox = new JTextArea(3, 10);
    JTextField schemaBox = new JTextField();
    JTextField packageBox = new JTextField();
    JCheckBox gettersButton = new JCheckBox();
    JCheckBox settersButton = new JCheckBox();
    JCheckBox saveableButton = new JCheckBox();
    JButton buildButton = new JButton();
    JButton saveButton = new JButton();
    JTable jTable1 = new JTable();
    JScrollPane parameterScrollPane = new JScrollPane();
    JTable parametersTable = new JTable();
    JButton getTablesBox = new JButton();
    JCheckBox removeableButton = new JCheckBox();
    JLabel jLabel10 = new JLabel();
    JComboBox tableBox = new JComboBox();
    JLabel jLabel11 = new JLabel();
    JButton exitButton = new JButton();
    TitledBorder titledBorder1;
    JScrollPane descriptionScrollPane = new JScrollPane();

    ActionListener listener;
    JButton jButton1 = new JButton();
    TitledBorder titledBorder2;
    TitledBorder titledBorder3;

    protected String server = "ussf-tdbx-ddb01.amgen.com";
    protected String port = "1521";
    protected String sid = "SFDSC01T.amgen.com";
    protected String username = "aigdev";
    protected String password = "";
    TitledBorder titledBorder4;
    TitledBorder titledBorder5;
    JTextField classNameBox = new JTextField();
    JLabel jLabel12 = new JLabel();
    JButton addTable = new JButton();

    public InputControlsPanel(ActionListener listener) {
        try {
            jbInit();
            this.listener = listener;
            buildButton.addActionListener(listener);
            saveButton.addActionListener(listener);
            exitButton.addActionListener(listener);
            tableBox.addItemListener( (ItemListener) listener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {
        titledBorder1 = new TitledBorder("");
        titledBorder2 = new TitledBorder("");
        titledBorder3 = new TitledBorder("");
        titledBorder4 = new TitledBorder("");
        titledBorder5 = new TitledBorder("");
        xYLayout1.setWidth(454);
        xYLayout1.setHeight(767);
        this.setLayout(xYLayout1);
        jLabel2.setText("Author");
        jLabel3.setText("Description");
        jLabel4.setText("Schema");
        jLabel5.setText("Table");
        jLabel6.setText("Package Name");
        jLabel7.setText("Saveable");
        jLabel8.setText("Add JavaBean Getters");
        jLabel9.setText("Add JavaBean Setters");
        authorBox.setText("Jeffrey McDowell");
        gettersButton.setText("");
        gettersButton.addActionListener(new InputControlsPanel_gettersButton_actionAdapter(this));
        settersButton.setText("");
        settersButton.addActionListener(new InputControlsPanel_settersButton_actionAdapter(this));
        saveableButton.setText("");
        saveableButton.addActionListener(new InputControlsPanel_saveableButton_actionAdapter(this));
        buildButton.setActionCommand("build");
        buildButton.setSelected(false);
        buildButton.setText("Build Class >>");
        saveButton.setText("Save File");
        saveButton.setSelected(false);
        saveButton.setActionCommand("save");
        getTablesBox.setText("Get Tables");
        getTablesBox.addActionListener(new InputControlsPanel_getTablesBox_actionAdapter(this));
        getTablesBox.setSelected(false);
        getTablesBox.setActionCommand("tables");
        removeableButton.setText("");
        removeableButton.addActionListener(new InputControlsPanel_removeableButton_actionAdapter(this));
        jLabel10.setText("Removeable");
        jLabel11.setText("Parameters");
        exitButton.setActionCommand("exit");
        exitButton.setSelected(false);
        exitButton.setText("Exit");
        jButton1.setBorder(BorderFactory.createEtchedBorder());
        jButton1.setHorizontalAlignment(SwingConstants.LEFT);
        jButton1.setMargin(new Insets(5, 14, 2, 14));
        jButton1.setText("Connection");
        jButton1.setFont(jLabel2.getFont());
        jButton1.setForeground(jLabel2.getForeground());
        jButton1.addActionListener(new InputControlsPanel_jButton1_actionAdapter(this));
        jLabel12.setText("Class Name");
        addTable.setActionCommand("tables");
        addTable.setText("Add Table");
        addTable.addActionListener(new InputControlsPanel_addTable_actionAdapter(this));
        this.add(jLabel2, new XYConstraints(10, 44, -1, -1));
        this.add(jLabel3, new XYConstraints(10, 69, -1, -1));
        this.add(jdbcURLBox, new XYConstraints(115, 19, 320, 20));
        this.add(authorBox, new XYConstraints(115, 44, 320, 20));
        this.add(jTable1, new XYConstraints(21, 344, -1, -1));
        this.add(descriptionScrollPane, new XYConstraints(115, 69, 320, 61));
        this.add(schemaBox, new XYConstraints(115, 139, 320, 20));
        this.add(jLabel4, new XYConstraints(10, 139, -1, -1));
        this.add(packageBox, new XYConstraints(115, 169, 320, 20));
        this.add(jLabel6, new XYConstraints(10, 169, -1, -1));
        this.add(jButton1, new XYConstraints(8, 19, 73, 20));
        this.add(buildButton, new XYConstraints(332, 706, 115, -1));
        this.add(getTablesBox, new XYConstraints(115, 337, 146, -1));
        this.add(jLabel7, new XYConstraints(10, 226, -1, -1));
        this.add(jLabel10, new XYConstraints(10, 254, -1, -1));
        this.add(jLabel8, new XYConstraints(10, 281, -1, -1));
        this.add(jLabel9, new XYConstraints(10, 309, -1, -1));
        this.add(settersButton, new XYConstraints(247, 308, 20, 16));
        this.add(gettersButton, new XYConstraints(247, 279, 20, 18));
        this.add(removeableButton, new XYConstraints(247, 251, 20, -1));
        this.add(saveableButton, new XYConstraints(247, 223, 20, -1));
        this.add(parameterScrollPane, new XYConstraints(4, 439, 444, 255));
        this.add(jLabel5, new XYConstraints(10, 377, -1, -1));
        this.add(tableBox, new XYConstraints(115, 374, 320, -1));
        this.add(jLabel11, new XYConstraints(9, 417, -1, -1));
        this.add(saveButton, new XYConstraints(6, 706, 115, -1));
        this.add(exitButton, new XYConstraints(6, 737, 115, -1));
        this.add(classNameBox, new XYConstraints(115, 195, 320, 20));
        this.add(jLabel12, new XYConstraints(10, 195, -1, -1));
        this.add(addTable, new XYConstraints(273, 338, -1, -1));
        parameterScrollPane.getViewport().add(parametersTable, null);
        descriptionScrollPane.getViewport().add(descriptionBox, null);
        parametersTable.setBackground(Color.white);

        setJdbcURLBox();
    }

    public JTextField getAuthorBox() {
        return authorBox;
    }

    public JTextArea getDescriptionBox() {
        return descriptionBox;
    }

    public JCheckBox getGettersButton() {
        return gettersButton;
    }

    public String getJdbcURL() {
        if (server == null || sid == null || username == null || password == null) {
            return null;
        }
        return "jdbc:oracle:thin:" + username + "/" + password + "@" + server + ":" + port + ":" + sid;
    }

    public void setJdbcURLBox() {
        if (server == null || sid == null || username == null || password == null) {
            return;
        }
        StringBuffer displayPassword = new StringBuffer();
        for (int i = 0; i < password.length(); i++) {
            displayPassword.append("*");
        }
        String value = "jdbc://" + username + "/" + displayPassword + "@" + server + ":" + port + ":" + sid;
        jdbcURLBox.setEnabled(true);
        jdbcURLBox.setDoubleBuffered(true);
        jdbcURLBox.setEditable(false);
        jdbcURLBox.setText(value);
    }

    public JTextField getPackageBox() {
        return packageBox;
    }

    public JTextField getClassNameBox() {
        return classNameBox;
    }

    public void setClassNameBox(String className) {
        classNameBox.setText(className);
    }

    public JCheckBox getSaveableButton() {
        return saveableButton;
    }

    public JButton getSaveButton() {
        return saveButton;
    }

    public JTextField getSchemaBox() {
        return schemaBox;
    }

    public JCheckBox getSettersButton() {
        return settersButton;
    }

    public void updateParameters(RdbDataParameter[] parameters) {
        ParameterTableModel parameterTableModel = new ParameterTableModel(this, parameters);
        parameterTableModel.addTableModelListener(this);
        parametersTable.setModel(parameterTableModel);
    }

    public JCheckBox getRemoveableButton() {
        return removeableButton;
    }

    void getTablesBox_actionPerformed(ActionEvent e) {
        try {
            String schema = getSchemaBox().getText();
            if (!JavaSourceEditorPanel.checkInput(new String[] {schema})) {
                return;
            }
            DefaultComboBoxModel model = (DefaultComboBoxModel) tableBox.getModel();
            model.removeAllElements();
            RdbDataBuilder rdbDataBuilder = new RdbDataBuilder(RdbDataClassBuilder.CONNECTION_POOL);
            String[] tables = rdbDataBuilder.getTables(schema);
            tableBox.removeAllItems();
            for (int i = 0; i < tables.length; i++) {
                tableBox.addItem(tables[i]);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(null, e1.getMessage(), "Error Retrieving Tables", JOptionPane.PLAIN_MESSAGE);
        }

    }

    public void tableChanged(TableModelEvent event) {
        ParameterTableModel parameterTableModel = (ParameterTableModel) parametersTable.getModel();
        int rowChanged = event.getFirstRow();
        int columnChanged = event.getColumn();
        switch (columnChanged) {
            case (3):
                parameterTableModel.updatePrimaryKey(rowChanged);
                break;
            default:
        }
        ActionEvent e = new ActionEvent(this, 0, "build");
        listener.actionPerformed(e);

        parametersTable.setModel(parameterTableModel);
        parametersTable.repaint();
    }

    public JButton getBuildButton() {
        return buildButton;
    }

    public JButton getGetTablesBox() {
        return getTablesBox;
    }

    public String getTableBoxValue() {
        if (tableBox == null || tableBox.getSelectedItem() == null) {
            return null;
        }
        return tableBox.getSelectedItem().toString();
    }

    public RdbDataParameter[] getParameters() {
        ParameterTableModel parameterTableModel = (ParameterTableModel) parametersTable.getModel();
        if (parameterTableModel == null) {
            return new RdbDataParameter[0];
        }
        return parameterTableModel.getParameters();
    }

    public void saveableButton_actionPerformed(ActionEvent e) {
        ActionEvent e1 = new ActionEvent(this, 0, "build");
        listener.actionPerformed(e1);
    }

    public void removeableButton_actionPerformed(ActionEvent e) {
        ActionEvent e1 = new ActionEvent(this, 0, "build");
        listener.actionPerformed(e1);
    }

    public void gettersButton_actionPerformed(ActionEvent e) {
        ActionEvent e1 = new ActionEvent(this, 0, "build");
        listener.actionPerformed(e1);
    }

    public void settersButton_actionPerformed(ActionEvent e) {
        ActionEvent e1 = new ActionEvent(this, 0, "build");
        listener.actionPerformed(e1);
    }

    void getConnectionInfo(ActionEvent e) {
        JDBCConnectionDialog jdbcConnectionDialog = new JDBCConnectionDialog(null, "Entry JDBC Connection Information", true);
        if (server != null) {
            jdbcConnectionDialog.setServerField(server);
        }
        if (port != null) {
            jdbcConnectionDialog.setPortField(port);
        }
        if (sid != null) {
            jdbcConnectionDialog.setSidField(sid);
        }
        if (username != null) {
            jdbcConnectionDialog.setUsernameField(username);
        }
        if (password != null) {
            jdbcConnectionDialog.setPasswordField(password);
        }
        jdbcConnectionDialog.setVisible(true);

        if (jdbcConnectionDialog.isCancelled()) {
            return;
        }
        server = jdbcConnectionDialog.getServerField().getText();
        port = jdbcConnectionDialog.getPortField().getText();
        sid = jdbcConnectionDialog.getSidField().getText();
        username = jdbcConnectionDialog.getUsernameField().getText();
        password = new String(jdbcConnectionDialog.getPasswordField().getPassword());
        setJdbcURLBox();
        updateJDBC();
    }

    public void updateJDBC() {
        try {
            ((OraConnectionManager)OraConnectionManager.getInstance()).addCache(RdbDataClassBuilder.CONNECTION_POOL, getJdbcURL(), true);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error Updating Connection", JOptionPane.PLAIN_MESSAGE);
        }
    }

    public void addTable_actionPerformed(ActionEvent e) {
        String newTable = JOptionPane.showInputDialog(this, "New Table Name", "Additional Table", JOptionPane.PLAIN_MESSAGE);
        if (newTable == null) {
            return;
        }
        tableBox.addItem(newTable);
    }
}

class InputControlsPanel_addTable_actionAdapter implements ActionListener {
    private InputControlsPanel adaptee;
    InputControlsPanel_addTable_actionAdapter(InputControlsPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.addTable_actionPerformed(e);
    }
}

class InputControlsPanel_getTablesBox_actionAdapter implements java.awt.event.ActionListener {
    InputControlsPanel adaptee;

    InputControlsPanel_getTablesBox_actionAdapter(InputControlsPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.getTablesBox_actionPerformed(e);
    }
}

class ParameterTableModel extends AbstractTableModel {
    String[] columnNames = {
        "Include Column",
        "Oracle Column",
        "Oracle Column Type",
        "Primary Key",
        "Java Mapping Name",
        "Java Mapping Type"
    };
    private InputControlsPanel inputControlsPanel;
    private RdbDataParameter[] parameters;

    public ParameterTableModel(InputControlsPanel inputControlsPanel, RdbDataParameter[] parameters) {
        this.inputControlsPanel = inputControlsPanel;
        this.parameters = parameters;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return parameters.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        if (row < 0 || row >= parameters.length) {
            return null;
        }
        switch (col) {
            case (0):
                return new Boolean(parameters[row].includeParameter());
            case (1):
                return parameters[row].getColumnName();
            case (2):
                return parameters[row].getColumnFullTypeName();
            case (3):
                return new Boolean(parameters[row].isPrimaryKey());
            case (4):
                return parameters[row].getDeclaredName();
            case (5):
                return parameters[row].getDeclaredType();
        }
        return null;
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        if (col == 0 || col == 3) {
            return true;
        }
        return false;
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
        Boolean boolVal;
        if (row < 0 || row >= parameters.length) {
            return;
        }
        switch (col) {
            case (0):
                boolVal = (Boolean) value;
                parameters[row].setIncludeParameter(boolVal.booleanValue());
                break;
            case (1):
                break;
            case (2):
                break;
            case (3):
                boolVal = (Boolean) value;
                parameters[row].setPrimaryKey(boolVal.booleanValue());
                break;
            case (4):
                break;
            case (5):
                break;
        }
        fireTableCellUpdated(row, col);
    }

    public void updatePrimaryKey(int rowChanged) {
        Boolean value = (Boolean) getValueAt(rowChanged, 3);
        for (int i = 0; i < parameters.length; i++) {
            parameters[i].setPrimaryKey(false);
        }
        if (value.booleanValue()) {
            parameters[rowChanged].setPrimaryKey(true);
            System.out.println("true");
        } else {
            parameters[0].setPrimaryKey(true);
            System.out.println("false");
        }
    }

    public RdbDataParameter[] getParameters() {
        return parameters;
    }

}

class InputControlsPanel_saveableButton_actionAdapter implements java.awt.event.ActionListener {
    InputControlsPanel adaptee;

    InputControlsPanel_saveableButton_actionAdapter(InputControlsPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.saveableButton_actionPerformed(e);
    }
}

class InputControlsPanel_removeableButton_actionAdapter implements java.awt.event.ActionListener {
    InputControlsPanel adaptee;

    InputControlsPanel_removeableButton_actionAdapter(InputControlsPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.removeableButton_actionPerformed(e);
    }
}

class InputControlsPanel_gettersButton_actionAdapter implements java.awt.event.ActionListener {
    InputControlsPanel adaptee;

    InputControlsPanel_gettersButton_actionAdapter(InputControlsPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.gettersButton_actionPerformed(e);
    }
}

class InputControlsPanel_settersButton_actionAdapter implements java.awt.event.ActionListener {
    InputControlsPanel adaptee;

    InputControlsPanel_settersButton_actionAdapter(InputControlsPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.settersButton_actionPerformed(e);
    }
}

class InputControlsPanel_jButton1_actionAdapter implements java.awt.event.ActionListener {
    InputControlsPanel adaptee;

    InputControlsPanel_jButton1_actionAdapter(InputControlsPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.getConnectionInfo(e);
    }
}
