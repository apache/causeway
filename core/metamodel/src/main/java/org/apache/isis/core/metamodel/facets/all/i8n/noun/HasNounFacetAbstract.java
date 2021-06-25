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
package org.apache.isis.core.metamodel.facets.all.i8n.noun;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

import lombok.NonNull;
import lombok.val;

public abstract class HasNounFacetAbstract
extends FacetAbstract
implements HasNoun {

    protected final TranslationContext translationContext;

    private final @NonNull NounForms nounForms;
    private final @NonNull _Lazy<NounForms> translatedNounForms;

    protected HasNounFacetAbstract(
            final Class<? extends Facet> facetType,
            final TranslationContext translationContext,
            final NounForms nounForms,
            final FacetHolder holder) {
        this(facetType, translationContext, nounForms, holder, Precedence.DEFAULT);
    }

    protected HasNounFacetAbstract(
            final Class<? extends Facet> facetType,
            final TranslationContext translationContext,
            final NounForms nounForms,
            final FacetHolder holder,
            final Precedence precedence) {
        super(facetType, holder, precedence);
        this.nounForms = nounForms;
        this.translationContext = translationContext;
        this.translatedNounForms = _Lazy.threadSafe(()->
            nounForms.translate(holder.getTranslationService(), translationContext));
    }

    @Override
    public final String preferredText() {
        return text(nounForms.getPreferredNounForm());
    }

    @Override
    public final String preferredTranslated() {
        return translated(nounForms.getPreferredNounForm());
    }

    @Override
    public final String text(final @NonNull NounForm nounForm) {
        return nounForms.get(nounForm);
    }

    @Override
    public final String translated(final NounForm nounForm) {
        return translatedNounForms.get().get(nounForm);
    }

    @Override
    public ImmutableEnumSet<NounForm> getSupportedNounForms() {
        return nounForms.getSupportedNounForms();
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("context", translationContext);
        visitor.accept("nounForms",
                getSupportedNounForms()
                .stream()
                .map(NounForm::name)
                .collect(Collectors.joining(", ")));

        getSupportedNounForms()
        .forEach(nounForm->{
            visitor.accept("originalText." + nounForm, text(nounForm));
            visitor.accept("translated." + nounForm, translated(nounForm)); // memoizes as a side-effect
        });
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {

        // equality by facet-type, (original) text and translation-context

        if(!this.facetType().equals(other.facetType())) {
            return false;
        }

        val otherFacet =  (HasNounFacetAbstract)other;

        return Objects.equals(this.nounForms, otherFacet.nounForms)
                && Objects.equals(this.translationContext, otherFacet.translationContext);

    }

}
