package org.apache.isis.extensions.fakedata.dom.services;

abstract class AbstractRandomValueGenerator {

    final FakeDataService fake;

    AbstractRandomValueGenerator(final FakeDataService fake) {
        this.fake = fake;
    }

}
