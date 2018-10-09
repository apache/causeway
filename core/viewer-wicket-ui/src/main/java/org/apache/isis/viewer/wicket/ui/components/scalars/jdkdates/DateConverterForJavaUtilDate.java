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

import java.util.Date;
import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;

import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.ui.components.scalars.DateFormatSettings;


public class DateConverterForJavaUtilDate extends DateConverterForJavaAbstract<java.util.Date> {
    private static final long serialVersionUID = 1L;

    public DateConverterForJavaUtilDate(WicketViewerSettings settings, int adjustBy) {
        this(DateFormatSettings.ofDateAndTime(settings, adjustBy));
    }

    private DateConverterForJavaUtilDate(DateFormatSettings dateFormatSettings) {
        super(java.util.Date.class, dateFormatSettings);
    }

    @Override
    protected java.util.Date doConvertToObject(String value, Locale locale) throws ConversionException {
        final Date date = parseDateTime(value);
        final Date adjustedDate = adjustDaysBackward(date);
        return adjustedDate;
    }

    @Override
    protected String doConvertToString(java.util.Date value, Locale locale) throws ConversionException {
        final Date adjustedDate = adjustDaysForward(value);
        return getDateTimeFormat().format(adjustedDate);
    }

    @Override
    protected Date temporalValueOf(Date date) {
        return date; //[ahuber] immutable, so just returning the same object
    }

}
