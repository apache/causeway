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

import org.apache.isis.applib.annotation.*;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.regex.RegExFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.regex.TitleFacetFormattedByRegex;
import org.apache.isis.core.metamodel.facets.param.parameter.fileaccept.FileAcceptFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.mandatory.MandatoryFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.mandatory.MandatoryFacetInvertedByNullableAnnotationOnParameter;
import org.apache.isis.core.metamodel.facets.param.parameter.mandatory.MandatoryFacetInvertedByOptionalAnnotationOnParameter;
import org.apache.isis.core.metamodel.facets.param.parameter.maxlen.MaxLengthFacetForMaxLengthAnnotationOnParameter;
import org.apache.isis.core.metamodel.facets.param.parameter.maxlen.MaxLengthFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.mustsatisfy.MustSatisfySpecificationFacetForMustSatisfyAnnotationOnParameter;
import org.apache.isis.core.metamodel.facets.param.parameter.mustsatisfy.MustSatisfySpecificationFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.regex.RegExFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.regex.RegExFacetFromRegExAnnotationOnParameter;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForConflictingOptionality;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForDeprecatedAnnotation;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class ParameterAnnotationFacetFactory extends FacetFactoryAbstract implements MetaModelValidatorRefiner {

    private final MetaModelValidatorForDeprecatedAnnotation maxLengthValidator = new MetaModelValidatorForDeprecatedAnnotation(MaxLength.class);
    private final MetaModelValidatorForDeprecatedAnnotation mustSatisfyValidator = new MetaModelValidatorForDeprecatedAnnotation(MustSatisfy.class);
    private final MetaModelValidatorForDeprecatedAnnotation regexValidator = new MetaModelValidatorForDeprecatedAnnotation(RegEx.class);
    private final MetaModelValidatorForDeprecatedAnnotation optionalValidator = new MetaModelValidatorForDeprecatedAnnotation(Optional.class);
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
        final Annotation[] parameterAnnotations = Annotations.getParameterAnnotations(method)[paramNum];

        for (final Annotation parameterAnnotation : parameterAnnotations) {
            if (parameterAnnotation instanceof MaxLength) {
                final MaxLength annotation = (MaxLength) parameterAnnotation;
                final org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet facet = MaxLengthFacetForMaxLengthAnnotationOnParameter.create(annotation, processParameterContext.getFacetHolder());
                FacetUtil.addFacet(maxLengthValidator.flagIfPresent(facet, processParameterContext));
                return;
            }
        }

        for (final Annotation parameterAnnotation : parameterAnnotations) {
            if (parameterAnnotation instanceof Parameter) {
                final Parameter parameter = (Parameter) parameterAnnotation;
                FacetUtil.addFacet(
                        MaxLengthFacetForParameterAnnotation.create(parameter, holder));
                return;
            }
        }
    }

    void processParamsMustSatisfy(final ProcessParameterContext processParameterContext) {

        final Method method = processParameterContext.getMethod();
        final int paramNum = processParameterContext.getParamNum();
        final FacetedMethodParameter holder = processParameterContext.getFacetHolder();
        final Annotation[] parameterAnnotations = Annotations.getParameterAnnotations(method)[paramNum];

        for (final Annotation parameterAnnotation : parameterAnnotations) {
            if (parameterAnnotation instanceof MustSatisfy) {
                final MustSatisfy annotation = (MustSatisfy) parameterAnnotation;
                final Facet facet = MustSatisfySpecificationFacetForMustSatisfyAnnotationOnParameter.create(annotation, processParameterContext.getFacetHolder(), servicesInjector);
                FacetUtil.addFacet(mustSatisfyValidator.flagIfPresent(facet, processParameterContext));
                return;
            }
        }

        for (final Annotation parameterAnnotation : parameterAnnotations) {
            if (parameterAnnotation instanceof Parameter) {
                final Parameter parameter = (Parameter) parameterAnnotation;
                FacetUtil.addFacet(
                        MustSatisfySpecificationFacetForParameterAnnotation.create(parameter, holder, servicesInjector));
                return;
            }
        }
    }

    void processParamsRegEx(final ProcessParameterContext processParameterContext) {

        final Method method = processParameterContext.getMethod();
        final int paramNum = processParameterContext.getParamNum();
        final FacetedMethodParameter holder = processParameterContext.getFacetHolder();
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Annotation[] parameterAnnotations = Annotations.getParameterAnnotations(method)[paramNum];

        for (final Annotation parameterAnnotation : parameterAnnotations) {
            final Class<?> parameterType = parameterTypes[paramNum];
            if (parameterAnnotation instanceof RegEx) {
                final RegEx annotation = (RegEx) parameterAnnotation;
                final RegExFacet facet = RegExFacetFromRegExAnnotationOnParameter.create(annotation, parameterType, holder);
                FacetUtil.addFacet(regexValidator.flagIfPresent(facet, processParameterContext));
                final RegExFacet regExFacet = facet;

                // regex also adds a title facet
                if(regExFacet != null) {
                    FacetUtil.addFacet(new TitleFacetFormattedByRegex(regExFacet));
                }
                return;
            }
        }

        for (final Annotation parameterAnnotation : parameterAnnotations) {
            if (parameterAnnotation instanceof Parameter) {
                final Parameter parameter = (Parameter) parameterAnnotation;
                final Class<?> parameterType = parameterTypes[paramNum];
                FacetUtil.addFacet(
                        RegExFacetForParameterAnnotation.create(parameter, parameterType, holder));
                return;
            }
        }
    }

    void processParamsOptional(final ProcessParameterContext processParameterContext) {

        final Method method = processParameterContext.getMethod();
        final int paramNum = processParameterContext.getParamNum();
        final FacetedMethodParameter holder = processParameterContext.getFacetHolder();
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Annotation[] parameterAnnotations = Annotations.getParameterAnnotations(method)[paramNum];

        for (final Annotation parameterAnnotation : parameterAnnotations) {
            final Class<?> parameterType = parameterTypes[paramNum];
            if (parameterAnnotation instanceof Optional) {
                final Optional annotation = (Optional) parameterAnnotation;
                FacetUtil.addFacet(optionalValidator.flagIfPresent(
                        MandatoryFacetInvertedByOptionalAnnotationOnParameter.create(
                                annotation, parameterType, holder), processParameterContext));
            }
        }

        for (final Annotation parameterAnnotation : parameterAnnotations) {
            final Class<?> parameterType = parameterTypes[paramNum];
            if (parameterAnnotation instanceof javax.annotation.Nullable) {
                final Nullable annotation = (Nullable) parameterAnnotation;
                final MandatoryFacet facet =
                        MandatoryFacetInvertedByNullableAnnotationOnParameter.create(annotation, parameterType, holder);
                FacetUtil.addFacet(facet);
                conflictingOptionalityValidator.flagIfConflict(
                        facet, "Conflicting @Nullable with other optionality annotation");
            }
        }

        for (final Annotation parameterAnnotation : parameterAnnotations) {
            if (parameterAnnotation instanceof Parameter) {
                final Parameter parameter = (Parameter) parameterAnnotation;
                final Class<?> parameterType = parameterTypes[paramNum];
                final MandatoryFacet facet =
                        MandatoryFacetForParameterAnnotation.create(parameter, parameterType, holder);
                FacetUtil.addFacet(facet);
                conflictingOptionalityValidator.flagIfConflict(
                        facet, "Conflicting @Parameter#optionality with other optionality annotation");
            }
        }
    }

    void processParamsFileAccept(final ProcessParameterContext processParameterContext) {

        final Method method = processParameterContext.getMethod();
        final int paramNum = processParameterContext.getParamNum();
        final FacetedMethodParameter holder = processParameterContext.getFacetHolder();
        final Annotation[] parameterAnnotations = Annotations.getParameterAnnotations(method)[paramNum];

        for (final Annotation parameterAnnotation : parameterAnnotations) {
            if (parameterAnnotation instanceof Parameter) {
                final Parameter parameter = (Parameter) parameterAnnotation;
                FacetUtil.addFacet(
                        FileAcceptFacetForParameterAnnotation.create(parameter, holder));
                return;
            }
        }
    }


    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator, final IsisConfiguration configuration) {
        metaModelValidator.add(maxLengthValidator);
        metaModelValidator.add(mustSatisfyValidator);
        metaModelValidator.add(regexValidator);
        metaModelValidator.add(optionalValidator);
        metaModelValidator.add(conflictingOptionalityValidator);
    }

    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        super.setServicesInjector(servicesInjector);
        IsisConfiguration configuration = servicesInjector.getConfigurationServiceInternal();
        maxLengthValidator.setConfiguration(configuration);
        mustSatisfyValidator.setConfiguration(configuration);
        regexValidator.setConfiguration(configuration);
        optionalValidator.setConfiguration(configuration);
    }


}
