package amgen.ri.rdb;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Dynamic Relation Db Mapping
 * Description: CLOB Wrapper
 * @author Jeffrey McDowell
 * @version 1.0
 */

public class ClobReader extends Reader {
    private ClobData clobData;
    private CharArrayReader charReader;

    public ClobReader(ClobData clobData) {
        this.clobData = clobData;
    }

    /**
     * Read characters into a portion of an array.  This method will block
     * until some input is available, an I/O error occurs, or the end of the
     * stream is reached.
     *
     * @param      cbuf  Destination buffer
     * @param      off   Offset at which to start storing characters
     * @param      len   Maximum number of characters to read
     *
     * @return     The number of characters read, or -1 if the end of the
     *             stream has been reached
     *
     * @exception  IOException  If an I/O error occurs
     */
    public int read(char cbuf[], int off, int len) throws IOException {
        if (charReader == null) {
            charReader = new CharArrayReader(clobData.getData());
        }
        return charReader.read(cbuf, off, len);
    }

    /**
     * Close the stream.  Once a stream has been closed, further read(),
     * ready(), mark(), or reset() invocations will throw an IOException.
     * Closing a previously-closed stream, however, has no effect.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void close() throws IOException {
        if (charReader == null) {
            return;
        }
        charReader.close();
        charReader = null;
    }

}
