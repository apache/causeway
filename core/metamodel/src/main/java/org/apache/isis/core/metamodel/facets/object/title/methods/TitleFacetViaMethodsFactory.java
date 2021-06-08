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

package org.apache.isis.core.metamodel.facets.object.title.methods;

import java.lang.reflect.Method;

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.commons.ClassExtensions;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.fallback.FallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.methods.MethodFinderUtils;
import org.apache.isis.core.metamodel.methods.MethodPrefixBasedFacetFactoryAbstract;

import static org.apache.isis.core.metamodel.methods.MethodLiteralConstants.TITLE;
import static org.apache.isis.core.metamodel.methods.MethodLiteralConstants.TO_STRING;

import lombok.val;

public class TitleFacetViaMethodsFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final Can<String> PREFIXES = Can.ofCollection(_Lists.of(
            TO_STRING,
            TITLE));

    public TitleFacetViaMethodsFactory() {
        super(FeatureType.OBJECTS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    /**
     * If no title or toString can be used then will use Facets provided by
     * {@link FallbackFacetFactory} instead.
     */
    @Override
    public void process(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        Method method = MethodFinderUtils.findMethod_returningText(
                cls,
                TITLE,
                NO_ARG);
        if (method != null) {
            processClassContext.removeMethod(method);
            val translationService = getTranslationService();
            // sadness: same as in TranslationFactory
            val translationContext = TranslationContext.forMethod(method);

            val titleFacet = new TitleFacetViaTitleMethod(method, translationService, translationContext, facetHolder);
            FacetUtil.addFacet(titleFacet);
            return;
        }

        // may have a facet by virtue of @Title, say.
        final TitleFacet existingTitleFacet = facetHolder.lookupNonFallbackFacet(TitleFacet.class)
                .orElse(null);
        if(existingTitleFacet != null) {
            return;
        }

        try {
            method = MethodFinderUtils.findMethod(cls, TO_STRING, String.class, null);
            if (method == null) {
                return;
            }
            if (ClassExtensions.isJavaClass(method.getDeclaringClass())) {
                return;
            }
            processClassContext.removeMethod(method);
            FacetUtil.addFacet(new TitleFacetViaToStringMethod(method, facetHolder));

        } catch (final Exception e) {
            return;
        }
    }

}
