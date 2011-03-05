package org.apache.isis.viewer.bdd.common.components;

import org.apache.isis.core.metamodel.facets.object.cached.CachedFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceSessionFactoryDelegate;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.defaults.objectstore.InMemoryPersistenceSessionFactory;
import org.apache.isis.defaults.objectstore.internal.ObjectStorePersistedObjects;

/**
 * As per {@link InMemoryPersistenceSessionFactory}, but uses the
 * {@link BddObjectStorePersistedObjects} implementation which stores any
 * {@link CachedFacet cached} {@link ObjectSpecification class}es
 * <tt>static</tt>ally (and thus persisted across multiple setups/teardowns of
 * the {@link NakedObjectsContext}.
 */
public class BddInMemoryPersistenceSessionFactory extends
        InMemoryPersistenceSessionFactory {

    public BddInMemoryPersistenceSessionFactory(
            final DeploymentType deploymentType,
            final PersistenceSessionFactoryDelegate persistenceSessionFactoryDelegate) {
        super(deploymentType, persistenceSessionFactoryDelegate);
    }

    @Override
    public ObjectStorePersistedObjects createPersistedObjects() {
        return new BddObjectStorePersistedObjects();
    }

}
