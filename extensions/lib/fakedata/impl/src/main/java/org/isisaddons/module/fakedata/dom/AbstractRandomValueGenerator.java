package org.isisaddons.module.fakedata.dom;

abstract class AbstractRandomValueGenerator {

    final FakeDataService fake;

    AbstractRandomValueGenerator(final FakeDataService fake) {
        this.fake = fake;
    }

}
