package org.apache.isis.extensions.fakedata.dom.services;

public class Booleans extends AbstractRandomValueGenerator {

    public Booleans(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    /**
     * Same as {@link #any()}.
     */
    public boolean coinFlip() {
        return any();
    }

    /**
     * Same as {@link #any()}.
     */
    public boolean either() {
        return any();
    }

    public boolean diceRollOf6() {
        return fake.ints().upTo(6) == 5;
    }

    public boolean any() {
        return fake.randomService.nextDouble() < 0.5;
    }
}
