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
package org.apache.causeway.core.metamodel.facets.object.value;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.commons.CanonicalInvoker;
import org.apache.causeway.core.metamodel.commons.ParameterConverters;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facets.HasFacetedMethod;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedInAction;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
abstract class CompositeValueUpdater {

    private final MixedInAction mixedInAction;

    public abstract ObjectSpecification getReturnType();
    protected abstract ManagedObject map(final ManagedObject valueType);

    public Identifier getFeatureIdentifier() {
        var id = mixedInAction.getFeatureIdentifier();
        return Identifier
                .actionIdentifier(
                        id.logicalType(),
                        id.memberLogicalName(),
                        id.memberParameterClassNames());
    }

    public ManagedObject execute(
            final InteractionHead head, final Can<ManagedObject> parameters,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return map(simpleExecute(head, parameters));
    }

    // -- HELPER

    private ManagedObject simpleExecute(
            final InteractionHead head, final Can<ManagedObject> parameters) {

        var methodFacade = mixedInAction instanceof HasFacetedMethod facetedMethodHolder
            ? facetedMethodHolder.getFacetedMethod().methodFacade()
            : null;
        if(methodFacade==null) return ManagedObject.empty(mixedInAction.getReturnType()); // unsupported MixedInAction

        var method = methodFacade.asMethodForIntrospection();
        final Object[] executionParameters = MmUnwrapUtils.multipleAsArray(parameters);
        final Object targetPojo = MmUnwrapUtils.single(head.getTarget());
        var resultPojo = CanonicalInvoker
                .invokeWithConvertedArgs(method.method(), targetPojo,
                        methodFacade.getArguments(executionParameters, ParameterConverters.DEFAULT));

        return ManagedObject.value(mixedInAction.getReturnType(), resultPojo);
    }

}
