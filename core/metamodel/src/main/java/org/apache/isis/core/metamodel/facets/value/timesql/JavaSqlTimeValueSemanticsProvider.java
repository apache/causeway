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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.value.ValueSemanticsProviderAbstractTemporal;

import lombok.Getter;
import lombok.Setter;

/**
 * Treats {@link java.sql.Time} as a time-only value type.
 *
 */
public class JavaSqlTimeValueSemanticsProvider extends ValueSemanticsProviderAbstractTemporal<Time> {

    protected static void initFormats(final Map<String, DateFormat> formats) {
        formats.put(ISO_ENCODING_FORMAT, createDateEncodingFormat("HHmmssSSS"));
        formats.put("short", DateFormat.getTimeInstance(DateFormat.SHORT));
    }

    @Getter @Setter
    private String configuredFormat;

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public JavaSqlTimeValueSemanticsProvider() {
        this(null);
    }

    public JavaSqlTimeValueSemanticsProvider(final FacetHolder holder) {
        super("time", type(), holder, java.sql.Time.class, 8, Immutability.NOT_IMMUTABLE, EqualByContent.NOT_HONOURED, null);

        configuredFormat = getConfiguration().getValue().getFormat().getOrDefault("time", "short").toLowerCase().trim();

        buildFormat(configuredFormat);

        final String formatRequired = getConfiguration().getValue().getFormat().get("time");
        if (formatRequired == null) {
            format = formats().get("short");
        } else {
            setMask(formatRequired);
        }
    }

    @Override
    protected void clearFields(final Calendar cal) {
        cal.set(Calendar.YEAR, 1970);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
    }

    @Override
    public String toString() {
        return "TimeValueSemanticsProvider: " + format;
    }

    @Override
    protected DateFormat format() {

        final Locale locale = Locale.getDefault();
        final DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
        dateFormat.setTimeZone(UTC_TIME_ZONE);
        return dateFormat;
    }

    @Override
    protected List<DateFormat> formatsToTry() {
        List<DateFormat> formats = new ArrayList<DateFormat>();

        final Locale locale = Locale.getDefault();

        formats.add(DateFormat.getTimeInstance(DateFormat.LONG, locale));
        formats.add(DateFormat.getTimeInstance(DateFormat.MEDIUM, locale));
        formats.add(DateFormat.getTimeInstance(DateFormat.SHORT, locale));
        formats.add(createDateFormat("HH:mm:ss.SSS"));
        formats.add(createDateFormat("HHmmssSSS"));
        formats.add(createDateFormat("HH:mm:ss"));
        formats.add(createDateFormat("HHmmss"));

        for (DateFormat format : formats) {
            format.setTimeZone(UTC_TIME_ZONE);
        }

        return formats;
    }

    @Override
    public void appendAttributesTo(Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("configuredFormat", configuredFormat);
    }


    private static Map<String, DateFormat> formats = _Maps.newHashMap();

    static {
        initFormats(formats);
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
        return new java.sql.Time(Clock.getEpochMillis());
    }

    @Override
    protected Time setDate(final Date date) {
        return new java.sql.Time(date.getTime());
    }

}
