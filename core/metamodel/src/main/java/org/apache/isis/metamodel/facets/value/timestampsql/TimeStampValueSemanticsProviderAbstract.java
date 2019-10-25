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

package org.apache.isis.metamodel.facets.value.timestampsql;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.isis.config.IsisConfiguration.Value.FormatIdentifier;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.value.ValueSemanticsProviderAbstractTemporal;

abstract class TimeStampValueSemanticsProviderAbstract<T> extends ValueSemanticsProviderAbstractTemporal<T> {

    private static final Object DEFAULT_VALUE = null; // no default
    private static final int TYPICAL_LENGTH = 25;

    protected static void initFormats(final Map<String, DateFormat> formats) {
        formats.put(ISO_ENCODING_FORMAT, createDateEncodingFormat("yyyyMMdd'T'HHmmssSSS"));
        formats.put("short", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG));
    }

    @SuppressWarnings("unchecked")
    public TimeStampValueSemanticsProviderAbstract(final FacetHolder holder, final Class<T> adaptedClass) {
        super(FormatIdentifier.TIMESTAMP, holder, adaptedClass, TYPICAL_LENGTH, Immutability.NOT_IMMUTABLE, EqualByContent.NOT_HONOURED, (T) DEFAULT_VALUE);
        
        final String formatRequired = getConfiguration()
                .getValue().getFormatOrElse(FormatIdentifier.TIMESTAMP, null);
        
        if (formatRequired == null) {
            format = formats().get(defaultFormat());
        } else {
            setMask(formatRequired);
        }
    }

    @Override
    protected T add(final T original, final int years, final int months, final int days, final int hours, final int minutes) {
        return original;
    }

    @Override
    protected String defaultFormat() {
        return "short";
    }

    @Override
    public String toString() {
        return "TimeStampValueSemanticsProvider: " + format;
    }

    @Override
    protected DateFormat format() {

        final Locale locale = Locale.getDefault();
        final TimeZone timeZone = TimeZone.getDefault();

        final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG, locale);
        dateFormat.setTimeZone(timeZone);

        return dateFormat;
    }

    @Override
    protected List<DateFormat> formatsToTry() {
        final List<DateFormat> formats = new ArrayList<DateFormat>();

        final Locale locale = Locale.getDefault();
        final TimeZone timeZone = TimeZone.getDefault();

        formats.add(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG, locale));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG, locale));
        formats.add(createDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));

        for (final DateFormat format : formats) {
            format.setTimeZone(timeZone);
        }

        return formats;
    }
}
