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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.causeway.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.causeway.core.metamodel.execution.InteractionInternal;

import org.apache.causeway.core.metamodel.services.deadlock.DeadlockRecognizer;
import org.apache.causeway.schema.chg.v2.ChangesDto;
import org.apache.causeway.schema.chg.v2.ObjectsDto;
import org.apache.causeway.schema.common.v2.OidsDto;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
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

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
@Log4j2
public class EntityChangeTrackerDefault
implements
    MetricsService,
    EntityChangeTracker,
    HasEnlistedEntityPropertyChanges,
    HasEnlistedEntityChanges,
    TransactionSynchronization,
    Ordered {

    static AtomicInteger transactionCounter = new AtomicInteger(0);

    @Inject
    public EntityChangeTrackerDefault(
            EntityPropertyChangePublisher entityPropertyChangePublisher,
            EntityChangesPublisher entityChangesPublisher,
            Provider<InteractionProvider> interactionProviderProvider,
            PreAndPostValueEvaluatorService preAndPostValueEvaluatorService) {

        if(log.isDebugEnabled()) {
            var interactionId = interactionProviderProvider.get().currentInteraction().map(Interaction::getInteractionId).orElseGet(null);
            log.debug("EntityChangeTrackerDefault.new xactn={} interactionId={} thread={}", transactionCounter.incrementAndGet(), interactionId, Thread.currentThread().getName());
        }

        this.entityPropertyChangePublisher = entityPropertyChangePublisher;
        this.entityChangesPublisher = entityChangesPublisher;
        this.interactionProviderProvider = interactionProviderProvider;
        this.preAndPostValueEvaluatorService = preAndPostValueEvaluatorService;
    }

    @Programmatic
    @Override
    public int getOrder() {
        return PriorityPrecedence.EARLY;
    }

    private final EntityPropertyChangePublisher entityPropertyChangePublisher;
    private final EntityChangesPublisher entityChangesPublisher;
    private final Provider<InteractionProvider> interactionProviderProvider;
    private final PreAndPostValueEvaluatorService preAndPostValueEvaluatorService;

    /**
     * Contains a record for every objectId/propertyId that was changed.
     * @implNote access to this {@link Map} must be thread-safe and the map also should preserve insertion order. We
     *           cannot use <code>newConcurrentHashMap</code> as this doesn't preserve insertion order; instead we
     *           make sure that it is only ever accessed within a <code>synchronized</code> block.
     */
    private final Map<PropertyChangeRecordId, PropertyChangeRecord> enlistedPropertyChangeRecordsById = _Maps.newLinkedHashMap();

    /**
     * As used when {@link #enlistCreated(ManagedObject)} or {@link #enlistUpdating(ManagedObject, Function)}
     */
    private void addPropertyChangeRecordIfAbsent(PropertyChangeRecordId pcrId, PropertyChangeRecord pcr) {
        enlistedPropertyChangeRecordsById.computeIfAbsent(pcrId, id -> pcr);
    }
    /**
     * As used when {@link #enlistDeleting(ManagedObject)}.
     */
    private void addPropertyChangeRecordIfAbsent(PropertyChangeRecordId pcrId, Function<PropertyChangeRecordId, PropertyChangeRecord> func) {
        enlistedPropertyChangeRecordsById.computeIfAbsent(pcrId, func);
    }

    private Changes evaluateChanges() {
        var changedProperties = evaluateChangedProperties();

        var isCountersAndDetail = causewayConfiguration.getApplib().getService().getMetricsService().getLevel().isCountersAndDetail();
        Set<Bookmark> loadedBookmarks =
                isCountersAndDetail
                        ? enlistedPropertyChangeRecordsById.keySet()
                                .stream()
                                .map(PropertyChangeRecordId::getBookmark)
                                .collect(Collectors.<Bookmark>toSet())
                        : Collections.emptySet();

        Set<Bookmark> dirtiedBookmarks =
                isCountersAndDetail
                        ? changedProperties
                                .stream()
                                .map(PropertyChangeRecord::getBookmark)
                                .collect(Collectors.<Bookmark>toSet())
                        :  Collections.emptySet();

        enlistedPropertyChangeRecordsById.clear();

        return new Changes(changedProperties, loadedBookmarks, dirtiedBookmarks);
    }

    private Set<PropertyChangeRecord> evaluateChangedProperties() {
        Set<PropertyChangeRecord> dirtiedProperties;
        try {
            dirtiedProperties = changedRecords(enlistedPropertyChangeRecordsById.values());
        } catch (ConcurrentModificationException ex) {
            log.warn(
                    "A concurrent modification exception, one of these properties seemed to change as we looked at it :\n" +
                            enlistedPropertyChangeRecordsById.keySet()
                                    .stream()
                                    .map(PropertyChangeRecordId::toString)
                                    .collect(Collectors.joining("\n"))
            );
            // instead, we take a copy
            dirtiedProperties = changedRecords(new ArrayList<>(enlistedPropertyChangeRecordsById.values()));
        }
        return dirtiedProperties;
    }

    @RequiredArgsConstructor
    static class Changes {
        @Getter private final Set<PropertyChangeRecord> dirtiedProperties;
        @Getter private final Set<Bookmark> loadedBookmarks;
        @Getter private final Set<Bookmark> dirtiedBookmarks;
    }

    /**
     * Contains pre- and post- values of every property of every object that actually changed. A lazy snapshot.
     */
    private final _Lazy<Changes> changes = _Lazy.of(this::evaluateChanges);

    /**
     * when iterating over the original value set, if any of the properties causes the entity to change state as it is
     * evaluated then a ConcurrentModificationException will be thrown because an enlist will occur.
     */
    private Set<PropertyChangeRecord> changedRecords(Collection<PropertyChangeRecord> propertyChangeRecords)
            throws ConcurrentModificationException {
        return propertyChangeRecords.stream()
                // set post values, which have been left empty up to now
                .peek(rec -> {
                    if (MmEntityUtils.getEntityState(rec.getEntity()).isTransientOrRemoved()) {
                        rec.withPostValueSetToDeleted();
                    } else {
                        rec.withPostValueSetToCurrentElseUnknown(deadlockRecognizer);
                    }
                })
                .filter(managedProperty -> shouldPublish(managedProperty.getPreAndPostValue()))
                .collect(_Sets.toUnmodifiable());
    }

    private Changes memoizeChangesIfRequired() {
        return changes.get();
    }

    /**
     * @implNote access to this {@link Map} must be thread-safe (insertion order preservation is not required)
     */
    private final Map<Bookmark, EntityChangeKind> changeKindByEnlistedAdapter = _Maps.newHashMap();

    private final LongAdder numberEntitiesLoaded = new LongAdder();
    private final LongAdder entityChangeEventCount = new LongAdder();
    private final AtomicBoolean persistentChangesEncountered = new AtomicBoolean();

    @Override
    public void destroy() {

        if(log.isDebugEnabled()) {
            var interactionId = interactionProviderProvider.get().currentInteraction().map(Interaction::getInteractionId).orElse(null);
            log.debug("EntityChangeTrackerDefault.destroy xactn={} interactionId={} thread={}", transactionCounter.get(), interactionId, Thread.currentThread().getName());
        }

        clearAndReset();
    }

    private void clearAndReset() {
        enlistedPropertyChangeRecordsById.clear();
        changes.clear();

        changeKindByEnlistedAdapter.clear();
        numberEntitiesLoaded.reset();
        entityChangeEventCount.reset();

        persistentChangesEncountered.set(false);
    }

    /**
     * @implNote sets a lock on the {@code enlistedPropertyChangeRecordsById} {@link Map}
     *      until given {@code runnable} completes<p>
     *      Note: Java supports reentrant locks,
     *      which allow a thread to acquire the same lock multiple times without deadlocking itself.
     *      Reentrant locks maintain a count of the number of times a thread has acquired the lock
     *      and ensure that the lock is released only when the thread exits the synchronized block
     *      or method the same number of times it entered it.
     */
    private void suppressAutoFlushIfRequired(final Runnable runnable) {
        if (configuration.isSuppressAutoFlush()) {
            FlushMgmt.suppressAutoFlush(runnable);
        } else {
            runnable.run();
        }
    }

    private boolean shouldPublish(final PreAndPostValue preAndPostValue) {
        return preAndPostValueEvaluatorService.differ(preAndPostValue);
    }

    private boolean isEntityExcludedForChangePublishing(final ManagedObject entity) {

        if (!configuration.isEnabled()) {
            return true;
        }

        if(!EntityChangePublishingFacet.isPublishingEnabled(entity.getSpecification())) {
            return true; // ignore entities that are not enabled for entity change publishing
        }

        // guard against transient
        if(ManagedObjects.bookmark(entity).isEmpty()) return true;

        if(changes.isMemoized()) {
            throw _Exceptions.illegalState("Cannot enlist additional changes for auditing, "
                    + "since changedObjectPropertiesRef was already prepared (memoized) for auditing.");
        }

        return false;
    }

    @Override
    public void beforeCommit(boolean readOnly) {
        _Xray.publish(this, interactionProviderProvider);

        if(log.isDebugEnabled()) {
            var interactionId = interactionProviderProvider.get().currentInteraction().map(Interaction::getInteractionId).orElse(null);
            log.debug("EntityChangeTrackerDefault.beforeCommit(readOnly={}) xactn={} interactionId={} thread={}", readOnly, transactionCounter.get(), interactionId, Thread.currentThread().getName());
        }

        // we memoize the property changes to (hopefully) avoid ConcurrentModificationExceptions with ourselves later
        memoizeChangesIfRequired();

        entityPropertyChangePublisher.publishChangedProperties();
        entityChangesPublisher.publishChangingEntities(this);
    }

    @Override
    public void afterCompletion(int status) {

        if(log.isDebugEnabled()) {
            var interactionId = interactionProviderProvider.get().currentInteraction().map(Interaction::getInteractionId).orElse(null);
            log.debug("EntityChangeTrackerDefault.afterCompletion(status={}) xactn={} interactionId={} thread={}", decodeStatus(status), transactionCounter.get(), interactionId, Thread.currentThread().getName());
        }

        clearAndReset();
    }

    private static String decodeStatus(int status) {
        if (status == STATUS_COMMITTED) return "STATUS_COMMITTED";
        if (status == STATUS_ROLLED_BACK) return "STATUS_ROLLED_BACK";
        if (status == STATUS_UNKNOWN) return "STATUS_UNKNOWN";
        return status + " [not recognised]";
    }

    private void enableCommandPublishing() {
        var alreadySet = persistentChangesEncountered.getAndSet(true);
        if(!alreadySet) {
            // has side effects
            var command = currentInteraction().getCommand();
        }
    }

    @Override
    public Optional<EntityChanges> getEntityChanges(
            final java.sql.Timestamp timestamp,
            final String userName) {

        // a defensive copy of
        var changeKindByEnlistedAdapter = (Map<Bookmark, EntityChangeKind>) new HashMap<>(this.changeKindByEnlistedAdapter);
        if(changeKindByEnlistedAdapter.isEmpty()) {
            return Optional.empty();
        }

        final Interaction interaction = currentInteraction();
        final int numberEntitiesLoaded1 = numberEntitiesLoaded();

        // this code path has side-effects, it locks the result for this transaction,
        // such that cannot enlist on top of it
        final int numberEntityPropertiesModified = memoizeChangesIfRequired().dirtiedProperties.size();

        var interactionId = interaction.getInteractionId();
        final int nextEventSequence = ((InteractionInternal) interaction).getThenIncrementTransactionSequence();

        // side-effect: it locks the result for this transaction,
        // such that cannot enlist on top of it
        var changingEntities = (EntityChanges) new _SimpleChangingEntities(
                interactionId, nextEventSequence,
                userName, timestamp,
                numberEntitiesLoaded1,
                numberEntityPropertiesModified,
                () -> newDto(
                        interactionId, nextEventSequence,
                        userName, timestamp,
                        numberEntitiesLoaded1,
                        numberEntityPropertiesModified,
                        changeKindByEnlistedAdapter));

        return Optional.of(changingEntities);
    }

    private static ChangesDto newDto(
            final UUID interactionId, final int transactionSequenceNum,
            final String userName, final java.sql.Timestamp completedAt,
            final int numberEntitiesLoaded,
            final int numberEntityPropertiesModified,
            final Map<Bookmark, EntityChangeKind> changeKindByEnlistedEntity) {

        var objectsDto = new ObjectsDto();
        objectsDto.setCreated(new OidsDto());
        objectsDto.setUpdated(new OidsDto());
        objectsDto.setDeleted(new OidsDto());

        changeKindByEnlistedEntity.forEach((bookmark, kind)->{
            var oidDto = bookmark.toOidDto();
            if(oidDto==null) {
                return;
            }
            switch(kind) {
                case CREATE:
                    objectsDto.getCreated().getOid().add(oidDto);
                    return;
                case UPDATE:
                    objectsDto.getUpdated().getOid().add(oidDto);
                    return;
                case DELETE:
                    objectsDto.getDeleted().getOid().add(oidDto);
                    return;
            }
        });

        objectsDto.setLoaded(numberEntitiesLoaded);
        objectsDto.setPropertiesModified(numberEntityPropertiesModified);

        var changesDto = new ChangesDto();

        changesDto.setMajorVersion("2");
        changesDto.setMinorVersion("0");

        changesDto.setInteractionId(interactionId.toString());
        changesDto.setSequence(transactionSequenceNum);

        changesDto.setUsername(userName);
        changesDto.setCompletedAt(JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(completedAt));

        changesDto.setObjects(objectsDto);
        return changesDto;
    }

    @Override
    public Can<EntityPropertyChange> getPropertyChanges(
            final java.sql.Timestamp timestamp,
            final String userName,
            final TransactionId txId) {

        // this code path has side-effects, it locks the result for this transaction,
        // such that cannot enlist on top of it
        Set<PropertyChangeRecord> propertyChangeRecords = memoizeChangesIfRequired().getDirtiedProperties();

        return propertyChangeRecords.stream()
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

        var bookmark = ManagedObjects.bookmarkElseFail(entity);

        var previousChangeKind = changeKindByEnlistedAdapter.get(bookmark);
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

        suppressAutoFlushIfRequired(() -> {
            enlistForChangeKindPublishing(entity, EntityChangeKind.CREATE);

            MmEntityUtils.streamPropertyChangeRecordIdsForChangePublishing(entity)
                .forEach(pcrId -> addPropertyChangeRecordIfAbsent(pcrId, PropertyChangeRecord.ofNew(pcrId)));
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

        if(log.isDebugEnabled()) {
            log.debug("enlist entity's property changes for publishing {}", entity);
        }

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
                    .forEach(pcr -> addPropertyChangeRecordIfAbsent(pcr.getId(), pcr));
            } else {
                // home-grown approach
                MmEntityUtils.streamPropertyChangeRecordIdsForChangePublishing(entity)
                    .forEach(pcrId -> {
                        addPropertyChangeRecordIfAbsent(pcrId, PropertyChangeRecord.ofCurrent(pcrId, deadlockRecognizer));
                    });
            }
        });
    }

    @Override
    public void enlistDeleting(final ManagedObject entity) {

        _Xray.enlistDeleting(entity, interactionProviderProvider);

        if (isEntityExcludedForChangePublishing(entity)) return;

        suppressAutoFlushIfRequired(() -> {
            final boolean enlisted = enlistForChangeKindPublishing(entity, EntityChangeKind.DELETE);
            if(enlisted) {
                if(log.isDebugEnabled()) {
                    log.debug("enlist entity's property changes for publishing {}", entity);
                }

                MmEntityUtils.streamPropertyChangeRecordIdsForChangePublishing(entity)
                    .forEach(pcrId -> {
                        addPropertyChangeRecordIfAbsent(pcrId, id -> PropertyChangeRecord.ofDeleting(id, deadlockRecognizer));
                    });
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

    @Override
    public Set<Bookmark> entitiesLoaded() {
        return memoizeChangesIfRequired().getLoadedBookmarks();
    }

    @Override
    public Set<Bookmark> entitiesDirtied() {
        return memoizeChangesIfRequired().getDirtiedBookmarks();
    }

    // -- HELPER

    /**
     * SPI to allow this service to be configured through different mechanisms.
     */
    public interface Configuration {

        boolean isSuppressAutoFlush();

        boolean isEnabled();
    }

    @Component
    @Priority(PriorityPrecedence.LATE)
    @ConditionalOnMissingBean(Configuration.class)
    @RequiredArgsConstructor(onConstructor_ = {@Inject})
    public static class ConfigurationDefault implements Configuration {
        private final CausewayConfiguration causewayConfiguration;

        @Override
        public boolean isSuppressAutoFlush() {
            return causewayConfiguration.getPersistence().getCommons().getEntityChangeTracker().isSuppressAutoFlush();
        }

        @Override
        public boolean isEnabled() {
            return causewayConfiguration.getPersistence().getCommons().getEntityChangeTracker().isEnabled();
        }
    }

    @Inject private Configuration configuration;
    @Inject private CausewayConfiguration causewayConfiguration;
    @Inject private DeadlockRecognizer deadlockRecognizer;

}
