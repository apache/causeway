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

import java.util.function.UnaryOperator;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.events.EventObjectBase;
import org.apache.causeway.applib.events.lifecycle.AbstractLifecycleEvent;
import org.apache.causeway.applib.exceptions.unrecoverable.DomainModelException;
import org.apache.causeway.applib.services.eventbus.EventBusService;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.facets.object.callbacks.CallbackFacet;
import org.apache.causeway.core.metamodel.facets.object.callbacks.CreatedCallbackFacet;
import org.apache.causeway.core.metamodel.facets.object.callbacks.CreatedLifecycleEventFacet;
import org.apache.causeway.core.metamodel.facets.object.callbacks.LifecycleEventFacet;
import org.apache.causeway.core.metamodel.facets.object.callbacks.LoadedCallbackFacet;
import org.apache.causeway.core.metamodel.facets.object.callbacks.LoadedLifecycleEventFacet;
import org.apache.causeway.core.metamodel.facets.object.callbacks.PersistedCallbackFacet;
import org.apache.causeway.core.metamodel.facets.object.callbacks.PersistedLifecycleEventFacet;
import org.apache.causeway.core.metamodel.facets.object.callbacks.PersistingCallbackFacet;
import org.apache.causeway.core.metamodel.facets.object.callbacks.PersistingLifecycleEventFacet;
import org.apache.causeway.core.metamodel.facets.object.callbacks.RemovingCallbackFacet;
import org.apache.causeway.core.metamodel.facets.object.callbacks.RemovingLifecycleEventFacet;
import org.apache.causeway.core.metamodel.facets.object.callbacks.UpdatedCallbackFacet;
import org.apache.causeway.core.metamodel.facets.object.callbacks.UpdatedLifecycleEventFacet;
import org.apache.causeway.core.metamodel.facets.object.callbacks.UpdatingCallbackFacet;
import org.apache.causeway.core.metamodel.facets.object.callbacks.UpdatingLifecycleEventFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.core.transaction.changetracking.events.PostStoreEvent;
import org.apache.causeway.core.transaction.changetracking.events.PreStoreEvent;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Calls lifecycle callbacks for entities, ensuring that any given entity is only ever called once.
 * @since 2.0 {@index}
 */
@Component
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".LifecycleCallbackNotifier")
@Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Qualifier("Default")
//@Log4j2
public class LifecycleCallbackNotifier {

    final @NonNull EventBusService eventBusService;

    public void postCreate(final ManagedObject entity) {
        dispatch(entity, CreatedCallbackFacet.class, CreatedLifecycleEventFacet.class);
    }

    public void postLoad(final ManagedObject entity) {
        dispatch(entity, LoadedCallbackFacet.class, LoadedLifecycleEventFacet.class);
    }

    /**
     * @param eitherWithOrWithoutOid - either the adapted entity with OID <i>left</i>,
     *      otherwise adapted entity without OID <i>right</i>
     */
    public void prePersist(final Either<ManagedObject, ManagedObject> eitherWithOrWithoutOid) {
        val pojo = eitherWithOrWithoutOid.fold(ManagedObject::getPojo, ManagedObject::getPojo);
        if(pojo==null) {return;}
        eventBusService.post(PreStoreEvent.of(pojo));
        val entity = eitherWithOrWithoutOid.fold(UnaryOperator.identity(), UnaryOperator.identity());
        dispatch(entity, PersistingCallbackFacet.class, PersistingLifecycleEventFacet.class);
    }

    public void postPersist(final ManagedObject entity) {
        eventBusService.post(PostStoreEvent.of(entity.getPojo()));
        dispatch(entity, PersistedCallbackFacet.class, PersistedLifecycleEventFacet.class);
    }

    public void preUpdate(final ManagedObject entity) {
        eventBusService.post(PreStoreEvent.of(entity.getPojo()));
        dispatch(entity, UpdatingCallbackFacet.class, UpdatingLifecycleEventFacet.class);
    }

    public void postUpdate(final ManagedObject entity) {
        dispatch(entity, UpdatedCallbackFacet.class, UpdatedLifecycleEventFacet.class);
    }

    public void preRemove(final ManagedObject entity) {
        dispatch(entity, RemovingCallbackFacet.class, RemovingLifecycleEventFacet.class);
    }

    //  -- HELPER

    private void dispatch(
            final ManagedObject entity,
            final Class<? extends CallbackFacet> callbackFacetType,
            final Class<? extends LifecycleEventFacet> lifecycleEventFacetClass) {

        ManagedObjects.asSpecified(entity)
        .map(ManagedObject::getSpecification)
        .ifPresent(spec->{

            spec.lookupFacet(callbackFacetType)
            .ifPresent(callbackFacet->invokeCallback(callbackFacet, entity));

            spec.lookupFacet(lifecycleEventFacetClass)
            .map(LifecycleEventFacet::getEventType)
            .ifPresent(eventType->postEvent(_Casts.uncheckedCast(eventType), entity));
        });
    }

    protected void invokeCallback(final CallbackFacet callbackFacet, final ManagedObject entity) {
        try {
            callbackFacet.invoke(entity);
        } catch (final RuntimeException e) {
            throw new DomainModelException(
                    "Callback failed.  Calling " + callbackFacet + " on " + entity, e);
        }
    }

    protected void postEvent(
            final Class<? extends AbstractLifecycleEvent<Object>> eventType,
            final ManagedObject entity) {
        EventObjectBase
            .getInstanceWithSourceSupplier(eventType, entity::getPojo)
            .ifPresent(eventBusService::post);
    }

}
