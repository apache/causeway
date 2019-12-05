package org.apache.isis.extensions.fakedata.dom.services;

import java.time.Period;

import org.apache.isis.applib.annotation.Programmatic;

public class J8Periods extends AbstractRandomValueGenerator {

    public J8Periods(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public Period daysBetween(final int minDays, final int maxDays) {
        return Period.ofDays(fake.ints().between(minDays, maxDays));
    }

    @Programmatic
    public Period daysUpTo(final int maxDays) {
        return daysBetween(0, maxDays);
    }

    @Programmatic
    public Period monthsBetween(final int minMonths, final int maxMonths) {
        return Period.ofMonths(fake.ints().between(minMonths, maxMonths));
    }

    @Programmatic
    public Period monthsUpTo(final int months) {
        return monthsBetween(0, months);
    }

    @Programmatic
    public Period yearsBetween(final int minYears, final int maxYears) {
        return Period.ofYears(fake.ints().between(minYears, maxYears));
    }

    @Programmatic
    public Period yearsUpTo(final int years) {
        return yearsBetween(0, years);
    }

}
