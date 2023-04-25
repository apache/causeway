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
package org.apache.causeway.core.metamodel.facets.object.domainobject.domainevents;

import java.util.Optional;

import org.apache.causeway.applib.events.domain.CollectionDomainEvent;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.DomainEventFacetAbstract;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacet;

/**
 * This does <i>NOT</i> implement {@link ActionDomainEventFacet}, rather it is to record the default type to use
 * for any actions as a fallback/default.
 */
public class CollectionDomainEventDefaultFacetForDomainObjectAnnotation
extends DomainEventFacetAbstract<CollectionDomainEvent<?, ?>> {

    public static Optional<CollectionDomainEventDefaultFacetForDomainObjectAnnotation> create(
            final Class<? extends CollectionDomainEvent<?,?>> domainEvent,
            final FacetHolder holder) {
        return domainEvent != CollectionDomainEvent.Default.class
                ? Optional.of(new CollectionDomainEventDefaultFacetForDomainObjectAnnotation(domainEvent, holder))
                : Optional.empty();
    }

    private static Class<? extends Facet> type() {
        return CollectionDomainEventDefaultFacetForDomainObjectAnnotation.class;
    }

    private CollectionDomainEventDefaultFacetForDomainObjectAnnotation(
            final Class<? extends CollectionDomainEvent<?, ?>> value,
            final FacetHolder holder) {
        super(type(), value, EventTypeOrigin.ANNOTATED_OBJECT, holder);
    }

}
