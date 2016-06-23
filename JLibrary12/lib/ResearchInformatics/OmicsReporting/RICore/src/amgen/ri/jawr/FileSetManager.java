package amgen.ri.jawr;

import amgen.ri.html.GenericHTMLElement;
import amgen.ri.html.HTMLElement;
import amgen.ri.util.ExtString;
import amgen.ri.xml.ExtXMLElement;
import amgen.ri.xml.XMLElement;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

/**
 * <p>@version $Id: FileSetManager.java,v 1.2 2013/03/20 23:05:29 jemcdowe Exp
 * $</p>
 *
 * <p> </p>
 *
 * <p> </p>
 *
 * <p> </p> not attributable
 */
public class FileSetManager {
  public static String CONFIG_FILENAME = "/jawr_filesets.xml";
  private HttpServletRequest request;
  private Document fileSetDoc;

  public FileSetManager(HttpServletRequest request) throws FileNotFoundException {
    this.request = request;
    fileSetDoc = ExtXMLElement.toDocument(this.getClass(), CONFIG_FILENAME);
  }

  public void writeScripts(Writer writer, String fileSetName) {
    HTMLElement parentEl = new GenericHTMLElement("parent");
    appendScripts(parentEl, fileSetName);
    for (XMLElement scriptEl : parentEl.getMembers()) {
      scriptEl.write(writer);
    }
  }

  public void writeStyles(Writer writer, String fileSetName) {
    HTMLElement parentEl = new GenericHTMLElement("parent");
    appendStyles(parentEl, fileSetName);
    for (XMLElement cssLinkEl : parentEl.getMembers()) {
      cssLinkEl.write(writer);
    }
  }

  public void appendScripts(HTMLElement parentEl, String fileSetName) {
    JAWRLinkRenderer jawrLinkRenderer = new JAWRLinkRenderer(request, false);
    Element fileSetEl = ExtXMLElement.getXPathElement(fileSetDoc, "//JSFileSets/FileSet[@name='" + fileSetName + "']");
    if (fileSetEl != null) {
      String fileSetBaseDir = fileSetEl.getAttributeValue("baseDir");
      List<Element> fileListEls = fileSetEl.getChildren("filelist");
      for (Element fileListEl : fileListEls) {
        String dir = fileListEl.getAttributeValue("dir");
        List<Element> fileEls = fileListEl.getChildren("file");
        for (Element fileEl : fileEls) {
          String fileName = fileEl.getAttributeValue("name");
          boolean isBundle = ExtXMLElement.getAttributeBoolean(fileEl, "bundle");
          if (ExtString.hasLength(fileName)) {
            if (isBundle) {
              jawrLinkRenderer.appendScriptLinks(fileName, parentEl);
            } else {
              if (!fileName.startsWith("http://")) {
                StringBuilder filePath = new StringBuilder(request.getContextPath());
                if (ExtString.hasLength(fileSetBaseDir)) {
                  filePath.append(fileSetBaseDir + "/");
                }
                if (ExtString.hasLength(dir)) {
                  filePath.append(dir + "/");
                }
                filePath.append(fileName);
                fileName = filePath.toString();
              }
              HTMLElement script = new GenericHTMLElement("script");
              script.addAttribute("type", "text/javascript");
              script.addAttribute("src", fileName);
              parentEl.addMemberElement(script);
            }
          }
        }
      }
    } else {
      System.err.println("Unable to find JS FileSet " + fileSetName);
    }
  }

  public void appendStyles(HTMLElement parentEl, String fileSetName) {
    JAWRLinkRenderer jawrLinkRenderer = new JAWRLinkRenderer(request, false);
    Element fileSetEl = ExtXMLElement.getXPathElement(fileSetDoc, "//CSSFileSets/FileSet[@name='" + fileSetName + "']");
    if (fileSetEl != null) {
      String fileSetBaseDir = fileSetEl.getAttributeValue("baseDir");
      List<Element> fileListEls = fileSetEl.getChildren("filelist");
      for (Element fileListEl : fileListEls) {
        String dir = fileListEl.getAttributeValue("dir");
        List<Element> fileEls = fileListEl.getChildren("file");
        for (Element fileEl : fileEls) {
          String fileName = fileEl.getAttributeValue("name");
          boolean isBundle = ExtXMLElement.getAttributeBoolean(fileEl, "bundle");
          if (ExtString.hasLength(fileName)) {
            if (isBundle) {
              jawrLinkRenderer.appendStyleLinks(fileName, parentEl);
            } else {
              if (!fileName.startsWith("http://")) {
                StringBuilder filePath = new StringBuilder(request.getContextPath());
                if (ExtString.hasLength(fileSetBaseDir)) {
                  filePath.append(fileSetBaseDir + "/");
                }
                if (ExtString.hasLength(dir)) {
                  filePath.append(dir + "/");
                }
                filePath.append(fileName);
                fileName = filePath.toString();
              }
              HTMLElement cssLink = new GenericHTMLElement("link");
              cssLink.addAttribute("rel", "stylesheet");
              cssLink.addAttribute("type", "text/css");
              cssLink.addAttribute("href", fileName);
              parentEl.addMemberElement(cssLink);
            }
          }
        }
      }
    } else {
      System.err.println("Unable to find CSS FileSet " + fileSetName);
    }

  }

  public Map<String, String> getContent(String fileSetName) {
    Map<String, String> contentMap = new LinkedHashMap<String, String>();
    Element fileSetEl = ExtXMLElement.getXPathElement(fileSetDoc, "//ContentFileSets/FileSet[@name='" + fileSetName + "']");
    if (fileSetEl != null) {
      String fileSetBaseDir = fileSetEl.getAttributeValue("baseDir");
      List<Element> fileListEls = fileSetEl.getChildren("filelist");
      for (Element fileListEl : fileListEls) {
        String dir = fileListEl.getAttributeValue("dir");
        List<Element> fileEls = fileListEl.getChildren("file");
        for (Element fileEl : fileEls) {
          String name= fileEl.getAttributeValue("name");
          StringBuilder filePath = new StringBuilder();
          if (ExtString.hasLength(fileSetBaseDir)) {
            filePath.append(fileSetBaseDir + "/");
          }
          if (ExtString.hasLength(dir)) {
            filePath.append(dir + "/");
          }
          filePath.append(name);
          
          File contentFile= new File(request.getServletContext().getRealPath(filePath.toString()));
          if (!contentFile.exists()) {
            contentFile= new File(filePath.toString());
          }
          if (contentFile.exists()) {
            try {
              contentMap.put(fileSetName+":"+name, FileUtils.readFileToString(contentFile));
            } catch (IOException ex) {
              Logger.getLogger(FileSetManager.class.getName()).log(Level.SEVERE, null, ex);
            }
          }
        }
      }
    } else {
      System.err.println("Unable to find FileSet " + fileSetName);
    }
    return contentMap;
  }
}
