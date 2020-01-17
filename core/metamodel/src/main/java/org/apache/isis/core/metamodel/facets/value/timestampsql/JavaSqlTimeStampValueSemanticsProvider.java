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

package org.apache.isis.core.metamodel.facets.value.timestampsql;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.parseable.InvalidEntryException;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.facets.value.ValueSemanticsProviderAbstractTemporal;

import lombok.Getter;
import lombok.Setter;

public class JavaSqlTimeStampValueSemanticsProvider
extends ValueSemanticsProviderAbstractTemporal<Timestamp> {


    protected static void initFormats(final Map<String, DateFormat> formats) {
        formats.put(ISO_ENCODING_FORMAT, createDateEncodingFormat("yyyyMMdd'T'HHmmssSSS"));
        formats.put("short", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG));
    }

    @Getter @Setter
    private String configuredFormat;

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public JavaSqlTimeStampValueSemanticsProvider() {
        this(null);
    }

    @SuppressWarnings("unchecked")
    public JavaSqlTimeStampValueSemanticsProvider(final FacetHolder holder) {
        super("timestamp", type(), holder, java.sql.Timestamp.class, 25, Immutability.NOT_IMMUTABLE, EqualByContent.NOT_HONOURED, null);

        configuredFormat = getConfiguration().getValue().getFormat().getOrDefault("timestamp", "short").toLowerCase().trim();

        buildFormat(configuredFormat);

        final String formatRequired = getConfiguration().getValue().getFormat().get("timestamp");

        if (formatRequired == null) {
            format = formats().get("short");
        } else {
            setMask(formatRequired);
        }
    }

    @Override
    protected Timestamp add(final Timestamp original, final int years, final int months, final int days, final int hours, final int minutes) {
        return original;
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

    @Override
    public void appendAttributesTo(Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("configuredFormat", configuredFormat);
    }



    public static final boolean isAPropertyDefaultFacet() {
        return PropertyDefaultFacet.class.isAssignableFrom(JavaSqlTimeStampValueSemanticsProvider.class);
    }

    private static Map<String, DateFormat> formats = _Maps.newHashMap();

    static {
        initFormats(formats);
    }


    // //////////////////////////////////////////////////////////////////
    // temporal-specific stuff
    // //////////////////////////////////////////////////////////////////

    @Override
    protected Date dateValue(final Object value) {
        return new Date(((java.sql.Timestamp) value).getTime());
    }

    @Override
    protected Map<String, DateFormat> formats() {
        return formats;
    }

    @Override
    protected Timestamp now() {
        throw new InvalidEntryException("Can't change a timestamp.");
    }

    @Override
    protected Timestamp setDate(final Date date) {
        return new Timestamp(date.getTime());
    }

}
