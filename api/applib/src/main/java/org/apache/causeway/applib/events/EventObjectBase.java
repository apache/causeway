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
package org.apache.causeway.applib.events;

import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.reflection._Reflect;

import static org.apache.causeway.commons.internal.reflection._Reflect.Filter.paramCount;

/**
 * @since 2.0 {@index}
 */
public abstract class EventObjectBase<T> {

    // -- FACTORIES

    /**
     * Optionally returns a new event instance,
     * based on whether the eventType has a public no-arg constructor.
     * <p>
     * Initializes the event's source with given {@code source}.
     */
    public static <T, E extends EventObjectBase<T>> Optional<E> getInstanceWithSource(
            final Class<E> eventType, final @Nullable T source) {
        return getInstanceWithSourceSupplier(eventType, (Supplier<T>) ()->source);
    }

    /**
     * Optionally returns a new event instance,
     * based on whether the eventType has a public no-arg constructor.
     * <p>
     * Initializes the event's source lazily, that is using given {@code eventSourceSupplier}.
     */
    public static <T, E extends EventObjectBase<T>> Optional<E> getInstanceWithSourceSupplier(
            final Class<E> eventType, final @Nullable Supplier<T> eventSourceSupplier) {
        return _Reflect.getPublicConstructors(eventType)
            .filter(paramCount(0))
            .getFirst()
            .map(_Reflect::invokeConstructor)
            .flatMap(Try::getValue)
            .map(evnt->{
                final E event = _Casts.uncheckedCast(evnt);
                event.sourceSupplier = eventSourceSupplier;
                return event;
            });
    }

    // --

    /**
     * Provides the object on which the Event initially occurred.
     */
    protected transient Supplier<T> sourceSupplier = null;

    /**
     * Constructs a prototypical Event.
     *
     * @param source object on which the Event initially occurred (nullable)
     */
    protected EventObjectBase(final @Nullable T source) {
        this.sourceSupplier = source!=null
                ? ()->source
                : null;
    }

    /**
     * Returns the object on which the Event initially occurred.
     */
    public @Nullable T getSource() {
        return sourceSupplier!=null
                ? sourceSupplier.get()
                : null;
    }

    /**
     * Returns a String representation of this EventObject.
     */
    @Override
    public String toString() {
        return getClass().getName() + "[source=" + getSource() + "]";
    }

}
