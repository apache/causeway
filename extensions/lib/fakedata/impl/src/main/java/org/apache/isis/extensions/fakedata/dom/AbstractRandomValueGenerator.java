package org.apache.isis.extensions.fakedata.dom;

abstract class AbstractRandomValueGenerator {

    final FakeDataService fake;

    AbstractRandomValueGenerator(final FakeDataService fake) {
        this.fake = fake;
    }

}
