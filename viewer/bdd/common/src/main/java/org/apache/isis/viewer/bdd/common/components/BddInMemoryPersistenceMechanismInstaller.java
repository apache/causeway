package org.apache.isis.viewer.bdd.common.components;

import org.apache.isis.core.runtime.persistence.PersistenceSessionFactory;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.defaults.objectstore.InMemoryPersistenceMechanismInstaller;

public class BddInMemoryPersistenceMechanismInstaller extends
        InMemoryPersistenceMechanismInstaller {

    public BddInMemoryPersistenceMechanismInstaller() {}

    // ///////////////////////////////////////////////////////////////
    // Name
    // ///////////////////////////////////////////////////////////////

    @Override
    public String getName() {
        return "bdd";
    }

    // ///////////////////////////////////////////////////////////////
    // Hook methods
    // ///////////////////////////////////////////////////////////////

    @Override
    public PersistenceSessionFactory createPersistenceSessionFactory(
            final DeploymentType deploymentType) {
        return new BddInMemoryPersistenceSessionFactory(deploymentType,
                this);
    }

}
