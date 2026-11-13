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
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR  CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.viewer.graphql.model.types;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

import graphql.GraphQLContext;
import graphql.schema.CoercingParseValueException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GraphQLValueScalarsTest {

    private static final GraphQLContext GRAPHQL_CONTEXT = GraphQLContext.newContext().build();

    @Test
    void localDateTimePreservesFractionalPrecision() {
        var scalar = GraphQLValueScalars.LOCAL_DATE_TIME.getCoercing();
        var parsed = scalar.parseValue("2026-08-18T09:10:11.123456789", GRAPHQL_CONTEXT, Locale.ROOT);
        assertEquals(LocalDateTime.of(2026, 8, 18, 9, 10, 11, 123_456_789), parsed);
        assertEquals("2026-08-18T09:10:11.123456789", scalar.serialize(parsed, GRAPHQL_CONTEXT, Locale.ROOT));
    }

    @Test
    void legacyDateUsesUtcInstant() {
        var scalar = GraphQLValueScalars.LEGACY_DATE_TIME.getCoercing();
        var parsed = scalar.parseValue("2026-08-18T08:10:11.123Z", GRAPHQL_CONTEXT, Locale.ROOT);
        assertEquals(Date.from(Instant.parse("2026-08-18T08:10:11.123Z")), parsed);
        assertEquals("2026-08-18T08:10:11.123Z", scalar.serialize(parsed, GRAPHQL_CONTEXT, Locale.ROOT));
    }

    @Test
    void sqlDateTimeAndTimestampRetainTheirDistinctSemantics() {
        var dateScalar = GraphQLValueScalars.SQL_DATE.getCoercing();
        var timeScalar = GraphQLValueScalars.SQL_TIME.getCoercing();
        var timestampScalar = GraphQLValueScalars.SQL_TIMESTAMP.getCoercing();

        var date = dateScalar.parseValue("2026-08-18", GRAPHQL_CONTEXT, Locale.ROOT);
        var time = timeScalar.parseValue("09:10:11", GRAPHQL_CONTEXT, Locale.ROOT);
        var timestamp = timestampScalar.parseValue(
                "2026-08-18T09:10:11.123456789",
                GRAPHQL_CONTEXT,
                Locale.ROOT);

        assertEquals(java.sql.Date.valueOf("2026-08-18"), date);
        assertEquals(Time.valueOf("09:10:11"), time);
        assertEquals(Timestamp.valueOf("2026-08-18 09:10:11.123456789"), timestamp);
        assertEquals("2026-08-18", dateScalar.serialize(date, GRAPHQL_CONTEXT, Locale.ROOT));
        assertEquals("09:10:11", timeScalar.serialize(time, GRAPHQL_CONTEXT, Locale.ROOT));
        assertThrows(
                CoercingParseValueException.class,
                () -> timeScalar.parseValue("09:10:11.123", GRAPHQL_CONTEXT, Locale.ROOT));
        assertEquals(
                "2026-08-18T09:10:11.123456789",
                timestampScalar.serialize(timestamp, GRAPHQL_CONTEXT, Locale.ROOT));
    }

    @Test
    void localeUsesCanonicalLanguageTags() {
        var scalar = GraphQLValueScalars.LOCALE.getCoercing();
        var parsed = scalar.parseValue("en-GB", GRAPHQL_CONTEXT, Locale.ROOT);
        assertEquals(Locale.UK, parsed);
        assertEquals("en-GB", scalar.serialize(parsed, GRAPHQL_CONTEXT, Locale.ROOT));
    }

    @Test
    void unsupportedOutputIsConstantAndDoesNotUseTheValueString() {
        var scalar = GraphQLValueScalars.UNSUPPORTED_VALUE.getCoercing();
        assertEquals(
                "[unsupported]",
                scalar.serialize("NEVER_DISCLOSE", GRAPHQL_CONTEXT, Locale.ROOT));
    }

    @Test
    void malformedAndUnsupportedInputProduceBoundedCoercionErrors() {
        var malformed = assertThrows(
                CoercingParseValueException.class,
                () -> GraphQLValueScalars.LOCAL_DATE_TIME.getCoercing()
                        .parseValue("not-a-date", GRAPHQL_CONTEXT, Locale.ROOT));
        var unsupported = assertThrows(
                CoercingParseValueException.class,
                () -> GraphQLValueScalars.UNSUPPORTED_INPUT.getCoercing()
                        .parseValue("secret", GRAPHQL_CONTEXT, Locale.ROOT));

        assertEquals("Invalid LocalDateTime value", malformed.getMessage());
        assertFalse(unsupported.getMessage().contains("secret"));
    }
}
