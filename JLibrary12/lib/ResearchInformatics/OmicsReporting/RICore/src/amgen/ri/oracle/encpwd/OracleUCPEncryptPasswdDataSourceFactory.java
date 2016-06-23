/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package amgen.ri.oracle.encpwd;

import oracle.ucp.jdbc.PoolDataSourceImpl;

import amgen.ri.crypt.StringEncrypter;
import amgen.ri.util.Debug;
import amgen.ri.util.ExtString;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import oracle.ucp.UniversalConnectionPoolException;
import oracle.ucp.admin.UniversalConnectionPoolManager;
import oracle.ucp.admin.UniversalConnectionPoolManagerImpl;
import oracle.ucp.jdbc.PoolDataSource;

/**
 *
 * @author jemcdowe
 */
public class OracleUCPEncryptPasswdDataSourceFactory extends PoolDataSourceImpl {

    public OracleUCPEncryptPasswdDataSourceFactory() {
        super();
        try {
            UniversalConnectionPoolManager ucpManager = UniversalConnectionPoolManagerImpl.getUniversalConnectionPoolManager();
            ucpManager.setJmxEnabled(false);
        } catch (UniversalConnectionPoolException ex) {
        }
    }

    @Override
    public Object getObjectInstance(Object refObj, Name name, Context nameCtx, Hashtable environment) throws Exception {
        Object o = super.getObjectInstance(refObj, name, nameCtx, environment);
        if (o != null && o instanceof PoolDataSource) {
            PoolDataSource ds = (PoolDataSource) o;

            Reference ref = (Reference) refObj;
            ds.setAbandonedConnectionTimeout(getReferenceAttrInteger(ref, "abandonedConnectionTimeout", 300));
            ds.setMinPoolSize(getReferenceAttrInteger(ref, "minPoolSize", 2));
            ds.setMaxPoolSize(getReferenceAttrInteger(ref, "maxPoolSize", 100));
            ds.setInitialPoolSize(getReferenceAttrInteger(ref, "initialPoolSize", 2));
            ds.setInactiveConnectionTimeout(getReferenceAttrInteger(ref, "inactiveConnectionTimeout", 60));
            ds.setMaxConnectionReuseTime(60);
            ds.setMaxConnectionReuseCount(50);
            ds.setValidateConnectionOnBorrow(false);

            String password = getReferenceAttr(ref, "password");
/*
            String sn= name.toString();
            int intPoolSize = ds.getInitialPoolSize();
            int minPoolSize = ds.getMinPoolSize();
            int maxPoolSize = ds.getMaxPoolSize();
            int abandonedTime = ds.getAbandonedConnectionTimeout();
            int inactiveTime = ds.getInactiveConnectionTimeout();
            int wait = ds.getConnectionWaitTimeout();
            int available = ds.getAvailableConnectionsCount();
            int borrowed = ds.getBorrowedConnectionsCount();

            String s= String.format("Connection: %s\n\tinitial: %5d\n\tmin: %5d\n\tmax: %5d\n\tabandonTime: %5d\n\tinactiveTime: %5d\n\twait: %5d\n\tavail: %5d\n\tborrowed: %5d\n",
                    sn,
                    intPoolSize,
                    minPoolSize,
                    maxPoolSize,
                    abandonedTime,
                    inactiveTime,
                    wait,
                    available,
                    borrowed
            );
            System.out.println(s);
*/
            if (ExtString.hasLength(password)) {
                ds.setPassword(unencryptPassword(password));
                return ds;
            }
        }
        return o;
    }

    private int getReferenceAttrInteger(Reference refObj, String attrName, int defaultValue) throws NamingException {
        String value = getReferenceAttr(refObj, attrName);
        return (ExtString.isAInteger(value) ? ExtString.toInteger(value) : defaultValue);
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
