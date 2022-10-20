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

import java.time.OffsetDateTime;
import java.time.Period;
import java.util.Date;

import lombok.val;

/**
 * Returns a random {@link java.util.Date}, optionally based on the current time but constrained by a {@link Period}.
 *
 * <p>
 *     The current time ('now') is obtained from the {@link org.apache.causeway.applib.services.clock.ClockService}.
 * </p>
 *
 * @since 2.0 {@index}
 */
public class JavaUtilDates extends AbstractRandomValueGenerator {

    public JavaUtilDates(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    /**
     * Returns a random date either before or after 'now', within the specified {@link Period}.
     */
    public Date around(final Period period) {
        return toJavaUtilDate(fake.javaTimeDateTimes().around(period));
    }

    /**
     * Returns a random date some time before 'now', within the specified {@link Period}.
     */
    public Date before(final Period period) {
        return toJavaUtilDate(fake.javaTimeDateTimes().before(period));
    }

    /**
     * Returns a random date some time after 'now', within the specified {@link Period}.
     */
    public Date after(final Period period) {
        return toJavaUtilDate(fake.javaTimeDateTimes().after(period));
    }

    /**
     * Returns a random date 5 years around 'now'.
     */
    public java.util.Date any() {
        return toJavaUtilDate(fake.javaTimeDateTimes().any());
    }

    private static Date toJavaUtilDate(OffsetDateTime dateTime) {
        val epochMillis = dateTime.toInstant().toEpochMilli();
        return new Date(epochMillis);
    }
}
