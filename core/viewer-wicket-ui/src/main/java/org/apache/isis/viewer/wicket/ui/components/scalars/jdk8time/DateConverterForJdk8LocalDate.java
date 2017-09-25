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
package org.apache.isis.viewer.wicket.ui.components.scalars.jdk8time;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.wicket.util.convert.ConversionException;

/**
 * Uses the same pattern for both date and date/time.  The only real consequence of this is that when converting a
 * string value to the date value, only a single pattern is used.
 */
public class DateConverterForJdk8LocalDate extends DateConverterForJdk8Abstract<LocalDate> {
    
    private static final long serialVersionUID = 1L;

    public DateConverterForJdk8LocalDate(WicketViewerSettings settings, int adjustBy) {
        super(LocalDate.class, settings.getDatePattern(), settings.getDatePattern(), adjustBy);
    }

    @Override
    protected LocalDate minusDays(LocalDate value, int adjustBy) {
        return value.minusDays(adjustBy);
    }

    @Override
    protected LocalDate plusDays(LocalDate value, int adjustBy) {
        return value.plusDays(adjustBy);
    }

    @Override
    protected LocalDate convert(String value) throws ConversionException {
        try {
            return getFormatterForDateTimePattern8().parse(value, LocalDate::from);
        } catch(Exception ex) {
            throw new ConversionException(String.format("Cannot convert '%s' into a date/time", value), ex);
        }
    }
    


    @Override
    protected String toString(LocalDate value, DateTimeFormatter dateTimeFormatter) {
        return value.format(dateTimeFormatter);
    }
}
