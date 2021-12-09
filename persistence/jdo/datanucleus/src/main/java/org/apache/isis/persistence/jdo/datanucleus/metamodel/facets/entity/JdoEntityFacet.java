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
package org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.jdo.FetchGroup;
import javax.jdo.PersistenceManager;

import org.datanucleus.enhancement.Persistable;
import org.datanucleus.store.rdbms.RDBMSPropertyNames;

import org.apache.isis.applib.exceptions.unrecoverable.ObjectNotFoundException;
import org.apache.isis.applib.query.AllInstancesQuery;
import org.apache.isis.applib.query.NamedQuery;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.exceprecog.Category;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerService;
import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionalProcessor;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.debug._Debug;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.facets.object.entity.PersistenceStandard;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.transaction.changetracking.EntityChangeTracker;
import org.apache.isis.persistence.jdo.datanucleus.entities.DnEntityStateProvider;
import org.apache.isis.persistence.jdo.datanucleus.oid.JdoObjectIdSerializer;
import org.apache.isis.persistence.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacetFactory;
import org.apache.isis.persistence.jdo.provider.entities.JdoFacetContext;
import org.apache.isis.persistence.jdo.spring.integration.TransactionAwarePersistenceManagerFactoryProxy;

import lombok.NonNull;
import lombok.val;
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

    @Inject private TransactionAwarePersistenceManagerFactoryProxy pmf;
    @Inject private TransactionService txService;
    @Inject private ObjectManager objectManager;
    @Inject private ExceptionRecognizerService exceptionRecognizerService;
    @Inject private JdoFacetContext jdoFacetContext;

    public JdoEntityFacet(
            final FacetHolder holder) {
        super(EntityFacet.class, holder);
    }

    @Override
    public PersistenceStandard getPersistenceStandard() {
        return PersistenceStandard.JDO;
    }

    @Override
    public String identifierFor(final ObjectSpecification spec, final Object pojo) {

        if(pojo==null) {
            throw _Exceptions.illegalArgument(
                    "The persistence layer cannot identify a pojo that is null (given type %s)",
                    spec.getCorrespondingClass().getName());
        }

        if(!isPersistableType(pojo.getClass())) {
            throw _Exceptions.illegalArgument(
                    "The persistence layer does not recognize given type %s",
                    pojo.getClass().getName());
        }

        val pm = getPersistenceManager();
        var primaryKey = pm.getObjectId(pojo);

//        if(primaryKey==null) {
//            pm.makePersistent(pojo);
//            primaryKey = pm.getObjectId(pojo);
//        }

        if(primaryKey==null) {

            _Debug.onCondition(XrayUi.isXrayEnabled(), ()->{
                _Debug.log("detached entity detected %s", pojo);
            });

            throw _Exceptions.illegalArgument(
                    "The persistence layer does not recognize given object of type %s, "
                    + "meaning the object has no identifier that associates it with the persistence layer. "
                    + "(most likely, because the object is detached, eg. was not persisted after being new-ed up)",
                    pojo.getClass().getName());
        }

        final String identifier = JdoObjectIdSerializer.toOidIdentifier(primaryKey);
        if(_Strings.isEmpty(identifier)) {
            throw _Exceptions.illegalArgument(
                    "JdoObjectIdSerializer failed to convert primary key %s to a String.",
                    primaryKey.getClass().getName());
        }

        return identifier;
    }

    @Override
    public ManagedObject fetchByIdentifier(
            final @NonNull ObjectSpecification entitySpec,
            final @NonNull Bookmark bookmark) {

        _Assert.assertTrue(entitySpec.isEntity());

        log.debug("fetchEntity; bookmark={}", bookmark);

        Object entityPojo;
        try {
            val primaryKey = JdoObjectIdSerializer.toJdoObjectId(entitySpec, bookmark);
            val persistenceManager = getPersistenceManager();
            val entityClass = entitySpec.getCorrespondingClass();
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

        if (entityPojo == null) {
            throw new ObjectNotFoundException(""+bookmark);
        }

        val actualEntitySpec = getSpecificationLoader().specForTypeElseFail(entityPojo.getClass());
        getServiceInjector().injectServicesInto(entityPojo); // might be redundant
        //TODO integrate with entity change tracking
        return ManagedObject.bookmarked(actualEntitySpec, entityPojo, bookmark);
    }

    @Override
    public Can<ManagedObject> fetchByQuery(final ObjectSpecification spec, final Query<?> query) {
        if(!spec.isEntity()) {
            throw _Exceptions.unexpectedCodeReach();
        }

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
    public void persist(final ObjectSpecification spec, final Object pojo) {

        if(pojo==null
                || !isPersistableType(pojo.getClass())
                || DnEntityStateProvider.entityState(pojo).isAttached()) {
            return; // nothing to do
        }

        val pm = getPersistenceManager();

        log.debug("about to persist entity {}", pojo);

        getTransactionalProcessor()
        .runWithinCurrentTransactionElseCreateNew(()->pm.makePersistent(pojo))
        .optionalElseFail();

        //TODO integrate with entity change tracking
    }

    @Override
    public void delete(final ObjectSpecification spec, final Object pojo) {

        if(pojo==null || !isPersistableType(pojo.getClass())) {
            return; // nothing to do
        }

        if (!DnEntityStateProvider.entityState(pojo).isAttached()) {
            throw _Exceptions.illegalArgument("can only delete an attached entity");
        }

        val pm = getPersistenceManager();

        log.debug("about to delete entity {}", pojo);

        getTransactionalProcessor()
        .runWithinCurrentTransactionElseCreateNew(()->pm.deletePersistent(pojo))
        .optionalElseFail();

        //TODO integrate with entity change tracking
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
        .optionalElseFail();

        //TODO integrate with entity change tracking
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

    // -- INTERACTION TRACKER LAZY LOOKUP

    // memoizes the lookup, just an optimization
//    private final _Lazy<InteractionLayerTracker> isisInteractionTrackerLazy = _Lazy.threadSafe(
//            ()->getServiceRegistry().lookupServiceElseFail(InteractionLayerTracker.class));

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

//    private JdoPersistenceSession getJdoPersistenceSession() {
//        return isisInteractionTrackerLazy.get().currentInteractionSession()
//                .map(interactionSession->interactionSession.getAttribute(JdoPersistenceSession.class))
//                .orElseThrow(()->_Exceptions.illegalState("no JdoPersistenceSession on current thread"));
//    }

    // -- HELPER

    private Can<ManagedObject> fetchWithinTransaction(final Supplier<List<?>> fetcher) {

        val entityChangeTracker = getFacetHolder().getServiceRegistry().lookupServiceElseFail(EntityChangeTracker.class);

        return getTransactionalProcessor().callWithinCurrentTransactionElseCreateNew(
                ()->_NullSafe.stream(fetcher.get())
                    .map(fetchedObject->adopt(entityChangeTracker, fetchedObject))
                    .collect(Can.toCan()))
                .presentElseFail();
    }

    private ManagedObject adopt(final EntityChangeTracker entityChangeTracker, final Object fetchedObject) {
        // handles lifecycle callbacks and injects services

        // ought not to be necessary, however for some queries it seems that the
        // lifecycle listener is not called
        if(fetchedObject instanceof Persistable) {
            // an entity
            val entity = objectManager.adapt(fetchedObject);
                    //fetchResultHandler.initializeEntityAfterFetched((Persistable) fetchedObject);
            entityChangeTracker.recognizeLoaded(entity);
            return entity;
        } else {
            // a value type
            return objectManager.adapt(fetchedObject);
        }
    }


}
