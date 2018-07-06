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

import java.util.function.Consumer;

import org.apache.isis.commons.internal.context._Plugin;

/**
 * Common interface for all Event Bus implementations.
 *
 * <p>
 *     Defines a plug-able SPI to the
 *     {@link org.apache.isis.applib.services.eventbus.EventBusService},
 *     to allow alternative implementations of in-memory event bus to be used.
 * </p>
 *
 */
public interface EventBusPlugin {

    /**
     * For {@link org.apache.isis.applib.services.eventbus.EventBusService} to call on
     * {@link org.apache.isis.applib.services.eventbus.EventBusService#register(Object)}.
     */
    void register(Object domainService);

    /**
     * For {@link org.apache.isis.applib.services.eventbus.EventBusService} to call on
     * {@link org.apache.isis.applib.services.eventbus.EventBusService#unregister(Object)}.
     */
    void unregister(Object domainService);

    /**
     * For {@link org.apache.isis.applib.services.eventbus.EventBusService} to call on
     * {@link org.apache.isis.applib.services.eventbus.EventBusService#post(Object)}.
     */
    void post(Object event);

    /**
     * Programmatically adds an event listener (wrapping the specified Consumer {@code onEvent})
     * to the event-bus.
     * @param onEvent
     * @return an EventListener instance
     * @since 2.0.0
     */
    <T> EventListener<T> addEventListener(final Class<T> targetType, Consumer<T> onEvent);

    /**
     * Removes the {@code eventListener} from the event-bus.
     * @param eventListener
     * @since 2.0.0
     */
    <T> void removeEventListener(EventListener<T> eventListener);

    // -- EVENT LISTENER

    public static interface EventListener<T> {
        public void on(T event);
    }

    // -- PLUGIN LOOKUP

    public static EventBusPlugin get() {
        return _Plugin.getOrElse(EventBusPlugin.class,
                ambiguousPlugins->{
                    throw _Plugin.ambiguityNonRecoverable(EventBusPlugin.class, ambiguousPlugins);
                },
                ()->{
                    throw _Plugin.absenceNonRecoverable(EventBusPlugin.class);
                });
    }

}