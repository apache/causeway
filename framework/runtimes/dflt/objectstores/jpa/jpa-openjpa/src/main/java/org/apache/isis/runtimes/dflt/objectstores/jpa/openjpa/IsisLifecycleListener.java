package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;

import org.apache.openjpa.event.AbstractLifecycleListener;
import org.apache.openjpa.event.LifecycleEvent;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackUtils;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistingCallbackFacet;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManagerExtended;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionHydrator;

public class IsisLifecycleListener extends AbstractLifecycleListener {

    @Override
    public void afterPersist(LifecycleEvent event) {
        final Object pojo = event.getSource();
        
        
        // TODO: see NakedInsertPostEventListener, since there is much more to be done here..
        
        final ObjectAdapter adapter = getAdapterManager().adapterFor(pojo);
        
        CallbackUtils.callCallback(adapter, PersistedCallbackFacet.class);
    }

    protected AdapterManagerExtended getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }
    
    @Override
    public void afterLoad(LifecycleEvent event) {
        final Object pojo = event.getSource();
        
        final OidGenerator oidGenerator = getOidGenerator();
        final RootOid oid = oidGenerator.createPersistent(pojo, null);
        
        PersistenceSessionHydrator hydrator = getPersistenceSession();
        hydrator.recreateAdapter(oid, pojo);
    }

    protected OidGenerator getOidGenerator() {
        return getPersistenceSession().getOidGenerator();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }
}
