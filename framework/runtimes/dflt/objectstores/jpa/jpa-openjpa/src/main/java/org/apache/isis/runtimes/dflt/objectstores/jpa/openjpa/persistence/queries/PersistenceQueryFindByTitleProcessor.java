package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence.queries;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindByTitle;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;

public class PersistenceQueryFindByTitleProcessor extends PersistenceQueryProcessorAbstract<PersistenceQueryFindByTitle> {

    public PersistenceQueryFindByTitleProcessor(final AdapterManager adapterManager, final EntityManager entityManager) {
        super(adapterManager, entityManager);
    }

    public List<ObjectAdapter> process(final PersistenceQueryFindByTitle persistenceQuery) {
        final ObjectSpecification objectSpec = persistenceQuery.getSpecification();
        final Class<?> correspondingClass = objectSpec.getCorrespondingClass();
        return process(persistenceQuery, correspondingClass);
    }

    private <Z> List<ObjectAdapter> process(final PersistenceQueryFindByTitle persistenceQuery, Class<Z> correspondingClass) {
        final CriteriaQuery<Z> criteriaQuery = getEntityManager().getCriteriaBuilder().createQuery(correspondingClass);

        final ObjectSpecification objectSpec = persistenceQuery.getSpecification();
        final Root<Z> from = criteriaQuery.from(correspondingClass);
        
        final EntityType<Z> model = from.getModel();
        final SingularAttribute<? super Z, String> titleAttribute = model.getSingularAttribute("title", String.class);
        final Path<String> titlePath = from.get(titleAttribute);
        titlePath.equals(persistenceQuery.getTitle());
        
        final TypedQuery<Z> query = getEntityManager().createQuery(criteriaQuery);
        final List<Z> pojos = query.getResultList();
        return loadAdapters(objectSpec, pojos);
    }
}

// Copyright (c) Naked Objects Group Ltd.
