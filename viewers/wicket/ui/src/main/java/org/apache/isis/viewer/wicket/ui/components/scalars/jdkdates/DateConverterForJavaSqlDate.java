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

import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;

import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.ui.components.scalars.DateFormatSettings;


public class DateConverterForJavaSqlDate extends DateConverterForJavaAbstract<java.sql.Date> {
    private static final long serialVersionUID = 1L;

    public DateConverterForJavaSqlDate(WicketViewerSettings settings, int adjustBy) {
        this(DateFormatSettings.ofDateOnly(settings, adjustBy));
    }

    private DateConverterForJavaSqlDate(DateFormatSettings dateFormatSettings) {
        super(java.sql.Date.class, dateFormatSettings);
    }

    @Override
    protected java.sql.Date doConvertToObject(String value, Locale locale) throws ConversionException {
        final java.sql.Date date = parseDateOnly(value);
        final java.sql.Date adjustedDate = adjustDaysBackward(date);
        return adjustedDate;
    }

    @Override
    protected String doConvertToString(java.sql.Date value, Locale locale) {
        final java.sql.Date adjustedDate = adjustDaysForward(value);
        return getDateOnlyFormat().format(adjustedDate);
    }

    @Override
    protected java.sql.Date temporalValueOf(java.util.Date date) {
        return new java.sql.Date(date.getTime());
    }

}
