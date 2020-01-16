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

package org.apache.isis.core.metamodel.facets.value.time;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.isis.core.config.IsisConfiguration.Value.FormatIdentifier;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.value.ValueSemanticsProviderAbstractTemporal;

import lombok.Getter;
import lombok.Setter;

public abstract class TimeValueSemanticsProviderAbstract<T> 
extends ValueSemanticsProviderAbstractTemporal<T> {

    private static final Object DEFAULT_VALUE = null; // no default
    private static final int TYPICAL_LENGTH = 8;

    protected static void initFormats(final Map<String, DateFormat> formats) {
        formats.put(ISO_ENCODING_FORMAT, createDateEncodingFormat("HHmmssSSS"));
        formats.put("short", DateFormat.getTimeInstance(DateFormat.SHORT));
    }

    @Getter @Setter
    private String configuredFormat;

    public TimeValueSemanticsProviderAbstract(final FormatIdentifier formatIdentifier, final FacetHolder holder, final Class<T> adaptedClass, final int typicalLength, final Immutability immutability, final EqualByContent equalByContent, final T defaultValue) {
        super(formatIdentifier, formatIdentifier.name().toLowerCase(), type(), holder, adaptedClass, typicalLength, immutability, equalByContent, defaultValue);
    }

    @SuppressWarnings("unchecked")
    public TimeValueSemanticsProviderAbstract(final FacetHolder holder, final Class<T> adaptedClass) {
        this(FormatIdentifier.TIME, holder, adaptedClass, TYPICAL_LENGTH, Immutability.NOT_IMMUTABLE, EqualByContent.NOT_HONOURED, (T) DEFAULT_VALUE);

        configuredFormat = getConfiguration()
                .getValue().getFormat().getOrDefault(FormatIdentifier.TIME.name().toLowerCase(), defaultFormat()).toLowerCase().trim();

        buildFormat(configuredFormat);

        final String formatRequired = getConfiguration()
                .getValue().getFormat().getOrDefault(FormatIdentifier.TIME.name().toLowerCase(), null);
        if (formatRequired == null) {
            format = formats().get(defaultFormat());
        } else {
            setMask(formatRequired);
        }
    }

    // //////////////////////////////////////////////////////////////////
    // DateValueFacet
    // //////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////
    // temporal-specific stuff
    // //////////////////////////////////////////////////////////////////

    @Override
    protected void clearFields(final Calendar cal) {
        cal.set(Calendar.YEAR, 1970);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
    }

    @Override
    protected String defaultFormat() {
        return "short";
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

}
