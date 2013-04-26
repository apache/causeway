package org.apache.isis.objectstore.jdo.applib.service.support;

import org.apache.isis.applib.annotation.Programmatic;

/**
 * Service that provide a number of workarounds when using JDO/DataNucleus. 
 */
public interface IsisJdoSupport {

    /**
     * Force a reload (corresponding to the JDO <tt>PersistenceManager</tt>'s <tt>refresh()</tt> method)
     * of a domain objects.
     * 
     * <p>
     * In fact, this may just reset the lazy-load state of the domain object, but the effect is the same: 
     * to cause the object's state to be reloaded from the database.
     * 
     * <p>
     * The particular example that led to this method being added was a 1:m bidirectional relationship,
     * analogous to <tt>Customer <-> * Order</tt>.  Persisting the child <tt>Order</tt> object did not cause
     * the parent <tt>Customer</tt>'s collection of orders to be updated.  In fact, JDO does not make any
     * such guarantee to do so.  Options are therefore either to maintain the collection in code, or to
     * refresh the parent.
     */
    @Programmatic
    <T> T refresh(T domainObject);
}
