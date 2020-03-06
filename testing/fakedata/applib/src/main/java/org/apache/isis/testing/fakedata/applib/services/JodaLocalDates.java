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

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.apache.isis.applib.annotation.Programmatic;

public class JodaLocalDates extends AbstractRandomValueGenerator{

    public JodaLocalDates(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public LocalDate around(final Period period) {
        return fake.booleans().coinFlip() ? before(period) : after(period);
    }

    @Programmatic
    public org.joda.time.LocalDate before(final Period period) {
        final org.joda.time.LocalDate now = fake.clockService.nowAsJodaLocalDate();
        return now.minus(period);
    }

    @Programmatic
    public org.joda.time.LocalDate after(final Period period) {
        final org.joda.time.LocalDate now = fake.clockService.nowAsJodaLocalDate();
        return now.plus(period);
    }

    @Programmatic
    public LocalDate any() {
        final org.joda.time.Period upTo5Years = fake.jodaPeriods().yearsUpTo(5);
        return around(upTo5Years);
    }
}