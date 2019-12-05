package org.apache.isis.extensions.fakedata.dom.types;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.extensions.fakedata.dom.AbstractRandomValueGenerator;
import org.apache.isis.extensions.fakedata.dom.FakeDataService;

public class Enums extends AbstractRandomValueGenerator {

    public Enums(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public <T extends Enum<T>> T anyOf(Class<T> cls) {
        final T[] enumConstants = cls.getEnumConstants();
        return fake.collections().anyOf(enumConstants);
    }
}
