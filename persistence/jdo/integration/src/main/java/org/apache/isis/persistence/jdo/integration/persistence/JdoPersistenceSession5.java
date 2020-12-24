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
package org.apache.isis.persistence.jdo.integration.persistence;

import java.util.Optional;

import javax.enterprise.inject.Vetoed;
import javax.jdo.FetchGroup;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.datanucleus.enhancement.Persistable;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.adapter.oid.ObjectNotFoundException;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.PojoRefreshException;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.transaction.integration.IsisTransactionObject;
import org.apache.isis.persistence.jdo.applib.exceptions.NotPersistableException;
import org.apache.isis.persistence.jdo.applib.fixturestate.FixturesInstalledStateHolder;
import org.apache.isis.persistence.jdo.datanucleus.entities.DnEntityStateProvider;
import org.apache.isis.persistence.jdo.datanucleus.oid.JdoObjectIdSerializer;
import org.apache.isis.persistence.jdo.integration.lifecycles.IsisLifecycleListener;
import org.apache.isis.persistence.jdo.integration.lifecycles.JdoStoreLifecycleListenerForIsis;
import org.apache.isis.persistence.jdo.integration.lifecycles.LoadLifecycleListenerForIsis;
import org.apache.isis.persistence.jdo.integration.persistence.command.CreateObjectCommand;
import org.apache.isis.persistence.jdo.integration.persistence.command.DeleteObjectCommand;
import org.apache.isis.persistence.jdo.integration.persistence.query.PersistenceQuery;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * A wrapper around the JDO {@link PersistenceManager}.
 */
@Vetoed @Log4j2
public class JdoPersistenceSession5 extends _JdoPersistenceSessionBase
implements IsisLifecycleListener.PersistenceSessionLifecycleManagement {

    private final TransactionService transactionService;
    private Runnable unregisterLifecycleListeners;

    /**
     * Initialize the object store so that calls to this object store access
     * persisted objects and persist changes to the object that are saved.
     * @param storeLifecycleListener 
     */
    public JdoPersistenceSession5(
            final MetaModelContext metaModelContext,
            final PersistenceManagerFactory jdoPersistenceManagerFactory,
            final FixturesInstalledStateHolder stateHolder) {

        super(metaModelContext, jdoPersistenceManagerFactory, stateHolder);
        this.transactionService = metaModelContext.getTransactionService();
    }

    // -- open

    /**
     * Injects components, calls open on subcomponents, and then creates service
     * adapters.
     */
    @Override
    public void open() {
        state.ensureNotOpened();

        if (log.isDebugEnabled()) {
            log.debug("opening {}", this);
        }

        persistenceManager = jdoPersistenceManagerFactory.getPersistenceManager();

        final IsisLifecycleListener.PersistenceSessionLifecycleManagement psLifecycleMgmt = this;
        final IsisLifecycleListener isisLifecycleListener = new IsisLifecycleListener(psLifecycleMgmt);
        persistenceManager.addInstanceLifecycleListener(isisLifecycleListener, (Class[]) null);

        // install JDO specific entity change listeners ...
        
        val loadLifecycleListener = new LoadLifecycleListenerForIsis();
        val storeLifecycleListener = new JdoStoreLifecycleListenerForIsis();
        
        getServiceInjector().injectServicesInto(loadLifecycleListener);
        getServiceInjector().injectServicesInto(storeLifecycleListener);
            
        persistenceManager.addInstanceLifecycleListener(loadLifecycleListener, (Class[]) null);
        persistenceManager.addInstanceLifecycleListener(storeLifecycleListener, (Class[]) null);
        
        this.unregisterLifecycleListeners = ()->{
            persistenceManager.removeInstanceLifecycleListener(loadLifecycleListener);
            persistenceManager.removeInstanceLifecycleListener(storeLifecycleListener);
        };

        this.state = State.OPEN;
    }

    // -- close

    /**
     * Closes the subcomponents.
     *
     * <p>
     * Automatically {@link _TxManagerInternal#commitTransaction(IsisTransactionObject)
     * ends (commits)} the current (Isis) {@link _Tx}. This in turn commits the underlying
     * JDO transaction.
     *
     * <p>
     * The corresponding DataNucleus entity is then closed.
     */
    @Override
    public void close() {

        if (state == State.CLOSED) {
            // nothing to do
            return;
        }

        unregisterLifecycleListeners.run();
        unregisterLifecycleListeners = null;
        
        try {
            persistenceManager.close();
        } catch(final Throwable ex) {
            // ignore
            log.error(
                    "close: failed to close JDO persistenceManager; continuing to avoid memory leakage");
        }

        this.state = State.CLOSED;
    }

    @Override
    public Can<ManagedObject> allMatchingQuery(final Query<?> query) {
        val instances = findInstancesInTransaction(query, QueryCardinality.MULTIPLE);
        return instances;
    }
    
    @Override
    public Optional<ManagedObject> firstMatchingQuery(final Query<?> query) {
        val instances = findInstancesInTransaction(query, QueryCardinality.SINGLE);
        return instances.getFirst();
    }

    /**
     * Finds and returns instances that match the specified query.
     *
     * <p>
     * The {@link QueryCardinality} determines whether all instances or just the
     * first matching instance is returned.
     *
     * @throws org.apache.isis.persistence.jdo.applib.exceptions.UnsupportedFindException
     *             if the criteria is not support by this persistor
     */
    private Can<ManagedObject> findInstancesInTransaction(
            final Query<?> query, 
            final QueryCardinality cardinality) {
        
        if (log.isDebugEnabled()) {
            log.debug("findInstances using (applib) Query: {}", query);
        }

        val persistenceQuery = createPersistenceQueryFor(query, cardinality);
        if (log.isDebugEnabled()) {
            log.debug("maps to (core runtime) PersistenceQuery: {}", persistenceQuery);
        }

        final Can<ManagedObject> instances = transactionService
                .executeWithinTransaction(
                        ()->persistenceQuery.execute(this) )
                .orElseFail();
        
        return instances;
    }

    /**
     * Converts the {@link Query applib representation of a query} into the
     * {@link PersistenceQuery} framework-internal representation.
     */
    private final PersistenceQuery createPersistenceQueryFor(
            final Query<?> query,
            final QueryCardinality cardinality) {

        final PersistenceQuery persistenceQuery =
                persistenceQueryFactory.createPersistenceQueryFor(query, cardinality);
        if (persistenceQuery == null) {
            throw new IllegalArgumentException("Unknown Query type: " + query.getDescription());
        }

        return persistenceQuery;
    }

    // -- FETCHING

    @Override
    public ManagedObject fetchByIdentifier(
            final @NonNull ObjectSpecification spec, 
            final @NonNull String identifier) {

        val rootOid = Oid.Factory.root(spec.getSpecId(), identifier);
        
        log.debug("fetchEntity; oid={}", rootOid);
        
        val entity = fetchEntity(spec, rootOid); // throws if null
        
        return ManagedObject.identified(spec, entity, rootOid);
    }
    
    private Object fetchEntity(final ObjectSpecification spec, final RootOid rootOid) {

        Object result;
        try {
            val cls = spec.getCorrespondingClass();
            val jdoObjectId = JdoObjectIdSerializer.toJdoObjectId(spec, rootOid);
            val fetchPlan = persistenceManager.getFetchPlan();
            fetchPlan.addGroup(FetchGroup.DEFAULT);
            result = persistenceManager.getObjectById(cls, jdoObjectId);
        } catch (final RuntimeException e) {

            //XXX this idiom could be delegated to a service
            //or remodel the method to return a Result<T>
            for (val exceptionRecognizer : getMetaModelContext().getServiceRegistry()
                    .select(ExceptionRecognizer.class)) {
                val recognition = exceptionRecognizer.recognize(e).orElse(null);
                if(recognition != null) {
                    if(recognition.getCategory() == ExceptionRecognizer.Category.NOT_FOUND) {
                        throw new ObjectNotFoundException(rootOid, e);
                    }
                }
            }

            throw e;
        }

        if (result == null) {
            throw new ObjectNotFoundException(rootOid);
        }

        return result;
    }

    // -- REFRESH

    @Override
    public void refreshEntity(final Object domainObject) {

        val state = DnEntityStateProvider.entityState(domainObject);
        val isRepresentingPersistent = state.isAttached() || state.isDestroyed();  

        if(!isRepresentingPersistent) {
            debugLogNotPersistentIgnoring(domainObject);
            return; // only resolve object that is representing persistent
        }

        debugLogRefreshImmediately(domainObject);

        try {
            persistenceManager.refresh(domainObject);
        } catch (final RuntimeException e) {
            final Oid oid = oidFor(domainObject);
            throw new PojoRefreshException(oid, e);
        }

        // possibly redundant because also called in the post-load event
        // listener, but (with JPA impl) found it was required if we were ever to
        // get an eager left-outer-join as the result of a refresh (sounds possible).
        initializeEntity((Persistable) domainObject);
    }

    // -- makePersistent


    @Override
    public void makePersistentInTransaction(final ManagedObject adapter) {
        
        val pojo = adapter.getPojo();
        
        if (DnEntityStateProvider.entityState(pojo).isAttached()) {
            throw new NotPersistableException("Object already persistent: " + adapter);
        }
        val spec = adapter.getSpecification();
        if (spec.isManagedBean()) {
            throw new NotPersistableException("Can only persist entity beans: "+ adapter);
        }
        if (spec.getBeanSort().isCollection()) {
            //XXX not sure if we can do better than that, eg. traverse each element of the collection and persist individually
            throw new NotPersistableException("Cannot persist a collection: " + adapter);
        }
        
        log.debug("persist {}", adapter);
        state.ensureOpened();
        
        transactionService.executeWithinTransaction(()->{
            commandQueue.addCommand(newCreateObjectCommand(adapter));
        });
    }
    
    // -- destroyObjectInTransaction

    @Override
    public void destroyObjectInTransaction(final ManagedObject adapter) {
        val spec = adapter.getSpecification();
        if (spec.isParented()) {
            return;
        }
        
        log.debug("deleteObject {}", adapter);
        state.ensureOpened();
        
        transactionService.executeWithinTransaction(()->{
            commandQueue.addCommand(newDeleteObjectCommand(adapter));
        });
    }

    // -- newXxxCommand
    /**
     * Makes an {@link ObjectAdapter} persistent. The specified object should be
     * stored away via this object store's persistence mechanism, and have an
     * new and unique OID assigned to it (by calling the object's
     * <code>setOid</code> method). The object, should also be added to the
     * cache as the object is implicitly 'in use'.
     *
     * <p>
     * If the object has any associations then each of these, where they aren't
     * already persistent, should also be made persistent by recursively calling
     * this method.
     * </p>
     *
     * <p>
     * If the object to be persisted is a collection, then each element of that
     * collection, that is not already persistent, should be made persistent by
     * recursively calling this method.
     * </p>
     *
     */
    private CreateObjectCommand newCreateObjectCommand(final ManagedObject adapter) {

        val pojo = adapter.getPojo();

        log.debug("create object - creating command for: {}", adapter);
        if (DnEntityStateProvider.entityState(pojo).isAttached()) {
            throw new IllegalArgumentException("Adapter is persistent; adapter: " + adapter);
        }
        return new CreateObjectCommand(adapter);
    }

    private DeleteObjectCommand newDeleteObjectCommand(final ManagedObject adapter) {
        
        val pojo = adapter.getPojo();

        log.debug("destroy object - creating command for: {}", adapter);
        if (!DnEntityStateProvider.entityState(pojo).isAttached()) {
            throw new IllegalArgumentException("Adapter is not persistent; adapter: " + adapter);
        }
        return new DeleteObjectCommand(adapter);
    }

    // -- FrameworkSynchronizer delegate methods

    @Override
    public void enlistDeletingAndInvokeIsisRemovingCallbackFacet(final Persistable pojo) {
        val entity = adapterFor(pojo);
        getEntityChangeTracker().enlistDeleting(entity);
    }

    @Override
    public ManagedObject initializeEntity(final Persistable pojo) {

        final ManagedObject entity = _Utils
                .identify(getMetaModelContext(), getPersistenceManager(), pojo);

        getEntityChangeTracker().recognizeLoaded(entity);

        return entity;
    }

    @Override
    public String identifierFor(final Object pojo) {
        return JdoObjectIdSerializer.identifierForElseFail(getPersistenceManager(), pojo);
    }

    /**
     * Called either when an entity is initially persisted, or when an entity is updated; fires the appropriate
     * lifecycle callback.
     *
     * <p>
     * The implementation therefore uses Isis' {@link Oid#isTransient() oid}
     * to determine which callback to fire.
     */
    @Override
    public void invokeIsisPersistingCallback(final Persistable pojo) {
        if (DnEntityStateProvider.entityState(pojo).isDetached()) {
            val entity = ManagedObject.of(
                    getMetaModelContext().getSpecificationLoader()::loadSpecification, 
                    pojo);

            getEntityChangeTracker().recognizePersisting(entity);

        } else {
            // updating

            // don't call here, already called in preDirty.

            // CallbackFacet.Util.callCallback(adapter, UpdatingCallbackFacet.class);
        }
    }

    /**
     * Called either when an entity is initially persisted, or when an entity is updated;
     * fires the appropriate lifecycle callback
     *
     * <p>
     * The implementation therefore uses Isis' {@link Oid#isTransient() oid}
     * to determine which callback to fire.
     */
    @Override
    public void enlistCreatedAndInvokeIsisPersistedCallback(final Persistable pojo) {
        val entity = adapterFor(pojo);
        getEntityChangeTracker().enlistCreated(entity);
    }

    @Override
    public void enlistUpdatingAndInvokeIsisUpdatingCallback(final Persistable pojo) {
        val entity = _Utils.fetchEntityElseFail(getMetaModelContext(), getPersistenceManager(), pojo);
        getEntityChangeTracker().enlistUpdating(entity);
    }

    @Override
    public void invokeIsisUpdatedCallback(Persistable pojo) {
        val entity = _Utils.fetchEntityElseFail(getMetaModelContext(), getPersistenceManager(), pojo);
        // the callback and transaction.enlist are done in the preStore callback
        // (can't be done here, as the enlist requires to capture the 'before' values)
        getEntityChangeTracker().recognizeUpdating(entity);
    }

    @Override
    public boolean isRecognized(Object pojo) {
        if (pojo!=null && pojo instanceof Persistable) {
            final Object jdoOid = getPersistenceManager().getObjectId(pojo);
            if(jdoOid!=null) {
                return true;
            }
        }
        return false;
    }
    
    // -- HELPER
    
    private void debugLogNotPersistentIgnoring(Object domainObject) {
        if (log.isDebugEnabled() && domainObject!=null) {
            val oid = oidFor(domainObject);
            log.debug("; oid={} not persistent - ignoring", oid.enString());
        }     
    }

    private void debugLogRefreshImmediately(Object domainObject) {
        if (log.isDebugEnabled()) {
            val oid = oidFor(domainObject);
            log.debug("refresh immediately; oid={}", oid.enString());
        }
    }

}



