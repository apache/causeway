package org.isisaddons.module.fakedata.dom;

import org.apache.isis.applib.annotation.Programmatic;

public class Booleans extends AbstractRandomValueGenerator {

    public Booleans(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    /**
     * Same as {@link #any()}.
     */
    @Programmatic
    public boolean coinFlip() {
        return any();
    }

    /**
     * Same as {@link #any()}.
     */
    @Programmatic
    public boolean either() {
        return any();
    }

    @Programmatic
    public boolean diceRollOf6() {
        return fake.ints().upTo(6) == 5;
    }

    @Programmatic
    public boolean any() {
        return fake.randomService.nextDouble() < 0.5;
    }
}
