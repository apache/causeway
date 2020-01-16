package org.apache.isis.testing.fakedata.applib.services;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.apache.isis.applib.annotation.Programmatic;

public class JodaLocalDates extends AbstractRandomValueGenerator{

    public JodaLocalDates(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public LocalDate around(final Period period) {
        return fake.booleans().coinFlip() ? before(period) : after(period);
    }

    @Programmatic
    public org.joda.time.LocalDate before(final Period period) {
        final org.joda.time.LocalDate now = fake.clockService.nowAsJodaLocalDate();
        return now.minus(period);
    }

    @Programmatic
    public org.joda.time.LocalDate after(final Period period) {
        final org.joda.time.LocalDate now = fake.clockService.nowAsJodaLocalDate();
        return now.plus(period);
    }

    @Programmatic
    public LocalDate any() {
        final org.joda.time.Period upTo5Years = fake.jodaPeriods().yearsUpTo(5);
        return around(upTo5Years);
    }
}