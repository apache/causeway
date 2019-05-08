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

package org.apache.isis.core.metamodel.facets.actions.validate.method;

import java.lang.reflect.Method;

import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.MethodPrefixConstants;
import org.apache.isis.core.metamodel.facets.actions.validate.ActionValidationFacet;
import org.apache.isis.core.metamodel.methodutils.MethodScope;

/**
 * Sets up {@link ActionValidationFacet}.
 */
public class ActionValidationFacetViaMethodFactory extends MethodPrefixBasedFacetFactoryAbstract  {

    private static final String[] PREFIXES = { MethodPrefixConstants.VALIDATE_PREFIX };

    /**
     * Note that the {@link Facet}s registered are the generic ones from
     * noa-architecture (where they exist)
     */
    public ActionValidationFacetViaMethodFactory() {
        super(FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    // ///////////////////////////////////////////////////////
    // Actions
    // ///////////////////////////////////////////////////////

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        handleValidateAllArgsMethod(processMethodContext);
    }

    private void handleValidateAllArgsMethod(final ProcessMethodContext processMethodContext) {

        final Class<?> cls = processMethodContext.getCls();
        final Method actionMethod = processMethodContext.getMethod();
        final IdentifiedHolder facetHolder = processMethodContext.getFacetHolder();

        final String capitalizedName = StringExtensions.asCapitalizedName(actionMethod.getName());
        final Class<?>[] paramTypes = actionMethod.getParameterTypes();
        final MethodScope onClass = MethodScope.scopeFor(actionMethod);

        final Method validateMethod = MethodFinderUtils.findMethod(
                cls, onClass,
                MethodPrefixConstants.VALIDATE_PREFIX + capitalizedName,
                new Class<?>[]{String.class, TranslatableString.class},
                paramTypes);
        if (validateMethod == null) {
            return;
        }
        processMethodContext.removeMethod(validateMethod);

        final TranslationService translationService = getTranslationService();
        // sadness: same as in TranslationFactory
        final String translationContext = facetHolder.getIdentifier().toClassAndNameIdentityString();
        final ActionValidationFacetViaMethod facet = new ActionValidationFacetViaMethod(validateMethod, translationService, translationContext, facetHolder);
        FacetUtil.addFacet(facet);
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {

        final Class<?> cls = processParameterContext.getCls();
        final Method actionMethod = processParameterContext.getMethod();
        final int param = processParameterContext.getParamNum();
        final IdentifiedHolder facetHolder = processParameterContext.getFacetHolder();

        final String capitalizedName = StringExtensions.asCapitalizedName(actionMethod.getName());
        final Class<?>[] paramTypes = actionMethod.getParameterTypes();
        final MethodScope onClass = MethodScope.scopeFor(actionMethod);

        final String validateName = MethodPrefixConstants.VALIDATE_PREFIX + param + capitalizedName;
        final Method validateMethod = MethodFinderUtils.findMethod(
                cls, onClass,
                validateName,
                new Class<?>[]{String.class, TranslatableString.class},
                new Class<?>[]{paramTypes[param]});
        if (validateMethod == null) {
            return;
        }

        processParameterContext.removeMethod(validateMethod);

        final TranslationService translationService = getTranslationService();
        // sadness: same as in TranslationFactory
        final String translationContext = facetHolder.getIdentifier().toFullIdentityString();
        final Facet facet = new ActionParameterValidationFacetViaMethod(validateMethod, translationService, translationContext, facetHolder);
        FacetUtil.addFacet(facet);
    }


}
