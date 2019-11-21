package org.isisaddons.module.fakedata.dom;

import org.apache.isis.applib.annotation.Programmatic;

public class Enums extends AbstractRandomValueGenerator{

    public Enums(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public <T extends Enum<T>> T anyOf(Class<T> cls) {
        final T[] enumConstants = cls.getEnumConstants();
        return fake.collections().anyOf(enumConstants);
    }
}
