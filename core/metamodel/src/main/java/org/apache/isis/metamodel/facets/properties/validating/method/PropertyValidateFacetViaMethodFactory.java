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

package org.apache.isis.metamodel.facets.properties.validating.method;

import java.lang.reflect.Method;

import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.metamodel.facets.MethodFinderUtils;
import org.apache.isis.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.metamodel.facets.MethodPrefixConstants;
import org.apache.isis.metamodel.methodutils.MethodScope;

public class PropertyValidateFacetViaMethodFactory extends MethodPrefixBasedFacetFactoryAbstract  {

    private static final String[] PREFIXES = { MethodPrefixConstants.VALIDATE_PREFIX };

    public PropertyValidateFacetViaMethodFactory() {
        super(FeatureType.PROPERTIES_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        attachValidateFacetIfValidateMethodIsFound(processMethodContext);
    }

    private void attachValidateFacetIfValidateMethodIsFound(final ProcessMethodContext processMethodContext) {

        final Method getMethod = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asJavaBaseName(getMethod.getName());

        final Class<?> returnType = getMethod.getReturnType();
        final Class<?>[] paramTypes = new Class[] { returnType };

        final Class<?> cls = processMethodContext.getCls();
        final Method method = MethodFinderUtils.findMethod(
                cls, MethodScope.OBJECT,
                MethodPrefixConstants.VALIDATE_PREFIX + capitalizedName,
                new Class<?>[]{String.class, TranslatableString.class},
                paramTypes);
        if (method == null) {
            return;
        }
        processMethodContext.removeMethod(method);

        final FacetedMethod facetHolder = processMethodContext.getFacetHolder();

        final TranslationService translationService = getTranslationService();
        // sadness: same as in TranslationFactory
        final String translationContext = facetHolder.getIdentifier().toClassAndNameIdentityString();
        final PropertyValidateFacetViaMethod facet = new PropertyValidateFacetViaMethod(method, translationService, translationContext, facetHolder);
        FacetUtil.addFacet(facet);
    }


}
