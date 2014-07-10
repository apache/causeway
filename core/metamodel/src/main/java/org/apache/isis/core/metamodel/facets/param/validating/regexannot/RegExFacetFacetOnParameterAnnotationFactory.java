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

package org.apache.isis.core.metamodel.facets.param.validating.regexannot;

import java.lang.annotation.Annotation;

import org.apache.isis.applib.annotation.RegEx;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.regex.RegExFacet;
import org.apache.isis.core.metamodel.facets.object.regex.TitleFacetFormattedByRegex;

public class RegExFacetFacetOnParameterAnnotationFactory extends FacetFactoryAbstract {

    public RegExFacetFacetOnParameterAnnotationFactory() {
        super(FeatureType.PARAMETERS_ONLY);
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        final Class<?>[] parameterTypes = processParameterContext.getMethod().getParameterTypes();
        if (processParameterContext.getParamNum() >= parameterTypes.length) {
            // ignore
            return;
        }
        if (!Annotations.isString(parameterTypes[processParameterContext.getParamNum()])) {
            return;
        }

        final Annotation[] parameterAnnotations = Annotations.getParameterAnnotations(processParameterContext.getMethod())[processParameterContext.getParamNum()];
        for (final Annotation parameterAnnotation : parameterAnnotations) {
            if (parameterAnnotation instanceof RegEx) {
                final RegEx annotation = (RegEx) parameterAnnotation;
                addRegexFacetAndCorrespondingTitleFacet(processParameterContext.getFacetHolder(), annotation);
                return;
            }
        }
    }

    private void addRegexFacetAndCorrespondingTitleFacet(final FacetHolder holder, final RegEx annotation) {
        final RegExFacet regexFacet = createRegexFacet(annotation, holder);
        if (regexFacet == null) {
            return;
        }
        FacetUtil.addFacet(regexFacet);

        final TitleFacet titleFacet = createTitleFacet(regexFacet);
        FacetUtil.addFacet(titleFacet);
    }

    private RegExFacet createRegexFacet(final RegEx annotation, final FacetHolder holder) {
        if (annotation == null) {
            return null;
        }

        final String validationExpression = annotation.validation();
        final boolean caseSensitive = annotation.caseSensitive();
        final String formatExpression = annotation.format();

        return new RegExFacetOnParameterAnnotation(validationExpression, formatExpression, caseSensitive, holder);
    }

    private TitleFacet createTitleFacet(final RegExFacet regexFacet) {
        return new TitleFacetFormattedByRegex(regexFacet);
    }

}
