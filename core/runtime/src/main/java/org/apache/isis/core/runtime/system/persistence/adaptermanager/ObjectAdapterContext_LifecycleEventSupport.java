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
package org.apache.isis.core.runtime.system.persistence.adaptermanager;

import org.apache.isis.applib.events.lifecycle.AbstractLifecycleEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.runtime.system.context.session.RuntimeContext;
import org.apache.isis.metamodel.facets.object.callbacks.LifecycleEventFacet;
import org.apache.isis.metamodel.spec.ManagedObject;

/**
 * package private mixin for ObjectAdapterContext
 * <p>
 * Responsibility: provides life-cycle event post support
 * </p> 
 * @since 2.0
 */
class ObjectAdapterContext_LifecycleEventSupport {
    
    private final EventBusService eventBusService; 
    
    ObjectAdapterContext_LifecycleEventSupport(RuntimeContext runtimeContext) {
        this.eventBusService = runtimeContext.getServiceRegistry()
                .lookupServiceElseFail(EventBusService.class);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void postLifecycleEventIfRequired(
            final ManagedObject adapter,
            final Class<? extends LifecycleEventFacet> lifecycleEventFacetClass) {
        final LifecycleEventFacet facet = adapter.getSpecification().getFacet(lifecycleEventFacetClass);
        if(facet != null) {
            final Class<? extends AbstractLifecycleEvent<?>> eventType = facet.getEventType();
            final Object instance = InstanceUtil.createInstance(eventType);
            final Object pojo = adapter.getPojo();
            postEvent((AbstractLifecycleEvent) instance, pojo);
        }
    }
    
    //  -- HELPER
    
    private void postEvent(final AbstractLifecycleEvent<Object> event, final Object pojo) {
        if(eventBusService!=null) {
            event.setSource(pojo);
            eventBusService.post(event);
        }
    }

    
}