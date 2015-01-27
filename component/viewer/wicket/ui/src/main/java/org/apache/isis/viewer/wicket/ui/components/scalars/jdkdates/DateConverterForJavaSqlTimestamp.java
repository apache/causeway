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


public class DateConverterForJavaSqlTimestamp extends DateConverterForJavaAbstract<java.sql.Timestamp> {
    private static final long serialVersionUID = 1L;
    
    public DateConverterForJavaSqlTimestamp(WicketViewerSettings settings, int adjustBy) {
        this(settings.getDatePattern(), settings.getTimestampPattern(), adjustBy);
    }
    public DateConverterForJavaSqlTimestamp(final String datePattern, String timestampPattern, int adjustBy) {
        super(java.sql.Timestamp.class, datePattern, timestampPattern, timestampPattern, adjustBy);
    }

    @Override
    protected java.sql.Timestamp doConvertToObject(String value, Locale locale) throws ConversionException {
        final java.sql.Timestamp date = convert(value);
        final java.sql.Timestamp adjustedDate = addDays(date, 0-adjustBy);
        return adjustedDate;
    }

    private java.sql.Timestamp convert(String valueStr) {
        try {
            Date parsed = newSimpleDateFormatUsingDateTimePattern().parse(valueStr);
            return new java.sql.Timestamp(parsed.getTime());
        } catch (ParseException ex) {
            try {
                return new java.sql.Timestamp(newSimpleDateFormatUsingDatePattern().parse(valueStr).getTime());
            } catch (ParseException ex2) {
                throw new ConversionException("Value cannot be converted as a date/time", ex);
            }
        }
    }

    @Override
    protected String doConvertToString(java.sql.Timestamp value, Locale locale) throws ConversionException {
        final java.sql.Timestamp adjustedDate = addDays(value, adjustBy);
        return newSimpleDateFormatUsingDateTimePattern().format(adjustedDate);
    }

    private static java.sql.Timestamp addDays(java.util.Date value, final int days) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(value);
        cal.add(Calendar.DATE, days);
        final java.sql.Timestamp adjusted = new java.sql.Timestamp(cal.getTime().getTime());
        return adjusted;
    }

}
