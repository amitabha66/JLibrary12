package amgen.ri.rdb.build;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.borland.jbcl.layout.XYConstraints;
import com.borland.jbcl.layout.XYLayout;

public class InputControlFilePanel extends JPanel implements TableModelListener {
    XYLayout xYLayout1 = new XYLayout();
    JLabel jLabel4 = new JLabel();
    JLabel jLabel5 = new JLabel();
    JTextField jdbcURLBox = new JTextField();
    JTextField schemaBox = new JTextField();
    JButton saveButton = new JButton();
    JTable jTable1 = new JTable();
    JScrollPane parameterScrollPane = new JScrollPane();
    JTable parametersTable = new JTable();
    JButton getTablesButton = new JButton();
    JComboBox tableBox = new JComboBox();
    JLabel jLabel11 = new JLabel();
    JButton exitButton = new JButton();
    TitledBorder titledBorder1;

    ActionListener listener;
    JButton jButton1 = new JButton();
    TitledBorder titledBorder2;
    TitledBorder titledBorder3;

    protected String server = "localhost";
    protected String port = "1521";
    protected String sid = "AMGEN";
    protected String username = "amgen";
    protected String password = "amgen;";
    TitledBorder titledBorder4;
    TitledBorder titledBorder5;
    JComboBox delimiterBox;
    JLabel jLabel6 = new JLabel();
    JButton newTable = new JButton();

    public InputControlFilePanel(ActionListener listener) {
        try {
            jbInit();
            this.listener = listener;
            saveButton.addActionListener(listener);
            exitButton.addActionListener(listener);
            tableBox.addItemListener( (ItemListener) listener);
            tableBox.addActionListener(new InputControlFilePanel_TableBox_actionAdapter(this));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {
        delimiterBox = new JComboBox(new String[] {"CSV", "TAB"});
        delimiterBox.addActionListener(new InputControlFilePanel_DelimiterBox_actionAdapter(this));
        titledBorder1 = new TitledBorder("");
        titledBorder2 = new TitledBorder("");
        titledBorder3 = new TitledBorder("");
        titledBorder4 = new TitledBorder("");
        titledBorder5 = new TitledBorder("");
        xYLayout1.setWidth(454);
        xYLayout1.setHeight(767);
        this.setLayout(xYLayout1);
        jLabel4.setText("Schema");
        jLabel5.setText("Table");
        saveButton.setText("Save File");
        saveButton.setSelected(false);
        saveButton.setActionCommand("save");
        getTablesButton.setText("Get Tables");
        getTablesButton.addActionListener(new InputControlFilePanel_getTablesButton_actionAdapter(this));
        getTablesButton.setSelected(false);
        getTablesButton.setActionCommand("tables");
        jLabel11.setText("Parameters");
        exitButton.setActionCommand("exit");
        exitButton.setSelected(false);
        exitButton.setText("Exit");
        jButton1.setBorder(BorderFactory.createEtchedBorder());
        jButton1.setHorizontalAlignment(SwingConstants.LEFT);
        jButton1.setMargin(new Insets(5, 14, 2, 14));
        jButton1.setText("Connection");
        jButton1.setFont(jLabel4.getFont());
        jButton1.setForeground(jLabel4.getForeground());
        jButton1.addActionListener(new InputControlFilePanel_jButton1_actionAdapter(this));
        jLabel6.setText("Delimiter");
        newTable.setActionCommand("tables");
        newTable.setText("Add Table");
        newTable.addActionListener(new InputControlFilePanel_newTable_actionAdapter(this));
        this.add(jdbcURLBox, new XYConstraints(115, 19, 320, 20));
        this.add(jTable1, new XYConstraints(21, 344, -1, -1));
        this.add(jButton1, new XYConstraints(8, 19, 73, 20));
        this.add(schemaBox, new XYConstraints(115, 51, 320, 20));
        this.add(jLabel4, new XYConstraints(8, 51, -1, -1));
        this.add(getTablesButton, new XYConstraints(116, 75, 146, -1));
        this.add(parameterScrollPane, new XYConstraints(5, 177, 444, 255));
        this.add(tableBox, new XYConstraints(116, 112, 320, -1));
        this.add(jLabel5, new XYConstraints(11, 115, -1, -1));
        this.add(jLabel11, new XYConstraints(10, 155, -1, -1));
        this.add(delimiterBox, new XYConstraints(114, 443, 187, -1));
        this.add(jLabel6, new XYConstraints(9, 446, -1, -1));
        this.add(exitButton, new XYConstraints(138, 489, 115, -1));
        this.add(saveButton, new XYConstraints(8, 489, 115, -1));
        this.add(newTable, new XYConstraints(283, 74, -1, -1));
        parameterScrollPane.getViewport().add(parametersTable, null);
        parametersTable.setBackground(Color.white);

        setJdbcURLBox();
    }

    public String getJdbcURL() {
        if (server == null || sid == null || username == null || password == null) {
            return null;
        }
        return username + "/" + password + "@" + server + ":" + port + ":" + sid;
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

    public JButton getSaveButton() {
        return saveButton;
    }

    public JTextField getSchemaBox() {
        return schemaBox;
    }

    public void updateParameters() {
        try {
            RdbDataBuilder rdbDataBuilder = new RdbDataBuilder(RdbDataClassBuilder.CONNECTION_POOL);
            RdbDataParameter[] parameters = rdbDataBuilder.getParameters(getSchemaBox().getText(), getTableBoxValue());

            InputControlFilePanelParameterTableModel parameterTableModel = new InputControlFilePanelParameterTableModel(this, parameters);
            parameterTableModel.addTableModelListener(this);
            parametersTable.setModel(parameterTableModel);
            ActionEvent e = new ActionEvent(this, 0, "build");
            listener.actionPerformed(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void getTablesButton_actionPerformed(ActionEvent e) {
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
            updateParameters();
        } catch (Exception e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(null, e1.getMessage(), "Error Retrieving Tables", JOptionPane.PLAIN_MESSAGE);
        }
    }

    void tableBox_actionPerformed(ActionEvent e) {
        updateParameters();
    }

    void delimiterBox_actionPerformed(ActionEvent evt) {
        ActionEvent e = new ActionEvent(this, 0, "build");
        listener.actionPerformed(e);
    }

    public void tableChanged(TableModelEvent event) {
        InputControlFilePanelParameterTableModel parameterTableModel = (InputControlFilePanelParameterTableModel) parametersTable.getModel();
        parametersTable.repaint();

        ActionEvent e = new ActionEvent(this, 0, "build");
        listener.actionPerformed(e);
    }

    public JButton getGetTablesBox() {
        return getTablesButton;
    }

    public String getTableBoxValue() {
        if (tableBox == null || tableBox.getSelectedItem() == null) {
            return null;
        }
        return tableBox.getSelectedItem().toString();
    }

    public String getDelimiterValue() {
        return (String) delimiterBox.getSelectedItem();
    }

    public RdbDataParameter[] getParameters() {
        TableModel model = parametersTable.getModel();
        if (! (model instanceof InputControlFilePanelParameterTableModel)) {
            return new RdbDataParameter[0];
        }
        InputControlFilePanelParameterTableModel parameterTableModel = (InputControlFilePanelParameterTableModel) parametersTable.getModel();
        return parameterTableModel.getParameters();
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
    }

    public void newTable_actionPerformed(ActionEvent e) {
        String newTable = JOptionPane.showInputDialog(this, "New Table Name", "Additional Table", JOptionPane.PLAIN_MESSAGE);
        if (newTable == null) {
            return;
        }
        tableBox.addItem(newTable);
    }
}

class InputControlFilePanel_newTable_actionAdapter implements ActionListener {
    private InputControlFilePanel adaptee;
    InputControlFilePanel_newTable_actionAdapter(InputControlFilePanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.newTable_actionPerformed(e);
    }
}

class InputControlFilePanel_getTablesButton_actionAdapter implements java.awt.event.ActionListener {
    InputControlFilePanel adaptee;

    InputControlFilePanel_getTablesButton_actionAdapter(InputControlFilePanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.getTablesButton_actionPerformed(e);
    }
}

class InputControlFilePanel_DelimiterBox_actionAdapter implements java.awt.event.ActionListener {
    InputControlFilePanel adaptee;

    InputControlFilePanel_DelimiterBox_actionAdapter(InputControlFilePanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.delimiterBox_actionPerformed(e);
    }
}

class InputControlFilePanel_TableBox_actionAdapter implements java.awt.event.ActionListener {
    InputControlFilePanel adaptee;

    InputControlFilePanel_TableBox_actionAdapter(InputControlFilePanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.tableBox_actionPerformed(e);
    }
}

class InputControlFilePanelParameterTableModel extends AbstractTableModel {
    String[] columnNames = {
        "Include Column",
        "Oracle Column",
        "Oracle Column Type",
        "Primary Key",
        "Is Nullable"
    };
    private InputControlFilePanel inputControlsPanel;
    private RdbDataParameter[] parameters;

    public InputControlFilePanelParameterTableModel(InputControlFilePanel inputControlsPanel, RdbDataParameter[] parameters) {
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
                return parameters[row].isPrimaryKey() + "";
            case (4):
                return parameters[row].isNullable() + "";
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
        if (col == 0) {
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
                break;
            case (4):
                break;
            case (5):
                break;
        }
        fireTableCellUpdated(row, col);
    }

    public RdbDataParameter[] getParameters() {
        return parameters;
    }

}

class InputControlFilePanel_jButton1_actionAdapter implements java.awt.event.ActionListener {
    InputControlFilePanel adaptee;

    InputControlFilePanel_jButton1_actionAdapter(InputControlFilePanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.getConnectionInfo(e);
    }
}
