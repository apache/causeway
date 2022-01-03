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

    @Data
    public static class TemporalEditingPattern {

        /**
         * The locale-independent (canonical) pattern used for editing dates in the UI.
         */
        @NotNull @NotEmpty
        private String datePattern = "yyyy-MM-dd";

        /**
         * The locale-independent (canonical) pattern used for editing time in the UI.
         * <p>
         * When editing, omitting nano-seconds, seconds or minutes will use zeros instead.
         */
        @NotNull @NotEmpty
        private String timePattern = "HH:mm:ss"; //FIXME[ISIS-2882] support omitted parts on input "HH[:mm[:ss[.SSSSSSSSS]]]"

        /**
         * The locale-independent (canonical) pattern used for editing time-zone in the UI.
         */
        @NotNull @NotEmpty
        private String zonePattern = "x";

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
                final @NonNull OffsetCharacteristic offsetCharacteristic) {

            switch (temporalCharacteristic) {
            case DATE_TIME:
                val dateTimePattern =
                    String.format(getDateTimeJoiningPattern(), getDatePattern(), getTimePattern());
                return offsetCharacteristic.isLocal()
                        ? dateTimePattern
                        : String.format(getZoneJoiningPattern(), dateTimePattern, getZonePattern());
            case DATE_ONLY:
                return offsetCharacteristic.isLocal()
                        ? getDatePattern()
                        : String.format(getZoneJoiningPattern(), getDatePattern(), getZonePattern());
            case TIME_ONLY:
                return offsetCharacteristic.isLocal()
                        ? getTimePattern()
                        : String.format(getZoneJoiningPattern(), getTimePattern(), getZonePattern());
            default:
                throw _Exceptions.unmatchedCase(temporalCharacteristic);
            }
        }

    }

}
