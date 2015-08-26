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

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.apache.isis.viewer.wicket.ui.components.scalars.DateConverterAbstract;

public abstract class DateConverterForJodaAbstract<T> extends DateConverterAbstract<T> {
    
    private static final long serialVersionUID = 1L;
    
    protected DateConverterForJodaAbstract(Class<T> cls, String datePattern, String dateTimePattern, String datePickerPattern, int adjustBy) {
        super(cls, datePattern, dateTimePattern, datePickerPattern, adjustBy);
    }

    protected DateTimeFormatter getFormatterForDatePattern() {
        return DateTimeFormat.forPattern(datePattern);
    }

    protected DateTimeFormatter getFormatterForDateTimePattern() {
        return DateTimeFormat.forPattern(dateTimePattern);
    }
    
}