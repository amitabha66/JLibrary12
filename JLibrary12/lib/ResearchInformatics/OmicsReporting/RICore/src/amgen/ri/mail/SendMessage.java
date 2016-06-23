package amgen.ri.mail;

import amgen.ri.ldap.PersonRecordIF;
import amgen.ri.util.Debug;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;

public class SendMessage {
  public static final String SMTP_HOST = "SMTP_HOST";
  private String smtpHost;
  private Properties mailSessionProperties;

  public SendMessage() {
    this("mailhost.amgen.com");
  }

  public SendMessage(ServletContext context) {
    this.smtpHost = context.getInitParameter(SMTP_HOST);

    mailSessionProperties = new Properties();
    mailSessionProperties.put("mail.smtp.host", smtpHost);
  }

  public SendMessage(String smtpHost) {
    this.smtpHost = smtpHost;
    mailSessionProperties = new Properties();
    mailSessionProperties.put("mail.smtp.host", smtpHost);
  }

  public void sendMessage(String to, String from, String subject, String message) throws AddressException, MessagingException {
    sendMessage(new String[]{to}, null, null, from, subject, message);
  }

  public void sendMessage(PersonRecordIF to, String from, String subject, String message) throws AddressException, MessagingException {
    sendMessage(new String[]{to.getEmail()}, null, null, from, subject, message);
  }

  public void sendMessage(String[] to, String from, String subject, String message) throws AddressException, MessagingException {
    sendMessage(to, null, null, from, subject, message);

  }

  public void sendMessage(String[] to, String[] cc, String[] bcc, String from, String subject, String message) throws AddressException, MessagingException {
    sendMessage(to, cc, bcc, from, subject, message, "text/plain");
  }

  public void sendHTMLMessage(String[] to, String[] cc, String[] bcc, String from, String subject, String message) throws AddressException, MessagingException {
    sendMessage(to, cc, bcc, from, subject, message, "text/html");
  }

  public void sendMessage(String[] to, String[] cc, String[] bcc, String from, String subject, Object message, String mimetype) throws AddressException,
          MessagingException {
    Session session = Session.getDefaultInstance(getMailSessionProperties(), null);
    MimeMessage mimeMessage = new MimeMessage(session);
    if (from != null) {
      mimeMessage.setFrom(new InternetAddress(from));
    }

    if (to != null) {
      for (int i = 0; i < to.length; i++) {
        mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to[i]));
        //Debug.print("TO: " + new InternetAddress(to[i]));
      }
    }
    if (cc != null) {
      for (int i = 0; i < cc.length; i++) {
        mimeMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(cc[i]));
        //Debug.print("CC: " + new InternetAddress(cc[i]));
      }
    }
    if (bcc != null) {
      for (int i = 0; i < bcc.length; i++) {
        mimeMessage.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc[i]));
        //Debug.print("BCC: " + new InternetAddress(bcc[i]));
      }
    }
    mimeMessage.setSubject(subject);
    mimeMessage.setContent(message, mimetype);
    Transport.send(mimeMessage);
  }

  public void sendHTMLMessage(PersonRecordIF[] to, PersonRecordIF[] cc, PersonRecordIF[] bcc, String from, String subject, String message) throws AddressException, MessagingException {
    sendMessage(to, cc, bcc, from, subject, message, "text/html");
  }

  public void sendMessage(PersonRecordIF[] to, PersonRecordIF[] cc, PersonRecordIF[] bcc, String from, String subject, Object message, String mimetype) throws AddressException,
          MessagingException {
    Session session = Session.getDefaultInstance(getMailSessionProperties(), null);
    MimeMessage mimeMessage = new MimeMessage(session);
    if (from != null) {
      mimeMessage.setFrom(new InternetAddress(from));
    }

    if (to != null) {
      for (int i = 0; i < to.length; i++) {
        mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to[i].getEmail()));
      }
    }
    if (cc != null) {
      for (int i = 0; i < cc.length; i++) {
        mimeMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(cc[i].getEmail()));
      }
    }
    if (bcc != null) {
      for (int i = 0; i < bcc.length; i++) {
        mimeMessage.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc[i].getEmail()));
      }
    }
    mimeMessage.setSubject(subject);
    mimeMessage.setContent(message, mimetype);
    Transport.send(mimeMessage);
  }

  public void sendMessage(String username, String password, String[] to, String[] cc, String[] bcc, String from, String subject, Object message, String mimetype) throws
          AddressException, MessagingException {
    getMailSessionProperties().put("mail.smtp.auth", "true");
    Session session = Session.getDefaultInstance(getMailSessionProperties(), null);
    MimeMessage mimeMessage = new MimeMessage(session);
    if (from != null) {
      mimeMessage.setFrom(new InternetAddress(from));
    }
    if (to != null) {
      for (int i = 0; i < to.length; i++) {
        mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to[i]));
      }
    }
    if (cc != null) {
      for (int i = 0; i < cc.length; i++) {
        mimeMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(cc[i]));
      }
    }
    if (bcc != null) {
      for (int i = 0; i < bcc.length; i++) {
        mimeMessage.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc[i]));
      }
    }
    mimeMessage.setSubject(subject);
    mimeMessage.setContent(message, mimetype);
    Transport tr = session.getTransport("smtp");
    tr.connect(getSmtpHost(), 465, username, password);
    tr.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
  }

  /**
   * @return the smtpHost
   */
  public String getSmtpHost() {
    return smtpHost;
  }

  /**
   * @param smtpHost the smtpHost to set
   */
  public void setSmtpHost(String smtpHost) {
    this.smtpHost = smtpHost;
  }

  /**
   * @return the mailSessionProperties
   */
  public Properties getMailSessionProperties() {
    return mailSessionProperties;
  }

  /**
   * @param mailSessionProperties the mailSessionProperties to set
   */
  public void setMailSessionProperties(Properties mailSessionProperties) {
    this.mailSessionProperties = mailSessionProperties;
  }

  public static void main(String[] args) {
    try {/*
       * Properties properties = new Properties();
       * properties.put("mail.transport.protocol", "smtp");
       * properties.put("mail.smtp.host", "mail.example.com");
       * properties.put("mail.smtp.port", "2525");
       * properties.put("mail.smtp.auth", "true"); final String username =
       * "username"; final String password = "password"; Authenticator
       * authenticator = new Authenticator() { protected PasswordAuthentication
       * getPasswordAuthentication() { return new
       * PasswordAuthentication(username, password); } }; Transport transport =
       * null; try { Session session = Session.getDefaultInstance(properties,
       * authenticator); MimeMessage mimeMessage = createMimeMessage(session,
       * mimeMessageData); transport = session.getTransport();
       * transport.connect(username, password);
       * transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients()); }
       * finally { if (transport != null) { try { transport.close(); } catch
       * (MessagingException logOrIgnore) { } } }
       */

      SendMessage m = new SendMessage("ussf-pmsg-mbs01.am.corp.amgen.com");
      m.sendMessage("AM\\jemcdowe", "Seagrams1", new String[]{"jemcdowe@amgen.com"}, null, null, "jemcdowe@amgen.com",
              "Test", "Test", "text/html");
    } catch (Exception ex) {
      ex.printStackTrace();
    }

  }
}
