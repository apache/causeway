package org.apache.isis.testing.fakedata.applib.services;

import org.apache.isis.applib.annotation.Programmatic;

public class Bytes extends AbstractRandomValueGenerator {

    public Bytes(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public byte upTo(final byte upTo) {
        return (byte) fake.ints().upTo(upTo);
    }

    @Programmatic
    public byte between(final byte min, final byte max) {
        return (byte) fake.ints().between(min, max);
    }

    @Programmatic
    public byte any() {
        return (byte) fake.ints().any();
    }
}
