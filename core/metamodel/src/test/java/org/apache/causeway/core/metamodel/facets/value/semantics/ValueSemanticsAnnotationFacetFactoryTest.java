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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.TimeZoneTranslation;
import org.apache.causeway.applib.annotation.ValueSemantics;
import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.objectvalue.daterenderedadjust.DateRenderAdjustFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MaxFractionalDigitsFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MaxTotalDigitsFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MinFractionalDigitsFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MinIntegerDigitsFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.temporalformat.DateFormatStyleFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.temporalformat.TimeFormatStyleFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.temporalformat.TimeZoneTranslationFacet;

@SuppressWarnings("unused")
class ValueSemanticsAnnotationFacetFactoryTest
extends AbstractFacetFactoryTest {

    // -- MAX TOTAL DIGITS

    public void testMaxTotalPickedUpOnProperty() {
        // given
        class Order {
            @ValueSemantics(maxTotalDigits = 5)
            public BigDecimal getCost() { return null; }
        }
        // when
        processMethod(newFacetFactory(), Order.class, "getCost", _Constants.emptyClasses);
        // then
        assertMaxTotalDigits(facetedMethod, 5);
        assertDefaultMinIntegerDigits(facetedMethod);
        assertDefaultMaxFractionalDigits(facetedMethod);
        assertDefaultMinFractionalDigits(facetedMethod);
    }

    public void testMaxTotalPickedUpOnActionParameter() {
        // given
        class Order {
            public void updateCost(
                    @ValueSemantics(maxTotalDigits = 5)
                    final BigDecimal cost) { }
        }
        // when
        processParams(newFacetFactory(), Order.class, "updateCost", new Class[] { BigDecimal.class });
        // then
        assertMaxTotalDigits(facetedMethodParameter, 5);
        assertDefaultMinIntegerDigits(facetedMethodParameter);
        assertDefaultMaxFractionalDigits(facetedMethodParameter);
        assertDefaultMinFractionalDigits(facetedMethodParameter);
    }

    // -- MIN INTEGER DIGITS

    public void testMinIntegerPickedUpOnProperty() {
        // given
        class Order {
            @ValueSemantics(minIntegerDigits = 5)
            public BigDecimal getCost() { return null; }
        }
        // when
        processMethod(newFacetFactory(), Order.class, "getCost", _Constants.emptyClasses);
        // then
        assertDefaultMaxTotalDigits(facetedMethod);
        assertMinIntegerDigits(facetedMethod, 5);
        assertDefaultMaxFractionalDigits(facetedMethod);
        assertDefaultMinFractionalDigits(facetedMethod);
    }

    public void testMinIntegerPickedUpOnActionParameter() {
        // given
        class Order {
            public void updateCost(
                    @ValueSemantics(minIntegerDigits = 5)
                    final BigDecimal cost) { }
        }
        // when
        processParams(newFacetFactory(), Order.class, "updateCost", new Class[] { BigDecimal.class });
        // then
        assertDefaultMaxTotalDigits(facetedMethodParameter);
        assertMinIntegerDigits(facetedMethodParameter, 5);
        assertDefaultMaxFractionalDigits(facetedMethodParameter);
        assertDefaultMinFractionalDigits(facetedMethodParameter);
    }

    // -- MAX FRACTIONAL DIGITS

    public void testMaxFracionalPickedUpOnProperty() {
        // given
        class Order {
            @ValueSemantics(maxFractionalDigits = 5)
            public BigDecimal getCost() { return null; }
        }
        // when
        processMethod(newFacetFactory(), Order.class, "getCost", _Constants.emptyClasses);
        // then
        assertDefaultMaxTotalDigits(facetedMethod);
        assertDefaultMinIntegerDigits(facetedMethod);
        assertMaxFractionalDigits(facetedMethod, 5);
        assertDefaultMinFractionalDigits(facetedMethod);
    }

    public void testMaxFracionalPickedUpOnActionParameter() {
        // given
        class Order {
            public void updateCost(
                    @ValueSemantics(maxFractionalDigits = 5)
                    final BigDecimal cost) { }
        }
        // when
        processParams(newFacetFactory(), Order.class, "updateCost", new Class[] { BigDecimal.class });
        // then
        assertDefaultMaxTotalDigits(facetedMethodParameter);
        assertDefaultMinIntegerDigits(facetedMethodParameter);
        assertMaxFractionalDigits(facetedMethodParameter, 5);
        assertDefaultMinFractionalDigits(facetedMethodParameter);
    }

    // -- MIN FRACTIONAL DIGITS

    public void testMinFracionalPickedUpOnProperty() {
        // given
        class Order {
            @ValueSemantics(minFractionalDigits = 5)
            public BigDecimal getCost() { return null; }
        }
        // when
        processMethod(newFacetFactory(), Order.class, "getCost", _Constants.emptyClasses);
        // then
        assertDefaultMaxTotalDigits(facetedMethod);
        assertDefaultMinIntegerDigits(facetedMethod);
        assertDefaultMaxFractionalDigits(facetedMethod);
        assertMinFractionalDigits(facetedMethod, 5);
    }

    public void testMinFracionalPickedUpOnActionParameter() {
        // given
        class Order {
            public void updateCost(
                    @ValueSemantics(minFractionalDigits = 5)
                    final BigDecimal cost) { }
        }
        // when
        processParams(newFacetFactory(), Order.class, "updateCost", new Class[] { BigDecimal.class });
        // then
        assertDefaultMaxTotalDigits(facetedMethodParameter);
        assertDefaultMinIntegerDigits(facetedMethodParameter);
        assertDefaultMaxFractionalDigits(facetedMethodParameter);
        assertMinFractionalDigits(facetedMethodParameter, 5);
    }

    // -- DIGITS ANNOTATION

    public void testDigitsAnnotationPickedUpOnProperty() {
        // given
        class Order {
            @javax.validation.constraints.Digits(integer=14, fraction=4)
            public BigDecimal getCost() { return null; }
        }
        // when
        processMethod(newFacetFactory(), Order.class, "getCost", _Constants.emptyClasses);
        // then
        assertDigitsFacets(facetedMethod, 18, 4);
    }

    public void testDigitsAnnotationPickedUpOnActionParameter() {
        // given
        class Order {
            public void updateCost(
                    @javax.validation.constraints.Digits(integer=14, fraction=4)
                    final BigDecimal cost) { }
        }
        // when
        processParams(newFacetFactory(), Order.class, "updateCost", new Class[] { BigDecimal.class });
        // then
        assertDigitsFacets(facetedMethodParameter, 18, 4);
    }

    // -- CONSTRAINT MERGERS

    public void testMultipleAnnotationsMergedOnProperty() {
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

        // when
        processMethod(newFacetFactory(), Order.class, "maxTotalA", _Constants.emptyClasses);
        // then - lowest bound wins
        assertMaxTotalDigits(facetedMethod, 18);

        // when
        processMethod(newFacetFactory(), Order.class, "maxTotalB", _Constants.emptyClasses);
        // then - lowest bound wins
        assertMaxTotalDigits(facetedMethod, 17);

        // when
        processMethod(newFacetFactory(), Order.class, "maxFracA", _Constants.emptyClasses);
        // then - lowest bound wins
        assertMaxFractionalDigits(facetedMethod, 4);

        // when
        processMethod(newFacetFactory(), Order.class, "maxFracB", _Constants.emptyClasses);
        // then - lowest bound wins
        assertMaxFractionalDigits(facetedMethod, 4);
    }

    // -- TEMPORAL FORMAT STYLE

    public void testDateAdjustPickedUpOnProperty() {
        // given
        class Order {
            @ValueSemantics(dateRenderAdjustDays = ValueSemantics.AS_DAY_BEFORE)
            public LocalDateTime getDateTime() { return null; }
        }
        // when
        processMethod(newFacetFactory(), Order.class, "getDateTime", _Constants.emptyClasses);
        // then
        assertDateRenderAdjustDays(facetedMethod, -1);
    }

    public void testTimeZoneTranslationPickedUpOnProperty() {
        // given
        class Order {
            @ValueSemantics(timeZoneTranslation = TimeZoneTranslation.NONE)
            public LocalDateTime getDateTimeA() { return null; }

            @ValueSemantics(timeZoneTranslation = TimeZoneTranslation.TO_LOCAL_TIMEZONE)
            public LocalDateTime getDateTimeB() { return null; }

        }
        // when
        processMethod(newFacetFactory(), Order.class, "getDateTimeA", _Constants.emptyClasses);
        // then
        assertTimeZoneTranslation(facetedMethod, TimeZoneTranslation.NONE);

        // when
        processMethod(newFacetFactory(), Order.class, "getDateTimeB", _Constants.emptyClasses);
        // then
        assertTimeZoneTranslation(facetedMethod, TimeZoneTranslation.TO_LOCAL_TIMEZONE);
    }

    public void testDateFormatStylePickedUpOnProperty() {
        // given
        class Order {
            @ValueSemantics(dateFormatStyle = FormatStyle.FULL)
            public LocalDateTime getDateTime() { return null; }
        }
        // when
        processMethod(newFacetFactory(), Order.class, "getDateTime", _Constants.emptyClasses);
        // then
        assertDateFormatStyle(facetedMethod, FormatStyle.FULL);
    }

    public void testTimeFormatStylePickedUpOnProperty() {
        // given
        class Order {
            @ValueSemantics(timeFormatStyle = FormatStyle.FULL)
            public LocalDateTime getDateTime() { return null; }
        }
        // when
        processMethod(newFacetFactory(), Order.class, "getDateTime", _Constants.emptyClasses);
        // then
        assertTimeFormatStyle(facetedMethod, FormatStyle.FULL);
    }

    // -- HELPER

    ValueSemanticsAnnotationFacetFactory newFacetFactory() {
        return new ValueSemanticsAnnotationFacetFactory(metaModelContext);
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
