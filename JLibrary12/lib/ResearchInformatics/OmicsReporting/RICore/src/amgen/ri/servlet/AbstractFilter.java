package amgen.ri.servlet;

import amgen.ri.util.Debug;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import amgen.ri.util.ExtString;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public abstract class AbstractFilter implements Filter {
  private FilterConfig filterConfig = null;
  private Map<String, String> filterParameters;
  private List<String> excludePatterns;

  public AbstractFilter() {
    filterParameters = new HashMap<String, String>();
    excludePatterns = new ArrayList<String>();
  }

  /**
   * Called by the web container to indicate to a filter that it is being
   * taken out of service.
   *
   */
  public void destroy() {
  }

  /**
   * The <code>doFilter</code> method of the Filter is called by the container
   * each time a request/response pair is passed through the chain due to a
   * client request for a resource at the end of the chain.
   * Subclasses should overwrite this method to do something.
   *
   * @param request ServletRequest
   * @param response ServletResponse
   * @param chain FilterChain
   * @throws IOException
   * @throws ServletException
   */
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    //Check for excludes
    try {
      for (String excludePattern : excludePatterns) {
        String file = new File(req.getRequestURL().toString()).getName();
        if (file.equals(excludePattern)) {
          chain.doFilter(request, response);
          return;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    doCustomFilter(request, response, chain);
  }

  public abstract void doCustomFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException;

  /**
   * Called by the web container to indicate to a filter that it is being
   * placed into service.
   * The default implementation loads the FilterConfig and the InitParameters.
   * Subclasses tha overwrite this method should call it if parameters are
   * needed.
   *
   * @param filterConfig FilterConfig
   * @throws ServletException
   */
  public void init(FilterConfig filterConfig) throws ServletException {
    this.filterConfig = filterConfig;
    Enumeration<String> initParams = filterConfig.getInitParameterNames();
    while (initParams.hasMoreElements()) {
      String initParam = initParams.nextElement();
      if (initParam.equals("excludePatterns")) {
        String[] patterns = filterConfig.getInitParameter(initParam).split(",");
        for (String pattern : patterns) {
          excludePatterns.add(pattern.trim());
        }
      } else {
        filterParameters.put(initParam, filterConfig.getInitParameter(initParam));
      }
    }

  }

  /**
   * getFilterConfig
   *
   * @return FilterConfig
   */
  public FilterConfig getFilterConfig() {
    return filterConfig;
  }

  /**
   * Filter init parameters as a Map
   *
   * @return Map
   */
  public Map<String, String> getFilterParameters() {
    return filterParameters;
  }

  /**
   * A single filter parameter or null if not available
   *
   * @param parameterName String
   * @return String
   */
  public String getFilterParameter(String parameterName) {
    return filterParameters.get(parameterName);
  }

  /**
   * Returns the FilterParameters as a Set spliting the value using the given
   * RegEx.
   * The returned Set is a LinkedHashSet, so the order is preserved.
   * Returns an empty Set if the parameter does not exist.
   *
   * @param paramName String
   * @param delimiterRegEx String
   * @return Set
   */
  protected Set<String> getFilterParameterAsSet(String paramName, String delimiterRegEx) {
    Set<String> values = new LinkedHashSet<String>();
    String paramValues = getFilterParameter(paramName);
    if (ExtString.hasLength(paramValues)) {
      String[] paramValuesSplit = paramValues.split(delimiterRegEx);
      values.addAll(Arrays.asList(paramValuesSplit));
    }
    return values;
  }
}
