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

import java.sql.Timestamp;
import java.time.Period;
import java.util.Date;

/**
 * Returns a random {@link java.sql.Timestamp}, optionally based on the current time but constrained by a {@link Period}.
 *
 * <p>
 *     The current time ('now') is obtained from the {@link org.apache.causeway.applib.services.clock.ClockService}.
 * </p>
 *
 * @since 2.0 {@index}
 */
public class JavaSqlTimestamps extends AbstractRandomValueGenerator {

    public JavaSqlTimestamps(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    /**
     * Returns a random timestamp either before or after 'now', within the specified {@link Period}.
     */
    public java.sql.Timestamp around(final Period period) {
        return asTimestamp(fake.javaSqlDates().around(period));
    }

    /**
     * Returns a random timestamp some time before 'now', within the specified {@link Period}.
     */
    public java.sql.Timestamp before(final Period period) {
        return asTimestamp(fake.javaSqlDates().before(period));
    }

    /**
     * Returns a random timestamp some time after 'now', within the specified {@link Period}.
     */
    public java.sql.Timestamp after(final Period period) {
        return asTimestamp(fake.javaSqlDates().after(period));
    }

    /**
     * Returns a random timestamp 5 years around 'now'.
     */
    public java.sql.Timestamp any() {
        return asTimestamp(fake.javaUtilDates().any());
    }

    private static Timestamp asTimestamp(Date date) {
        return new Timestamp(date.getTime());
    }
}
