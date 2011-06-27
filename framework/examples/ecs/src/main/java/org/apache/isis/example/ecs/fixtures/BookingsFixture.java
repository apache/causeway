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


package org.apache.isis.example.ecs.fixtures;

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.Time;
import org.apache.isis.example.ecs.Booking;
import org.apache.isis.example.ecs.BookingFactory;
import org.apache.isis.example.ecs.CreditCard;
import org.apache.isis.example.ecs.Customer;
import org.apache.isis.example.ecs.Location;
import org.apache.isis.example.ecs.Telephone;


public class BookingsFixture extends AbstractFixture {
    private BookingFactory bookingFactory;

    public void install() {
        setDate(2003, 10, 23);
        setTime(20, 15);

        Customer newCustomer = uniqueMatch(Customer.class, "Matthews");

        Booking booking = (Booking) newTransientInstance(Booking.class);
        booking.modifyCustomer(newCustomer);
        booking.setPickUp((Location) newCustomer.getLocations().get(1));
        booking.setDropOff((Location) newCustomer.getLocations().get(2));
        booking.setDate(new Date());
        booking.setTime(new Time());

        booking.setContactTelephone((Telephone) newCustomer.getPhoneNumbers().get(0));

        CreditCard cc = (CreditCard) newTransientInstance(CreditCard.class);
        cc.setNumber("773829889938221");
        cc.setExpires("10/04");
        cc.setNameOnCard("MR R MATTHEWS");
        booking.setPaymentMethod(cc);

        persist(booking);
        
        
        booking = bookingFactory.createBooking(newCustomer, (Location) newCustomer.getLocations().get(1), (Location) newCustomer
                .getLocations().get(0), new Date().add(0, 0, 2), cc);
        booking.checkAvailability();
        booking.confirm();


        resetClock();
    }

    public void setFactory(BookingFactory bookingFactory) {
        this.bookingFactory = bookingFactory;
    }

}
