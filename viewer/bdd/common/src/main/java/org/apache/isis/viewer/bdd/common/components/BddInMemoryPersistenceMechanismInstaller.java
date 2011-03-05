package org.apache.isis.viewer.bdd.common.components;

import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceSessionFactory;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.objectstores.dflt.InMemoryPersistenceMechanismInstaller;

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
