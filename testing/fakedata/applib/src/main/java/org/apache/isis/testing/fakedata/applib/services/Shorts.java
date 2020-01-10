package org.apache.isis.testing.fakedata.applib.services;

import org.apache.isis.applib.annotation.Programmatic;

public class Shorts extends AbstractRandomValueGenerator {

    public Shorts(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public short upTo(final short upTo) {
        return (short) fake.ints().upTo(upTo);
    }

    @Programmatic
    public short between(final short min, final short max) {
        return (short) fake.ints().between(min, max);
    }

    @Programmatic
    public short any() {
        return (short) fake.ints().any();
    }
}
