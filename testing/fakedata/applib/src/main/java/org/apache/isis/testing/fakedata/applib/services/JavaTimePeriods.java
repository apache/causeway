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
package org.apache.isis.testing.fakedata.applib.services;

import java.time.Period;

import lombok.val;

/**
 * Returns random {@link Period}s constrained to last a certain number number of days, months and/or years.
 *
 * @since 2.0 {@index}
 */
public class JavaTimePeriods extends AbstractRandomValueGenerator {

    public JavaTimePeriods(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    public Period daysBetween(final int minDays, final int maxDays) {
        return Period.ofDays(fake.ints().between(minDays, maxDays));
    }

    public Period daysUpTo(final int maxDays) {
        return daysBetween(0, maxDays);
    }

    public Period monthsBetween(final int minMonths, final int maxMonths) {
        return Period.ofMonths(fake.ints().between(minMonths, maxMonths));
    }

    public Period monthsUpTo(final int months) {
        return monthsBetween(0, months);
    }

    public Period yearsBetween(final int minYears, final int maxYears) {
        return Period.ofYears(fake.ints().between(minYears, maxYears));
    }

    public Period yearsUpTo(final int years) {
        return yearsBetween(0, years);
    }

    public Period within(Period period) {
        val newDays = fake.ints().between(0, period.getDays());
        val newMonths = fake.ints().between(0, period.getMonths());
        val newYears = fake.ints().between(0, period.getYears());
        return Period.of(newYears, newMonths, newDays);
    }
}
