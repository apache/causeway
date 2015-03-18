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
package org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;

import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;


public class DateConverterForJavaUtilDate extends DateConverterForJavaAbstract<java.util.Date> {
    private static final long serialVersionUID = 1L;
    
    public DateConverterForJavaUtilDate(WicketViewerSettings settings, int adjustBy) {
        this(settings.getDatePattern(), settings.getDateTimePattern(), settings.getDateTimePattern(), adjustBy);
    }
    public DateConverterForJavaUtilDate(String datePattern, String dateTimePattern, String datePickerPattern, int adjustBy) {
        super(java.util.Date.class, datePattern, dateTimePattern, datePickerPattern, adjustBy);
    }
    

    @Override
    protected java.util.Date doConvertToObject(String value, Locale locale) throws ConversionException {
        final Date date = convert(value);
        final Date adjustedDate = addDays(date, 0-adjustBy);
        return adjustedDate;
    }
    private java.util.Date convert(String valueStr) {
        try {
            return newSimpleDateFormatUsingDateTimePattern().parse(valueStr);
        } catch (ParseException ex) {
            try {
                return newSimpleDateFormatUsingDatePattern().parse(valueStr);
            } catch (ParseException ex2) {
                throw new ConversionException("Value cannot be converted as a date/time", ex);
            }
        }
    }

    @Override
    protected String doConvertToString(java.util.Date value, Locale locale) throws ConversionException {
        final Date adjustedDate = addDays(value, adjustBy);
        return newSimpleDateFormatUsingDateTimePattern().format(adjustedDate);
    }

    private static Date addDays(java.util.Date value, final int days) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(value);
        cal.add(Calendar.DATE, days);
        final Date adjusted = cal.getTime();
        return adjusted;
    }

}
