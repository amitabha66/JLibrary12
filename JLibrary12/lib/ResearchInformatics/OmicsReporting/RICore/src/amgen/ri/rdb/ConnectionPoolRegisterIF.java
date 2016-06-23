package amgen.ri.rdb;

/**
 * <p> Dynamic Relation Db Mapping</p>
 * <p>Description: The ConnectionPoolRegister allows a list of connection pools to be associated to class types. The
 * implementing class must provide the mapping via the only method getConnectionPoolForClass(Class className).
 * The RdbData class requests this for any class which has its connection pool set to null</p>
 * @author Jeffrey McDowell
 * @version 1.0
 */

public interface ConnectionPoolRegisterIF {
    public String getConnectionPoolForClass(Class className);
}
