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

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotations.PriorityPrecedence;
import org.apache.isis.applib.services.clock.ClockService;

import lombok.RequiredArgsConstructor;

@Service
@Named("isis.sub.base.CalendarServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class CalendarServiceDefault implements CalendarService {

    private final ClockService clockService;

    @Override
    public LocalDate beginningOfMonth() {
        return CalendarService.beginningOfMonth(nowAsLocalDate());
    }

    @Override
    public LocalDate beginningOfQuarter() {
        return CalendarService.beginningOfQuarter(nowAsLocalDate());
    }

    @Override
    public LocalDate beginningOfNextQuarter() {
        return CalendarService.beginningOfQuarter(nowAsLocalDate().plusMonths(3));
    }

    // -- HELPER

    private LocalDate nowAsLocalDate() {
        return clockService.getClock().nowAsLocalDate(ZoneId.systemDefault());
    }

}
