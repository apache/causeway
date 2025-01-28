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
package org.apache.causeway.core.metamodel.facets.properties.validating.method;

import java.util.function.BiConsumer;

import org.apache.causeway.applib.services.i18n.TranslatableString;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.facets.properties.validating.PropertyValidateFacetAbstract;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmInvokeUtils;

import lombok.Getter;
import org.jspecify.annotations.NonNull;

public class PropertyValidateFacetViaMethod extends PropertyValidateFacetAbstract implements ImperativeFacet {

    @Getter(onMethod_ = {@Override}) private final @NonNull Can<MethodFacade> methods;
    private final TranslationContext translationContext;

    public PropertyValidateFacetViaMethod(
            final ResolvedMethod method,
    		final FacetHolder holder) {
        super(holder);
        this.methods = ImperativeFacet.singleRegularMethod(method);
        this.translationContext = holder.getTranslationContext();
    }

    @Override
    public Intent getIntent() {
        return Intent.CHECK_IF_VALID;
    }

    @Override
    public String invalidReason(final ManagedObject owningAdapter, final ManagedObject proposedAdapter) {
        var method = methods.getFirstElseFail().asMethodElseFail(); // expected regular
        final Object returnValue = MmInvokeUtils.invokeWithSingleArg(method.method(), owningAdapter, proposedAdapter);
        if(returnValue instanceof String) {
            return (String) returnValue;
        }
        if(returnValue instanceof TranslatableString) {
            final TranslatableString ts = (TranslatableString) returnValue;
            return ts.translate(getTranslationService(), translationContext);
        }
        return null;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        ImperativeFacet.visitAttributes(this, visitor);
    }

}
