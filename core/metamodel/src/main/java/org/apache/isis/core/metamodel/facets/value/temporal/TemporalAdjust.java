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
package org.apache.isis.core.metamodel.facets.value.temporal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;

import org.apache.isis.core.commons.internal.exceptions._Exceptions;

import lombok.Value;

/**
 * 
 * @since 2.0
 * @see {@link TemporalAdjuster}
 */
@Value(staticConstructor = "of")
public class TemporalAdjust {

    private int years;
    private int months;
    private int days;
    private int hours;
    private int minutes;
    
    public static TemporalAdjust parse(String str) {
        int hours = 0;
        int minutes = 0;
        int days = 0;
        int months = 0;
        int years = 0;

        if (str.endsWith("H")) {
            str = str.substring(0, str.length() - 1);
            hours = Integer.valueOf(str).intValue();
        } else if (str.endsWith("M")) {
            str = str.substring(0, str.length() - 1);
            minutes = Integer.valueOf(str).intValue();
        } else if (str.endsWith("w")) {
            str = str.substring(0, str.length() - 1);
            days = 7 * Integer.valueOf(str).intValue();
        } else if (str.endsWith("y")) {
            str = str.substring(0, str.length() - 1);
            years = Integer.valueOf(str).intValue();
        } else if (str.endsWith("m")) {
            str = str.substring(0, str.length() - 1);
            months = Integer.valueOf(str).intValue();
        } else if (str.endsWith("d")) {
            str = str.substring(0, str.length() - 1);
            days = Integer.valueOf(str).intValue();
        } else {
            days = Integer.valueOf(str).intValue();
        }
        
        return TemporalAdjust.of(years, months, days, hours, minutes);
    }
    
    public TemporalAdjust sign(int sign) {
        if(sign==1) {
            return this;
        }
        if(sign==-1) {
            return of(-this.years, -this.months, -this.days, -this.hours, -this.minutes);
        }
        throw _Exceptions.unsupportedOperation();
    }
    
    
    public LocalDate adjustLocalDate(final LocalDate temporal) {
        if(hours != 0 || minutes != 0) {
            throw noTime(temporal);
        }
        return temporal.plusYears(years).plusMonths(months).plusDays(days);
    }

    public LocalTime adjustLocalTime(final LocalTime temporal) {
        if(years != 0 || months != 0 || days != 0) {
            throw noDate(temporal);
        }
        return temporal.plusHours(hours).plusMinutes(minutes);
    }
    
    public OffsetTime adjustOffsetTime(final OffsetTime temporal) {
        if(years != 0 || months != 0 || days != 0) {
            throw noDate(temporal);
        }
        return temporal.plusHours(hours).plusMinutes(minutes);
    }

    public LocalDateTime adjustLocalDateTime(final LocalDateTime temporal) {
        return temporal.plusYears(years).plusMonths(months).plusDays(days)
                .plusHours(hours).plusMinutes(minutes);
    }

    public OffsetDateTime adjustOffsetDateTime(final OffsetDateTime temporal) {
        return temporal.plusYears(years).plusMonths(months).plusDays(days)
                .plusHours(hours).plusMinutes(minutes);
    }
    
    public ZonedDateTime adjustZonedDateTime(final ZonedDateTime temporal) {
        return temporal.plusYears(years).plusMonths(months).plusDays(days)
                .plusHours(hours).plusMinutes(minutes);
    }
    
    // -- HELPER
    
    private IllegalArgumentException noTime(Temporal temporal) {
        return _Exceptions.illegalArgument("cannot add non-zero hours or minutes to a %s", 
                temporal.getClass().getName());
    }
    
    private IllegalArgumentException noDate(Temporal temporal) {
        throw _Exceptions.illegalArgument("cannot add non-zero years, months or days to a %s", 
                temporal.getClass().getName());
    }
    

}
