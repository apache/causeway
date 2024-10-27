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
package org.apache.causeway.viewer.wicket.ui.test.components.scalars;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

import org.apache.wicket.util.convert.ConversionException;
import org.assertj.core.util.Arrays;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.clock.VirtualClock;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.locale.UserLocale;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.applib.value.semantics.ValueSemanticsResolver;
import org.apache.causeway.commons.functional.ThrowingRunnable;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.commons.ViewOrEditMode;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.security._testing.InteractionService_forTesting;
import org.apache.causeway.viewer.wicket.model.value.ConverterBasedOnValueSemantics;

import lombok.AllArgsConstructor;
import lombok.NonNull;

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
                .interactionService(interactionService = new InteractionService_forTesting())
                .memberExecutor(Mockito.mock(MemberExecutorService.class))
                .build();

        mmc.getServiceInjector().injectServicesInto(valueSemantics);

        // pre-requisites for testing
        var identifier = Identifier.classIdentifier(LogicalType.fqcn(valueType));
        var reg = mmc.getServiceRegistry().lookupServiceElseFail(ValueSemanticsResolver.class);
        assertNotNull(reg.selectValueSemantics(identifier, valueType));
        assertTrue(reg.selectValueSemantics(identifier, valueType).isNotEmpty());
        assertNotNull(mmc.getServiceRegistry().lookupServiceElseFail(InteractionService.class));
        assertNotNull(mmc.getInteractionService());
    }

    public void setScenario(
            final @NonNull Locale locale,
            final @NonNull ConverterBasedOnValueSemantics<T> converter) {
        this.scenario = new Scenario(locale, converter);
    }

    public ConverterBasedOnValueSemantics<T> converterForProperty(
            final Class<?> type,
            final String propertyId,
            final ViewOrEditMode representation) {
        var customerSpec = mmc.getSpecificationLoader().specForTypeElseFail(type);
        var prop = customerSpec.getPropertyElseFail("value");
        var propType = (Class<T>) prop.getElementType().getCorrespondingClass();
        return new ConverterBasedOnValueSemantics<T>(propType, prop, representation);
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

    public CausewayConfiguration.ValueTypes.BigDecimal getConfigurationForBigDecimalValueType() {
        return mmc.getConfiguration().getValueTypes().getBigDecimal();
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

            var parsedValue = scenario.converter.convertToObject(valueAsText, LOCALE_NOT_USED);

            if(value instanceof BigDecimal) {
                _Assert.assertNumberEquals(
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
            var ex = assertThrows(ConversionException.class, //TODO find a more generic exception type
                    ()->scenario.converter.convertToObject(valueAsInvalidText, LOCALE_NOT_USED));
            assertEquals(expectedMessage, ex.getMessage());
        });
    }

    // -- HELPER

    @SuppressWarnings("deprecation")
    private void assertTemporalEquals(final java.util.Date a, final java.util.Date b) {
        assertEquals(a.getDay(), b.getDay());
        assertEquals(a.getMonth(), b.getMonth());
        assertEquals(a.getYear(), b.getYear());
        assertEquals(a.getTime(), b.getTime());
    }

}
