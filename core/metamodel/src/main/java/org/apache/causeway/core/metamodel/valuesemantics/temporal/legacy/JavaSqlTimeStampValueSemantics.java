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

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.valuesemantics.temporal.LocalDateTimeValueSemantics;
import org.apache.causeway.core.metamodel.valuetypes.TemporalSemanticsAdapter;

@Component
@Named("causeway.val.JavaSqlTimeStampValueSemantics")
public class JavaSqlTimeStampValueSemantics
extends TemporalSemanticsAdapter<Timestamp, LocalDateTime> {

    @Inject LocalDateTimeValueSemantics localDateTimeValueSemantics;

    @Override
    public Class<Timestamp> getCorrespondingClass() {
        return java.sql.Timestamp.class;
    }

    @Override
    public ValueSemanticsAbstract<LocalDateTime> getDelegate() {
        return localDateTimeValueSemantics;
    }

    @Override
    public Timestamp fromDelegateValue(final LocalDateTime delegateValue) {
        return delegateValue!=null
                ? Timestamp.valueOf(delegateValue)
                : null;
    }

    @Override
    public LocalDateTime toDelegateValue(final Timestamp value) {
        return value!=null
                ? value.toLocalDateTime()
                : null;
    }

    @Override
    public Can<Timestamp> getExamples() {
        return Can.of(
                new Timestamp(new java.util.Date().getTime()),
                new Timestamp(0L));
    }

}
