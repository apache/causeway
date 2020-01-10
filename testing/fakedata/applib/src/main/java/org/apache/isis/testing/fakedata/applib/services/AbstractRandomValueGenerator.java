package org.apache.isis.testing.fakedata.applib.services;

abstract class AbstractRandomValueGenerator {

    final FakeDataService fake;

    AbstractRandomValueGenerator(final FakeDataService fake) {
        this.fake = fake;
    }

}
