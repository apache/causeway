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
package org.apache.causeway.core.metamodel.facets.actions.action.invocation;

import java.util.function.BiConsumer;

import org.apache.causeway.applib.events.domain.ActionDomainEvent;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.DomainEventFacetAbstract;
import org.apache.causeway.core.metamodel.facets.DomainEventHolder;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;

import lombok.NonNull;

public abstract class ActionInvocationFacetAbstract
extends DomainEventFacetAbstract<ActionDomainEvent<?>>
implements ActionInvocationFacet, ImperativeFacet {

    private static final Class<? extends Facet> type() {
        return ActionInvocationFacet.class;
    }

    protected ActionInvocationFacetAbstract(
            final DomainEventHolder<ActionDomainEvent<?>> domainEventHolder,
            final FacetHolder holder) {
        // binds this DomainEventHolder to given DomainEventHolder, updateEventType not allowed
        super(type(), domainEventHolder, holder);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        ImperativeFacet.visitAttributes(this, visitor);
        visitor.accept("declaringType", getDeclaringType());
        visitor.accept("returnType", getReturnType());
        //visitor.accept("eventType", getEventType()); done already in super
        //visitor.accept("eventTypeOrigin", getEventTypeOrigin()); done already in super
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {
        return other instanceof ActionInvocationFacetAbstract
                ? this.getEventType() == ((ActionInvocationFacetAbstract)other).getEventType()
                : false;
    }

}
