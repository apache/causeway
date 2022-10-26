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
package org.apache.causeway.core.runtimeservices.publish;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.objectlifecycle.ObjectLifecyclePublisher;
import org.apache.causeway.core.metamodel.services.objectlifecycle.PropertyChangeRecord;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.core.transaction.changetracking.EntityChangeTracker;

import lombok.RequiredArgsConstructor;

/**
 * @see ObjectLifecyclePublisher
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".ObjectLifecyclePublisherDefault")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
//@Log4j2
public class ObjectLifecyclePublisherDefault implements ObjectLifecyclePublisher {

    private final Provider<EntityChangeTracker> entityChangeTrackerProvider;
    private final Provider<LifecycleCallbackNotifier> lifecycleCallbackNotifierProvider;
    private final Provider<InteractionService> interactionServiceProvider;

    private InteractionService interactionService() {
        return interactionServiceProvider.get();
    }

    private EntityChangeTracker entityChangeTracker() {
        return interactionService().isInInteraction()
                ? entityChangeTrackerProvider.get()
                : EntityChangeTracker.NOOP;
    }

    LifecycleCallbackNotifier lifecycleCallbackNotifier() {
        return lifecycleCallbackNotifierProvider.get();
    }

    @Override
    public void onPostCreate(final ManagedObject entity) {
        lifecycleCallbackNotifier().postCreate(entity);
    }

    @Override
    public void onPostLoad(final ManagedObject entity) {
        entityChangeTracker().incrementLoaded(entity);
        lifecycleCallbackNotifier().postLoad(entity);
    }

    @Override
    public void onPrePersist(final Either<ManagedObject, ManagedObject> eitherWithOrWithoutOid) {
        lifecycleCallbackNotifier().prePersist(eitherWithOrWithoutOid);
    }

    @Override
    public void onPostPersist(final ManagedObject entity) {
        entityChangeTracker().enlistCreated(entity);
        lifecycleCallbackNotifier().postPersist(entity);
    }

    @Override
    public void onPreUpdate(
            final ManagedObject entity,
            @Nullable final Can<PropertyChangeRecord> changeRecords) {
        entityChangeTracker().enlistUpdating(entity, changeRecords);
        lifecycleCallbackNotifier().preUpdate(entity);
    }


    @Override
    public void onPostUpdate(final ManagedObject entity) {
        lifecycleCallbackNotifier().postUpdate(entity);
    }


    @Override
    public void onPreRemove(final ManagedObject entity) {
        entityChangeTracker().enlistDeleting(entity);
        lifecycleCallbackNotifier().preRemove(entity);
    }


}
