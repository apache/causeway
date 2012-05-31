package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.google.common.collect.Maps;

import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;

import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.extensions.jpa.metamodel.facets.object.entity.JpaEntityFacet;
import org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery.JpaNamedQueryFacet;
import org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery.NamedQuery;
import org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence.IsisLifecycleListener;

public class OpenJpaApplicationComponents implements ApplicationScopedComponent {

    private final EntityManagerFactory entityManagerFactory;
    private final OpenJPAEntityManagerFactorySPI entityManagerFactorySpi;
    private final Map<String, NamedQuery> namedQueryByName;
    private final IsisLifecycleListener lifecycleListener;


    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    public OpenJpaApplicationComponents(final Map<String, String> props, final Collection<ObjectSpecification> objectSpecs) {
        final String typeList = entityTypeList(objectSpecs);
        props.put("openjpa.MetaDataFactory", "org.apache.openjpa.persistence.jdbc.PersistenceMappingFactory(types=" + typeList + ")");
        entityManagerFactory = javax.persistence.Persistence.createEntityManagerFactory(null, props);
        
        namedQueryByName = Collections.unmodifiableMap(catalogNamedQueries(objectSpecs));
        
        entityManagerFactorySpi = (OpenJPAEntityManagerFactorySPI) entityManagerFactory;
        lifecycleListener = new IsisLifecycleListener();
        
        entityManagerFactorySpi.addLifecycleListener(lifecycleListener, (Class[])null);
    }

    private String entityTypeList(final Collection<ObjectSpecification> objectSpecs) {
        final StringBuilder buf = new StringBuilder();
        for(ObjectSpecification objSpec: objectSpecs) {
            if(objSpec.containsFacet(JpaEntityFacet.class)) {
                final String fqcn = objSpec.getFullIdentifier();
                buf.append(fqcn).append(";");
            }
        }
        return buf.toString();
    }
    
    private Map<String, NamedQuery> catalogNamedQueries(Collection<ObjectSpecification> objectSpecs) {
        final Map<String, NamedQuery> namedQueryByName = Maps.newHashMap();
        for (final ObjectSpecification spec : objectSpecs) {
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
    
    public EntityManager createEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    public NamedQuery getNamedQuery(String queryName) {
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
