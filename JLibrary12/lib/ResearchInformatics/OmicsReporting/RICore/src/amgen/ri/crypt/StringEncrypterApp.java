package amgen.ri.crypt;

import amgen.ri.util.ExtString;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.borland.jbcl.layout.XYConstraints;
import com.borland.jbcl.layout.XYLayout;
import java.awt.datatransfer.*;

public final class StringEncrypterApp extends JFrame implements ClipboardOwner {
  JLabel encryptedTextLabel = new JLabel();
  JTextField inputField = new JTextField();
  JLabel inputTextLabel = new JLabel();
  XYLayout mainLayout = new XYLayout();
  JTextField encryptedField = new JTextField();
  JButton closeButton = new JButton();
  JPanel mainPanel = new JPanel();
  XYLayout subLayout = new XYLayout();
  JButton copyCloseButton = new JButton();
  JButton copyButton = new JButton();

  public StringEncrypterApp(String text) {
    super("String Encrypter");
    try {
      jbInit();
      if (ExtString.hasTrimmedLength(text) && !text.trim().equalsIgnoreCase("#{selection}")) {
        setEncryptedText(text.trim());
      } else {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable clipData = clipboard.getContents(this);
        String clipText = null;
        try {
          clipText = (String) (clipData.getTransferData(DataFlavor.stringFlavor));
          clipText= clipText.trim();
        } catch (Exception ee) {
          clipText = null;
        }
        setEncryptedText(clipText);
      }
    } catch (Exception e) {
      //e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setResizable(false);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    copyButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setEncryptedTextClipboardContents();
      }
    });
    copyCloseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setEncryptedTextClipboardContents();
        System.exit(0);
      }
    });
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });
    inputField.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        setEncryptedText(null);
      }
    });

    getContentPane().setLayout(mainLayout);
    encryptedTextLabel.setText("Encrypted Text");
    inputTextLabel.setPreferredSize(new Dimension(21, 10));
    inputTextLabel.setText("Input Text");
    inputField.setColumns(1);
    mainLayout.setWidth(392);
    mainLayout.setHeight(104);
    encryptedField.setEditable(false);
    encryptedField.setColumns(1);

    mainPanel.setBorder(BorderFactory.createLineBorder(Color.black));
    mainPanel.setLayout(subLayout);
    this.getRootPane().setDefaultButton(copyCloseButton);
    closeButton.setText("Close");
    copyCloseButton.setText("Copy & Close");
    copyButton.setText("Copy");
    mainPanel.add(inputField, new XYConstraints(113, 7, 260, 20));
    mainPanel.add(encryptedTextLabel, new XYConstraints(6, 28, 100, 24));
    mainPanel.add(encryptedField, new XYConstraints(113, 30, 260, 20));
    mainPanel.add(inputTextLabel, new XYConstraints(6, 5, 100, 24));
    this.getContentPane().add(mainPanel, new XYConstraints(3, 4, 386, 60));
    this.getContentPane().add(closeButton, new XYConstraints(289, 76, 100, 24));
    this.getContentPane().add(copyButton, new XYConstraints(69, 76, 100, 24));
    this.getContentPane().add(copyCloseButton, new XYConstraints(179, 76, 100, 24));
  }

  public void setEncryptedText(String plainText) {
    if (plainText != null) {
      inputField.setText(plainText);
    }
    String encryptedText = "";
    String inputText = inputField.getText();
    if (inputText != null && inputText.length() > 0) {
      try {
        if (inputText.startsWith("!")) {
          encryptedText = new StringEncrypter().decrypt(inputText.substring(1));
          encryptedTextLabel.setText("Decrypted Text");
        } else {
          encryptedText = new StringEncrypter().encrypt(inputText);
          encryptedTextLabel.setText("Encrypted Text");
        }
      } catch (Exception ex) {
      }
    }
    encryptedField.setText(encryptedText);
  }

  /**
   * Place a String on the clipboard, and make this class the
   * owner of the Clipboard's contents.
   */
  public void setEncryptedTextClipboardContents() {
    StringSelection stringSelection = new StringSelection(encryptedField.getText());
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(stringSelection, this);
  }

  /**
   * Empty implementation of the ClipboardOwner interface.
   */
  public void lostOwnership(Clipboard aClipboard, Transferable aContents) {
    //do nothing
  }

  public static void main(String[] args) throws UnsupportedLookAndFeelException, IllegalAccessException, InstantiationException, ClassNotFoundException {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    StringEncrypterApp app = new StringEncrypterApp((args != null && args.length > 0 ? args[0] : null));
    app.pack();
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension screenSize = toolkit.getScreenSize();
    int x = (screenSize.width - app.getWidth()) / 2;
    int y = (screenSize.height - app.getHeight()) / 2;
    app.setLocation(x, y);
    app.setVisible(true);
  }
}
