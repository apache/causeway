package org.isisaddons.module.fakedata.dom;

import java.util.UUID;
import org.apache.isis.applib.annotation.Programmatic;

public class Uuids extends AbstractRandomValueGenerator{

    public Uuids(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public UUID any() {
        return UUID.randomUUID();
    }
}
