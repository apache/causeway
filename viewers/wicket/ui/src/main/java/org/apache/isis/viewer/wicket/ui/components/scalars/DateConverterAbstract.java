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
package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public abstract class DateConverterAbstract<T> implements DateConverter<T> {

    private static final long serialVersionUID = 1L;

    private final Class<T> cls;
    protected final DateFormatSettings dateFormatSettings;

    protected DateConverterAbstract(Class<T> cls, DateFormatSettings dateFormatSettings) {
        this.cls = cls;
        this.dateFormatSettings = dateFormatSettings;
    }

    @Override
    public Class<T> getConvertableClass() {
        return cls;
    }

    @Override
    public String getDatePattern(Locale locale) {
        return dateFormatSettings.getDatePattern();
    }

    @Override
    public String getDateTimePattern(Locale locale) {
        return dateFormatSettings.getDateTimePattern();
    }

    protected DateTimeFormatter getFormatterForDatePattern() {
        return DateTimeFormat.forPattern(dateFormatSettings.getDatePattern());
    }

    protected DateTimeFormatter getFormatterForDateTimePattern() {
        return DateTimeFormat.forPattern(dateFormatSettings.getDateTimePattern());
    }

    @Override
    public T convertToObject(String value, Locale locale) throws ConversionException {
        return value != null? doConvertToObject(value, locale): null;
    }
    @Override
    public String convertToString(T value, Locale locale) {
        return value != null? doConvertToString(value, locale): null;
    }

    protected abstract T doConvertToObject(String value, Locale locale) throws ConversionException;


    protected abstract String doConvertToString(T value, Locale locale);
}