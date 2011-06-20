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

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.util.TitleBuffer;


public class Customer extends AbstractDomainObject {

	public static String fieldOrder() {
        return "first name, last name, phone numbers, bookings";
    }

    private String firstName;
    public String getFirstName() {
        resolve(firstName);
        return firstName;
    }
    public void setFirstName(String firstName) {
    	this.firstName = firstName;
    	objectChanged();
    }
    
    
    
    private String lastName;
    public String getLastName() {
        resolve(lastName);
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
        objectChanged();
    }



    private List<Booking> bookings = new ArrayList<Booking>();
    public List<Booking> getBookings() {
    	return bookings;
    }
    public void addToBookings(final Booking booking) {
    	getBookings().add(booking);
    	booking.setCustomer(this);
    	objectChanged();
    }
    public void removeFromBookings(final Booking booking) {
    	getBookings().remove(booking);
    	booking.setCustomer(null);
    	objectChanged();
    }
    
    
    private List<Location> locations = new ArrayList<Location>();
    public List<Location> getLocations() {
    	return locations;
    }
    public void addToLocations(final Location location) {
    	if (!locations.contains(location)) {
    		locations.add(location);
    		location.setCustomer(this);
    		objectChanged();
    	}
    }
    public void removeFromLocations(final Location location) {
    	locations.remove(location);
    	location.setCustomer(null);
    	objectChanged();
    }

    
    
    private List<Telephone> phoneNumbers = new ArrayList<Telephone>();
    public List<Telephone> getPhoneNumbers() {
    	return phoneNumbers;
    }
    public void addToPhoneNumbers(final Telephone telephone) {
        phoneNumbers.add(telephone);
        objectChanged();
    }
    public void removeFromPhoneNumbers(final Telephone telephone) {
    	phoneNumbers.remove(telephone);
    	objectChanged();
    }
    




    private PaymentMethod preferredPaymentMethod;
    public PaymentMethod getPreferredPaymentMethod() {
    	resolve(preferredPaymentMethod);
    	return preferredPaymentMethod;
    }
    public void setPreferredPaymentMethod(final PaymentMethod method) {
        preferredPaymentMethod = method;
        objectChanged();
    }

    
    public Booking newBooking() {
        return bookingFactory.newBookingForCustomer(this);
    }

    
    private transient BookingFactory bookingFactory;
    public void setBookingFactory(BookingFactory bookingFactory) {
        this.bookingFactory = bookingFactory;
    }

    
    public String toString() {
        TitleBuffer title = new TitleBuffer();
        title.append(firstName);
        title.append(" ", lastName);
        return title.toString();
    }
    

}
