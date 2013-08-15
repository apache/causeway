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

package org.apache.isis.core.integtestsupport.legacy.sample.domain;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Bounded;
import org.apache.isis.applib.annotation.MaxLength;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.TypicalLength;
import org.apache.isis.applib.util.TitleBuffer;

@Bounded
public class Country extends AbstractDomainObject {

    // {{ Logger
    @SuppressWarnings("unused")
    private final static Logger LOGGER = LoggerFactory.getLogger(Country.class);

    // }}

    // {{ Identification Methods
    /**
     * Defines the title that will be displayed on the user interface in order
     * to identity this object.
     */
    public String title() {
        final TitleBuffer t = new TitleBuffer();
        t.append(getName());
        return t.toString();
    }

    // }}

    // {{ Code
    private String code;

    @TypicalLength(3)
    @MaxLength(3)
    public String getCode() {
        return this.code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    // }}

    // {{ Name
    private String name;

    @TypicalLength(50)
    @MaxLength(255)
    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    // }}

    // {{ FavouriteHolidayDestination
    private Country favouriteHolidayDestination;

    @Optional
    public Country getFavouriteHolidayDestination() {
        return favouriteHolidayDestination;
    }

    public void setFavouriteHolidayDestination(final Country favouriteHolidayDestination) {
        this.favouriteHolidayDestination = favouriteHolidayDestination;
    }

    // }}

    // {{ Colonies
    private List<Country> colonies = new ArrayList<Country>();

    public List<Country> getColonies() {
        return this.colonies;
    }

    @SuppressWarnings("unused")
    private void setColonies(final List<Country> colonies) {
        this.colonies = colonies;
    }

    public void addToColonies(final Country country) {
        getColonies().add(country);
    }

    public void removeFromColonies(final Country country) {
        getColonies().remove(country);
    }

    public String validateAddToColonies;

    public String validateAddToColonies(final Country country) {
        return validateAddToColonies;
    }

    public String validateRemoveFromColonies;

    public String validateRemoveFromColonies(final Country country) {
        return validateRemoveFromColonies;
    }

    public String disableColonies;

    public String disableColonies() {
        return this.disableColonies;
    }

    public boolean hideColonies;

    public boolean hideColonies() {
        return this.hideColonies;
    }

    // }}

    // {{
    /**
     * An action to invoke
     */
    public void foobar() {
    }
    // }}

}
