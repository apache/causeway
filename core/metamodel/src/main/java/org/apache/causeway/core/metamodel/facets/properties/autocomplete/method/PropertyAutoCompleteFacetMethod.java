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
package org.apache.causeway.core.metamodel.facets.properties.autocomplete.method;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.facets.param.autocomplete.MinLengthUtil;
import org.apache.causeway.core.metamodel.facets.properties.autocomplete.PropertyAutoCompleteFacetAbstract;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmInvokeUtil;
import org.apache.causeway.core.metamodel.object.MmVisibilityUtil;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class PropertyAutoCompleteFacetMethod
extends PropertyAutoCompleteFacetAbstract
implements ImperativeFacet {

    @Getter(onMethod_ = {@Override}) private final @NonNull Can<Method> methods;
    private final Class<?> choicesClass;
    private final int minLength;

    public PropertyAutoCompleteFacetMethod(
            final Method method, // member support method
            final Class<?> choicesClass, // return type of the getter, which is subject to be supported by this facet
            final FacetHolder holder) {
        super(holder);
        this.methods = ImperativeFacet.singleMethod(method);
        this.choicesClass = choicesClass;
        this.minLength = MinLengthUtil.determineMinLength(method);
    }

    @Override
    public Intent getIntent(final Method method) {
        return Intent.CHOICES_OR_AUTOCOMPLETE;
    }

    @Override
    public int getMinLength() {
        return minLength;
    }

    @Override
    public Object[] autoComplete(
            final ObjectFeature objectFeature,
            final ManagedObject owningAdapter,
            final String searchArg,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val method = methods.getFirstElseFail();
        final Object collectionOrArray = MmInvokeUtil.invoke(method, owningAdapter, searchArg);
        if (collectionOrArray == null) {
            return null;
        }

        val collectionAdapter = getObjectManager().adapt(collectionOrArray, Optional.of(objectFeature));

        val visiblePojos = MmVisibilityUtil
                .visiblePojosAsArray(collectionAdapter, interactionInitiatedBy);

        return visiblePojos;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        ImperativeFacet.visitAttributes(this, visitor);
        visitor.accept("choicesType", choicesClass);
        visitor.accept("minLength", minLength);
    }

}
