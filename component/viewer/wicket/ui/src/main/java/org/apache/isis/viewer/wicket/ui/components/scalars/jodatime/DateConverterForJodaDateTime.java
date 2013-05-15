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

import org.joda.time.DateTime;

import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;

public class DateConverterForJodaDateTime extends DateConverterForJodaAbstract<DateTime> {
    
    private static final long serialVersionUID = 1L;

    public DateConverterForJodaDateTime(WicketViewerSettings settings) {
        this(settings.getDatePattern(), settings.getDateTimePattern(), settings.getDatePickerPattern());
    }
    
    private DateConverterForJodaDateTime(String datePattern, String dateTimePattern, String datePickerPattern) {
        super(DateTime.class, datePattern, dateTimePattern, datePickerPattern);
    }
    

    @Override
    protected DateTime doConvertToObject(String value, Locale locale) {
        try {
            return getFormatterForDateTimePattern().parseDateTime(value);
        } catch(IllegalArgumentException ex) {
            try {
                return getFormatterForDatePattern().parseDateTime(value);
            } catch(IllegalArgumentException ex2) {
                return null;
            }
        }
    }

    @Override
    protected String doConvertToString(DateTime value, Locale locale) {
        return value.toString(getFormatterForDateTimePattern());
    }

}