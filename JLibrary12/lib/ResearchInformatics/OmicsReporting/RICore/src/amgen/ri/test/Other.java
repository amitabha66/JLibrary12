package amgen.ri.test;

import amgen.ri.crypt.StringEncrypter;
import amgen.ri.json.JSONObject;
import amgen.ri.ldap.ActiveDirectoryEntry;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import oracle.jdbc.OracleResultSet;
import amgen.ri.oracle.OraConnectionManager;
import amgen.ri.oracle.OraSQLManager;
import amgen.ri.security.FASFEncrypter;
import amgen.ri.security.FASFIdentity;
import amgen.ri.util.*;
import static amgen.ri.util.ExtNet.getCanonicalHostName;
import amgen.ri.xml.ExtXMLElement;
import com.steadystate.css.parser.CSSOMParser;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.RequestTargetAuthentication;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.client.TargetAuthenticationStrategy;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;

/**
 * <p>
 * Title: Dynamic Relation Db Mapping</p>
 *
 * <p>
 * Description: </p>
 *
 * <p>
 * Copyright: Copyright (c) 2010</p>
 *
 * <p>
 * Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Other {

  public static void main(String[] args) throws Exception {
    URL url = new URL("http://uswa-dapp-dweb2.amgen.com:9090/demo/sdXML/demo_sd.xml");
    url = new URL("http://ussf2ua10711p9.am.corp.amgen.com:8084/demo/demo");

    Map<String, String> params = new HashMap<String, String>();
    params.put("ids", "1005-1");

    String s = "iipziJHQLC2vGTNJ2IAvE8DnC63hdnMWBQkO9gBwPb0/GZ5qhogi4NMWrfdrtVOyKqS2Lo4H4khZ1LYCmauBzi688qythZqHyebpAtMCxXx0zZdlM3CD37sPufjYOIrNraBLt41EM/pZ16l502SPqVxbFLk0mummUz1+kDmzVhpPlmGqnvk7QA==";

    ExtNet net = new ExtNet();
    
    BasicClientCookie cookie = new BasicClientCookie("identity", ExtString.escape(s));
    cookie.setPath("/");
    cookie.setDomain(".amgen.com");
    net.addCookie(cookie);

    String response = EntityUtils.toString(net.openHttpEntityViaPost(url, params));

    System.out.println(response);

  }
}
