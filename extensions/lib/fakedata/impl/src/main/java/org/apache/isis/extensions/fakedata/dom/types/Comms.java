package org.apache.isis.extensions.fakedata.dom.types;

import org.apache.isis.extensions.fakedata.dom.AbstractRandomValueGenerator;
import org.apache.isis.extensions.fakedata.dom.FakeDataService;

public class Comms extends AbstractRandomValueGenerator {

    final com.github.javafaker.Internet javaFakerInternet;
    final com.github.javafaker.PhoneNumber javaFakerPhoneNumber;

    Comms(final FakeDataService fakeDataService) {
        super(fakeDataService);
        final com.github.javafaker.Name name = fakeDataService.name().javaFakerName;
        javaFakerInternet =  fakeDataService.javaFaker().internet();
        javaFakerPhoneNumber = fakeDataService.javaFaker().phoneNumber();
    }

    public String emailAddress() {
        return javaFakerInternet.emailAddress();
    }

    public String url() {
        return javaFakerInternet.url();
    }

    public String phoneNumber() {
        return javaFakerPhoneNumber.phoneNumber();
    }

}
