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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import lombok.val;

public interface TemporalCharacteristicsProvider {

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

    /**
     * For temporal value editing, provides the list of available time zones to choose from.
     */
    default List<ZoneId> getAvailableZoneIds() {
        return ZoneId.getAvailableZoneIds().stream()
            .sorted()
            .map(ZoneId::of)
            .collect(Collectors.toList());
    }

    /**
     * For temporal value editing, provides the list of available offsets to choose from.
     */
    default List<ZoneOffset> getAvailableOffsets() {
        val now = LocalDateTime.now();
        return getAvailableZoneIds().stream()
            .map(ZoneId::getRules)
            .flatMap(zoneIdRules->zoneIdRules.getValidOffsets(now).stream())
            .sorted()
            .distinct()
            .collect(Collectors.toList());
    }
}
