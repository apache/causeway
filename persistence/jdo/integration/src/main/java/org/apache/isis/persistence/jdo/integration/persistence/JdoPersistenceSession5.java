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

import javax.annotation.Nullable;
import javax.enterprise.inject.Vetoed;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.datanucleus.enhancement.Persistable;

import org.apache.isis.applib.services.xactn.TransactionalProcessor;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.transaction.changetracking.EntityChangeTracker;
import org.apache.isis.persistence.jdo.datanucleus.entities.DnEntityStateProvider;
import org.apache.isis.persistence.jdo.integration.lifecycles.FetchResultHandler;
import org.apache.isis.persistence.jdo.integration.lifecycles.IsisLifecycleListener;
import org.apache.isis.persistence.jdo.integration.lifecycles.JdoStoreLifecycleListenerForIsis;
import org.apache.isis.persistence.jdo.integration.lifecycles.LoadLifecycleListenerForIsis;
import org.apache.isis.persistence.jdo.integration.transaction.TxManagerInternalFactory;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * A wrapper around the JDO {@link PersistenceManager}.
 */
@Vetoed @Log4j2
public class JdoPersistenceSession5
implements
    JdoPersistenceSession,
    FetchResultHandler,
    IsisLifecycleListener.EntityChangeEmitter {

    // -- FIELDS

    /**
     * populated only when {@link #open()}ed.
     */
    @Getter(onMethod_ = {@Override}) private PersistenceManager persistenceManager;
    
    @Getter(onMethod_ = {@Override}) private final TransactionalProcessor transactionalProcessor;
    @Getter(onMethod_ = {@Override}) private final MetaModelContext metaModelContext;

    /**
     * Used to create the {@link #persistenceManager} when {@link #open()}ed.
     */
    private final PersistenceManagerFactory jdoPersistenceManagerFactory;
    
    private Runnable unregisterLifecycleListeners;

    // -- CONSTRUCTOR
    
    /**
     * Initialize the object store so that calls to this object store access
     * persisted objects and persist changes to the object that are saved.
     */
    public JdoPersistenceSession5(
            final MetaModelContext metaModelContext,
            final PersistenceManagerFactory jdoPersistenceManagerFactory) {

        if (log.isDebugEnabled()) {
            log.debug("creating {}", this);
        }

        this.metaModelContext = metaModelContext;
        this.jdoPersistenceManagerFactory = jdoPersistenceManagerFactory;

        // sub-components
        this.transactionalProcessor = TxManagerInternalFactory.newTransactionalProcessor(
                metaModelContext, 
                this); 

        this.state = State.NOT_INITIALIZED;
    }

    // -- STATE

    protected enum State {
        NOT_INITIALIZED, OPEN, CLOSED
        ;
        protected void ensureNotOpened() {
            if (this != State.NOT_INITIALIZED) {
                throw new IllegalStateException("Persistence session has already been initialized");
            }
        }
        protected void ensureOpened() {
            ensureStateIs(State.OPEN);
        }
        private void ensureStateIs(final State stateRequired) {
            if (this == stateRequired) {
                return;
            }
            throw new IllegalStateException("State is: " + this + "; should be: " + stateRequired);
        }
    }
    
    protected State state = State.NOT_INITIALIZED;

    // -- OPEN


    @Override
    public void open() {
        state.ensureNotOpened();

        if (log.isDebugEnabled()) {
            log.debug("opening {}", this);
        }

        persistenceManager = jdoPersistenceManagerFactory.getPersistenceManager();

        final IsisLifecycleListener.EntityChangeEmitter psLifecycleMgmt = this;
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

    // -- CLOSE

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
    public ManagedObject adaptEntityAndInjectServices(final @NonNull Persistable pojo) {
        return _Utils.adaptEntityAndInjectServices(getMetaModelContext(), pojo);
    }

    @Override
    public void enlistDeletingAndInvokeIsisRemovingCallbackFacet(final Persistable pojo) {
        val entity = adaptEntityAndInjectServices(pojo);
        getEntityChangeTracker().enlistDeleting(entity);
    }

    @Override
    public ManagedObject initializeEntityAfterFetched(final Persistable pojo) {

        final ManagedObject entity = _Utils
                .identify(getMetaModelContext(), getPersistenceManager(), pojo);

        getEntityChangeTracker().recognizeLoaded(entity);

        return entity;
    }
    
    @Override
    public ManagedObject initializeValueAfterFetched(final @Nullable Object pojo) {
        return _Utils.adaptNullableAndInjectServices(getMetaModelContext(), pojo);
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
        val entity = adaptEntityAndInjectServices(pojo);
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
    
    // -- DEPENDENCIES
    
    private EntityChangeTracker getEntityChangeTracker() {
        return metaModelContext.getServiceRegistry()
                .lookupServiceElseFail(EntityChangeTracker.class);
    }

}



