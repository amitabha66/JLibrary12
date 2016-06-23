package amgen.ri.test;

import java.io.ByteArrayOutputStream;
import java.io.File;

import amgen.ri.ssh.SSHCopy;
import amgen.ri.ssh.SSHExec;

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
public class JSSH {
    public static void main(String[] arg) throws Exception {

        String user= "xxia";
        String host= "adapt";

        ByteArrayOutputStream out= new ByteArrayOutputStream();


        new SSHCopy(user+"@"+host, "Amgen295").copyFileToRemote(new File("C:/test.smi"), "/common/xxia/tmp/ResearchGateway/rg2.smi", "0755");
        new SSHExec(user+"@"+host, "Amgen295").exec("/home/xxia/ResearchGateway/rundaylight.csh "+ "/common/xxia/tmp/rg2.smi", out);

        out.flush();
        out.close();

        System.out.println(new String(out.toByteArray()));

    }
}
