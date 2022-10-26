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
package org.apache.causeway.core.config.progmodel;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.DateTimeFormat;

class ProgrammingModelConstantsTest {

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideTimeCandidates")
    void canonincalDateTimeFormatRoundTrip(
            final String displayName,
            final OffsetDateTime dateTime,
            final String representation,
            final String parsable) {

        assertEquals(
                representation,
                DateTimeFormat.CANONICAL.formatDateTime(dateTime));

        assertEquals(
                dateTime.toInstant(),
                DateTimeFormat.CANONICAL.parseDateTime(parsable).toInstant());
    }

    private static Stream<Arguments> provideTimeCandidates() {

        return Stream.of(
          Arguments.of(
                  "full form",
                  OffsetDateTime.of(2022, 1, 31, 14, 04, 33, 17_000_000, ZoneOffset.ofHoursMinutes(-3, -30)),
                  "2022-01-31 14:04:33.017 -03:30",
                  "2022-01-31 14:04:33.017 -03:30"),
          Arguments.of(
                  "no millis, no offset minutes",
                  OffsetDateTime.of(2022, 1, 31, 14, 04, 33, 0, ZoneOffset.ofHours(-3)),
                  "2022-01-31 14:04:33.000 -03:00",
                  "2022-01-31 14:04:33 -03"),
          Arguments.of(
                  "no millis, no offset",
                  OffsetDateTime.of(2022, 1, 31, 14, 04, 33, 0, ZoneOffset.UTC),
                  "2022-01-31 14:04:33.000 Z",
                  "2022-01-31 14:04:33 Z")
        );
    }

}
