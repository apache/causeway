package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.queries;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.metadata.TypeMetadata;
import javax.jdo.spi.PersistenceCapable;

import com.google.common.collect.Lists;

import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.DataNucleusObjectStore;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.FrameworkSynchronizer;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.FrameworkSynchronizer.CalledFrom;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.IsisLifecycleListener;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManagerSpi;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceQuery;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.Persistor;

public abstract class PersistenceQueryProcessorAbstract<T extends PersistenceQuery>
        implements PersistenceQueryProcessor<T> {

    private final PersistenceManager persistenceManager;
    private final FrameworkSynchronizer frameworkSynchronizer;

    protected PersistenceQueryProcessorAbstract(final PersistenceManager persistenceManager, final FrameworkSynchronizer frameworkSynchronizer) {
        this.persistenceManager = persistenceManager;
        this.frameworkSynchronizer = frameworkSynchronizer;
    }

    protected PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }
    
    
    // /////////////////////////////////////////////////////////////
    // helpers for subclasses
    // /////////////////////////////////////////////////////////////

    protected PersistenceManagerFactory getPersistenceManagerFactory() {
        return getPersistenceManager().getPersistenceManagerFactory();
    }
    
    protected TypeMetadata getTypeMetadata(final String classFullName) {
        return getPersistenceManagerFactory().getMetadata(classFullName);
    }
    
    /**
     * Traversing the provided list causes (or should cause) the
     * {@link IsisLifecycleListener#postLoad(InstanceLifecycleEvent) {
     * to be called.
     */
    protected List<ObjectAdapter> loadAdapters(
            final ObjectSpecification specification, final List<?> pojos) {
        final List<ObjectAdapter> adapters = Lists.newArrayList();
        for (final Object pojo : pojos) {
        	// ought not to be necessary, however for some queries it seems that the 
        	// lifecycle listener is not called
        	frameworkSynchronizer.postLoadProcessingFor((PersistenceCapable) pojo, CalledFrom.OS_QUERY);
            ObjectAdapter adapter = getAdapterManager().getAdapterFor(pojo);
            Assert.assertNotNull(adapter);
            adapters.add(adapter);
        }
        return adapters;
    }

    // /////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // /////////////////////////////////////////////////////////////

    protected Persistor getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected AdapterManager getAdapterManager() {
        return IsisContext.getPersistenceSession().getAdapterManager();
    }

    protected DataNucleusObjectStore getJdoObjectStore() {
        return (DataNucleusObjectStore) IsisContext.getPersistenceSession().getObjectStore();
    }

}
