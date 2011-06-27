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
import org.apache.isis.applib.util.ReasonBuffer;
import org.apache.isis.applib.util.TitleBuffer;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.Time;


public class Booking extends AbstractDomainObject {
    public static String actionOrder() {
        return "Check Availability, Confirm, Copy Booking, Return Booking";
    }

    public static String descriptionPickUp() {
        return "The location to pick up the customer from.";
    }

    public static String descriptionReturnBooking() {
        return "Creates a new Booking based on the current booking.  The new booking has the pick up amd drop off locations reversed.";
    }

    public static String fieldOrder() {
        return "reference, status, customer, date, time, pick up, drop off, payment method";
    }

    
    
    // lifecycle methods

    public void created() {
        setStatus("New Booking");
        setReference(bookingFactory.createReference());
    }
    


    // {{ Customer
    private Customer customer;
    public Customer getCustomer() {
        resolve(customer);
        return customer;
    }
    public void setCustomer(final Customer newCustomer) {
        customer = newCustomer;
        objectChanged();
    }
    public void clearCustomer(final Customer customer) {
        customer.removeFromBookings(this);
    }
    public void modifyCustomer(final Customer customer) {
        customer.addToBookings(this);
        objectChanged();
    }
    // }}
    
    

    
    // {{ Drop off
    private Location dropOff;
    public Location getDropOff() {
        resolve(dropOff);
        return dropOff;
    }
    public void setDropOff(final Location newDropOff) {
        dropOff = newDropOff;
        objectChanged();
    }
    public void modifyDropOff(final Location newDropOff) {
        setDropOff(newDropOff);
        setCity(newDropOff.getCity());
        objectChanged();
    }
    // }}
    
    
    // {{ Pick up
    private Location pickUp;
    public Location getPickUp() {
        resolve(pickUp);
        return pickUp;
    }
    public void setPickUp(final Location newPickUp) {
        pickUp = newPickUp;
        objectChanged();
    }
    public void modifyPickUp(final Location newPickUp) {
        setPickUp(newPickUp);
        setCity(newPickUp.getCity());
        objectChanged();
    }
    public String validatePickUp(final Location newPickup) {
        if (newPickup != null) {
            if (getCity() == null || getDropOff() == null) {
                return null;
            } else {
                if (!getCity().equals(newPickup.getCity())) {
                    return "Location must be in " + getCity();
                }
                if (newPickup.equals(getDropOff())) {
                    return "Pick up must differ from the drop off location";
                }
            }
        }
        return null;
    }
    // }}


    // {{ City
    private City city;
    public City getCity() {
        resolve(city);
        return city;
    }

    public void setCity(final City newCity) {
        city = newCity;
        objectChanged();
    }
    // }}

    
    
    // {{ Contact Telephone
    private Telephone contactTelephone;
    public Telephone getContactTelephone() {
        resolve(contactTelephone);
        return contactTelephone;
    }
    public void setContactTelephone(final Telephone newContactTelephone) {
        contactTelephone = newContactTelephone;
        objectChanged();
    }
    // }}

    
    // {{ Date
    private Date date;
    public Date getDate() {
        resolve(date);
        return date;
    }
    public void setDate(final Date date) {
        this.date = date;
        objectChanged();
    }
    // }}
    
    
    // {{ Payment Method
    private PaymentMethod paymentMethod;
    public PaymentMethod getPaymentMethod() {
        resolve(paymentMethod);
        return paymentMethod;
    }
    public void setPaymentMethod(final PaymentMethod newPaymentMethod) {
        paymentMethod = newPaymentMethod;
        objectChanged();
    }
    // }}

    
    
    // {{ Reference
    private String reference;
    public String getReference() {
        resolve(reference);
        return reference;
    }
    public void setReference(String reference) {
        this.reference = reference;
        objectChanged();
    }
    public String disableReference(String reference) {
        return "Not editable";
    }
    public String disableStatus(String reference) {
        return "Not editable";
    }
    // }}

    
    
    // {{ Status
    private String status;
    public String getStatus() {
        resolve(status);
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
        objectChanged();
    }
    // }}

    
    
    // {{ Time
    private Time time;
    public Time getTime() {
        resolve();
        return time;
    }
    public void setTime(Time time) {
        this.time = time;
        objectChanged();
    }
    // }}

    
    

    // {{ action: checkAvailability
    public void checkAvailability() {
        setStatus("Available");
    }
    public String disableCheckAvailability() {
        ReasonBuffer reason = new ReasonBuffer();
        reason.appendOnCondition(getStatus().equals("Available"), "Already available");
        reason.appendOnCondition(getDate() == null, "Date required first");
        return reason.getReason();
    }
    // }}

    
    
    // {{ action: confirm
    public void confirm() {
        setStatus("Confirmed");
        /*
         * The locations used are added to the customer's Locations field. Note that the accessor methods are
         * used to ensure that the objects are loaded first.
         */
        getCustomer().addToLocations(getPickUp());
        getCustomer().addToLocations(getDropOff());
        if (getCustomer().getPreferredPaymentMethod() == null) {
            getCustomer().setPreferredPaymentMethod(getPaymentMethod());
        }
    }
    public String disableConfirm() {
        if (getStatus().equals("Available")) {
            return null;
        } else {
            return "Status must be Available";
        }
    }
    // }}

    
    
    // {{ action: copyBooking
    public Booking copyBooking() {
        return bookingFactory.copyBooking(this);
    }
    public String disableCopyBooking() {
        int sets = 0;

        sets += ((getCustomer() != null) ? 1 : 0);
        sets += ((getPickUp() != null) ? 1 : 0);
        sets += ((getDropOff() != null) ? 1 : 0);
        sets += ((getPaymentMethod() != null) ? 1 : 0);
        sets += ((getContactTelephone() != null) ? 1 : 0);

        return sets >= 3 ? null : "Not enough detail present to copy booking";
    }
    // }}

    
    // {{ action: returnBooking
    public Booking returnBooking() {
        return bookingFactory.newReturnBooking(this);
    }
    public String validateReturnBooking() {
        if (!getStatus().equalsIgnoreCase("Confirmed")) {
            return "Can only create a return based on a confirmed booking";
        }
        return null;
    }
    // }}

    public String toString() {
        TitleBuffer title = new TitleBuffer();
        title.append(reference);
        title.append(" ", date == null ? null : date.title());
        return title.toString();
    }
    
    // related services
    private BookingFactory bookingFactory;
    public void setBookingFactory(BookingFactory bookingFactory) {
        this.bookingFactory = bookingFactory;
    }

}
