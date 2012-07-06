package org.apache.isis.runtimes.dflt.objectstores.datanucleus.persistence.queries;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.metadata.TypeMetadata;

import com.google.common.collect.Lists;

import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceQuery;

public abstract class PersistenceQueryProcessorAbstract<T extends PersistenceQuery>
        implements PersistenceQueryProcessor<T> {

    private final AdapterManager adapterManager;
    private final PersistenceManager persistenceManager;

    protected PersistenceQueryProcessorAbstract(
            final AdapterManager objectManager , final PersistenceManager persistenceManager) {
        this.adapterManager = objectManager;
        this.persistenceManager = persistenceManager;
    }

    protected PersistenceManagerFactory getPersistenceManagerFactory() {
        return getPersistenceManager().getPersistenceManagerFactory();
    }

    protected PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }
    
    protected TypeMetadata getTypeMetadata(final String classFullName) {
        return getPersistenceManagerFactory().getMetadata(classFullName);
    }


    protected List<ObjectAdapter> loadAdapters(
            final ObjectSpecification specification, final List<?> pojos) {
        final List<ObjectAdapter> adapters = Lists.newArrayList();
        for (final Object pojo : pojos) {
            ObjectAdapter adapter = adapterManager.getAdapterFor(pojo);
            Assert.assertNotNull(adapter);
            adapters.add( adapter );
        }
        return adapters;
    }

    protected AdapterManager getAdapterManager() {
        return adapterManager;
    }

}

// Copyright (c) Naked Objects Group Ltd.
