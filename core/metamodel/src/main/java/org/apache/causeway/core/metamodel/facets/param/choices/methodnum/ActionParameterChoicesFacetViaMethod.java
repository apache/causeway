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
package org.apache.causeway.core.metamodel.facets.param.choices.methodnum;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedConstructor;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedType;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.commons.semantics.CollectionSemantics;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.facets.param.choices.ActionParameterChoicesFacetAbstract;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmInvokeUtils;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;
import lombok.NonNull;

public class ActionParameterChoicesFacetViaMethod
extends ActionParameterChoicesFacetAbstract
implements ImperativeFacet {

    @Getter(onMethod_ = {@Override}) private final @NonNull Can<MethodFacade> methods;
    private final ResolvedType paramSupportReturnType;
    private final Optional<ResolvedConstructor> patConstructor;

    public ActionParameterChoicesFacetViaMethod(
            final ResolvedMethod method,
            final ResolvedType paramSupportReturnType,
            final Optional<ResolvedConstructor> patConstructor,
            final FacetHolder holder) {

        super(holder);
        this.methods = ImperativeFacet.singleMethod(method, patConstructor);
        this.paramSupportReturnType = paramSupportReturnType;
        this.patConstructor = patConstructor;
    }

    @Override
    public Intent getIntent() {
        return Intent.CHOICES_OR_AUTOCOMPLETE;
    }

    @Override
    public Can<ManagedObject> getChoices(
            final ObjectSpecification requiredSpec,
            final ActionInteractionHead head,
            final Can<ManagedObject> pendingArgs,
            final InteractionInitiatedBy interactionInitiatedBy) {

        var method = methods.getFirstElseFail();
        final Object collectionOrArray = MmInvokeUtils.invokeAutofit(patConstructor, method, head.getTarget(), pendingArgs);
        if (collectionOrArray == null) {
            return Can.empty();
        }

        var visibleChoices = ManagedObjects
                .adaptMultipleOfTypeThenFilterByVisibility(
                        requiredSpec, collectionOrArray, interactionInitiatedBy);

        return visibleChoices;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        ImperativeFacet.visitAttributes(this, visitor);
        visitor.accept("choicesType", paramSupportReturnType.collectionSemantics()
                .map(CollectionSemantics::name)
                .orElse("NONE"));
    }

}
