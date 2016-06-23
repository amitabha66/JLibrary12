package amgen.ri.ssh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

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
public class SSHCopy extends AbstractSSH {
    public SSHCopy(String user, String password, String host) {
        super(user, password, host);
    }
    public SSHCopy(String host, String password) {
        super(host, password);
    }

    public void copyFileToRemote(File localFile, String remoteFile, String permissions) throws IOException,
        JSchException {
        if (permissions== null) {
            permissions= "0644";
        }
        JSch jsch = new JSch();
        jsch.setConfig("StrictHostKeyChecking", "no");
        Session session = connect();


        boolean ptimestamp = true;

        // exec 'scp -t rfile' remotely
        String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + remoteFile;
        Channel channel = session.openChannel("exec");
        ( (ChannelExec) channel).setCommand(command);

        // get I/O streams for remote scp
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();

        channel.connect();

        if (checkAck(in) != 0) {
            throw new IOException("Connect failed");
        }

        if (ptimestamp) {
            command = "T " + (localFile.lastModified() / 1000) + " 0";
            // The access time should be sent here,
            // but it is not accessible with JavaAPI ;-<
            command += (" " + (localFile.lastModified() / 1000) + " 0\n");
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
            throw new IOException("Change timestamp failed");
            }
        }

        // send "C0644 filesize filename", where filename should not include '/'
        long filesize = localFile.length();
        if (permissions != null) {
            command = "C" + permissions + " " + filesize + " ";
            if (localFile.getAbsolutePath().lastIndexOf('/') > 0) {
                command += localFile.getAbsolutePath().substring(localFile.getAbsolutePath().lastIndexOf('/') + 1);
            } else {
                command += localFile;
            }
            command += "\n";
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
            throw new IOException("Change permissions failed");
            }
        }

        // send a content of lfile
        FileInputStream fis = new FileInputStream(localFile);
        byte[] buf = new byte[1024];
        while (true) {
            int len = fis.read(buf, 0, buf.length);
            if (len <= 0) {
                break;
            }
            out.write(buf, 0, len); //out.flush();
        }
        fis.close();
        fis = null;
        // send '\0'
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();
        if (checkAck(in) != 0) {
            throw new IOException("Copy failed");
        }
        out.close();

        channel.disconnect();
        session.disconnect();

    }

    public void copyFileToRemote(InputStream in, String remoteFile, String permissions) throws IOException, JSchException {
        File tempFile= File.createTempFile("ssh", ".tmp");
        FileOutputStream out= new FileOutputStream(tempFile);
        byte[] b= new byte[1024];
        int len;
        while((len= in.read(b))> 0) {
            out.write(b, 0, len);
        }
        in.close();
        out.close();
        copyFileToRemote(tempFile, remoteFile, permissions);
        tempFile.delete();
    }

}
