package org.isisaddons.module.fakedata.dom;

import org.apache.commons.lang3.RandomUtils;
import org.apache.isis.applib.annotation.Programmatic;

public class Integers extends AbstractRandomValueGenerator{

    public Integers(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public int upTo(final int upTo) {
        return fake.randomService.nextInt(upTo);
    }

    @Programmatic
    public int between(final int min, final int max) {
        return min + fake.randomService.nextInt(max - min);
    }

    @Programmatic
    public int any() {
        return RandomUtils.nextInt();
    }
}
