package org.apache.isis.extensions.bdd.common.components;

import org.apache.isis.runtime.objectstore.inmemory.InMemoryPersistenceMechanismInstaller;
import org.apache.isis.runtime.persistence.PersistenceSessionFactory;
import org.apache.isis.runtime.system.DeploymentType;

public class StoryInMemoryPersistenceMechanismInstaller extends
        InMemoryPersistenceMechanismInstaller {

    public StoryInMemoryPersistenceMechanismInstaller() {}

    // ///////////////////////////////////////////////////////////////
    // Name
    // ///////////////////////////////////////////////////////////////

    @Override
    public String getName() {
        return "story";
    }

    // ///////////////////////////////////////////////////////////////
    // Hook methods
    // ///////////////////////////////////////////////////////////////

    @Override
    public PersistenceSessionFactory createPersistenceSessionFactory(
            final DeploymentType deploymentType) {
        return new StoryInMemoryPersistenceSessionFactory(deploymentType,
                this);
    }

}

// Copyright (c) Naked Objects Group Ltd.
