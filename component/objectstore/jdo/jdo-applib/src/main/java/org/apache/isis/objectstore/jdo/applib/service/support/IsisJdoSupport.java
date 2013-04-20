package org.apache.isis.objectstore.jdo.applib.service.support;

/**
 * Service that provide a number of workarounds when using JDO/DataNucleus. 
 */
public interface IsisJdoSupport {

    /**
     * Inject services and container into a domain object.
     * 
     * <p>
     * This method is provided because we can't figure out why JDO/DataNucleus is not 
     * calling the Isis callback (<tt>IsisLifecycleListener</tt>) when an object is lazily loaded.
     * 
     * <p>
     * The particular example that led to this method being added was a 1:m bidirectional relationship,
     * analogous to <tt>Customer <-> * Order</tt>.  No callback is received for the child <tt>Order</tt> objects. 
     */
    <T> T injected(T domainObject);

    /**
     * Force a reload (corresponding to the JDO <tt>PersistenceManager</tt>'s <tt>refresh()</tt> method)
     * of a domain objects.
     * 
     * <p>
     * In fact, may reset the lazy-load state of the domain object, but the effect is the same: to cause
     * the object's state to be reloaded from the database.
     * 
     * <p>
     * The particular example that led to this method being added was a 1:m bidirectional relationship,
     * analogous to <tt>Customer <-> * Order</tt>.  Persisting the child <tt>Order</tt> object did not cause
     * the parent <tt>Customer</tt>'s collection of orders to be updated.  Calling refresh on the parent
     * <tt>Customer</tt> does the trick, however. 
     */
    <T> T refresh(T domainObject);
}
