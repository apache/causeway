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
import java.util.function.Function;

import org.apache.causeway.applib.query.AllInstancesQuery;
import org.apache.causeway.applib.query.NamedQuery;
import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.repository.EntityState;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData.PersistenceStack;
import org.apache.causeway.core.config.metamodel.facets.DomainObjectConfigOptions.LockingPolicy;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration.ObservationProvider;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityOrmMetadata;
import org.apache.causeway.core.metamodel.facets.object.locking.LockingFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.idstringifier.IdStringifierLookupService;
import org.apache.causeway.persistence.jpa.applib.integration.HasVersion;
import org.apache.causeway.persistence.jpa.integration.CausewayModulePersistenceJpaIntegration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaContext;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.TypedQuery;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import io.micrometer.observation.Observation;

@Slf4j
class JpaEntityFacet
        extends FacetAbstract
        implements EntityFacet {

    // self managed injections via constructor
    @Inject private JpaContext jpaContext;
    @Inject private IdStringifierLookupService idStringifierLookupService;
    @Inject private OrmMetadataProvider ormMetadataProvider;
    @Inject private CausewayObservationIntegration observationIntegration;

    private final Class<?> entityClass;
    private final PrimaryKeyType<?> primaryKeyType;
    private final ObservationProvider observationProvider;

    protected JpaEntityFacet(
            final FacetHolder holder,
            final Class<?> entityClass) {
        super(EntityFacet.class, holder);
        getServiceInjector().injectServicesInto(this);

        this.entityClass = entityClass;
        this.primaryKeyType = idStringifierLookupService
                .primaryKeyTypeFor(entityClass, getPrimaryKeyType());
        var timeThreshold = observationIntegration.policy().jpaDurationThreshold();
        this.observationProvider = observationIntegration.provider(getClass(),
                CausewayObservationIntegration.withModuleName(CausewayModulePersistenceJpaIntegration.NAMESPACE)
                .andThen(obs->observationIntegration.withTimeThreshold(obs, timeThreshold)));
    }

    @Override
    public Precedence precedence() {
    	return Precedence.HIGH;
    }

    // -- ENTITY FACET

    @Override
    public PersistenceStack getPersistenceStack() {
        return PersistenceStack.JPA;
    }

    @Override
    public Optional<String> identifierFor(final @Nullable Object pojo) {

        if (!getEntityState(pojo).hasOid())
            return Optional.empty();

        var entityManager = getEntityManager();
        var persistenceUnitUtil = getPersistenceUnitUtil(entityManager);
        var primaryKeyIfAny = persistenceUnitUtil.getIdentifier(pojo);

        return Optional.ofNullable(primaryKeyIfAny)
                .map(primaryKey->
                    primaryKeyType.enstringWithCast(primaryKey));
    }

    @Override
    public Bookmark validateBookmark(final @NonNull Bookmark bookmark) {
        _Assert.assertNotNull(primaryKeyType.destring(bookmark.identifier()));
        return bookmark;
    }

    @Override
    public Optional<Object> fetchByBookmark(final @NonNull Bookmark bookmark) {
        log.debug("fetchEntity; bookmark={}", bookmark);
        return observation("fetch-by-bookmark", "Fetch by Bookmark")
            .observe(()->{
                var primaryKey = primaryKeyType.destring(bookmark.identifier());
                var policy = facetHolder().lookupFacet(LockingFacet.class)
                        .map(LockingFacet::getPolicy).orElse(LockingPolicy.OPTIMISTIC);
                return Optional.ofNullable(policy == LockingPolicy.PESSIMISTIC
                        ? getEntityManager().find(entityClass, primaryKey, LockModeType.PESSIMISTIC_WRITE)
                        : getEntityManager().find(entityClass, primaryKey));
            });
    }

    private Class<?> getPrimaryKeyType() {
        return getOrmMetadata().primaryKeyClass();
    }

    @Override
    public Can<ManagedObject> fetchByQuery(final Query<?> query) {

        var range = query.getRange();

        var entitySpec = getEntitySpecification();
        final Function<Object, ManagedObject> adapter = entity -> ManagedObject.adaptSingular(entitySpec, entity);

        if (query instanceof AllInstancesQuery) {
            var queryFindAllInstances = (AllInstancesQuery<?>) query;
            var queryEntityType = queryFindAllInstances.getResultType();

            // guard against misuse
            _Assert.assertTypeIsInstanceOf(queryEntityType, entityClass);

            var typedQuery = selectFrom(entityClass);
            if (range.hasOffset()) {
                typedQuery.setFirstResult(range.getStartAsInt());
            }
            if (range.hasLimit()) {
                typedQuery.setMaxResults(range.getLimitAsInt());
            }

            var obs = observation("fetch-all", "Fetch all Instances");

            return obs.observe(()->
                    Can.ofStream(typedQuery.getResultStream().map(adapter)));

        } else if (query instanceof NamedQuery) {
            var applibNamedQuery = (NamedQuery<?>) query;
            var queryResultType = applibNamedQuery.getResultType();

            var entityManager = getEntityManager();

            var namedQuery = entityManager
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

            var obs = observation("named-query", "Named Query")
                    .lowCardinalityKeyValue("causeway.query.name", applibNamedQuery.getName())
                    .contextualName("Named Query " + entityClass.getName() + " " + applibNamedQuery.getName());
            return obs.observe(()->
                    Can.ofStream(namedQuery.getResultStream().map(adapter)));
        }

        throw _Exceptions.unsupportedOperation(
                "Support for Query of type %s not implemented.", query.getClass());
    }

    @Override
    public void persist(final Object pojo) {
        if (pojo == null)
            return; // nothing to do

        // guard against misuse
        _Assert.assertNullableObjectIsInstanceOf(pojo, entityClass);

        var entityManager = getEntityManager();

        log.debug("about to persist entity {}", pojo);

        observation("persist", "Persist")
            .observe(()->entityManager.persist(pojo));
    }

    @Override @Nullable
    public <T> T refresh(final @Nullable T pojo) {
        if (pojo == null) return pojo; // nothing to do

        // guard against misuse
        _Assert.assertNullableObjectIsInstanceOf(pojo, entityClass);

        var entityManager = getEntityManager();

        observation("refresh", "Refresh")
            .observe(()->entityManager.refresh(pojo));

        return pojo;
    }

    @Override
    public void delete(final Object pojo) {
        if (pojo == null)
            return; // nothing to do

        // guard against misuse
        _Assert.assertNullableObjectIsInstanceOf(pojo, entityClass);

        var entityManager = getEntityManager();

        observation("remove", "Remove")
            .observe(()->entityManager.remove(pojo));
    }

    @Override
    public EntityState getEntityState(final Object pojo) {

        if (pojo == null
                || !entityClass.isAssignableFrom(pojo.getClass()))
            return EntityState.NOT_PERSISTABLE;

        var entityManager = getEntityManager();
        var persistenceUnitUtil = getPersistenceUnitUtil(entityManager);

        return _JpaEntityStateUtil.getEntityState(entityManager, persistenceUnitUtil, entityClass, primaryKeyType, pojo);
    }

    @Override
    public Object versionOf(final Object pojo) {
        if (getEntityState(pojo).isAttached()) {
            if (pojo instanceof HasVersion)
                return ((HasVersion<?>)pojo).getVersion();
        }
        return null;
    }

    @Override
    public boolean isProxyEnhancement(final Method method) {
        return false;
    }

    @Override
    public <T> T detach(final T pojo) {
        if (pojo == null)
            return null;

        // guard against misuse
        _Assert.assertNullableObjectIsInstanceOf(pojo, entityClass);

        var entityManager = getEntityManager();

        observation("detach", "Detach")
            .observe(()->entityManager.detach(pojo));

        return pojo;
    }

    /** Only static entity metadata enters telemetry; never a bookmark or query description. */
    private Observation observation(final String operation, final String label) {
        return observationProvider.get("causeway.jpa." + operation)
                .contextualName(label + " " + entityClass.getName())
                .lowCardinalityKeyValue("causeway.entity.type", entityClass.getName());
    }

    // -- JPA METAMODEL

    // lazily looks up the ORM metadata (needs an EntityManager)
    @Getter(lazy=true)
    private final EntityOrmMetadata ormMetadata =
            ormMetadataProvider.ormMetadataFor(getEntityManager(), entityClass);

    // -- DEPENDENCIES

    protected EntityManager getEntityManager() {
        return jpaContext.getEntityManagerByManagedType(entityClass);
    }

    protected PersistenceUnitUtil getPersistenceUnitUtil(final EntityManager entityManager) {
        return entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
    }

    // -- HELPER

    private <T> TypedQuery<T> selectFrom(final Class<T> entityClass) {
        var entityManager = getEntityManager();
        var q = entityManager.getCriteriaBuilder().createQuery(entityClass);
        q.select(q.from(entityClass));
        return entityManager
                .createQuery(q);
    }
}
