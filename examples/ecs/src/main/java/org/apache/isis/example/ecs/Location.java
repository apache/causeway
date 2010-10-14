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


package org.apache.isis.example.ecs;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.util.TitleBuffer;


public class Location extends AbstractDomainObject {
    public static String descriptionNewBooking(final Location location) {
        return "Giving one location to another location creates a new booking going from the given location to the receiving location.";
    }

    public static String[] namesNewBooking() {
        return new String[] { "Pick Up" };
    }


    private Customer customer;
    public Customer getCustomer() {
    	resolve(customer);
    	return customer;
    }
    public void setCustomer(final Customer newCustomer) {
    	customer = newCustomer;
    	objectChanged();
    }
    public void modifyCustomer(final Customer newCustomer) {
        newCustomer.addToLocations(this);
    }
    public void clearCustomer() {
    	getCustomer().removeFromLocations(this);
    }


    private City city;
    public City getCity() {
        resolve(city);
        return city;
    }
    public void setCity(final City newCity) {
    	city = newCity;
    	objectChanged();
    }


    private String knownAs;
    public String getKnownAs() {
        resolve(knownAs);
        return knownAs;
    }
    public void setKnownAs(String knownAs) {
    	this.knownAs = knownAs;
    	objectChanged();
    }

    
    private String streetAddress;
    public String getStreetAddress() {
        resolve(knownAs);
        return streetAddress;
    }
    public void setStreetAddress(String streetAddress) {
    	this.streetAddress = streetAddress;
    	objectChanged();
    }

    
    
    public Booking newBooking(final Location pickup) {
        return bookingFactory.newBookingBetweenLocations(pickup, this);
    }



    public String toString() {
        TitleBuffer title = new TitleBuffer();
        if (TitleBuffer.isEmpty(getKnownAs())) {
            title.append(getStreetAddress());
        } else {
            title.append(getKnownAs());
        }
        return title.toString();
    }

    public String validateNewBooking(final Location location) {
        if (equals(location)) {
            return "Two different locations are required";
        }
        boolean sameCity = getCity() != null && location != null && getCity().equals(location.getCity());
        if (!sameCity) {
            return "Locations must be in the same city";
        }
        return null;
    }
    
    
    private BookingFactory bookingFactory;
    public void setBookingFactory(BookingFactory bookingFactory) {
    	this.bookingFactory = bookingFactory;
    }

}
