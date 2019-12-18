package org.apache.isis.extensions.fakedata.dom.services;

import org.apache.commons.lang3.RandomUtils;
import org.apache.isis.applib.annotation.Programmatic;

public class Doubles extends AbstractRandomValueGenerator {

    public Doubles(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public double any() {
        return fake.booleans().coinFlip()
                ?  RandomUtils.nextDouble() * Double.MAX_VALUE
                : -RandomUtils.nextDouble() * Double.MAX_VALUE;
    }

    public double upTo(final double max) {
        return RandomUtils.nextDouble() * max;
    }
}
