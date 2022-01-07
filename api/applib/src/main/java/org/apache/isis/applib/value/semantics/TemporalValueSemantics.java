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
package org.apache.isis.applib.value.semantics;

import java.time.Duration;
import java.time.temporal.Temporal;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.apache.isis.applib.annotations.TimePrecision;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.Data;
import lombok.NonNull;
import lombok.val;

/**
 * Common base for {@link java.time.temporal.Temporal} value types.
 *
 * @since 2.0
 *
 * @param <T> implementing {@link java.time.temporal.Temporal} type
 */
public interface TemporalValueSemantics<T extends Temporal>
extends
    OrderRelation<T, Duration>,
    EncoderDecoder<T>,
    Parser<T>,
    Renderer<T> {

    static enum TemporalCharacteristic {

        /**
         * Temporal value type has no date information, just time.
         */
        TIME_ONLY,

        /**
         * Temporal value type has no time information, just date.
         */
        DATE_ONLY,

        /**
         * Temporal value type has both date and time information.
         */
        DATE_TIME
    }

    static enum OffsetCharacteristic {

        /**
         * Temporal value type has no time-zone data.
         */
        LOCAL,

        /**
         * Temporal value type has time-zone data.
         */
        OFFSET;

        public boolean isLocal() {return this == LOCAL;}
    }

    TemporalCharacteristic getTemporalCharacteristic();
    OffsetCharacteristic getOffsetCharacteristic();

    static enum EditingFormatDirection {

        /**
         * Input parsable text.
         */
        INPUT,

        /**
         * Output parsable text.
         */
        OUTPUT;

        public boolean isInput() {return this == INPUT;}
        public boolean isOutput() {return this == OUTPUT;}
    }


    @Data
    public static class TemporalEditingPattern {

        /**
         * The locale-independent (canonical) pattern used for editing dates in the UI.
         */
        @NotNull @NotEmpty
        private String datePattern = "yyyy-MM-dd";

        // -- TIME PATTERNS - SECOND

        /**
         * The locale-independent (canonical) input pattern used for editing time in the UI.
         * Yielding {@link TimeFormatPrecision#NANO_SECOND}.
         * <p>
         * Any missing temporal parts are filled up with zeros to meet the {@link TimeFormatPrecision}.
         */
        @NotNull @NotEmpty
        private String timePatternNanoSecond = "HH[:mm[:ss][.SSSSSSSSS]]";

        /**
         * The locale-independent (canonical) input pattern used for editing time in the UI.
         * Yielding {@link TimeFormatPrecision#MICRO_SECOND}.
         * <p>
         * Any missing temporal parts are filled up with zeros to meet the {@link TimeFormatPrecision}.
         */
        @NotNull @NotEmpty
        private String timePatternMicroSecond = "HH[:mm[:ss][.SSSSSS]]";

        /**
         * The locale-independent (canonical) input pattern used for editing time in the UI.
         * Yielding {@link TimeFormatPrecision#MILLI_SECOND}.
         * <p>
         * Any missing temporal parts are filled up with zeros to meet the {@link TimeFormatPrecision}.
         */
        @NotNull @NotEmpty
        private String timePatternMilliSecond = "HH[:mm[:ss][.SSS]]";

        /**
         * The locale-independent (canonical) input pattern used for editing time in the UI.
         * Yielding {@link TimeFormatPrecision#SECOND}.
         * <p>
         * Any missing temporal parts are filled up with zeros to meet the {@link TimeFormatPrecision}.
         */
        @NotNull @NotEmpty
        private String timePatternSecond = "HH[:mm[:ss]]";

        /**
         * The locale-independent (canonical) input pattern used for editing time in the UI.
         * Yielding {@link TimeFormatPrecision#MINUTE}.
         * <p>
         * Any missing temporal parts are filled up with zeros to meet the {@link TimeFormatPrecision}.
         */
        @NotNull @NotEmpty
        private String timePatternMinute = "HH[:mm]";

        /**
         * The locale-independent (canonical) input pattern used for editing time in the UI.
         * Yielding {@link TimeFormatPrecision#HOUR}.
         * <p>
         * Any missing temporal parts are filled up with zeros to meet the {@link TimeFormatPrecision}.
         */
        @NotNull @NotEmpty
        private String timePatternHour = "HH";

        // -- ZONE PATTERN

        /**
         * The locale-independent (canonical) pattern used for editing time-zone in the UI.
         */
        @NotNull @NotEmpty
        private String zonePattern = "x";

        // -- JOINING PATTERNS

        /**
         * The locale-independent (canonical) pattern used for editing date and time in the UI.
         * <p>
         * Uses {@code String.format(dateTimeJoiningPattern, datePattern, timePattern)}
         * to interpolate the effective date-time format.
         * @see String#format(String, Object...)
         */
        @NotNull @NotEmpty
        private String dateTimeJoiningPattern = "%1$s %2$s";

        /**
         * The locale-independent (canonical) pattern used for editing zoned temporals
         * (date, time or date-time) in the UI.
         * <p>
         * Uses {@code String.format(zoneJoiningPattern, temporalPattern, zonePattern)}
         * to interpolate the effective zoned temporal format.
         * @see String#format(String, Object...)
         */
        @NotNull @NotEmpty
        private String zoneJoiningPattern = "%1$s %2$s";

        public String getEditingFormatAsPattern(
                final @NonNull TemporalCharacteristic temporalCharacteristic,
                final @NonNull OffsetCharacteristic offsetCharacteristic,
                final @NonNull TimePrecision timePrecision,
                final @NonNull EditingFormatDirection direction) {

            switch (temporalCharacteristic) {
            case DATE_TIME:
                val dateTimePattern =
                    String.format(getDateTimeJoiningPattern(), getDatePattern(), timePattern(timePrecision, direction));
                return offsetCharacteristic.isLocal()
                        ? dateTimePattern
                        : String.format(getZoneJoiningPattern(), dateTimePattern, getZonePattern());
            case DATE_ONLY:
                return offsetCharacteristic.isLocal()
                        ? getDatePattern()
                        : String.format(getZoneJoiningPattern(), getDatePattern(), getZonePattern());
            case TIME_ONLY:
                return offsetCharacteristic.isLocal()
                        ? timePattern(timePrecision, direction)
                        : String.format(getZoneJoiningPattern(), timePattern(timePrecision, direction), getZonePattern());
            default:
                throw _Exceptions.unmatchedCase(temporalCharacteristic);
            }
        }

        // -- HELPER

        private String timePattern(
                final @NonNull TimePrecision timePrecision,
                final @NonNull EditingFormatDirection direction) {
            switch (direction) {
            case INPUT:
                return timePattern(timePrecision);
            case OUTPUT:
                return timePattern(timePrecision)
                        .replace("[", "").replace("]", ""); // remove brackets for optional temporal parts
            }
            throw _Exceptions.unmatchedCase(direction);
        }

        private String timePattern(final @NonNull TimePrecision timePrecision) {
            switch (timePrecision) {
            case NANO_SECOND:
                return getTimePatternNanoSecond();
            case MICRO_SECOND:
                return getTimePatternMicroSecond();
            case MILLI_SECOND:
                return getTimePatternMilliSecond();
            case UNSPECIFIED:
            case SECOND:
                return getTimePatternSecond();
            case MINUTE:
                return getTimePatternMinute();
            case HOUR:
                return getTimePatternHour();
            }
            throw _Exceptions.unmatchedCase(timePrecision);
        }

    }

}
