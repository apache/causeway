package org.isisaddons.module.fakedata.dom;

import org.apache.isis.applib.annotation.Programmatic;

public class Comms extends AbstractRandomValueGenerator {

    final com.github.javafaker.Internet javaFakerInternet;
    final com.github.javafaker.PhoneNumber javaFakerPhoneNumber;

    Comms(final FakeDataService fakeDataService) {
        super(fakeDataService);
        final com.github.javafaker.Name name = fakeDataService.name().javaFakerName;
        javaFakerInternet = new com.github.javafaker.Internet(name, fakeDataService.fakeValuesService);
        javaFakerPhoneNumber = new com.github.javafaker.PhoneNumber(fakeDataService.fakeValuesService);
    }

    @Programmatic
    public String emailAddress() {
        return javaFakerInternet.emailAddress();
    }

    @Programmatic
    public String url() {
        return javaFakerInternet.url();
    }

    @Programmatic
    public String phoneNumber() {
        return javaFakerPhoneNumber.phoneNumber();
    }

}
