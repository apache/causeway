package org.apache.isis.testing.unittestsupport.applib.bean;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

public class FixtureDatumFactoriesForJoda {

	public static PojoTester.FixtureDatumFactory<LocalDate> dates() {
		return new PojoTester.FixtureDatumFactory<>(
				LocalDate.class,
				new LocalDate(2012, 7, 19),
				new LocalDate(2012, 7, 20),
				new LocalDate(2012, 8, 19),
				new LocalDate(2013, 7, 19)
		);
	}

	public static PojoTester.FixtureDatumFactory<LocalDateTime> localDateTimes() {
		return new PojoTester.FixtureDatumFactory<>(
				LocalDateTime.class,
				new LocalDateTime(2012, 7, 19, 11, 15),
				new LocalDateTime(2012, 7, 20, 12, 20),
				new LocalDateTime(2012, 8, 19, 13, 30),
				new LocalDateTime(2013, 7, 19, 14, 45)
		);
	}


	public static PojoTester.FixtureDatumFactory<DateTime> dateTimes() {
		return new PojoTester.FixtureDatumFactory<>(
				DateTime.class,
				new DateTime(2012, 7, 19, 11, 15),
				new DateTime(2012, 7, 20, 12, 20),
				new DateTime(2012, 8, 19, 13, 30),
				new DateTime(2013, 7, 19, 14, 45)
		);
	}


}
