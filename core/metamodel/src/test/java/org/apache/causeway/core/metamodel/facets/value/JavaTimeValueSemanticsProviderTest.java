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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.causeway.applib.annotation.TimePrecision;
import org.apache.causeway.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.causeway.applib.locale.UserLocale;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.value.semantics.TemporalValueSemantics;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider.Context;
import org.apache.causeway.core.metamodel.valuesemantics.temporal.LocalDateTimeValueSemantics;
import org.apache.causeway.core.metamodel.valuesemantics.temporal.legacy.JavaUtilDateValueSemantics;

import lombok.NonNull;

/**
 * Testing the LocalDateTimeValueSemantics under the hood.
 */
class JavaTimeValueSemanticsProviderTest
extends ValueSemanticsProviderAbstractTestCase<java.util.Date> {

    @SuppressWarnings("deprecation")
    private final java.util.Date date = new java.util.Date(2013-1900, 03-1, 13, 17, 59, 03);
    private JavaUtilDateValueSemantics valueSemantics;

    @Test
    void invalidParse() {
        setSemantics(valueSemantics = createValueSemantics(TimePrecision.SECOND));

        assertThrows(TextEntryParseException.class, ()->{
            valueSemantics.parseTextRepresentation(null, "invalid entry");
        });
    }

    @Test
    void rendering() {
        setSemantics(valueSemantics = createValueSemantics(TimePrecision.SECOND));
        var _context = Context.of(null, InteractionContext.builder().locale(UserLocale.valueOf(Locale.ENGLISH)).build());
        assertEquals("Mar 13, 2013, 5:59:03 PM", valueSemantics.titlePresentation(_context , date));
    }

    @Test // support omitted parts on input
    void parseNoMinutes() {
        setSemantics(valueSemantics = createValueSemantics(TimePrecision.SECOND));
        var _context = Context.of(null, InteractionContext.builder().locale(UserLocale.valueOf(Locale.ENGLISH)).build());
        var parsedDate = valueSemantics.parseTextRepresentation(_context, "2013-03-13 17");
        assertEquals(date.getTime() - 3540_000L - 3000L, parsedDate.getTime());
    }

    @Test // support omitted parts on input
    void parseNoSeconds() {
        setSemantics(valueSemantics = createValueSemantics(TimePrecision.SECOND));
        var _context = Context.of(null, InteractionContext.builder().locale(UserLocale.valueOf(Locale.ENGLISH)).build());
        var parsedDate = valueSemantics.parseTextRepresentation(_context, "2013-03-13 17:59");
        assertEquals(date.getTime() - 3000L, parsedDate.getTime());
    }

    @Test
    void parseSeconds() {
        setSemantics(valueSemantics = createValueSemantics(TimePrecision.SECOND));
        var _context = Context.of(null, InteractionContext.builder().locale(UserLocale.valueOf(Locale.ENGLISH)).build());
        var parsedDate = valueSemantics.parseTextRepresentation(_context, "2013-03-13 17:59:03");
        assertEquals(date.getTime(), parsedDate.getTime());
    }

    @Test
    void parseMillis() {
        setSemantics(valueSemantics = createValueSemantics(TimePrecision.MILLI_SECOND));
        var _context = Context.of(null, InteractionContext.builder().locale(UserLocale.valueOf(Locale.ENGLISH)).build());
        var parsedDate = valueSemantics.parseTextRepresentation(_context, "2013-03-13 17:59:03.123");
        assertEquals(date.getTime() + 123L, parsedDate.getTime());
    }

    @Test
    void parseMicros() {
        setSemantics(valueSemantics = createValueSemantics(TimePrecision.MICRO_SECOND));
        var _context = Context.of(null, InteractionContext.builder().locale(UserLocale.valueOf(Locale.ENGLISH)).build());
        var parsedDate = valueSemantics.parseTextRepresentation(_context, "2013-03-13 17:59:03.123456");
        assertEquals(date.getTime() + 123L, parsedDate.getTime());
    }

    @Test
    void parseNanos() {
        setSemantics(valueSemantics = createValueSemantics(TimePrecision.NANO_SECOND));
        var _context = Context.of(null, InteractionContext.builder().locale(UserLocale.valueOf(Locale.ENGLISH)).build());
        var parsedDate = valueSemantics.parseTextRepresentation(_context, "2013-03-13 17:59:03.123456789");
        assertEquals(date.getTime() + 123L, parsedDate.getTime());
    }

    // -- HELPER

    /**
     * Overrides the delegate LocalDateTimeValueSemantics,
     * such that we can inject a custom TimePrecision for testing.
     */
    private JavaUtilDateValueSemantics createValueSemantics(final TimePrecision timePrecision) {

        final ValueSemanticsAbstract<LocalDateTime> delegate =
                new LocalDateTimeValueSemantics(metaModelContext) {
            @Override protected DateTimeFormatter getTemporalEditingFormat(final Context context,
                    @NonNull final TemporalValueSemantics.TemporalCharacteristic temporalCharacteristic,
                    @NonNull final TemporalValueSemantics.OffsetCharacteristic offsetCharacteristic,
                    @NonNull final TimePrecision _timePrecision,
                    @NonNull final EditingFormatDirection direction,
                    @NonNull final TemporalEditingPattern editingPattern) {
                return super.getTemporalEditingFormat(context,
                        temporalCharacteristic, offsetCharacteristic, timePrecision, direction, editingPattern);}
        };

        return new JavaUtilDateValueSemantics() {
            @Override public ValueSemanticsAbstract<LocalDateTime> getDelegate() {
                return delegate; }
        };
    }

    @Override
    protected Date getSample() {
        return date;
    }

    @Override
    protected void assertValueEncodesToJsonAs(final Date a, final String json) {
        // TODO
        assertEquals("todo", json);
    }

}
