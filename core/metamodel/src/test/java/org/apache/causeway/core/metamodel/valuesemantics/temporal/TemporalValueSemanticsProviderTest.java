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
package org.apache.causeway.core.metamodel.valuesemantics.temporal;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.annotation.TimePrecision;
import org.apache.causeway.applib.locale.UserLocale;
import org.apache.causeway.applib.value.semantics.TemporalCharacteristicsProvider.OffsetCharacteristic;
import org.apache.causeway.applib.value.semantics.TemporalCharacteristicsProvider.TemporalCharacteristic;
import org.apache.causeway.applib.value.semantics.TemporalValueSemantics;
import org.apache.causeway.applib.value.semantics.TemporalValueSemantics.EditingFormatDirection;
import org.apache.causeway.applib.value.semantics.TemporalValueSemantics.TemporalEditingPattern;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider.Context;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.schema.common.v2.ValueType;

import org.jspecify.annotations.NonNull;

class TemporalValueSemanticsProviderTest {

    private TemporalValueSemanticsProvider_forTesting target;
    private TemporalEditingPattern editingPattern;

    @BeforeEach
    void setUp() throws Exception {
        editingPattern = (new CausewayConfiguration.ValueTypes.Temporal()).getEditing();
    }

    @ParameterizedTest
    @EnumSource(TimePrecision.class)
    void timeFormats(final TimePrecision timePrecision) {

        target = new TemporalValueSemanticsProvider_forTesting(
                TemporalCharacteristic.TIME_ONLY, OffsetCharacteristic.LOCAL);

        Context context = null;
        LocalTime localTime = LocalTime.of(13, 12, 45);

        var formatter = target.getTemporalEditingFormat(context ,
                target.getTemporalCharacteristic(),
                target.getOffsetCharacteristic(),
                timePrecision,
                EditingFormatDirection.OUTPUT,
                editingPattern);

        var formattedTemporal = formatter.format(localTime);
        assertNotNull(formattedTemporal);
    }

    // -- HELPER

    private static class TemporalValueSemanticsProvider_forTesting
    extends TemporalValueSemanticsProvider<Temporal> {

        public TemporalValueSemanticsProvider_forTesting(
                final TemporalCharacteristic temporalCharacteristic,
                final OffsetCharacteristic offsetCharacteristic) {
            super(temporalCharacteristic, offsetCharacteristic, 80, 80, null, null);
        }

        @Override public Class<Temporal> getCorrespondingClass() {
            return Temporal.class;}

        @Override public ValueType getSchemaValueType() {
            return ValueType.VOID;}

        @Override protected UserLocale getUserLocale(final Context context) {
            return super.getUserLocale(context);}

        @Override public Duration epsilon() {
            return null;}

        @Override public DateTimeFormatter getTemporalEditingFormat(final Context context,
                final TemporalValueSemantics.@NonNull TemporalCharacteristic temporalCharacteristic,
                final TemporalValueSemantics.@NonNull OffsetCharacteristic offsetCharacteristic,
                final @NonNull TimePrecision timePrecision,
                final @NonNull EditingFormatDirection direction,
                final @NonNull TemporalEditingPattern editingPattern) {
            return super.getTemporalEditingFormat(context, temporalCharacteristic, offsetCharacteristic, timePrecision, direction,
                    editingPattern); }

        @Override
        public Optional<TemporalDecomposition> decomposeTemporal(final @Nullable Temporal temporal) {
            throw _Exceptions.notImplemented(); // method not tested yet
        }

    }

}
