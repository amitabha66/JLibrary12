package amgen.ri.log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class RemoteClient {
    public static void main(String[] args) throws Exception {
        Socket s;
        while (true) {
            try {
                s = new Socket("ussf235313.am.corp.amgen.com", 4554);
                break;
            } catch (java.net.ConnectException e) { // Assume that the host isn't available yet, wait
                // a moment, then try again.
                Thread.currentThread().sleep(50);
            }
        }

        BufferedReader in = new BufferedReader(
            new InputStreamReader(s.getInputStream()));

        String line;
        while ( (line = in.readLine()) != null) {
            System.err.println(line);
        }
    }
}
