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

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.MethodLiteralConstants;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.actions.validate.ActionValidationFacet;

import lombok.val;

/**
 * Sets up {@link ActionValidationFacet} and {@link ActionParameterValidationFacetViaMethod}.
 */
public class ActionValidationFacetViaMethodFactory 
extends MethodPrefixBasedFacetFactoryAbstract  {
    
    private static final String PREFIX = MethodLiteralConstants.VALIDATE_PREFIX;

    public ActionValidationFacetViaMethodFactory() {
        super(FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE, Can.ofSingleton(PREFIX));
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        handleValidateAllArgsMethod(processMethodContext);
    }

    private void handleValidateAllArgsMethod(final ProcessMethodContext processMethodContext) {

        val cls = processMethodContext.getCls();
        val actionMethod = processMethodContext.getMethod();
        val facetHolder = processMethodContext.getFacetHolder();

        val paramTypes = actionMethod.getParameterTypes();

        val namingConvention = PREFIX_BASED_NAMING.providerForAction(actionMethod, PREFIX);
        
        final Method validateMethod = MethodFinderUtils.findMethod_returningText(
                cls,
                namingConvention.get(),
                paramTypes);
        if (validateMethod == null) {
            return;
        }
        processMethodContext.removeMethod(validateMethod);

        final TranslationService translationService = getTranslationService();
        // sadness: same as in TranslationFactory
        final String translationContext = facetHolder.getIdentifier().toClassAndNameIdentityString();
        final ActionValidationFacetViaMethod facet = 
                new ActionValidationFacetViaMethod(validateMethod, translationService, translationContext, facetHolder);
        super.addFacet(facet);
    }



}
