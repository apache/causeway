package org.apache.isis.extensions.fakedata.dom.types;

import java.util.UUID;

import org.apache.isis.extensions.fakedata.dom.AbstractRandomValueGenerator;
import org.apache.isis.extensions.fakedata.dom.FakeDataService;

public class Uuids extends AbstractRandomValueGenerator {

    public Uuids(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    public UUID any() {
        return UUID.randomUUID();
    }
}
