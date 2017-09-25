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

import org.apache.wicket.util.convert.ConversionException;
import org.apache.isis.viewer.wicket.ui.components.scalars.DateConverterAbstract;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

abstract class DateConverterForJdk8Abstract<T> extends DateConverterAbstract<T> {
    
    private static final long serialVersionUID = 1L;
    
    DateConverterForJdk8Abstract(Class<T> cls, String datePattern, String dateTimePattern, int adjustBy) {
        super(cls, datePattern, dateTimePattern, adjustBy);
    }

    @Override
    protected final T doConvertToObject(String value, Locale locale) {
        T dateTime = convert(value);
        return minusDays(dateTime, adjustBy);
    }

    @Override
    protected String doConvertToString(T value, Locale locale) {
        // for JodaLocalDate, the date time pattern is same as date pattern, so can use either to convert to string.
        T t = plusDays(value, adjustBy);
        return toString(t, DateTimeFormatter.ofPattern(dateTimePattern));
    }

    protected DateTimeFormatter getFormatterForDatePattern8() {
        return DateTimeFormatter.ofPattern(datePattern);
    }

    protected DateTimeFormatter getFormatterForDateTimePattern8() {
        return DateTimeFormatter.ofPattern(dateTimePattern);
    }

    protected abstract T minusDays(T value, int adjustBy);
    protected abstract T plusDays(T value, int adjustBy);

    protected abstract T convert(String value) throws ConversionException;
    protected abstract String toString(T value, DateTimeFormatter dateTimeFormatter);

}