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
package org.apache.causeway.core.metamodel.valuesemantics.temporal.legacy;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.valuesemantics.temporal.LocalDateTimeValueSemantics;
import org.apache.causeway.core.metamodel.valuetypes.TemporalSemanticsAdapter;

/**
 * An adapter that handles {@link java.util.Date} as both a date AND time
 * component.
 *
 * @see JavaSqlDateValueSemantics
 * @see JavaSqlTimeValueSemantics
 */
@Component
@Named("causeway.val.JavaUtilDateValueSemantics")
public class JavaUtilDateValueSemantics
extends TemporalSemanticsAdapter<java.util.Date, LocalDateTime>  {

    @Inject LocalDateTimeValueSemantics localDateTimeValueSemantics;

    @Override
    public Class<Date> getCorrespondingClass() {
        return java.util.Date.class;
    }

    @Override
    public ValueSemanticsAbstract<LocalDateTime> getDelegate() {
        return localDateTimeValueSemantics;
    }

    @Override
    public Date fromDelegateValue(final LocalDateTime delegateValue) {
        return delegateValue!=null
                ? java.util.Date.from(delegateValue
                    .atZone(ZoneId.systemDefault())
                    .toInstant())
                : null;
    }

    @Override
    public LocalDateTime toDelegateValue(final java.util.Date value) {
        return value!=null
                ? LocalDateTime.ofInstant(
                        value.toInstant(), ZoneId.systemDefault())
                : null;
    }

    @Override
    public Can<Date> getExamples() {
        return Can.of(
                new java.util.Date(),
                new java.util.Date(0L));
    }

}
