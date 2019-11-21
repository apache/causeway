package org.isisaddons.module.fakedata.dom;

import org.apache.isis.applib.annotation.Programmatic;

public class Books extends AbstractRandomValueGenerator {

    com.github.javafaker.Code javaFakerCode;

    Books(final FakeDataService fakeDataService) {
        super(fakeDataService);
        javaFakerCode = new com.github.javafaker.Code(fakeDataService.randomService);
    }

    @Programmatic
    public String isbn10() {
        return javaFakerCode.isbn10();
    }

    @Programmatic
    public String isbn13() {
        return javaFakerCode.isbn13();
    }
}
