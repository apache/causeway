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
package org.apache.causeway.core.metamodel.facets.value;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.causeway.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.valuesemantics.BigDecimalValueSemantics;

class BigDecimalValueSemanticsProviderTest
extends ValueSemanticsProviderAbstractTestCase<BigDecimal> {

    private BigDecimalValueSemantics value;
    private BigDecimal bigDecimal;

    enum Scenario {
        DEFAULT,
        NO_GROUPING,
        LOCALE_GROUPING_DISPLAY,
        LOCALE_GROUPING_ALL;

        BigDecimalValueSemantics valueSemantics(final CausewayConfiguration causewayConfiguration) {
            BigDecimalValueSemantics valSem = switch (this) {
                case DEFAULT -> new BigDecimalValueSemantics();
                case NO_GROUPING -> new BigDecimalValueSemantics.NoGrouping();
                case LOCALE_GROUPING_DISPLAY -> new BigDecimalValueSemantics.LocaleGroupingDisplay();
                case LOCALE_GROUPING_ALL -> new BigDecimalValueSemantics.LocaleGroupingAll();
            };
            valSem.setCausewayConfiguration(causewayConfiguration);
            return valSem;
        }
    }

    @BeforeEach
    void setUpObjects() {
        bigDecimal = new BigDecimal("34132.199");
        allowMockAdapterToReturn(bigDecimal);

        setSemantics(value = Scenario.DEFAULT.valueSemantics(causewayConfiguration));
    }

    @Test
    void parseValidString() {
        final Object newValue = value.parseTextRepresentation(null, "2142342334");
        assertEquals(new BigDecimal(2142342334L), newValue);
    }

    @Test
    void parseInvalidString() {
        assertThrows(TextEntryParseException.class, ()->value.parseTextRepresentation(null, "214xxx2342334"));
    }

    @Test
    void parseInvalidStringWithGroupingSeparator() {
        assertThrows(TextEntryParseException.class, ()->value.parseTextRepresentation(null, "123,999.01"));
    }

    @Test
    void parseValidStringWithGroupingSeparatorIfAllowed() {
        setSemantics(value = Scenario.LOCALE_GROUPING_ALL.valueSemantics(causewayConfiguration));

        assertThat(value.parseTextRepresentation(null, "123,999.01"))
            .isEqualTo(new BigDecimal("123999.01").setScale(2, RoundingMode.HALF_EVEN));
    }

    @Test
    void parseInvalidStringWithGroupingSeparatorIfAllowed() {
        setSemantics(value = Scenario.LOCALE_GROUPING_ALL.valueSemantics(causewayConfiguration));

        // valid cases should pass, that is, use separators consistently or not at all
        assertThat(value.parseTextRepresentation(null, "1234567")).isEqualTo(new BigDecimal(1234567));
        assertThat(value.parseTextRepresentation(null, "1234567.0")).isEqualTo(new BigDecimal(1234567).setScale(1, RoundingMode.HALF_EVEN));
        assertThat(value.parseTextRepresentation(null, "1,234,567")).isEqualTo(new BigDecimal(1234567));
        assertThat(value.parseTextRepresentation(null, "1,234,567.0")).isEqualTo(new BigDecimal(1234567).setScale(1, RoundingMode.HALF_EVEN));

        // inconsistent use of separators
        assertThrows(TextEntryParseException.class, ()->value.parseTextRepresentation(null, "239,99"));
        assertThrows(TextEntryParseException.class, ()->value.parseTextRepresentation(null, "239,99.0"));
        assertThrows(TextEntryParseException.class, ()->value.parseTextRepresentation(null, "1234,567"));
        assertThrows(TextEntryParseException.class, ()->value.parseTextRepresentation(null, "1234,567.0"));
        assertThrows(TextEntryParseException.class, ()->value.parseTextRepresentation(null, "1,234567"));
        assertThrows(TextEntryParseException.class, ()->value.parseTextRepresentation(null, "1,234567.0"));
    }

    @Test
    void parseValidStringWithNoGroupingSeparator() {
        value.parseTextRepresentation(null, "123999.01");
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    void title(final Scenario scenario) {
        setSemantics(value = scenario.valueSemantics(causewayConfiguration));
        var actual = value.titlePresentation(null, bigDecimal);
        switch (scenario) {
            case DEFAULT -> assertEquals("34 132.199", actual);
            case NO_GROUPING -> assertEquals("34132.199", actual);
            case LOCALE_GROUPING_DISPLAY -> assertEquals("34,132.199", actual);
            case LOCALE_GROUPING_ALL -> assertEquals("34,132.199", actual);
        }
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    void html(final Scenario scenario) {
        setSemantics(value = scenario.valueSemantics(causewayConfiguration));
        var actual = value.htmlPresentation(null, bigDecimal);
        switch (scenario) {
            case DEFAULT -> assertEquals("""
                    <span class="fw-light">34&#8239;132.199</span>""", actual);
            case NO_GROUPING -> assertEquals("""
                    <span class="fw-light">34132.199</span>""", actual);
            case LOCALE_GROUPING_DISPLAY -> assertEquals("""
                    <span class="fw-light">34,132.199</span>""", actual);
            case LOCALE_GROUPING_ALL -> assertEquals("""
                    <span class="fw-light">34,132.199</span>""", actual);
        }
    }

    @Override
    protected BigDecimal getSample() {
        return bigDecimal;
    }

    @Override
    protected void assertValueEncodesToJsonAs(final BigDecimal a, final String json) {
        assertEquals("34132.199", json);
    }

}
