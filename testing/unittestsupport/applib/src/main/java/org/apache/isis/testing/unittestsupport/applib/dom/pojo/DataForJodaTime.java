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
package org.apache.isis.testing.unittestsupport.applib.dom.pojo;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

/**
 * @since 2.0 {@index}
 */
public class DataForJodaTime {

	public static PojoTester.DatumFactory<LocalDate> localDates() {
		return new PojoTester.DatumFactory<>(
				LocalDate.class,
				new LocalDate(2012, 7, 19),
				new LocalDate(2012, 7, 20),
				new LocalDate(2012, 8, 19),
				new LocalDate(2013, 7, 19)
		);
	}

	public static PojoTester.DatumFactory<LocalTime> localTimes() {
		return new PojoTester.DatumFactory<>(
				LocalTime.class,
				new LocalTime(7, 19, 11, 15),
				new LocalTime(7, 20, 12, 20),
				new LocalTime(8, 19, 13, 30),
				new LocalTime(7, 19, 14, 45)
		);
	}

	public static PojoTester.DatumFactory<LocalDateTime> localDateTimes() {
		return new PojoTester.DatumFactory<>(
				LocalDateTime.class,
				new LocalDateTime(2012, 7, 19, 11, 15),
				new LocalDateTime(2012, 7, 20, 12, 20),
				new LocalDateTime(2012, 8, 19, 13, 30),
				new LocalDateTime(2013, 7, 19, 14, 45)
		);
	}


	public static PojoTester.DatumFactory<DateTime> dateTimes() {
		return new PojoTester.DatumFactory<>(
				DateTime.class,
				new DateTime(2012, 7, 19, 11, 15),
				new DateTime(2012, 7, 20, 12, 20),
				new DateTime(2012, 8, 19, 13, 30),
				new DateTime(2013, 7, 19, 14, 45)
		);
	}


}
