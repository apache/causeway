package org.apache.isis.viewer.bdd.common.components;

import org.apache.isis.core.runtime.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.defaults.objectstore.InMemoryPersistenceMechanismInstaller;

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
