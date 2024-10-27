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
package org.apache.causeway.core.metamodel.facets.members.disabled.method;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.causeway.applib.services.i18n.TranslatableString;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.metamodel.consent.Consent.VetoReason;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.interactions.UsabilityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmInvokeUtils;

import lombok.Getter;
import lombok.NonNull;

public class DisableForContextFacetViaMethod
extends DisableForContextFacetAbstract
implements ImperativeFacet {

    @Getter(onMethod_ = {@Override}) private final @NonNull Can<MethodFacade> methods;
    private final TranslationContext translationContext;

    public DisableForContextFacetViaMethod(
            final ResolvedMethod method,
            final FacetHolder holder) {
        super(holder);
        this.methods = ImperativeFacet.singleRegularMethod(method);
        this.translationContext = holder.getTranslationContext();
    }

    @Override
    public Intent getIntent() {
        return Intent.CHECK_IF_DISABLED;
    }

    /**
     * The reason this object is disabled, or <tt>null</tt> otherwise.
     */
    @Override
    public Optional<VetoReason> disables(final UsabilityContext ic) {
        final ManagedObject target = ic.getTarget();
        if (target == null) {
            return Optional.empty();
        }
        var method = methods.getFirstElseFail().asMethodElseFail(); // expected regular
        final Object returnValue = MmInvokeUtils.invokeAutofit(method.method(), target);
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
