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
package org.apache.isis.viewer.restfulobjects.rendering.domainobjects;

import java.util.stream.Stream;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ActionResultRepresentation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectAndActionInvocation {

    public static ObjectAndActionInvocation of(
            @NonNull ActionInteraction.Result actionInteractionResult,
            @NonNull JsonRepresentation argsJsonRepr,
            @NonNull ActionResultReprRenderer.SelfLink selfLink) {
        return new ObjectAndActionInvocation(
                actionInteractionResult.getManagedAction().getOwner(),
                actionInteractionResult.getManagedAction().getAction(),
                argsJsonRepr,
                actionInteractionResult.getParameterList(),
                actionInteractionResult.getActionReturnedObject(),
                selfLink);
    }

    @Getter private final ManagedObject objectAdapter;
    @Getter private final ObjectAction action;
    @Getter private final JsonRepresentation arguments;
    @Getter private final Can<ManagedObject> argAdapters;
    @Getter private final ManagedObject returnedAdapter;
    @Getter private final ActionResultReprRenderer.SelfLink selfLink;


    /**
     * not API
     */
    public ActionResultRepresentation.ResultType determineResultType() {

        val returnTypeSpec = this.action.getReturnType();

        if (returnTypeSpec.getCorrespondingClass() == void.class) {
            return ActionResultRepresentation.ResultType.VOID;
        }

        if (isVector(returnedAdapter.getSpecification())) {

            // though not strictly required, try to be consistent:  empty list vs populated list
            if(elementAdapters.get().isEmpty()) {
                val isElementTypeAScalarValue = returnTypeSpec.getElementSpecification()
                .map(elementSpec->isScalarValue(elementSpec))
                .orElse(false);
                return isElementTypeAScalarValue
                        ? ActionResultRepresentation.ResultType.SCALAR_VALUES
                        : ActionResultRepresentation.ResultType.LIST;
            }

            // inspect the collection's elements
            val isListOfDomainObjects = streamElementAdapters()
                    .allMatch(elementAdapter->!isScalarValue(elementAdapter.getSpecification()));

            return isListOfDomainObjects
                    ? ActionResultRepresentation.ResultType.LIST
                    : ActionResultRepresentation.ResultType.SCALAR_VALUES;
        }

        if (isScalarValue(returnedAdapter.getSpecification())) {
            return ActionResultRepresentation.ResultType.SCALAR_VALUE;
        }

        // else
        return ActionResultRepresentation.ResultType.DOMAIN_OBJECT;
    }

    public Stream<ManagedObject> streamElementAdapters() {
        return elementAdapters.get().stream();
    }

    public boolean hasElements() {
        return !elementAdapters.get().isEmpty();
    }

    // -- HELPER

    private final _Lazy<Can<ManagedObject>> elementAdapters = _Lazy.threadSafe(this::initElementAdapters);
    private Can<ManagedObject> initElementAdapters() {
        return CollectionFacet.streamAdapters(returnedAdapter).collect(Can.toCan());
    }

    //TODO[2449] need to check whether that strategy holds consistently
    private static boolean isScalarValue(final @NonNull ObjectSpecification spec) {
        return spec.getFacet(EncodableFacet.class)!=null;
    }

    private static boolean isVector(final @NonNull ObjectSpecification spec) {
        return spec.getFacet(CollectionFacet.class)!=null;
    }


}