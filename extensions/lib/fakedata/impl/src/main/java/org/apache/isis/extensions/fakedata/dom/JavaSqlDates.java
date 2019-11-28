package org.apache.isis.extensions.fakedata.dom;

import java.sql.Date;
import org.joda.time.DateTime;
import org.apache.isis.applib.annotation.Programmatic;

public class JavaSqlDates extends AbstractRandomValueGenerator{

    public JavaSqlDates(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public java.sql.Date any() {
        final DateTime dateTime = fake.jodaDateTimes().any();
        final Date sqldt = asSqlDate(dateTime);
        return sqldt;
    }

    private static Date asSqlDate(final DateTime dateTime) {
        final DateTime dateTimeAtStartOfDay = dateTime.withTimeAtStartOfDay();
        return new Date(dateTimeAtStartOfDay.toDate().getTime());
    }
}
