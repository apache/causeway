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

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;

import org.apache.isis.viewer.wicket.ui.components.scalars.DateConverterAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.DateFormatSettings;

abstract class DateConverterForJdk8Abstract<T> extends DateConverterAbstract<T> {

    private static final long serialVersionUID = 1L;

    DateConverterForJdk8Abstract(Class<T> cls, DateFormatSettings dateFormatSettings) {
        super(cls, dateFormatSettings);
    }
    
    @Override
    protected final T doConvertToObject(String value, Locale locale) {
        T dateTime = convert(value);
        return minusDays(dateTime, dateFormatSettings.getAdjustBy());
    }

    @Override
    protected String doConvertToString(T value, Locale locale) {
        // for JodaLocalDate, the date time pattern is same as date pattern, so can use either to convert to string.
        T t = plusDays(value, dateFormatSettings.getAdjustBy());
        return toString(t, DateTimeFormatter.ofPattern(dateFormatSettings.getDateTimePattern()));
    }

    protected DateTimeFormatter getFormatterForDatePattern8() {
        return DateTimeFormatter.ofPattern(dateFormatSettings.getDatePattern());
    }

    protected DateTimeFormatter getFormatterForDateTimePattern8() {
        return DateTimeFormatter.ofPattern(dateFormatSettings.getDateTimePattern());
    }

    protected abstract T minusDays(T value, int adjustBy);
    protected abstract T plusDays(T value, int adjustBy);

    protected abstract T convert(String value) throws ConversionException;
    protected abstract String toString(T value, DateTimeFormatter dateTimeFormatter);

}