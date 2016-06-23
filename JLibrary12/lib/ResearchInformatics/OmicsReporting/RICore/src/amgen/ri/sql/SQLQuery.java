package amgen.ri.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import amgen.ri.xml.ExtXMLElement;

/**
 * A simple SQL query class which retrieves a saved SQL auery from an XML
 * document. The SQL query has a name child (Name element or name attribute) The
 * SQL code is in the code child Element. Any Variables are in the Variable
 * Elements
 *
 * @version $Id: SQLQuery.java,v 1.2 2012/03/08 06:32:42 cvs Exp $
 */
public class SQLQuery {
  private String name;
  private String sql;
  private List<Element> variableEls;
  private List<String> variables;
  private Map<String, String> attributes;

  public SQLQuery(Element sqlQueryEl) {
    attributes = new HashMap<String, String>();
    name = ExtXMLElement.findTextInElement(sqlQueryEl, "Name");
    sql = sqlQueryEl.getChildText("Code");
    variables = new ArrayList<String>();
    variableEls = ExtXMLElement.getXPathElements(sqlQueryEl, "Variable");
    for (Element variableEl : variableEls) {
      variables.add(variableEl.getText());
    }
    for (Object child : sqlQueryEl.getChildren("Attribute")) {
      if (child instanceof Element) {
        String name = ((Element) child).getAttributeValue("name");
        attributes.put(name, ((Element) child).getAttributeValue("value"));
      }
    }
  }

  public SQLQuery(String name, String sql, List<String> variables) {
    this.attributes = new HashMap<String, String>();
    this.variables = variables;
    this.variableEls = new ArrayList<Element>();
    this.name = name;
    this.sql = sql;
  }

  /**
   * Returns the SQL code
   *
   * @return String
   */
  public String getSql() {
    return sql;
  }

  /**
   * Returns the SQL with the given variable name replaced with the given value
   *
   * @param variable String
   * @param value String
   * @return String
   */
  public String getSQL(String variable, String value) {
    return getSql().replace(":" + variable, value);
  }

  /**
   * Returns the SQL with the given variable names replaced with the given
   * values provided in a Map
   *
   * @param variable String
   * @param value String
   * @return String
   */
  public String getSQL(Map<String, String> values) {
    String sql = getSql();
    for (String variable : values.keySet()) {
      sql = sql.replace(":" + variable, values.get(variable));
    }
    return sql;
  }

  /**
   * Returns the name of the SQL
   *
   * @return String
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the variables in the SQL, if any
   *
   * @return List
   */
  public List<Element> getVariableEls() {
    return variableEls;
  }

  /**
   * Returns the variables in the SQL, if any
   *
   * @return List
   */
  public List<String> getVariables() {
    return variables;
  }

  /**
   * Returns an attribute or null if not found
   *
   * @param attr String
   * @return String
   */
  public String getAttribute(String attr) {
    return attributes.get(attr);
  }
}
