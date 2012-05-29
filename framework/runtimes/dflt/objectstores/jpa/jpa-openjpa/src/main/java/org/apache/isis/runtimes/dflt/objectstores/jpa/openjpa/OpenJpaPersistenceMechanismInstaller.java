package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import com.google.common.collect.Maps;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLookup;
import org.apache.isis.extensions.jpa.metamodel.facets.object.entity.JpaEntityFacet;
import org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery.JpaNamedQueryFacet;
import org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery.NamedQuery;
import org.apache.isis.runtimes.dflt.bytecode.identity.objectfactory.ObjectFactoryBasic;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.PersistenceMechanismInstallerAbstract;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm.PersistAlgorithm;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.ObjectFactory;

public class OpenJpaPersistenceMechanismInstaller extends PersistenceMechanismInstallerAbstract {

    public static final String NAME = "openjpa";

    private EntityManagerFactory entityManagerFactory = null;
    private Map<String, NamedQuery> namedQueryByName = null;

    public OpenJpaPersistenceMechanismInstaller() {
        super(NAME);
    }

    @Override
    protected ObjectStore createObjectStore(IsisConfiguration configuration, ObjectAdapterFactory adapterFactory, AdapterManager adapterManager) {
        if(entityManagerFactory == null) {
            Map<String,String> props = Maps.newHashMap();

            props.put("openjpa.jdbc.SynchronizeMappings", "buildSchema");
            props.put("openjpa.ConnectionURL", "jdbc:hsqldb:db/test");
            props.put("openjpa.ConnectionDriverName", "org.hsqldb.jdbcDriver");
            props.put("openjpa.ConnectionUserName", "sa");
            props.put("openjpa.ConnectionPassword", "");
            props.put("openjpa.Log", "DefaultLevel=WARN, Tool=INFO");
            props.put("openjpa.RuntimeUnenhancedClasses", "supported"); // in production, should always pre-enhance using the maven openjpa plugin
            
            final String typeList = entityTypeList();
            props.put("openjpa.MetaDataFactory", "org.apache.openjpa.persistence.jdbc.PersistenceMappingFactory(types=" + typeList + ")");
            
            entityManagerFactory = javax.persistence.Persistence.createEntityManagerFactory(null, props);
            
            namedQueryByName = Collections.unmodifiableMap(catalogNamedQueries());
        }
        final OpenJpaObjectStore objectStore = new OpenJpaObjectStore(configuration, adapterFactory, adapterManager, entityManagerFactory, namedQueryByName);
        return objectStore;
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
