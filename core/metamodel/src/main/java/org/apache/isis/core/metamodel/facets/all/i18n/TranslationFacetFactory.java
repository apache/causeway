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
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;

public class TranslationFacetFactory
extends FacetFactoryAbstract {

    public TranslationFacetFactory() {
        super(FeatureType.EVERYTHING);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final FacetHolder facetHolder = processClassContext.getFacetHolder();
        if(facetHolder instanceof IdentifiedHolder) {
            final IdentifiedHolder holder = (IdentifiedHolder) facetHolder;
            final TranslationContext context = TranslationContext.ofIdentifier(holder.getIdentifier()); // .getClassName();
            translateName(holder, context);
            translateDescription(holder, context);
        }
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final IdentifiedHolder holder = processMethodContext.getFacetHolder();

        final TranslationContext context = TranslationContext.ofIdentifier(holder.getIdentifier()); // .getTranslationContext();
        translateName(holder, context);
        translateDescription(holder, context);
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        final IdentifiedHolder holder = processParameterContext.getFacetHolder();

        final TranslationContext context = TranslationContext.ofIdentifierFullIdentity(holder.getIdentifier()); // .getFullIdentityString();
        translateName(holder, context);
        translateDescription(holder, context);
    }

    // //////////////////////////////////////

    void translateName(final IdentifiedHolder facetHolder, final TranslationContext context) {
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

        final TranslationService translationService = getTranslationService();
        NamedFacetTranslated facetTranslated = new NamedFacetTranslated(context, originalText, translationService, facetHolder);
        facetTranslated.setUnderlyingFacet(facet);
        super.addFacet(facetTranslated);
    }

    void translateDescription(final FacetHolder facetHolder, final TranslationContext context) {

        final IdentifiedHolder holder = (IdentifiedHolder) facetHolder;
        final DescribedAsFacet facet = facetHolder.getFacet(DescribedAsFacet.class);
        if(facet == null) {
            return;
        }
        final String originalText = facet.value();
        if (isNullOrEmptyWhenTrimmed(originalText)) {
            return;
        }

        final TranslationService translationService = getTranslationService();
        super.addFacet(new DescribedAsFacetTranslated(context, originalText, translationService, holder));

    }

    private boolean isNullOrEmptyWhenTrimmed(final String originalText) {
        return originalText == null || _Strings.isNullOrEmpty(originalText.trim());
    }



}
