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

import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;

import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.ui.components.scalars.DateFormatSettings;


public class DateConverterForJavaSqlTimestamp extends DateConverterForJavaAbstract<java.sql.Timestamp> {
    private static final long serialVersionUID = 1L;

    public DateConverterForJavaSqlTimestamp(WicketViewerSettings settings, int adjustBy) {
        this(DateFormatSettings.ofDateAndTime(settings, adjustBy));
    }

    private DateConverterForJavaSqlTimestamp(DateFormatSettings dateFormatSettings) {
        super(java.sql.Timestamp.class, dateFormatSettings);
    }

    @Override
    protected java.sql.Timestamp doConvertToObject(String value, Locale locale) throws ConversionException {
        final java.sql.Timestamp date = parseDateTime(value);
        final java.sql.Timestamp adjustedDate = adjustDaysBackward(date);
        return adjustedDate;
    }

    @Override
    protected String doConvertToString(java.sql.Timestamp value, Locale locale) throws ConversionException {
        final java.sql.Timestamp adjustedDate = adjustDaysForward(value);
        return getDateTimeFormat().format(adjustedDate);
    }

    @Override
    protected Timestamp temporalValueOf(Date date) {
        return new java.sql.Timestamp(date.getTime());
    }

}
