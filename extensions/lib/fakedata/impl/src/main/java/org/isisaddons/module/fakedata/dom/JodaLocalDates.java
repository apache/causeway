package org.isisaddons.module.fakedata.dom;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.apache.isis.applib.annotation.Programmatic;

public class JodaLocalDates extends AbstractRandomValueGenerator{

    public JodaLocalDates(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public LocalDate around(final Period period) {
        final LocalDate now = fake.clockService.now();
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
