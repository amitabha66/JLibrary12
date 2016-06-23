package amgen.ri.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;

/**
 * This class contains a set of static method utilities for operating on Files
 */
public class ExtFile {
  /**
   * Returns whether time between the now and the last time the file was
   * modified is greater than the given minutes
   *
   * @param file File
   * @param minutes long
   * @return boolean
   */
  public static boolean isOlderMinutes(File file, long minutes) {
    Date modified = new Date(file.lastModified());
    double millsDiff = System.currentTimeMillis() - modified.getTime();
    double minDiff = millsDiff / 60000;
    return (minDiff > minutes);
  }

  /**
   * Deletes the given File and all members if it is a directory. Returns true
   * if successful false if unable.
   */
  public static boolean deepdelete(File file) {
    if (file == null || !file.exists()) {
      return false;
    }
    File[] files = file.listFiles();
    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        if (files[i].isDirectory()) {
          deepdelete(files[i]);
        } else {
          if (!files[i].delete()) {
            return false;
          }
        }
      }
    }
    return file.delete();
  }

  /**
   * Reads a URL and given parameters added as a GET.
   *
   * @param url Reader
   * @param parameters Map
   * @param outWriter Writer
   * @return Writer
   * @throws IOException @todo Implement this amgen.ri.asf.sa.xslt.TransformerIF
   * method
   */
  public static Writer getURL(String url, Map<String, ?> parameters, Writer outWriter) throws IOException {
    StringBuffer paramBuffer = new StringBuffer();
    if (parameters != null) {
      for (String paramName : parameters.keySet()) {
        if (paramBuffer.length() > 0) {
          paramBuffer.append("&");
        }
        paramBuffer.append(URLEncoder.encode(paramName, "UTF-8") + "=" + URLEncoder.encode(parameters.get(paramName) + "", "UTF-8"));
      }
    }
    InputStream in = new URL(url + (paramBuffer.length() > 0 ? "?" + paramBuffer : "")).openStream();
    InputStreamReader reader = new InputStreamReader(in);
    char[] buffer = new char[1024];
    int charsRead;
    while ((charsRead = reader.read(buffer)) != -1) {
      outWriter.write(buffer, 0, charsRead);
    }
    reader.close();
    outWriter.flush();
    return outWriter;
  }

  /**
   * Reads a URL and given parameters added as a POST.
   *
   * @param url1 String
   * @param parameters Map
   * @param out OutputStream
   * @throws IOException
   */
  public static void postURL(String url, Map<String, ?> parameters, OutputStream out) throws IOException {
    // Pass-through the response from the connection InputStream to the
    // servlet OutputStream
    byte[] buffer = new byte[1024];
    int bytesRead;
    InputStream in = postURLInputStream(url, parameters);
    while ((bytesRead = in.read(buffer)) != -1) {
      out.write(buffer, 0, bytesRead);
    }
    in.close();
    out.flush();
  }

  /**
   * Returns the InputStream for a POST.
   *
   * @param url String
   * @param parameters Map
   * @throws IOException
   * @return InputStream
   */
  public static InputStream postURLInputStream(String url, Map<String, ?> parameters) throws IOException {
    StringBuffer postRequest = new StringBuffer();
    if (parameters != null) {
      for (String paramName : parameters.keySet()) {
        if (postRequest.length() > 0) {
          postRequest.append("&");
        }
        postRequest.append(URLEncoder.encode(paramName, "UTF-8") + "=" + URLEncoder.encode(parameters.get(paramName) + "", "UTF-8"));
      }
    }
    URL urlObj = new URL(url);
    HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();

    conn.setDoOutput(true);
    if (postRequest.length() > 0) {
      OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
      wr.write(postRequest.toString());
      wr.flush();
    }
    return conn.getInputStream();
  }

  /**
   * Returns the InputStream for a POST.
   *
   * @param url String
   * @param parameters Map
   * @throws IOException
   * @return InputStream
   */
  public static InputStream postURLInputStream(String url, Map<String, ?> parameters, Map<String, String> cookies) throws IOException {
    StringBuffer postRequest = new StringBuffer();
    if (parameters != null) {
      for (String paramName : parameters.keySet()) {
        if (postRequest.length() > 0) {
          postRequest.append("&");
        }
        postRequest.append(URLEncoder.encode(paramName, "UTF-8") + "=" + URLEncoder.encode(parameters.get(paramName) + "", "UTF-8"));
      }
    }
    URL urlObj = new URL(url);
    HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
    conn.setDoOutput(true);

    if (cookies != null) {
      List<String> cookieList = new ArrayList<String>();
      for (String name : cookies.keySet()) {
        cookieList.add(name + "=" + cookies.get(name));
      }

      conn.setRequestProperty("Cookie", ExtString.join(cookieList, ";"));
      System.out.println(ExtString.join(cookieList, ";"));
    }

    if (postRequest.length() > 0) {
      OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
      wr.write(postRequest.toString());
      wr.flush();
    }
    return conn.getInputStream();
  }

  /**
   * Reads a URL and given parameters added as a POST.
   *
   * @param url1 String
   * @param parameters Map
   * @param out OutputStream
   * @throws IOException
   * @return Writer
   */
  public static Writer postURL(String url, Map<String, ?> parameters, Writer out) throws IOException {
    // Pass-through the response from the connection InputStream to the
    // servlet OutputStream
    char[] buffer = new char[1024];
    int bytesRead;
    InputStreamReader in = new InputStreamReader(postURLInputStream(url, parameters), "UTF-8");
    while ((bytesRead = in.read(buffer)) != -1) {
      out.write(buffer, 0, bytesRead);

    }
    in.close();
    out.flush();
    return out;
  }

  /**
   * Reads a URL and given parameters added as a POST.
   *
   * @param url1 String
   * @param parameters Map
   * @param out OutputStream
   * @throws IOException
   * @return Writer
   */
  public static Writer postURL(String url, Map<String, ?> parameters, Map<String, String> cookies, Writer out) throws IOException {
    // Pass-through the response from the connection InputStream to the
    // servlet OutputStream
    char[] buffer = new char[1024];
    int bytesRead;
    InputStreamReader in = new InputStreamReader(postURLInputStream(url, parameters, cookies), "UTF-8");
    while ((bytesRead = in.read(buffer)) != -1) {
      out.write(buffer, 0, bytesRead);

    }
    in.close();
    out.flush();
    return out;
  }

  /**
   * Reads a URL and given parameters added as a POST and returns the results as
   * a String.
   *
   * @param url String
   * @param parameters Map
   * @throws IOException
   * @return Writer
   */
  public static String postURL(String url, Map<String, ?> parameters) throws IOException {
    StringWriter writer = (StringWriter) postURL(url, parameters, new StringWriter());
    writer.close();
    return writer.toString();
  }

  /**
   * Reads a text File and returns the String
   *
   * @param file File
   * @return String
   * @throws IOException
   */
  public static String readTextFile(File file) throws IOException {
    StringBuffer sb = new StringBuffer();
    FileReader reader = new FileReader(file);
    char[] buf = new char[1024];
    int count;
    while ((count = reader.read(buf)) > 0) {
      sb.append(buf, 0, count);
    }
    reader.close();
    return sb.toString();
  }

  /**
   * Writes to a String to a text file
   *
   * @param file File
   * @param contents String
   * @return String
   * @throws IOException
   */
  public static File writeTextFile(File file, String contents) throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    writer.write(contents);
    writer.flush();
    writer.close();
    return file;
  }

  /**
   * Writes a File to an OutputStseam
   *
   * @param file File
   * @param out String
   * @throws IOException
   */
  public static void writeFile(File file, OutputStream out) throws IOException {
    FileInputStream in = new FileInputStream(file);
    byte[] buf = new byte[1024];
    int count;
    while ((count = in.read(buf)) > 0) {
      out.write(buf, 0, count);
    }
    in.close();
    out.flush();
  }

  /**
   * Writes byte array to a File
   *
   * @param file File
   * @param out byte[]
   * @throws IOException
   */
  public static void writeFile(File file, byte[] bytes) throws IOException {
    FileOutputStream out = new FileOutputStream(file);
    out.write(bytes);
    out.close();
  }

  /**
   * Reads a text from a URL and returns the String
   *
   * @param url URL
   * @return String
   * @throws IOException
   */
  public static String readTextFile(URL url) throws IOException {
    StringBuffer sb = new StringBuffer();
    Reader reader = new InputStreamReader(url.openStream());
    char[] buf = new char[1024];
    int count;
    while ((count = reader.read(buf)) > 0) {
      sb.append(buf, 0, count);
    }
    reader.close();
    return sb.toString();
  }

  /**
   * Reads a File and returns the bytes
   *
   * @param file File
   * @return byte[]
   * @throws IOException
   */
  public static byte[] readFile(File file) throws IOException {
    return readStream(new FileInputStream(file));
  }

  /**
   * Reads an InputStream and returns the bytes
   *
   * @param in InputStream
   * @return byte[]
   * @throws IOException
   */
  public static byte[] readStream(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buf = new byte[1024];
    int count;
    while ((count = in.read(buf)) > 0) {
      out.write(buf, 0, count);
    }
    in.close();
    return out.toByteArray();
  }

  /**
   * Reads a stream from the URL and returns the bytes
   *
   * @param url URL
   * @throws IOException
   * @return byte[]
   */
  public static byte[] readStream(URL url) throws IOException {
    return readStream(url.openStream());
  }

  /**
   * Fast & simple file copy.
   *
   * @param source File
   * @param dest File
   * @throws IOException
   */
  public static void copy(File source, File dest) throws IOException {
    FileInputStream in = null;
    FileOutputStream out = null;
    try {
      in = new FileInputStream(source);
      out = new FileOutputStream(dest);
      copy(in, out);
    } finally {
      if (in != null) {
        in.close();
      }
      if (out != null) {
        out.close();
      }
    }
  }

  /**
   * Fast & simple file copy.
   *
   * @param source File
   * @param dest File
   * @throws IOException
   */
  public static void copyByChannels(File source, File dest) throws IOException {
    FileChannel in = null;
    FileChannel out = null;
    try {
      in = new FileInputStream(source).getChannel();
      out = new FileOutputStream(dest).getChannel();
      long size = in.size();
      MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);
      out.write(buf);
    } finally {
      if (in != null) {
        in.close();
      }
      if (out != null) {
        out.close();
      }
    }
  }

  /**
   * Copies data from the InputStream to the OutputStream using a buffered
   * streams
   *
   * @param from InputStream
   * @param to OutputStream
   * @throws IOException
   */
  public static OutputStream copy(InputStream from, OutputStream to) throws IOException {
    InputStream in = null;
    OutputStream out = null;
    try {
      in = new BufferedInputStream(from);
      out = new BufferedOutputStream(to);
      while (true) {
        int data = in.read();
        if (data == -1) {
          break;
        }
        out.write(data);
      }
    } finally {
      if (in != null) {
        in.close();
      }
      if (out != null) {
        out.close();
      }
    }
    return to;
  }

  /**
   * Copies data from the File to an output stream
   *
   * @param file
   * @param out
   * @throws IOException
   */
  public static void copy(File file, OutputStream out) throws IOException {
    InputStream in = new FileInputStream(file);
    try {
      copy(in, out);
    } finally {
      in.close();
    }
  }

  /**
   * Copies data from the InputStream to the output file using a buffered
   * streams
   *
   * @param from InputStream
   * @param to OutputStream
   * @throws IOException
   */
  public static void copy(InputStream from, File to) throws IOException {
    InputStream in = null;
    OutputStream out = null;
    try {
      in = new BufferedInputStream(from);
      out = new BufferedOutputStream(new FileOutputStream(to));
      while (true) {
        int data = in.read();
        if (data == -1) {
          break;
        }
        out.write(data);
      }
    } finally {
      if (in != null) {
        in.close();
      }
      if (out != null) {
        out.close();
      }
    }
  }

  /**
   * Get the extension of a file.
   */
  public static String getExtension(File f) {
    String ext = null;
    String s = f.getName();
    int i = s.lastIndexOf('.');

    if (i > 0 && i < s.length() - 1) {
      ext = s.substring(i + 1).toLowerCase();
    }
    return ext;
  }

  /**
   * Returns all files in a directory with an extensions matching one in the
   * given Collection. If extensions is null, it just returns all files in the
   * directory.
   *
   * @param dir File
   * @param extensions Collection
   * @return String[]
   */
  public static List<File> list(File dir, String[] extensions) {
    return list(dir, Arrays.asList(extensions));
  }

  /**
   * Returns all files in a directory with an extensions matching one in the
   * given Collection. If extensions is null, it just returns all files in the
   * directory.
   *
   * @param dir File
   * @param extensions Collection
   * @return List
   */
  public static List<File> list(File dir, final Collection<String> extensions) {
    final List<File> validFiles = new ArrayList<File>();
    if (dir.isDirectory()) {
      validFiles.addAll(Arrays.asList(dir.listFiles(new FilenameFilter() {
        public boolean accept(File parent, String fileName) {
          File f = new File(parent, fileName);
          if (f.isFile()) {
            String extension = getExtension(f);
            if (extensions == null || extensions.contains(extension)) {
              return true;
            }
          } else if (f.isDirectory()) {
            List<File> files = list(f, extensions);
            validFiles.addAll(files);
          }
          return false;
        }
      })));
    }
    return validFiles;
  }

  public static void main(String[] args) throws Exception {
    /*
     * HttpURLConnection conn = (HttpURLConnection) new URL("http://core-dev.amgen.com:9090/coreservices/compound/structure/v3000").openConnection();
     * List<String> cookies = new ArrayList<String>();
     * cookies.add("CoRe__v__1_1_9__IDENTITY="+
     * //URLDecoder.decode(
     * //URLDecoder.decode(
     * "jxTCTiYRsjWLTQwvgfzdFg%3D%3D"
     * //)
     * //)
     * );
     * cookies.add("SMSESSION="+
     * //URLDecoder.decode(
     * //URLDecoder.decode(
     * "5RQPKfIePS4JtcsmePO/NDCIvyg0sjRlZiXT/Lf6T3F+Zcaj1s5Ad852GbZT9HIk6P7JF1/iDJq5Hzq3kQvEco/hF6RUyHn8+RpywbzaINah4+W2vwlHtxdf+fgJVeUze41C+7y2xtoleni/38n4F1UK0becCZWEEpgez6HhDb4owj3mcBUuTo5owYWYgrJBomP+3GVFHRitieg6XjP1euBp6v4PzBWNvac2WKonKwxuOQC34gBZSM9yeAO6C2t3iWdBkw2axlbu2lm8EMwki2qr7Vuzs2UMrYfANT5f00vmvcidVTfnbqQHnR95yqSGQD3QiL/DRegRZMChH2mdxK57JqweU9VGrh0Rr6n8U9t66IAlEMNHZSlbBKwZAECc6ih+16S/qdca5VjsLyF88N6GT6oOGMRk2iDJwSL1oA8ECJBD6pQJN+BAmumbsGKhtgDAH9t9UCVwdiHCOW7RnPCVEpdQguBkRR+QC7SrWrVZ4SfbgF/LpkB1ZcEiZxgEWYtzU0SZhEOSqKdrAQRsg34Z/IlY3cZgerPDW6ljgGQgDsou3ztfYPa74jiL1HxUHxVvp+Qqe8fzxaWvy1KGqOyANLGCPN2K0r7XqESnQJ212+K+NwMmnRdnBw8sdobNRlh99rRrPwfk4rWYjhKmKDRfqXI9BIhTMIeZEfgKE5G/O7O8cUbJwLKoJwZwlT2RrVuS5wfWsmf12c9RPpyvl0aVHOOEbfZoTEDNamo3FJ1qUs4fRjfNFYWIVJ1R2qyavH/gfBVbLQRHu7wHNsKDSLtb98MoMBRG1Y5T4Tw9v/6uzleQlsrSPy3bEBehvXAX7JcwFGH5bxYy+heSJiXSkkTtjt26pfx4xeVp6EUmV1ff1f5LVZGuq4YkFhHUsU6Tdb0SOIOAl2xUAqPszzaM0i2kMenDS7haK/weuj87NkNbS+n7MGVPohWdu20ihp97bjJIrkOVQjlG47PQL7qi993SgKyVI3sGq/zUOLfAzmX4ZHTBCCoizcZ0nic6JM+/bvOdQgpTyG39rGsb6cJve4seaM4SwUHPbB6bca5wwtD2IsiHmG5AG6hAuAyKTi6f55an4q/I/s1hcp6C+zHX8akD+YL+L8w6niwhM8EGHp+ICqNKw9ac5hR9j/kNWxquH9DPICmACe5aAGWMfp0T5alhubABMSPR"
     * //)
     * //)
     * );
     * cookies.add("SMIDENTITY="+
     * //URLDecoder.decode(
     * // URLDecoder.decode(
     * "lcu52GLXm749BJEHACE2NCLyvhgmdQ9tZ7FvIMYxPs/TXGWibmkHIuYJpVxczDYoucckD1XG0Xu5QieTT6hZdliG57WpFHpX3XujM81nGeE7ClVSY5kSAzgJHxsjRqo+Hr//9yzYcVDsovD98o/MLXliep2IkZdBn95xFYSJQVD5qHC+ABFGpVgC5zbxKnDW2+KxZ8wCVouJod/0ydn/MqJl1kzM8aAC5fMS1wGBBCaRmJz8q5+eXrFUwwcuJGRGO10BXecie6QhJDFy+3OrP4tXeBbuulXaGMZcmmigMClHDYhcTTXTgrO7wI1zeUAmuM35atxM4o7Q2QVWcec/bPT2bEdHV7bdAcPYkqIJVXVnDYBgxlUH8aO43J4EULjaeJzyRFLojxl2koF908uvxUsoTn3qo1fazGxK77u3MyiEx2C8KerZtcfcsWLgVyrjf1tgHQHey16JvAisyZVRdkl4zzNK3mBAuQldatqHkpukd1hxCVv6Uj4oZoNdTjkpJ3tVirAOU15mAv8IAwRXNY9Z8R2PH64LRatCsAUwZtXs33TN1MXY1Wv0SazFgnBYHd+Z/onWCFrUXXoS1xW2gc/gUhT9I0GvFyrfg8JeF0ztA1fXFXD9dGbMjJXt3If95J7TQbk2On2hRvcN3iyi8Prukt5XbskCSYCwbTiWveriQIQbPm+do53Rhkw32e2GMWXqn6h5NUgALUSWYH5g+RO2o4slzfVOUjTbmNHK3t1a37Egbziy6/n8G085zhyz"
     * // )
     * //)
     * );
     * cookies.add("CoRe__v__1_1_9__SESSIONIDENTITY="+
     * //URLDecoder.decode(
     * //URLDecoder.decode(
     * "iipziJHQLC2BdqYzykKXVyCjflz1m%2B03ryf77leGdy%2FxpWwkQLBEpCjubteeteyPHpRXCaBy3R9SIyhYNvLtB%2BXzdG%2F7117lyT%2FM%2Fc1Mgk0cBIJCTkMjYoQbJb5MMCvzMgF7QYOAGeGZzC15Wd2sqOOZ%2F%2BR0rJLBJdCITarKRBS3xkM0QX%2FsvN3CMhwTbOZ8TPPzxXhSmRicQ8dVl46FKjQnIxrmyrKRt7%2Fs1LhQPlrcApOJS4jBm6TBekfmcMozjb0wxTH%2FG8r0BkMCxGdpvaem4%2BTUQ2iFSKUyufPNzB0vz%2Bs6sVGoro0aewHcURp615X83neQpyaALvLqcwXGf3JWfhJ42lXqSGUHFxbUqG7%2BrkMpDORYCtnm9KOceiZRBS%2Fp%2B%2BkVaHahXPJGUwbeyt%2FgJIVlmH2Okat1tSfhV0ATauRIGV9XansjnRE0oTfzraaLWST2jbrfb%2F7ulVXgyzdZHdJJlv2kW9KES%2FRzgTJBzdnYbXQjFbWkENRH72Vg%2FxecS5VqtSc1yp7ZPP%2BaY5Ci8YcBLbB9EHfpfIyt%2F5%2B1b0ev49j%2BSMBq3s0Cle5x1XTULUjDrwFA0qrFXQmhpuwZfG3s2xdJ%0A"
     * // )
     * //)
     * );
     * if (cookies.size() > 0) {
     * conn.setRequestProperty("Cookie", ExtString.join(cookies, "; "));
     * }
     * //Debug.print(ExtString.join(cookies, ";"));
     */
    HttpURLConnection conn = (HttpURLConnection) new URL("http://core-dev.amgen.com:9090/coreservices/compound/structure/v3000QP?id=14019").openConnection();
    conn.setDoOutput(true);
    conn.setRequestProperty("Content-Type", "text/plain"); 
    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
    wr.write("id=14019");
    wr.close();
    conn.getInputStream();
    
    
    ByteArrayOutputStream response = (ByteArrayOutputStream)copy(conn.getInputStream(), new ByteArrayOutputStream());
    System.out.println(new String(response.toByteArray()));
    
    
    
    /*
     *
     * Map<String, String> p= new HashMap<String, String>();
     * p.put("id", "14019");
     * //String s=
     * postURL("http://core-dev.amgen.com:9090/coreservices/compound/structure/v3000",
     * p);
     * //System.out.println(s);
     *
     * ExtNet net= new ExtNet("am", "jemcdowe", "Ra1n1ng#");
     * HttpEntity s= net.openHttpEntityViaPost(new
     * URL("http://core-dev.amgen.com:9090/coreservices/compound/structure/v3000"),
     * p);
     * s.writeTo(System.out);
     * System.out.flush();
     */
  }
}
