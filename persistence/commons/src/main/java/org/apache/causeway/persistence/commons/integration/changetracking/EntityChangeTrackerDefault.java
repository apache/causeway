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
package org.apache.causeway.persistence.commons.integration.changetracking;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.EntityChangeKind;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.TransactionScope;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.iactn.InteractionProvider;
import org.apache.causeway.applib.services.metrics.MetricsService;
import org.apache.causeway.applib.services.publishing.spi.EntityChanges;
import org.apache.causeway.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.causeway.applib.services.xactn.TransactionId;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.facets.object.publish.entitychange.EntityChangePublishingFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmEntityUtils;
import org.apache.causeway.core.metamodel.services.objectlifecycle.HasEnlistedEntityPropertyChanges;
import org.apache.causeway.core.metamodel.services.objectlifecycle.PreAndPostValue;
import org.apache.causeway.core.metamodel.services.objectlifecycle.PropertyChangeRecord;
import org.apache.causeway.core.metamodel.services.objectlifecycle.PropertyChangeRecordId;
import org.apache.causeway.core.runtime.flushmgmt.FlushMgmt;
import org.apache.causeway.core.transaction.changetracking.EntityChangeTracker;
import org.apache.causeway.core.transaction.changetracking.EntityChangesPublisher;
import org.apache.causeway.core.transaction.changetracking.EntityPropertyChangePublisher;
import org.apache.causeway.core.transaction.changetracking.HasEnlistedEntityChanges;
import org.apache.causeway.persistence.commons.CausewayModulePersistenceCommons;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * This object keeps track of all of the changes within a transaction, for entities for which entity property change
 * publishing is enabled (typically using the
 * {@link DomainObject#entityChangePublishing() @DomainObject(entityChangePublishing=)} annotation attribute.
 *
 * <p>
 * The service is {@link TransactionScope transaction-scope}d and implements Spring's {@link TransactionSynchronization}
 * interface, meaning that Spring will call the {@link #beforeCompletion()} callback.  This service also implements
 * {@link org.springframework.core.Ordered} to ensure it isn't called last by {@link TransactionSynchronizationManager}.
 * </p>
 *
 * @since 2.0 {@index}
 */
@Service
@TransactionScope
@Named(CausewayModulePersistenceCommons.NAMESPACE + ".EntityChangeTrackerDefault")
@Qualifier("default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class EntityChangeTrackerDefault
implements
    MetricsService,
    EntityChangeTracker,
    HasEnlistedEntityPropertyChanges,
    HasEnlistedEntityChanges,
    TransactionSynchronization,
    Ordered {

    @Programmatic
    @Override
    public int getOrder() {
        return PriorityPrecedence.EARLY;
    }

    private final EntityPropertyChangePublisher entityPropertyChangePublisher;
    private final EntityChangesPublisher entityChangesPublisher;
    private final Provider<InteractionProvider> interactionProviderProvider;
    private final PreAndPostValueEvaluatorService preAndPostValueEvaluatorService;
    private final CausewayConfiguration causewayConfiguration;

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


    private boolean suppressAutoFlush;

    @PostConstruct
    public void init() {
        this.suppressAutoFlush = causewayConfiguration.getPersistence().getCommons().getEntityChangeTracker().isSuppressAutoFlush();
    }


    @Override
    public void destroy() throws Exception {
        enlistedPropertyChangeRecordsById.clear();
        entityPropertyChangeRecordsForPublishing.clear();
        changeKindByEnlistedAdapter.clear();

        numberEntitiesLoaded.reset();
        entityChangeEventCount.reset();
        persistentChangesEncountered.set(false);
    }

    private void suppressAutoFlushIfRequired(final Runnable runnable) {
        if (suppressAutoFlush) {
            FlushMgmt.suppressAutoFlush(runnable);
        } else {
            runnable.run();
        }
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
                    if(MmEntityUtils.getEntityState(rec.getEntity()).isTransientOrRemoved()) {
                        rec.withPostValueSetToDeleted();
                    } else {
                        rec.withPostValueSetToCurrent();
                    }
                })
                .filter(managedProperty-> shouldPublish(managedProperty.getPreAndPostValue()))
                .collect(_Sets.toUnmodifiable());

        enlistedPropertyChangeRecordsById.clear();

        return records;
    }

    private boolean shouldPublish(final PreAndPostValue preAndPostValue) {
        return preAndPostValueEvaluatorService.differ(preAndPostValue);
    }

    private boolean isEntityExcludedForChangePublishing(final ManagedObject entity) {

        if(!EntityChangePublishingFacet.isPublishingEnabled(entity.getSpecification())) {
            return true; // ignore entities that are not enabled for entity change publishing
        }

        // guard against transient
        if(ManagedObjects.bookmark(entity).isEmpty()) return true;

        if(entityPropertyChangeRecordsForPublishing.isMemoized()) {
            throw _Exceptions.illegalState("Cannot enlist additional changes for auditing, "
                    + "since changedObjectPropertiesRef was already prepared (memoized) for auditing.");
        }

        return false;
    }


    @Override
    public void beforeCompletion() {
        try {
            _Xray.publish(this, interactionProviderProvider);

            log.debug("about to publish entity changes");
            entityPropertyChangePublisher.publishChangedProperties();
            entityChangesPublisher.publishChangingEntities(this);

        } finally {
            log.debug("purging entity change records");

            enlistedPropertyChangeRecordsById.clear();
            entityPropertyChangeRecordsForPublishing.clear();

            changeKindByEnlistedAdapter.clear();
            entityChangeEventCount.reset();
            numberEntitiesLoaded.reset();
        }
    }

    private void enableCommandPublishing() {
        val alreadySet = persistentChangesEncountered.getAndSet(true);
        if(!alreadySet) {
            val command = currentInteraction().getCommand();
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

        suppressAutoFlushIfRequired(() -> {
            log.debug("enlist entity's property changes for publishing {}", entity);
            enlistForChangeKindPublishing(entity, EntityChangeKind.CREATE);

            MmEntityUtils.streamPropertyChangeRecordIdsForChangePublishing(entity)
                    .filter(pcrId -> ! enlistedPropertyChangeRecordsById.containsKey(pcrId)) // only if not previously seen
                    .forEach(pcrId -> enlistedPropertyChangeRecordsById.put(pcrId, PropertyChangeRecord.ofNew(pcrId)));
        });
    }

    @Override
    public void enlistUpdating(
            final ManagedObject entity,
            final @Nullable Function<ManagedObject, Can<PropertyChangeRecord>> propertyChangeRecordSupplier) {

        _Xray.enlistUpdating(entity, interactionProviderProvider);

        if (isEntityExcludedForChangePublishing(entity)) {
            return;
        }

        log.debug("enlist entity's property changes for publishing {}", entity);

        suppressAutoFlushIfRequired(() -> {
            // we call this come what may;
            // additional properties may now have been changed, and the changeKind for publishing might also be modified
            enlistForChangeKindPublishing(entity, EntityChangeKind.UPDATE);

            final Can<PropertyChangeRecord> ormPropertyChangeRecords = propertyChangeRecordSupplier !=null
                    ? propertyChangeRecordSupplier.apply(entity)
                    : null;

            if(ormPropertyChangeRecords != null) {
                // provided by ORM
                ormPropertyChangeRecords
                        .stream()
                        .filter(pcr -> ! enlistedPropertyChangeRecordsById.containsKey(pcr.getId())) // only if not previously seen
                        .forEach(pcr -> enlistedPropertyChangeRecordsById.put(pcr.getId(), pcr));
            } else {
                // home-grown approach
                MmEntityUtils.streamPropertyChangeRecordIdsForChangePublishing(entity)
                        .filter(pcrId -> ! enlistedPropertyChangeRecordsById.containsKey(pcrId)) // only if not previously seen
                        .map(pcrId -> enlistedPropertyChangeRecordsById.put(pcrId, PropertyChangeRecord.ofCurrent(pcrId)))
                        .filter(Objects::nonNull)   // shouldn't happen, just keeping compiler happy
                        .forEach(PropertyChangeRecord::withPreValueSetToCurrent);
            }
        });
    }

    @Override
    public void enlistDeleting(final ManagedObject entity) {

        _Xray.enlistDeleting(entity, interactionProviderProvider);

        if (isEntityExcludedForChangePublishing(entity)) {
            return;
        }

        suppressAutoFlushIfRequired(() -> {
            final boolean enlisted = enlistForChangeKindPublishing(entity, EntityChangeKind.DELETE);
            if(enlisted) {
                log.debug("enlist entity's property changes for publishing {}", entity);

                MmEntityUtils.streamPropertyChangeRecordIdsForChangePublishing(entity)
                        .forEach(pcrId -> enlistedPropertyChangeRecordsById
                                .computeIfAbsent(pcrId, id -> PropertyChangeRecord.ofDeleting(id)));
            }
        });
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
