package org.apache.isis.extensions.fakedata.dom.types;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.extensions.fakedata.dom.AbstractRandomValueGenerator;
import org.apache.isis.extensions.fakedata.dom.FakeDataService;

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
