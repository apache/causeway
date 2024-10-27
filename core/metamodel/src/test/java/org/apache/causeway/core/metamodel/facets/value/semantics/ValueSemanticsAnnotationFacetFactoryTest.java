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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.TimeZoneTranslation;
import org.apache.causeway.applib.annotation.ValueSemantics;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.objectvalue.daterenderedadjust.DateRenderAdjustFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MaxFractionalDigitsFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MaxTotalDigitsFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MinFractionalDigitsFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MinIntegerDigitsFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.temporalformat.DateFormatStyleFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.temporalformat.TimeFormatStyleFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.temporalformat.TimeZoneTranslationFacet;

class ValueSemanticsAnnotationFacetFactoryTest
extends FacetFactoryTestAbstract {

    // -- MAX TOTAL DIGITS

    @Test
    void maxTotalPickedUpOnProperty() {
        // given
        class Order {
            @ValueSemantics(maxTotalDigits = 5)
            public BigDecimal getCost() { return null; }
        }
        propertyScenario(Order.class, "cost", (processMethodContext, facetHolder, facetedMethod) -> {
            // when
            newFacetFactory().process(processMethodContext);
            // then
            assertMaxTotalDigits(facetedMethod, 5);
            assertDefaultMinIntegerDigits(facetedMethod);
            assertDefaultMaxFractionalDigits(facetedMethod);
            assertDefaultMinFractionalDigits(facetedMethod);
        });
    }

    @Test
    void maxTotalPickedUpOnActionParameter() {
        // given
        @SuppressWarnings("unused")
        class Order {
            public void updateCost(
                    @ValueSemantics(maxTotalDigits = 5)
                    final BigDecimal cost) { }
        }
        parameterScenario(Order.class, "updateCost", 0, (processParameterContext, facetHolder, facetedMethod, facetedMethodParameter) -> {
            // when
            newFacetFactory().processParams(processParameterContext);
            // then
            assertMaxTotalDigits(facetedMethodParameter, 5);
            assertDefaultMinIntegerDigits(facetedMethodParameter);
            assertDefaultMaxFractionalDigits(facetedMethodParameter);
            assertDefaultMinFractionalDigits(facetedMethodParameter);
        });
    }

    // -- MIN INTEGER DIGITS

    @Test
    void minIntegerPickedUpOnProperty() {
        // given
        class Order {
            @ValueSemantics(minIntegerDigits = 5)
            public BigDecimal getCost() { return null; }
        }
        propertyScenario(Order.class, "cost", (processMethodContext, facetHolder, facetedMethod) -> {
            // when
            newFacetFactory().process(processMethodContext);
            // then
            assertDefaultMaxTotalDigits(facetedMethod);
            assertMinIntegerDigits(facetedMethod, 5);
            assertDefaultMaxFractionalDigits(facetedMethod);
            assertDefaultMinFractionalDigits(facetedMethod);
        });
    }

    @Test
    void minIntegerPickedUpOnActionParameter() {
        // given
        @SuppressWarnings("unused")
        class Order {
            public void updateCost(
                    @ValueSemantics(minIntegerDigits = 5)
                    final BigDecimal cost) { }
        }
        parameterScenario(Order.class, "updateCost", 0, (processParameterContext, facetHolder, facetedMethod, facetedMethodParameter) -> {
            // when
            newFacetFactory().processParams(processParameterContext);
            // then
            assertDefaultMaxTotalDigits(facetedMethodParameter);
            assertMinIntegerDigits(facetedMethodParameter, 5);
            assertDefaultMaxFractionalDigits(facetedMethodParameter);
            assertDefaultMinFractionalDigits(facetedMethodParameter);
        });
    }

    // -- MAX FRACTIONAL DIGITS

    @Test
    void maxFracionalPickedUpOnProperty() {
        // given
        class Order {
            @ValueSemantics(maxFractionalDigits = 5)
            public BigDecimal getCost() { return null; }
        }
        propertyScenario(Order.class, "cost", (processMethodContext, facetHolder, facetedMethod) -> {
            // when
            newFacetFactory().process(processMethodContext);
            // then
            assertDefaultMaxTotalDigits(facetedMethod);
            assertDefaultMinIntegerDigits(facetedMethod);
            assertMaxFractionalDigits(facetedMethod, 5);
            assertDefaultMinFractionalDigits(facetedMethod);
        });
    }

    @Test
    void maxFracionalPickedUpOnActionParameter() {
        // given
        @SuppressWarnings("unused")
        class Order {
            public void updateCost(
                    @ValueSemantics(maxFractionalDigits = 5)
                    final BigDecimal cost) { }
        }
        parameterScenario(Order.class, "updateCost", 0, (processParameterContext, facetHolder, facetedMethod, facetedMethodParameter) -> {
            // when
            newFacetFactory().processParams(processParameterContext);
            // then
            assertDefaultMaxTotalDigits(facetedMethodParameter);
            assertDefaultMinIntegerDigits(facetedMethodParameter);
            assertMaxFractionalDigits(facetedMethodParameter, 5);
            assertDefaultMinFractionalDigits(facetedMethodParameter);
        });
    }

    // -- MIN FRACTIONAL DIGITS

    @Test
    void minFracionalPickedUpOnProperty() {
        // given
        class Order {
            @ValueSemantics(minFractionalDigits = 5)
            public BigDecimal getCost() { return null; }
        }
        propertyScenario(Order.class, "cost", (processMethodContext, facetHolder, facetedMethod) -> {
            // when
            newFacetFactory().process(processMethodContext);
            // then
            assertDefaultMaxTotalDigits(facetedMethod);
            assertDefaultMinIntegerDigits(facetedMethod);
            assertDefaultMaxFractionalDigits(facetedMethod);
            assertMinFractionalDigits(facetedMethod, 5);
        });
    }

    @Test
    void minFracionalPickedUpOnActionParameter() {
        // given
        @SuppressWarnings("unused")
        class Order {
            public void updateCost(
                    @ValueSemantics(minFractionalDigits = 5)
                    final BigDecimal cost) { }
        }
        parameterScenario(Order.class, "updateCost", 0, (processParameterContext, facetHolder, facetedMethod, facetedMethodParameter) -> {
            // when
            newFacetFactory().processParams(processParameterContext);
            // then
            assertDefaultMaxTotalDigits(facetedMethodParameter);
            assertDefaultMinIntegerDigits(facetedMethodParameter);
            assertDefaultMaxFractionalDigits(facetedMethodParameter);
            assertMinFractionalDigits(facetedMethodParameter, 5);
        });
    }

    // -- DIGITS ANNOTATION

    @Test
    void digitsAnnotationPickedUpOnProperty() {
        // given
        class Order {
            @javax.validation.constraints.Digits(integer=14, fraction=4)
            public BigDecimal getCost() { return null; }
        }
        propertyScenario(Order.class, "cost", (processMethodContext, facetHolder, facetedMethod) -> {
            // when
            newFacetFactory().process(processMethodContext);
            // then
            assertDigitsFacets(facetedMethod, 18, 4);
        });
    }

    @Test
    void digitsAnnotationPickedUpOnActionParameter() {
        // given
        @SuppressWarnings("unused")
        class Order {
            public void updateCost(
                    @javax.validation.constraints.Digits(integer=14, fraction=4)
                    final BigDecimal cost) { }
        }
        parameterScenario(Order.class, "updateCost", 0, (processParameterContext, facetHolder, facetedMethod, facetedMethodParameter) -> {
            // when
            newFacetFactory().processParams(processParameterContext);
            // then
            assertDigitsFacets(facetedMethodParameter, 18, 4);
        });
    }

    // -- CONSTRAINT MERGERS

    @Test
    void multipleAnnotationsMergedOnProperty() {
        // given
        class Order {

            @javax.validation.constraints.Digits(integer=14, fraction=4)
            @ValueSemantics(maxTotalDigits = 19)
            public BigDecimal maxTotalA() { return null; }

            @javax.validation.constraints.Digits(integer=14, fraction=5)
            @ValueSemantics(maxTotalDigits = 17)
            public BigDecimal maxTotalB() { return null; }

            @javax.validation.constraints.Digits(integer=14, fraction=4)
            @ValueSemantics(maxFractionalDigits = 5)
            public BigDecimal maxFracA() { return null; }

            @javax.validation.constraints.Digits(integer=14, fraction=5)
            @ValueSemantics(maxFractionalDigits = 4)
            public BigDecimal maxFracB() { return null; }

        }

        actionScenario(Order.class, "maxTotalA", (processMethodContext, facetHolder, facetedMethod) -> {
            // when
            newFacetFactory().process(processMethodContext);
            // then - lowest bound wins
            assertMaxTotalDigits(facetedMethod, 18);
        });

        actionScenario(Order.class, "maxTotalB", (processMethodContext, facetHolder, facetedMethod) -> {
            // when
            newFacetFactory().process(processMethodContext);
            // then - lowest bound wins
            assertMaxTotalDigits(facetedMethod, 17);
        });

        actionScenario(Order.class, "maxFracA", (processMethodContext, facetHolder, facetedMethod) -> {
            // when
            newFacetFactory().process(processMethodContext);
            // then - lowest bound wins
            assertMaxFractionalDigits(facetedMethod, 4);
        });

        actionScenario(Order.class, "maxFracB", (processMethodContext, facetHolder, facetedMethod) -> {
            // when
            newFacetFactory().process(processMethodContext);
            // then - lowest bound wins
            assertMaxFractionalDigits(facetedMethod, 4);
        });
    }

    // -- TEMPORAL FORMAT STYLE

    @Test
    void dateAdjustPickedUpOnProperty() {
        // given
        class Order {
            @ValueSemantics(dateRenderAdjustDays = ValueSemantics.AS_DAY_BEFORE)
            public LocalDateTime getDateTime() { return null; }
        }
        propertyScenario(Order.class, "dateTime", (processMethodContext, facetHolder, facetedMethod) -> {
            // when
            newFacetFactory().process(processMethodContext);
            // then
            assertDateRenderAdjustDays(facetedMethod, -1);
        });
    }

    @Test
    void timeZoneTranslationPickedUpOnProperty() {
        // given
        class Order {
            @ValueSemantics(timeZoneTranslation = TimeZoneTranslation.NONE)
            public LocalDateTime getDateTimeA() { return null; }

            @ValueSemantics(timeZoneTranslation = TimeZoneTranslation.TO_LOCAL_TIMEZONE)
            public LocalDateTime getDateTimeB() { return null; }

        }
        propertyScenario(Order.class, "dateTimeA", (processMethodContext, facetHolder, facetedMethod) -> {
            // when
            newFacetFactory().process(processMethodContext);
            // then
            assertTimeZoneTranslation(facetedMethod, TimeZoneTranslation.NONE);
        });
        propertyScenario(Order.class, "dateTimeB", (processMethodContext, facetHolder, facetedMethod) -> {
            // when
            newFacetFactory().process(processMethodContext);
            // then
            assertTimeZoneTranslation(facetedMethod, TimeZoneTranslation.TO_LOCAL_TIMEZONE);
        });
    }

    @Test
    void dateFormatStylePickedUpOnProperty() {
        // given
        class Order {
            @ValueSemantics(dateFormatStyle = FormatStyle.FULL)
            public LocalDateTime getDateTime() { return null; }
        }
        // when
        propertyScenario(Order.class, "dateTime", (processMethodContext, facetHolder, facetedMethod) -> {
            // when
            newFacetFactory().process(processMethodContext);
            // then
            assertDateFormatStyle(facetedMethod, FormatStyle.FULL);
        });
    }

    @Test
    void timeFormatStylePickedUpOnProperty() {
        // given
        class Order {
            @ValueSemantics(timeFormatStyle = FormatStyle.FULL)
            public LocalDateTime getDateTime() { return null; }
        }
        propertyScenario(Order.class, "dateTime", (processMethodContext, facetHolder, facetedMethod) -> {
            // when
            newFacetFactory().process(processMethodContext);
            // then
            assertTimeFormatStyle(facetedMethod, FormatStyle.FULL);
        });
    }

    // -- HELPER

    ValueSemanticsAnnotationFacetFactory newFacetFactory() {
        return new ValueSemanticsAnnotationFacetFactory(getMetaModelContext());
    }

    private void assertDefaultMaxTotalDigits(final FacetHolder facetedMethod) {
        assertMaxTotalDigits(facetedMethod, 65);
    }

    private void assertDefaultMinIntegerDigits(final FacetHolder facetedMethod) {
        assertMinIntegerDigits(facetedMethod, 1);
    }

    private void assertDefaultMaxFractionalDigits(final FacetHolder facetedMethod) {
        assertMaxFractionalDigits(facetedMethod, 30);
    }

    private void assertDefaultMinFractionalDigits(final FacetHolder facetedMethod) {
        assertMinFractionalDigits(facetedMethod, 0);
    }

    private void assertMaxTotalDigits(
            final FacetHolder facetedMethod, final int maxTotalDigits) {
        final MaxTotalDigitsFacet facet = facetedMethod.getFacet(MaxTotalDigitsFacet.class);
        assertNotNull(facet);
        assertThat(facet.getMaxTotalDigits(), is(maxTotalDigits));
    }

    private void assertMinIntegerDigits(
            final FacetHolder facetedMethod, final int minIntegerDigits) {
        final MinIntegerDigitsFacet facet = facetedMethod.getFacet(MinIntegerDigitsFacet.class);
        assertNotNull(facet);
        assertThat(facet.getMinIntegerDigits(), is(minIntegerDigits));
    }

    private void assertMaxFractionalDigits(
            final FacetHolder facetedMethod, final int maxFractionalDigits) {
        final MaxFractionalDigitsFacet facet = facetedMethod.getFacet(MaxFractionalDigitsFacet.class);
        assertNotNull(facet);
        assertThat(facet.getMaxFractionalDigits(), is(maxFractionalDigits));
    }

    private void assertMinFractionalDigits(
            final FacetHolder facetedMethod, final int minFractionalDigits) {
        final MinFractionalDigitsFacet facet = facetedMethod.getFacet(MinFractionalDigitsFacet.class);
        assertNotNull(facet);
        assertThat(facet.getMinFractionalDigits(), is(minFractionalDigits));
    }

    private void assertDigitsFacets(
            final FacetHolder facetedMethod, final int maxTotalDigits, final int maxFractionalDigits) {
        if(maxTotalDigits>=0) {
            final MaxTotalDigitsFacet facet = facetedMethod.getFacet(MaxTotalDigitsFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof MaxTotalDigitsFacetFromJavaxValidationDigitsAnnotation);
            assertThat(facet.getMaxTotalDigits(), is(maxTotalDigits));
        }

        if(maxFractionalDigits>=0) {
            final MaxFractionalDigitsFacet facet = facetedMethod.getFacet(MaxFractionalDigitsFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof MaxFractionalDigitsFacetFromJavaxValidationDigitsAnnotation);
            assertThat(facet.getMaxFractionalDigits(), is(maxFractionalDigits));
        }
    }

    private void assertDateRenderAdjustDays(
            final FacetedMethod facetedMethod, final int adjustDays) {
        final DateRenderAdjustFacet facet = facetedMethod.getFacet(DateRenderAdjustFacet.class);
        assertNotNull(facet);
        assertThat(facet.getDateRenderAdjustDays(), is(adjustDays));
    }

    private void assertDateFormatStyle(
            final FacetedMethod facetedMethod, final FormatStyle formatStyle) {
        final DateFormatStyleFacet facet = facetedMethod.getFacet(DateFormatStyleFacet.class);
        assertNotNull(facet);
        assertThat(facet.getDateFormatStyle(), is(formatStyle));
    }

    private void assertTimeFormatStyle(
            final FacetedMethod facetedMethod, final FormatStyle formatStyle) {
        final TimeFormatStyleFacet facet = facetedMethod.getFacet(TimeFormatStyleFacet.class);
        assertNotNull(facet);
        assertThat(facet.getTimeFormatStyle(), is(formatStyle));
    }

    private void assertTimeZoneTranslation(
            final FacetedMethod facetedMethod, final TimeZoneTranslation timeZoneTranslation) {
        final TimeZoneTranslationFacet facet = facetedMethod.getFacet(TimeZoneTranslationFacet.class);
        assertNotNull(facet);
        assertThat(facet.getTimeZoneTranslation(), is(timeZoneTranslation));
    }

}
