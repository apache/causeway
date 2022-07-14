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
package org.apache.isis.core.metamodel.valuesemantics.temporal;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.val;

@Component
@Named("isis.val.OffsetDateTimeValueSemantics")
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

    // -- ORDER RELATION

    @Override
    public Duration epsilon() {
        return ALMOST_A_SECOND;
    }

    @Override
    public Can<OffsetDateTime> getExamples() {
        // don't depend on current TimeZone.getDefault(),
        // instead use an arbitrary mix of fixed time-zone offsets Z, +02:00 and -02:00
        val localNow = LocalDateTime.now();
        return Can.of(
                OffsetDateTime.of(localNow, ZoneOffset.UTC),
                OffsetDateTime.of(localNow, ZoneOffset.ofHours(2)),
                OffsetDateTime.of(localNow, ZoneOffset.ofHours(-2)).plusDays(2).plusSeconds(15));
    }

}
