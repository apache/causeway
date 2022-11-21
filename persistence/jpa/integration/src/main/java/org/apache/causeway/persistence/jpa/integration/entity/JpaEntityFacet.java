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
package org.apache.causeway.persistence.jpa.integration.entity;

import java.lang.reflect.Method;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.metamodel.EntityType;

import org.eclipse.persistence.exceptions.DescriptorException;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.lang.Nullable;

import org.apache.causeway.applib.query.AllInstancesQuery;
import org.apache.causeway.applib.query.NamedQuery;
import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.repository.EntityState;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.beans.PersistenceStack;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.idstringifier.IdStringifierLookupService;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JpaEntityFacet
        extends FacetAbstract
        implements EntityFacet {

    // self managed injections via constructor
    @Inject private JpaContext jpaContext;
    @Inject private IdStringifierLookupService idStringifierLookupService;

    private final Class<?> entityClass;
    private PrimaryKeyType<?> primaryKeyType;

    protected JpaEntityFacet(
            final FacetHolder holder,
            final Class<?> entityClass) {
        super(EntityFacet.class, holder, Precedence.HIGH);
        getServiceInjector().injectServicesInto(this);

        this.entityClass = entityClass;
        this.primaryKeyType = idStringifierLookupService
                .primaryKeyTypeFor(entityClass, getPrimaryKeyType());
    }

    // -- ENTITY FACET

    @Override
    public PersistenceStack getPersistenceStack() {
        return PersistenceStack.JPA;
    }

    @Override
    public Optional<String> identifierFor(final @Nullable Object pojo) {

        if (!getEntityState(pojo).hasOid()) {
            return Optional.empty();
        }

        val entityManager = getEntityManager();
        val persistenceUnitUtil = getPersistenceUnitUtil(entityManager);
        val primaryKeyIfAny = persistenceUnitUtil.getIdentifier(pojo);

        return Optional.ofNullable(primaryKeyIfAny)
                .map(primaryKey->
                    primaryKeyType.enstringWithCast(primaryKey));
    }

    @Override
    public Bookmark validateBookmark(final @NonNull Bookmark bookmark) {
        _Assert.assertNotNull(primaryKeyType.destring(bookmark.getIdentifier()));
        return bookmark;
    }

    @Override
    public Optional<Object> fetchByBookmark(final @NonNull Bookmark bookmark) {

        log.debug("fetchEntity; bookmark={}", bookmark);

        val primaryKey = primaryKeyType.destring(bookmark.getIdentifier());

        val entityManager = getEntityManager();
        val entityPojo = entityManager.find(entityClass, primaryKey);
        return Optional.ofNullable(entityPojo);
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

            val entitySpec = getEntitySpecification();
            return Can.ofStream(
                    typedQuery.getResultStream()
                            .map(entity -> ManagedObject.adaptSingular(entitySpec, entity)));

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

            val entitySpec = getEntitySpecification();
            return Can.ofStream(
                    namedQuery.getResultStream()
                            .map(entity -> ManagedObject.adaptSingular(entitySpec, entity)));

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
        val persistenceUnitUtil = getPersistenceUnitUtil(entityManager);

        if (entityManager.contains(pojo)) {
            val primaryKey = persistenceUnitUtil.getIdentifier(pojo);
            if (primaryKey == null) {
                return EntityState.PERSISTABLE_ATTACHED_NO_OID;
            }
            return EntityState.PERSISTABLE_ATTACHED;
        }

        try {
            val primaryKey = persistenceUnitUtil.getIdentifier(pojo);
            if (primaryKey == null) {
                return EntityState.PERSISTABLE_DETACHED;
            } else {
                // detect shallow primary key
                //TODO this is a hack - see whether we can actually ask the EntityManager to give us an accurate answer
                return primaryKeyType.isValid(primaryKey)
                    ? EntityState.PERSISTABLE_DETACHED_WITH_OID
                    : EntityState.PERSISTABLE_DETACHED;
            }
        } catch (PersistenceException ex) {
            /* horrible hack, but encountered NPEs if using a composite key (eg CommandLogEntry)
                (this was without any weaving) */
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

    protected EntityManager getEntityManager() {
        return jpaContext.getEntityManagerByManagedType(entityClass);
    }

    protected PersistenceUnitUtil getPersistenceUnitUtil(final EntityManager entityManager) {
        return entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
    }

}
