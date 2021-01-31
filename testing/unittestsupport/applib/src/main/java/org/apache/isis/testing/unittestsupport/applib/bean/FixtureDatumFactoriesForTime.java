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
package org.apache.isis.testing.unittestsupport.applib.bean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * @since 2.0 {@index}
 */
public class FixtureDatumFactoriesForTime {

    public static PojoTester.FixtureDatumFactory<LocalTime> localTimes() {
        return new PojoTester.FixtureDatumFactory<>(
                LocalTime.class,
                LocalTime.of(11, 15),
                LocalTime.of(12, 20),
                LocalTime.of(13, 30),
                LocalTime.of(14, 45)
        );
    }

	public static PojoTester.FixtureDatumFactory<LocalDate> localDates() {
		return new PojoTester.FixtureDatumFactory<>(
				LocalDate.class,
				LocalDate.of(2012, 7, 19),
				LocalDate.of(2012, 7, 20),
				LocalDate.of(2012, 8, 19),
				LocalDate.of(2013, 7, 19)
		);
	}

	public static PojoTester.FixtureDatumFactory<LocalDateTime> localDateTimes() {
		return new PojoTester.FixtureDatumFactory<>(
				LocalDateTime.class,
				LocalDateTime.of(2012, 7, 19, 11, 15),
				LocalDateTime.of(2012, 7, 20, 12, 20),
				LocalDateTime.of(2012, 8, 19, 13, 30),
				LocalDateTime.of(2013, 7, 19, 14, 45)
		);
	}


	public static PojoTester.FixtureDatumFactory<OffsetDateTime> offsetDateTimes() {
		return new PojoTester.FixtureDatumFactory<>(
		        OffsetDateTime.class,
				OffsetDateTime.of(2012, 7, 19, 11, 15, 0, 0, ZoneOffset.UTC),
				OffsetDateTime.of(2012, 7, 20, 12, 20, 0, 0, ZoneOffset.UTC),
				OffsetDateTime.of(2012, 8, 19, 13, 30, 0, 0, ZoneOffset.UTC),
				OffsetDateTime.of(2013, 7, 19, 14, 45, 0, 0, ZoneOffset.UTC)
		);
	}


}
