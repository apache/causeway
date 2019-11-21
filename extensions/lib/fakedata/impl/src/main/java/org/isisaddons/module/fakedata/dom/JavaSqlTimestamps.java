package org.isisaddons.module.fakedata.dom;

import java.sql.Timestamp;
import java.util.Date;
import org.apache.isis.applib.annotation.Programmatic;

public class JavaSqlTimestamps extends AbstractRandomValueGenerator{

    public JavaSqlTimestamps(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public java.sql.Timestamp any() {
        final Date date = fake.javaUtilDates().any();
        return new Timestamp(date.getTime());
    }
}
