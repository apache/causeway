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

package org.apache.isis.metamodel.facets.object.validating.validateobject.method;

import java.lang.reflect.Method;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.metamodel.facets.MethodFinderUtils;
import org.apache.isis.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;

import static org.apache.isis.metamodel.facets.MethodLiteralConstants.VALIDATE_PREFIX;

public class ValidateObjectFacetMethodFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String[] PREFIXES = { VALIDATE_PREFIX, };

    public ValidateObjectFacetMethodFactory() {
        super(FeatureType.OBJECTS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final Method method = MethodFinderUtils.findMethod_returningText(
                cls,
                VALIDATE_PREFIX,
                NO_PARAMETERS_TYPES);
        if (method != null) {
            final TranslationService translationService = getTranslationService();
            // sadness: same as in TranslationFactory
            final String translationContext = ((IdentifiedHolder)facetHolder).getIdentifier().toClassIdentityString();
            FacetUtil.addFacet(new ValidateObjectFacetMethod(method, translationService, translationContext, facetHolder));
            processClassContext.removeMethod(method);
        }
    }

}
