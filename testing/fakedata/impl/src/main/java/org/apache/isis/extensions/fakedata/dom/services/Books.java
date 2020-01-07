package org.apache.isis.extensions.fakedata.dom.services;

public class Books extends AbstractRandomValueGenerator {

    com.github.javafaker.Code javaFakerCode;

    Books(final FakeDataService fakeDataService) {
        super(fakeDataService);
        javaFakerCode = fakeDataService.javaFaker().code();
    }

    public String isbn10() {
        return javaFakerCode.isbn10();
    }

    public String isbn13() {
        return javaFakerCode.isbn13();
    }
}
