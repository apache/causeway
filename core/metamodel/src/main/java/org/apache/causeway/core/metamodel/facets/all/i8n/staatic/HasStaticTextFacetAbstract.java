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
package org.apache.causeway.core.metamodel.facets.all.i8n.staatic;

import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

import lombok.NonNull;
import lombok.val;

public abstract class HasStaticTextFacetAbstract
extends FacetAbstract
implements HasStaticText {

    protected final TranslationContext translationContext;

    private final @NonNull String originalText;
    private final @NonNull _Lazy<String> translatedText;

    protected HasStaticTextFacetAbstract(
            final Class<? extends Facet> facetType,
            final TranslationContext translationContext,
            final String originalText,
            final FacetHolder holder) {
        this(facetType, translationContext, originalText, holder, Precedence.DEFAULT);
    }

    protected HasStaticTextFacetAbstract(
            final Class<? extends Facet> facetType,
            final TranslationContext translationContext,
            final String originalText,
            final FacetHolder holder,
            final Precedence precedence) {
        super(facetType, holder, precedence);
        this.originalText = originalText;
        this.translationContext = translationContext;
        this.translatedText = _Lazy.threadSafe(()->
            holder.getTranslationService().translate(translationContext, originalText));
    }

    @Override
    public final String text() {
        return originalText;
    }

    @Override
    public final String translated() {
        return translatedText.get();
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("context", translationContext);
        visitor.accept("originalText", text());
        visitor.accept("translated", translated()); // memoizes as a side-effect
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {

        // equality by facet-type, (original) text and translation-context

        if(!this.facetType().equals(other.facetType())) {
            return false;
        }

        val otherFacet =  (HasStaticTextFacetAbstract)other;

        return Objects.equals(this.originalText, otherFacet.originalText)
                && Objects.equals(this.translationContext, otherFacet.translationContext);

    }

}
