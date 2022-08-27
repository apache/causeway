/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.persistence.jpa.integration.entity;

import java.lang.reflect.Method;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.metamodel.EntityType;

import org.eclipse.persistence.exceptions.DescriptorException;
import org.springframework.data.jpa.repository.JpaContext;

import org.apache.isis.applib.exceptions.unrecoverable.ObjectNotFoundException;
import org.apache.isis.applib.query.AllInstancesQuery;
import org.apache.isis.applib.query.NamedQuery;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.beans.PersistenceStack;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.idstringifier.IdStringifierService;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JpaEntityFacet
        extends FacetAbstract
        implements EntityFacet {

    private final Class<?> entityClass;
    private final ServiceRegistry serviceRegistry;
    private final IdStringifierService idStringifierService;

    protected JpaEntityFacet(
            final FacetHolder holder,
            final Class<?> entityClass,
            final @NonNull ServiceRegistry serviceRegistry) {

        super(EntityFacet.class, holder, Precedence.HIGH);
        this.entityClass = entityClass;
        this.serviceRegistry = serviceRegistry;
        this.idStringifierService = serviceRegistry.lookupServiceElseFail(IdStringifierService.class);
    }

    // -- ENTITY FACET

    @Override
    public PersistenceStack getPersistenceStack() {
        return PersistenceStack.JPA;
    }

    @Override
    public String identifierFor(final Object pojo) {

        if (pojo == null) {
            throw _Exceptions.illegalArgument(
                    "The persistence layer cannot identify a pojo that is null (given type %s)",
                    entityClass.getName());
        }

        val entityManager = getEntityManager();
        val persistenceUnitUtil = getPersistenceUnitUtil(entityManager);
        val primaryKey = persistenceUnitUtil.getIdentifier(pojo);

        if (primaryKey == null) {
            throw _Exceptions.illegalArgument(
                    "The persistence layer does not recognize given object of type %s, "
                            + "meaning the object has no identifier that associates it with the persistence layer. "
                            + "(most likely, because the object is detached, eg. was not persisted after being new-ed up)",
                    pojo.getClass().getName());
        }

        return idStringifierService.enstringPrimaryKey(getPrimaryKeyType(), primaryKey);
    }

    @Override
    public ManagedObject fetchByIdentifier(
            final @NonNull Bookmark bookmark) {

        log.debug("fetchEntity; bookmark={}", bookmark);

        val primaryKey = idStringifierService
                .destringPrimaryKey(getPrimaryKeyType(), entityClass, bookmark.getIdentifier());

        val entityManager = getEntityManager();
        val entityPojo = entityManager.find(entityClass, primaryKey);

        if (entityPojo == null) {
            throw new ObjectNotFoundException("" + bookmark);
        }

        final ObjectSpecification entitySpec = getEntitySpec();
        return ManagedObject.bookmarked(entitySpec, entityPojo, bookmark);
    }

    private ObjectSpecification getEntitySpec() {
        return getSpecificationLoader().specForType(entityClass)
                            .orElseThrow(() -> new IllegalStateException(String.format("Could not load specification for entity class '%s'", entityClass)));
    }

    private Class<?> getPrimaryKeyType() {
        return getJpaEntityType().getIdType().getJavaType();
    }

    @Override
    public Can<ManagedObject> fetchByQuery(final Query<?> query) {

        val range = query.getRange();

        if (query instanceof AllInstancesQuery) {

            val queryFindAllInstances = (AllInstancesQuery<?>) query;
            val queryEntityType = queryFindAllInstances.getResultType();

            // guard against misuse
            if (!entityClass.isAssignableFrom(queryEntityType)) {
                throw _Exceptions.unexpectedCodeReach();
            }

            val entityManager = getEntityManager();

            val cb = entityManager.getCriteriaBuilder();
            val cr = cb.createQuery(entityClass);

            cr.select(_Casts.uncheckedCast(cr.from(entityClass)));

            val typedQuery = entityManager
                    .createQuery(cr);

            if (range.hasOffset()) {
                typedQuery.setFirstResult(range.getStartAsInt());
            }
            if (range.hasLimit()) {
                typedQuery.setMaxResults(range.getLimitAsInt());
            }

            val spec = getEntitySpec();
            return Can.ofStream(
                    typedQuery.getResultStream()
                            .map(entity -> ManagedObject.of(spec, entity)));

        } else if (query instanceof NamedQuery) {

            val applibNamedQuery = (NamedQuery<?>) query;
            val queryResultType = applibNamedQuery.getResultType();

            val entityManager = getEntityManager();

            val namedQuery = entityManager
                    .createNamedQuery(applibNamedQuery.getName(), queryResultType);

            if (range.hasOffset()) {
                namedQuery.setFirstResult(range.getStartAsInt());
            }
            if (range.hasLimit()) {
                namedQuery.setMaxResults(range.getLimitAsInt());
            }

            applibNamedQuery
                    .getParametersByName()
                    .forEach((paramName, paramValue) ->
                            namedQuery.setParameter(paramName, paramValue));

            val spec = getEntitySpec();
            return Can.ofStream(
                    namedQuery.getResultStream()
                            .map(entity -> ManagedObject.of(spec, entity)));

        }

        throw _Exceptions.unsupportedOperation(
                "Support for Query of type %s not implemented.", query.getClass());
    }

    @Override
    public void persist(final Object pojo) {
        if (pojo == null) {
            return; // nothing to do
        }

        // guard against misuse
        if (!entityClass.isAssignableFrom(pojo.getClass())) {
            throw _Exceptions.unexpectedCodeReach();
        }

        val entityManager = getEntityManager();

        log.debug("about to persist entity {}", pojo);

        entityManager.persist(pojo);
    }

    @Override
    public void refresh(final Object pojo) {
        if (pojo == null) {
            return; // nothing to do
        }

        // guard against misuse
        if (!entityClass.isAssignableFrom(pojo.getClass())) {
            throw _Exceptions.unexpectedCodeReach();
        }

        val entityManager = getEntityManager();
        entityManager.refresh(pojo);
    }

    @Override
    public void delete(final Object pojo) {

        if (pojo == null) {
            return; // nothing to do
        }

        // guard against misuse
        if (!entityClass.isAssignableFrom(pojo.getClass())) {
            throw _Exceptions.unexpectedCodeReach();
        }

        val entityManager = getEntityManager();
        entityManager.remove(pojo);
    }

    @Override
    public EntityState getEntityState(final Object pojo) {

        if (pojo == null) {
            return EntityState.NOT_PERSISTABLE;
        }

        // guard against misuse
        if (!entityClass.isAssignableFrom(pojo.getClass())) {
            //throw _Exceptions.unexpectedCodeReach();
            return EntityState.NOT_PERSISTABLE;
        }

        val entityManager = getEntityManager();

        if (entityManager.contains(pojo)) {
            return EntityState.PERSISTABLE_ATTACHED;
        }

        try {
            val primaryKey = getPersistenceUnitUtil(entityManager).getIdentifier(pojo);
            if (primaryKey == null) {
                return EntityState.PERSISTABLE_DETACHED; // an optimization, not strictly required
            }
        } catch (PersistenceException ex) {
            // horrible hack, but encountered NPEs if using a composite key (eg CommandLogEntry) (this was without any weaving)
            Throwable cause = ex.getCause();
            if (cause instanceof DescriptorException) {
                DescriptorException descriptorException = (DescriptorException) cause;
                Throwable internalException = descriptorException.getInternalException();
                if (internalException instanceof NullPointerException) {
                    return EntityState.PERSISTABLE_DETACHED;
                }
            }
            if (cause instanceof NullPointerException) {
                // horrible hack, encountered if using composite key (eg ExecutionLogEntry) with dynamic weaving
                return EntityState.PERSISTABLE_DETACHED;
            }
            throw ex;
        }

        //XXX whether DETACHED or REMOVED is currently undecidable (JPA)
        return EntityState.PERSISTABLE_DETACHED;
    }

    @Override
    public boolean isProxyEnhancement(final Method method) {
        return false;
    }

    @Override
    public <T> T detach(final T pojo) {
        getEntityManager().detach(pojo);
        return pojo;
    }

    // -- JPA METAMODEL

    private final _Lazy<Optional<EntityType<?>>> jpaEntityTypeRef = _Lazy.threadSafe(this::queryJpaMetamodel);

    /**
     * get the JPA meta-model associated with this (corresponding) entity
     */
    private EntityType<?> getJpaEntityType() {
        return jpaEntityTypeRef.get().orElseThrow(_Exceptions::noSuchElement);
    }

    /**
     * find the JPA meta-model associated with this (corresponding) entity
     */
    private Optional<EntityType<?>> queryJpaMetamodel() {
        return getEntityManager().getMetamodel().getEntities()
                .stream()
                .filter(type -> type.getJavaType().equals(entityClass))
                .findFirst();
    }



    // -- DEPENDENCIES

    protected JpaContext getJpaContext() {
        return serviceRegistry.lookupServiceElseFail(JpaContext.class);
    }

    protected EntityManager getEntityManager() {
        return getJpaContext().getEntityManagerByManagedType(entityClass);
    }

    protected PersistenceUnitUtil getPersistenceUnitUtil(final EntityManager entityManager) {
        return entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
    }

}
