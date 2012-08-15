package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.adaptermanager;

import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.DataNucleusObjectStore;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.PojoRecreator;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;

public class DataNucleusPojoRecreator implements PojoRecreator {

    @Override
    public Object recreatePojo(TypedOid oid) {
        return getObjectStore().loadPojo(oid);
    }

    ///////////////////////////////
    

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected DataNucleusObjectStore getObjectStore() {
        return (DataNucleusObjectStore) getPersistenceSession().getObjectStore();
    }
    
    
}

