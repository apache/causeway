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

package org.apache.isis.metamodel.facets.value.dateutil;

import java.util.Calendar;
import java.util.Date;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.value.DateAndTimeValueSemanticsProviderAbstract;
import org.apache.isis.metamodel.facets.value.datesql.JavaSqlDateValueSemanticsProvider;
import org.apache.isis.metamodel.facets.value.timesql.JavaSqlTimeValueSemanticsProvider;

/**
 * An adapter that handles {@link java.util.Date} as both a date AND time
 * component.
 *
 * @see JavaSqlDateValueSemanticsProvider
 * @see JavaSqlTimeValueSemanticsProvider
 */
public class JavaUtilDateValueSemanticsProvider extends DateAndTimeValueSemanticsProviderAbstract<java.util.Date> {

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public JavaUtilDateValueSemanticsProvider() {
        this(null);
    }

    public JavaUtilDateValueSemanticsProvider(final FacetHolder holder) {
        super(holder, Date.class, Immutability.NOT_IMMUTABLE, EqualByContent.NOT_HONOURED);
    }

    @Override
    protected Date dateValue(final Object value) {
        return value == null ? null : (Date) value;
    }

    @Override
    protected Date add(final Date original, final int years, final int months, final int days, final int hours, final int minutes) {
        final Date date = original;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.add(Calendar.YEAR, years);
        cal.add(Calendar.MONTH, months);
        cal.add(Calendar.DAY_OF_MONTH, days);
        cal.add(Calendar.HOUR, hours);
        cal.add(Calendar.MINUTE, minutes);

        return setDate(cal.getTime());
    }

    @Override
    protected Date now() {
        return new Date(Clock.getTime());
    }

    @Override
    protected Date setDate(final Date date) {
        return date;
    }
}
