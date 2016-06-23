/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package amgen.ri.oracle.encpwd;

import amgen.ri.crypt.StringEncrypter;
import amgen.ri.util.Debug;
import amgen.ri.util.ExtString;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.naming.*;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.jdbc.pool.OracleDataSourceFactory;

/**
 * An oracle data source factory that handles de-encrypting an encrypted
 * password.
 * 
 * @author jemcdowe
 */
public class OracleEncryptPasswdDataSourceFactory extends OracleDataSourceFactory {
  public OracleEncryptPasswdDataSourceFactory() {
    super();
  }

  @Override
  public Object getObjectInstance(Object refObj, Name name, Context nameCtx, Hashtable environment) throws Exception {
    Object o = super.getObjectInstance(refObj, name, nameCtx, environment);
    if (o != null && o instanceof PoolDataSource) {
      PoolDataSource ds = (PoolDataSource) o;
      ds.setValidateConnectionOnBorrow(false);
      String password = getReferenceAttr((Reference)refObj, "password");
      if (ExtString.hasLength(password)) {
        ds.setPassword(unencryptPassword(password));
        return ds;
      }
    }
    return o;
  }


  private String getReferenceAttr(Reference refObj, String attrName) throws NamingException {
    Enumeration enu = refObj.getAll();
    for (int i = 0; enu.hasMoreElements(); i++) {
      RefAddr addr = (RefAddr) enu.nextElement();
      if (ExtString.equalsIgnoreCase(addr.getType(), attrName)) {
        return addr.getContent().toString();
      }
    }
    return null;
  }


  private String unencryptPassword(String password) {
    try {
      if (password.startsWith("!")) {
        try {
          return new StringEncrypter().decrypt(password.substring(1));
        } catch (Exception e) {
        }
      }
      return new StringEncrypter().decrypt(password);
    } catch (Exception e) {
    }
    return password;
  }
}
