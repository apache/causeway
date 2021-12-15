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

import java.time.ZonedDateTime;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.valuesemantics.temporal.ZonedDateTimeValueSemantics;
import org.apache.isis.core.metamodel.valuetypes.TemporalSemanticsAdapter;
import org.apache.isis.valuetypes.jodatime.applib.value.JodaTimeConverters;

@Component
@Named("isis.val.JodaDateTimeValueSemantics")
public class JodaDateTimeValueSemantics
extends TemporalSemanticsAdapter<org.joda.time.DateTime, ZonedDateTime>  {

    @Inject ZonedDateTimeValueSemantics zonedDateTimeValueSemantics;

    @Override
    public Class<DateTime> getCorrespondingClass() {
        return org.joda.time.DateTime.class;
    }

    @Override
    public ValueSemanticsAbstract<ZonedDateTime> getDelegate() {
        return zonedDateTimeValueSemantics;
    }

    @Override
    public DateTime fromDelegateValue(final ZonedDateTime delegateValue) {
        return JodaTimeConverters.toJoda(delegateValue);
    }

    @Override
    public ZonedDateTime toDelegateValue(final DateTime value) {
        return JodaTimeConverters.fromJoda(value);
    }

    @Override
    public Can<DateTime> getExamples() {
        return Can.of(
                org.joda.time.DateTime.now(),
                org.joda.time.DateTime.now().plusDays(2).plusSeconds(15));
    }

}
