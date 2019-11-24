package org.isisaddons.module.fakedata.dom;

import org.apache.isis.applib.annotation.Programmatic;

public class Names extends AbstractRandomValueGenerator {
    com.github.javafaker.Name javaFakerName;

    Names(final FakeDataService fakeDataService) {
        super(fakeDataService);
        javaFakerName = fakeDataService.javaFaker().name();
    }

    @Programmatic
    public String fullName() {
        return javaFakerName.name();
    }

    @Programmatic
    public String firstName() {
        return javaFakerName.firstName();
    }

    @Programmatic
    public String lastName() {
        return javaFakerName.lastName();
    }

    @Programmatic
    public String prefix() {
        return javaFakerName.prefix();
    }

    @Programmatic
    public String suffix() {
        return javaFakerName.suffix();
    }
}
