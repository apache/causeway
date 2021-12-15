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
package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

import org.apache.wicket.util.convert.ConversionException;
import org.assertj.core.util.Arrays;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.clock.VirtualClock;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.locale.UserLocale;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.applib.value.semantics.ValueSemanticsResolver;
import org.apache.isis.commons.functional.ThrowingRunnable;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.security._testing.InteractionService_forTesting;
import org.apache.isis.viewer.wicket.model.converter.ConverterBasedOnValueSemantics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;

public class ConverterTester<T extends Serializable> {

    private final Locale LOCALE_NOT_USED = null;

    private final InteractionService interactionService;
    private final MetaModelContext mmc;
    private Scenario scenario;

    @AllArgsConstructor
    private class Scenario {
        Locale locale;
        ConverterBasedOnValueSemantics<T> converter;
    }

    public ConverterTester(final Class<T> valueType, final ValueSemanticsAbstract<T> valueSemantics,
            final Object ...additionalSingletons) {
        this(valueType, valueSemantics, VirtualClock.frozenTestClock(), additionalSingletons);
    }

    public ConverterTester(
            final @NonNull Class<T> valueType,
            final @NonNull ValueSemanticsAbstract<T> valueSemantics,
            final @NonNull VirtualClock virtualClock,
            final Object ...additionalSingletons) {

        mmc = MetaModelContext_forTesting.builder()
                .valueSemantic(valueSemantics)
                .singleton(new ClockService(null) {
                    @Override public VirtualClock getClock() {
                        return virtualClock;
                    }
                })
                .singletons(Arrays.asList(additionalSingletons))
                .interactionProvider(interactionService = new InteractionService_forTesting())
                .build();

        mmc.getServiceInjector().injectServicesInto(valueSemantics);

        // pre-requisites for testing
        val identifier = Identifier.classIdentifier(LogicalType.fqcn(valueType));
        val reg = mmc.getServiceRegistry().lookupServiceElseFail(ValueSemanticsResolver.class);
        assertNotNull(reg.selectValueSemantics(identifier, valueType));
        assertTrue(reg.selectValueSemantics(identifier, valueType).isNotEmpty());
        assertNotNull(mmc.getServiceRegistry().lookupServiceElseFail(InteractionService.class));
        assertNotNull(mmc.getInteractionProvider());
    }

    public void setScenario(
            final @NonNull Locale locale,
            final @NonNull ConverterBasedOnValueSemantics<T> converter) {
        this.scenario = new Scenario(locale, converter);
    }

    public ConverterBasedOnValueSemantics<T> converterForProperty(
            final Class<?> type,
            final String propertyId,
            final ScalarRepresentation representation) {
        val customerSpec = mmc.getSpecificationLoader().specForTypeElseFail(type);
        val prop = customerSpec.getPropertyElseFail("value");
        return new ConverterBasedOnValueSemantics<>(prop, representation);
    }

    public void runWithInteraction(final @NonNull ThrowingRunnable runnable) {

        Objects.requireNonNull(scenario, "must select a scenario before using this method");

        interactionService.run(
                InteractionContext.builder().locale(UserLocale.valueOf(scenario.locale)).build(),
                runnable);
    }

    public void assertRoundtrip(
            final @NonNull T value,
            final @NonNull String valueAsText) {
        assertRoundtrip(value, valueAsText, valueAsText);
    }

    /**
     * @param value - non-null
     * @param valueAsText - parser input
     * @param expectedText - reversed parser output
     */
    public void assertRoundtrip(
            final @NonNull T value,
            final @NonNull String valueAsText,
            final @NonNull String expectedText) {
        runWithInteraction(()->{

            val parsedValue = scenario.converter.convertToObject(valueAsText, LOCALE_NOT_USED);

            if(value instanceof BigDecimal) {
                assertNumberEquals(
                        (BigDecimal)value, (BigDecimal)parsedValue);
            } else if(value instanceof java.util.Date) {
                assertTemporalEquals(
                        (java.util.Date)value, (java.util.Date)parsedValue);
            } else {
                assertEquals(
                        value, parsedValue);
            }

            assertEquals(
                    expectedText, scenario.converter.convertToString(value, LOCALE_NOT_USED));
        });
    }

    public void assertHandlesEmpty() {
        runWithInteraction(()->{
            assertNull(scenario.converter.convertToObject(null, LOCALE_NOT_USED));
            assertNull(scenario.converter.convertToObject("", LOCALE_NOT_USED));
            assertTrue(_Strings.isEmpty(scenario.converter.convertToString(null, LOCALE_NOT_USED)));
        });
    }

    public void assertConversionFailure(
            final @NonNull String valueAsInvalidText,
            final @NonNull String expectedMessage) {

        runWithInteraction(()->{
            val ex = assertThrows(ConversionException.class, //TODO find a more generic exception type
                    ()->scenario.converter.convertToObject(valueAsInvalidText, LOCALE_NOT_USED));
            assertEquals(expectedMessage, ex.getMessage());
        });
    }

    // -- HELPER

    private static void assertNumberEquals(final BigDecimal a, final BigDecimal b) {
        val maxScale = Math.max(a.scale(), b.scale());
        assertEquals(
                a.setScale(maxScale),
                b.setScale(maxScale));
    }

    @SuppressWarnings("deprecation")
    private void assertTemporalEquals(final java.util.Date a, final java.util.Date b) {
        assertEquals(a.getDay(), b.getDay());
        assertEquals(a.getMonth(), b.getMonth());
        assertEquals(a.getYear(), b.getYear());
        assertEquals(a.getTime(), b.getTime());
    }

}
