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
package org.apache.isis.viewer.bdd.common.parsers;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * A mutable wrapper around a {@link DateFormat}, allowing the date and time
 * parts of the format to be specified independently specified.
 */
public class DateParser {

    /**
     * Taken from the {@link org.apache.isis.applib.value.Date}
     */
    private static final TimeZone UTC_TIME_ZONE;
    static {
        TimeZone timeZone = TimeZone.getTimeZone("Etc/UTC");
        if (timeZone == null) {
            timeZone = TimeZone.getTimeZone("UTC");
        }
        UTC_TIME_ZONE = timeZone;
    }

    private static final String DEFAULT_DATE_MASK = "yyyy-MM-dd";
    private static final String DEFAULT_TIME_MASK = "hh:mm";

    private String dateMask = DEFAULT_DATE_MASK;
    private String timeMask = DEFAULT_TIME_MASK;
    private DateFormat dateAndTimeFormat = null;
    private DateFormat dateOnlyFormat = null;
    private DateFormat timeOnlyFormat = null;

    public DateParser() {
    }

    public Date parse(final String dateAndOrTimeStr) {
        try {
            return getDateAndTimeFormat().parse(dateAndOrTimeStr);
        } catch (final ParseException e) {
            try {
                return getDateFormat().parse(dateAndOrTimeStr);
            } catch (final ParseException e2) {
                try {
                    return getTimeFormat().parse(dateAndOrTimeStr);
                } catch (final ParseException e3) {
                    return null;
                }
            }
        }
    }

    public void setDateFormat(final String dateMask) {
        this.dateMask = dateMask;
        invalidateFormats();
    }

    public void setTimeFormat(final String timeMask) {
        this.timeMask = timeMask;
        invalidateFormats();
    }

    private void invalidateFormats() {
        this.dateAndTimeFormat = null;
        this.dateOnlyFormat = null;
        this.timeOnlyFormat = null;
    }

    public String format(final Date resultDate) {
        return getDateAndTimeFormat().format(resultDate);
    }

    private DateFormat getDateAndTimeFormat() {
        if (dateAndTimeFormat == null) {
            dateAndTimeFormat = getUTCDateFormat(getCombinedMask());
        }
        return dateAndTimeFormat;
    }

    private DateFormat getTimeFormat() {
        if (timeOnlyFormat == null) {
            timeOnlyFormat = getUTCDateFormat(timeMask);
        }
        return timeOnlyFormat;
    }

    private DateFormat getDateFormat() {
        if (dateOnlyFormat == null) {
            dateOnlyFormat = getUTCDateFormat(dateMask);
        }
        return dateOnlyFormat;
    }

    private DateFormat getUTCDateFormat(final String dateTimeMask) {
        final DateFormat dateFormat = new SimpleDateFormat(dateTimeMask);
        dateFormat.setTimeZone(UTC_TIME_ZONE);
        return dateFormat;
    }

    public String getCombinedMask() {
        return MessageFormat.format("{0} {1}", dateMask, timeMask);
    }

    @Override
    public String toString() {
        return getCombinedMask();
    }

}
