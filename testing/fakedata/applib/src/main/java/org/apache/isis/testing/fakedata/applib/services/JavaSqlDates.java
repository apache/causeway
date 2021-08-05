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

import java.sql.Date;
import java.time.OffsetDateTime;
import java.time.Period;

/**
 * Returns a random {@link Date}, optionally based on the current time but constrained by a {@link Period}.
 *
 * <p>
 *     The current time ('now') is obtained from the {@link org.apache.isis.applib.services.clock.ClockService}.
 * </p>
 *
 * @since 2.0 {@index}
 */
public class JavaSqlDates extends AbstractRandomValueGenerator {

    public JavaSqlDates(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    /**
     * Returns a random date either before or after 'now', within the specified {@link Period}.
     */
    public Date around(final Period period) {
        return asSqlDate(fake.javaTimeDateTimes().around(period));
    }

    /**
     * Returns a random date some time before 'now', within the specified {@link Period}.
     */
    public Date before(final Period period) {
        return asSqlDate(fake.javaTimeDateTimes().before(period));
    }

    /**
     * Returns a random date some time after 'now', within the specified {@link Period}.
     */
    public java.sql.Date after(final Period period) {
        return asSqlDate(fake.javaTimeDateTimes().after(period));
    }

    /**
     * Returns a random date 5 years around 'now'.
     */
    public java.sql.Date any() {
        return asSqlDate(fake.javaTimeDateTimes().any());
    }

    private static Date asSqlDate(final OffsetDateTime dateTime) {
        long epochMillis = dateTime.toInstant().toEpochMilli();
        return new Date(epochMillis);
    }


}
