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

import org.apache.causeway.core.metamodel.facets.DomainEventFacetAbstract.EventTypeOrigin;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Simply a tuple (pair) of {event-type, {@link EventTypeOrigin}}.
 */
public interface DomainEventHolder<T> {

    Class<? extends T> getEventType();
    EventTypeOrigin getEventTypeOrigin();

    static <X> DomainEventHolder<X> eager(final Class<? extends X> eventType, final EventTypeOrigin eventTypeOrigin) {
        return new DomainEventHolderEager<>(eventType, eventTypeOrigin);
    }

    @Getter @AllArgsConstructor
    static class DomainEventHolderEager<T> implements DomainEventHolder<T> {
        private final Class<? extends T> eventType;
        private final EventTypeOrigin eventTypeOrigin;
    }
}
