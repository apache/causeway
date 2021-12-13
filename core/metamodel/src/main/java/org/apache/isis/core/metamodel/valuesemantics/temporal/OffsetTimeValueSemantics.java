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
import java.time.OffsetTime;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.schema.common.v2.ValueType;

@Component
@Named("isis.val.OffsetTimeValueSemantics")
//@Log4j2
public class OffsetTimeValueSemantics
extends TemporalValueSemanticsProvider<OffsetTime> {

    public static final int MAX_LENGTH = 12;
    public static final int TYPICAL_LENGTH = MAX_LENGTH;

    @Override
    public Class<OffsetTime> getCorrespondingClass() {
        return OffsetTime.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.OFFSET_TIME;
    }

    public OffsetTimeValueSemantics() {
        super(TemporalCharacteristic.TIME_ONLY, OffsetCharacteristic.OFFSET,
                TYPICAL_LENGTH, MAX_LENGTH,
                OffsetTime::from,
                TemporalAdjust::adjustOffsetTime);
    }

    // -- ORDER RELATION

    @Override
    public Duration epsilon() {
        return ALMOST_A_SECOND;
    }

    @Override
    public Can<OffsetTime> getExamples() {
        return Can.of(
                OffsetTime.now(),
                OffsetTime.now().plusSeconds(15));
    }

}
