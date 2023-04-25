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

import java.lang.reflect.Method;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.DomainEventHolder;
import org.apache.causeway.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacet;
import org.apache.causeway.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacet;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

/**
 * Handles execution of the pseudo getter for mixed-in properties and collections,
 * which triggers NO execution related domain events.
 * <p>
 * 'Pseudo getter' because the underlying {@link Method} is not a bean-specification-obeying getter,
 * but rather an arbitrary named no-arg method like {@code prop()} or {@code coll()}.
 * <p>
 * Hiding advisory is instead given by {@link PropertyDomainEventFacet} or {@link CollectionDomainEventFacet},
 * expected to be installed on the same {@link FacetHolder}.
 */
public class ActionInvocationFacetForMixedInPropertyOrCollection
extends ActionInvocationFacetAbstract {

    public ActionInvocationFacetForMixedInPropertyOrCollection(
            final MethodFacade method,
            final ObjectSpecification declaringType,
            final ObjectSpecification returnType,
            final FacetHolder holder) {

        /* DomainEventHolder is empty, because for mixed-in prop/coll there are no execution related events.
         * However, we are using PropertyDomainEventFacet or CollectionDomainEventFacet for hiding advisory. */
        super(DomainEventHolder.empty(), method, declaringType, returnType, holder);
    }

    @Override
    public ManagedObject invoke(
            final ObjectAction owningAction,
            final InteractionHead head,
            final Can<ManagedObject> argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return memberExecutorService.invokeAction(getFacetHolder(), interactionInitiatedBy,
                head, argumentAdapters, owningAction, this);
    }

}
