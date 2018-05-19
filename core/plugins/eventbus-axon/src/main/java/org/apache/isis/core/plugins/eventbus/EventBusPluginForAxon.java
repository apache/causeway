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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.apache.isis.applib.events.domain.AbstractDomainEvent;
import org.apache.isis.core.plugins.eventbus.EventBusPlugin;
import org.apache.isis.core.runtime.services.eventbus.EventBusImplementationAbstract;
import org.axonframework.domain.EventMessage;
import org.axonframework.domain.GenericEventMessage;
import org.axonframework.eventhandling.EventListenerProxy;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventhandling.annotation.AnnotationEventListenerAdapter;

/**
 * A wrapper for an Axon {@link org.axonframework.eventhandling.SimpleEventBus},
 * allowing arbitrary events to be posted and subscribed to.
 */
public class EventBusPluginForAxon extends EventBusImplementationAbstract {

    private SimpleEventBus simpleEventBus = new SimpleEventBus();

    private Map<Object, AxonEventListenerAdapter> listenerAdapterByDomainService = new ConcurrentHashMap<>();

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
	public <T> EventBusPlugin.EventListener<T> addEventListener(
			final Class<T> targetType, 
			final Consumer<T> onEvent) {
		
		final AxonEventListener<T> eventListener = new AxonEventListener<T>(targetType, onEvent);
		simpleEventBus.subscribe(eventListener.proxy());
		return eventListener;
	}

	@Override
	public <T> void removeEventListener(EventBusPlugin.EventListener<T> eventListener) {
		if(eventListener instanceof AxonEventListener) {
			simpleEventBus.unsubscribe(((AxonEventListener<T>)eventListener).proxy());	
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
        // don't think this occurs with axon, but this is the original behaviour
        // before the above change to detect GenericEventMessage
        return asDomainEventIfPossible(event);
    }

    // -- HELPER
    
    /**
     * Wraps a Consumer as EventBusImplementation.EventListener with the given targetType.
     * @param <T>
     * @since 2.0.0
     */
    static class AxonEventListener<T> implements EventBusPlugin.EventListener<T> {
    	private final Consumer<T> eventConsumer;
		private final EventListenerProxy proxy;
    	private AxonEventListener(final Class<T> targetType, final Consumer<T> eventConsumer) {
			this.eventConsumer = Objects.requireNonNull(eventConsumer);
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
    
    private AxonEventListenerAdapter adapterFor(final Object domainService) {
        AxonEventListenerAdapter annotationEventListenerAdapter = listenerAdapterByDomainService.get(domainService);
        if (annotationEventListenerAdapter == null) {
            annotationEventListenerAdapter = new AxonEventListenerAdapter(domainService);
            listenerAdapterByDomainService.put(domainService, annotationEventListenerAdapter);
        }
        return annotationEventListenerAdapter;
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