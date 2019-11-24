package org.isisaddons.module.fakedata.dom;

public class Addresses extends AbstractRandomValueGenerator {

    com.github.javafaker.Address javaFakerAddress;

    Addresses(final FakeDataService fakeDataService) {
        super(fakeDataService);
        ;
        javaFakerAddress = fakeDataService.javaFaker().address();
    }

    public String streetName() {
        return javaFakerAddress.streetName();
    }

    public String streetAddressNumber() {
        return javaFakerAddress.streetAddressNumber();
    }

    public String streetAddress() {
        return javaFakerAddress.streetAddress(false);
    }

    public String streetAddressWithSecondary() {
        return javaFakerAddress.streetAddress(true);
    }

    public String usZipCode() {
        return javaFakerAddress.zipCode();
    }

    public String streetSuffix() {
        return javaFakerAddress.streetSuffix();
    }

    public String citySuffix() {
        return javaFakerAddress.citySuffix();
    }

    public String cityPrefix() {
        return javaFakerAddress.cityPrefix();
    }

    public String city() {
        return cityPrefix() + " " + fake.name().firstName() + " " + citySuffix();
    }

    public String usStateAbbr() {
        return javaFakerAddress.stateAbbr();
    }

    public String country() {
        return javaFakerAddress.country();
    }

    public String latitude() {
        return javaFakerAddress.latitude();
    }

    public String longitude() {
        return javaFakerAddress.longitude();
    }

}
