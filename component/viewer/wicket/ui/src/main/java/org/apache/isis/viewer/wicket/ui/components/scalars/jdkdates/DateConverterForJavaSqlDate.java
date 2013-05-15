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
import java.util.Locale;

import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;


public class DateConverterForJavaSqlDate extends DateConverterForJavaAbstract<java.sql.Date> {
    private static final long serialVersionUID = 1L;
    
    public DateConverterForJavaSqlDate(WicketViewerSettings settings) {
        this(settings.getDatePattern(), settings.getDatePickerPattern());
    }

    private DateConverterForJavaSqlDate(String datePattern, String datePickerPattern) {
        super(java.sql.Date.class, datePattern, datePattern, datePickerPattern);
    }

    @Override
    protected java.sql.Date doConvertToObject(String value, Locale locale) {
        try {
            final java.util.Date parsedJavaUtilDate = newSimpleDateFormatUsingDatePattern().parse(value);
            return new java.sql.Date(parsedJavaUtilDate.getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    protected String doConvertToString(java.sql.Date value, Locale locale) {
        return newSimpleDateFormatUsingDatePattern().format(value);
    }

    
}