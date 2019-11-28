package org.apache.isis.extensions.fakedata.dom;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.value.Password;

public class IsisPasswords extends AbstractRandomValueGenerator{

    public IsisPasswords(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public Password any() {
        return new Password(fake.strings().fixed(12));
    }
}
