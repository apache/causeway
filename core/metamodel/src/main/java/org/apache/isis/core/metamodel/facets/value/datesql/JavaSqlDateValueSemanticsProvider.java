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

package org.apache.isis.core.metamodel.facets.value.datesql;

import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.value.ValueSemanticsProviderAbstractTemporal;
import org.apache.isis.core.metamodel.facets.value.dateutil.JavaUtilDateValueSemanticsProvider;
import org.apache.isis.core.metamodel.facets.value.timesql.JavaSqlTimeValueSemanticsProvider;

import lombok.Getter;
import lombok.Setter;

/**
 * An adapter that handles {@link java.sql.Date} with only date component.
 *
 * @see JavaUtilDateValueSemanticsProvider
 * @see JavaSqlTimeValueSemanticsProvider
 */
public class JavaSqlDateValueSemanticsProvider extends ValueSemanticsProviderAbstractTemporal<Date> {


    private static Map<String, DateFormat> formats = _Maps.newHashMap();

    static {
        formats.put(ISO_ENCODING_FORMAT, createDateEncodingFormat("yyyyMMdd"));
        formats.put("iso", createDateFormat("yyyy-MM-dd"));
        formats.put("medium", DateFormat.getDateInstance(DateFormat.MEDIUM));
    }

    @Getter @Setter
    private String configuredFormat;

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public JavaSqlDateValueSemanticsProvider() {
        this(null);
    }

    public JavaSqlDateValueSemanticsProvider(final FacetHolder holder) {
        super("date", type(), holder, Date.class, 12, Immutability.NOT_IMMUTABLE, EqualByContent.NOT_HONOURED, null);

        final Map<String, DateFormat> formats = formats();
        configuredFormat = getConfiguration().getValueTypes().getJavaSql().getDate().getFormat();
        format = formats.get(configuredFormat);
        if (format == null) {
            setMask(configuredFormat);
        }
    }


    @Override
    protected void clearFields(final Calendar cal) {
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.AM_PM, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }


    @Override
    protected boolean ignoreTimeZone() {
        return true;
    }

    @Override
    protected Map<String, DateFormat> formats() {
        return formats;
    }

    @Override
    public String toString() {
        return "DateValueSemanticsProvider: " + format;
    }

    @Override
    protected DateFormat format() {
        final Locale locale = Locale.getDefault();
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        dateFormat.setTimeZone(UTC_TIME_ZONE);
        return dateFormat;
    }

    @Override
    protected List<DateFormat> formatsToTry() {
        List<DateFormat> formats = new ArrayList<DateFormat>();

        Locale locale = Locale.getDefault();
        formats.add(DateFormat.getDateInstance(DateFormat.LONG, locale));
        formats.add(DateFormat.getDateInstance(DateFormat.MEDIUM, locale));
        formats.add(DateFormat.getDateInstance(DateFormat.SHORT, locale));
        formats.add(createDateFormat("yyyy-MM-dd"));
        formats.add(createDateFormat("yyyyMMdd"));

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


    @Override
    protected Date add(final Date original, final int years, final int months, final int days, final int hours, final int minutes) {
        final Date date = original;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.AM_PM, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.add(Calendar.YEAR, years);
        cal.add(Calendar.MONTH, months);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return setDate(cal.getTime());
    }

    @Override
    protected java.util.Date dateValue(final Object value) {
        return (java.util.Date) value;
    }

    @Override
    protected Date setDate(final java.util.Date date) {
        return new Date(date.getTime());
    }

    @Override
    protected Date now() {
        return getServiceRegistry().lookupService(ClockService.class)
                .map(ClockService::nowAsMillis)
                .map(Date::new)
                .orElseGet(()->new Date(System.currentTimeMillis())); // fallback to system time
    }

    @Override //[ISIS-2005] java.sql.Date requires special treatment, so overriding the default
    protected String doEncode(final Object pojo) {
        return ((Date)pojo).toString();
    }

    @Override //[ISIS-2005] java.sql.Date requires special treatment, so overriding the default
    protected Date doRestore(final String enString) {
        return Date.valueOf(enString);
    }

}
