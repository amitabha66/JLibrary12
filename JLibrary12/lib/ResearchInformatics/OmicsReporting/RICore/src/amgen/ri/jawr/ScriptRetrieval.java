package amgen.ri.jawr;

import amgen.ri.servlet.ServletBase;
import amgen.ri.util.ExtFile;
import amgen.ri.util.ExtString;
import java.io.File;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;
import net.jawr.web.resource.bundle.renderer.BundleRendererContext;
import net.jawr.web.resource.bundle.renderer.JavascriptHTMLBundleLinkRenderer;
import net.jawr.web.servlet.RendererRequestUtils;

/**
 * @version $Id: ScriptRetrieval.java,v 1.2 2012/09/14 23:05:28 cvs Exp $
 */
public class ScriptRetrieval extends ServletBase {
  public ScriptRetrieval() {
    super();
  }

  public ScriptRetrieval(HttpServletRequest req, HttpServletResponse resp) {
    super(req, resp);
  }

  public ScriptRetrieval(HttpServletRequest req, HttpServletResponse resp, String responseEncoding) {
    super(req, resp, responseEncoding);
  }

  /**
   *
   * @param req HttpServletRequest
   * @param resp HttpServletResponse
   * @return TBXServlet @todo Implement this amgen.ri.servlet.ServletBase method
   */
  protected ServletBase getServlet(HttpServletRequest req, HttpServletResponse resp) {
    return new ScriptRetrieval(req, resp);
  }

  /**
   *
   * @return String @todo Implement this amgen.ri.servlet.ServletBase method
   */
  protected String getServletMimeType() {
    return "text/plain";
  }

  /**
   *
   * @throws Exception @todo Implement this amgen.ri.servlet.ServletBase method
   */
  protected void performRequest() throws Exception {
    String jsRequest = null;
    try {
      String requestName = request.getRequestURI().replaceFirst(request.getContextPath(), "");
      requestName = requestName.replaceAll("\\/+", "/");
      jsRequest = requestName.replaceFirst("\\.jsx$", ".js");
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (ExtString.hasLength(jsRequest)) {
      if (doesParameterExist("debug")) {
        jsRequest = request.getSession().getServletContext().getRealPath(request.getRequestURI()).replaceFirst("\\.jsx$", ".js");
        File script = new File(jsRequest);
        ExtFile.writeFile(script, response.getOutputStream());
      } else {
        ResourceBundlesHandler rsHandler = (ResourceBundlesHandler) context.getAttribute(JawrConstant.JS_CONTEXT_ATTRIBUTE);        
        BundleRenderer myJsRenderer = new MyJSRenderer(rsHandler, false);
        BundleRendererContext ctx = RendererRequestUtils.getBundleRendererContext(request, myJsRenderer);
        StringWriter linkWriter = new StringWriter();
        myJsRenderer.renderBundleLinks(jsRequest, ctx, linkWriter);
        String redirectLink = linkWriter.toString();
        if (getParameter("refreshKey") != null) {
          if (redirectLink.indexOf('?') > 0) {
            redirectLink = redirectLink + "&refreshKey=" + getParameter("refreshKey");
          } else {
            redirectLink = redirectLink + "?refreshKey=" + getParameter("refreshKey");
          }
        }
        response.sendRedirect(redirectLink);
      }
    }
  }
}

class MyJSRenderer extends JavascriptHTMLBundleLinkRenderer {
  public MyJSRenderer(ResourceBundlesHandler bundler, boolean useRandomParam) {
    super(bundler, useRandomParam);
  }

  public String renderLink(String fullPath) {
    return fullPath;
  }
}
