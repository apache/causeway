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
package org.apache.causeway.core.metamodel.facets.collections.collection.modify;

import org.apache.causeway.applib.events.domain.AbstractDomainEvent;
import org.apache.causeway.applib.events.domain.CollectionDomainEvent;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.DomainEventFacetAbstract;
import org.apache.causeway.core.metamodel.facets.DomainEventHelper;
import org.apache.causeway.core.metamodel.interactions.HidingInteractionAdvisor;
import org.apache.causeway.core.metamodel.interactions.VisibilityContext;

public class CollectionDomainEventFacet
extends DomainEventFacetAbstract<CollectionDomainEvent<?, ?>>
implements HidingInteractionAdvisor {

    private final DomainEventHelper domainEventHelper;

    public CollectionDomainEventFacet(
            final Class<? extends CollectionDomainEvent<?, ?>> eventType,
            final EventTypeOrigin eventTypeOrigin,
            final FacetHolder holder) {

        super(CollectionDomainEventFacet.class, eventType, eventTypeOrigin, holder);
        domainEventHelper = DomainEventHelper.ofServiceRegistry(getServiceRegistry());
    }

    @Override
    public String hides(final VisibilityContext ic) {

        final CollectionDomainEvent<?, ?> event =
                domainEventHelper.postEventForCollection(
                        AbstractDomainEvent.Phase.HIDE,
                        _Casts.uncheckedCast(getEventType()),
                        getFacetHolder(), ic.getHead()
                );
        if (event != null && event.isHidden()) {
            return "Hidden by subscriber";
        }
        return null;
    }

}
