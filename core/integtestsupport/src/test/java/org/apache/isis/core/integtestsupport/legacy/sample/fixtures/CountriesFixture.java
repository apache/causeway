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

package org.apache.isis.core.integtestsupport.legacy.sample.fixtures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.core.integtestsupport.legacy.sample.service.CountryRepository;

public class CountriesFixture extends AbstractFixture {

    // {{ Logger
    private final static Logger LOGGER = LoggerFactory.getLogger(CountriesFixture.class);

    public Logger getLOGGER() {
        return LOGGER;
    }

    // }}

    @Override
    public void install() {
        getLOGGER().debug("installing");
        getCountryRepository().newCountry("AUS", "Australia");
        getCountryRepository().newCountry("GBR", "United Kingdom of Great Britain & N. Ireland");
        getCountryRepository().newCountry("USA", "United States of America");
    }

    // {{ Injected: CountryRepository
    private CountryRepository countryRepository;

    /**
     * This field is not persisted, nor displayed to the user.
     */
    protected CountryRepository getCountryRepository() {
        return this.countryRepository;
    }

    /**
     * Injected by the application container.
     */
    public void setCountryRepository(final CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }
    // }}

}
