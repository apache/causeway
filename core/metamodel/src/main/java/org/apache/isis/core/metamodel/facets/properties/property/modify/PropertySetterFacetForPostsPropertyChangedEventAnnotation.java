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

package org.apache.isis.core.metamodel.facets.properties.property.modify;

import org.apache.isis.applib.services.eventbus.PropertyDomainEvent;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;

/**
 * @deprecated
 */
@Deprecated
public class PropertySetterFacetForPostsPropertyChangedEventAnnotation
        extends PropertySetterFacetForDomainEventAbstract {


    public PropertySetterFacetForPostsPropertyChangedEventAnnotation(
            final Class<? extends PropertyDomainEvent<?, ?>> eventType,
            final PropertyOrCollectionAccessorFacet getterFacet,
            final PropertySetterFacet setterFacet,
            final PropertyDomainEventFacetAbstract propertyInteractionFacet,
            final FacetHolder holder, final ServicesInjector servicesInjector) {
        super(eventType, getterFacet, setterFacet, propertyInteractionFacet, servicesInjector, holder);
    }

    @Override
    protected PropertyDomainEvent<?, ?> verify(final PropertyDomainEvent<?, ?> event) {
        // will discard event if different type to that specified in the PostsPropertyChangedEvent annotation.
        return event != null && value() == event.getClass() ? event : null;
    }

}
