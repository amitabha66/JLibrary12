package amgen.ri.crypt;

import amgen.ri.util.ExtBase64;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;

/**
 * Simple String Encryption/Decryption class.
 *
 */
public class StringEncrypter {
  //Available schemes
  public static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
  public static final String DES_ENCRYPTION_SCHEME = "DES";
  //The crypt key
  private KeySpec keySpec;
  //Key factory
  private SecretKeyFactory keyFactory;
  //Cipher for the scheme
  private Cipher cipher;
  //Encoding
  private static final String UNICODE_FORMAT = "UTF8";

  /**
   * StringEncrypter constructor which uses the DESede encryption scheme
   *
   * @throws EncryptionException
   */
  public StringEncrypter() throws EncryptionException {
    this(DESEDE_ENCRYPTION_SCHEME);
  }

  /**
   * StringEncrypter constructor which uses the provided encryption scheme
   *
   * @param encryptionScheme String
   * @throws EncryptionException
   */
  public StringEncrypter(String encryptionScheme) throws EncryptionException {
    //Super-secret decoder key
    this(encryptionScheme, "This is Jeff's default encryption key");
  }

  /**
   * StringEncrypter constructor which uses the provided encryption scheme and
   * key
   *
   * @param encryptionScheme String
   * @param encryptionKey String
   * @throws EncryptionException
   */
  public StringEncrypter(String encryptionScheme, String encryptionKey) throws EncryptionException {
    if (encryptionKey == null) {
      throw new IllegalArgumentException("encryption key was null");
    }
    if (encryptionKey.trim().length() < 24) {
      throw new IllegalArgumentException(
              "encryption key was less than 24 characters");
    }
    try {
      byte[] keyAsBytes = encryptionKey.getBytes(UNICODE_FORMAT);

      if (encryptionScheme.equals(DESEDE_ENCRYPTION_SCHEME)) {
        keySpec = new DESedeKeySpec(keyAsBytes);
      } else if (encryptionScheme.equals(DES_ENCRYPTION_SCHEME)) {
        keySpec = new DESKeySpec(keyAsBytes);
      } else {
        throw new IllegalArgumentException("Encryption scheme not supported: "
                + encryptionScheme);
      }
      keyFactory = SecretKeyFactory.getInstance(encryptionScheme);
      cipher = Cipher.getInstance(encryptionScheme);
    } catch (InvalidKeyException e) {
      throw new EncryptionException(e);
    } catch (UnsupportedEncodingException e) {
      throw new EncryptionException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new EncryptionException(e);
    } catch (NoSuchPaddingException e) {
      throw new EncryptionException(e);
    }
  }

  /**
   * Encrypts the given string
   *
   * @param unencryptedString String
   * @return String
   * @throws EncryptionException
   */
  public String encrypt(String unencryptedString) throws EncryptionException {
    if (unencryptedString == null || unencryptedString.trim().length() == 0) {
      throw new IllegalArgumentException(
              "unencrypted string was null or empty");
    }
    try {
      SecretKey key = keyFactory.generateSecret(keySpec);
      cipher.init(Cipher.ENCRYPT_MODE, key);
      byte[] cleartext = unencryptedString.getBytes(UNICODE_FORMAT);
      byte[] ciphertext = cipher.doFinal(cleartext);
      //BASE64Encoder base64encoder = new BASE64Encoder();
      //return base64encoder.encode(ciphertext);

      return ExtBase64.encodeToString(ciphertext, false);

    } catch (Exception e) {
      throw new EncryptionException(e);
    }
  }

  /**
   * Decrypts the given encrypted text
   *
   * @param encryptedString String
   * @return String
   * @throws EncryptionException
   */
  public String decrypt(String encryptedString) throws EncryptionException {
    if (encryptedString == null || encryptedString.trim().length() <= 0) {
      throw new EncryptionException(new IllegalArgumentException("encrypted string was null or empty"));
    }
    try {
      SecretKey key = keyFactory.generateSecret(keySpec);
      cipher.init(Cipher.DECRYPT_MODE, key);
      //byte[] cleartext = base64decoder.decodeBuffer(encryptedString);
      byte[] cleartext = ExtBase64.decode(encryptedString);
      byte[] ciphertext = cipher.doFinal(cleartext);
      return new String(ciphertext);

    } catch (Exception e) {
      throw new EncryptionException(e);
    }
  }

  /**
   * An exception specific for the encryption class
   */
  public static class EncryptionException extends Exception {
    public EncryptionException(Throwable t) {
      super(t);
    }
  }

  /**
   * Example
   *
   * @param args String[]
   */
  public static void main(String[] args) {
    try {
      String inputText = (args.length == 0 ? "MDLITEST" : args[0]);
      String encryptedText = new StringEncrypter().encrypt("dedee");

      String decryptedText = new StringEncrypter().decrypt("VaE8zpX4YqfTGvH4AH2Hyw==");
      //String decryptedText2 = new StringEncrypter().decrypt(encryptedText);
      System.out.printf("%-15s\t%s\n%-15s\t%s\n%-15s\t%s\n", new Object[]{
                "Input Text:", inputText,
                "Encrypted:", encryptedText,
                "Decrypted:", decryptedText
              });
      System.out.println("VERYQUICKP" + " " + new StringEncrypter().encrypt("VERYQUICKP"));;

    } catch (EncryptionException e) {
      e.printStackTrace();
    }
  }
}
