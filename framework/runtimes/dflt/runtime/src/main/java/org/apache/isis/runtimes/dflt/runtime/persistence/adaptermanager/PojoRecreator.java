package org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;

public interface PojoRecreator {

    Object recreatePojo(final TypedOid oid);

    /**
     * Return an adapter, if possible, for a pojo that was instantiated by the
     * object store as a result of lazily loading, but which hasn't yet been seen
     * by the Isis framework.
     * 
     * <p>
     * For example, in the case of JDO object store, downcast to <tt>PersistenceCapable</tt>
     * and 'look inside' its state.
     */

    ObjectAdapter lazilyLoaded(Object pojo);
    
}
