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
package org.apache.causeway.core.metamodel.facets.param.disable.method;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.causeway.applib.services.i18n.TranslatableString;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedConstructor;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.metamodel.consent.Consent.VetoReason;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.facets.param.disable.ActionParameterDisabledFacetAbstract;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmInvokeUtils;

import lombok.Getter;
import org.jspecify.annotations.NonNull;

public class ActionParameterDisabledFacetViaMethod
extends ActionParameterDisabledFacetAbstract
implements ImperativeFacet {

    @Getter(onMethod_ = {@Override}) private final @NonNull Can<MethodFacade> methods;
    private final TranslationContext translationContext;
    private final Optional<ResolvedConstructor> patConstructor;

    public ActionParameterDisabledFacetViaMethod(
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

    @Override
    public Optional<VetoReason> disabledReason(
            final ManagedObject owningAdapter,
            final Can<ManagedObject> pendingArgs) {

        var method = methods.getFirstElseFail();
        final Object returnValue = MmInvokeUtils.invokeAutofit(patConstructor, method, owningAdapter, pendingArgs);
        final String reasonString = returnValue instanceof String
                ? (String) returnValue
                : returnValue instanceof TranslatableString
                    ? ((TranslatableString) returnValue).translate(getTranslationService(), translationContext)
                    : null;

        return Optional.ofNullable(reasonString)
            .map(VetoReason::explicit);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        ImperativeFacet.visitAttributes(this, visitor);
    }

}
