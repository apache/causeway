package org.apache.isis.extensions.fakedata.dom.types;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.value.Password;
import org.apache.isis.extensions.fakedata.dom.AbstractRandomValueGenerator;
import org.apache.isis.extensions.fakedata.dom.FakeDataService;

public class IsisPasswords extends AbstractRandomValueGenerator {

    public IsisPasswords(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public Password any() {
        return new Password(fake.strings().fixed(12));
    }
}
