package org.apache.isis.extensions.fakedata.dom;

import java.sql.Date;
import java.time.OffsetDateTime;

import org.apache.isis.applib.annotation.Programmatic;

public class JavaSqlDates extends AbstractRandomValueGenerator{

    public JavaSqlDates(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public java.sql.Date any() {
        final OffsetDateTime dateTime = fake.jodaDateTimes().any();
        final Date sqldt = asSqlDate(dateTime);
        return sqldt;
    }

    private static Date asSqlDate(final OffsetDateTime dateTime) {
        long epochMillis = dateTime.toInstant().toEpochMilli();
        return new Date(epochMillis);
    }
}
