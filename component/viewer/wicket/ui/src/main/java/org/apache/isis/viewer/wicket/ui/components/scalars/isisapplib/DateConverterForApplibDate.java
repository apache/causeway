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
package org.apache.isis.viewer.wicket.ui.components.scalars.isisapplib;

import java.util.Locale;

import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.ui.components.scalars.DateConverterAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates.DateConverterForJavaUtilDate;

public class DateConverterForApplibDate extends DateConverterAbstract<org.apache.isis.applib.value.Date> {

    private static final long serialVersionUID = 1L;
    
    private final DateConverterForJavaUtilDate converter;
    
    public DateConverterForApplibDate(WicketViewerSettings settings, int adjustBy) {
        this(settings.getDatePattern(), settings.getDatePattern(), adjustBy);
    }

    private DateConverterForApplibDate(String datePattern, String datePickerPattern, int adjustBy) {
        super(org.apache.isis.applib.value.Date.class, datePattern, datePattern, datePickerPattern, adjustBy);
        converter = new DateConverterForJavaUtilDate(datePattern, datePattern, datePickerPattern, adjustBy);
    }
    
    @Override
    protected org.apache.isis.applib.value.Date doConvertToObject(String value, Locale locale) {
        final java.util.Date javaUtilDate = converter.convertToObject(value, locale);
        return new org.apache.isis.applib.value.Date(javaUtilDate);
    }

    @Override
    protected String doConvertToString(org.apache.isis.applib.value.Date value, Locale locale) {
        return converter.convertToString(value.dateValue(), locale);
    }


}
