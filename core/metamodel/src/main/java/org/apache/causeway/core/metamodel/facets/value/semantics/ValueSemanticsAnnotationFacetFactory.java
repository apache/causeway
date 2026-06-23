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

import jakarta.inject.Inject;
import jakarta.validation.constraints.Digits;

import org.apache.causeway.applib.annotation.ValueSemantics;
import org.apache.causeway.core.metamodel.commons.ClassUtil;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.TypedFacetHolder;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MaxFractionalDigitsFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MaxIntegerDigitsFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MinFractionalDigitsFacet;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailureUtils;

public class ValueSemanticsAnnotationFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public ValueSemanticsAnnotationFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.EVERYTHING);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        var valueSemanticsOpt = processMethodContext
            .synthesizeOnMethodOrMixinType(
                    ValueSemantics.class,
                    () -> ValidationFailureUtils
                        .raiseAmbiguousMixinAnnotations(processMethodContext.getFacetHolder(), ValueSemantics.class));

        // support for @jakarta.validation.constraints.Digits
        var digitsOpt = processMethodContext
            .synthesizeOnMethodOrMixinType(
                    Digits.class,
                    () -> ValidationFailureUtils
                        .raiseAmbiguousMixinAnnotations(processMethodContext.getFacetHolder(), Digits.class));

        processAll(processMethodContext.getFacetHolder(), valueSemanticsOpt, digitsOpt);
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        var valueSemanticsOpt = processParameterContext.synthesizeOnParameter(ValueSemantics.class);
        // support for @jakarta.validation.constraints.Digits
        var digitsOpt = processParameterContext.synthesizeOnParameter(Digits.class);

        processAll(processParameterContext.getFacetHolder(), valueSemanticsOpt, digitsOpt);
    }

    // -- HELPER

    private void processAll(
            final TypedFacetHolder facetHolder,
            final Optional<ValueSemantics> valueSemanticsOpt,
            final Optional<Digits> digitsOpt) {
        processProvider(facetHolder, valueSemanticsOpt);
        processDigits(facetHolder, valueSemanticsOpt, digitsOpt);
        processTemporalFormat(facetHolder, valueSemanticsOpt);
    }

    private void processProvider(
            final TypedFacetHolder facetHolder,
            final Optional<ValueSemantics> valueSemanticsOpt) {

        // check for @ValueSemantics(provider=...)
        addFacetIfPresent(
            ValueSemanticsSelectingFacetForAnnotation
                .create(valueSemanticsOpt, facetHolder));
    }

    private void processDigits(
            final TypedFacetHolder facetHolder,
            final Optional<ValueSemantics> valueSemanticsOpt,
            final Optional<Digits> digitsOpt){

        // max total digits
        addFacetIfPresent(
            MaxTotalDigitsFacetFromValueSemanticsAnnotation
                .create(valueSemanticsOpt, facetHolder));

        // max integer digits
        addFacetIfPresent(
            MaxIntegerDigitsFacet.strongestConstraint(
                MaxIntegerDigitsFacetFromValueSemanticsAnnotation
                    .create(valueSemanticsOpt, facetHolder),
                // support for @jakarta.validation.constraints.Digits
                MaxIntegerDigitsFacetFromJakartaDigitsAnnotation
                    .create(digitsOpt, facetHolder)));

        // min integer digits
        addFacetIfPresent(
            MinIntegerDigitsFacetFromValueSemanticsAnnotation
                .create(valueSemanticsOpt, facetHolder));

        if(ClassUtil.isJavaBuiltInInteger(facetHolder.getFeatureIdentifier().logicalType().correspondingClass()))
            return; // skip fractional facets

        // max fractional digits
        addFacetIfPresent(
            MaxFractionalDigitsFacet.strongestConstraint(
                MaxFractionalDigitsFacetFromValueSemanticsAnnotation
                    .create(valueSemanticsOpt, facetHolder),
                // support for @jakarta.validation.constraints.Digits
                MaxFractionalDigitsFacetFromJakartaDigitsAnnotation
                    .create(digitsOpt, facetHolder)));

        // min fractional digits
        addFacetIfPresent(
            MinFractionalDigitsFacet.strongestConstraint(
                MinFractionalDigitsFacetFromValueSemanticsAnnotation
                    .create(valueSemanticsOpt, facetHolder),
                // support for @jakarta.validation.constraints.Digits (if enabled)
                getConfiguration().valueTypes().bigDecimal().useScaleForMinFractionalFacet()
                    ? MinFractionalDigitsFacetFromJakartaDigitsAnnotation
                        .create(digitsOpt, facetHolder)
                    : Optional.empty()));
    }

    private void processTemporalFormat(
            final TypedFacetHolder facetHolder,
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
