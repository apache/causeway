/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.testing.fakedata.applib.services;

/**
 * @since 2.0 {@index}
 */
public class Addresses extends AbstractRandomValueGenerator {

    com.github.javafaker.Address javaFakerAddress;

    Addresses(final FakeDataService fakeDataService) {
        super(fakeDataService);

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
