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
package org.apache.isis.core.metamodel.valuesemantics.temporal.legacy;

import java.sql.Time;
import java.time.LocalTime;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.valuesemantics.temporal.LocalTimeValueSemantics;
import org.apache.isis.core.metamodel.valuetypes.TemporalSemanticsAdapter;

/**
 * Treats {@link java.sql.Time} as a time-only value type.
 *
 */
@Component
@Named("isis.val.JavaSqlTimeValueSemantics")
public class JavaSqlTimeValueSemantics
extends TemporalSemanticsAdapter<Time, LocalTime>  {

    @Inject LocalTimeValueSemantics localTimeValueSemantics;

    @Override
    public Class<Time> getCorrespondingClass() {
        return java.sql.Time.class;
    }

    @Override
    public ValueSemanticsAbstract<LocalTime> getDelegate() {
        return localTimeValueSemantics;
    }

    @Override
    public Time fromDelegateValue(final LocalTime delegateValue) {
        return Time.valueOf(delegateValue);
    }

    @Override
    public LocalTime toDelegateValue(final Time value) {
        return value.toLocalTime();
    }

    @Override
    public Can<Time> getExamples() {
        return Can.of(
                new java.sql.Time(new java.util.Date().getTime()),
                new java.sql.Time(0L));
    }

}
