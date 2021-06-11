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
import org.apache.isis.core.metamodel.commons.StringExtensions;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.plural.PluralFacet;

public class PluralFacetTranslated
extends FacetAbstract
implements PluralFacet {

    private final TranslationService translationService;
    private TranslationContext context;
    private String originalText;

    public PluralFacetTranslated(final NamedFacetTranslated facet, final FacetHolder facetHolder) {
        super(PluralFacet.class, facetHolder, Precedence.INFERRED);
        this.translationService = facet.translationService;
        this.context = facet.context;
        this.originalText = facet.originalText;
    }

    @Override
    public String value() {
        final String singularName = translationService.translate(context, originalText);
        // TODO: sure this could be improved somehow using the other overload of translationService#translate(...)
        return StringExtensions.asPluralName(singularName);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("context", context);
        visitor.accept("originalText", originalText);
    }
}
