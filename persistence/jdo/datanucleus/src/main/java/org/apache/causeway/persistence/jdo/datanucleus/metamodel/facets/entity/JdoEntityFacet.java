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
import javax.jdo.PersistenceManager;

import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.store.rdbms.RDBMSPropertyNames;

import org.apache.causeway.applib.exceptions.unrecoverable.ObjectNotFoundException;
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
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.services.idstringifier.IdStringifierLookupService;
import org.apache.causeway.core.metamodel.services.objectlifecycle.ObjectLifecyclePublisher;
import org.apache.causeway.persistence.jdo.datanucleus.entities.DnEntityStateProvider;
import org.apache.causeway.persistence.jdo.datanucleus.entities.DnObjectProviderForCauseway;
import org.apache.causeway.persistence.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacetFactory;
import org.apache.causeway.persistence.jdo.provider.entities.JdoFacetContext;
import org.apache.causeway.persistence.jdo.spring.integration.TransactionAwarePersistenceManagerFactoryProxy;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
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

    // self managed injections via getPersistenceManager or getTransactionalProcessor
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
            .primaryKeyTypeFor(entityClass, primaryKeyTypeFor(entityClass));

    public JdoEntityFacet(
            final FacetHolder holder, final Class<?> entityClass) {
        super(EntityFacet.class, holder);
        this.entityClass = entityClass;
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
        val actualPrimaryKeyClass = oid.getClass();
        val primaryKeyType = primaryKeyTypesForEncoding.computeIfAbsent(actualPrimaryKeyClass, key->
            idStringifierLookupService()
                .primaryKeyTypeFor(entityClass, actualPrimaryKeyClass));
        return primaryKeyType;
    }

    @Override
    public boolean isInjectionPointsResolved(final Object pojo) {
        if(pojo instanceof Persistable) {
            DnObjectProviderForCauseway.extractFrom((Persistable) pojo)
            .map(DnObjectProviderForCauseway::injectServicesIfNotAlready)
            .orElse(false);
        }
        return pojo==null;
    }

    @Override
    public Optional<String> identifierFor(final Object pojo) {

        if (!getEntityState(pojo).hasOid()) {
            return Optional.empty();
        }

        val pm = getPersistenceManager();
        var primaryKeyIfAny = Optional.ofNullable(pm.getObjectId(pojo));

        _Assert.assertTrue(primaryKeyIfAny.isPresent(), ()->
            String.format("failed to get OID even though entity is attached %s", pojo.getClass().getName()));

        val idIfAny = primaryKeyIfAny
                .map(primaryKey->
                    primaryKeyTypeForEncoding(primaryKey).enstringWithCast(primaryKey));
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

            val persistenceManager = getPersistenceManager();
            val primaryKey = primaryKeyTypeForDecoding().destring(bookmark.getIdentifier());

            val fetchPlan = persistenceManager.getFetchPlan();
            fetchPlan.addGroup(FetchGroup.DEFAULT);
            entityPojo = persistenceManager.getObjectById(entityClass, primaryKey);

        } catch (final RuntimeException e) {

            val recognition = exceptionRecognizerService.recognize(e);
            if(recognition.isPresent()) {
                if(recognition.get().getCategory() == Category.NOT_FOUND) {
                    throw new ObjectNotFoundException(""+bookmark, e);
                }
            }

            throw e;
        }

        return Optional.ofNullable(entityPojo);
    }

    private Map<Class<?>, Class<?>> primaryKeyClassByEntityClass = new ConcurrentHashMap<>();

    private Class<?> primaryKeyTypeFor(final Class<?> entityClass) {
        return primaryKeyClassByEntityClass.computeIfAbsent(entityClass, this::lookupPrimaryKeyTypeFor);
    }

    private Class<?> lookupPrimaryKeyTypeFor(final Class<?> entityClass) {

        val persistenceManager = getPersistenceManager();
        val pmf = (JDOPersistenceManagerFactory) persistenceManager.getPersistenceManagerFactory();
        val nucleusContext = pmf.getNucleusContext();

        val contextLoader = Thread.currentThread().getContextClassLoader();
        val clr = nucleusContext.getClassLoaderResolver(contextLoader);

        val typeMetadata = pmf.getMetadata(entityClass.getName());

        val identityType = typeMetadata.getIdentityType();
        switch (identityType) {
            case APPLICATION:
                String objectIdClass = typeMetadata.getObjectIdClass();
                return clr.classForName(objectIdClass);
            case DATASTORE:
                return nucleusContext.getIdentityManager().getDatastoreIdClass();
            case UNSPECIFIED:
            case NONDURABLE:
            default:
                throw new IllegalStateException(String.format(
                        "JdoEntityFacet has been incorrectly installed on '%s' which has an supported identityType of '%s'",
                        entityClass.getName(), identityType));
        }
    }

    @Override
    public Can<ManagedObject> fetchByQuery(final Query<?> query) {

        if (log.isDebugEnabled()) {
            log.debug("about to execute Query: {}", query.getDescription());
        }

        val range = query.getRange();

        if(query instanceof AllInstancesQuery) {

            val queryFindAllInstances = (AllInstancesQuery<?>) query;
            val queryEntityType = queryFindAllInstances.getResultType();

            val persistenceManager = getPersistenceManager();

            val typedQuery = persistenceManager.newJDOQLTypedQuery(queryEntityType);
            typedQuery.extension(RDBMSPropertyNames.PROPERTY_RDBMS_QUERY_MULTIVALUED_FETCH, "none");

            if(!range.isUnconstrained()) {
                typedQuery.range(range.getStart(), range.getEnd());
            }

            val resultList = fetchWithinTransaction(typedQuery::executeList);

            if(range.hasLimit()) {
                _Assert.assertTrue(resultList.size()<=range.getLimit());
            }

            return resultList;

        } else if(query instanceof NamedQuery) {

            val applibNamedQuery = (NamedQuery<?>) query;
            val queryResultType = applibNamedQuery.getResultType();

            val persistenceManager = getPersistenceManager();

            val namedParams = _Maps.<String, Object>newHashMap();
            val namedQuery = persistenceManager.newNamedQuery(queryResultType, applibNamedQuery.getName())
                    .setNamedParameters(namedParams);
            namedQuery.extension(RDBMSPropertyNames.PROPERTY_RDBMS_QUERY_MULTIVALUED_FETCH, "none");

            if(!range.isUnconstrained()) {
                namedQuery.range(range.getStart(), range.getEnd());
            }

            // inject services into query params; not sure if required (might be redundant)
            {
                val injector = getServiceInjector();

                applibNamedQuery
                .getParametersByName()
                .values()
                .forEach(injector::injectServicesInto);
            }

            applibNamedQuery
                .getParametersByName()
                .forEach(namedParams::put);

            val resultList = fetchWithinTransaction(namedQuery::executeList);

            if(range.hasLimit()) {
                _Assert.assertTrue(resultList.size()<=range.getLimit());
            }

            return resultList;
        }

        throw _Exceptions.unsupportedOperation("query type %s (%s) not supported by this persistence implementation",
                query.getClass(),
                query.getDescription());
    }

    @Override
    public void persist(final Object pojo) {

        if(pojo==null
                || !isPersistableType(pojo.getClass())
                || DnEntityStateProvider.entityState(pojo).hasOid()) {
            return; // nothing to do
        }

        val pm = getPersistenceManager();

        log.debug("about to persist entity {}", pojo);

        getTransactionalProcessor()
        .runWithinCurrentTransactionElseCreateNew(()->pm.makePersistent(pojo))
        .ifFailureFail();
    }

    @Override
    public void delete(final Object pojo) {

        if(pojo==null || !isPersistableType(pojo.getClass())) {
            return; // nothing to do
        }

        if (!DnEntityStateProvider.entityState(pojo).hasOid()) {
            throw _Exceptions.illegalArgument("can only delete an attached entity");
        }

        val pm = getPersistenceManager();

        log.debug("about to delete entity {}", pojo);

        getTransactionalProcessor()
        .runWithinCurrentTransactionElseCreateNew(()->pm.deletePersistent(pojo))
        .ifFailureFail();
    }

    @Override
    public void refresh(final Object pojo) {

        if(pojo==null
                || !isPersistableType(pojo.getClass())
                || !DnEntityStateProvider.entityState(pojo).isPersistable()) {
            return; // nothing to do
        }

        val pm = getPersistenceManager();

        log.debug("about to refresh entity {}", pojo);

        getTransactionalProcessor()
        .runWithinCurrentTransactionElseCreateNew(()->pm.refresh(pojo))
        .ifFailureFail();
    }

    @Override
    public EntityState getEntityState(final Object pojo) {
        return DnEntityStateProvider.entityState(pojo);
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
        if(pmf==null) {
            getFacetHolder().getServiceInjector().injectServicesInto(this);
        }
        return pmf.getPersistenceManagerFactory().getPersistenceManager();
    }

    private TransactionalProcessor getTransactionalProcessor() {
        if(txService==null) {
            getFacetHolder().getServiceInjector().injectServicesInto(this);
        }
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
            val entity = objectManager.adapt(fetchedObject);
            objectLifecyclePublisher.onPostLoad(entity);
            return entity;
        } else {
            // a value type
            return objectManager.adapt(fetchedObject);
        }
    }

}
