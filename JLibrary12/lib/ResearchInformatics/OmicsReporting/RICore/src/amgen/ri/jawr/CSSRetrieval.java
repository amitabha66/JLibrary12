package amgen.ri.jawr;

import amgen.ri.servlet.ServletBase;
import amgen.ri.util.ExtString;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;
import net.jawr.web.resource.bundle.renderer.BundleRendererContext;
import net.jawr.web.resource.bundle.renderer.CSSHTMLBundleLinkRenderer;
import net.jawr.web.servlet.RendererRequestUtils;

/**
 * @version $Id: CSSRetrieval.java,v 1.2 2012/09/14 23:05:28 cvs Exp $
 */
public class CSSRetrieval extends ServletBase {
  public CSSRetrieval() {
    super();
  }

  public CSSRetrieval(HttpServletRequest req, HttpServletResponse resp) {
    super(req, resp);
  }

  public CSSRetrieval(HttpServletRequest req, HttpServletResponse resp, String responseEncoding) {
    super(req, resp, responseEncoding);
  }

  /**
   *
   * @param req HttpServletRequest
   * @param resp HttpServletResponse
   * @return TBXServlet @todo Implement this amgen.ri.servlet.ServletBase method
   */
  protected ServletBase getServlet(HttpServletRequest req, HttpServletResponse resp) {
    return new CSSRetrieval(req, resp);
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
    String cssRequest = null;
    try {
      String requestName = request.getRequestURI().replaceFirst(request.getContextPath(), "");
      requestName = requestName.replaceAll("\\/+", "/");
      cssRequest = requestName.replaceFirst("\\.cssx$", ".css");
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (ExtString.hasLength(cssRequest)) {
      ResourceBundlesHandler rsHandler = (ResourceBundlesHandler) context.getAttribute(JawrConstant.CSS_CONTEXT_ATTRIBUTE);
      BundleRenderer myJsRenderer = new MyCSSRenderer(rsHandler);
      BundleRendererContext ctx = RendererRequestUtils.getBundleRendererContext(request, myJsRenderer);
      StringWriter linkWriter = new StringWriter();
      myJsRenderer.renderBundleLinks(cssRequest, ctx, linkWriter);
      response.sendRedirect(linkWriter.toString());
    }
  }
}

class MyCSSRenderer extends CSSHTMLBundleLinkRenderer {
  public MyCSSRenderer(ResourceBundlesHandler bundler) {
    super(bundler, false, null, false, false, null);
  }

  public String renderLink(String fullPath) {
    return fullPath;
  }
}
