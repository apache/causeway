package org.apache.isis.extensions.fakedata.dom.types;

import java.time.OffsetDateTime;
import java.time.Period;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.extensions.fakedata.dom.AbstractRandomValueGenerator;
import org.apache.isis.extensions.fakedata.dom.FakeDataService;

public class J8DateTimes extends AbstractRandomValueGenerator {

    public J8DateTimes(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public OffsetDateTime around(final Period period) {
        final OffsetDateTime now = fake.clockService.nowAsOffsetDateTime();
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
        final Period upTo5Years = fake.jodaPeriods().yearsUpTo(5);
        return around(upTo5Years);
    }
}
