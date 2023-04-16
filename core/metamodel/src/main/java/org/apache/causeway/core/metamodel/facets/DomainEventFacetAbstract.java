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

import java.util.function.BiConsumer;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

import lombok.NonNull;

public abstract class DomainEventFacetAbstract<T>
extends FacetAbstract {

    public static enum EventTypeOrigin {
        /** {@link #eventType} originates from configured defaults */
        DEFAULT,
        /** {@link #eventType} originates from domain object annotation */
        ANNOTATED_OBJECT,
        /** {@link #eventType} originates from member annotation */
        ANNOTATED_MEMBER;
        public boolean isDefault() { return this==DEFAULT; }
        public boolean isAnnotatedObject() { return this==ANNOTATED_OBJECT; }
        public boolean isAnnotatedMember() { return this==ANNOTATED_MEMBER; }
    }

    private Class<? extends T> eventType;
    private EventTypeOrigin eventTypeOrigin;

    protected DomainEventFacetAbstract(
            final Class<? extends Facet> facetType,
            final Class<? extends T> eventType,
            final EventTypeOrigin eventTypeOrigin,
            final FacetHolder holder) {
        super(facetType, holder);
        this.eventType = eventType;
        this.eventTypeOrigin = eventTypeOrigin;
    }

    public final Class<? extends T> getEventType() {
        return eventType;
    }

    public final EventTypeOrigin getEventTypeOrigin() {
        return eventTypeOrigin;
    }

    /** called during meta-model post-processing only */
    protected final void updateEventType(
            final Class<? extends T> eventType,
            final EventTypeOrigin eventTypeOrigin) {
        this.eventType = eventType;
        this.eventTypeOrigin = eventTypeOrigin;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("eventType", getEventType());
        visitor.accept("eventTypeOrigin", getEventTypeOrigin().name());
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {
        return other instanceof DomainEventFacetAbstract
                ? this.getEventType() == ((DomainEventFacetAbstract<?>)other).getEventType()
                : false;
    }

}
