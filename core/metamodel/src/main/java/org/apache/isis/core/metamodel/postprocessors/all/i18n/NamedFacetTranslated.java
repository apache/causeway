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

package org.apache.isis.core.metamodel.postprocessors.all.i18n;

import java.util.function.BiConsumer;

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;

import lombok.Getter;
import lombok.NonNull;

public class NamedFacetTranslated
extends FacetAbstract
implements NamedFacet {

    final TranslationService translationService;
    final TranslationContext context;
    final String originalText;

    @Getter private final @NonNull NamedFacet underlyingNamedFacet;

    public NamedFacetTranslated(
            final NamedFacet underlyingNamedFacet,
            final TranslationContext context,
            final String originalText,
            final TranslationService translationService,
            final IdentifiedHolder facetHolder) {
        super(
                NamedFacet.type(),
                facetHolder,
                underlyingNamedFacet.getPrecedence()); // keep original precedence

        this.context = context;
        this.originalText = originalText;
        this.translationService = translationService;
        this.underlyingNamedFacet = underlyingNamedFacet;

        if(translationService!=null
                && translationService.getMode().isWrite()) {
            // force PoWriter to be called to capture this text that needs translating
            translateText();
        }
    }

    @Override
    public String value() {
        return translateText();
    }

    private String translateText() {
        return translationService!=null
                ? translationService.translate(context, originalText)
                : originalText;
    }

    @Override
    public boolean escaped() {
        return getUnderlyingNamedFacet().escaped();
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("context", context);
        visitor.accept("originalText", originalText);
        visitor.accept("underlyingNamedFacet", underlyingNamedFacet.getClass().getName());
    }
}
