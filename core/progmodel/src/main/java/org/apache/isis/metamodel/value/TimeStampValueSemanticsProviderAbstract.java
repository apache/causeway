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


package org.apache.isis.metamodel.value;

import java.text.DateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.apache.isis.applib.value.TimeStamp;
import org.apache.isis.core.metamodel.config.ConfigurationConstants;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.specloader.SpecificationLoader;


public abstract class TimeStampValueSemanticsProviderAbstract extends ValueSemanticsProviderAbstractTemporal {

    private static final Object DEFAULT_VALUE = null; // no default
    private static final int TYPICAL_LENGTH = 12;

    private static final boolean IMMUTABLE = false;
    private static final boolean EQUAL_BY_CONTENT = false;

    protected static void initFormats(final Map<String, DateFormat> formats) {
        formats.put("iso", createDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));
        formats.put(ISO_ENCODING_FORMAT, createDateFormat("yyyyMMdd'T'HHmmssSSS"));
        formats.put("medium", DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG));
        formats.put("short", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG));
    }

    public TimeStampValueSemanticsProviderAbstract(
            final FacetHolder holder,
            final IsisConfiguration configuration,
            final SpecificationLoader specificationLoader,
            final RuntimeContext runtimeContext) {
        super("timestamp", holder, TimeStamp.class, TYPICAL_LENGTH, IMMUTABLE, EQUAL_BY_CONTENT, DEFAULT_VALUE, configuration,
                specificationLoader, runtimeContext);
        final String formatRequired = configuration.getString(ConfigurationConstants.ROOT + "value.format.timestamp");
        if (formatRequired == null) {
            format = (DateFormat) formats().get(defaultFormat());
        } else {
            setMask(formatRequired);
        }
    }

    @Override
    public int getLevel() {
        return TIMESTAMP;
    }

    @Override
    protected Object add(
            final Object original,
            final int years,
            final int months,
            final int days,
            final int hours,
            final int minutes) {
        return original;
    }

    @Override
    protected Date dateValue(final Object value) {
        return new Date(((TimeStamp) value).longValue());
    }

    @Override
    protected String defaultFormat() {
        return "short";
    }

    @Override
    public String toString() {
        return "TimeStampValueSemanticsProvider: " + format;
    }

}

