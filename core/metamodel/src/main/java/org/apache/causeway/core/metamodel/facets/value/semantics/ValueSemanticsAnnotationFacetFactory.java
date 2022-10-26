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
package org.apache.causeway.core.metamodel.facets.value.semantics;

import java.util.Optional;

import javax.inject.Inject;
import javax.validation.constraints.Digits;

import org.apache.causeway.applib.annotation.ValueSemantics;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.TypedHolderAbstract;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MaxFractionalDigitsFacetAbstract;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MaxTotalDigitsFacetAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidatorForAmbiguousMixinAnnotations;

import lombok.val;

public class ValueSemanticsAnnotationFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public ValueSemanticsAnnotationFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.EVERYTHING);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        val valueSemanticsIfAny = processMethodContext
                .synthesizeOnMethodOrMixinType(
                        ValueSemantics.class,
                        () -> MetaModelValidatorForAmbiguousMixinAnnotations
                            .addValidationFailure(processMethodContext.getFacetHolder(), ValueSemantics.class));

        // support for @javax.validation.constraints.Digits
        val digitsIfAny = processMethodContext
                .synthesizeOnMethodOrMixinType(
                        Digits.class,
                        () -> MetaModelValidatorForAmbiguousMixinAnnotations
                            .addValidationFailure(processMethodContext.getFacetHolder(), Digits.class));

        processAll(processMethodContext.getFacetHolder(), valueSemanticsIfAny, digitsIfAny);
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        val valueSemanticsIfAny = processParameterContext.synthesizeOnParameter(ValueSemantics.class);

        // support for @javax.validation.constraints.Digits
        val digitsIfAny = processParameterContext.synthesizeOnParameter(Digits.class);

        processAll(processParameterContext.getFacetHolder(), valueSemanticsIfAny, digitsIfAny);
    }

    // -- HELPER

    private void processAll(
            final TypedHolderAbstract facetHolder,
            final Optional<ValueSemantics> valueSemanticsIfAny,
            final Optional<Digits> digitsIfAny) {
        processProvider(facetHolder, valueSemanticsIfAny);
        processDigits(facetHolder, valueSemanticsIfAny, digitsIfAny);
        processTemporalFormat(facetHolder, valueSemanticsIfAny);
    }

    private void processProvider(
            final TypedHolderAbstract facetHolder,
            final Optional<ValueSemantics> valueSemanticsIfAny) {

        // check for @ValueSemantics(provider=...)
        addFacetIfPresent(
                ValueSemanticsSelectingFacetForAnnotation
                .create(valueSemanticsIfAny, facetHolder));
    }

    private void processDigits(
            final TypedHolderAbstract facetHolder,
            final Optional<ValueSemantics> valueSemanticsIfAny,
            final Optional<Digits> digitsIfAny){

        addFacetIfPresent(
                MaxTotalDigitsFacetAbstract.minimum(
                        MaxTotalDigitsFacetFromValueSemanticsAnnotation
                        .create(valueSemanticsIfAny, facetHolder),
                        // support for @javax.validation.constraints.Digits
                        MaxTotalDigitsFacetFromJavaxValidationDigitsAnnotation
                        .create(digitsIfAny, facetHolder)
                        ));

        addFacetIfPresent(
                MinIntegerDigitsFacetFromValueSemanticsAnnotation
                .create(valueSemanticsIfAny, facetHolder));

        addFacetIfPresent(
                MaxFractionalDigitsFacetAbstract.minimum(
                        MaxFractionalDigitsFacetFromValueSemanticsAnnotation
                        .create(valueSemanticsIfAny, facetHolder),
                        // support for @javax.validation.constraints.Digits
                        MaxFractionalDigitsFacetFromJavaxValidationDigitsAnnotation
                        .create(digitsIfAny, facetHolder)
                        ));

        addFacetIfPresent(
                MinFractionalDigitsFacetFromValueSemanticsAnnotation
                .create(valueSemanticsIfAny, facetHolder));

    }

    private void processTemporalFormat(
            final TypedHolderAbstract facetHolder,
            final Optional<ValueSemantics> valueSemanticsIfAny){

        addFacetIfPresent(
                DateFormatStyleFacetFromValueSemanticsAnnotation
                .create(valueSemanticsIfAny, facetHolder));

        addFacetIfPresent(
                TimeFormatStyleFacetFromValueSemanticsAnnotation
                .create(valueSemanticsIfAny, facetHolder));

        addFacetIfPresent(
                TimeFormatPrecisionFacetFromValueSemanticsAnnotation
                .create(valueSemanticsIfAny, facetHolder));

        addFacetIfPresent(
                DateRenderAdjustFacetFromValueSemanticsAnnotation
                .create(valueSemanticsIfAny, facetHolder));

        addFacetIfPresent(
                TimeZoneTranslationFacetFromValueSemanticsAnnotation
                .create(valueSemanticsIfAny, facetHolder));
    }

}
