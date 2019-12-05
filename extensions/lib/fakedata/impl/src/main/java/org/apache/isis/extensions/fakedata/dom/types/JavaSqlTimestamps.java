package org.apache.isis.extensions.fakedata.dom.types;

import java.sql.Timestamp;
import java.util.Date;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.extensions.fakedata.dom.AbstractRandomValueGenerator;
import org.apache.isis.extensions.fakedata.dom.FakeDataService;

public class JavaSqlTimestamps extends AbstractRandomValueGenerator {

    public JavaSqlTimestamps(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public java.sql.Timestamp any() {
        final Date date = fake.javaUtilDates().any();
        return new Timestamp(date.getTime());
    }
}
