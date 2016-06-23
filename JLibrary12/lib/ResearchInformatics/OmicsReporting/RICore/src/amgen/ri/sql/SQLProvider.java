package amgen.ri.sql;

/**
 * Interface to return a SQLQuery for a name
 *
 * @version $Id: SQLProvider.java,v 1.1 2011/10/26 04:13:33 cvs Exp $
 */
public interface SQLProvider {
    /**
     * Returns a SQLQuery given its name
     *
     * @param name String
     * @return SQLQuery
     */
    public SQLQuery getSQLQuery(String name);
}
