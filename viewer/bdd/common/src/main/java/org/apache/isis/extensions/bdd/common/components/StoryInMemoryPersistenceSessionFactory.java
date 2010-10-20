package org.apache.isis.extensions.bdd.common.components;

import org.apache.isis.metamodel.facets.object.cached.CachedFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtime.objectstore.inmemory.InMemoryPersistenceSessionFactory;
import org.apache.isis.runtime.objectstore.inmemory.internal.ObjectStorePersistedObjects;
import org.apache.isis.runtime.persistence.PersistenceSessionFactoryDelegate;
import org.apache.isis.runtime.system.DeploymentType;

/**
 * As per {@link InMemoryPersistenceSessionFactory}, but uses the
 * {@link StoryObjectStorePersistedObjects} implementation which stores any
 * {@link CachedFacet cached} {@link ObjectSpecification class}es
 * <tt>static</tt>ally (and thus persisted across multiple setups/teardowns of
 * the {@link NakedObjectsContext}.
 */
public class StoryInMemoryPersistenceSessionFactory extends
        InMemoryPersistenceSessionFactory {

    public StoryInMemoryPersistenceSessionFactory(
            final DeploymentType deploymentType,
            final PersistenceSessionFactoryDelegate persistenceSessionFactoryDelegate) {
        super(deploymentType, persistenceSessionFactoryDelegate);
    }

    @Override
    public ObjectStorePersistedObjects createPersistedObjects() {
        return new StoryObjectStorePersistedObjects();
    }

}
