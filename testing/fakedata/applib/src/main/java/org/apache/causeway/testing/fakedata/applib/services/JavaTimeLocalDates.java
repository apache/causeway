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
package org.apache.causeway.testing.fakedata.applib.services;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

import lombok.val;

/**
 * Returns a random {@link LocalDate}, optionally based on the current time but constrained by a {@link Period}.
 *
 * <p>
 *     The current time ('now') is obtained from the {@link org.apache.causeway.applib.services.clock.ClockService}.
 * </p>
 *
 * @since 2.0 {@index}
 */
public class JavaTimeLocalDates extends AbstractRandomValueGenerator {

    public JavaTimeLocalDates(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    /**
     * Returns a random date either before or after 'now', within the specified {@link Period}.
     */
    public LocalDate around(final Period period) {
        return fake.booleans().coinFlip() ? before(period) : after(period);
    }

    /**
     * Returns a random date some time before 'now', within the specified {@link Period}.
     */
    public LocalDate before(final Period period) {
        val periodWithin = fake.javaTimePeriods().within(period);
        return now().minus(periodWithin);
    }

    /**
     * Returns a random date some time after 'now', within the specified {@link Period}.
     */
    public LocalDate after(final Period period) {
        val periodWithin = fake.javaTimePeriods().within(period);
        return now().plus(periodWithin);
    }

    /**
     * Returns a random date 5 years around 'now'.
     */
    public LocalDate any() {
        val periodUpTo5Years = fake.javaTimePeriods().yearsUpTo(5);
        return around(periodUpTo5Years);
    }

    private LocalDate now() {
        return fake.clockService.getClock().nowAsLocalDate(ZoneId.systemDefault());
    }


}
