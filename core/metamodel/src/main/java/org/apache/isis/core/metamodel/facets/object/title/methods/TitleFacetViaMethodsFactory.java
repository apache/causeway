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

import javax.inject.Inject;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.commons.ClassExtensions;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.fallback.FallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.methods.MethodFinderOptions;
import org.apache.isis.core.metamodel.methods.MethodFinderUtils;
import org.apache.isis.core.metamodel.methods.MethodPrefixBasedFacetFactoryAbstract;

import static org.apache.isis.core.metamodel.methods.MethodLiteralConstants.TITLE;
import static org.apache.isis.core.metamodel.methods.MethodLiteralConstants.TO_STRING;

import lombok.val;

/**
 * @implNote removes the {@link Object#toString()} method as action candidate,
 * regardless of whether this method is used for the domain-object's title or not
 */
public class TitleFacetViaMethodsFactory
extends MethodPrefixBasedFacetFactoryAbstract {

    private static final Can<String> PREFIXES = Can.ofCollection(_Lists.of(
            TO_STRING,
            TITLE));

    @Inject
    public TitleFacetViaMethodsFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    /**
     * If no title or toString can be used then will use Facets provided by
     * {@link FallbackFacetFactory} instead.
     */
    @Override
    public void process(final ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();

        val titleMethod = MethodFinderUtils.findMethod_returningText(
                MethodFinderOptions
                .layoutSupport(processClassContext.getMemberIntrospectionPolicy()),
                cls,
                TITLE,
                NO_ARG);
        if (titleMethod != null) {
            processClassContext.removeMethod(titleMethod);
            // sadness: same as in TranslationFactory
            val translationContext = TranslationContext.forMethod(titleMethod);

            FacetUtil.addFacet(
                    new TitleFacetViaTitleMethod(
                            titleMethod, translationContext, facetHolder));

            removeToString(processClassContext);
            return;
        }

        // may have a facet by virtue of @Title, say.
        val existingTitleFacet = facetHolder.lookupNonFallbackFacet(TitleFacet.class);
        if(existingTitleFacet.isPresent()) {
            removeToString(processClassContext);
            return;
        }

        try {
            val toStringMethod = removeToString(processClassContext);
            if (toStringMethod != null) {
                if(!ClassExtensions.isJavaClass(toStringMethod.getDeclaringClass())) {
                    FacetUtil.addFacet(new TitleFacetInferredFromToStringMethod(toStringMethod, facetHolder));
                }
            }
        } catch (final Exception e) {
            // ignore
        }

    }

    // -- HELPER

    private @Nullable Method removeToString(final ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();
        val toStringMethod = MethodFinderUtils.findMethod(
                MethodFinderOptions.publicOnly(),
                cls, TO_STRING, String.class, null);
        if (toStringMethod != null) {
            processClassContext.removeMethod(toStringMethod);
        }
        return toStringMethod;
    }


}
