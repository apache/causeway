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
package org.apache.isis.persistence.jdo.integration.changetracking;

import javax.enterprise.inject.Vetoed;
import javax.jdo.listener.AttachLifecycleListener;
import javax.jdo.listener.ClearLifecycleListener;
import javax.jdo.listener.CreateLifecycleListener;
import javax.jdo.listener.DeleteLifecycleListener;
import javax.jdo.listener.DetachLifecycleListener;
import javax.jdo.listener.DirtyLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.LoadLifecycleListener;
import javax.jdo.listener.StoreLifecycleListener;

import org.datanucleus.enhancement.Persistable;

import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.transaction.changetracking.EntityChangeTracker;
import org.apache.isis.core.transaction.changetracking.events.PostStoreEvent;
import org.apache.isis.core.transaction.changetracking.events.PreStoreEvent;
import org.apache.isis.persistence.jdo.datanucleus.entities.DnEntityStateProvider;
import org.apache.isis.persistence.jdo.integration.metamodel.JdoMetamodelUtil;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Vetoed // managed by isis
@RequiredArgsConstructor
public class JdoLifecycleListener
implements AttachLifecycleListener, ClearLifecycleListener, CreateLifecycleListener, DeleteLifecycleListener,
DetachLifecycleListener, DirtyLifecycleListener, LoadLifecycleListener, StoreLifecycleListener {

    private final @NonNull MetaModelContext metaModelContext;
    private final @NonNull EventBusService eventBusService;

    /////////////////////////////////////////////////////////////////////////
    // callbacks
    /////////////////////////////////////////////////////////////////////////

    @Override
    public void postCreate(final InstanceLifecycleEvent event) {
        // no-op
    }

    @Override
    public void preAttach(final InstanceLifecycleEvent event) {
        // no-op
    }

    @Override
    public void postAttach(final InstanceLifecycleEvent event) {
        // no-op
    }

    @Override
    public void postLoad(final InstanceLifecycleEvent event) {
        final Persistable pojo = _Utils.persistableFor(event);
        adaptEntityAndInjectServices(pojo);
        getEntityChangeTracker().incrementLoaded();
    }

    @Override
    public void preStore(InstanceLifecycleEvent event) {

        val persistableObject = event.getPersistentInstance();

        if(persistableObject!=null 
                && JdoMetamodelUtil.isPersistenceEnhanced(persistableObject.getClass())) {

            eventBusService.post(PreStoreEvent.of(persistableObject));
        }
        
        final Persistable pojo = _Utils.persistableFor(event);
        if(pojo.dnGetStateManager().isNew(pojo)) {
            invokeIsisPersistingCallback(pojo);
        }
        
    }

    @Override
    public void postStore(InstanceLifecycleEvent event) {

        val persistableObject = event.getPersistentInstance();

        if(persistableObject!=null && 
                JdoMetamodelUtil.isPersistenceEnhanced(persistableObject.getClass())) {

            eventBusService.post(PostStoreEvent.of(persistableObject));
        }
        
        final Persistable pojo = _Utils.persistableFor(event);
        if(pojo.dnGetStateManager().isNew(pojo)) {
            enlistCreatedAndInvokeIsisPersistedCallback(pojo);
        } else {
            invokeIsisUpdatedCallback(pojo);
        }
        
    }
    

    @Override
    public void preDirty(InstanceLifecycleEvent event) {
        final Persistable pojo = _Utils.persistableFor(event);
        enlistUpdatingAndInvokeIsisUpdatingCallback(pojo);
    }

    @Override
    public void postDirty(InstanceLifecycleEvent event) {
        // cannot assert on the frameworks being in agreement, due to the scenario documented
        // in the FrameworkSynchronizer#preDirtyProcessing(...)
        //
        // 1<->m bidirectional, persistence-by-reachability

        // no-op
    }

    @Override
    public void preDelete(InstanceLifecycleEvent event) {
        final Persistable pojo = _Utils.persistableFor(event);
        enlistDeletingAndInvokeIsisRemovingCallbackFacet(pojo);
    }

    @Override
    public void postDelete(InstanceLifecycleEvent event) {

        // previously we called the PersistenceSession to invoke the removed callback (if any).
        // however, this is almost certainly incorrect, because DN will not allow us
        // to "touch" the pojo once deleted.
        //
        // CallbackFacet.Util.callCallback(adapter, RemovedCallbackFacet.class);

    }

    /**
     * Does nothing, not important event for Isis to track.
     */
    @Override
    public void preClear(InstanceLifecycleEvent event) {
        // ignoring, not important to us
    }

    /**
     * Does nothing, not important event for Isis to track.
     */
    @Override
    public void postClear(InstanceLifecycleEvent event) {
        // ignoring, not important to us
    }

    @Override
    public void preDetach(InstanceLifecycleEvent event) {
        // no-op
    }

    @Override
    public void postDetach(InstanceLifecycleEvent event) {
        // no-op
    }
    
    // -- HELPER
    
    private ManagedObject adaptEntityAndInjectServices(final @NonNull Persistable pojo) {
        return _Utils.adaptEntityAndInjectServices(metaModelContext, pojo);
    }

    private void enlistDeletingAndInvokeIsisRemovingCallbackFacet(final Persistable pojo) {
        val entity = adaptEntityAndInjectServices(pojo);
        getEntityChangeTracker().enlistDeleting(entity);
    }

    /**
     * Called either when an entity is initially persisted, or when an entity is updated; fires the appropriate
     * lifecycle callback.
     *
     * <p>
     * The implementation therefore uses Isis' {@link Oid#isTransient() oid}
     * to determine which callback to fire.
     */
    private void invokeIsisPersistingCallback(final Persistable pojo) {
        if (DnEntityStateProvider.entityState(pojo).isDetached()) {
            val entity = ManagedObject.of(
                    metaModelContext.getSpecificationLoader()::loadSpecification, 
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
    private void enlistCreatedAndInvokeIsisPersistedCallback(final Persistable pojo) {
        val entity = adaptEntityAndInjectServices(pojo);
        getEntityChangeTracker().enlistCreated(entity);
    }

    private void enlistUpdatingAndInvokeIsisUpdatingCallback(final Persistable pojo) {
        val entity = ManagedObject.of(
                metaModelContext.getSpecificationLoader()::loadSpecification, 
                pojo);
        getEntityChangeTracker().enlistUpdating(entity);
    }

    private void invokeIsisUpdatedCallback(Persistable pojo) {
        val entity = ManagedObject.of(
                metaModelContext.getSpecificationLoader()::loadSpecification, 
                pojo);
        // the callback and transaction.enlist are done in the preStore callback
        // (can't be done here, as the enlist requires to capture the 'before' values)
        getEntityChangeTracker().recognizeUpdating(entity);
    }
            
    // -- DEPENDENCIES
    
    @Getter(lazy = true)
    private final EntityChangeTracker entityChangeTracker = 
        metaModelContext.getServiceRegistry()
            .lookupServiceElseFail(EntityChangeTracker.class);

}
