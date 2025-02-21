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
package org.apache.causeway.viewer.restfulobjects.rendering.domainobjects;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.domainobjects.ActionResultRepresentation;

import lombok.AccessLevel;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectAndActionInvocation {

    public static ObjectAndActionInvocation of(
            final ActionInteraction.@NonNull Result actionInteractionResult,
            final @NonNull JsonRepresentation argsJsonRepr,
            final ActionResultReprRenderer.@NonNull SelfLink selfLink) {
        return new ObjectAndActionInvocation(
                actionInteractionResult.managedAction().getOwner(),
                actionInteractionResult.managedAction().getAction(),
                argsJsonRepr,
                actionInteractionResult.parameterList(),
                actionInteractionResult.actionReturnedObject(),
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

        var returnTypeSpec = this.action.getReturnType();

        if (returnTypeSpec.getCorrespondingClass() == void.class) {
            return ActionResultRepresentation.ResultType.VOID;
        }

        //FIXME following decision tree should not depend on the returned runtime types
        // but on the returnTypeSpec,
        // which is introspected eagerly on application start and should be the binding contract
        var actualReturnTypeSpec = returnedAdapter.getSpecification();

        if (ManagedObjects.isPacked(returnedAdapter)
                || isVector(actualReturnTypeSpec)) {

            // though not strictly required, try to be consistent:  empty list vs populated list
            if(elementAdapters.get().isEmpty()) {
                var isElementTypeAScalarValue = returnTypeSpec.getElementSpecification()
                .map(elementSpec->isScalarValue(elementSpec))
                .orElse(false);
                return isElementTypeAScalarValue
                        ? ActionResultRepresentation.ResultType.SCALAR_VALUES
                        : ActionResultRepresentation.ResultType.LIST;
            }

            // inspect the collection's elements
            var isListOfDomainObjects = streamElementAdapters()
                    .allMatch(elementAdapter->!isScalarValue(elementAdapter.getSpecification()));

            return isListOfDomainObjects
                    ? ActionResultRepresentation.ResultType.LIST
                    : ActionResultRepresentation.ResultType.SCALAR_VALUES;
        }

        if (isScalarValue(actualReturnTypeSpec)) {
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

    /**
     * Returns the ObjectSpecification of the compile time return type of the associated action.
     * (not inspecting the runtime type)
     */
    public ObjectSpecification getReturnTypeSpecification() {
        return getAction().getReturnType();
    }

    // -- UTILITY

    /**
     * Returns the action result as either as is (singular/left) or a list (plural/right), based on
     * whether the action return type has collection semantics.
     */
    public Either<ManagedObject, List<ManagedObject>> asEitherSingularOrPlural() {
        return getReturnTypeSpecification().isPlural()
                ? Either.right(Facets.collectionStream(getReturnTypeSpecification(), getReturnedAdapter())
                        .collect(Collectors.toList()))
                : Either.left(getReturnedAdapter());
    }

    // -- HELPER

    private final _Lazy<Can<ManagedObject>> elementAdapters = _Lazy.threadSafe(this::initElementAdapters);
    private Can<ManagedObject> initElementAdapters() {
        return Facets.collectionStream(returnedAdapter).collect(Can.toCan());
    }

    //TODO[2449] need to check whether that strategy holds consistently
    private static boolean isScalarValue(final @NonNull ObjectSpecification spec) {
        return spec.isValue();
    }

    private static boolean isVector(final @NonNull ObjectSpecification spec) {
        return Facets.collectionIsPresent(spec);
    }

}
