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
import java.time.OffsetDateTime;
import java.util.Optional;

import jakarta.inject.Named;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.commons.internal.base._Temporals;
import org.apache.causeway.schema.common.v2.ValueType;

@Component
@Named("causeway.metamodel.value.OffsetDateTimeValueSemantics")
//@Log4j2
public class OffsetDateTimeValueSemantics
extends TemporalValueSemanticsProvider<OffsetDateTime> {

    public static final int MAX_LENGTH = 36;
    public static final int TYPICAL_LENGTH = 22;

    @Override
    public Class<OffsetDateTime> getCorrespondingClass() {
        return OffsetDateTime.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.OFFSET_DATE_TIME;
    }

    public OffsetDateTimeValueSemantics() {
        super(TemporalCharacteristic.DATE_TIME, OffsetCharacteristic.OFFSET,
                TYPICAL_LENGTH, MAX_LENGTH,
                OffsetDateTime::from,
                TemporalAdjust::adjustOffsetDateTime);
    }

    // -- TEMPORAL DECOMPOSITION

    @Override
    public Optional<TemporalDecomposition> decomposeTemporal(final @Nullable OffsetDateTime temporal) {
        return Optional.ofNullable(temporal)
                .map(t->new TemporalDecomposition(
                        temporal.toLocalDateTime(),
                        Optional.of(Either.right(t.getOffset())),
                        temporalCharacteristic, offsetCharacteristic));
    }

    // -- ORDER RELATION

    @Override
    public Duration epsilon() {
        return ALMOST_A_SECOND;
    }

    @Override
    public Can<OffsetDateTime> getExamples() {
        return _Temporals.sampleOffsetDateTime();
    }

}
