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
package org.apache.isis.valuetypes.jodatime.integration.valuesemantics;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.adapters.ValueSemanticsAbstract;
import org.apache.isis.core.metamodel.valuesemantics.temporal.LocalDateTimeValueSemantics;
import org.apache.isis.core.metamodel.valuetypes.ValueSemanticsAdapter;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.valuetypes.jodatime.applib.value.JodatimeConverters;

@Component
@Named("isis.val.JodaLocalDateTimeValueSemantics")
public class JodaLocalDateTimeValueSemantics
extends ValueSemanticsAdapter<org.joda.time.LocalDateTime, java.time.LocalDateTime>  {

    @Inject LocalDateTimeValueSemantics localDateTimeValueSemantics;

    @Override
    public Class<org.joda.time.LocalDateTime> getCorrespondingClass() {
        return org.joda.time.LocalDateTime.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.JODA_LOCAL_DATE_TIME;
    }

    @Override
    public ValueSemanticsAbstract<java.time.LocalDateTime> getDelegate() {
        return localDateTimeValueSemantics;
    }

    @Override
    public org.joda.time.LocalDateTime fromDelegateValue(final java.time.LocalDateTime delegateValue) {
        return JodatimeConverters.toJoda(delegateValue);
    }

    @Override
    public java.time.LocalDateTime toDelegateValue(final org.joda.time.LocalDateTime value) {
        return JodatimeConverters.fromJoda(value);
    }

}

