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

package org.apache.isis.metamodel.facets.collections.collection.modify;

import org.apache.isis.applib.events.domain.CollectionDomainEvent;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;

public class CollectionRemoveFromFacetForDomainEventFromCollectionAnnotation extends
CollectionRemoveFromFacetForDomainEventFromAbstract {

    public CollectionRemoveFromFacetForDomainEventFromCollectionAnnotation(
            final Class<? extends CollectionDomainEvent<?, ?>> eventType,
                    final PropertyOrCollectionAccessorFacet getterFacet,
                    final CollectionRemoveFromFacet collectionRemoveFromFacet,
                    final CollectionDomainEventFacetAbstract collectionInteractionFacet,
                    final ServiceRegistry serviceRegistry,
                    final FacetHolder holder) {

        super(eventType, getterFacet, collectionRemoveFromFacet, collectionInteractionFacet, 
                serviceRegistry, holder);
    }

}
