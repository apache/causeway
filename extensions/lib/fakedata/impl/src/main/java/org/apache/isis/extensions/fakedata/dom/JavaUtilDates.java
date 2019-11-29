package org.apache.isis.extensions.fakedata.dom;

import java.time.OffsetDateTime;

import org.apache.isis.applib.annotation.Programmatic;

import lombok.val;

public class JavaUtilDates extends AbstractRandomValueGenerator{

    public JavaUtilDates(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public java.util.Date any() {
        final OffsetDateTime dateTime = fake.jodaDateTimes().any();
        val epochMillis = dateTime.toInstant().toEpochMilli();
        return new java.util.Date(epochMillis); 
    }
}
