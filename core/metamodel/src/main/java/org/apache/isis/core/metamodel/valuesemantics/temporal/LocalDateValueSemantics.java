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
import java.time.LocalDate;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.value.semantics.OrderRelation;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.schema.common.v2.ValueType;

@Component
@Named("isis.val.LocalDateValueSemantics")
//@Log4j2
public class LocalDateValueSemantics
extends TemporalValueSemanticsProvider<LocalDate>
implements OrderRelation<LocalDate, Duration> {

    public static final int MAX_LENGTH = 12;
    public static final int TYPICAL_LENGTH = MAX_LENGTH;

    @Override
    public Class<LocalDate> getCorrespondingClass() {
        return LocalDate.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.LOCAL_DATE;
    }

    public LocalDateValueSemantics() {
        super(TemporalCharacteristic.DATE_ONLY, OffsetCharacteristic.LOCAL,
                TYPICAL_LENGTH, MAX_LENGTH,
                LocalDate::from,
                TemporalAdjust::adjustLocalDate);
    }

    // -- ORDER RELATION

    @Override
    public Duration epsilon() {
        return Duration.ZERO; // not used for dates, as these are integer based
    }

    @Override
    public Can<LocalDate> getExamples() {
        return Can.of(
                LocalDate.now(),
                LocalDate.now().plusDays(2));
    }

}
