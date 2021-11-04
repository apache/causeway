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
package org.apache.isis.viewer.wicket.ui.components.scalars.jdkmath;

import java.math.BigDecimal;
import java.util.Locale;

import javax.validation.constraints.Digits;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.core.config.valuetypes.ValueSemanticsRegistry;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.valuesemantics.BigDecimalValueSemantics;
import org.apache.isis.core.security._testing.InteractionService_forTesting;
import org.apache.isis.viewer.wicket.ui.components.scalars.ConverterBasedOnValueSemantics;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

class BigDecimalConverter_roundtrip {

    final BigDecimal bd_123_45_scale2 = new BigDecimal("123.45").setScale(2);
    final BigDecimal bd_123_4500_scale2 = new BigDecimal("123.4500").setScale(2);

    final BigDecimal bd_789123_45_scale2 = new BigDecimal("789123.45").setScale(2);

    final BigDecimal bd_123_45_scale4 = new BigDecimal("123.45").setScale(4);
    final BigDecimal bd_123_4500_scale4 = new BigDecimal("123.4500").setScale(4);

    private InteractionService interactionService;
    private MetaModelContext mmc;

    private static class BigDecimalConverterForFeature
    extends ConverterBasedOnValueSemantics<BigDecimal> {

        private static final long serialVersionUID = 1L;

        protected BigDecimalConverterForFeature(final ObjectFeature propOrParam) {
            super(propOrParam);
        }
    }

    @BeforeEach
    void setUp() throws Exception {

        BigDecimalValueSemantics valueSemantics;
        mmc = MetaModelContext_forTesting.builder()
                .valueSemantic(valueSemantics = new BigDecimalValueSemantics())
                .interactionProvider(interactionService = new InteractionService_forTesting())
                .build();
        valueSemantics.setSpecificationLoader(mmc.getSpecificationLoader());

        // pre-requisites for testing
        val reg = mmc.getServiceRegistry().lookupServiceElseFail(ValueSemanticsRegistry.class);
        assertNotNull(reg.selectValueSemantics(BigDecimal.class));
        assertTrue(reg.selectValueSemantics(BigDecimal.class).isNotEmpty());
        assertNotNull(mmc.getServiceRegistry().lookupServiceElseFail(InteractionService.class));
        assertNotNull(mmc.getInteractionProvider());
    }

    @Test
    void scale2_english() {
        assertRoundtrip(CustomerScale2.class, bd_123_45_scale2, "123.45", "123.45", Locale.ENGLISH);
    }

    @Test @Disabled //FIXME[ISIS-2741] scale not picked up yet
    void scale4_english() {
        assertRoundtrip(CustomerScale4.class, bd_123_45_scale4, "123.4500", "123.4500", Locale.ENGLISH);
    }

    @Test @Disabled //FIXME[ISIS-2741] scale not picked up yet
    void scaleNull_english() {
        assertRoundtrip(Customer.class, bd_123_45_scale2, "123.45", "123.45", Locale.ENGLISH);
        assertRoundtrip(Customer.class, bd_123_45_scale4, "123.4500", "123.4500", Locale.ENGLISH);
    }

    @Test
    void scale2_italian() {
        assertRoundtrip(Customer.class, bd_123_45_scale2, "123,45", "123,45", Locale.ITALIAN);
    }

    @Test
    void scale2_english_withThousandSeparators() {
        assertRoundtrip(CustomerScale2.class, bd_789123_45_scale2, "789,123.45", "789,123.45", Locale.ENGLISH);
    }

    @Test
    void scale2_english_withoutThousandSeparators() {
        assertRoundtrip(CustomerScale2.class, bd_789123_45_scale2, "789123.45", "789,123.45", Locale.ENGLISH);
    }

    @Test
    void scale2_english_tooLargeScale() {
        assertParseError(CustomerScale2.class, "123.454", Locale.ENGLISH,
                "No more than 2 fraction digits can be entered, got 3 in '123.454'.");
    }

    // -- HELPER

    private void assertRoundtrip(final Class<?> type, final BigDecimal expected, final String valueAsText, final String expectedText, final Locale locale) {

        val converter = newConverter(type);

        interactionService.run(InteractionContext.builder()
                .locale(locale)
                .build(), ()->{

                    // when
                    final BigDecimal actual = converter.convertToObject(valueAsText, null);
                    assertNumberEquals(expected, actual);

                    // when
                    final String actualStr = converter.convertToString(actual, null);
                    assertEquals(expectedText, actualStr);

                });
    }

    private void assertParseError(final Class<?> type, final String valueAsText, final Locale locale, final String expectedMessage) {

        val converter = newConverter(type);

        interactionService.run(InteractionContext.builder()
                .locale(locale)
                .build(), ()->{
                    // when
                    val ex = assertThrows(TextEntryParseException.class, ()->
                        converter.convertToObject(valueAsText, null));
                    assertEquals(expectedMessage, ex.getMessage());
                });
    }

    private static void assertNumberEquals(final BigDecimal a, final BigDecimal b) {
        assertEquals(a, b);
    }

    // -- SCENARIOS

    @DomainObject
    static class Customer {
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

    private BigDecimalConverterForFeature newConverter(final Class<?> type) {
        val customerSpec = mmc.getSpecificationLoader().specForTypeElseFail(type);
        val prop = customerSpec.getPropertyElseFail("value");
        return new BigDecimalConverterForFeature(prop);
    }

}
