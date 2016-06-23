package amgen.ri.rdb;

/**
 * An interface to hold method(s) which relate to a deferred field- one which is not loaded
 * by the database until its data is requested.
 */
public interface DeferredField {
    public boolean setData();

    public void setAllData();
}