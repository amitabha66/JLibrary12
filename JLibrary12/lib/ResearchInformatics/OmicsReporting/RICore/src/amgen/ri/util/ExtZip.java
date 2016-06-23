/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package amgen.ri.util;

import java.io.*;
import java.net.URI;
import java.util.Deque;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author jemcdowe
 */
public class ExtZip {
  /**
   * Creates a Zipped archive file of a given directory
   *
   * @param directory
   * @param zipfile
   * @throws IOException
   */
  public static void zip(File directory, File zipfile) throws IOException {
    URI base = directory.toURI();
    Deque<File> queue = new LinkedList<File>();
    queue.push(directory);
    OutputStream out = new FileOutputStream(zipfile);
    Closeable res = out;
    try {
      ZipOutputStream zipOut = new ZipOutputStream(out);
      res = zipOut;
      while (!queue.isEmpty()) {
        directory = queue.pop();
        for (File file : directory.listFiles()) {
          String name = base.relativize(file.toURI()).getPath();
          if (file.isDirectory()) {
            queue.push(file);
            name = name.endsWith("/") ? name : name + "/";
            zipOut.putNextEntry(new ZipEntry(name));
          } else {
            zipOut.putNextEntry(new ZipEntry(name));
            FileInputStream in = new FileInputStream(file);
            copy(in, zipOut);
            in.close();
            zipOut.closeEntry();
          }
        }
      }
    } finally {
      res.close();
    }
  }

  /**
   * Creates a Zipped archive file of a given files
   *
   * @param files
   * @param zipfile
   * @throws IOException
   */
  public static void zip(File[] files, File zipfile) throws IOException {
    OutputStream out = new FileOutputStream(zipfile);
    Closeable res = out;
    try {
      ZipOutputStream zipOut = new ZipOutputStream(out);
      res = zipOut;
      for (File file : files) {        
        zipOut.putNextEntry(new ZipEntry(file.getName()));
        FileInputStream in = new FileInputStream(file);
        copy(in, zipOut);
        in.close();
        zipOut.closeEntry();
      }
    } finally {
      res.close();
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
  private static void copy(InputStream from, OutputStream to) throws IOException {
    InputStream in = null;
    OutputStream out = null;
    in = new BufferedInputStream(from);
    out = new BufferedOutputStream(to);
    while (true) {
      int data = in.read();
      if (data == -1) {
        break;
      }
      out.write(data);
    }
    out.flush();
  }
}
