package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.adaptermanager;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.DataNucleusObjectStore;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.PojoRecreator;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.PojoRecreatorDefault;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;

public class DataNucleusPojoRecreator implements PojoRecreator {

    private final PojoRecreator delegate = new PojoRecreatorDefault();
    
    @Override
    public Object recreatePojo(TypedOid oid) {
        if(oid.isTransient()) {
            return delegate.recreatePojo(oid);
        }
        return getObjectStore().loadPojo(oid);
    }

    
    @Override
    public ObjectAdapter lazilyLoaded(Object pojo) {
        return getObjectStore().lazilyLoaded(pojo);
    }

    ///////////////////////////////
    

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected DataNucleusObjectStore getObjectStore() {
        return (DataNucleusObjectStore) getPersistenceSession().getObjectStore();
    }

    
    
    
}

