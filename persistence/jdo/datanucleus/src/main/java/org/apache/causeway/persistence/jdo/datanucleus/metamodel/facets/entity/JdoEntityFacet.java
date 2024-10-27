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
package org.apache.causeway.persistence.jdo.datanucleus.metamodel.facets.entity;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.jdo.FetchGroup;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.datanucleus.api.jdo.JDOQuery;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.store.rdbms.RDBMSPropertyNames;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.query.AllInstancesQuery;
import org.apache.causeway.applib.query.NamedQuery;
import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.exceprecog.Category;
import org.apache.causeway.applib.services.exceprecog.ExceptionRecognizerService;
import org.apache.causeway.applib.services.repository.EntityState;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.applib.services.xactn.TransactionalProcessor;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.beans.PersistenceStack;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityOrmMetadata;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.services.idstringifier.IdStringifierLookupService;
import org.apache.causeway.core.metamodel.services.objectlifecycle.ObjectLifecyclePublisher;
import org.apache.causeway.persistence.jdo.datanucleus.entities.DnEntityStateProvider;
import org.apache.causeway.persistence.jdo.datanucleus.entities.DnOidStoreAndRecoverHelper;
import org.apache.causeway.persistence.jdo.datanucleus.entities.DnStateManagerForCauseway;
import org.apache.causeway.persistence.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacetFactory;
import org.apache.causeway.persistence.jdo.provider.entities.JdoFacetContext;
import org.apache.causeway.persistence.jdo.spring.integration.TransactionAwarePersistenceManagerFactoryProxy;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * @apiNote Does not have its own (exclusive) FacetFactory, but is installed via
 * {@link JdoPersistenceCapableFacetFactory} by means of dependency inversion using
 * {@link JdoFacetContext}
 */
@Log4j2
public class JdoEntityFacet
extends FacetAbstract
implements EntityFacet {

    // self managed injections via constructor ...
    @Inject private TransactionAwarePersistenceManagerFactoryProxy pmf;
    @Inject private TransactionService txService;
    @Inject private ObjectManager objectManager;
    @Inject private ExceptionRecognizerService exceptionRecognizerService;
    @Inject private JdoFacetContext jdoFacetContext;
    @Inject private ObjectLifecyclePublisher objectLifecyclePublisher;

    @Getter(value = AccessLevel.PROTECTED) @Accessors(fluent = true)
    @Inject private IdStringifierLookupService idStringifierLookupService;

    private final Class<?> entityClass;

    // lazily looks up the primaryKeyTypeFor (needs a PersistenceManager)
    @Getter(lazy=true, value = AccessLevel.PROTECTED) @Accessors(fluent = true)
    private final PrimaryKeyType<?> primaryKeyTypeForDecoding = idStringifierLookupService()
            .primaryKeyTypeFor(entityClass, getOrmMetadata().primaryKeyClass());

    // lazily looks up the ORM metadata (needs a PersistenceManager)
    @Getter(lazy=true)
    private final EntityOrmMetadata ormMetadata =
            _MetadataUtil.ormMetadataFor(getPersistenceManager(), entityClass);

    public JdoEntityFacet(
            final FacetHolder holder, final Class<?> entityClass) {
        super(EntityFacet.class, holder);
        this.entityClass = entityClass;

        _Assert.assertTrue(isPersistableType(entityClass),
                ()->String.format("JdoEntityFacet initialized with type '%s' "
                        + "that is not Persistable (JDO)", entityClass));

        // resolve injection points on self
        getServiceInjector().injectServicesInto(this);
    }

    @Override
    public PersistenceStack getPersistenceStack() {
        return PersistenceStack.JDO;
    }

    /* OPTIMIZATION
     * even though the IdStringifierLookupService already holds a lookup map,
     * for performance reasons,
     * we define another one local to the context of the associated entity class */
    private final Map<Class<?>, PrimaryKeyType<?>> primaryKeyTypesForEncoding = new ConcurrentHashMap<>();
    private final PrimaryKeyType<?> primaryKeyTypeForEncoding(final @NonNull Object oid) {
        var actualPrimaryKeyClass = oid.getClass();
        var primaryKeyType = primaryKeyTypesForEncoding.computeIfAbsent(actualPrimaryKeyClass, key->
            idStringifierLookupService()
                .primaryKeyTypeFor(entityClass, actualPrimaryKeyClass));
        return primaryKeyType;
    }

    @Override
    public boolean isInjectionPointsResolved(final Object pojo) {
        if(pojo instanceof Persistable) {
            DnStateManagerForCauseway.extractFrom((Persistable) pojo)
            .map(DnStateManagerForCauseway::injectServicesIfNotAlready)
            .orElse(false);
        }
        return pojo==null;
    }

    @Override
    public Optional<String> identifierFor(final Object pojo) {

        var entityState = getEntityState(pojo);

        if (!entityState.hasOid()) {
            return Optional.empty();
        }

        if(entityState.isHollow()) {
            /* for previously attached objects that have become hollow,
             * the OID can be looked up in DnStateManagerForHollow,
             * that simply acts as a holder of OID. */
            return DnOidStoreAndRecoverHelper.forEntity((Persistable)pojo).recoverOid();
        }

        var pm = getPersistenceManager();
        var primaryKey = pm.getObjectId(pojo);

        _Assert.assertNotNull(primaryKey, ()->
            String.format("failed to get OID even though entity is attached %s", pojo.getClass().getName()));

        return identifierForDnPrimaryKey(primaryKey);
    }

    public Optional<String> identifierForDnPrimaryKey(final @Nullable Object primaryKey) {
        var idIfAny = Optional.ofNullable(primaryKey)
                .map(pk->
                    primaryKeyTypeForEncoding(pk).enstringWithCast(pk));
        return idIfAny;
    }

    @Override
    public Bookmark validateBookmark(final @NonNull Bookmark bookmark) {
        _Assert.assertNotNull(primaryKeyTypeForDecoding().destring(bookmark.getIdentifier()));
        return bookmark;
    }

    @Override
    public Optional<Object> fetchByBookmark(final @NonNull Bookmark bookmark) {

        log.debug("fetchEntity; bookmark={}", bookmark);

        Object entityPojo;
        try {

            var persistenceManager = getPersistenceManager();
            var primaryKey = primaryKeyTypeForDecoding().destring(bookmark.getIdentifier());

            var fetchPlan = persistenceManager.getFetchPlan();
            fetchPlan.addGroup(FetchGroup.DEFAULT);
            entityPojo = persistenceManager.getObjectById(entityClass, primaryKey);

        } catch (final RuntimeException e) {

            var recognition = exceptionRecognizerService.recognize(e);
            if(recognition.isPresent()) {
                if(recognition.get().getCategory() == Category.NOT_FOUND) {
                    return Optional.empty();
                }
            }

            throw e;
        }

        return Optional.ofNullable(entityPojo);
    }

    @Override
    public Can<ManagedObject> fetchByQuery(final Query<?> query) {

        if (log.isDebugEnabled()) {
            log.debug("about to execute Query: {}", query.getDescription());
        }

        var range = query.getRange();

        if(query instanceof AllInstancesQuery) {

            var queryFindAllInstances = (AllInstancesQuery<?>) query;
            var queryEntityType = queryFindAllInstances.getResultType();

            // guard against misuse
            _Assert.assertTypeIsInstanceOf(queryEntityType, entityClass);

            var persistenceManager = getPersistenceManager();

            var typedQuery = persistenceManager.newJDOQLTypedQuery(queryEntityType);
            typedQuery.extension(RDBMSPropertyNames.PROPERTY_RDBMS_QUERY_MULTIVALUED_FETCH, "none");

            if(!range.isUnconstrained()) {
                typedQuery.range(range.getStart(), range.getEnd());
            }

            var resultList = fetchWithinTransaction(typedQuery::executeList);

            if(range.hasLimit()) {
                _Assert.assertTrue(resultList.size()<=range.getLimit());
            }

            return resultList;

        } else if(query instanceof NamedQuery) {

            var applibNamedQuery = (NamedQuery<?>) query;
            var queryResultType = applibNamedQuery.getResultType();

            var persistenceManager = getPersistenceManager();

            var namedParams = _Maps.<String, Object>newHashMap();
            var namedQuery = persistenceManager.newNamedQuery(queryResultType, applibNamedQuery.getName())
                    .setNamedParameters(namedParams);

            namedQuery.extension(RDBMSPropertyNames.PROPERTY_RDBMS_QUERY_MULTIVALUED_FETCH, "none");

            if(!range.isUnconstrained()) {
                namedQuery.range(range.getStart(), range.getEnd());
            }

            // inject services into query params; not sure if required (might be redundant)
            {
                var injector = getServiceInjector();

                applibNamedQuery
                .getParametersByName()
                .values()
                .forEach(injector::injectServicesInto);
            }

            applibNamedQuery
                .getParametersByName()
                .forEach(namedParams::put);

            Supplier<List<?>> executeMethod = hasResultPhrase(namedQuery)
                    ? namedQuery::executeResultList     // eg SELECT DISTINCT this.paymentMethod FROM IncomingInvoice WHERE ...
                    : namedQuery::executeList;          // eg SELECT FROM IncomingInvoice WHERE ...
            var resultList = fetchWithinTransaction(executeMethod);

            if(range.hasLimit()) {
                _Assert.assertTrue(resultList.size()<=range.getLimit());
            }

            return resultList;
        }

        throw _Exceptions.unsupportedOperation("query type %s (%s) not supported by this persistence implementation",
                query.getClass(),
                query.getDescription());
    }

    private static boolean hasResultPhrase(final javax.jdo.Query<?> namedQuery) {
        if (namedQuery instanceof JDOQuery) {
            JDOQuery<?> jdoQuery = (JDOQuery<?>) namedQuery;
            return jdoQuery.getInternalQuery().getResult() != null;
        }
        return false;
    }

    @Override
    public void persist(final Object pojo) {
        // guard against misuse
        _Assert.assertNullableObjectIsInstanceOf(pojo, entityClass);

        if(pojo==null
                || DnEntityStateProvider.entityState(pojo).isAttached()) {
            return; // nothing to do
        }

        var pm = getPersistenceManager();

        log.debug("about to persist entity {}", pojo);

        getTransactionalProcessor()
        .runWithinCurrentTransactionElseCreateNew(()->pm.makePersistent(pojo))
        .ifFailureFail();
    }

    @Override
    public void refresh(final Object pojo) {
        if(pojo==null) {
            return; // nothing to do
        }

        // guard against misuse
        _Assert.assertNullableObjectIsInstanceOf(pojo, entityClass);

        var pm = getPersistenceManager();

        log.debug("about to refresh entity {}", pojo);

        getTransactionalProcessor()
        .runWithinCurrentTransactionElseCreateNew(()->pm.refresh(pojo))
        .ifFailureFail();
    }

    @Override
    public void delete(final Object pojo) {
        if(pojo==null) {
            return; // nothing to do
        }

        // guard against misuse
        _Assert.assertNullableObjectIsInstanceOf(pojo, entityClass);

        if (!DnEntityStateProvider.entityState(pojo).hasOid()) {
            throw _Exceptions.illegalArgument("can only delete an entity with an OID");
        }

        var pm = getPersistenceManager();

        log.debug("about to delete entity {}", pojo);

        getTransactionalProcessor()
        .runWithinCurrentTransactionElseCreateNew(()->pm.deletePersistent(pojo))
        .ifFailureFail();
    }

    @Override
    public EntityState getEntityState(final Object pojo) {
        return DnEntityStateProvider.entityState(pojo);
    }

    @Override
    public Object versionOf(Object pojo) {
        if (getEntityState(pojo).isAttached()) {
            return JDOHelper.getVersion(pojo);
        }
        return null;
    }

    @Override
    public <T> T detach(final T pojo) {
        return getPersistenceManager().detachCopy(pojo);
    }

    // -- HELPER

    private static boolean isPersistableType(final Class<?> type) {
        return Persistable.class.isAssignableFrom(type);
    }

    @Override
    public boolean isProxyEnhancement(final Method method) {
        return jdoFacetContext.isMethodProvidedByEnhancement(method);
    }

    // -- DEPENDENCIES

    private PersistenceManager getPersistenceManager() {
        return pmf.getPersistenceManagerFactory().getPersistenceManager();
    }

    private TransactionalProcessor getTransactionalProcessor() {
        return txService;
    }

    // -- HELPER

    private Can<ManagedObject> fetchWithinTransaction(final Supplier<List<?>> fetcher) {
        return getTransactionalProcessor().callWithinCurrentTransactionElseCreateNew(
                ()->_NullSafe.stream(fetcher.get())
                    .map(fetchedObject->adapt(objectLifecyclePublisher, fetchedObject))
                    .collect(Can.toCan()))
                .ifFailureFail()
                .getValue().orElseThrow();
    }

    private ManagedObject adapt(
            final ObjectLifecyclePublisher objectLifecyclePublisher,
            final Object fetchedObject) {
        // handles lifecycle callbacks and injects services

        // ought not to be necessary, however for some queries it seems that the
        // lifecycle listener is not called
        if(fetchedObject instanceof Persistable) {
            // an entity
            var entity = objectManager.adapt(fetchedObject);
            objectLifecyclePublisher.onPostLoad(entity);
            return entity;
        } else {
            // a value type
            return objectManager.adapt(fetchedObject);
        }
    }

}
