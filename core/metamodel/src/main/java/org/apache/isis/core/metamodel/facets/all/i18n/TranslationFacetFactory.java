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
package org.apache.isis.core.metamodel.facets.all.i18n;


import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;

import lombok.val;

public class TranslationFacetFactory
extends FacetFactoryAbstract {

    public TranslationFacetFactory() {
        super(FeatureType.EVERYTHING);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        val facetHolder = processClassContext.getFacetHolder();
        if(facetHolder instanceof IdentifiedHolder) {
            val identifiedHolder = (IdentifiedHolder) facetHolder;
            val translationContext = TranslationContext.forTranslationContextHolder(identifiedHolder.getIdentifier());
            translateName(identifiedHolder, translationContext);
            translateDescription(identifiedHolder, translationContext);
        }
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        val identifiedHolder = processMethodContext.getFacetHolder();

        val translationContext = TranslationContext.forTranslationContextHolder(identifiedHolder.getIdentifier());
        translateName(identifiedHolder, translationContext);
        translateDescription(identifiedHolder, translationContext);
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        val identifiedHolder = processParameterContext.getFacetHolder();

        val translationContext = TranslationContext.forTranslationContextHolder(identifiedHolder.getIdentifier());
        translateName(identifiedHolder, translationContext);
        translateDescription(identifiedHolder, translationContext);
    }

    // -- HELPER

    void translateName(final IdentifiedHolder facetHolder, final TranslationContext translationContext) {
        final NamedFacet facet = facetHolder.getFacet(NamedFacet.class);
        if(facet == null) {
            // not expected...
            return;
        }
        final String originalText = facet.value();
        if (isNullOrEmptyWhenTrimmed(originalText)) {
            // not expected...
            return;
        }

        val translationService = getTranslationService();
        val namedFacetTranslated 
            = new NamedFacetTranslated(translationContext, originalText, translationService, facetHolder);
        namedFacetTranslated.setUnderlyingFacet(facet);
        super.addFacet(namedFacetTranslated);
    }

    void translateDescription(final FacetHolder facetHolder, final TranslationContext translationContext) {

        val identifiedHolder = (IdentifiedHolder) facetHolder;
        val describedAsFacet = facetHolder.getFacet(DescribedAsFacet.class);
        if(describedAsFacet == null) {
            return;
        }
        final String originalText = describedAsFacet.value();
        if (isNullOrEmptyWhenTrimmed(originalText)) {
            return;
        }

        val translationService = getTranslationService();
        super.addFacet(new DescribedAsFacetTranslated(
                translationContext, originalText, translationService, identifiedHolder));

    }

    private boolean isNullOrEmptyWhenTrimmed(final String originalText) {
        return originalText == null || _Strings.isNullOrEmpty(originalText.trim());
    }



}
