package org.isisaddons.module.fakedata.dom;

import org.apache.isis.applib.annotation.Programmatic;

public class Addresses extends AbstractRandomValueGenerator {

    com.github.javafaker.Address javaFakerAddress;

    Addresses(final FakeDataService fakeDataService) {
        super(fakeDataService);
        javaFakerAddress = new com.github.javafaker.Address(
                fakeDataService.name().javaFakerName,
                fakeDataService.fakeValuesService,
                fakeDataService.randomService);
    }

    @Programmatic
    public String streetName() {
        return javaFakerAddress.streetName();
    }

    @Programmatic
    public String streetAddressNumber() {
        return javaFakerAddress.streetAddressNumber();
    }

    @Programmatic
    public String streetAddress() {
        return javaFakerAddress.streetAddress(false);
    }

    @Programmatic
    public String streetAddressWithSecondary() {
        return javaFakerAddress.streetAddress(true);
    }

    @Programmatic
    public String usZipCode() {
        return javaFakerAddress.zipCode();
    }

    @Programmatic
    public String streetSuffix() {
        return javaFakerAddress.streetSuffix();
    }

    @Programmatic
    public String citySuffix() {
        return javaFakerAddress.citySuffix();
    }

    @Programmatic
    public String cityPrefix() {
        return javaFakerAddress.cityPrefix();
    }

    @Programmatic
    public String city() {
        return cityPrefix() + " " + fake.name().firstName() + " " + citySuffix();
    }

    @Programmatic
    public String usStateAbbr() {
        return javaFakerAddress.stateAbbr();
    }

    @Programmatic
    public String country() {
        return javaFakerAddress.country();
    }

    @Programmatic
    public String latitude() {
        return javaFakerAddress.latitude();
    }

    @Programmatic
    public String longitude() {
        return javaFakerAddress.longitude();
    }

}
