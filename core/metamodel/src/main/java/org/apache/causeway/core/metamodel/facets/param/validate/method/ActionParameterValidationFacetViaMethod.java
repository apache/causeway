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
package org.apache.causeway.core.metamodel.facets.param.validate.method;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.causeway.applib.services.i18n.TranslatableString;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedConstructor;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.facets.param.validate.ActionParameterValidationFacetAbstract;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmInvokeUtils;

import lombok.Getter;
import lombok.NonNull;

public class ActionParameterValidationFacetViaMethod
extends ActionParameterValidationFacetAbstract
implements ImperativeFacet {

    @Getter(onMethod_ = {@Override}) private final @NonNull Can<MethodFacade> methods;
    private final TranslationContext translationContext;
    private final Optional<ResolvedConstructor> patConstructor;

    public ActionParameterValidationFacetViaMethod(
            final ResolvedMethod method,
            final Optional<ResolvedConstructor> patConstructor,
            final FacetHolder holder) {

        super(holder);
        this.methods = ImperativeFacet.singleMethod(method, patConstructor);
        this.translationContext = holder.getTranslationContext();
        this.patConstructor = patConstructor;
    }

    @Override
    public Intent getIntent() {
        return Intent.CHECK_IF_VALID;
    }

    //@Override
    @Override
    public String invalidReason(
            final ManagedObject owningAdapter,
            final Can<ManagedObject> pendingArgs,
            final int paramIndex) {

        var method = methods.getFirstElseFail();
        final Object returnValue = patConstructor.isPresent()
                // provides all pending args as a tuple (for validation)
                ? MmInvokeUtils.invokeWithPAT(
                        patConstructor.get(),
                        method.asMethodForIntrospection(),
                        owningAdapter, pendingArgs)
                : method.asMethodElseFail().isSingleArg()
                         // provides the a single arg, namely the param under validation
                        ? MmInvokeUtils.invokeWithSingleArg(
                                method.asMethodElseFail().method(),
                                owningAdapter,
                                pendingArgs.getElseFail(paramIndex))
                        // provides pending args up to paramIndex (for validation)
                        : MmInvokeUtils.invokeWithArgs(
                            method.asMethodElseFail().method(),
                            owningAdapter,
                            pendingArgs.subCan(0, paramIndex + 1));

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
