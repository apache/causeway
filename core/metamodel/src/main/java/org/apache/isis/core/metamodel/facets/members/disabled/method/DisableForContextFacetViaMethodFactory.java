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

package org.apache.isis.core.metamodel.facets.members.disabled.method;

import java.lang.reflect.Method;

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.MethodFinder2;
import org.apache.isis.core.metamodel.facets.MethodLiteralConstants;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;

import lombok.val;

public class DisableForContextFacetViaMethodFactory 
extends MethodPrefixBasedFacetFactoryAbstract  {

    private static final String PREFIX = MethodLiteralConstants.DISABLE_PREFIX;

    public DisableForContextFacetViaMethodFactory() {
        super(FeatureType.MEMBERS, OrphanValidation.VALIDATE, Can.ofSingleton(PREFIX));
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        attachDisabledFacetIfDisabledMethodIsFound(processMethodContext);
    }

    private void attachDisabledFacetIfDisabledMethodIsFound(final ProcessMethodContext processMethodContext) {

        val actionOrGetter = processMethodContext.getMethod();

        val cls = processMethodContext.getCls();

        Method disableMethod = null;
        
        val namingConvention = getNamingConventionForPropertyAndCollectionSupport(processMethodContext, PREFIX);

        boolean noParamsOnly = getConfiguration().getCore().getMetaModel().getValidator().isNoParamsOnly();
        boolean searchExactMatch = !noParamsOnly;
        if(searchExactMatch) {
            // search for exact match
            disableMethod = MethodFinder2.findMethod_returningText(
                    cls,
                    namingConvention,
                    actionOrGetter.getParameterTypes())
                    .findFirst()
                    .orElse(null);
        }
        if (disableMethod == null) {
            // search for no-arg version
            disableMethod = MethodFinder2.findMethod_returningText(
                    cls,
                    namingConvention,
                    NO_ARG)
                    .findFirst()
                    .orElse(null);
        }
        if (disableMethod == null) {
            return;
        }

        processMethodContext.removeMethod(disableMethod);

        val facetHolder = processMethodContext.getFacetHolder();
        val translationService = getTranslationService();
        // sadness: same logic as in I18nFacetFactory
        val translationContext = TranslationContext
                .forTranslationContextHolder(((IdentifiedHolder)facetHolder).getIdentifier());
        super.addFacet(new DisableForContextFacetViaMethod(disableMethod, translationService, translationContext, facetHolder));
    }

}
