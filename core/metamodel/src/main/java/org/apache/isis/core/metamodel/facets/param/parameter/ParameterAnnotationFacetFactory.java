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

import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.Pattern;

import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.param.parameter.fileaccept.FileAcceptFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.mandatory.MandatoryFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.mandatory.MandatoryFacetInvertedByNullableAnnotationOnParameter;
import org.apache.isis.core.metamodel.facets.param.parameter.maxlen.MaxLengthFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.mustsatisfy.MustSatisfySpecificationFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.regex.RegExFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.regex.RegExFacetForPatternAnnotationOnParameter;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForConflictingOptionality;

public class ParameterAnnotationFacetFactory extends FacetFactoryAbstract implements MetaModelValidatorRefiner {

    private final MetaModelValidatorForConflictingOptionality conflictingOptionalityValidator = new MetaModelValidatorForConflictingOptionality();

    public ParameterAnnotationFacetFactory() {
        super(FeatureType.PARAMETERS_ONLY);
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {

        final Method method = processParameterContext.getMethod();
        final int paramNum = processParameterContext.getParamNum();

        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (paramNum >= parameterTypes.length) {
            return; // ignore
        }

        processParamsMaxLength(processParameterContext);
        processParamsMustSatisfy(processParameterContext);
        processParamsRegEx(processParameterContext);
        processParamsOptional(processParameterContext);
        processParamsFileAccept(processParameterContext);
    }

    void processParamsMaxLength(final ProcessParameterContext processParameterContext) {

        final Method method = processParameterContext.getMethod();
        final int paramNum = processParameterContext.getParamNum();
        final FacetedMethodParameter holder = processParameterContext.getFacetHolder();
        final List<Parameter> parameters = Annotations.getAnnotations(method, paramNum, Parameter.class);

        FacetUtil.addFacet(MaxLengthFacetForParameterAnnotation.create(parameters, holder));
    }

    void processParamsMustSatisfy(final ProcessParameterContext processParameterContext) {

        final Method method = processParameterContext.getMethod();
        final int paramNum = processParameterContext.getParamNum();
        final FacetedMethodParameter holder = processParameterContext.getFacetHolder();
        final List<Parameter> parameters = Annotations.getAnnotations(method, paramNum, Parameter.class);

        FacetUtil.addFacet(
                MustSatisfySpecificationFacetForParameterAnnotation.create(parameters, holder, getServiceInjector()));
    }

    void processParamsRegEx(final ProcessParameterContext processParameterContext) {

        final Method method = processParameterContext.getMethod();
        final int paramNum = processParameterContext.getParamNum();
        final FacetedMethodParameter holder = processParameterContext.getFacetHolder();
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Class<?> parameterType = parameterTypes[paramNum];


        final List<Pattern> patterns = Annotations.getAnnotations(method, paramNum, Pattern.class);
        FacetUtil.addFacet(
                RegExFacetForPatternAnnotationOnParameter.create(patterns, parameterType, holder));

        final List<Parameter> parameters = Annotations.getAnnotations(method, paramNum, Parameter.class);
        FacetUtil.addFacet(
                RegExFacetForParameterAnnotation.create(parameters, parameterType, holder));
    }

    void processParamsOptional(final ProcessParameterContext processParameterContext) {

        final Method method = processParameterContext.getMethod();
        final int paramNum = processParameterContext.getParamNum();
        final FacetedMethodParameter holder = processParameterContext.getFacetHolder();
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Class<?> parameterType = parameterTypes[paramNum];

        final List<Nullable> nullabilities = Annotations.getAnnotations(method, paramNum, Nullable.class);
        final MandatoryFacet facet =
                MandatoryFacetInvertedByNullableAnnotationOnParameter.create(nullabilities, parameterType, holder);
        FacetUtil.addFacet(facet);
        conflictingOptionalityValidator.flagIfConflict(
                facet, "Conflicting @Nullable with other optionality annotation");

        final List<Parameter> parameters = Annotations.getAnnotations(method, paramNum, Parameter.class);
        final MandatoryFacet mandatoryFacet =
                MandatoryFacetForParameterAnnotation.create(parameters, parameterType, holder);
        FacetUtil.addFacet(mandatoryFacet);
        conflictingOptionalityValidator.flagIfConflict(
                mandatoryFacet, "Conflicting @Parameter#optionality with other optionality annotation");

    }

    void processParamsFileAccept(final ProcessParameterContext processParameterContext) {

        final Method method = processParameterContext.getMethod();
        final int paramNum = processParameterContext.getParamNum();
        final FacetedMethodParameter holder = processParameterContext.getFacetHolder();
        final List<Parameter> parameters = Annotations.getAnnotations(method, paramNum, Parameter.class);

        FacetUtil.addFacet(FileAcceptFacetForParameterAnnotation.create(parameters, holder));
    }


    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator) {
        metaModelValidator.add(conflictingOptionalityValidator);
    }


}
