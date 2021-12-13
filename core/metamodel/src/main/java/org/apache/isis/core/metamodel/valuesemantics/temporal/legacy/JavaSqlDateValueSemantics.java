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

import java.sql.Date;
import java.time.LocalDate;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.valuesemantics.temporal.LocalDateValueSemantics;
import org.apache.isis.core.metamodel.valuetypes.TemporalSemanticsAdapter;

/**
 * An adapter that handles {@link java.sql.Date} with only date component.
 *
 * @see JavaUtilDateValueSemantics
 * @see JavaSqlTimeValueSemantics
 */
@Component
@Named("isis.val.JavaSqlDateValueSemantics")
public class JavaSqlDateValueSemantics
extends TemporalSemanticsAdapter<Date, LocalDate> {

    @Inject LocalDateValueSemantics localDateValueSemantics;

    @Override
    public Class<Date> getCorrespondingClass() {
        return java.sql.Date.class;
    }

    @Override
    public ValueSemanticsAbstract<LocalDate> getDelegate() {
        return localDateValueSemantics;
    }

    @Override
    public Date fromDelegateValue(final LocalDate delegateValue) {
        return java.sql.Date.valueOf(delegateValue);
    }

    @Override
    public LocalDate toDelegateValue(final java.sql.Date value) {
        return value.toLocalDate();
    }

    @Override
    public Can<Date> getExamples() {
        return Can.of(
                new java.sql.Date(new java.util.Date().getTime()),
                new java.sql.Date(0L));
    }

}
