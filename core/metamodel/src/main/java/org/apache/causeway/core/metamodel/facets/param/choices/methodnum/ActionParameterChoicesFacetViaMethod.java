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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.CollectionSemantics;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.facets.param.choices.ActionParameterChoicesFacetAbstract;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmInvokeUtil;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.TypeOfAnyCardinality;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class ActionParameterChoicesFacetViaMethod
extends ActionParameterChoicesFacetAbstract
implements ImperativeFacet {

    @Getter(onMethod_ = {@Override}) private final @NonNull Can<Method> methods;
    private final TypeOfAnyCardinality paramSupportReturnType;
    private final Optional<Constructor<?>> patConstructor;

    public ActionParameterChoicesFacetViaMethod(
            final Method method,
            final TypeOfAnyCardinality paramSupportReturnType,
            final Optional<Constructor<?>> patConstructor,
            final FacetHolder holder) {

        super(holder);
        this.methods = ImperativeFacet.singleMethod(method);
        this.paramSupportReturnType = paramSupportReturnType;
        this.patConstructor = patConstructor;
    }

    @Override
    public Intent getIntent(final Method method) {
        return Intent.CHOICES_OR_AUTOCOMPLETE;
    }

    @Override
    public Can<ManagedObject> getChoices(
            final ObjectSpecification requiredSpec,
            final ActionInteractionHead head,
            final Can<ManagedObject> pendingArgs,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val method = methods.getFirstOrFail();
        final Object collectionOrArray = patConstructor.isPresent()
                ? MmInvokeUtil.invokeWithPAT(patConstructor.get(), method, head.getTarget(), pendingArgs)
                : MmInvokeUtil.invokeAutofit(method, head.getTarget(), pendingArgs);
        if (collectionOrArray == null) {
            return Can.empty();
        }

        val visibleChoices = ManagedObjects
                .adaptMultipleOfTypeThenFilterByVisibility(
                        requiredSpec, collectionOrArray, interactionInitiatedBy);

        return visibleChoices;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        ImperativeFacet.visitAttributes(this, visitor);
        visitor.accept("choicesType", paramSupportReturnType.getCollectionSemantics()
                .map(CollectionSemantics::name)
                .orElse("NONE"));
    }

}
