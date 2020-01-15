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

import javax.annotation.Nullable;
import javax.validation.constraints.Pattern;

import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.param.parameter.fileaccept.FileAcceptFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.mandatory.MandatoryFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.mandatory.MandatoryFacetInvertedByNullableAnnotationOnParameter;
import org.apache.isis.core.metamodel.facets.param.parameter.maxlen.MaxLengthFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.mustsatisfy.MustSatisfySpecificationFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.regex.RegExFacetForParameterAnnotation;
import org.apache.isis.core.metamodel.facets.param.parameter.regex.RegExFacetForPatternAnnotationOnParameter;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForConflictingOptionality;

import lombok.val;

public class ParameterAnnotationFacetFactory extends FacetFactoryAbstract
implements MetaModelRefiner {

    private final MetaModelValidatorForConflictingOptionality conflictingOptionalityValidator = 
            new MetaModelValidatorForConflictingOptionality();

    public ParameterAnnotationFacetFactory() {
        super(FeatureType.PARAMETERS_ONLY);
    }
    
    @Override
    public void setMetaModelContext(MetaModelContext metaModelContext) {
        super.setMetaModelContext(metaModelContext);
        conflictingOptionalityValidator.setMetaModelContext(metaModelContext);
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

        super.addFacet(MaxLengthFacetForParameterAnnotation.create(parameterIfAny, holder));
    }

    void processParamsMustSatisfy(final ProcessParameterContext processParameterContext) {

        val holder = processParameterContext.getFacetHolder();
        val parameterIfAny = processParameterContext.synthesizeOnParameter(Parameter.class);

        super.addFacet(
                MustSatisfySpecificationFacetForParameterAnnotation.create(parameterIfAny, holder, getServiceInjector()));
    }

    void processParamsRegEx(final ProcessParameterContext processParameterContext) {

        val holder = processParameterContext.getFacetHolder();
        val parameterType = processParameterContext.getParameterType();

        val patternIfAny = processParameterContext.synthesizeOnParameter(Pattern.class);
        super.addFacet(
                RegExFacetForPatternAnnotationOnParameter.create(patternIfAny, parameterType, holder));

        val parameterIfAny = processParameterContext.synthesizeOnParameter(Parameter.class);
        super.addFacet(
                RegExFacetForParameterAnnotation.create(parameterIfAny, parameterType, holder));
    }

    void processParamsOptional(final ProcessParameterContext processParameterContext) {

        val holder = processParameterContext.getFacetHolder();
        val parameterType = processParameterContext.getParameterType();

        val nullableIfAny = processParameterContext.synthesizeOnParameter(Nullable.class);
        final MandatoryFacet facet =
                MandatoryFacetInvertedByNullableAnnotationOnParameter.create(nullableIfAny, parameterType, holder);
        super.addFacet(facet);
        conflictingOptionalityValidator.flagIfConflict(
                facet, "Conflicting @Nullable with other optionality annotation");

        val parameterIfAny = processParameterContext.synthesizeOnParameter(Parameter.class);
        final MandatoryFacet mandatoryFacet =
                MandatoryFacetForParameterAnnotation.create(parameterIfAny, parameterType, holder);
        super.addFacet(mandatoryFacet);
        conflictingOptionalityValidator.flagIfConflict(
                mandatoryFacet, "Conflicting @Parameter#optionality with other optionality annotation");

    }

    void processParamsFileAccept(final ProcessParameterContext processParameterContext) {

        val holder = processParameterContext.getFacetHolder();
        val parameterIfAny = processParameterContext.synthesizeOnParameter(Parameter.class);

        super.addFacet(FileAcceptFacetForParameterAnnotation.create(parameterIfAny, holder));
    }

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {
        programmingModel.addValidator(conflictingOptionalityValidator);
    }


}
