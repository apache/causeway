package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.queries;

import java.util.List;

import javax.jdo.PersistenceManager;

import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.FrameworkSynchronizer;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindByTitle;

public class PersistenceQueryFindByTitleProcessor extends PersistenceQueryProcessorAbstract<PersistenceQueryFindByTitle> {

    public PersistenceQueryFindByTitleProcessor(final PersistenceManager persistenceManager, final FrameworkSynchronizer frameworkSynchronizer) {
        super(persistenceManager, frameworkSynchronizer);
    }

    public List<ObjectAdapter> process(final PersistenceQueryFindByTitle persistenceQuery) {
        final ObjectSpecification objectSpec = persistenceQuery.getSpecification();
        final Class<?> correspondingClass = objectSpec.getCorrespondingClass();
        return process(persistenceQuery, correspondingClass);
    }

    private <Z> List<ObjectAdapter> process(final PersistenceQueryFindByTitle persistenceQuery, Class<Z> correspondingClass) {
        // TODO
        throw new NotYetImplementedException();

//        final CriteriaQuery<Z> criteriaQuery = getEntityManager().getCriteriaBuilder().createQuery(correspondingClass);
//
//        final ObjectSpecification objectSpec = persistenceQuery.getSpecification();
//        final Root<Z> from = criteriaQuery.from(correspondingClass);
//        
//        final EntityType<Z> model = from.getModel();
//        final SingularAttribute<? super Z, String> titleAttribute = model.getSingularAttribute("title", String.class);
//        final Path<String> titlePath = from.get(titleAttribute);
//        titlePath.equals(persistenceQuery.getTitle());
//        
//        final TypedQuery<Z> query = getPersistenceManager().createQuery(criteriaQuery);
//        final List<Z> pojos = query.getResultList();
//        return loadAdapters(objectSpec, pojos);
    }
}

// Copyright (c) Naked Objects Group Ltd.
