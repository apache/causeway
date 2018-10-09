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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.ui.components.scalars.DateFormatSettings;

import org.apache.wicket.util.convert.ConversionException;

public class DateConverterForJdk8OffsetDateTime extends DateConverterForJdk8Abstract<OffsetDateTime> {

    private static final long serialVersionUID = 1L;

    public DateConverterForJdk8OffsetDateTime(WicketViewerSettings settings, int adjustBy) {
        super(OffsetDateTime.class, DateFormatSettings.ofDateAndTime(settings, adjustBy));
    }

    @Override
    protected OffsetDateTime minusDays(OffsetDateTime value, int adjustBy) {
        return value.minusDays(adjustBy);
    }

    @Override
    protected OffsetDateTime plusDays(OffsetDateTime value, int adjustBy) {
        return value.plusDays(adjustBy);
    }

    @Override
    protected OffsetDateTime convert(String value) throws ConversionException {

        return OffsetDateTime.of(
                convertToLocal(value),
                defaultOffset()
                );

    }

    @Override
    protected String toString(OffsetDateTime value, DateTimeFormatter dateTimeFormatter) {
        return value.format(dateTimeFormatter);
    }

    // -- HELPER

    private LocalDateTime convertToLocal(String value) throws ConversionException {
        try {
            return getFormatterForDateTimePattern8().parse(value, LocalDateTime::from);
        } catch(Exception ex) {

            try {

                return LocalDateTime.of(
                        getFormatterForDatePattern8().parse(value, LocalDate::from),
                        LocalTime.of(0, 0)	);

            } catch(Exception ex2) {

                throw new ConversionException(
                        String.format(
                                "Cannot convert '%s' into a date/time using pattern '%s' or '%s'",
                                value, dateFormatSettings.getDateTimePattern(), dateFormatSettings.getDatePattern()),
                        ex2	);
            }
        }
    }

    private ZoneOffset defaultOffset() {
        return OffsetDateTime.now().getOffset();

    }
}
