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
 *
 */
package org.apache.isis.persistence.jpa.integration.changetracking;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.EntityChangeKind;
import org.apache.isis.applib.annotation.InteractionScope;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionProvider;
import org.apache.isis.applib.services.metrics.MetricsService;
import org.apache.isis.applib.services.publishing.spi.EntityChanges;
import org.apache.isis.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.isis.applib.services.xactn.TransactionId;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facets.object.publish.entitychange.EntityChangePublishingFacet;
import org.apache.isis.core.metamodel.facets.properties.property.entitychangepublishing.EntityPropertyChangePublishingPolicyFacet;
import org.apache.isis.core.metamodel.services.objectlifecycle.HasEnlistedEntityPropertyChanges;
import org.apache.isis.core.metamodel.services.objectlifecycle.PropertyChangeRecord;
import org.apache.isis.core.metamodel.services.objectlifecycle.PropertyChangeRecordId;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.transaction.changetracking.EntityChangeTracker;
import org.apache.isis.core.transaction.changetracking.EntityChangesPublisher;
import org.apache.isis.core.transaction.changetracking.EntityPropertyChangePublisher;
import org.apache.isis.core.transaction.changetracking.HasEnlistedEntityChanges;
import org.apache.isis.core.transaction.changetracking.PersistenceCallbackHandlerAbstract;
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
@Named("isis.persistence.commons.EntityChangeTrackerDefault")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("default")
@InteractionScope
@Log4j2
public class EntityChangeTrackerDefault
extends PersistenceCallbackHandlerAbstract
implements
    MetricsService,
    EntityChangeTracker,
    HasEnlistedEntityPropertyChanges,
    HasEnlistedEntityChanges {


    /**
     * Contains a record for every objectId/propertyId that was changed.
     */
    private final Map<PropertyChangeRecordId, PropertyChangeRecord> enlistedPropertyChangeRecordsById = _Maps.newLinkedHashMap();

    /**
     * Contains pre- and post- values of every property of every object that actually changed. A lazy snapshot,
     * triggered by internal call to {@link #snapshotPropertyChangeRecords()}.
     */
    private final _Lazy<Set<PropertyChangeRecord>> entityPropertyChangeRecordsForPublishing
        = _Lazy.threadSafe(this::capturePostValuesAndDrain);


    @Getter(AccessLevel.PACKAGE)
    private final Map<Bookmark, EntityChangeKind> changeKindByEnlistedAdapter = _Maps.newLinkedHashMap();


    private final EntityPropertyChangePublisher entityPropertyChangePublisher;
    private final EntityChangesPublisher entityChangesPublisher;
    private final Provider<InteractionProvider> interactionProviderProvider;

    private final LongAdder numberEntitiesLoaded = new LongAdder();
    private final LongAdder entityChangeEventCount = new LongAdder();
    private final AtomicBoolean persistentChangesEncountered = new AtomicBoolean();

    @Inject
    public EntityChangeTrackerDefault(
            final EntityPropertyChangePublisher entityPropertyChangePublisher,
            final EntityChangesPublisher entityChangesPublisher,
            final EventBusService eventBusService,
            final Provider<InteractionProvider> interactionProviderProvider) {
        super(eventBusService);
        this.entityPropertyChangePublisher = entityPropertyChangePublisher;
        this.entityChangesPublisher = entityChangesPublisher;
        this.interactionProviderProvider = interactionProviderProvider;
    }

    private boolean isEnlistedWrtChangeKind(final @NonNull ManagedObject adapter) {
        return ManagedObjects.bookmark(adapter)
        .map(changeKindByEnlistedAdapter::containsKey)
        .orElse(false);
    }

    Set<PropertyChangeRecord> snapshotPropertyChangeRecords() {
        // this code path has side-effects, it locks the result for this transaction,
        // such that cannot enlist on top of it
        return entityPropertyChangeRecordsForPublishing.get();
    }

    private boolean isEntityExcludedForChangePublishing(ManagedObject entity) {

        if(!EntityChangePublishingFacet.isPublishingEnabled(entity.getSpecification())) {
            return true; // ignore entities that are not enabled for entity change publishing
        }

        if(entityPropertyChangeRecordsForPublishing.isMemoized()) {
            throw _Exceptions.illegalState("Cannot enlist additional changes for auditing, "
                    + "since changedObjectPropertiesRef was already prepared (memoized) for auditing.");
        }

        return false;
    }

    /**
     * TRANSACTION END BOUNDARY
     * @apiNote intended to be called during before transaction completion by the framework internally
     */
    @EventListener(value = TransactionBeforeCompletionEvent.class) @Order(PriorityPrecedence.LATE)
    public void onTransactionCompleting(final TransactionBeforeCompletionEvent event) {
        try {
            doPublish();
        } finally {
            postPublishing();
        }
    }

    private void doPublish() {
        _Xray.publish(this, interactionProviderProvider);

        log.debug("about to publish entity changes");
        entityPropertyChangePublisher.publishChangedProperties();
        entityChangesPublisher.publishChangingEntities(this);
    }

    private void postPublishing() {
        log.debug("purging entity change records");

        enlistedPropertyChangeRecordsById.clear();
        entityPropertyChangeRecordsForPublishing.clear();

        changeKindByEnlistedAdapter.clear();
        entityChangeEventCount.reset();
        numberEntitiesLoaded.reset();
    }

    private void enableCommandPublishing() {
        val alreadySet = persistentChangesEncountered.getAndSet(true);
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
    public Can<EntityPropertyChange> getPropertyChanges(
            final java.sql.Timestamp timestamp,
            final String userName,
            final TransactionId txId) {
        return snapshotPropertyChangeRecords().stream()
                .map(propertyChangeRecord -> propertyChangeRecord.toEntityPropertyChange(timestamp, userName, txId))
                .collect(Can.toCan());
    }

    // -- DEPENDENCIES

    Interaction currentInteraction() {
        return interactionProviderProvider.get().currentInteractionElseFail();
    }

    // -- HELPER

    /**
     * @return <code>true</code> if successfully enlisted, <code>false</code> if not (no longer) enlisted ... eg delete of an entity that was created earlier in the transaction
     */
    private boolean enlistForChangeKindPublishing(
            final @NonNull ManagedObject entity,
            final @NonNull EntityChangeKind changeKind) {

        entityChangeEventCount.increment();
        enableCommandPublishing();

        val bookmark = ManagedObjects.bookmarkElseFail(entity);

        val previousChangeKind = changeKindByEnlistedAdapter.get(bookmark);
        if(previousChangeKind == null) {
            changeKindByEnlistedAdapter.put(bookmark, changeKind);
            return true;
        }
        switch (previousChangeKind) {
        case CREATE:
            switch (changeKind) {
            case DELETE:
                changeKindByEnlistedAdapter.remove(bookmark);
            case CREATE:
            case UPDATE:
                return false;
            }
            break;
        case UPDATE:
            switch (changeKind) {
            case DELETE:
                changeKindByEnlistedAdapter.put(bookmark, changeKind);
                return true;
            case CREATE:
            case UPDATE:
                return false;
            }
            break;
        case DELETE:
            return false;
        }
        return false;
    }

    /**
     * For any enlisted Object Properties collects those, that are meant for publishing,
     * then clears enlisted objects.
     */
    private Set<PropertyChangeRecord> capturePostValuesAndDrain() {

        val records = enlistedPropertyChangeRecordsById.values().stream()
                // set post values, which have been left empty up to now
                .peek(rec -> {
                    // assuming this check correctly detects deleted entities (JDO)
                    if(ManagedObjects.EntityUtil.isDetachedOrRemoved(rec.getEntity())) {
                        rec.updatePostValueAsDeleted();
                    } else {
                        rec.updatePostValueWithCurrent();
                    }
                })
                .filter(managedProperty->managedProperty.getPreAndPostValue().shouldPublish())
                .collect(_Sets.toUnmodifiable());

        enlistedPropertyChangeRecordsById.clear();

        return records;

    }

    // side-effect free, used by XRay
    long countPotentialPropertyChangeRecords() {
        return enlistedPropertyChangeRecordsById.size();
    }

    // -- ENTITY CHANGE TRACKING

    @Override
    public void enlistCreated(final ManagedObject entity) {

        _Xray.enlistCreated(entity, interactionProviderProvider);

        if (isEntityExcludedForChangePublishing(entity)) {
            return;
        }

        log.debug("enlist entity's property changes for publishing {}", entity);
        enlistForChangeKindPublishing(entity, EntityChangeKind.CREATE);

        enlistForCreateOrUpdate(entity, PropertyChangeRecord::updatePreValueAsNew);
    }

    @Override
    public void enlistUpdating(
            final ManagedObject entity,
            @Nullable final Can<PropertyChangeRecord> ormPropertyChangeRecords) {

        _Xray.enlistUpdating(entity, interactionProviderProvider);

        if (isEntityExcludedForChangePublishing(entity)) {
            return;
        }

        // we call this come what may;
        // additional properties may now have been changed, and the changeKind for publishing might also be modified
        enlistForChangeKindPublishing(entity, EntityChangeKind.UPDATE);

        if(ormPropertyChangeRecords != null) {
            // provided by ORM
            ormPropertyChangeRecords
                    .stream()
                    .filter(pcr -> !EntityPropertyChangePublishingPolicyFacet.isExcludedFromPublishing(pcr.getProperty()))
                    .forEach(pcr -> this.enlistedPropertyChangeRecordsById.put(pcr.getId(), pcr)); // if already known, then we don't replace (keep first pre-value we know about)
        } else {
            // home-grown approach
            log.debug("enlist entity's property changes for publishing {}", entity);

            enlistForCreateOrUpdate(entity, PropertyChangeRecord::updatePreValueWithCurrent);
        }
    }

    private void enlistForCreateOrUpdate(ManagedObject entity, Consumer<PropertyChangeRecord> propertyChangeRecordConsumer) {
        entity.getSpecification().streamProperties(MixedIn.EXCLUDED)
                .filter(property->!EntityPropertyChangePublishingPolicyFacet.isExcludedFromPublishing(property))
                .map(property -> PropertyChangeRecordId.of(entity, property))
                .filter(pcrId -> ! enlistedPropertyChangeRecordsById.containsKey(pcrId)) // only if not previously seen
                .map(pcrId -> enlistedPropertyChangeRecordsById.put(pcrId, PropertyChangeRecord.of(pcrId)))
                .filter(Objects::nonNull)   // shouldn't happen, just keeping compiler happy
                .forEach(propertyChangeRecordConsumer);
    }


    @Override
    public void enlistDeleting(final ManagedObject entity) {

        _Xray.enlistDeleting(entity, interactionProviderProvider);

        if (isEntityExcludedForChangePublishing(entity)) {
            return;
        }

        final boolean enlisted = enlistForChangeKindPublishing(entity, EntityChangeKind.DELETE);
        if(enlisted) {

            log.debug("enlist entity's property changes for publishing {}", entity);

            entity.getSpecification()
                    .streamProperties(MixedIn.EXCLUDED)
                    .filter(property -> EntityChangePublishingFacet.isPublishingEnabled(entity.getSpecification()))
                    .filter(property -> !EntityPropertyChangePublishingPolicyFacet.isExcludedFromPublishing(property))
                    .map(property -> PropertyChangeRecordId.of(entity, property))
                    .map(pcrId -> enlistedPropertyChangeRecordsById.computeIfAbsent(pcrId, PropertyChangeRecord::of))
                    .forEach(pcr -> {
                        pcr.updatePreValueWithCurrent();
                        pcr.updatePostValueAsDeleted();
                    });
        }
    }



    /**
     * Used only for the implementation of {@link MetricsService}.
     * @param entity
     */
    @Override
    public void incrementLoaded(final ManagedObject entity) {
        _Xray.recognizeLoaded(entity, interactionProviderProvider);
        numberEntitiesLoaded.increment();
    }


    // -- METRICS SERVICE

    @Override
    public int numberEntitiesLoaded() {
        return Math.toIntExact(numberEntitiesLoaded.longValue());
    }

    @Override
    public int numberEntitiesDirtied() {
        return changeKindByEnlistedAdapter.size();
    }




}
