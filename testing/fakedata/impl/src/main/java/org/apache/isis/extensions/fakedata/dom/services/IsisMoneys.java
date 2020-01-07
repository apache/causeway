package org.apache.isis.extensions.fakedata.dom.services;

//TODO[2249] deprecated
@Deprecated
public class IsisMoneys extends AbstractRandomValueGenerator {

    public IsisMoneys(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

//    @Programmatic
//    public Money any() {
//        return new Money(fake.doubles().any(), fake.collections().anyOf("GBP", "USD", "EUR", "YEN"));
//    }

}
