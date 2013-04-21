package org.apache.isis.objectstore.jdo.datanucleus.service.support;

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.objectstore.jdo.applib.service.support.IsisJdoSupport;
import org.apache.isis.objectstore.jdo.datanucleus.DataNucleusObjectStore;

@Hidden
public class IsisJdoSupportImpl implements IsisJdoSupport {
    
    @Hidden
    @Override
    public <T> T injected(T domainObject) {
        getServicesInjector().injectServicesInto(domainObject);
        return domainObject;
    }

    @Override
    public <T> T refresh(T domainObject) {
        DataNucleusObjectStore objectStore = (DataNucleusObjectStore) getPersistenceSession().getObjectStore();
        ObjectAdapter adapter = getAdapterManager().adapterFor(domainObject);
        objectStore.refreshRoot(adapter);
        return domainObject;
    }

    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected ServicesInjectorSpi getServicesInjector() {
        return getPersistenceSession().getServicesInjector();
    }


}
