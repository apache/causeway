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

package org.apache.isis.core.metamodel.facets.properties.validating.method;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.MethodFinder2;
import org.apache.isis.core.metamodel.facets.MethodLiteralConstants;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;

import lombok.val;

public class PropertyValidateFacetViaMethodFactory extends MethodPrefixBasedFacetFactoryAbstract  {

    private static final String PREFIX = MethodLiteralConstants.VALIDATE_PREFIX;

    public PropertyValidateFacetViaMethodFactory() {
        super(FeatureType.PROPERTIES_ONLY, OrphanValidation.VALIDATE, Can.ofSingleton(PREFIX));
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        attachValidateFacetIfValidateMethodIsFound(processMethodContext);
    }

    private void attachValidateFacetIfValidateMethodIsFound(final ProcessMethodContext processMethodContext) {

        val cls = processMethodContext.getCls();
        val getterMethod = processMethodContext.getMethod();
        
        val namingConvention = getNamingConventionForPropertyAndCollectionSupport(processMethodContext, PREFIX);
        val returnType = getterMethod.getReturnType();

        val validateMethod = MethodFinder2.findMethod_returningText(
                cls,
                namingConvention,
                new Class[] { returnType })
                .findFirst()
                .orElse(null);
        if (validateMethod == null) {
            return;
        }
        processMethodContext.removeMethod(validateMethod);

        val facetHolder = processMethodContext.getFacetHolder();
        val translationService = getTranslationService();
        // sadness: same as in TranslationFactory
        val translationContext = facetHolder.getIdentifier().getTranslationContext();
        super.addFacet(
                new PropertyValidateFacetViaMethod(
                        validateMethod, translationService, translationContext, facetHolder));
    }


}
