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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.causeway.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.causeway.core.metamodel.valuesemantics.BigDecimalValueSemantics;

class BigDecimalValueSemanticsProviderTest
extends ValueSemanticsProviderAbstractTestCase<BigDecimal> {

    private BigDecimalValueSemantics value;
    private BigDecimal bigDecimal;

    @BeforeEach
    void setUpObjects() throws Exception {
        bigDecimal = new BigDecimal("34132.199");
        allowMockAdapterToReturn(bigDecimal);

        BigDecimalValueSemantics valueSemantics = new BigDecimalValueSemantics();
        valueSemantics.setCausewayConfiguration(causewayConfiguration);
        setSemantics(value = valueSemantics);
    }

    @Test
    void parseValidString() throws Exception {
        final Object newValue = value.parseTextRepresentation(null, "2142342334");
        assertEquals(new BigDecimal(2142342334L), newValue);
    }

    @Test
    void parseInvalidString() throws Exception {
        try {
            value.parseTextRepresentation(null, "214xxx2342334");
            fail();
        } catch (final TextEntryParseException expected) {
        }
    }

    @Test
    void parseInvalidStringWithGroupingSeparator() throws Exception {
        try {
            value.parseTextRepresentation(null, "123,999.01");
            fail();
        } catch (final TextEntryParseException expected) {
        }
    }

    @Test
    void parseValidStringWithGroupingSeparatorIfConfiguredToAllow() throws Exception {
        causewayConfiguration.getValueTypes().getBigDecimal().getEditing().setUseGroupingSeparator(true);

        BigDecimal bd = value.parseTextRepresentation(null, "123,999.01");

        assertThat(bd).isEqualTo(new BigDecimal("123999.01").setScale(2, RoundingMode.HALF_EVEN));
    }

    @Test
    void demonstrateTheRiskOfAllowingGroupingSeparatorIfConfiguredToAllow() throws Exception {

        // default disallows grouping separator
        try {
            value.parseTextRepresentation(null, "1239,99");
            fail();
        } catch (final TextEntryParseException expected) {
        }

        // but if we allow it...
        causewayConfiguration.getValueTypes().getBigDecimal().getEditing().setUseGroupingSeparator(true);

        BigDecimal bigDecimal = value.parseTextRepresentation(null, "1239,99");
        assertThat(bigDecimal).isEqualTo(new BigDecimal(123999));
    }

    @Test
    void parseValidStringWithNoGroupingSeparator() throws Exception {
        value.parseTextRepresentation(null, "123999.01");
    }

    @Test
    void titleOf() {
        assertEquals("34,132.199", value.titlePresentation(null, bigDecimal));
    }

    @Test
    void titleOfWhenUseGroupingSeparator() {
        causewayConfiguration.getValueTypes().getBigDecimal().setUseGroupingSeparator(true);

        assertEquals("34,132.199", value.titlePresentation(null, bigDecimal));
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
