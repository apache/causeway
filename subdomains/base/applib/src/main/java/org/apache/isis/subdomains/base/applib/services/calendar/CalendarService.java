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

import java.time.LocalDate;
import java.time.ZoneId;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.clock.ClockService;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("isis.sub.base.CalendarService")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class CalendarService {

    private static final int MONTHS_IN_QUARTER = 3;

    private ClockService clockService;

    @Inject
    public CalendarService(ClockService clockService) {
        this.clockService = clockService;
    }

    public LocalDate beginningOfMonth() {
        return beginningOfMonth(nowAsLocalDate());
    }

    static LocalDate beginningOfMonth(final LocalDate date) {
        final long dayOfMonth = date.getDayOfMonth();
        return date.minusDays(dayOfMonth-1L);
    }

    public LocalDate beginningOfQuarter() {
        return beginningOfQuarter(nowAsLocalDate());
    }

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

    /**
     * @deprecated - use {@link ClockService#getClock()}.getEpochMillis()
     */
    @Deprecated
    public long timestamp() {
        return clockService.getClock().getEpochMillis();
    }


}
