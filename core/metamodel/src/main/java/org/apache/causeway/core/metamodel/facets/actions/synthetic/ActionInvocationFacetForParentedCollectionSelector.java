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
package org.apache.causeway.core.metamodel.facets.actions.synthetic;

import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.causeway.core.metamodel.facets.collections.CollectionFacet;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;

import lombok.NonNull;
import lombok.val;

public class ActionInvocationFacetForParentedCollectionSelector
extends FacetAbstract
implements ActionInvocationFacet {

    private static Class<? extends Facet> type() {
        return ActionInvocationFacet.class;
    }

    private final @NonNull ObjectSpecification declaringType;
    private final @NonNull ObjectSpecification returnType;
    private final @NonNull OneToManyAssociation collection;
    private final @NonNull Can<ObjectAssociation> scalarProperties;

    public ActionInvocationFacetForParentedCollectionSelector(
            final @NonNull org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade method,
            final @NonNull ObjectSpecification declaringType,
            final @NonNull ObjectSpecification returnType,
            final @NonNull OneToManyAssociation collection,
            final @NonNull Can<ObjectAssociation> scalarProperties,
            final @NonNull org.apache.causeway.core.metamodel.facetapi.FacetHolder holder) {
        super(type(), holder);
        this.declaringType = declaringType;
        this.returnType = returnType;
        this.collection = collection;
        this.scalarProperties = scalarProperties;
    }

    @Override
    public ObjectSpecification getDeclaringType() {
        return declaringType;
    }

    @Override
    public ObjectSpecification getReturnType() {
        return returnType;
    }

    @Override
    public ManagedObject invoke(
            final ObjectAction owningAction,
            final InteractionHead head,
            final Can<ManagedObject> argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val parentAdapter = argumentAdapters.getElseFail(0);
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(parentAdapter)) {
            throw _Exceptions.illegalArgument("missing parent argument for synthetic selector %s", owningAction.getId());
        }

        val collectionAdapter = collection.get(parentAdapter, interactionInitiatedBy);
        val matches = CollectionFacet.streamAdapters(collectionAdapter)
                .filter(childAdapter -> matches(childAdapter, argumentAdapters, interactionInitiatedBy))
                .collect(Collectors.toList());

        if(matches.size() == 1) {
            return matches.get(0);
        }
        if(matches.isEmpty()) {
            throw _Exceptions.illegalArgument("no element of collection %s matches selector action %s", collection.getId(), owningAction.getId());
        }
        throw _Exceptions.illegalArgument("multiple elements of collection %s match selector action %s", collection.getId(), owningAction.getId());
    }

    private boolean matches(
            final ManagedObject childAdapter,
            final Can<ManagedObject> argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {
        for(int i = 0; i < scalarProperties.size(); i++) {
            val argumentAdapter = argumentAdapters.getElseFail(i + 1);
            if(ManagedObjects.isNullOrUnspecifiedOrEmpty(argumentAdapter)) {
                continue;
            }
            val property = scalarProperties.getElseFail(i);
            val propertyValue = property.get(childAdapter, interactionInitiatedBy);
            if(!Objects.equals(MmUnwrapUtils.single(propertyValue), MmUnwrapUtils.single(argumentAdapter))) {
                return false;
            }
        }
        return true;
    }

}
