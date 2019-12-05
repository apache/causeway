package org.apache.isis.extensions.fakedata.dom.types;

import org.apache.commons.lang3.RandomUtils;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.extensions.fakedata.dom.AbstractRandomValueGenerator;
import org.apache.isis.extensions.fakedata.dom.FakeDataService;

public class Floats extends AbstractRandomValueGenerator {

    public Floats(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public float any() {
        return RandomUtils.nextFloat();
    }
}
