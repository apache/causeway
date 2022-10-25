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
package org.apache.causeway.applib.value.semantics;

import java.time.Duration;
import java.time.temporal.Temporal;

import org.apache.causeway.applib.annotation.TimePrecision;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.Data;
import lombok.NonNull;

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
         * Temporal value type has time-zone offset data.
         */
        OFFSET,

        /**
         * Temporal value type has time-zone id data.
         */
        ZONED;

        public boolean isLocal() {return this == LOCAL;}
        public boolean isOffset() {return this == OFFSET;}
        public boolean isZoned() {return this == ZONED;}
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
        @NonNull //@NotEmpty
        private String datePattern = "yyyy-MM-dd";

        // -- TIME PATTERNS - SECOND

        /**
         * The locale-independent (canonical) input pattern used for editing time in the UI.
         * Yielding {@link TimeFormatPrecision#NANO_SECOND}.
         * <p>
         * Any missing temporal parts are filled up with zeros to meet the {@link TimeFormatPrecision}.
         *
         * @apiNote Supports various input forms as denoted by optional blocks (square brackets).
         * The output format is inferred by removal of the square brackets (not their content).
         */
        @NonNull //@NotEmpty
        private String timePatternNanoSecond = "HH[:mm[:ss][.SSSSSSSSS]]";

        /**
         * The locale-independent (canonical) input pattern used for editing time in the UI.
         * Yielding {@link TimeFormatPrecision#MICRO_SECOND}.
         * <p>
         * Any missing temporal parts are filled up with zeros to meet the {@link TimeFormatPrecision}.
         *
         * @apiNote Supports various input forms as denoted by optional blocks (square brackets).
         * The output format is inferred by removal of the square brackets (not their content).
         */
        @NonNull //@NotEmpty
        private String timePatternMicroSecond = "HH[:mm[:ss][.SSSSSS]]";

        /**
         * The locale-independent (canonical) input pattern used for editing time in the UI.
         * Yielding {@link TimeFormatPrecision#MILLI_SECOND}.
         * <p>
         * Any missing temporal parts are filled up with zeros to meet the {@link TimeFormatPrecision}.
         *
         * @apiNote Supports various input forms as denoted by optional blocks (square brackets).
         * The output format is inferred by removal of the square brackets (not their content).
         */
        @NonNull //@NotEmpty
        private String timePatternMilliSecond = "HH[:mm[:ss][.SSS]]";

        /**
         * The locale-independent (canonical) input pattern used for editing time in the UI.
         * Yielding {@link TimeFormatPrecision#SECOND}.
         * <p>
         * Any missing temporal parts are filled up with zeros to meet the {@link TimeFormatPrecision}.
         *
         * @apiNote Supports various input forms as denoted by optional blocks (square brackets).
         * The output format is inferred by removal of the square brackets (not their content).
         */
        @NonNull //@NotEmpty
        private String timePatternSecond = "HH[:mm[:ss]]";

        /**
         * The locale-independent (canonical) input pattern used for editing time in the UI.
         * Yielding {@link TimeFormatPrecision#MINUTE}.
         * <p>
         * Any missing temporal parts are filled up with zeros to meet the {@link TimeFormatPrecision}.
         *
         * @apiNote Supports various input forms as denoted by optional blocks (square brackets).
         * The output format is inferred by removal of the square brackets (not their content).
         */
        @NonNull //@NotEmpty
        private String timePatternMinute = "HH[:mm]";

        /**
         * The locale-independent (canonical) input pattern used for editing time in the UI.
         * Yielding {@link TimeFormatPrecision#HOUR}.
         * <p>
         * Any missing temporal parts are filled up with zeros to meet the {@link TimeFormatPrecision}.
         *
         * @apiNote Supports various input forms as denoted by optional blocks (square brackets).
         * The output format is inferred by removal of the square brackets (not their content).
         */
        @NonNull //@NotEmpty
        private String timePatternHour = "HH";

        // -- ZONE PATTERN

        /**
         * The locale-independent (canonical) pattern used for editing time-zone in the UI.
         * <p>
         * Java time-zone formats<pre>
         * V       time-zone ID                zone-id           America/Los_Angeles; Z; -08:30
         * z       time-zone name              zone-name         Pacific Standard Time; PST
         * O       localized zone-offset       offset-O          GMT+8; GMT+08:00; UTC-08:00;
         * X       zone-offset 'Z' for zero    offset-X          Z; -08; -0830; -08:30; -083015; -08:30:15;
         * x       zone-offset                 offset-x          +0000; -08; -0830; -08:30; -083015; -08:30:15;
         * Z       zone-offset                 offset-Z          +0000; -0800; -08:00;
         *</pre>
         *
         * TODO no <i>tempus-dominus</i> date/time-picker support yet.
         */
        @NonNull //@NotEmpty
        private String zoneIdPatternForOutput = "VV";

        /**
         * TODO no <i>tempus-dominus</i> date/time-picker support yet.
         */
        @NonNull //@NotEmpty
        private String zoneIdPatternForInput = "VV";

        /**
         * The locale-independent (canonical) pattern used for editing time-offset in the UI.
         * <p>
         * Java time-zone formats<pre>
         * V       time-zone ID                zone-id           America/Los_Angeles; Z; -08:30
         * z       time-zone name              zone-name         Pacific Standard Time; PST
         * O       localized zone-offset       offset-O          GMT+8; GMT+08:00; UTC-08:00;
         * X       zone-offset 'Z' for zero    offset-X          Z; -08; -0830; -08:30; -083015; -08:30:15;
         * x       zone-offset                 offset-x          +0000; -08; -0830; -08:30; -083015; -08:30:15;
         * Z       zone-offset                 offset-Z          +0000; -0800; -08:00;
         *</pre>
         *
         * @apiNote Yet only tested with {@literal XXX}, as there needs to be a format correspondence with
         * <i>momentJs</i> for the <i>tempus-dominus</i> date/time-picker to work
         * (as used by the <i>Wicket Viewer</i>).
         * {@link org.apache.causeway.viewer.wicket.ui.components.scalars.datepicker._TimeFormatUtil}
         * does the format conversion.
         */
        @NonNull //@NotEmpty
        private String offsetPatternForOutput = "XXX";

        /**
         * Support both forms for parsing, with or without colon.
         * <p>
         * (Order of optional blocks matter, eg. {@literal [X][XXX]} would not work.)
         * @see "https://stackoverflow.com/questions/34637626/java-datetimeformatter-for-time-zone-with-an-optional-colon-separator"
         */
        @NonNull //@NotEmpty
        private String offsetPatternForInput = "[XXX][X]";

        // -- JOINING PATTERNS

        /**
         * The locale-independent (canonical) pattern used for editing date and time in the UI.
         * <p>
         * Uses {@code String.format(dateTimeJoiningPattern, datePattern, timePattern)}
         * to interpolate the effective date-time format.
         * @see String#format(String, Object...)
         */
        @NonNull //@NotEmpty
        private String dateTimeJoiningPattern = "%1$s %2$s";

        /**
         * The locale-independent (canonical) pattern used for editing zoned temporals
         * (date, time or date-time) in the UI.
         * <p>
         * Uses {@code String.format(zoneJoiningPattern, temporalPattern, zonePattern)}
         * to interpolate the effective zoned temporal format.
         * @see String#format(String, Object...)
         */
        @NonNull //@NotEmpty
        private String zoneJoiningPattern = "%1$s %2$s";

        public String getEditingFormatAsPattern(
                final @NonNull TemporalCharacteristic temporalCharacteristic,
                final @NonNull OffsetCharacteristic offsetCharacteristic,
                final @NonNull TimePrecision timePrecision,
                final @NonNull EditingFormatDirection direction) {

            switch (temporalCharacteristic) {
            case DATE_TIME:
                var dateTimePattern =
                    String.format(getDateTimeJoiningPattern(),
                            getDatePattern(),
                            timePattern(timePrecision, direction));
                return offsetCharacteristic.isLocal()
                        ? dateTimePattern
                        : String.format(getZoneJoiningPattern(),
                                dateTimePattern,
                                zonePattern(offsetCharacteristic, direction));
            case DATE_ONLY:
                return offsetCharacteristic.isLocal()
                        ? getDatePattern()
                        : String.format(getZoneJoiningPattern(),
                                getDatePattern(),
                                zonePattern(offsetCharacteristic,direction));
            case TIME_ONLY:
                return offsetCharacteristic.isLocal()
                        ? timePattern(timePrecision, direction)
                        : String.format(getZoneJoiningPattern(),
                                timePattern(timePrecision, direction),
                                zonePattern(offsetCharacteristic, direction));
            default:
                throw _Exceptions.unmatchedCase(temporalCharacteristic);
            }
        }

        private String zonePattern(
                final @NonNull OffsetCharacteristic offsetCharacteristic,
                final @NonNull EditingFormatDirection direction) {

            switch(offsetCharacteristic) {
            case OFFSET:
                return direction.isInput()
                        ? getOffsetPatternForInput()
                        : getOffsetPatternForOutput();
            case ZONED:
                return direction.isInput()
                        ? getZoneIdPatternForInput()
                        : getZoneIdPatternForOutput();
            default:
                throw _Exceptions.unexpectedCodeReach();
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
