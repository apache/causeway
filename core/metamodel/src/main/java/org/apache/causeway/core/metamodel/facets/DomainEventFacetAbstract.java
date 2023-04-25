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

import org.apache.causeway.applib.events.domain.AbstractDomainEvent;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.object.MmEventUtils;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;

/**
 * Base for any event-type holding facets,
 * while defining the facet-type is up to implementors.
 */
public abstract class DomainEventFacetAbstract<T extends AbstractDomainEvent<?>>
extends FacetAbstract
implements DomainEventHolder<T> {

    public static enum EventTypeOrigin {
        /** {@link #getEventType} originates from configured defaults */
        DEFAULT,
        /** {@link #getEventType} originates from domain object annotation */
        ANNOTATED_OBJECT,
        /** {@link #getEventType} originates from member annotation */
        ANNOTATED_MEMBER;
        public boolean isDefault() { return this==DEFAULT; }
        public boolean isAnnotatedObject() { return this==ANNOTATED_OBJECT; }
        public boolean isAnnotatedMember() { return this==ANNOTATED_MEMBER; }
    }

    private DomainEventHolder<T> domainEventHolder;
    private final boolean isUpdateEventTypeAllowed;

    /**  using delegated event type holder, updateEventType not allowed */
    protected DomainEventFacetAbstract(
            final Class<? extends Facet> facetType,
            final DomainEventHolder<T> domainEventHolder,
            final FacetHolder holder) {
        super(facetType, holder);
        this.isUpdateEventTypeAllowed = false;
        this.domainEventHolder = domainEventHolder;
    }

    /** using eager event type, updateEventType allowed */
    protected DomainEventFacetAbstract(
            final Class<? extends Facet> facetType,
            final Class<? extends T> eventType,
            final EventTypeOrigin eventTypeOrigin,
            final FacetHolder holder) {
        super(facetType, holder);
        this.isUpdateEventTypeAllowed = true;
        updateEventType(eventType, eventTypeOrigin);
    }

    @Override
    public final Class<? extends T> getEventType() {
        return domainEventHolder.getEventType();
    }

    @Override
    public final EventTypeOrigin getEventTypeOrigin() {
        return domainEventHolder.getEventTypeOrigin();
    }

    @Override
    public final boolean isPostable() {
        return domainEventHolder.isPostable();
    }

    /** called during meta-model post-processing only */
    protected final void updateEventType(
            final Class<? extends T> eventType,
            final EventTypeOrigin eventTypeOrigin) {
        _Assert.assertTrue(isUpdateEventTypeAllowed, ()->
            "framework bug: this DomainEventHolder is bound to another DomainEventHolder,"
            + "the binding is immutable and cannot be changed");
        this.domainEventHolder = DomainEventHolder.eager(eventType, eventTypeOrigin, isPostable(eventType));
    }

    /**
     * Called by meta-model post-processors, to honor domain object annotations on mixees.
     * (required only, if this facet belongs to a mixed-in member)
     */
    public void initWithMixee(final ObjectSpecification mixeeSpec) {}

    /**
     * Whether given {@code eventType} is post-able.
     * <ul>
     * <li>If the Noop event-type is assignable from {@code eventType} then NO.</li>
     * <li>If {@code eventType} is the Default event-type and its configured to act as Noop then NO.</li>
     * <li>Otherwise YES.</li>
     * </ul>
     */
    protected final boolean isPostable(final Class<? extends T> eventType) {
        return MmEventUtils.isDomainEventPostable(getConfiguration(), eventType);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        if(DomainEventHolder.isEmpty(domainEventHolder)) {
            visitor.accept("eventType", "None "
                    + "(No execution related domain events for mixed-in prop/coll.)");
            visitor.accept("eventTypeOrigin", "NONE");
        } else {
            visitor.accept("eventType", getEventType());
            visitor.accept("eventTypeOrigin", getEventTypeOrigin().name());
        }
        visitor.accept("isPostable", isPostable());
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {
        return other instanceof DomainEventFacetAbstract
                ? this.getEventType() == ((DomainEventFacetAbstract<?>)other).getEventType()
                : false;
    }

}
