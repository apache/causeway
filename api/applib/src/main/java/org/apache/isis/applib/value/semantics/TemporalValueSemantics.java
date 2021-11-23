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

}
