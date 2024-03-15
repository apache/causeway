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

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.functional.Either;

import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Decomposes arbitrary time representing values of type T into a canonical representation.
 */
public interface TemporalSupport<T> extends TemporalCharacteristicsProvider {

    @Value @Accessors(fluent=true) // record candidate
    public static class TemporalDecomposition implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Temporal localTemporal;
        private final Optional<Either<ZoneId, ZoneOffset>> zoneOrOffset;
        TemporalCharacteristic temporalCharacteristic;
        OffsetCharacteristic offsetCharacteristic;
    }

    /**
     * Decomposes any temporal value into 2 parts, a 'local' {@link Temporal} and a {@link ZoneId}.
     */
    Optional<TemporalDecomposition> decomposeTemporal(final @Nullable T temporal);

}
