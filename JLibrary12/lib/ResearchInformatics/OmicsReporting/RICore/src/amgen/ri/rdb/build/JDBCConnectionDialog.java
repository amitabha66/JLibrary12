package amgen.ri.rdb.build;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.borland.jbcl.layout.XYConstraints;
import com.borland.jbcl.layout.XYLayout;

public class JDBCConnectionDialog extends JDialog {
    JPanel panel1 = new JPanel();
    JButton jButton1 = new JButton();
    JPanel inputPanel = new JPanel();
    XYLayout xYLayout2 = new XYLayout();
    JLabel jLabel4 = new JLabel();
    JLabel jLabel2 = new JLabel();
    JTextField portField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JLabel jLabel6 = new JLabel();
    JLabel jLabel3 = new JLabel();
    JTextField serverField = new JTextField();
    JTextField sidField = new JTextField();
    JTextField usernameField = new JTextField();
    JLabel jLabel5 = new JLabel();
    JButton jButton2 = new JButton();
    boolean cancelled;
    JPanel jPanel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    FlowLayout flowLayout1 = new FlowLayout();

    public JDBCConnectionDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        try {
            jbInit();
            setSize(350, 220);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public JDBCConnectionDialog() {
        this(null, "", false);
    }

    private void jbInit() throws Exception {
        panel1.setLayout(borderLayout1);
        jButton1.setText("OK");
        this.getRootPane().setDefaultButton(jButton1);
        jButton1.addActionListener(new JDBCConnectionDialog_jButton1_actionAdapter(this));
        inputPanel.setLayout(xYLayout2);
        jLabel4.setToolTipText("");
        jLabel4.setText("SID");
        jLabel2.setText("Server");
        jLabel6.setText("Password");
        jLabel3.setText("Port");
        jLabel5.setText("Username");
        inputPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        inputPanel.setMinimumSize(new Dimension(317, 160));
        jButton2.setText("Cancel");
        jButton2.addActionListener(new JDBCConnectionDialog_jButton2_actionAdapter(this));
        serverField.setText("");
        portField.setText("");
        sidField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        jPanel1.setLayout(flowLayout1);
        getContentPane().add(panel1);
        panel1.add(inputPanel, BorderLayout.CENTER);
        inputPanel.add(serverField, new XYConstraints(106, 14, 209, 18));
        inputPanel.add(jLabel2, new XYConstraints(39, 16, -1, -1));
        inputPanel.add(portField, new XYConstraints(106, 41, 209, 18));
        inputPanel.add(jLabel3, new XYConstraints(39, 43, -1, -1));
        inputPanel.add(jLabel4, new XYConstraints(39, 71, -1, -1));
        inputPanel.add(sidField, new XYConstraints(106, 69, 209, 18));
        inputPanel.add(jLabel5, new XYConstraints(39, 101, -1, -1));
        inputPanel.add(usernameField, new XYConstraints(106, 99, 209, 18));
        inputPanel.add(jLabel6, new XYConstraints(39, 129, -1, -1));
        inputPanel.add(passwordField, new XYConstraints(106, 127, 209, 18));
        panel1.add(jPanel1, BorderLayout.SOUTH);
        jPanel1.add(jButton1, null);
        jPanel1.add(jButton2, null);
    }

    void pressedOK(ActionEvent e) {
        setCancelled(false);
        setVisible(false);
    }

    void pressedCancel(ActionEvent e) {
        setCancelled(true);
        setVisible(false);
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    public void setPasswordField(String passwordField) {
        this.passwordField.setText(passwordField);
    }

    public JTextField getPortField() {
        return portField;
    }

    public void setPortField(String portField) {
        this.portField.setText(portField);
    }

    public void setServerField(String serverField) {
        this.serverField.setText(serverField);
    }

    public JTextField getServerField() {
        return serverField;
    }

    public JTextField getSidField() {
        return sidField;
    }

    public void setSidField(String sidField) {
        this.sidField.setText(sidField);
    }

    public void setUsernameField(String usernameField) {
        this.usernameField.setText(usernameField);
    }

    public JTextField getUsernameField() {
        return usernameField;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}

class JDBCConnectionDialog_jButton1_actionAdapter implements java.awt.event.ActionListener {
    JDBCConnectionDialog adaptee;

    JDBCConnectionDialog_jButton1_actionAdapter(JDBCConnectionDialog adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.pressedOK(e);
    }
}

class JDBCConnectionDialog_jButton2_actionAdapter implements java.awt.event.ActionListener {
    JDBCConnectionDialog adaptee;

    JDBCConnectionDialog_jButton2_actionAdapter(JDBCConnectionDialog adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.pressedCancel(e);
    }
}
