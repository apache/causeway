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
package org.apache.isis.viewer.wicket.ui.components.scalars.jodatime;

import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.joda.time.DateTime;

import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;

public class DateConverterForJodaDateTime extends DateConverterForJodaAbstract<DateTime> {
    
    private static final long serialVersionUID = 1L;

    public DateConverterForJodaDateTime(WicketViewerSettings settings, int adjustBy) {
        this(settings.getDatePattern(), settings.getDateTimePattern(), settings.getDateTimePattern(), adjustBy);
    }
    
    private DateConverterForJodaDateTime(String datePattern, String dateTimePattern, String datePickerPattern, int adjustBy) {
        super(DateTime.class, datePattern, dateTimePattern, datePickerPattern, adjustBy);
    }
    

    @Override
    protected DateTime doConvertToObject(String value, Locale locale) throws ConversionException {
        final DateTime parsedDateTime = convert(value);
        return parsedDateTime.minusDays(adjustBy);
    }

    private DateTime convert(String value) throws ConversionException {
        try {
            return getFormatterForDateTimePattern().parseDateTime(value);
        } catch(IllegalArgumentException ex) {
            try {
                return getFormatterForDatePattern().parseDateTime(value);
            } catch(IllegalArgumentException ex2) {
                throw new ConversionException("Cannot convert into a date/time", ex2);
            }
        }
    }

    @Override
    protected String doConvertToString(DateTime value, Locale locale) {
        return value.plusDays(adjustBy).toString(getFormatterForDateTimePattern());
    }

}
