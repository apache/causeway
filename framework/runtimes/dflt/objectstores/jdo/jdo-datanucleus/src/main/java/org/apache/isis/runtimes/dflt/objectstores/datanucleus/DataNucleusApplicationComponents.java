package org.apache.isis.runtimes.dflt.objectstores.datanucleus;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import com.google.common.collect.Maps;

import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.objectstores.datanucleus.persistence.IsisLifecycleListener;
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
        
        namedQueryByName = Collections.unmodifiableMap(catalogNamedQueries(objectSpecs));
        
        lifecycleListener = new IsisLifecycleListener();
    }

    private Map<String, JdoNamedQuery> catalogNamedQueries(Collection<ObjectSpecification> objectSpecs) {
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
