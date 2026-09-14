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
package org.apache.causeway.viewer.graphql.model.marshallers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.value.Password;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScalarMarshallerBuiltInInputTest {

    @Test
    void exactNumericStringsBecomeDeclaredJavaTypesWithoutPrecisionLoss() {
        final var decimal = new ScalarMarshallerBigDecimal(null)
                .unmarshal("9007199254740993.1200", BigDecimal.class);
        final var integer = new ScalarMarshallerBigInteger(null)
                .unmarshal("123456789012345678901234567890", BigInteger.class);

        assertThat(decimal).isEqualByComparingTo(new BigDecimal("9007199254740993.1200"));
        assertThat(decimal.scale()).isEqualTo(4);
        assertThat(integer).isEqualTo(new BigInteger("123456789012345678901234567890"));
        assertThatThrownBy(() -> new ScalarMarshallerBigDecimal(null).unmarshal("1.2.3", BigDecimal.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid BigDecimal value");
    }

    @Test
    void nullableBooleanAndProtectedValuesPreserveTheirDeclaredTypes() {
        assertThat(new ScalarMarshallerBooleanWrapper(null).unmarshal(null, Boolean.class)).isNull();
        assertThat(new ScalarMarshallerBooleanWrapper(null).unmarshal(Boolean.FALSE, Boolean.class)).isFalse();
        assertThat(new ScalarMarshallerPassword(null).unmarshal("new secret", Password.class))
                .isInstanceOf(Password.class);
    }

    @Test
    void localOffsetAndZonedTemporalValuesPreservePrecisionOffsetAndZone() {
        final var local = new ScalarMarshallerJdk8LocalDateTime(null)
                .unmarshal("2026-08-23T10:15:30.123456789", LocalDateTime.class);
        final var offsetDateTime = OffsetDateTime.parse("2026-08-23T10:15:30.123456789-04:00");
        final var offsetTime = OffsetTime.parse("10:15:30.123456789+05:30");
        final var zoned = ScalarMarshallerJdk8ZonedDateTime.parse(
                "2026-11-01T01:30:00-04:00[America/New_York]",
                "yyyy-MM-dd'T'HH:mm:ssXXX");

        assertThat(local.getNano()).isEqualTo(123456789);
        assertThat(new ScalarMarshallerJdk8OffsetDateTime(null)
                .unmarshal(offsetDateTime, OffsetDateTime.class)).isEqualTo(offsetDateTime);
        assertThat(new ScalarMarshallerJdk8OffsetTime(null)
                .unmarshal(offsetTime, OffsetTime.class)).isEqualTo(offsetTime);
        assertThat(zoned.getZone()).isEqualTo(ZoneId.of("America/New_York"));
        assertThat(zoned.getOffset().getId()).isEqualTo("-04:00");
        assertThat(ScalarMarshallerJdk8ZonedDateTime.parse(
                "2026-08-23T10:15:30+02:00",
                "yyyy-MM-dd'T'HH:mm:ssXXX").getOffset().getId()).isEqualTo("+02:00");
    }

    @Test
    void urlInputIsTypedAndRejectsMalformedValues() throws Exception {
        final URL value = new ScalarMarshallerUrl(null).unmarshal("https://causeway.apache.org/", URL.class);

        assertThat(value).isEqualTo(new URL("https://causeway.apache.org/"));
        assertThatThrownBy(() -> new ScalarMarshallerUrl(null).unmarshal("not a URL", URL.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid URL value");
    }
}
