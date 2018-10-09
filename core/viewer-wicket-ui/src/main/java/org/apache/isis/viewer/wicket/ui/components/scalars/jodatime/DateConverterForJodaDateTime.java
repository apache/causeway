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

import org.apache.wicket.util.convert.ConversionException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.ui.components.scalars.DateFormatSettings;

public class DateConverterForJodaDateTime extends DateConverterForJodaAbstract<DateTime> {

    private static final long serialVersionUID = 1L;

    public DateConverterForJodaDateTime(WicketViewerSettings settings, int adjustBy) {
        super(DateTime.class, DateFormatSettings.ofDateAndTime(settings, adjustBy));
    }

    @Override
    protected DateTime minusDays(DateTime value, int adjustBy) {
        return value.minusDays(adjustBy);
    }

    @Override
    protected DateTime plusDays(DateTime value, int adjustBy) {
        return value.plusDays(adjustBy);
    }

    @Override
    protected DateTime convert(String value) throws ConversionException {
        try {
            return getFormatterForDateTimePattern().parseDateTime(value);
        } catch(IllegalArgumentException ex) {
            try {
                return getFormatterForDatePattern().parseDateTime(value);
            } catch(IllegalArgumentException ex2) {
                throw new ConversionException(String.format("Cannot convert '%s' into a date/time", value), ex2);
            }
        }
    }

    @Override
    protected String toString(DateTime value, DateTimeFormatter dateTimeFormatter) {
        return value.toString(dateTimeFormatter);
    }
}
