package org.apache.isis.extensions.fakedata.dom;

import org.joda.time.DateTime;
import org.apache.isis.applib.annotation.Programmatic;

public class JavaUtilDates extends AbstractRandomValueGenerator{

    public JavaUtilDates(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public java.util.Date any() {
        final DateTime dateTime = fake.jodaDateTimes().any();
        return dateTime.toDate();
    }
}
