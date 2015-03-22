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

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;

public class NamedFacetTranslated extends FacetAbstract implements NamedFacet {

    final TranslationService translationService;
    String context;
    String originalText;

    public NamedFacetTranslated(
            final String context, final String originalText,
            final TranslationService translationService,
            final IdentifiedHolder facetHolder) {
        super(NamedFacet.class, facetHolder, Derivation.NOT_DERIVED);
        this.context = context;
        this.originalText = originalText;
        this.translationService = translationService;

        if(translationService.getMode().isWrite()) {
            // force PoWriter to be called to capture this text that needs translating
            translateText();
        }
    }

    @Override
    public String value() {
        return translateText();
    }

    private String translateText() {
        return translationService.translate(context, originalText);
    }

    @Override
    public boolean escaped() {
        final NamedFacet underlyingFacet = (NamedFacet) getUnderlyingFacet();
        return underlyingFacet != null && underlyingFacet.escaped();
    }
}
