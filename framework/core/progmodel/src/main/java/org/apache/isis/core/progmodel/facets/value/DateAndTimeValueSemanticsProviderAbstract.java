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

package org.apache.isis.core.progmodel.facets.value;

import java.text.DateFormat;
import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.progmodel.facets.object.value.ValueSemanticsProviderContext;

public abstract class DateAndTimeValueSemanticsProviderAbstract<T> extends ValueSemanticsProviderAbstractTemporal<T> {

    private static Map<String, DateFormat> formats = Maps.newHashMap();

    static {
        formats.put("iso", createDateFormat("yyyy-MM-dd HH:mm"));
        formats.put("iso_short", createDateFormat("yyyyMMdd'T'HHmm"));
        formats.put("iso_sec", createDateFormat("yyyy-MM-dd HH:mm:ss"));
        formats.put("iso_sec_short", createDateFormat("yyyyMMdd'T'HHmmss"));
        formats.put("iso_milli", createDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));
        formats.put("iso_milli_short", createDateFormat("yyyyMMdd'T'HHmmssSSS"));
        formats.put(ISO_ENCODING_FORMAT, createDateFormat("yyyyMMdd'T'HHmmssSSS"));
        formats.put("long", DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG));
        formats.put("medium", DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT));
        formats.put("short", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT));
        formats.put("custom1", createDateFormat("dd-MMM-yyyy HH:mm"));
    }

    private static final Object DEFAULT_VALUE = null; // no default
    private static final int TYPICAL_LENGTH = 18;

    @SuppressWarnings("unchecked")
    public DateAndTimeValueSemanticsProviderAbstract(final FacetHolder holder, final Class<T> adaptedClass, final boolean immutable, final boolean equalByContent, final IsisConfiguration configuration, final ValueSemanticsProviderContext context) {
        super("datetime", holder, adaptedClass, TYPICAL_LENGTH, immutable, equalByContent, (T) DEFAULT_VALUE, configuration, context);

        final String formatRequired = configuration.getString(ConfigurationConstants.ROOT + "value.format.datetime");
        if (formatRequired == null) {
            format = formats().get(defaultFormat());
        } else {
            setMask(formatRequired);
        }
    }

    // //////////////////////////////////////////////////////////////////
    // DateValueFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public int getLevel() {
        return DATE_AND_TIME;
    }

    // //////////////////////////////////////////////////////////////////
    // temporal-specific stuff
    // //////////////////////////////////////////////////////////////////

    @Override
    protected String defaultFormat() {
        return "medium";
    }

    @Override
    protected Map<String, DateFormat> formats() {
        return formats;
    }

    @Override
    protected DateFormat format(final Localization localization) {
        final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, localization.getLocale());
        dateFormat.setTimeZone(localization.getTimeZone());
        return dateFormat;
    }

    @Override
    public String toString() {
        return "JavaDateTimeValueSemanticsProvider: " + format;
    }

}
