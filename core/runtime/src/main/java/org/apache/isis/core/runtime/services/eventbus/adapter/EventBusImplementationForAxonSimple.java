/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.runtime.services.eventbus.adapter;

import java.util.Map;
import com.google.common.collect.Maps;
import org.axonframework.domain.EventMessage;
import org.axonframework.domain.GenericEventMessage;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventhandling.annotation.AnnotationEventListenerAdapter;
import org.apache.isis.core.runtime.services.eventbus.EventBusImplementationAbstract;

/**
 * A wrapper for an Axon {@link org.axonframework.eventhandling.SimpleEventBus},
 * allowing arbitrary events to be posted and subscribed to.
 */
public class EventBusImplementationForAxonSimple extends EventBusImplementationAbstract {

    // TODO: does this need to be static?
    private static SimpleEventBus simpleEventBus = new SimpleEventBus();

    // TODO: does this need to be static?
    private static Map<Object, AxonEventListenerAdapter> adapters = Maps.newConcurrentMap();

    private static AxonEventListenerAdapter adapterFor(final Object domainService) {
        AxonEventListenerAdapter annotationEventListenerAdapter = adapters.get(domainService);
        if (annotationEventListenerAdapter == null) {
            annotationEventListenerAdapter = new AxonEventListenerAdapter(domainService);
            adapters.put(domainService, annotationEventListenerAdapter);
        }
        return annotationEventListenerAdapter;
    }

    @Override
    public void register(final Object domainService) {
        simpleEventBus.subscribe(EventBusImplementationForAxonSimple.adapterFor(domainService));
    }

    @Override
    public void unregister(final Object domainService) {
        // Seems it's needed to be a no-op (See EventBusService).
        // AxonSimpleEventBusAdapter.simpleEventBus.unsubscribe(AxonSimpleEventBusAdapter.adapterFor(domainService));

    }

    /*
     * Logic equivalent to Guava Event Bus.
     *
     * <p>
     *     Despite that, event processing cannot be followed after an Exception is thrown.
     * </p>
     */
    @Override
    public void post(final Object event) {
        simpleEventBus.publish(GenericEventMessage.asEventMessage(event));
    }


    static class AxonEventListenerAdapter extends AnnotationEventListenerAdapter {

        public AxonEventListenerAdapter(final Object annotatedEventListener) {
            super(annotatedEventListener);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void handle(@SuppressWarnings("rawtypes") final EventMessage event) {
            try {
                super.handle(event);
            } catch (final Exception exception) {
                processException(exception, event);
            }
        }
    }

}