package amgen.ri.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;

import com.borland.jbcl.layout.XYConstraints;
import com.borland.jbcl.layout.XYLayout;

public class LoginDialog extends JDialog {
    JLabel usernameLabel = new JLabel();
    JTextField usernameField = new JTextField();
    JLabel passwordLabel = new JLabel();
    JPasswordField passwordField = new JPasswordField();
    private boolean okClicked;
    public LoginDialog(JFrame frame, boolean modal, String title, String label) {
        super(frame, modal);
        try {
            jbInit(title, label);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void jbInit(String title, String label) throws Exception {
        setTitle(title);
        this.getContentPane().setLayout(dialogLayout);
        dialogLabel.setBorder(null);
        dialogLabel.setText(label);
        usernameLabel.setText("Username");
        usernameField.setText("");
        passwordLabel.setText("Password");
        passwordField.setText("");
        jPanel1.setBorder(BorderFactory.createEtchedBorder());
        jPanel1.setLayout(panelLayout);
        okButton.setText("OK");
        okButton.addActionListener(new LoginDialog_okButton_actionAdapter(this));
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new LoginDialog_cancelButton_actionAdapter(this));
        dialogLayout.setWidth(400);
        dialogLayout.setHeight(184);
        jPanel1.add(usernameField, new XYConstraints(113, 43, 200, 20));
        jPanel1.add(usernameLabel, new XYConstraints(32, 40, 70, 26));
        jPanel1.add(dialogLabel, new XYConstraints(14, 6, 331, 26));
        jPanel1.add(passwordLabel, new XYConstraints(32, 71, 70, 25));
        jPanel1.add(passwordField, new XYConstraints(113, 73, 200, 20));
        this.getContentPane().add(cancelButton, new XYConstraints(205, 135, 85, -1));
        this.getContentPane().add(okButton, new XYConstraints(86, 135, 85, -1));
        this.getContentPane().add(jPanel1, new XYConstraints(18, 9, 362, 110));
        getRootPane().setDefaultButton(okButton);
    }

    com.borland.jbcl.layout.XYLayout dialogLayout = new XYLayout();
    javax.swing.JLabel dialogLabel = new JLabel();
    javax.swing.JPanel jPanel1 = new JPanel();
    com.borland.jbcl.layout.XYLayout panelLayout = new XYLayout();
    javax.swing.JButton okButton = new JButton();
    javax.swing.JButton cancelButton = new JButton();
    public void okButton_actionPerformed(ActionEvent e) {
        setOkClicked(true);
        setVisible(false);
        dispose();
    }

    public void cancelButton_actionPerformed(ActionEvent e) {
        setOkClicked(false);
        setVisible(false);
        dispose();
    }

    public void setUsername(String username) {
        usernameField.setText(username);
    }

    public void setPassword(String password) {
        passwordField.setText(password);
    }

    public void setOkClicked(boolean okClicked) {
        this.okClicked = okClicked;
    }

    public String getUsername() {
        return usernameField.getText();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {}

        LoginDialog dialog = new LoginDialog(null, true, "title", "label");
        /*
             dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                System.exit(0);
            }

            public void windowClosed(WindowEvent event) {
                System.exit(0);
            }
             });
         */

        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        if (dialog.isOkClicked()) {
            System.out.println(dialog.getUsername());
            System.out.println(dialog.getPassword());
        }
    }
} // class LoginDialog

class LoginDialog_cancelButton_actionAdapter implements ActionListener {
    private LoginDialog adaptee;
    LoginDialog_cancelButton_actionAdapter(LoginDialog adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.cancelButton_actionPerformed(e);
    }
}

class LoginDialog_okButton_actionAdapter implements ActionListener {
    private LoginDialog adaptee;
    LoginDialog_okButton_actionAdapter(LoginDialog adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.okButton_actionPerformed(e);
    }
}
