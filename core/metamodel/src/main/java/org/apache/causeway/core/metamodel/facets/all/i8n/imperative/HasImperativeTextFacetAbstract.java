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
package org.apache.causeway.core.metamodel.facets.all.i8n.imperative;

import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;

import lombok.Getter;
import lombok.NonNull;

public class HasImperativeTextFacetAbstract
extends FacetAbstract
implements
    ImperativeFacet,
    HasImperativeText {

    protected final TranslationContext translationContext;

    @Getter(onMethod_ = {@Override}) private final @NonNull Can<MethodFacade> methods;

    protected HasImperativeTextFacetAbstract(
            final Class<? extends Facet> facetType,
            final TranslationContext translationContext,
            final ResolvedMethod method,
            final FacetHolder holder) {
        // imperative takes precedence over any other (except for events)
        super(facetType, holder, Precedence.IMPERATIVE);
        this.methods = ImperativeFacet.singleRegularMethod(method);
        this.translationContext = translationContext;
    }

    @Override
    public final Try<String> text(final ManagedObject object) {
        var method = methods.getFirstElseFail().asMethodElseFail(); // expected regular
        return ManagedObjects.imperativeText(object, method, translationContext);
    }

    @Override
    public final Intent getIntent() {
        return Intent.UI_HINT;
    }

    @Override
    public final void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("context", translationContext);
        ImperativeFacet.visitAttributes(this, visitor);
    }

    @Override
    public final boolean semanticEquals(final @NonNull Facet other) {

        // equality by facet-type, java-method and translation-context

        if(!this.facetType().equals(other.facetType())) {
            return false;
        }

        var otherFacet = (HasImperativeTextFacetAbstract)other;

        return Objects.equals(this.methods, otherFacet.methods)
                && Objects.equals(this.translationContext, otherFacet.translationContext);

    }

}
