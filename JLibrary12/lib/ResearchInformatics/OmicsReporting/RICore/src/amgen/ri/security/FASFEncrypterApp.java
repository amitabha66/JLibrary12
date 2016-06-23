package amgen.ri.security;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

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
public class FASFEncrypterApp {
    public FASFEncrypterApp() {
        super();
    }

    public static void main(String[] args) {
        try {
            Options options = new Options();
            options.addOption("u", true, "Username");
            options.addOption("r", true, "Remote Host");
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(options, args);

            FASFIdentity identity = new FASFIdentity(cmd.getOptionValue("u", "none"), cmd.getOptionValue("r", "none"));
            String usernameCypherText = new FASFEncrypter().encrypt(cmd.getOptionValue("u", "none"));
            //String identityCypherText = new FASFEncrypter().encryptFASFIdentity(identity);
            String identityCypherText = new FASFEncrypter().encryptFASFdentityAsString(identity);
            System.out.println(usernameCypherText);
            System.out.println(identityCypherText);            

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
