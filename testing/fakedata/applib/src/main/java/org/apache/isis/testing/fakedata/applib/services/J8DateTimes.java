package org.apache.isis.testing.fakedata.applib.services;

import java.time.OffsetDateTime;
import java.time.Period;

import org.apache.isis.applib.annotation.Programmatic;

public class J8DateTimes extends AbstractRandomValueGenerator {

    public J8DateTimes(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public OffsetDateTime around(final Period period) {
        return fake.booleans().coinFlip() ? before(period) : after(period);
    }

    @Programmatic
    public OffsetDateTime before(final Period period) {
        final OffsetDateTime now = fake.clockService.nowAsOffsetDateTime();
        return now.minus(period);
    }

    @Programmatic
    public OffsetDateTime after(final Period period) {
        final OffsetDateTime now = fake.clockService.nowAsOffsetDateTime();
        return now.plus(period);
    }

    @Programmatic
    public OffsetDateTime any() {
        final Period upTo5Years = fake.j8Periods().yearsUpTo(5);
        return around(upTo5Years);
    }
}
