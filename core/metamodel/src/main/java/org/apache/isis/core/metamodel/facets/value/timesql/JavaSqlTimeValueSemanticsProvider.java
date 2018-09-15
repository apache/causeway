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

package org.apache.isis.core.metamodel.facets.value.timesql;

import java.sql.Time;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.value.time.TimeValueSemanticsProviderAbstract;
import org.apache.isis.core.metamodel.services.ServicesInjector;

/**
 * Treats {@link java.sql.Time} as a time-only value type.
 *
 */
public class JavaSqlTimeValueSemanticsProvider extends TimeValueSemanticsProviderAbstract<java.sql.Time> {
    private static Map<String, DateFormat> formats = Maps.newHashMap();

    static {
        initFormats(formats);
    }

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public JavaSqlTimeValueSemanticsProvider() {
        this(null, null);
    }

    public JavaSqlTimeValueSemanticsProvider(final FacetHolder holder, final ServicesInjector context) {
        super(holder, java.sql.Time.class, context);
    }

    @Override
    public Time add(final Time original, final int years, final int months, final int days, final int hours, final int minutes) {
        final java.sql.Time time = original;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        cal.set(Calendar.YEAR, 0);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.add(Calendar.HOUR, hours);
        cal.add(Calendar.MINUTE, minutes);

        return setDate(cal.getTime());
    }

    @Override
    public java.util.Date dateValue(final Object object) {
        final java.sql.Time time = (Time) object;
        return time == null ? null : new java.util.Date(time.getTime());
    }

    @Override
    protected Map<String, DateFormat> formats() {
        return formats;
    }

    @Override
    protected Time now() {
        return new java.sql.Time(Clock.getTime());
    }

    @Override
    protected Time setDate(final Date date) {
        return new java.sql.Time(date.getTime());
    }

}
