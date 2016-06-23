package amgen.ri.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import amgen.ri.crypt.StringEncrypter;
import amgen.ri.crypt.StringEncrypter.EncryptionException;
import amgen.ri.util.ExtBase64;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple encryptiong for FASF processing
 *
 * @author not attributable
 * @version 1.0
 */
public class FASFEncrypter {

  public static final String ISO8601_DATEPATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";
  private static SimpleDateFormat ISO8601_DATEFORMATTER;

  static {
    ISO8601_DATEFORMATTER = new SimpleDateFormat(ISO8601_DATEPATTERN);
  }

  private StringEncrypter encrypter;

  public FASFEncrypter() throws EncryptionException {
    String key = "U2FsdGVkX19LJQGj5LRfX7uXjO6IfVutnLUimvUGOXPeDBEDEn5R8g";
    encrypter = new StringEncrypter(StringEncrypter.DES_ENCRYPTION_SCHEME, key);
  }

  public String encrypt(String text) throws EncryptionException {
    return encrypter.encrypt(text);
  }

  public String decrypt(String cypherText) throws EncryptionException {
    return encrypter.decrypt(cypherText);
  }

  public String encryptFASFIdentity(FASFIdentity identity) throws EncryptionException, IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ObjectOutputStream objOut = new ObjectOutputStream(new GZIPOutputStream(out));
    objOut.writeObject(identity);
    objOut.close();
    String identityEncoded = ExtBase64.encodeToString(out.toByteArray(), false);
    return encrypter.encrypt(identityEncoded);
  }

  public String encryptFASFdentityAsString(FASFIdentity identity) throws EncryptionException, IOException {
    String username = identity.getUsername();
    String requestHost = identity.getRequestHost();
    String sessionID = identity.getSessionID();
    Date created = identity.getCreated();
    Date lastAccess = identity.getLastAccess();
    Date sessionStart = identity.getSessionStart();

    String fasfString = username + "|"
            + requestHost + "|"
            + sessionID + "|"
            + ISO8601_DATEFORMATTER.format(created) + "|"
            + (lastAccess == null ? "null" : ISO8601_DATEFORMATTER.format(lastAccess)) + "|"
            + (sessionStart == null ? "null" : ISO8601_DATEFORMATTER.format(sessionStart));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ObjectOutputStream objOut = new ObjectOutputStream(new GZIPOutputStream(out));
    objOut.writeObject(fasfString.getBytes());
    objOut.close();
    String identityEncoded = ExtBase64.encodeToString(out.toByteArray(), false);
    return encrypter.encrypt(identityEncoded);
  }

  public FASFIdentity decryptFASFIdentity(String cypherText) throws EncryptionException, IOException, ClassNotFoundException {
    byte[] identityDecoded = ExtBase64.decode(decrypt(cypherText));
    if (identityDecoded == null || identityDecoded.length == 0) {
      return null;
    }
    ObjectInputStream objIn = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(identityDecoded)));
    Object obj = objIn.readObject();
    objIn.close();
    if (obj instanceof FASFIdentity) {
      return (FASFIdentity) obj;
    } else if (obj instanceof byte[]) {
      String fasfString = new String((byte[]) obj);
      String[] fasfFields = fasfString.split("\\|", 6);

      if (fasfFields.length == 6) {
        String username = fasfFields[0];
        String requestHost = fasfFields[1];
        String sessionID = fasfFields[2];
        Date created = decodeDate(fasfFields[3]);
        Date lastAccess = decodeDate(fasfFields[4]);
        Date sessionStart = decodeDate(fasfFields[5]);

        FASFIdentity identity = new FASFIdentity(username, requestHost);
        identity.setSessionID(sessionID);
        identity.setCreated(created.getTime());
        if (lastAccess != null) {
          identity.setLastAccess(lastAccess.getTime());
        }
        if (sessionStart != null) {
          identity.setSessionStart(sessionStart.getTime());
        }
        identity.setTransientAttributes("fasfToken", fasfString);
        return identity;
      }
    }
    return null;
  }

  private Date decodeDate(String d) {
    try {
      return ISO8601_DATEFORMATTER.parse(d);
    } catch (Exception e) {
    }
    return null;
  }
}
