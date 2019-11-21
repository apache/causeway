package org.isisaddons.module.fakedata.dom;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.isis.applib.annotation.Programmatic;

public class Doubles extends AbstractRandomValueGenerator  {

    public Doubles(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public double any() {
        return fake.booleans().coinFlip()
                ?  RandomUtils.nextDouble(fake.random) * Double.MAX_VALUE
                : -RandomUtils.nextDouble(fake.random) * Double.MAX_VALUE;
    }

    public double upTo(final double max) {
        return RandomUtils.nextDouble() * max;
    }
}
