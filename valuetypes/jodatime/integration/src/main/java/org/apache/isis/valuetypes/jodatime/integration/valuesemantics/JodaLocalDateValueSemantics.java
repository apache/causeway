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

import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.core.metamodel.valuesemantics.temporal.LocalDateValueSemantics;
import org.apache.isis.core.metamodel.valuetypes.ValueSemanticsAdapter;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.valuetypes.jodatime.applib.value.JodatimeConverters;

@Component
@Named("isis.val.JodaLocalDateValueSemantics")
public class JodaLocalDateValueSemantics
extends ValueSemanticsAdapter<org.joda.time.LocalDate, java.time.LocalDate>  {

    @Inject LocalDateValueSemantics localDateValueSemantics;

    @Override
    public Class<org.joda.time.LocalDate> getCorrespondingClass() {
        return org.joda.time.LocalDate.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.JODA_LOCAL_DATE;
    }

    @Override
    public ValueSemanticsAbstract<java.time.LocalDate> getDelegate() {
        return localDateValueSemantics;
    }

    @Override
    public org.joda.time.LocalDate fromDelegateValue(final java.time.LocalDate delegateValue) {
        return JodatimeConverters.toJoda(delegateValue);
    }

    @Override
    public java.time.LocalDate toDelegateValue(final org.joda.time.LocalDate value) {
        return JodatimeConverters.fromJoda(value);
    }


}
