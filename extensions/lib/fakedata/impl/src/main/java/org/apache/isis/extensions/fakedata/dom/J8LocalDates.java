package org.apache.isis.extensions.fakedata.dom;

import java.time.LocalDate;
import java.time.Period;

import org.apache.isis.applib.annotation.Programmatic;

public class J8LocalDates extends AbstractRandomValueGenerator{

    public J8LocalDates(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public LocalDate around(final Period period) {
        return fake.booleans().coinFlip() ? before(period) : after(period);
    }

    @Programmatic
    public LocalDate before(final Period period) {
        final LocalDate now = fake.clockService.now();
        return now.minus(period);
    }

    @Programmatic
    public LocalDate after(final Period period) {
        final LocalDate now = fake.clockService.now();
        return now.plus(period);
    }

    @Programmatic
    public LocalDate any() {
        final Period upTo5Years = fake.jodaPeriods().yearsUpTo(5);
        return around(upTo5Years);
    }
}
