package amgen.ri.jawr;

import java.io.File;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import net.jawr.web.resource.bundle.factory.util.ConfigPropertiesSource;
import net.jawr.web.resource.bundle.factory.util.ServletContextAware;

import org.jdom.Document;
import org.jdom.Element;

import amgen.ri.xml.ExtXMLElement;

/**
 * Creates a custom JAWR property source
 *
 * @version $Id: JAWRProperties.java,v 1.2 2012/07/30 22:58:46 cvs Exp $
 */
public class JAWRProperties implements ConfigPropertiesSource, ServletContextAware {
  /**
   * The JAWR XML property Document
   */
  protected Document jawrDefinitionDoc;

  public JAWRProperties() {
    jawrDefinitionDoc = ExtXMLElement.toDocument(getClass(), "/jawr_filesets.xml");
    if (jawrDefinitionDoc== null) {
      throw new IllegalArgumentException("Unable to load JAWR file sets xml");
    } else {
      System.out.println("using "+"/jawr_filesets.xml");
    }
  }

  /**
   * Determine if configuration is changed to reconfigure Jawr during
   * development without having to restart the server.
   *
   * @return boolean
   */
  public boolean configChanged() {
    return false;
  }

  /**
   * Read/modify configuration from the XML property file.
   *
   * @return java.util.Properties
   */
  public Properties getConfigProperties() {
    Properties jawrProperties = new Properties();
    if (jawrDefinitionDoc != null) {
      List<Element> propertyEls = ExtXMLElement.getXPathElements(jawrDefinitionDoc, "//BundleDefinitions//Property");
      for (Element propertyEl : propertyEls) {
        String name = propertyEl.getAttributeValue("name");
        if (name != null) {
          String value = ExtXMLElement.findTextInElement(propertyEl, "Value");
          if (value != null) {
            jawrProperties.put(name, value);
          }
        }
      }
    }
    return jawrProperties;
  }

  /**
   * Called by the JAWR framework. Used to set the JAWR property file from the
   * ServletContext
   *
   * @param context ServletContext
   */
  public void setServletContext(ServletContext context) {
  }
}
