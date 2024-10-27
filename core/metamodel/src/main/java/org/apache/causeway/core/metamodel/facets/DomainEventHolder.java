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
package org.apache.causeway.core.metamodel.facets;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.facets.DomainEventFacetAbstract.EventTypeOrigin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * Simply a tuple of {event-type, {@link EventTypeOrigin}, post-able-flag}.
 */
public interface DomainEventHolder<T> {

    Class<? extends T> getEventType();
    EventTypeOrigin getEventTypeOrigin();

    /**
     * Whether {@link #getEventType()} is post-able.
     * <ul>
     * <li>If the Noop event-type is assignable from {@link #getEventType()} then NO.</li>
     * <li>If {@link #getEventType()} is the Default event-type and its configured to act as Noop then NO.</li>
     * <li>Otherwise YES.</li>
     * </ul>
     */
    boolean isPostable();

    /**
     * Whether this {@link DomainEventHolder} is an empty holder.
     * That is a holder, that will never participate with the frameworks
     * event system.
     */
    static boolean isEmpty(final @Nullable DomainEventHolder<?> domainEventHolder) {
        return domainEventHolder==null
                || domainEventHolder instanceof DomainEventHolderEmpty;
    }

    // -- FACTORIES

    static <X> DomainEventHolder<X> empty() {
        return new DomainEventHolderEmpty<>();
    }

    static <X> DomainEventHolder<X> eager(
            final Class<? extends X> eventType,
            final EventTypeOrigin eventTypeOrigin,
            final boolean postable) {
        return new DomainEventHolderEager<>(eventType, eventTypeOrigin, postable);
    }

    // -- IMPLEMENTATIONS

    static final class DomainEventHolderEmpty<T> implements DomainEventHolder<T> {
        @Override public Class<? extends T> getEventType() {
            throw _Exceptions.noSuchElement("the empty DomainEventHolder has no event-type");
        }
        @Override public EventTypeOrigin getEventTypeOrigin() {
            throw _Exceptions.noSuchElement("the empty DomainEventHolder has no event-type-origin");
        }
        @Override public boolean isPostable() { return false; }
    }

    @Getter @AllArgsConstructor
    static final class DomainEventHolderEager<T> implements DomainEventHolder<T> {
        private final @NonNull Class<? extends T> eventType;
        private final @NonNull EventTypeOrigin eventTypeOrigin;
        private boolean postable;
    }

}
