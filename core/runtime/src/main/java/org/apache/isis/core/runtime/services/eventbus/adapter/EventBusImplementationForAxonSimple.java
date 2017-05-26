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

import org.apache.isis.applib.services.eventbus.AbstractDomainEvent;
import org.apache.isis.core.runtime.services.eventbus.EventBusImplementationAbstract;

/**
 * A wrapper for an Axon {@link org.axonframework.eventhandling.SimpleEventBus},
 * allowing arbitrary events to be posted and subscribed to.
 */
public class EventBusImplementationForAxonSimple extends EventBusImplementationAbstract {

    private SimpleEventBus simpleEventBus = new SimpleEventBus();

    private Map<Object, AxonEventListenerAdapter> listenerAdapterByDomainService = Maps.newConcurrentMap();

    private AxonEventListenerAdapter adapterFor(final Object domainService) {
        AxonEventListenerAdapter annotationEventListenerAdapter = listenerAdapterByDomainService.get(domainService);
        if (annotationEventListenerAdapter == null) {
            annotationEventListenerAdapter = new AxonEventListenerAdapter(domainService);
            listenerAdapterByDomainService.put(domainService, annotationEventListenerAdapter);
        }
        return annotationEventListenerAdapter;
    }

    @Override
    public void register(final Object domainService) {
        simpleEventBus.subscribe(adapterFor(domainService));
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


    @Override
    protected AbstractDomainEvent<?> asDomainEvent(final Object event) {
        if(event instanceof GenericEventMessage) {
            // this seems to be the case on error

            final GenericEventMessage genericEventMessage = (GenericEventMessage) event;
            final Object payload = genericEventMessage.getPayload();
            return asDomainEventIfPossible(payload);
        }
        // don't think this occurs with axon, but this is the original behaviour
        // before the above change to detect GenericEventMessage
        return asDomainEventIfPossible(event);
    }

    private AbstractDomainEvent<?> asDomainEventIfPossible(final Object event) {
        if (event instanceof AbstractDomainEvent)
            return (AbstractDomainEvent<?>) event;
        else
            return null;
    }

    class AxonEventListenerAdapter extends AnnotationEventListenerAdapter {

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