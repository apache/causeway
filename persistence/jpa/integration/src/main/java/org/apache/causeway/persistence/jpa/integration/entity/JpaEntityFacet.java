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
import javax.persistence.PersistenceUnitUtil;

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
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.beans.PersistenceStack;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityOrmMetadata;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.idstringifier.IdStringifierLookupService;

import lombok.Getter;
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
        return getOrmMetadata().primaryKeyClass();
    }

    @Override
    public Can<ManagedObject> fetchByQuery(final Query<?> query) {

        val range = query.getRange();

        if (query instanceof AllInstancesQuery) {

            val queryFindAllInstances = (AllInstancesQuery<?>) query;
            val queryEntityType = queryFindAllInstances.getResultType();

            // guard against misuse
            _Assert.assertTypeIsInstanceOf(queryEntityType, entityClass);

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
        _Assert.assertNullableObjectIsInstanceOf(pojo, entityClass);

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
        _Assert.assertNullableObjectIsInstanceOf(pojo, entityClass);

        val entityManager = getEntityManager();
        entityManager.refresh(pojo);
    }

    @Override
    public void delete(final Object pojo) {
        if (pojo == null) {
            return; // nothing to do
        }

        // guard against misuse
        _Assert.assertNullableObjectIsInstanceOf(pojo, entityClass);

        val entityManager = getEntityManager();
        entityManager.remove(pojo);
    }

    @Override
    public EntityState getEntityState(final Object pojo) {

        if (pojo == null
                || !entityClass.isAssignableFrom(pojo.getClass())) {
            return EntityState.NOT_PERSISTABLE;
        }

        val entityManager = getEntityManager();
        val persistenceUnitUtil = getPersistenceUnitUtil(entityManager);

        return _JpaEntityStateUtil.getEntityState(entityManager, persistenceUnitUtil, entityClass, primaryKeyType, pojo);
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

    // lazily looks up the ORM metadata (needs an EntityManager)
    @Getter(lazy=true)
    private final EntityOrmMetadata ormMetadata =
            _MetadataUtil.ormMetadataFor(getEntityManager(), entityClass);

    // -- DEPENDENCIES

    protected EntityManager getEntityManager() {
        return jpaContext.getEntityManagerByManagedType(entityClass);
    }

    protected PersistenceUnitUtil getPersistenceUnitUtil(final EntityManager entityManager) {
        return entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
    }

}
