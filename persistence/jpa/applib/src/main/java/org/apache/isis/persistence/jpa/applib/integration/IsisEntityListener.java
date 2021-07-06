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
package org.apache.isis.persistence.jpa.applib.integration;

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

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.transaction.changetracking.EntityChangeTracker;
import org.apache.isis.core.transaction.changetracking.EntityChangeTrackerWithPreValue;
import org.apache.isis.persistence.jpa.applib.services.JpaSupportService;

import lombok.extern.log4j.Log4j2;
import lombok.val;

/**
 * EntityListener class for listing with the {@link javax.persistence.EntityListeners} annotation, to
 * support injection point resolving for entities, and to notify {@link EntityChangeTracker} of changes.
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
public class IsisEntityListener {

    // not managed by Spring (directly)
    @Inject private ServiceInjector serviceInjector;
    @Inject private Provider<EntityChangeTrackerWithPreValue> entityChangeTrackerProvider;
    @Inject private Provider<JpaSupportService> jpaSupportServiceProvider;
    @Inject private ObjectManager objectManager;


    @PrePersist
    private void onPrePersist(Object entityPojo) {
        log.debug("onPrePersist: {}", entityPojo);
        serviceInjector.injectServicesInto(entityPojo);
        val entity = objectManager.adapt(entityPojo);
        val entityChangeTracker = entityChangeTrackerProvider.get();
        entityChangeTracker.recognizePersisting(entity);
    }

    @PreUpdate
    private void onPreUpdate(Object entityPojo) {
        log.debug("onPreUpdate: {}", entityPojo);
        serviceInjector.injectServicesInto(entityPojo);
        val entity = objectManager.adapt(entityPojo);
        val entityChangeTracker = entityChangeTrackerProvider.get();
        val entityManagerResult = jpaSupportServiceProvider.get().getEntityManager(entityPojo.getClass());
        entityManagerResult.getValue().ifPresent(em -> {  // https://wiki.eclipse.org/EclipseLink/FAQ/JPA#How_to_access_what_changed_in_an_object_or_transaction.3F
            val unwrap = em.unwrap(UnitOfWork.class);
            val changes = unwrap.getCurrentChanges();
            val objectChanges = changes.getObjectChangeSetForClone(entityPojo);
            val changeRecords = objectChanges.getChanges();
            changeRecords.forEach(changeRecord -> {
                val propertyName = changeRecord.getAttribute();
                val oldValue = changeRecord.getOldValue();
                entityChangeTracker.enlistUpdating(entity, propertyName, oldValue);
            });
        });
    }

    @PreRemove
    private void onPreRemove(Object entityPojo) {
        log.debug("onAnyRemove: {}", entityPojo);
        serviceInjector.injectServicesInto(entityPojo);
        val entity = objectManager.adapt(entityPojo);
        val entityChangeTracker = entityChangeTrackerProvider.get();
        entityChangeTracker.enlistDeleting(entity);
    }

    @PostPersist
    private void onPostPersist(Object entityPojo) {
        log.debug("onPostPersist: {}", entityPojo);
        val entity = objectManager.adapt(entityPojo);
        val entityChangeTracker = entityChangeTrackerProvider.get();
        entityChangeTracker.enlistCreated(entity);
    }

    @PostUpdate
    private void onPostUpdate(Object entityPojo) {
        log.debug("onPostUpdate: {}", entityPojo);
        val entity = objectManager.adapt(entityPojo);
        val entityChangeTracker = entityChangeTrackerProvider.get();
        entityChangeTracker.recognizeUpdating(entity);
    }

    @PostRemove
    private void onPostRemove(Object entityPojo) {
        log.debug("onPostRemove: {}", entityPojo);
    }

    @PostLoad
    private void onPostLoad(Object entityPojo) {
        log.debug("onPostLoad: {}", entityPojo);
        serviceInjector.injectServicesInto(entityPojo);
        val entity = objectManager.adapt(entityPojo);
        val entityChangeTracker = entityChangeTrackerProvider.get();
        entityChangeTracker.recognizeLoaded(entity);
    }

}
