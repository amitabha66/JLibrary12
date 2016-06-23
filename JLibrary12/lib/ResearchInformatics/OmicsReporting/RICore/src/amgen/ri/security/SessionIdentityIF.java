package amgen.ri.security;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: </p>
 *
 * <p>Company: </p>
 *
 * @author J. McDowell
 * @version $Id
 */
public interface SessionIdentityIF extends IdentityIF {
    public String getRequestHost();
    public String getEncryptedBase64();
    public Object clone();
}
