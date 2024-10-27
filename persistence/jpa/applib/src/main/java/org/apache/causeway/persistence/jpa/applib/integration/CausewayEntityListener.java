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
package org.apache.causeway.persistence.jpa.applib.integration;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.sessions.changesets.DirectToFieldChangeRecord;
import org.eclipse.persistence.sessions.changesets.ObjectChangeSet;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmEntityUtils;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.services.objectlifecycle.ObjectLifecyclePublisher;
import org.apache.causeway.core.metamodel.services.objectlifecycle.PropertyChangeRecord;
import org.apache.causeway.persistence.jpa.applib.services.JpaSupportService;

import lombok.extern.log4j.Log4j2;

/**
 * EntityListener class for listing with the {@link javax.persistence.EntityListeners} annotation, to
 * support injection point resolving for entities, and to notify {@link ObjectLifecyclePublisher} of changes.
 *
 * <p>
 * Instances of this class are not managed by Spring, but by the persistence layer.
 * </p>
 *
 * <p>
 * The particular persistence layer implementation in use needs to be configured,
 * with a BeanManager, that is able to resolve injection points for this EntityListener.
 * </p>
 *
 * @since 2.0 {@index}
 */
@Log4j2
public class CausewayEntityListener {

    // injection points resolved via constructor ...
    @Inject private ObjectLifecyclePublisher objectLifecyclePublisher;
    @Inject private Provider<JpaSupportService> jpaSupportServiceProvider;
    @Inject private ObjectManager objectManager;

    @PrePersist void onPrePersist(final Object entityPojo) {
        log.debug("onPrePersist: {}", entityPojo);
        var entity = objectManager.adapt(entityPojo);
        objectLifecyclePublisher.onPrePersist(Either.left(entity));
    }

    @PostLoad void onPostLoad(final Object entityPojo) {
        log.debug("onPostLoad: {}", entityPojo);
        var entity = objectManager.adapt(entityPojo);

        var entityState = entity.getEntityState();
        if(!entityState.isAttached()) {
            // [CAUSEWAY-3265] seeing this with JPA
            // if we don't exit here will cause a nested loop repeatedly trying to refetch the pojo
            log.error("onPostLoad event while pojo not attached ({}); ignoring the event",
                    entityState.name());
            return;
        }
        objectLifecyclePublisher.onPostLoad(entity);
    }

    @PreUpdate void onPreUpdate(final Object entityPojo) {
        log.debug("onPreUpdate: {}", entityPojo);
        var entity = objectManager.adapt(entityPojo);
        objectLifecyclePublisher.onPreUpdate(entity, this::gatherPropertyChangeRecords);
    }

    @PreRemove void onPreRemove(final Object entityPojo) {
        log.debug("onAnyRemove: {}", entityPojo);
        var entity = objectManager.adapt(entityPojo);
        objectLifecyclePublisher.onPreRemove(entity);
    }

    @PostPersist void onPostPersist(final Object entityPojo) {
        log.debug("onPostPersist: {}", entityPojo);
        var entity = objectManager.adapt(entityPojo);
        objectLifecyclePublisher.onPostPersist(entity);
    }

    @PostUpdate void onPostUpdate(final Object entityPojo) {
        log.debug("onPostUpdate: {}", entityPojo);
        var entity = objectManager.adapt(entityPojo);
        objectLifecyclePublisher.onPostUpdate(entity);
    }

    @PostRemove void onPostRemove(final Object entityPojo) {
        log.debug("onPostRemove: {}", entityPojo);
    }

    // -- HELPER

    private Can<PropertyChangeRecord> gatherPropertyChangeRecords(final @Nullable ManagedObject entity) {

        // guard against null and non-entity types
        if(!ManagedObjects.isEntity(entity)) {
            return Can.empty();
        }

        final Object entityPojo = entity.getPojo();
        var entityManagerIfAny = jpaSupportServiceProvider.get().getEntityManager(entityPojo.getClass())
                .getValue();

        final Can<PropertyChangeRecord> propertyChangeRecords = entityManagerIfAny
            .map(em -> {
                // https://wiki.eclipse.org/EclipseLink/FAQ/JPA#How_to_access_what_changed_in_an_object_or_transaction.3F
                var unwrap = em.unwrap(UnitOfWork.class);
                var changes = unwrap.getCurrentChanges();
                return changes.getObjectChangeSetForClone(entityPojo);
            })
            .map((final ObjectChangeSet objectChanges)->{
                return
                    objectChanges.getChanges().stream()
                    .filter(DirectToFieldChangeRecord.class::isInstance)
                    .map(DirectToFieldChangeRecord.class::cast)
                    .map(ormChangeRecord -> {
                        final String propertyName = ormChangeRecord.getAttribute();
                        return MmEntityUtils.lookupPropertyChangeRecordIdForChangePublishing(entity, propertyName)
                                .map(id -> PropertyChangeRecord.ofCurrent(id, ormChangeRecord.getOldValue()))
                                .orElse(null); // ignore
                    })
                    .collect(Can.toCan()); // a Can only collects non-null elements
            })
            .orElseGet(Can::empty);

        return propertyChangeRecords;
    }

}
