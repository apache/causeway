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
package org.apache.isis.core.transaction.changetracking;

import org.apache.isis.applib.events.lifecycle.AbstractLifecycleEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.commons.internal.factory._InstanceUtil;
import org.apache.isis.core.metamodel.facets.object.callbacks.LifecycleEventFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PersistenceCallbackHandlerAbstract {

    protected final EventBusService eventBusService;

    //  -- HELPER

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void postLifecycleEventIfRequired(
            final ManagedObject adapter,
            final Class<? extends LifecycleEventFacet> lifecycleEventFacetClass) {

        val lifecycleEventFacet = adapter.getSpecification().getFacet(lifecycleEventFacetClass);
        if(lifecycleEventFacet == null) {
            return;
        }
        val eventInstance = (AbstractLifecycleEvent) _InstanceUtil
                .createInstance(lifecycleEventFacet.getEventType());
        val pojo = adapter.getPojo();
        postEvent(eventInstance, pojo);

    }

    protected void postEvent(final AbstractLifecycleEvent<Object> event, final Object pojo) {
        if(eventBusService!=null) {
            event.initSource(pojo);
            eventBusService.post(event);
        }
    }

}
