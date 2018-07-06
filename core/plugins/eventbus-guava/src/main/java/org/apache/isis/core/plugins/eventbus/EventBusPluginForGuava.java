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

import java.util.Objects;
import java.util.function.Consumer;

import org.apache.isis.applib.events.domain.AbstractDomainEvent;
import org.apache.isis.core.plugins.eventbus.EventBusPlugin;
import org.apache.isis.core.runtime.services.eventbus.EventBusImplementationAbstract;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

/**
 * A wrapper for a Guava {@link com.google.common.eventbus.EventBus},
 * allowing arbitrary events to be posted and subscribed to.
 */
public class EventBusPluginForGuava extends EventBusImplementationAbstract {

    private final com.google.common.eventbus.EventBus eventBus =
            new com.google.common.eventbus.EventBus(newEventBusSubscriberExceptionHandler());

    protected SubscriberExceptionHandler newEventBusSubscriberExceptionHandler() {
        return new SubscriberExceptionHandler() {
            @Override
            public void handleException(final Throwable exception,
                    final SubscriberExceptionContext context) {
                final Object event = context.getEvent();
                processException(exception, event);
            }

        };
    }

    @Override
    public void register(final Object domainService) {
        eventBus.register(domainService);
    }

    @Override
    public void unregister(final Object domainService) {
        // Intentionally no-op. //TODO [ahuber] why?
        // this.eventBus.unregister(domainService);
    }

    @Override
    public void post(final Object event) {
        eventBus.post(event);
    }

    @Override
    protected AbstractDomainEvent<?> asDomainEvent(final Object event) {
        return event instanceof AbstractDomainEvent
                ? (AbstractDomainEvent<?>) event
                        : null;
    }

    @Override
    public <T> EventListener<T> addEventListener(final Class<T> targetType, final Consumer<T> onEvent) {
        final EventListener<T> eventListener = new GuavaEventListener<>(targetType, onEvent);
        eventBus.register(eventListener);
        return eventListener;
    }

    @Override
    public <T> void removeEventListener(EventListener<T> eventListener) {
        eventBus.unregister(eventListener);
    }

    // -- HELPER

    private static class GuavaEventListener<T> implements EventBusPlugin.EventListener<T> {
        private final Class<T> targetType;
        private final Consumer<T> eventConsumer;
        private GuavaEventListener(final Class<T> targetType, final Consumer<T> eventConsumer) {
            this.targetType = Objects.requireNonNull(targetType);
            this.eventConsumer = Objects.requireNonNull(eventConsumer);
        }
        @com.google.common.eventbus.Subscribe
        @Override
        public void on(T payload) {
            if(payload==null) {
                return;
            }
            if(targetType.isAssignableFrom(payload.getClass())){
                eventConsumer.accept(payload);
            }
        }
    }



}
