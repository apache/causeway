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

package org.apache.isis.core.metamodel.facets.param.choices.method;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import org.apache.isis.applib.exceptions.unrecoverable.DomainModelException;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.collections.CanVector;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionChoicesFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class ActionChoicesFacetViaMethod
extends ActionChoicesFacetAbstract
implements ImperativeFacet {

    @Getter(onMethod_ = {@Override}) private final @NonNull Can<Method> methods;
    private final Class<?> choicesType;

    public ActionChoicesFacetViaMethod(
            final Method method,
            final Class<?> choicesType,
            final FacetHolder holder) {

        super(holder);
        this.methods = Can.ofSingleton(method);
        this.choicesType = choicesType;
    }

    @Override
    public Intent getIntent(final Method method) {
        return Intent.CHOICES_OR_AUTOCOMPLETE;
    }

    @Override
    public CanVector<ManagedObject> getChoices(
            final ManagedObject owningAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val method = methods.getFirstOrFail();
        final Object objectOrCollection = ManagedObjects.InvokeUtil.invoke(method, owningAdapter);
        if (!(objectOrCollection instanceof Object[])) {
            throw new DomainModelException(String.format(
                    "Expected an array of collections (Object[]) containing choices for all parameters, "
                            + "but got %s instead. Perhaps the parameter number is missing?",
                            objectOrCollection));
        }
        final Object[] options = (Object[]) objectOrCollection;

        val choicesVector = new CanVector<ManagedObject>(options.length);
        val parameterTypes = method.getParameterTypes();

        for (int i = 0; i < choicesVector.size(); i++) {
            choicesVector.set(i, handleResults(options[i], parameterTypes[i], interactionInitiatedBy));
        }
        return choicesVector;
    }

    private Can<ManagedObject> handleResults(
            final Object collectionOrArray,
            final Class<?> parameterType,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val elementSpec = specForTypeElseFail(parameterType);
        val visibleChoices = ManagedObjects
                .adaptMultipleOfTypeThenAttachThenFilterByVisibility(
                        elementSpec, collectionOrArray, interactionInitiatedBy);
        return visibleChoices;
    }

    @Override
    protected String toStringValues() {
        val method = methods.getFirstOrFail();
        return "method=" + method + ",type=" + choicesType;
    }

    @Override public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        ImperativeFacet.Util.appendAttributesTo(this, visitor);
        visitor.accept("choicesType", choicesType);
    }
}
