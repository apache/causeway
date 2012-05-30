package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import com.google.common.collect.Maps;

import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;

import org.apache.isis.core.commons.components.Installer;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLookup;
import org.apache.isis.extensions.jpa.metamodel.facets.object.entity.JpaEntityFacet;
import org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery.JpaNamedQueryFacet;
import org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery.NamedQuery;
import org.apache.isis.runtimes.dflt.bytecode.identity.objectfactory.ObjectFactoryBasic;
import org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.spi.OpenJpaIdentifierGenerator;
import org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.spi.OpenJpaSimplePersistAlgorithm;
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

    private EntityManagerFactory entityManagerFactory = null;
    private Map<String, NamedQuery> namedQueryByName = null;

    private OpenJpaIdentifierGenerator identifierGenerator;

    public OpenJpaPersistenceMechanismInstaller() {
        super(NAME);
    }

    @Override
    protected ObjectStore createObjectStore(IsisConfiguration configuration, ObjectAdapterFactory adapterFactory, AdapterManager adapterManager) {
        createEntityManagerFactoryIfRequired(configuration);
        final OpenJpaObjectStore objectStore = new OpenJpaObjectStore(configuration, adapterFactory, adapterManager, entityManagerFactory, namedQueryByName);
        return objectStore;
    }

    private void createEntityManagerFactoryIfRequired(IsisConfiguration configuration) {
        if(entityManagerFactory != null) {
            return;
        }
        
        final IsisConfiguration openJpaConfig = configuration.createSubset("isis.persistor.openjpa.impl");
        final Map<String, String> props = openJpaConfig.asMap();
        
        final String typeList = entityTypeList();
        props.put("openjpa.MetaDataFactory", "org.apache.openjpa.persistence.jdbc.PersistenceMappingFactory(types=" + typeList + ")");
        
        entityManagerFactory = javax.persistence.Persistence.createEntityManagerFactory(null, props);
        
        OpenJPAEntityManagerFactorySPI emfSpi = (OpenJPAEntityManagerFactorySPI) entityManagerFactory;
        emfSpi.addLifecycleListener(new IsisLifecycleListener(), (Class[])null);
        
        namedQueryByName = Collections.unmodifiableMap(catalogNamedQueries());
    }

    @Override
    protected IdentifierGenerator createIdentifierGenerator(IsisConfiguration configuration) {
        identifierGenerator = new OpenJpaIdentifierGenerator();
        return identifierGenerator;
    }
    
    /**
     * "org.apache.openjpa.persistence.jdbc.PersistenceMappingFactory(types=org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.fixtures.JpaPrimitiveValuedEntity;)"
     */
    private String entityTypeList() {
        final StringBuilder buf = new StringBuilder();
        final Collection<ObjectSpecification> allSpecifications = getSpecificationLoader().allSpecifications();
        for(ObjectSpecification objSpec: allSpecifications) {
            if(objSpec.containsFacet(JpaEntityFacet.class)) {
                final String fqcn = objSpec.getFullIdentifier();
                buf.append(fqcn).append(";");
            }
        }
        final String typeList = buf.toString();
        return typeList;
    }
    
    private Map<String, NamedQuery> catalogNamedQueries() {
        final HashMap<String, NamedQuery> namedQueryByName = Maps.newHashMap();
        for (final ObjectSpecification spec : getSpecificationLoader().allSpecifications()) {
            final JpaNamedQueryFacet facet = spec.getFacet(JpaNamedQueryFacet.class);
            if (facet == null) {
                continue;
            }
            for (final NamedQuery namedQuery : facet.getNamedQueries()) {
                namedQueryByName.put(namedQuery.getName(), namedQuery);
            }
        }
        return namedQueryByName;
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
