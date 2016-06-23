package amgen.ri.security;

import amgen.ri.ldap.AmgenEnterpriseEntry;
import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * An Identity object which holds user information and if necessary session information. This class is
 * designed for use in the FASFEncrypter object to enable crypt & decrypting in base64
 *
 * @version $Id: FASFIdentity.java,v 1.4 2014/04/09 22:56:57 jemcdowe Exp $
 */
public class FASFIdentity extends AbstractIdentity implements SessionIdentityIF, Serializable {
  static final long serialVersionUID = -6033752535977277536L;
  private String requestHost;
  private String sessionID;
  private Date sessionStart;
  private Date lastAccess;
  private Map<String, Object> attributes;
  transient Map<String, Object> transientAttributes;
  transient private AmgenEnterpriseEntry entry;

  /**
   * Create a FASFIdentity object using just a username
   *
   * @param username String
   */
  protected FASFIdentity() {
    super();
    this.pingLastAccess();
    this.attributes = new HashMap<String, Object>();
    this.transientAttributes = new HashMap<String, Object>();
  }

  /**
   * Create a FASFIdentity object using just a username
   *
   * @param username String
   */
  public FASFIdentity(String username) {
    this();
    if (username != null) {
      String[] usernameSplit = username.split("[\\/\\\\]+", 2);
      username = (usernameSplit.length == 1 ? usernameSplit[0] : usernameSplit[1]);
    }
    setUsername(username);
  }

  /**
   * Create a FASFIdentity using a username and a request host
   *
   * @param username String
   * @param requestHost String
   */
  public FASFIdentity(String username, String requestHost) {
    this(username);
    this.requestHost = requestHost;
  }

  /**
   * Create a FASFIdentity from a username and information in an HTTPServletRequest
   *
   * @param username String
   * @param request HttpServletRequest
   * @throws SecurityException
   * @throws ServletException
   */
  public FASFIdentity(String username, HttpServletRequest request) throws SecurityException, ServletException {
    this(username, request.getRemoteHost());
    this.sessionStart = new Date(request.getSession().getCreationTime());
    this.lastAccess = new Date(request.getSession().getLastAccessedTime());
  }

  /**
   * Returns the request host
   *
   * @return String
   */
  public String getRequestHost() {
    return requestHost;
  }

  /**
   * Returns the session ID if available
   *
   * @return String
   */
  public String getSessionID() {
    return sessionID;
  }

  /**
   * Returns the session start Date if available
   *
   * @return Date
   */
  public Date getSessionStart() {
    return sessionStart;
  }

  /**
   * Returns the last access Date if available
   *
   * @return Date
   */
  public Date getLastAccess() {
    return lastAccess;
  }

  /**
   * Returns the time since last access in a particular unit. Use the following: Calendar.HOUR,
   * Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND
   *
   * @return
   */
  public long getTimeSinceLastAccess(int calendarUnit) {
    long millis = System.currentTimeMillis() - getLastAccess().getTime();
    switch (calendarUnit) {
      case Calendar.HOUR:
        return ((millis / 1000) / 60) / 60;
      case Calendar.MINUTE:
        return (millis / 1000) / 60;
      case Calendar.SECOND:
        return millis / 1000;
      case Calendar.MILLISECOND:
      default:
        return millis;
    }
  }

  /**
   * Returns the time since this object was created in a particular unit. Use the following: Calendar.HOUR,
   * Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND
   *
   * @return
   */
  public long getTimeSinceCreateD(int calendarUnit) {
    long millis = System.currentTimeMillis() - getCreated().getTime();
    switch (calendarUnit) {
      case Calendar.HOUR:
        return ((millis / 1000) / 60) / 60;
      case Calendar.MINUTE:
        return (millis / 1000) / 60;
      case Calendar.SECOND:
        return millis / 1000;
      case Calendar.MILLISECOND:
      default:
        return millis;
    }
  }

  /**
   * Updates the session ID
   *
   * @param sessionID String
   */
  public void setSessionID(String sessionID) {
    this.sessionID = sessionID;
  }

  /**
   * Updates the session start Date
   */
  public void setSessionStart(long sessionStart) {
    this.sessionStart = new Date(sessionStart);
  }

  /**
   * Sets Last Access data to current
   */
  public final void pingLastAccess() {
    setLastAccess(System.currentTimeMillis());
  }

  /**
   * Updates the session last access Date
   */
  public void setLastAccess(long lastAccess) {
    this.lastAccess = new Date(lastAccess);
  }

  /**
   * Returns an attribute. Null if not set
   *
   * @param attributeName String
   * @return Object
   */
  public Object getAttribute(String attributeName) {
    return attributes.get(attributeName);
  }

  /**
   * Sets an attribute
   *
   * @param attributeName String
   * @param attributeValue Object
   */
  public void setAttribute(String attributeName, Object attributeValue) {
    attributes.put(attributeName, attributeValue);
  }

  /**
   * Returns a transient attribute. Null if not set
   *
   * @param attributeName String
   * @return Object
   */
  public Object getTransientAttributes(String attributeName) {
    if (transientAttributes == null) {
      this.transientAttributes = new HashMap<String, Object>();
    }
    return transientAttributes.get(attributeName);
  }

  /**
   * Sets a transient attribute
   *
   * @param attributeName String
   * @param attributeValue Object
   */
  public void setTransientAttributes(String attributeName, Object attributeValue) {
    if (transientAttributes == null) {
      this.transientAttributes = new HashMap<String, Object>();
    }
    transientAttributes.put(attributeName, attributeValue);
  }

  /**
   * Updates this FASFIdentity with a copy of another FASFIdentity object
   */
  protected void update(FASFIdentity fasfIdentity) {
    FASFIdentity clone = (FASFIdentity) fasfIdentity.clone();
    this.setUsername(clone.getUsername());
    this.requestHost = clone.requestHost;
    this.sessionID = clone.sessionID;
    this.sessionStart = clone.sessionStart;
    this.lastAccess = clone.lastAccess;
    this.attributes = clone.attributes;
  }

  /**
   * Returns a clone of this object
   *
   * @return Object
   */
  public Object clone() {
    FASFIdentity clone = null;
    try {
      //Use the Serializer to create a copy of this Object
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bos);
      out.writeObject(this);
      out.flush();
      out.close();
      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
      clone = (FASFIdentity) in.readObject();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return clone;
  }

  /**
   * Returns an encrypted base64 String of this object
   *
   * @return String
   */
  public String getEncryptedBase64() {
    try {
      return new FASFEncrypter().encryptFASFIdentity(this);
    } catch (Exception ex) {
      return null;
    }
  }

  public String toString() {
    return getUsername() + " [" + getRequestHost() + "] "+getCreated();
  }
}
