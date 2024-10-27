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
package org.apache.causeway.core.metamodel.facets.object.disabled.method;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.services.i18n.TranslatableString;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.consent.Consent.VetoReason;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.HasImperativeAspect;
import org.apache.causeway.core.metamodel.facets.ImperativeAspect;
import org.apache.causeway.core.metamodel.facets.object.disabled.DisabledObjectFacet;
import org.apache.causeway.core.metamodel.facets.object.disabled.DisabledObjectFacetAbstract;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.Getter;
import lombok.NonNull;

public class DisabledObjectFacetViaMethod
extends DisabledObjectFacetAbstract
implements HasImperativeAspect {

    @Getter(onMethod_ = {@Override}) private final @NonNull ImperativeAspect imperativeAspect;

    private final TranslationContext translationContext;

    public static Optional<DisabledObjectFacet> create(
            final @Nullable ResolvedMethod methodIfAny,
            final FacetHolder holder) {

        return Optional.ofNullable(methodIfAny)
        .map(method->
            new DisabledObjectFacetViaMethod(
                    ImperativeAspect.singleRegularMethod(method, Intent.CHECK_IF_DISABLED),
                    TranslationContext.forMethod(method),
                    holder));
    }

    public DisabledObjectFacetViaMethod(
            final ImperativeAspect imperativeAspect,
            final TranslationContext translationContext,
            final FacetHolder holder) {
        super(holder);
        this.imperativeAspect = imperativeAspect;
        this.translationContext = translationContext;
    }

    @Override
    public Optional<VetoReason> disabledReason(final ManagedObject domainObject) {
        var returnValue = imperativeAspect.eval(domainObject, null);

        final String reasonString = returnValue instanceof String
                ? (String) returnValue
                : returnValue instanceof TranslatableString
                    ? ((TranslatableString) returnValue).translate(getTranslationService(), translationContext)
                    : null;

        return Optional.ofNullable(reasonString)
            .map(VetoReason::explicit);
    }

    @Override
    public DisabledObjectFacetViaMethod clone(final FacetHolder holder) {
        return new DisabledObjectFacetViaMethod(imperativeAspect, translationContext, holder);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        imperativeAspect.visitAttributes(visitor);
    }

}
