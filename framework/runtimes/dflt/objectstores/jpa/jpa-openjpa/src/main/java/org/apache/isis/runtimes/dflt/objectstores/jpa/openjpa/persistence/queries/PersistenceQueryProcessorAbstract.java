package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence.queries;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import com.google.common.collect.Lists;

import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.commons.lang.CastUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceQuery;

public abstract class PersistenceQueryProcessorAbstract<T extends PersistenceQuery>
        implements PersistenceQueryProcessor<T> {

    private final AdapterManager adapterManager;
    private final EntityManager entityManager;

    protected PersistenceQueryProcessorAbstract(
            final AdapterManager objectManager , final EntityManager entityManager) {
        this.adapterManager = objectManager;
        this.entityManager = entityManager;
    }

    protected EntityManagerFactory getEntityManagerFactory() {
        return getEntityManager().getEntityManagerFactory();
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }
    
    protected <X> ManagedType<X> getClassMetadata(final String classFullName) {
        final ManagedType<X> managedType = CastUtils.cast(getMetaModel().managedType(InstanceUtil.loadClass(classFullName)));
        return managedType;
    }

    protected Metamodel getMetaModel() {
        return getEntityManagerFactory().getMetamodel();
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
