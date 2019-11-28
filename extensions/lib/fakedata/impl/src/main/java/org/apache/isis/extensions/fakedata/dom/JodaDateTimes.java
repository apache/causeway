package org.apache.isis.extensions.fakedata.dom;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.apache.isis.applib.annotation.Programmatic;

public class JodaDateTimes extends AbstractRandomValueGenerator{

    public JodaDateTimes(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public DateTime around(final Period period) {
        final DateTime now = fake.clockService.nowAsDateTime();
        return fake.booleans().coinFlip() ? before(period) : after(period);
    }

    @Programmatic
    public DateTime before(final Period period) {
        final DateTime now = fake.clockService.nowAsDateTime();
        return now.minus(period);
    }

    @Programmatic
    public DateTime after(final Period period) {
        final DateTime now = fake.clockService.nowAsDateTime();
        return now.plus(period);
    }

    @Programmatic
    public DateTime any() {
        final Period upTo5Years = fake.jodaPeriods().yearsUpTo(5);
        return around(upTo5Years);
    }
}
