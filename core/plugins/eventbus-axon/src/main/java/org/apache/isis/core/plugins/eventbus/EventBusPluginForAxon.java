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
package org.apache.isis.core.plugins.eventbus;

import static java.util.Objects.requireNonNull;
import static org.apache.isis.commons.internal.base._With.acceptIfPresent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.axonframework.common.Registration;
import org.axonframework.eventhandling.AnnotationEventListenerAdapter;
import org.axonframework.eventhandling.EventListenerProxy;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventhandling.SimpleEventBus;

import org.apache.isis.applib.events.domain.AbstractDomainEvent;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.runtime.services.eventbus.EventBusImplementationAbstract;


/**
 * A wrapper for an Axon {@link org.axonframework.eventhandling.SimpleEventBus},
 * allowing arbitrary events to be posted and subscribed to.
 */
public class EventBusPluginForAxon extends EventBusImplementationAbstract {

    private SimpleEventBus simpleEventBus = new SimpleEventBus();

    private Map<Object, AxonEventListenerAdapter> listenerAdapterByDomainService = new ConcurrentHashMap<>();

    @Override
    public void register(final Object domainService) {
        final AxonEventListenerAdapter adapter = lookupOrCreateAdapterFor(domainService);
        final Registration registrationHandle = simpleEventBus.subscribe(eventProcessorFor(adapter));
        adapter.registration = registrationHandle;
    }
    
    @Override
    public void unregister(final Object domainService) {
        acceptIfPresent(lookupAdapterFor(domainService), adapter->{
            acceptIfPresent(adapter.registration, Registration::cancel);
        });
    }

    @Override
    public void post(final Object event) {
        simpleEventBus.publish(GenericEventMessage.asEventMessage(event));
    }

    @Override
    public <T> EventBusPlugin.EventListener<T> addEventListener(
            final Class<T> targetType,
            final Consumer<T> onEvent) {

        final AxonEventListener<T> eventListener = new AxonEventListener<T>(targetType, onEvent);
        final EventListenerProxy proxy = eventListener.proxy();
        
        final Registration registrationHandle = simpleEventBus.subscribe(eventProcessorFor(proxy));
        eventListener.registration = registrationHandle;
        
        return eventListener;
    }

    @Override
    public <T> void removeEventListener(EventBusPlugin.EventListener<T> eventListener) {
        if(eventListener instanceof AxonEventListener) {
            final AxonEventListener<T> listenerInstance = (AxonEventListener<T>) eventListener;
            acceptIfPresent(listenerInstance.registration, Registration::cancel);
        }
    }

    @Override
    protected AbstractDomainEvent<?> asDomainEvent(final Object event) {
        if(event instanceof GenericEventMessage) {
            // this seems to be the case on error

            @SuppressWarnings("rawtypes")
            final GenericEventMessage genericEventMessage = (GenericEventMessage) event;
            final Object payload = genericEventMessage.getPayload();
            return asDomainEventIfPossible(payload);
        }
        // don't think this occurs with axon, but this is the original behavior
        // before the above change to detect GenericEventMessage
        return asDomainEventIfPossible(event);
    }

    // -- HELPER
    
    private Consumer<List<? extends EventMessage<?>>> eventProcessorFor(final EventListenerProxy proxy) {
        return eventMessages->{
            _NullSafe.stream(eventMessages)
            .filter(proxy::canHandle)
            .forEach(event->{
                try {
                    proxy.handle(event);
                } catch (final Exception exception) {
                    processException(exception, event);
                }
            });
        };
    }
    
    private Consumer<List<? extends EventMessage<?>>> eventProcessorFor(final AxonEventListenerAdapter adapter) {
        return eventMessages->{
            _NullSafe.stream(eventMessages)
            .filter(adapter::canHandle)
            .forEach(event->{
                try {
                    adapter.handle(event);
                } catch (final Exception exception) {
                    processException(exception, event);
                }
            });
        };
    }

    /**
     * Wraps a Consumer as EventBusImplementation.EventListener with the given targetType.
     * @param <T>
     * @since 2.0.0
     */
    static class AxonEventListener<T> implements EventBusPlugin.EventListener<T> {
        private final Consumer<T> eventConsumer;
        private final EventListenerProxy proxy;
        private Registration registration;
        
        private AxonEventListener(final Class<T> targetType, final Consumer<T> eventConsumer) {
            this.eventConsumer = requireNonNull(eventConsumer);
            this.proxy = new EventListenerProxy() {
                @SuppressWarnings("unchecked")
                @Override
                public void handle(@SuppressWarnings("rawtypes") EventMessage event) {
                    final Object payload = event.getPayload();
                    if(payload==null) {
                        return;
                    }
                    if(targetType.isAssignableFrom(event.getPayloadType())){
                        on((T)event.getPayload());
                    }
                }
                @Override
                public Class<?> getTargetType() {
                    return targetType;
                }
            };
        }
        @Override
        public void on(T event) {
            eventConsumer.accept(event);
        }
        public EventListenerProxy proxy() {
            return proxy;
        }

    }

    private AxonEventListenerAdapter lookupOrCreateAdapterFor(final Object domainService) {
        AxonEventListenerAdapter annotationEventListenerAdapter = lookupAdapterFor(domainService);
        if (annotationEventListenerAdapter == null) {
            annotationEventListenerAdapter = new AxonEventListenerAdapter(domainService);
            listenerAdapterByDomainService.put(domainService, annotationEventListenerAdapter);
        }
        return annotationEventListenerAdapter;
    }

    private AxonEventListenerAdapter lookupAdapterFor(final Object domainService) {
        return listenerAdapterByDomainService.get(domainService);
    }
    
    private AbstractDomainEvent<?> asDomainEventIfPossible(final Object event) {
        if (event instanceof AbstractDomainEvent)
            return (AbstractDomainEvent<?>) event;
        else
            return null;
    }

    class AxonEventListenerAdapter extends AnnotationEventListenerAdapter {

        private Registration registration;

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