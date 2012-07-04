package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence.queries;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.common.collect.Maps;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.spec.ObjectAdapterUtils;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.object.namedquery.NamedQuery;
import org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.OpenJpaObjectStore;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindUsingApplibQueryDefault;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;

public class PersistenceQueryFindUsingApplibQueryProcessor extends PersistenceQueryProcessorAbstract<PersistenceQueryFindUsingApplibQueryDefault> {
    
    public PersistenceQueryFindUsingApplibQueryProcessor(final AdapterManager objectManager, final EntityManager entityManager) {
        super(objectManager, entityManager);
    }

    public List<ObjectAdapter> process(final PersistenceQueryFindUsingApplibQueryDefault persistenceQuery) {
        final String queryName = persistenceQuery.getQueryName();
        final Map<String, Object> map = unwrap(persistenceQuery.getArgumentsAdaptersByParameterName());
        final QueryCardinality cardinality = persistenceQuery.getCardinality();
        final List<?> results = getResults(queryName, map, cardinality);
        return loadAdapters(persistenceQuery.getSpecification(), results);
    }

    public List<?> getResults(final String queryName, final Map<String, Object> argumentsByParameterName, final QueryCardinality cardinality) {
        final NamedQuery namedQuery = getNamedQuery(queryName);

        final EntityManager entityManager = getJpaObjectStore().getEntityManager();
        final Query emQuery = entityManager.createNamedQuery(namedQuery.getName());
        
        for (final Map.Entry<String, Object> argumentByParameterName : argumentsByParameterName.entrySet()) {
            final String parameterName = argumentByParameterName.getKey();
            final Object argument = argumentByParameterName.getValue();
            emQuery.setParameter(parameterName, argument);
        }

        if (cardinality == QueryCardinality.MULTIPLE) {
            return emQuery.getResultList();
        }
        if (cardinality == QueryCardinality.SINGLE) {
            final Object result = emQuery.getSingleResult();
            return result == null ? Collections.EMPTY_LIST : Arrays.asList(result);
        }
        // should never get here
        return Collections.EMPTY_LIST;
    }

    private NamedQuery getNamedQuery(final String queryName) {
        return getJpaObjectStore().getNamedQuery(queryName);
    }

    private static Map<String, Object> unwrap(final Map<String, ObjectAdapter> argumentAdaptersByParameterName) {
        final Map<String, Object> argumentsByParameterName = Maps.newHashMap();
        for (final String parameterName : argumentAdaptersByParameterName.keySet()) {
            final ObjectAdapter argumentAdapter = argumentAdaptersByParameterName.get(parameterName);
            final Object argument = ObjectAdapterUtils.unwrapObject(argumentAdapter);
            argumentsByParameterName.put(parameterName, argument);
        }
        return argumentsByParameterName;
    }

    // /////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // /////////////////////////////////////////////////////////////

    private OpenJpaObjectStore getJpaObjectStore() {
        return (OpenJpaObjectStore) getPersistenceSession().getObjectStore();
    }

    private PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

}

// Copyright (c) Naked Objects Group Ltd.
