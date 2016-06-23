package amgen.ri.sql;

import java.io.File;
import java.io.InputStream;

import org.jdom.Document;
import org.jdom.Element;

import amgen.ri.xml.ExtXMLElement;

/**
 * Creates a basic SQLProvider implementation which take a File as input.
 * This assumes an XML structure with SQLQuery elements
 *
 * @author J. McDowell
 * @version $Id
 */
public class GenericSQLProvider implements SQLProvider {
  private Document sqlXMLDoc;

  /**
   * Empty GenericSQLProvider- no Document is set. This must be set before
   * using by calling one of the setSQLXMLDocument methods
   */
  protected GenericSQLProvider() {
  }

  /**
   * Creates a GenericSQLProvider setting the SQL XML Document
   *
   * @param sqlXMLFile File
   */
  public GenericSQLProvider(File sqlXMLFile) {
    sqlXMLDoc = ExtXMLElement.toDocument(sqlXMLFile);
  }

  /**
   * Creates a GenericSQLProvider setting the SQL XML Document from the
   * ClassLoader of the given class
   *
   * @param clazz Class
   * @param sqlXMLFile File
   */
  public GenericSQLProvider(Class clazz, String sqlXMLFile) {
    sqlXMLDoc = ExtXMLElement.toDocument(clazz, sqlXMLFile);
  }

  /**
   * Creates a GenericSQLProvider setting the SQL XML Document from the
   * ClassLoader of the given class
   *
   * @param clazz Class
   * @param sqlXMLFile File
   */
  public GenericSQLProvider(Class clazz) {
    sqlXMLDoc = ExtXMLElement.toDocument(clazz, clazz.getName() + ".sql");
    if (sqlXMLDoc == null) {
      sqlXMLDoc = ExtXMLElement.toDocument(clazz, clazz.getName() + ".xml");
    }
  }

  /**
   * Creates a GenericSQLProvider setting the SQL XML Document
   *
   * @param sqlXMLFile String
   */
  public GenericSQLProvider(String sqlXMLFile) {
    this(new File(sqlXMLFile));
  }

  /**
   * Creates a GenericSQLProvider setting the SQL XML Document
   *
   * @param xmlFileInStream InputStream
   */
  public GenericSQLProvider(InputStream xmlFileInStream) {
    sqlXMLDoc = ExtXMLElement.toDocument(xmlFileInStream);
  }

  /**
   * Sets the SQL XML Document
   *
   * @param sqlXMLFile File
   */
  public void setSQLXMLDocument(File sqlXMLFile) {
    sqlXMLDoc = ExtXMLElement.toDocument(sqlXMLFile);
  }

  /**
   * Sets the SQL XML Document
   *
   * @param sqlXMLFile String
   */
  public void setSQLXMLDocument(String sqlXMLFile) {
    sqlXMLDoc = ExtXMLElement.toDocument(new File(sqlXMLFile));
  }

  /**
   * Sets the SQL XML Document
   *
   * @param xmlFileInStream InputStream
   */
  public void setSQLXMLDocument(InputStream xmlFileInStream) {
    sqlXMLDoc = ExtXMLElement.toDocument(xmlFileInStream);
  }

  /**
   * Sets the SQL XML Document
   *
   * @param xmlFileInStream InputStream
   */
  public void setSQLXMLDocument(Document sqlXMLDoc) {
    this.sqlXMLDoc = sqlXMLDoc;
  }

  /**
   * Returns the SQLQuery from the first encoutered Element
   * SQLQuery[@name=name] in the source Document or null
   *
   * @param name String
   * @return SQLQuery
   */
  public SQLQuery getSQLQuery(String name) {
    if (sqlXMLDoc != null) {
      Element sqlQueryEl = ExtXMLElement.getXPathElement(sqlXMLDoc, "//SQLQuery[@name='" + name + "']");
      if (sqlQueryEl != null) {
        return new SQLQuery(sqlQueryEl);
      }
    }
    return null;
  }
}
