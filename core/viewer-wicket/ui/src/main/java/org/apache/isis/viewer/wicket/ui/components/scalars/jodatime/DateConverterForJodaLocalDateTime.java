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
import org.joda.time.LocalDateTime;

import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;

public class DateConverterForJodaLocalDateTime extends DateConverterForJodaAbstract<LocalDateTime> {

    private static final long serialVersionUID = 1L;

    public DateConverterForJodaLocalDateTime(WicketViewerSettings settings, int adjustBy) {
        this(settings.getDatePattern(), settings.getDateTimePattern(), settings.getDateTimePattern(), adjustBy);
    }

    private DateConverterForJodaLocalDateTime(String datePattern, String dateTimePattern, String datePickerPattern, int adjustBy) {
        super(LocalDateTime.class, datePattern, dateTimePattern, datePickerPattern, adjustBy);
    }

    @Override
    protected LocalDateTime doConvertToObject(String value, Locale locale) {
        LocalDateTime dateTime = convert(value);
        LocalDateTime adjustedDateTime = dateTime.minusDays(adjustBy);
        return adjustedDateTime;
    }

    private LocalDateTime convert(String value) {
        try {
            final LocalDateTime dateTime = getFormatterForDateTimePattern().parseLocalDateTime(value);
            return dateTime;
        } catch(IllegalArgumentException ex) {
            try {
                final LocalDateTime dateTime = getFormatterForDatePattern().parseLocalDateTime(value);
                return dateTime;
            } catch(IllegalArgumentException ex2) {
                throw new ConversionException("Cannot convert into a date/time", ex);
            }
        }
    }

    @Override
    protected String doConvertToString(LocalDateTime value, Locale locale) {
        return value.plusDays(adjustBy).toString(getFormatterForDateTimePattern());
    }


}
