package amgen.ri.ssh;

import java.io.IOException;
import java.io.InputStream;

import amgen.ri.crypt.StringEncrypter;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

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
public abstract class AbstractSSH {
    private String user;
    private String password;
    private String host;

    public AbstractSSH(String user, String password, String host) {
        this.user= user;
        this.password= decryptPassword(password);
        this.host= host;
    }
    public AbstractSSH(String host, String password) {
        this(host.substring(0, host.indexOf('@')), password, host.substring(host.indexOf('@') + 1));
    }

    private String decryptPassword(String password) {
        if (password == null) {
            return null;
        }
        if (!password.startsWith("!")) {
            return password;
        }
        try {
            return new StringEncrypter().decrypt(password.substring(1));
        } catch (Exception ex) {
            return password;
        }
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    protected Session connect() throws JSchException {
        JSch jsch = new JSch();
        jsch.setConfig("StrictHostKeyChecking", "no");
        Session session = jsch.getSession(user, host, 22);

        // username and password will be given via UserInfo interface.
        session.setUserInfo(new UserInfo() {
            public String getPassphrase() {
                return null;
            }

            public String getPassword() {
                return password;
            }

            public boolean promptPassword(String message) {
                return true;
            }

            public boolean promptPassphrase(String message) {
                return true;
            }

            public boolean promptYesNo(String message) {
                return true;
            }

            public void showMessage(String message) {
            }
        });
        session.connect();
        return session;
    }


    protected int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if (b == 0) {
            return b;
        }
        if (b == -1) {
            return b;
        }

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append( (char) c);
            } while (c != '\n');
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }
}
