package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.queries;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.common.collect.Maps;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.spec.ObjectAdapterUtils;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.DataNucleusObjectStore;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.metamodel.JdoPropertyUtils;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.FrameworkSynchronizer;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindUsingApplibQueryDefault;

public class PersistenceQueryFindUsingApplibQueryProcessor extends PersistenceQueryProcessorAbstract<PersistenceQueryFindUsingApplibQueryDefault> {
    
    public PersistenceQueryFindUsingApplibQueryProcessor(final PersistenceManager persistenceManager, final FrameworkSynchronizer frameworkSynchronizer) {
        super(persistenceManager, frameworkSynchronizer);
    }

    public List<ObjectAdapter> process(final PersistenceQueryFindUsingApplibQueryDefault persistenceQuery) {
        final String queryName = persistenceQuery.getQueryName();
        final Map<String, Object> map = unwrap(persistenceQuery.getArgumentsAdaptersByParameterName());
        final QueryCardinality cardinality = persistenceQuery.getCardinality();
        final ObjectSpecification objectSpec = persistenceQuery.getSpecification();
        
        final List<?> results;
        if((objectSpec.getFullIdentifier() + "#pk").equals(queryName)) {
            // special case handling
            final Class<?> cls = objectSpec.getCorrespondingClass();
            if(!JdoPropertyUtils.hasPrimaryKeyProperty(objectSpec)) {
                throw new UnsupportedOperationException("cannot search by primary key for DataStore-assigned entities");
            }
            final OneToOneAssociation pkOtoa = JdoPropertyUtils.getPrimaryKeyPropertyFor(objectSpec);
            String pkOtoaId = pkOtoa.getId();
            final String filter = pkOtoaId + "==" + map.get(pkOtoaId);
            final Query jdoQuery = getPersistenceManager().newQuery(cls, filter);
            results = (List<?>) jdoQuery.execute();
        } else {
            results = getResults(objectSpec, queryName, map, cardinality);
        }
        
        return loadAdapters(objectSpec, results);
    }

    private List<?> getResults(ObjectSpecification objectSpec, final String queryName, final Map<String, Object> argumentsByParameterName, final QueryCardinality cardinality) {
        
        final PersistenceManager persistenceManager = getJdoObjectStore().getPersistenceManager();
        final Class<?> cls = objectSpec.getCorrespondingClass();
        final Query jdoQuery = persistenceManager.newNamedQuery(cls, queryName);
        
        final List<?> results = (List<?>) jdoQuery.executeWithMap(argumentsByParameterName);
        if (cardinality == QueryCardinality.MULTIPLE) {
            return results;
        }
        return results.isEmpty()?Collections.emptyList():results.subList(0, 1);
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


}

// Copyright (c) Naked Objects Group Ltd.
