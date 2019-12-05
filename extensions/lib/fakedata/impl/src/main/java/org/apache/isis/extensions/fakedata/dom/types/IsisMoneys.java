package org.apache.isis.extensions.fakedata.dom.types;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.value.Money;
import org.apache.isis.extensions.fakedata.dom.AbstractRandomValueGenerator;
import org.apache.isis.extensions.fakedata.dom.FakeDataService;

public class IsisMoneys extends AbstractRandomValueGenerator {

    public IsisMoneys(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public Money any() {
        return new Money(fake.doubles().any(), fake.collections().anyOf("GBP", "USD", "EUR", "YEN"));
    }

}
