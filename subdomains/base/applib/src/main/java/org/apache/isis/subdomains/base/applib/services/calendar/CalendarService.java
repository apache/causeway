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
package org.apache.isis.subdomains.base.applib.services.calendar;

import lombok.RequiredArgsConstructor;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.clock.ClockService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("isis.sub.base.CalendarService")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class CalendarService {

    private static final int MONTHS_IN_QUARTER = 3;

    private final ClockService clockService;

    /**
     * Returns the date corresponding to the beginning of the current month.
     */
    public LocalDate beginningOfMonth() {
        return beginningOfMonth(nowAsLocalDate());
    }

    static LocalDate beginningOfMonth(final LocalDate date) {
        final long dayOfMonth = date.getDayOfMonth();
        return date.minusDays(dayOfMonth-1L);
    }

    /**
     * Returns the date corresponding to the beginning of the current quarter (typically: January, April, July or October).
     *
     * @see #beginningOfQuarter(LocalDate)
     * @see #beginningOfNextQuarter()
     */
    public LocalDate beginningOfQuarter() {
        return beginningOfQuarter(nowAsLocalDate());
    }

    /**
     * Returns the date corresponding to the beginning of the quarter following this one.
     *
     * @see #beginningOfQuarter()
     */
    public LocalDate beginningOfNextQuarter() {
        return beginningOfQuarter(nowAsLocalDate().plusMonths(3));
    }

    static LocalDate beginningOfQuarter(final LocalDate date) {
        final LocalDate beginningOfMonth = beginningOfMonth(date);
        final int monthOfYear = beginningOfMonth.getMonthValue();
        final int quarter = (monthOfYear-1)/MONTHS_IN_QUARTER; // 0, 1, 2, 3
        final int monthStartOfQuarter = quarter*MONTHS_IN_QUARTER+1;
        final long deltaMonth = (long)monthOfYear - monthStartOfQuarter;
        return beginningOfMonth.minusMonths(deltaMonth);
    }

    private LocalDate nowAsLocalDate() {
        return clockService.getClock().localDate(ZoneId.systemDefault());
    }

}
