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

import java.io.Serializable;

import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;

public class DateFormatSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String datePattern;
    private final String dateTimePattern;
    private final int adjustBy;

    // -- FACTORIES

    public static DateFormatSettings of(String datePattern, String dateTimePattern, int adjustBy) {
        return new DateFormatSettings(datePattern, dateTimePattern, adjustBy);
    }

    public static DateFormatSettings of(String datePattern, String dateTimePattern) {
        return of(datePattern, dateTimePattern, /*adjustBy*/ 0);
    }

    public static DateFormatSettings of(String datePattern) {
        return of(datePattern, /*dateTimePattern same as*/ datePattern, /*adjustBy*/ 0);
    }

    public static DateFormatSettings ofDateAndTime(WicketViewerSettings settings, int adjustBy) {
        return of(settings.getDatePattern(), settings.getDateTimePattern(), adjustBy);
    }

    public static DateFormatSettings ofDateOnly(WicketViewerSettings settings, int adjustBy) {
        return of(settings.getDatePattern(), settings.getDatePattern(), adjustBy);
    }

    // -- IMPLEMENTATION

    private DateFormatSettings(String datePattern, String dateTimePattern, int adjustBy) {
        this.datePattern = datePattern;
        this.dateTimePattern = dateTimePattern;
        this.adjustBy = adjustBy;
    }

    public String getDatePattern() {
        return datePattern;
    }

    public String getDateTimePattern() {
        return dateTimePattern;
    }

    /**
     * day offset
     */
    public int getAdjustBy() {
        return adjustBy;
    }

    @Override
    public String toString() {
        return "DateFormatSettings [datePattern=" + datePattern + ", dateTimePattern=" + dateTimePattern + ", adjustBy="
                + adjustBy + "]";
    }



}
