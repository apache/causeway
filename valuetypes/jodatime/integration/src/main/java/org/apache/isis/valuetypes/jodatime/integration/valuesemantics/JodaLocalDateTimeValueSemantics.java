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

import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.valuesemantics.temporal.LocalDateTimeValueSemantics;
import org.apache.isis.core.metamodel.valuetypes.TemporalSemanticsAdapter;
import org.apache.isis.valuetypes.jodatime.applib.value.JodaTimeConverters;

@Component
@Named("isis.val.JodaLocalDateTimeValueSemantics")
public class JodaLocalDateTimeValueSemantics
extends TemporalSemanticsAdapter<org.joda.time.LocalDateTime, java.time.LocalDateTime>  {

    @Inject LocalDateTimeValueSemantics localDateTimeValueSemantics;

    @Override
    public Class<org.joda.time.LocalDateTime> getCorrespondingClass() {
        return org.joda.time.LocalDateTime.class;
    }

    @Override
    public ValueSemanticsAbstract<java.time.LocalDateTime> getDelegate() {
        return localDateTimeValueSemantics;
    }

    @Override
    public org.joda.time.LocalDateTime fromDelegateValue(final java.time.LocalDateTime delegateValue) {
        return JodaTimeConverters.toJoda(delegateValue);
    }

    @Override
    public java.time.LocalDateTime toDelegateValue(final org.joda.time.LocalDateTime value) {
        return JodaTimeConverters.fromJoda(value);
    }

    @Override
    public Can<LocalDateTime> getExamples() {
        return Can.of(
                org.joda.time.LocalDateTime.now(),
                org.joda.time.LocalDateTime.now().plusDays(2).plusSeconds(15));
    }

}

