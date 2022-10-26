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
package org.apache.causeway.persistence.jpa.integration.changetracking;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.EntityChangeKind;
import org.apache.causeway.applib.annotation.InteractionScope;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.iactn.InteractionProvider;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.metrics.MetricsService;
import org.apache.causeway.applib.services.publishing.spi.EntityChanges;
import org.apache.causeway.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.causeway.applib.services.xactn.TransactionId;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.facets.object.publish.entitychange.EntityChangePublishingFacet;
import org.apache.causeway.core.metamodel.facets.properties.property.entitychangepublishing.EntityPropertyChangePublishingPolicyFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmEntityUtil;
import org.apache.causeway.core.metamodel.services.objectlifecycle.HasEnlistedEntityPropertyChanges;
import org.apache.causeway.core.metamodel.services.objectlifecycle.PropertyChangeRecord;
import org.apache.causeway.core.metamodel.services.objectlifecycle.PropertyChangeRecordId;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.transaction.changetracking.EntityChangeTracker;
import org.apache.causeway.core.transaction.changetracking.EntityChangesPublisher;
import org.apache.causeway.core.transaction.changetracking.EntityPropertyChangePublisher;
import org.apache.causeway.core.transaction.changetracking.HasEnlistedEntityChanges;
import org.apache.causeway.core.transaction.events.TransactionBeforeCompletionEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * This service keeps track of all of the changes within a transactoin, for entities for which entity property change
 * publishing is enabled (typically using the
 * {@link DomainObject#entityChangePublishing() @DomainObject(entityChangePublishing=)} annotation attribute.
 *
 * <p>
 * The service is {@link InteractionScope}d.  In theory this could happen multiple times per interaction, so the
 * data structures are cleared on each commit for potential reuse within the same interaction.  (Of course, because the
 * service <i>is</i> interaction-scoped, a new instance of the service is created for each interaction, and so the
 * data held in this service is private to each user's interaction.
 * </p>
 *
 * @since 2.0 {@index}
 */
@Service
@Named("causeway.persistence.commons.EntityChangeTrackerDefault")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("default")
@InteractionScope   // see note above regarding this
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class EntityChangeTrackerDefault
implements
    MetricsService,
    EntityChangeTracker,
    HasEnlistedEntityPropertyChanges,
    HasEnlistedEntityChanges {


    private final EntityPropertyChangePublisher entityPropertyChangePublisher;
    private final EntityChangesPublisher entityChangesPublisher;
    private final Provider<InteractionProvider> interactionProviderProvider;

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

    private final LongAdder numberEntitiesLoaded = new LongAdder();
    private final LongAdder entityChangeEventCount = new LongAdder();
    private final AtomicBoolean persistentChangesEncountered = new AtomicBoolean();


    @Override
    public void destroy() throws Exception {
        enlistedPropertyChangeRecordsById.clear();
        entityPropertyChangeRecordsForPublishing.clear();
        changeKindByEnlistedAdapter.clear();

        numberEntitiesLoaded.reset();
        entityChangeEventCount.reset();
        persistentChangesEncountered.set(false);
    }

    Set<PropertyChangeRecord> snapshotPropertyChangeRecords() {
        // this code path has side-effects, it locks the result for this transaction,
        // such that cannot enlist on top of it
        return entityPropertyChangeRecordsForPublishing.get();
    }

    /**
     * For any enlisted Object Properties collects those, that are meant for publishing,
     * then clears enlisted objects.
     */
    private Set<PropertyChangeRecord> capturePostValuesAndDrain() {

        val records = enlistedPropertyChangeRecordsById.values().stream()
                // set post values, which have been left empty up to now
                .peek(rec -> {
                    // assuming this check correctly detects deleted entities
                    if(MmEntityUtil.isDeleted(rec.getEntity())) {
                        rec.withPostValueSetToDeleted();
                    } else {
                        rec.withPostValueSetToCurrent();
                    }
                })
                .filter(managedProperty->managedProperty.getPreAndPostValue().shouldPublish())
                .collect(_Sets.toUnmodifiable());

        enlistedPropertyChangeRecordsById.clear();

        return records;

    }

    private boolean isEntityExcludedForChangePublishing(final ManagedObject entity) {

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
     * Subscribes to transactions and forwards onto the current interaction's EntityChangeTracker, if available.
     *
     * <p>
     *     Note that this service has singleton-scope, unlike {@link EntityChangeTrackerDefault} which has
     *     {@link InteractionScope interaction scope}. The problem with using {@link EntityChangeTrackerDefault} as
     *     the direct subscriber is that if there's no {@link Interaction}, then Spring will fail to activate an instance resulting in an
     *     {@link org.springframework.beans.factory.support.ScopeNotActiveException}.  Now, admittedly that exception
     *     gets swallowed in the call stack somewhere, but it's still not pretty.
     * </p>
     *
     * <p>
     *     This design, instead, at least lets us check if there's an interaction in scope, and effectively ignore
     *     the call if not.
     * </p>
     */
    @Component
    @Named("causeway.persistence.commons.EntityChangeTrackerDefault.TransactionSubscriber")
    @Priority(PriorityPrecedence.EARLY)
    @Qualifier("default")
    @RequiredArgsConstructor(onConstructor_ = {@Inject})
    public static class TransactionSubscriber {

        private final InteractionService interactionService;
        private final Provider<EntityChangeTrackerDefault> entityChangeTrackerProvider;

        /**
         * TRANSACTION END BOUNDARY
         * @apiNote intended to be called during before transaction completion by the framework internally
         */
        @EventListener(value = TransactionBeforeCompletionEvent.class)
        @Order(PriorityPrecedence.LATE)
        public void onTransactionCompleting(final TransactionBeforeCompletionEvent event) {

            if(!interactionService.isInInteraction()) {
                // discard request is there is no interaction in scope.
                // this shouldn't ever really occur, but some low-level (could be improved?) integration tests do
                // hit this case.
                return;
            }
            entityChangeTracker().onTransactionCompleting(event);
        }

        private EntityChangeTrackerDefault entityChangeTracker() {
            return entityChangeTrackerProvider.get();
        }
    }

    /**
     * As called by {@link TransactionSubscriber}, so long as there is an {@link Interaction} in
     * {@link InteractionScope scope}.
     */
    void onTransactionCompleting(final TransactionBeforeCompletionEvent event) {
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

        entity.getSpecification().streamProperties(MixedIn.EXCLUDED)
                .filter(property->!EntityPropertyChangePublishingPolicyFacet.isExcludedFromPublishing(property))
                .map(property -> PropertyChangeRecordId.of(entity, property))
                .filter(pcrId -> ! enlistedPropertyChangeRecordsById.containsKey(pcrId)) // only if not previously seen
                .forEach(pcrId -> enlistedPropertyChangeRecordsById.put(pcrId, PropertyChangeRecord.ofNew(pcrId)));
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
                    .filter(pcr -> ! enlistedPropertyChangeRecordsById.containsKey(pcr.getId())) // only if not previously seen
                    .forEach(pcr -> this.enlistedPropertyChangeRecordsById.put(pcr.getId(), pcr));
        } else {
            // home-grown approach
            log.debug("enlist entity's property changes for publishing {}", entity);

            entity.getSpecification().streamProperties(MixedIn.EXCLUDED)
                    .filter(property->!EntityPropertyChangePublishingPolicyFacet.isExcludedFromPublishing(property))
                    .map(property -> PropertyChangeRecordId.of(entity, property))
                    .filter(pcrId -> ! enlistedPropertyChangeRecordsById.containsKey(pcrId)) // only if not previously seen
                    .map(pcrId -> enlistedPropertyChangeRecordsById.put(pcrId, PropertyChangeRecord.ofCurrent(pcrId)))
                    .filter(Objects::nonNull)   // shouldn't happen, just keeping compiler happy
                    .forEach(PropertyChangeRecord::withPreValueSetToCurrent);
        }
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
                    .forEach(pcrId -> enlistedPropertyChangeRecordsById.computeIfAbsent(pcrId, id -> PropertyChangeRecord.ofDeleting(id)));
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
