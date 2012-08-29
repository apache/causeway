package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.queries;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.FrameworkSynchronizer;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindAllInstances;

public class PersistenceQueryFindAllInstancesProcessor extends PersistenceQueryProcessorAbstract<PersistenceQueryFindAllInstances> {

    private static final Logger LOG = Logger.getLogger(PersistenceQueryFindAllInstancesProcessor.class);

    public PersistenceQueryFindAllInstancesProcessor(final PersistenceManager persistenceManager, final FrameworkSynchronizer frameworkSynchronizer) {
        super(persistenceManager, frameworkSynchronizer);
    }

    public List<ObjectAdapter> process(final PersistenceQueryFindAllInstances persistenceQuery) {

        final ObjectSpecification specification = persistenceQuery.getSpecification();
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstances: class=" + specification.getFullIdentifier());
        }
        
        Class<?> cls = specification.getCorrespondingClass();
        final Query query = getPersistenceManager().newQuery(cls);
        
        final List<?> pojos = (List<?>) query.execute();
        return loadAdapters(specification, pojos);
    }
}

// Copyright (c) Naked Objects Group Ltd.
