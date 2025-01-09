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
package org.apache.causeway.viewer.wicket.ui.test.components.scalars.jdkmath;

import java.math.BigDecimal;
import java.util.Locale;

import jakarta.validation.constraints.Digits;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.core.metamodel.commons.ViewOrEditMode;
import org.apache.causeway.core.metamodel.valuesemantics.BigDecimalValueSemantics;
import org.apache.causeway.viewer.wicket.model.value.ConverterBasedOnValueSemantics;
import org.apache.causeway.viewer.wicket.ui.test.components.scalars.ConverterTester;

import lombok.Getter;
import lombok.Setter;

class BigDecimalConverterTest {

    final BigDecimal bd_123_45_scale2 = new BigDecimal("123.45").setScale(2);
    final BigDecimal bd_123_4500_scale2 = new BigDecimal("123.4500").setScale(2);

    final BigDecimal bd_789123_45_scale2 = new BigDecimal("789123.45").setScale(2);

    final BigDecimal bd_123_45_scale4 = new BigDecimal("123.45").setScale(4);
    final BigDecimal bd_123_4500_scale4 = new BigDecimal("123.4500").setScale(4);

    private ConverterTester<BigDecimal> converterTester;

    @BeforeEach
    void setUp() throws Exception {
        this.converterTester = new ConverterTester<>(BigDecimal.class, new BigDecimalValueSemantics());
    }

    @Test
    void scale2_english() {
        converterTester.setScenario(Locale.ENGLISH, newConverter(CustomerScale2.class));
        converterTester.assertRoundtrip(bd_123_45_scale2, "123.45");
    }

    @Test
    void scale4_english() {
        converterTester.setScenario(Locale.ENGLISH, newConverter(CustomerScale4.class));
        converterTester.assertRoundtrip(bd_123_45_scale4, "123.4500", "123.45");    // the 2nd value is the bigdecimal as rendered for parsing; by default we do not preserve fractional digits/scale
    }

    @Test
    void scale4_english_preserve_scale() {
        converterTester.getConfigurationForBigDecimalValueType().getEditing().setPreserveScale(true);
        converterTester.setScenario(Locale.ENGLISH, newConverter(CustomerScale4.class));
        converterTester.assertRoundtrip(bd_123_45_scale4, "123.4500", "123.4500");
    }

    @Test
    void scaleNull_english() {
        converterTester.setScenario(Locale.ENGLISH, newConverter(CustomerScaleNone.class));
        converterTester.assertRoundtrip(bd_123_45_scale4, "123.45");

        converterTester.setScenario(Locale.ENGLISH, newConverter(CustomerScaleNone.class));
        converterTester.assertRoundtrip(bd_123_45_scale4, "123.4500", "123.45");
    }

    @Test
    void scale2_italian() {
        converterTester.setScenario(Locale.ITALIAN, newConverter(CustomerScaleNone.class));
        converterTester.assertRoundtrip(bd_123_45_scale4, "123,45");
    }

    @Test
    void scale2_english_withThousandSeparators_not_allowed() {
        assertThat(converterTester.getConfigurationForBigDecimalValueType().isUseGroupingSeparator()).isFalse();

        converterTester.setScenario(Locale.ENGLISH, newConverter(CustomerScale2.class));
        converterTester.assertConversionFailure("789,123.45", "Invalid value '789,123.45'; do not use the ',' grouping separator");
    }

    @Test
    void scale2_english_withThousandSeparators_allowed() {
        converterTester.getConfigurationForBigDecimalValueType().setUseGroupingSeparator(true);
        assertThat(converterTester.getConfigurationForBigDecimalValueType().isUseGroupingSeparator()).isTrue();

        converterTester.setScenario(Locale.ENGLISH, newConverter(CustomerScale2.class));
        converterTester.assertRoundtrip(bd_789123_45_scale2, "789123.45");
    }

    @Test
    void scale2_english_withoutThousandSeparators() {
        converterTester.setScenario(Locale.ENGLISH, newConverter(CustomerScale2.class));
        converterTester.assertRoundtrip(bd_789123_45_scale2, "789123.45", "789123.45");
    }

    @Test
    void scale2_english_tooLargeScale() {
        converterTester.setScenario(Locale.ENGLISH, newConverter(CustomerScale2.class));
        converterTester.assertConversionFailure("123.454",
                "No more than 2 digits can be entered after the decimal separator, got 3 in '123.454'.");
    }

    @Test
    void when_null() {
        converterTester.setScenario(Locale.ENGLISH, newConverter(CustomerScale2.class));
        converterTester.assertHandlesEmpty();
    }

    @Test
    void invalid() {
        converterTester.setScenario(Locale.ENGLISH, newConverter(CustomerScale2.class));
        converterTester.assertConversionFailure("junk", "Not a decimal value 'junk': could not parse input='junk'");
    }

    // -- SCENARIOS

    @DomainObject
    static class CustomerScaleNone {
        @Property @Getter @Setter
        private BigDecimal value;
    }

    @DomainObject
    static class CustomerScale2 {
        @Property @Getter @Setter
        @Digits(fraction = 2, integer = 20)
        private BigDecimal value;
    }

    @DomainObject
    static class CustomerScale4 {
        @Property @Getter @Setter
        @Digits(fraction = 4, integer = 20)
        private BigDecimal value;
    }

    private ConverterBasedOnValueSemantics<BigDecimal> newConverter(final Class<?> type) {
        return converterTester.converterForProperty(type, "value", ViewOrEditMode.EDITING);
    }

}
