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
package org.apache.isis.core.transaction.changetracking;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.EntityChangeKind;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.events.lifecycle.AbstractLifecycleEvent;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionProvider;
import org.apache.isis.applib.services.publishing.spi.EntityChanges;
import org.apache.isis.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.isis.applib.services.xactn.TransactionId;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.factory._InstanceUtil;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.LifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.LoadedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.LoadedLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistedLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistingLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemovingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemovingLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatedLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatingLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.publish.entitychange.EntityChangePublishingFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.transaction.changetracking.events.IsisTransactionPlaceholder;
import org.apache.isis.core.transaction.events.TransactionBeforeCompletionEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("isis.transaction.EntityChangeTrackerDefault")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("Default")
//@InteractionScope
@Log4j2
public class EntityChangeTrackerDefault2573
extends EntityChangeTrackerAbstract {

    private final EntityPropertyChangePublisher entityPropertyChangePublisher;
    private final EntityChangesPublisher entityChangesPublisher;
    private final EventBusService eventBusService;
    @Getter(value = AccessLevel.PRIVATE) private final Provider<InteractionProvider> interactionProviderProvider;

    private ThreadLocal<_TransactionScopedContext> transactionScopedContext = ThreadLocal.withInitial(()->
        new _TransactionScopedContext(getInteractionProviderProvider()));

    @Inject
    public EntityChangeTrackerDefault2573(
            final EntityPropertyChangePublisher entityPropertyChangePublisher,
            final EntityChangesPublisher entityChangesPublisher,
            final EventBusService eventBusService,
            final Provider<InteractionProvider> interactionProviderProvider) {
        this.entityPropertyChangePublisher = entityPropertyChangePublisher;
        this.entityChangesPublisher = entityChangesPublisher;
        this.eventBusService = eventBusService;
        this.interactionProviderProvider = interactionProviderProvider;
    }



    private boolean isEnlisted(final @NonNull ManagedObject adapter) {
        return ManagedObjects.bookmark(adapter)
                .map(bookmark->transactionScopedContext.get()
                        .changeKindByEnlistedAdapter()
                        .containsKey(bookmark))
                .orElse(false);
    }

    private void enlistCreatedInternal(final @NonNull ManagedObject entity) {
        onEntityChangeRecognized();
        if(isEntityEnabledForChangePublishing(entity)) {
            transactionScopedContext.get()
            .enlistForChangeKindPublishing(entity, EntityChangeKind.CREATE);
            enlistForPreAndPostValuePublishing(entity, record->record.setPreValue(IsisTransactionPlaceholder.NEW));
        }
    }

    private void enlistUpdatingInternal(final @NonNull ManagedObject entity) {
        onEntityChangeRecognized();
        if(!isEntityEnabledForChangePublishing(entity)) {
            return;
        }
        transactionScopedContext.get()
        .enlistForChangeKindPublishing(entity, EntityChangeKind.UPDATE);
        enlistForPreAndPostValuePublishing(entity, _PropertyChangeRecord::updatePreValue);

    }

    private void enlistUpdatingInternal(
            final @NonNull ManagedObject entity,
            final @Nullable String propertyIdIfAny,
            final @Nullable Object preValue) {
        if(!isEntityEnabledForChangePublishing(entity)) {
            return;
        }
        transactionScopedContext.get()
        .enlistForChangeKindPublishing(entity, EntityChangeKind.UPDATE);

        //TODO[ISIS-2573] bring in this new code branch!?
//        if(propertyIdIfAny != null) {
//            // if we've been provided with the preValue, then just save it
//            // in the appropriate PropertyChangeRecord
//
//            if(propertyChangeRecordsById.containsKey(propertyIdIfAny)) {
//                return;
//            }
//            entity.getSpecification().getAssociation(propertyIdIfAny)
//                .filter(assoc -> !assoc.isMixedIn())
//                .filter(ObjectMember::isOneToOneAssociation)
//                .map(OneToOneAssociation.class::cast)
//                .filter(property->!property.isNotPersisted())
//                .map(property->_PropertyChangeRecord.of(entity, property))
//                .ifPresent(record -> {
//                    record.setPreValue(preValue);
//                    propertyChangeRecordsById.put(propertyIdIfAny, record);
//                });
//
//        } else {
//            // read from the pojo using the Isis MM.
//            enlistForPreAndPostValuePublishing(entity, _PropertyChangeRecord::updatePreValue);
//        }

        enlistForPreAndPostValuePublishing(entity, _PropertyChangeRecord::updatePreValue);

    }


    private void enlistDeletingInternal(final @NonNull ManagedObject entity) {
        onEntityChangeRecognized();
        if(isEntityEnabledForChangePublishing(entity)) {
            val successfullyEnlisted = transactionScopedContext.get()
                    .enlistForChangeKindPublishing(entity, EntityChangeKind.DELETE);
            if(successfullyEnlisted) {
                enlistForPreAndPostValuePublishing(entity, _PropertyChangeRecord::updatePreValue);
            }
        }
    }

    private void onEntityChangeRecognized() {
        if(transactionScopedContext.get().isAlreadyPreparedForPublishing()) {
            throw _Exceptions.illegalState("Cannot enlist additional changes for auditing, "
                    + "since changedObjectPropertiesRef was already prepared (memoized) for auditing.");
        }
        entityChangeEventCount.increment();
        enableCommandPublishing();
    }

    boolean isEntityEnabledForChangePublishing(final @NonNull ManagedObject entity) {
        // ignore entities that are not enabled for entity change publishing
        return EntityChangePublishingFacet.isPublishingEnabled(entity.getSpecification());
    }

    /**
     * TRANSACTION END BOUNDARY
     * @apiNote intended to be called during before transaction completion by the framework internally
     */
    @EventListener(value = TransactionBeforeCompletionEvent.class)
    public void onTransactionCompleting(TransactionBeforeCompletionEvent event) {
        try {
            doPublish();
        } finally {
            postPublishing();
        }
    }

    private void doPublish() {
        _Xray.publish(this::propertyChangeRecordCount, interactionProviderProvider);

        log.debug("about to publish entity changes");
        entityPropertyChangePublisher.publishChangedProperties(this);
        entityChangesPublisher.publishChangingEntities(this);
    }

    private void postPublishing() {
        debug("PURGE");
        log.debug("purging entity change records");
        transactionScopedContext.remove();
        entityChangeEventCount.reset();
        numberEntitiesLoaded.reset();
    }

    private void debug(String label) {
        _Probe.errOut("!!![%s] %s %d",
                Integer.toHexString(EntityChangeTrackerDefault2573.this.hashCode()),
                label,
                transactionScopedContext.get().entityPropertyChangeRecords().size());
    }

    private void enableCommandPublishing() {
        val alreadySet = persitentChangesEncountered.getAndSet(true);
        if(!alreadySet) {
            val command = currentInteraction().getCommand();
            command.updater().setSystemStateChanged(true);
        }
    }

    @Override
    public Optional<EntityChanges> getEntityChanges(
            final java.sql.Timestamp timestamp,
            final String userName) {
        return _ChangingEntitiesFactory.createChangingEntities(timestamp, userName, this);
    }

    @Override
    Interaction currentInteraction() {
        return interactionProviderProvider.get().currentInteractionElseFail();
    }

    // -- HELPER

    private void enlistForPreAndPostValuePublishing(
            final ManagedObject entity,
            final Consumer<_PropertyChangeRecord> onNewRecord) {

        log.debug("enlist entity's property changes for publishing {}", entity);

        val entityPropertyChangeRecords = transactionScopedContext.get()
                .entityPropertyChangeRecords();
        entity.getSpecification().streamProperties(MixedIn.EXCLUDED)
        .filter(property->!property.isNotPersisted())
        .map(property->_PropertyChangeRecord.of(entity, property))
        .filter(record->!entityPropertyChangeRecords.contains(record)) // already enlisted, so ignore
        .forEach(record->{
            onNewRecord.accept(record);
            debug("ADD");
            entityPropertyChangeRecords.add(record);
            debug("ADDED");
        });
    }

    // -- METRICS SERVICE

    @Override
    public int numberEntitiesLoaded() {
        return Math.toIntExact(numberEntitiesLoaded.longValue());
    }

    @Override
    public int numberEntitiesDirtied() {
        return transactionScopedContext.get().numberEntitiesDirtied();
    }

    // side-effect free peeking
    long propertyChangeRecordCount() {
        return transactionScopedContext.get().countPotentialPropertyChangeRecords();
    }

    @Override
    public int numberPropertyChangeRecordsThenLock() {
        return transactionScopedContext.get().snapshotPropertyChangeRecords().size();
    }

    // -- ENTITY CHANGE TRACKING

    @Override
    public void enlistCreated(ManagedObject entity) {
        _Xray.enlistCreated(entity, interactionProviderProvider);
        val hasAlreadyBeenEnlisted = isEnlisted(entity);
        enlistCreatedInternal(entity);

        if(!hasAlreadyBeenEnlisted) {
            CallbackFacet.callCallback(entity, PersistedCallbackFacet.class);
            postLifecycleEventIfRequired(entity, PersistedLifecycleEventFacet.class);
        }
    }

    @Override
    public void enlistDeleting(ManagedObject entity) {
        _Xray.enlistDeleting(entity, interactionProviderProvider);
        enlistDeletingInternal(entity);
        CallbackFacet.callCallback(entity, RemovingCallbackFacet.class);
        postLifecycleEventIfRequired(entity, RemovingLifecycleEventFacet.class);
    }


    @Override
    public void enlistUpdating(ManagedObject entity) {
        enlistUpdating(entity, null, null);
    }

    @Override
    public void enlistUpdating(ManagedObject entity, String propertyIdIfAny, Object preValue) {
        _Xray.enlistUpdating(entity, interactionProviderProvider);
        val hasAlreadyBeenEnlisted = isEnlisted(entity);
        // we call this come what may;
        // additional properties may now have been changed, and the changeKind for publishing might also be modified
        enlistUpdatingInternal(entity, propertyIdIfAny, preValue);

        if(!hasAlreadyBeenEnlisted) {
            // prevent an infinite loop... don't call the 'updating()' callback on this object if we have already done so
            CallbackFacet.callCallback(entity, UpdatingCallbackFacet.class);
            postLifecycleEventIfRequired(entity, UpdatingLifecycleEventFacet.class);
        }
    }

    @Override
    public void recognizeLoaded(ManagedObject entity) {
        _Xray.recognizeLoaded(entity, interactionProviderProvider);
        CallbackFacet.callCallback(entity, LoadedCallbackFacet.class);
        postLifecycleEventIfRequired(entity, LoadedLifecycleEventFacet.class);
        numberEntitiesLoaded.increment();
    }

    @Override
    public void recognizePersisting(ManagedObject entity) {
        _Xray.recognizePersisting(entity, interactionProviderProvider);
        CallbackFacet.callCallback(entity, PersistingCallbackFacet.class);
        postLifecycleEventIfRequired(entity, PersistingLifecycleEventFacet.class);
    }

    @Override
    public void recognizeUpdating(ManagedObject entity) {
        _Xray.recognizeUpdating(entity, interactionProviderProvider);
        CallbackFacet.callCallback(entity, UpdatedCallbackFacet.class);
        postLifecycleEventIfRequired(entity, UpdatedLifecycleEventFacet.class);
    }

    private final LongAdder numberEntitiesLoaded = new LongAdder();
    private final LongAdder entityChangeEventCount = new LongAdder();
    private final AtomicBoolean persitentChangesEncountered = new AtomicBoolean();

    //  -- HELPER

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void postLifecycleEventIfRequired(
            ManagedObject adapter,
            Class<? extends LifecycleEventFacet> lifecycleEventFacetClass) {

        val lifecycleEventFacet = adapter.getSpecification().getFacet(lifecycleEventFacetClass);
        if(lifecycleEventFacet == null) {
            return;
        }
        val eventInstance = (AbstractLifecycleEvent) _InstanceUtil
                .createInstance(lifecycleEventFacet.getEventType());
        val pojo = adapter.getPojo();
        postEvent(eventInstance, pojo);

    }

    private void postEvent(final AbstractLifecycleEvent<Object> event, final Object pojo) {
        if(eventBusService!=null) {
            event.initSource(pojo);
            eventBusService.post(event);
        }
    }

    @Override
    public Can<EntityPropertyChange> getPropertyChanges(
            final java.sql.Timestamp timestamp,
            final String userName,
            final TransactionId txId) {

        return transactionScopedContext.get()
                .snapshotPropertyChangeRecords()
                .stream()
                .map(propertyChangeRecord->_EntityPropertyChangeFactory
                        .createEntityPropertyChange(timestamp, userName, txId, propertyChangeRecord))
                .collect(Can.toCan());
    }

    @Override
    Map<Bookmark, EntityChangeKind> getChangeKindByEnlistedAdapter() {
        return transactionScopedContext.get().changeKindByEnlistedAdapter();
    }

}