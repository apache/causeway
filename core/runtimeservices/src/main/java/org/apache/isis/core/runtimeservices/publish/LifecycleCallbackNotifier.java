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
package org.apache.isis.core.runtimeservices.publish;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.events.lifecycle.AbstractLifecycleEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.commons.functional.Either;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.factory._InstanceUtil;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.CreatedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.CreatedLifecycleEventFacet;
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
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;
import org.apache.isis.core.transaction.changetracking.events.PostStoreEvent;
import org.apache.isis.core.transaction.changetracking.events.PreStoreEvent;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Calls lifecycle callbacks for entities, ensuring that any given entity is only ever called once.
 * @since 2.0 {@index}
 */
@Component
@Named(IsisModuleCoreRuntimeServices.NAMESPACE + ".LifecycleCallbackNotifier")
@Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Qualifier("Default")
//@Log4j2
public class LifecycleCallbackNotifier {

    final EventBusService eventBusService;
    final SpecificationLoader specLoader;

    public void postCreate(final ManagedObject entity) {
        CallbackFacet.callCallback(entity, CreatedCallbackFacet.class);
        postLifecycleEventIfRequired(entity, CreatedLifecycleEventFacet.class);
    }

    public void postLoad(final ManagedObject entity) {
        CallbackFacet.callCallback(entity, LoadedCallbackFacet.class);
        postLifecycleEventIfRequired(entity, LoadedLifecycleEventFacet.class);
    }

    /**
     * @param either - either the adapted entity with OID to the <i>left</i>,
     *      otherwise if no OID available the entity pojo <i>right</i>
     */
    public void prePersist(final Either<ManagedObject, Object> either) {
        val pojo = either.fold(ManagedObject::getPojo, UnaryOperator.identity());
        if(pojo==null) {return;}
        eventBusService.post(PreStoreEvent.of(pojo));
        either.accept(
                entity->{
                    CallbackFacet.callCallback(entity, PersistingCallbackFacet.class);
                    postLifecycleEventIfRequired(entity, PersistingLifecycleEventFacet.class);
                },
                _pojo->{
                    val spec = specLoader.specForTypeElseFail(pojo.getClass());
                    // calling PersistingCallbackFacet not supported if we have no OID
                    postLifecycleEventIfRequired(spec, ()->pojo, PersistingLifecycleEventFacet.class);
                });
    }

    public void postPersist(final ManagedObject entity) {
        eventBusService.post(PostStoreEvent.of(entity.getPojo()));
        CallbackFacet.callCallback(entity, PersistedCallbackFacet.class);
        postLifecycleEventIfRequired(entity, PersistedLifecycleEventFacet.class);
    }

    public void preUpdate(final ManagedObject entity) {
        eventBusService.post(PreStoreEvent.of(entity.getPojo()));
        CallbackFacet.callCallback(entity, UpdatingCallbackFacet.class);
        postLifecycleEventIfRequired(entity, UpdatingLifecycleEventFacet.class);
    }

    public void postUpdate(final ManagedObject entity) {
        CallbackFacet.callCallback(entity, UpdatedCallbackFacet.class);
        postLifecycleEventIfRequired(entity, UpdatedLifecycleEventFacet.class);
    }

    public void preRemove(final ManagedObject entity) {
        CallbackFacet.callCallback(entity, RemovingCallbackFacet.class);
        postLifecycleEventIfRequired(entity, RemovingLifecycleEventFacet.class);
    }

    //  -- HELPER

    private void postLifecycleEventIfRequired(
            final ObjectSpecification spec,
            final Supplier<Object> pojo,
            final Class<? extends LifecycleEventFacet> lifecycleEventFacetClass) {

        spec.lookupFacet(lifecycleEventFacetClass)
        .map(LifecycleEventFacet::getEventType)
        .map(_InstanceUtil::createInstance)
        .ifPresent(eventInstance->{
            postEvent(_Casts.uncheckedCast(eventInstance), pojo);
        });
    }

    private void postLifecycleEventIfRequired(
            final ManagedObject object,
            final Class<? extends LifecycleEventFacet> lifecycleEventFacetClass) {

        // getPojo has side-effects, don't call if not needed
        if(object==null
                || object.getSpecialization().isEmpty()) {
            return;
        }
        postLifecycleEventIfRequired(
                object.getSpecification(),
                object::getPojo,
                lifecycleEventFacetClass);
    }

    protected void postEvent(final AbstractLifecycleEvent<Object> event, final Object pojo) {
        if(eventBusService!=null) {
            event.initSource(pojo);
            eventBusService.post(event);
        }
    }

}
