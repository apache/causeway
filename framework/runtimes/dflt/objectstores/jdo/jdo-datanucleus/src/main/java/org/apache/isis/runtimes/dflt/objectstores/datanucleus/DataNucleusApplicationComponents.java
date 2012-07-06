package org.apache.isis.runtimes.dflt.objectstores.datanucleus;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jdo.JDOEnhancer;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.datanucleus.NucleusContext;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.schema.SchemaAwareStoreManager;

import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.objectstores.datanucleus.persistence.IsisLifecycleListener;
import org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.facets.object.embeddedonly.JdoEmbeddedOnlyFacet;
import org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;
import org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.facets.object.query.JdoNamedQuery;
import org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.facets.object.query.JdoQueryFacet;

public class DataNucleusApplicationComponents implements ApplicationScopedComponent {

    private final PersistenceManagerFactory persistenceManagerFactory;
    private final Map<String, JdoNamedQuery> namedQueryByName;
    private final IsisLifecycleListener lifecycleListener;


    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    public DataNucleusApplicationComponents(final Map<String, String> props, final Collection<ObjectSpecification> objectSpecs) {
        persistenceManagerFactory = JDOHelper.getPersistenceManagerFactory(props);
        
        final Set<String> classesToBePersisted = catalogClassesToBePersisted(objectSpecs);

        enhanceClasses(classesToBePersisted.toArray(new String[0]));
        createSchema(props, classesToBePersisted);

        namedQueryByName = Collections.unmodifiableMap(catalogNamedQueries(objectSpecs));

        lifecycleListener = new IsisLifecycleListener();
    }

    private static void enhanceClasses(String[] classesToBePersisted) {
        JDOEnhancer enhancer = JDOHelper.getEnhancer();
        enhancer.addClasses(classesToBePersisted);
        int numberClassesEnhanced = enhancer.enhance();
        if(numberClassesEnhanced == 0) {
            throw new JdoRuntimeException("Failed to enhance any classes");
        }
    }

    private void createSchema(final Map<String, String> props, final Set<String> classesToBePersisted) {
        final JDOPersistenceManagerFactory jdopmf = (JDOPersistenceManagerFactory)persistenceManagerFactory;
        final NucleusContext nucleusContext = jdopmf.getNucleusContext();
        final SchemaAwareStoreManager storeManager = (SchemaAwareStoreManager) nucleusContext.getStoreManager();
        storeManager.createSchema(classesToBePersisted, asProperties(props));
    }

    private static Set<String> catalogClassesToBePersisted(Collection<ObjectSpecification> objectSpecs) {
        Set<String> classNames = Sets.newTreeSet();
        for (final ObjectSpecification spec : objectSpecs) {
            if(spec.containsFacet(JdoPersistenceCapableFacet.class) || spec.containsFacet(JdoEmbeddedOnlyFacet.class)) {
                classNames.add(spec.getFullIdentifier());
            }
        }
        return Collections.unmodifiableSet(classNames);
    }

    private static Properties asProperties(Map<String, String> props) {
        Properties properties = new Properties();
        properties.putAll(props);
        return properties;
    }

    private static Map<String, JdoNamedQuery> catalogNamedQueries(Collection<ObjectSpecification> objectSpecs) {
        final Map<String, JdoNamedQuery> namedQueryByName = Maps.newHashMap();
        for (final ObjectSpecification spec : objectSpecs) {
            final JdoQueryFacet facet = spec.getFacet(JdoQueryFacet.class);
            if (facet == null) {
                continue;
            }
            for (final JdoNamedQuery namedQuery : facet.getNamedQueries()) {
                namedQueryByName.put(namedQuery.getName(), namedQuery);
            }
        }
        return namedQueryByName;
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
    }


    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////
    
    public PersistenceManager createPersistenceManager() {
        PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
        
        persistenceManager.addInstanceLifecycleListener(lifecycleListener, (Class[])null);
        return persistenceManager;
    }

    public JdoNamedQuery getNamedQuery(String queryName) {
        return namedQueryByName.get(queryName);
    }

    
    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    public void suspendListener() {
        lifecycleListener.setSuspended(true);
    }

    public void resumeListener() {
        lifecycleListener.setSuspended(false);
    }

}
