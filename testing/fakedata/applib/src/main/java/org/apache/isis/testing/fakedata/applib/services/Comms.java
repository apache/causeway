package org.apache.isis.testing.fakedata.applib.services;

public class Comms extends AbstractRandomValueGenerator {

    final com.github.javafaker.Internet javaFakerInternet;
    final com.github.javafaker.PhoneNumber javaFakerPhoneNumber;

    Comms(final FakeDataService fakeDataService) {
        super(fakeDataService);
        //final com.github.javafaker.Name name = fakeDataService.name().javaFakerName;
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
