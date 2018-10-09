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
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.wicket.util.convert.ConversionException;

import org.apache.isis.viewer.wicket.ui.components.scalars.DateConverterAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.DateFormatSettings;

public abstract class DateConverterForJavaAbstract<T extends java.util.Date> extends DateConverterAbstract<T> {
    private static final long serialVersionUID = 1L;
    
    private transient SimpleDateFormat dateOnlyPattern;
    private transient SimpleDateFormat dateTimePattern;

    public DateConverterForJavaAbstract(Class<T> cls, DateFormatSettings dateFormatSettings) {
        super(cls, dateFormatSettings);
    }

    protected SimpleDateFormat getDateOnlyFormat() {
        if(dateOnlyPattern==null) {
            dateOnlyPattern = new SimpleDateFormat(dateFormatSettings.getDatePattern()); 
        }
        return dateOnlyPattern;
    }

    protected SimpleDateFormat getDateTimeFormat() {
        if(dateTimePattern==null) {
            dateTimePattern = new SimpleDateFormat(dateFormatSettings.getDateTimePattern()); 
        }
        return dateTimePattern;
    }

    protected <X extends java.util.Date> T adjustDaysForward(X value) {
        final int days = dateFormatSettings.getAdjustBy();
        if(days==0) {
            return temporalValueOf(value);
        }
        return temporalValueOf(addDaysTo(value, days));
    }
    
    protected <X extends java.util.Date> T adjustDaysBackward(X value) {
        final int days = dateFormatSettings.getAdjustBy();
        if(days==0) {
            return temporalValueOf(value);
        }
        return temporalValueOf(addDaysTo(value, -days));
    }    
    
    protected T parseDateTime(String valueStr) {
        try {
            java.util.Date parsed = getDateTimeFormat().parse(valueStr);
            return temporalValueOf(parsed);
        } catch (ParseException ex) {
            try {
                java.util.Date parsed = getDateOnlyFormat().parse(valueStr);
                return temporalValueOf(parsed);
            } catch (ParseException ex2) {
                throw new ConversionException("Value cannot be converted as a date/time", ex);
            }
        }
    }
    
    protected T parseDateOnly(String valueStr) {
        try {
            java.util.Date parsed =  getDateOnlyFormat().parse(valueStr);
            return temporalValueOf(parsed);
        } catch (ParseException e) {
            throw new ConversionException("Cannot convert into a date", e);
        }
    }
    
    protected abstract T temporalValueOf(java.util.Date date);
    
    // -- HELPER
    
    private final <X extends java.util.Date> T addDaysTo(X value, final int days) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(value);
        cal.add(Calendar.DATE, days);
        final java.util.Date adjusted = cal.getTime();
        return temporalValueOf(adjusted);
    }

}