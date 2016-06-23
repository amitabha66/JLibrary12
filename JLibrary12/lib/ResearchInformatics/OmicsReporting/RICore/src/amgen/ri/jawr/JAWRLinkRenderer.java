package amgen.ri.jawr;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;
import net.jawr.web.resource.bundle.renderer.BundleRendererContext;
import net.jawr.web.resource.bundle.renderer.CSSHTMLBundleLinkRenderer;
import net.jawr.web.resource.bundle.renderer.JavascriptHTMLBundleLinkRenderer;
import net.jawr.web.servlet.RendererRequestUtils;
import amgen.ri.html.HTMLElement;
import amgen.ri.servlet.ServletBase;

/**
 * Creates an Object which will generate and write import tags for bundled
 * resources.
 *
 * @version $Id: JAWRLinkRenderer.java,v 1.2 2012/09/14 23:05:28 cvs Exp $
 */
public class JAWRLinkRenderer {
  private HttpServletRequest request;
  private boolean useRandomParam;

  /**
   * Creates a new JawrLinkRenderer object for the given AIGBase object
   *
   * @param requestor AIGBase
   */
  public JAWRLinkRenderer(HttpServletRequest request, boolean useRandomParam) {
    this.request = request;
    this.useRandomParam = useRandomParam;
  }

  /**
   * Creates a new JawrLinkRenderer object for the given AIGBase object
   *
   * @param requestor AIGBase
   */
  public JAWRLinkRenderer(ServletBase servletBase, boolean useRandomParam) {
    this(servletBase.getHttpServletRequest(), useRandomParam);
  }

  /**
   * Writes JavaScript JAWR bundle links to the given Writer
   *
   * @param scriptRequestName String
   * @param writer Writer
   * @throws IOException
   */
  public void writeScriptLinks(String scriptRequestName, Writer writer) throws IOException {
    ResourceBundlesHandler rsHandler = (ResourceBundlesHandler) request.getSession().getServletContext().getAttribute(JawrConstant.JS_CONTEXT_ATTRIBUTE);
    BundleRenderer jsRenderer = new JavascriptHTMLBundleLinkRenderer(rsHandler, useRandomParam);
    BundleRendererContext ctx = RendererRequestUtils.getBundleRendererContext(request, jsRenderer);
    jsRenderer.renderBundleLinks(scriptRequestName, ctx, writer);
    writer.flush();
  }

  /**
   * Writes CSS JAWR bundle links to the given Writer
   *
   * @param scriptRequestName String
   * @param writer Writer
   * @throws IOException
   */
  public void writeStyleLinks(String scriptRequestName, Writer writer) throws IOException {
    ResourceBundlesHandler rsHandler = (ResourceBundlesHandler) request.getSession().getServletContext().getAttribute(JawrConstant.CSS_CONTEXT_ATTRIBUTE);
    BundleRenderer cssRenderer = new CSSHTMLBundleLinkRenderer(rsHandler, useRandomParam, null, false, false, null);
    BundleRendererContext ctx = RendererRequestUtils.getBundleRendererContext(request, cssRenderer);
    cssRenderer.renderBundleLinks(scriptRequestName, ctx, writer);
    writer.flush();
  }

  /**
   * Appends JavaScript JAWR bundle links to the given HTMLElement parent
   *
   * @param scriptRequestName String
   * @param writer Writer
   * @throws IOException
   */
  public void appendScriptLinks(String scriptRequestName, HTMLElement parentEl) {
    ResourceBundlesHandler rsHandler = (ResourceBundlesHandler) request.getSession().getServletContext().getAttribute(JawrConstant.JS_CONTEXT_ATTRIBUTE);

    JSHTMLElementAppender jsRenderer = new JSHTMLElementAppender(parentEl, rsHandler, useRandomParam);
    BundleRendererContext ctx = RendererRequestUtils.getBundleRendererContext(request, jsRenderer);

    jsRenderer.renderBundleLinks(scriptRequestName, ctx);
  }

  /**
   * Writes CSS JAWR bundle links to the given Writer
   *
   * @param scriptRequestName String
   * @param writer Writer
   * @throws IOException
   */
  public void appendStyleLinks(String scriptRequestName, HTMLElement parentEl) {
    ResourceBundlesHandler rsHandler = (ResourceBundlesHandler) request.getSession().getServletContext().getAttribute(JawrConstant.CSS_CONTEXT_ATTRIBUTE);
    CSSHTMLElementAppender cssRenderer = new CSSHTMLElementAppender(parentEl, rsHandler, useRandomParam);
    BundleRendererContext ctx = RendererRequestUtils.getBundleRendererContext(request, cssRenderer);
    cssRenderer.renderBundleLinks(scriptRequestName, ctx);
  }
}

/**
 * A JAWR script renderer which appends Script tags to a parent HTMLElement
 * rather than writes them to a Writer
 */
class JSHTMLElementAppender extends JavascriptHTMLBundleLinkRenderer {
  private HTMLElement parentEl;

  /**
   * Creates a new JSHTMLElementAppender object with a provided Script parent
   * element
   *
   * @param parentEl HTMLElement
   * @param bundler ResourceBundlesHandler
   */
  public JSHTMLElementAppender(HTMLElement parentEl, ResourceBundlesHandler bundler, boolean useRandomParam) {
    super(bundler, useRandomParam);
    this.parentEl = parentEl;
  }

  /**
   * Overrides the renderBundleLinks method to append the parent HTMLElement
   * object rather than write to a Writer
   *
   * @param requestedPath String
   * @param ctx BundleRendererContext
   */
  public void renderBundleLinks(String requestedPath, BundleRendererContext ctx) {
    try {
      super.renderBundleLinks(requestedPath, ctx, new StringWriter());
    } catch (IOException ex) {
    }
  }

  /**
   * Overrides the renderLink method to append the parent HTMLElement object
   * rather than write to a Writer- Actually does the works.
   *
   * @param fullPath String
   * @return String
   */
  public String renderLink(String fullPath) {
    HTMLElement scriptEl = parentEl.addMemberElement("script");
    scriptEl.addAttribute("type", "text/javascript");
    scriptEl.addAttribute("src", fullPath);
    return fullPath;
  }
}

/**
 * A JAWR script renderer which appends CSS Link tags to a parent HTMLElement
 * rather than writes them to a Writer
 */
class CSSHTMLElementAppender extends CSSHTMLBundleLinkRenderer {
  private HTMLElement parentEl;

  /**
   * Creates a new JSHTMLElementAppender object with a provided CSS Link parent
   * element (probably the HEAD element)
   *
   * @param parentEl HTMLElement
   * @param bundler ResourceBundlesHandler
   */
  public CSSHTMLElementAppender(HTMLElement parentEl, ResourceBundlesHandler bundler, boolean useRandomParam) {
    super(bundler, useRandomParam, null, false, false, null);
    this.parentEl = parentEl;
  }

  /**
   * Overrides the renderBundleLinks method to append the parent HTMLElement
   * object rather than write to a Writer
   *
   * @param requestedPath String
   * @param ctx BundleRendererContext
   */
  public void renderBundleLinks(String requestedPath, BundleRendererContext ctx) {
    try {
      super.renderBundleLinks(requestedPath, ctx, new StringWriter());
    } catch (IOException ex) {
    }
  }

  /**
   * Overrides the renderLink method to append the parent HTMLElement object
   * rather than write to a Writer- Actually does the works.
   *
   * @param fullPath String
   * @return String
   */
  public String renderLink(String fullPath) {
    HTMLElement linkEl = parentEl.addMemberElement("link");
    linkEl.addAttribute("rel", "stylesheet");
    linkEl.addAttribute("type", "text/css");
    linkEl.addAttribute("href", fullPath);
    return fullPath;
  }
}
