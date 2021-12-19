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
package org.apache.isis.persistence.jdo.datanucleus.changetracking;

import javax.enterprise.inject.Vetoed;
import javax.inject.Provider;
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
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager.EntityAdaptingMode;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.transaction.changetracking.EntityChangeTracker;
import org.apache.isis.core.transaction.changetracking.events.PostStoreEvent;
import org.apache.isis.core.transaction.changetracking.events.PreStoreEvent;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * <ul>
 * <li>enlistCreated <-> postStore (when NEW)</li>
 * <li>enlistDeleting <-> preDelete</li>
 * <li>enlistUpdating <-> preDirty</li>
 * <li>recognizeLoaded <-> postLoad</li>
 * <li>recognizePersisting <-> preStore (when NEW)</li>
 * <li>recognizeUpdating <-> postStore (when NOT NEW)</li>
 * </ul>
 *
 * @since 2.0 {@index}
 */
@Vetoed // managed by isis
@RequiredArgsConstructor
@Log4j2
public class JdoLifecycleListener
implements AttachLifecycleListener, ClearLifecycleListener, CreateLifecycleListener, DeleteLifecycleListener,
DetachLifecycleListener, DirtyLifecycleListener, LoadLifecycleListener, StoreLifecycleListener {

    private final @NonNull MetaModelContext metaModelContext;
    private final @NonNull EventBusService eventBusService;
    private final @NonNull Provider<EntityChangeTracker> entityChangeTrackerProvider;

    // -- CALLBACKS

    @Override
    public void postCreate(final InstanceLifecycleEvent event) {
        log.debug("postCreate {}", ()->_Utils.debug(event));
        _Utils.resolveInjectionPoints(metaModelContext, event);
    }

    @Override
    public void preAttach(final InstanceLifecycleEvent event) {
        log.debug("preAttach {}", ()->_Utils.debug(event));
    }

    @Override
    public void postAttach(final InstanceLifecycleEvent event) {
        log.debug("postAttach {}", ()->_Utils.debug(event));
        _Utils.resolveInjectionPoints(metaModelContext, event);
    }

    @Override
    public void postLoad(final InstanceLifecycleEvent event) {
        log.debug("postLoad {}", ()->_Utils.debug(event));
        final Persistable pojo = _Utils.persistableFor(event);
        val entity = adaptEntityAndInjectServices(pojo, EntityAdaptingMode.MEMOIZE_BOOKMARK);
        getEntityChangeTracker().recognizeLoaded(entity);
    }

    @Override
    public void preStore(final InstanceLifecycleEvent event) {
        log.debug("preStore {}", ()->_Utils.debug(event));

        final Persistable pojo = _Utils.persistableFor(event);

        eventBusService.post(PreStoreEvent.of(pojo));

        /* Called either when an entity is initially persisted, or when an entity is updated; fires the appropriate
         * lifecycle callback. So filter for those events when initially persisting. */
        if(pojo.dnGetStateManager().isNew(pojo)) {
            val entity = adaptEntity(pojo, EntityAdaptingMode.SKIP_MEMOIZATION);
            getEntityChangeTracker().recognizePersisting(entity);
        }
    }

    @Override
    public void postStore(final InstanceLifecycleEvent event) {
        log.debug("postStore {}", ()->_Utils.debug(event));

        final Persistable pojo = _Utils.persistableFor(event);

        val entity = adaptEntityAndInjectServices(pojo, EntityAdaptingMode.MEMOIZE_BOOKMARK);

        eventBusService.post(PostStoreEvent.of(pojo));

        /* Called either when an entity is initially persisted, or when an entity is updated;
         * fires the appropriate lifecycle callback.*/
        if(pojo.dnGetStateManager().isNew(pojo)) {

            getEntityChangeTracker().enlistCreated(entity);
        } else {
            // the callback and transaction.enlist are done in the preStore callback
            // (can't be done here, as the enlist requires to capture the 'before' values)
            getEntityChangeTracker().recognizeUpdating(entity);
        }

    }


    @Override
    public void preDirty(final InstanceLifecycleEvent event) {
        log.debug("preDirty {}", ()->_Utils.debug(event));

        final Persistable pojo = _Utils.persistableFor(event);
        val entity = adaptEntity(pojo, EntityAdaptingMode.MEMOIZE_BOOKMARK);
        getEntityChangeTracker().enlistUpdating(entity);
    }

    @Override
    public void postDirty(final InstanceLifecycleEvent event) {
        log.debug("postDirty {}", ()->_Utils.debug(event));
    }

    @Override
    public void preDelete(final InstanceLifecycleEvent event) {
        log.debug("preDelete {}", ()->_Utils.debug(event));

        final Persistable pojo = _Utils.persistableFor(event);
        val entity = adaptEntity(pojo, EntityAdaptingMode.SKIP_MEMOIZATION);
        getEntityChangeTracker().enlistDeleting(entity);
    }

    @Override
    public void postDelete(final InstanceLifecycleEvent event) {
        log.debug("postDelete {}", ()->_Utils.debug(event));
    }

    /**
     * Does nothing, not important event for Isis to track.
     */
    @Override
    public void preClear(final InstanceLifecycleEvent event) {
        log.debug("preClear {}", ()->_Utils.debug(event));
    }

    /**
     * Does nothing, not important event for Isis to track.
     */
    @Override
    public void postClear(final InstanceLifecycleEvent event) {
        log.debug("postClear {}", ()->_Utils.debug(event));
    }

    @Override
    public void preDetach(final InstanceLifecycleEvent event) {
        log.debug("preDetach {}", ()->_Utils.debug(event));
    }

    @Override
    public void postDetach(final InstanceLifecycleEvent event) {
        log.debug("postDetach {}", ()->_Utils.debug(event));
        _Utils.resolveInjectionPoints(metaModelContext, event);
    }

    // -- HELPER

    private ManagedObject adaptEntity(
            final @NonNull Persistable pojo,
            final @NonNull EntityAdaptingMode bookmarking) {
        return _Utils.adaptEntity(metaModelContext, pojo, bookmarking);
    }

    private ManagedObject adaptEntityAndInjectServices(
            final @NonNull Persistable pojo,
            final @NonNull EntityAdaptingMode bookmarking) {
        return _Utils.adaptEntityAndInjectServices(metaModelContext, pojo, bookmarking);
    }

    // -- DEPENDENCIES

    private EntityChangeTracker getEntityChangeTracker() {
        return entityChangeTrackerProvider.get();
    }

}
