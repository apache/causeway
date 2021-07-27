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
import org.eclipse.persistence.sessions.changesets.DirectToFieldChangeRecord;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.transaction.changetracking.EntityChangeTracker;
import org.apache.isis.core.transaction.changetracking.PreAndPostValue;
import org.apache.isis.core.transaction.changetracking.PropertyChangeRecord;
import org.apache.isis.core.transaction.changetracking.PropertyChangeTracker;
import org.apache.isis.persistence.jpa.applib.services.JpaSupportService;

import lombok.val;
import lombok.extern.log4j.Log4j2;

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
    @Inject private PropertyChangeTracker propertyChangePublisher;
    @Inject private Provider<JpaSupportService> jpaSupportServiceProvider;
    @Inject private ObjectManager objectManager;

    @PrePersist void onPrePersist(final Object entityPojo) {
        log.debug("onPrePersist: {}", entityPojo);
        serviceInjector.injectServicesInto(entityPojo);
        val entity = objectManager.adapt(entityPojo);
        propertyChangePublisher.onPrePersist(entity);
    }

    @PreUpdate void onPreUpdate(final Object entityPojo) {
        log.debug("onPreUpdate: {}", entityPojo);
        serviceInjector.injectServicesInto(entityPojo);
        val entity = objectManager.adapt(entityPojo);
        val entityManagerResult = jpaSupportServiceProvider.get().getEntityManager(entityPojo.getClass());
        entityManagerResult.getValue().ifPresent(em -> {  // https://wiki.eclipse.org/EclipseLink/FAQ/JPA#How_to_access_what_changed_in_an_object_or_transaction.3F
            val unwrap = em.unwrap(UnitOfWork.class);
            val changes = unwrap.getCurrentChanges();
            val objectChanges = changes.getObjectChangeSetForClone(entityPojo);
            if(objectChanges==null) {
                return;
            }

            final Can<PropertyChangeRecord> propertyChangeRecords =
            objectChanges
            .getChanges()
            .stream()
            .filter(DirectToFieldChangeRecord.class::isInstance)
            .map(DirectToFieldChangeRecord.class::cast)
            .map(changeRecord -> {
                val propertyName = changeRecord.getAttribute();
                return entity
                        .getSpecification()
                        .getProperty(propertyName)
                        .filter(property->!property.isMixedIn())
                        .filter(property->!property.isNotPersisted())
                        .map(property->PropertyChangeRecord.of(
                                entity,
                                property,
                                PreAndPostValue
                                    .pre(changeRecord.getOldValue())
                                    .withPost(changeRecord.getNewValue())))
                        .orElse(null); // ignore
            })
            .collect(Can.toCan()); // a Can<T> only collects non-null elements

            propertyChangePublisher.onPreUpdate(entity, propertyChangeRecords);

        });
    }

    @PreRemove void onPreRemove(final Object entityPojo) {
        log.debug("onAnyRemove: {}", entityPojo);
        serviceInjector.injectServicesInto(entityPojo);
        val entity = objectManager.adapt(entityPojo);
        propertyChangePublisher.onPreRemove(entity);
    }

    @PostPersist void onPostPersist(final Object entityPojo) {
        log.debug("onPostPersist: {}", entityPojo);
        val entity = objectManager.adapt(entityPojo);
        propertyChangePublisher.onPostPersist(entity);
    }

    @PostUpdate void onPostUpdate(final Object entityPojo) {
        log.debug("onPostUpdate: {}", entityPojo);
        val entity = objectManager.adapt(entityPojo);
        propertyChangePublisher.onPostUpdate(entity);
    }

    @PostRemove void onPostRemove(final Object entityPojo) {
        log.debug("onPostRemove: {}", entityPojo);
    }

    @PostLoad void onPostLoad(final Object entityPojo) {
        log.debug("onPostLoad: {}", entityPojo);
        serviceInjector.injectServicesInto(entityPojo);
    }

}
