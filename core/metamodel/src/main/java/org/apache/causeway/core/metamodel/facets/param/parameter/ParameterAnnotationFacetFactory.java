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
package org.apache.causeway.core.metamodel.facets.param.parameter;

import javax.inject.Inject;
import javax.validation.constraints.Pattern;

import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.param.parameter.fileaccept.FileAcceptFacetForParameterAnnotation;
import org.apache.causeway.core.metamodel.facets.param.parameter.mandatory.MandatoryFacetForParameterAnnotation;
import org.apache.causeway.core.metamodel.facets.param.parameter.mandatory.MandatoryFacetInvertedByNullableAnnotationOnParameter;
import org.apache.causeway.core.metamodel.facets.param.parameter.maxlen.MaxLengthFacetForParameterAnnotation;
import org.apache.causeway.core.metamodel.facets.param.parameter.mustsatisfy.MustSatisfySpecificationFacetForParameterAnnotation;
import org.apache.causeway.core.metamodel.facets.param.parameter.precpol.PrecedingParametersPolicyFacet;
import org.apache.causeway.core.metamodel.facets.param.parameter.regex.RegExFacetForParameterAnnotation;
import org.apache.causeway.core.metamodel.facets.param.parameter.regex.RegExFacetForPatternAnnotationOnParameter;

public class ParameterAnnotationFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public ParameterAnnotationFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.PARAMETERS_ONLY);
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        processPrecedingParamsPolicy(processParameterContext);
        processParamsMaxLength(processParameterContext);
        processParamsMustSatisfy(processParameterContext);
        processParamsRegEx(processParameterContext);
        processParamsOptional(processParameterContext);
        processParamsFileAccept(processParameterContext);
    }

    // check for @Parameter(precedingParamsPolicy=...)
    void processPrecedingParamsPolicy(final ProcessParameterContext processParameterContext) {

        var holder = processParameterContext.getFacetHolder();
        var parameterIfAny = processParameterContext.synthesizeOnParameter(Parameter.class);

        addFacetIfPresent(
                PrecedingParametersPolicyFacet
                        .create(parameterIfAny, getConfiguration(), holder));
    }

    void processParamsMaxLength(final ProcessParameterContext processParameterContext) {

        var holder = processParameterContext.getFacetHolder();
        var parameterIfAny = processParameterContext.synthesizeOnParameter(Parameter.class);

        addFacetIfPresent(
                MaxLengthFacetForParameterAnnotation
                .create(parameterIfAny, holder));
    }

    void processParamsMustSatisfy(final ProcessParameterContext processParameterContext) {

        var holder = processParameterContext.getFacetHolder();
        var parameterIfAny = processParameterContext.synthesizeOnParameter(Parameter.class);

        addFacetIfPresent(
                MustSatisfySpecificationFacetForParameterAnnotation
                .create(parameterIfAny, holder, getFactoryService()));
    }

    void processParamsRegEx(final ProcessParameterContext processParameterContext) {

        var holder = processParameterContext.getFacetHolder();
        var parameterIfAny = processParameterContext.synthesizeOnParameter(Parameter.class);

        var parameterType = processParameterContext.getParameterType();

        var patternIfAny = processParameterContext.synthesizeOnParameter(Pattern.class);
        addFacetIfPresent(
                RegExFacetForPatternAnnotationOnParameter
                .create(patternIfAny, parameterType, holder));

        addFacetIfPresent(
                RegExFacetForParameterAnnotation
                .create(parameterIfAny, parameterType, holder));
    }

    void processParamsOptional(final ProcessParameterContext processParameterContext) {

        var holder = processParameterContext.getFacetHolder();
        var parameterIfAny = processParameterContext.synthesizeOnParameter(Parameter.class);

        var hasNullable = processParameterContext.streamParameterAnnotations()
            .anyMatch(annot->annot.annotationType().getSimpleName().equals("Nullable"));

        var parameterType = processParameterContext.getParameterType();

        addFacetIfPresent(
                MandatoryFacetInvertedByNullableAnnotationOnParameter
                .create(hasNullable, parameterType, holder));

        addFacetIfPresent(
                MandatoryFacetForParameterAnnotation
                .create(parameterIfAny, parameterType, holder));
    }

    void processParamsFileAccept(final ProcessParameterContext processParameterContext) {

        var holder = processParameterContext.getFacetHolder();
        var parameterIfAny = processParameterContext.synthesizeOnParameter(Parameter.class);

        addFacetIfPresent(
                FileAcceptFacetForParameterAnnotation
                .create(parameterIfAny, holder));
    }

}
