package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa;

import java.util.Map;

import org.apache.isis.core.commons.components.Installer;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.spec.SpecificationLookup;
import org.apache.isis.runtimes.dflt.bytecode.identity.objectfactory.ObjectFactoryBasic;
import org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence.spi.OpenJpaIdentifierGenerator;
import org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence.spi.OpenJpaSimplePersistAlgorithm;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.PersistenceMechanismInstallerAbstract;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm.PersistAlgorithm;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.IdentifierGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.ObjectFactory;

/**
 * Configuration files are read in the usual fashion (as per {@link Installer#getConfigurationResources()}, ie will consult all of:
 * <ul>
 * <li><tt>persistor_openjpa.properties</tt>
 * <li><tt>persistor.properties</tt>
 * <li><tt>isis.properties</tt>
 * </ul>
 * 
 * <p>
 * With respect to configuration, all properties under <tt>isis.persistor.openjpa.impl.</tt> prefix are passed thru verbatim to the OpenJPA runtime.
 * For example:
 * <table>
 * <tr><th>Isis Property</th><th>OpenJPA Property</th></tr>
 * <tr><td><tt>isis.persistor.openjpa.impl.openjpa.ConnectionURL</tt></td><td><tt>openjpa.ConnectionURL</tt></td></tr>
 * <tr><td><tt>isis.persistor.openjpa.impl.openjpa.ConnectionDriverName</tt></td><td><tt>openjpa.ConnectionDriverName</tt></td></tr>
 * <tr><td><tt>isis.persistor.openjpa.impl.openjpa.ConnectionUserName</tt></td><td><tt>openjpa.ConnectionUserName</tt></td></tr>
 * <tr><td><tt>isis.persistor.openjpa.impl.openjpa.ConnectionPassword</tt></td><td><tt>openjpa.ConnectionPassword</tt></td></tr>
 * </table>
 *
 */
public class OpenJpaPersistenceMechanismInstaller extends PersistenceMechanismInstallerAbstract {

    public static final String NAME = "openjpa";

    private OpenJpaApplicationComponents applicationComponents = null;
    
    public OpenJpaPersistenceMechanismInstaller() {
        super(NAME);
    }

    @Override
    protected ObjectStore createObjectStore(IsisConfiguration configuration, ObjectAdapterFactory adapterFactory, AdapterManager adapterManager) {
        createOpenJpaApplicationComponentsIfRequired(configuration);
        return new OpenJpaObjectStore(configuration, adapterFactory, adapterManager, applicationComponents);
    }

    private void createOpenJpaApplicationComponentsIfRequired(IsisConfiguration configuration) {
        if(applicationComponents != null) {
            return;
        }
        
        final IsisConfiguration openJpaConfig = configuration.createSubset("isis.persistor.openjpa.impl");
        final Map<String, String> props = openJpaConfig.asMap();
        
        applicationComponents = new OpenJpaApplicationComponents(props, getSpecificationLoader().allSpecifications());
    }

    @Override
    protected IdentifierGenerator createIdentifierGenerator(IsisConfiguration configuration) {
        return new OpenJpaIdentifierGenerator();
    }


    @Override
    protected PersistAlgorithm createPersistAlgorithm(IsisConfiguration configuration) {
        return new OpenJpaSimplePersistAlgorithm();
    }
    
    @Override
    protected ObjectFactory createObjectFactory(IsisConfiguration configuration) {
        return new ObjectFactoryBasic();
    }
    
    protected SpecificationLookup getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

}
