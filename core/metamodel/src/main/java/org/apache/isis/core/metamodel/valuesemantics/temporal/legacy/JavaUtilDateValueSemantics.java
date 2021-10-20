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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.clock.VirtualClock;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.Getter;
import lombok.Setter;

/**
 * An adapter that handles {@link java.util.Date} as both a date AND time
 * component.
 *
 * @see JavaSqlDateValueSemantics
 * @see JavaSqlTimeValueSemantics
 */
@Component
@Named("isis.val.JavaUtilDateValueSemantics")
public class JavaUtilDateValueSemantics
extends LegacyTemporalValueSemanticsAbstract<java.util.Date> {

    private static Map<String, DateFormat> formats = _Maps.newHashMap();

    @Inject ClockService clockService;

    static {
        formats.put(ISO_ENCODING_FORMAT, createDateEncodingFormat("yyyyMMdd'T'HHmmssSSS"));
        formats.put("iso", createDateFormat("yyyy-MM-dd HH:mm"));
        formats.put("medium", DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT));
    }

    @Override
    public Class<Date> getCorrespondingClass() {
        return Date.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.LOCAL_DATE;
    }

    @Getter @Setter
    private String configuredFormat;

    public JavaUtilDateValueSemantics(final IsisConfiguration config) {
        super(Date.class, 18);

        final Map<String, DateFormat> formats = formats();
        configuredFormat = config.getValueTypes().getJavaUtil().getDate().getFormat();
        format = formats.get(configuredFormat);
        if (format == null) {
            setMask(configuredFormat);
        }
    }

    // //////////////////////////////////////////////////////////////////
    // temporal-specific stuff
    // //////////////////////////////////////////////////////////////////

    @Override
    protected Map<String, DateFormat> formats() {
        return formats;
    }

    @Override
    protected DateFormat format() {
        final Locale locale = Locale.getDefault();
        final TimeZone timeZone = TimeZone.getDefault();

        final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
        dateFormat.setTimeZone(timeZone);
        return dateFormat;
    }

    @Override
    protected List<DateFormat> formatsToTry() {
        List<DateFormat> formats = new ArrayList<>();

        final Locale locale = Locale.getDefault();
        final TimeZone timeZone = TimeZone.getDefault();

        formats.add(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale));
        formats.add(createDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));
        formats.add(createDateFormat("yyyyMMdd'T'HHmmssSSS"));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale));
        formats.add(createDateFormat("yyyy-MM-dd HH:mm:ss"));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale));
        formats.add(createDateFormat("yyyyMMdd'T'HHmmss"));
        formats.add(createDateFormat("yyyy-MM-dd HH:mm"));
        formats.add(createDateFormat("yyyyMMdd'T'HHmm"));
        formats.add(createDateFormat("dd-MMM-yyyy HH:mm"));

        for (DateFormat format : formats) {
            format.setTimeZone(timeZone);
        }

        return formats;
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
        return Optional.ofNullable(clockService)
                .map(ClockService::getClock)
                .map(VirtualClock::nowAsEpochMilli)
                .map(Date::new)
                .orElseGet(Date::new); // fallback to system time
    }

    @Override
    protected Date setDate(final Date date) {
        return date;
    }
}
