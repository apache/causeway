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
package org.apache.isis.core.metamodel.facets.param.parameter;

import javax.inject.Inject;
import javax.validation.constraints.Pattern;

import org.springframework.core.MethodParameter;

import org.apache.isis.applib.annotations.Parameter;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.param.parameter.fileaccept.FileAcceptFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.mandatory.MandatoryFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.mandatory.MandatoryFacetInvertedByNullableAnnotationOnParameter;
import org.apache.isis.core.metamodel.facets.param.parameter.maxlen.MaxLengthFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.mustsatisfy.MustSatisfySpecificationFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.regex.RegExFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.regex.RegExFacetForPatternAnnotationOnParameter;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForConflictingOptionality;

import lombok.val;

public class ParameterAnnotationFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public ParameterAnnotationFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.PARAMETERS_ONLY);
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        processParamsMaxLength(processParameterContext);
        processParamsMustSatisfy(processParameterContext);
        processParamsRegEx(processParameterContext);
        processParamsOptional(processParameterContext);
        processParamsFileAccept(processParameterContext);
    }

    void processParamsMaxLength(final ProcessParameterContext processParameterContext) {

        val holder = processParameterContext.getFacetHolder();
        val parameterIfAny = processParameterContext.synthesizeOnParameter(Parameter.class);

        addFacetIfPresent(
                MaxLengthFacetForParameterAnnotation
                .create(parameterIfAny, holder));
    }

    void processParamsMustSatisfy(final ProcessParameterContext processParameterContext) {

        val holder = processParameterContext.getFacetHolder();
        val parameterIfAny = processParameterContext.synthesizeOnParameter(Parameter.class);

        addFacetIfPresent(
                MustSatisfySpecificationFacetForParameterAnnotation
                .create(parameterIfAny, holder, getFactoryService()));
    }

    void processParamsRegEx(final ProcessParameterContext processParameterContext) {

        val holder = processParameterContext.getFacetHolder();
        val parameterType = processParameterContext.getParameterType();

        val patternIfAny = processParameterContext.synthesizeOnParameter(Pattern.class);
        addFacetIfPresent(
                RegExFacetForPatternAnnotationOnParameter
                .create(patternIfAny, parameterType, holder));

        val parameterIfAny = processParameterContext.synthesizeOnParameter(Parameter.class);
        addFacetIfPresent(
                RegExFacetForParameterAnnotation
                .create(parameterIfAny, parameterType, holder));
    }

    void processParamsOptional(final ProcessParameterContext processParameterContext) {

        val holder = processParameterContext.getFacetHolder();
        val parameterAnnotations = MethodParameter
                .forExecutable(processParameterContext.getMethod(), processParameterContext.getParamNum())
                .getParameterAnnotations();
        val parameterType = processParameterContext.getParameterType();
        val parameterIfAny = processParameterContext.synthesizeOnParameter(Parameter.class);
        
        val hasNullable = 
                _NullSafe.stream(parameterAnnotations)
                    .map(annot->annot.annotationType().getSimpleName())
                    .anyMatch(name->name.equals("Nullable"));

        addFacetIfPresent(
                MandatoryFacetInvertedByNullableAnnotationOnParameter
                .create(hasNullable, parameterType, holder))
        .ifPresent(mandatoryFacet->
            MetaModelValidatorForConflictingOptionality.flagIfConflict(
                    mandatoryFacet, "Conflicting @Nullable with other optionality annotation"));

        addFacetIfPresent(
                MandatoryFacetForParameterAnnotation
                .create(parameterIfAny, parameterType, holder))
        .ifPresent(mandatoryFacet->
            MetaModelValidatorForConflictingOptionality.flagIfConflict(
                    mandatoryFacet, "Conflicting @Parameter#optionality with other optionality annotation"));
    }

    void processParamsFileAccept(final ProcessParameterContext processParameterContext) {

        val holder = processParameterContext.getFacetHolder();
        val parameterIfAny = processParameterContext.synthesizeOnParameter(Parameter.class);

        addFacetIfPresent(
                FileAcceptFacetForParameterAnnotation
                .create(parameterIfAny, holder));
    }


}
