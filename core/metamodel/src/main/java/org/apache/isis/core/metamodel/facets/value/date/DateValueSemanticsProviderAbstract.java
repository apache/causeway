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

package org.apache.isis.core.metamodel.facets.value.date;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.config.IsisConfiguration.Value.FormatIdentifier;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.value.ValueSemanticsProviderAbstractTemporal;

import lombok.Getter;
import lombok.Setter;

public abstract class DateValueSemanticsProviderAbstract<T> extends ValueSemanticsProviderAbstractTemporal<T> {

    private static Map<String, DateFormat> formats = _Maps.newHashMap();

    static {
        formats.put(ISO_ENCODING_FORMAT, createDateEncodingFormat("yyyyMMdd"));
        formats.put("iso", createDateFormat("yyyy-MM-dd"));
        formats.put("medium", DateFormat.getDateInstance(DateFormat.MEDIUM));
    }

    @Getter @Setter
    private String configuredFormat;

    public DateValueSemanticsProviderAbstract(final FormatIdentifier formatIdentifier, final FacetHolder holder, final Class<T> adaptedClass, final int typicalLength, final Immutability immutability, final EqualByContent equalByContent, final T defaultValue) {
        super(formatIdentifier.name().toLowerCase(), type(), holder, adaptedClass, typicalLength, immutability, equalByContent, defaultValue);
    }

    public DateValueSemanticsProviderAbstract(final FacetHolder holder, final Class<T> adaptedClass, final Immutability immutability, final EqualByContent equalByContent, final T defaultValue) {
        this(FormatIdentifier.DATE, holder, adaptedClass, 12, immutability, equalByContent, defaultValue);

        configuredFormat = getConfiguration().getValue().getFormat().getOrDefault("date", "medium").toLowerCase().trim();

        buildFormat(configuredFormat);

        final String formatRequired = getConfiguration().getValue().getFormat().get("date");
        if (formatRequired == null) {
            format = formats().get(defaultFormat());
        } else {
            setMask(formatRequired); //TODO fails when using format names eg 'medium'
        }


    }

    // //////////////////////////////////////////////////////////////////
    // temporal-specific stuff
    // //////////////////////////////////////////////////////////////////

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
    protected String defaultFormat() {
        return "medium";
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
}
