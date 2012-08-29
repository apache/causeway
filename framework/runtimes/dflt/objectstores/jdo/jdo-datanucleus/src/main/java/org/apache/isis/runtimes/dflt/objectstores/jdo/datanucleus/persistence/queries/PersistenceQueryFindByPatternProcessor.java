package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.queries;

import java.util.List;

import javax.jdo.PersistenceManager;

import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.FrameworkSynchronizer;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindByPattern;

public class PersistenceQueryFindByPatternProcessor extends
        PersistenceQueryProcessorAbstract<PersistenceQueryFindByPattern> {

    public PersistenceQueryFindByPatternProcessor(
            final PersistenceManager persistenceManager, final FrameworkSynchronizer frameworkSynchronizer) {
        super(persistenceManager, frameworkSynchronizer);
    }

    public List<ObjectAdapter> process(
            final PersistenceQueryFindByPattern persistenceQuery) {

        

        
//        final Object pojoPattern = persistenceQuery.getPattern().getObject();
//        
//        final CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
//        
//        
//        final CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery();
//        
//        final Query query = getEntityManager().createQuery(criteriaQuery);
//        final List<?> results = query.getResultList();
//        return loadAdapters(persistenceQuery.getSpecification(), results);
        
        throw new NotYetImplementedException();
    }
}

// Copyright (c) Naked Objects Group Ltd.
