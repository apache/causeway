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

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.ApplicationException;
import org.apache.isis.applib.value.Date;

public class BookingFactory extends AbstractFactoryAndRepository {
    public static String[] namesCreateBooking() {
        return new String[] { "Customer", "Pick up", "Drop off", "Date", "Payment method" };
    }

    public static boolean[] optionalCreateBooking() {
        return new boolean[] {false, false, false, true, true };
    }

    public Booking copyBooking(Booking booking) {
        Booking copiedBooking = (Booking) newTransientInstance(Booking.class);
        if (booking.getPickUp() != null) {
            copiedBooking.setPickUp(booking.getPickUp());
        }
        if (booking.getDropOff() != null) {
            copiedBooking.setDropOff(booking.getDropOff());
        }
        if (booking.getPaymentMethod() != null) {
            copiedBooking.setPaymentMethod(booking.getPaymentMethod());
        }
        if (booking.getContactTelephone() != null) {
            copiedBooking.setContactTelephone(booking.getContactTelephone());
        }
        persist(copiedBooking);
        if (booking.getCustomer() != null) {
            copiedBooking.modifyCustomer(booking.getCustomer());
        }
        return copiedBooking;
    }

    public Booking createBooking(Customer forCustomer, Location from, Location to, Date date, PaymentMethod paymentMethod) {
        Booking booking = (Booking) newInstance(Booking.class, forCustomer);
        booking.modifyCustomer(forCustomer);
        booking.setPaymentMethod(paymentMethod);
        booking.setPickUp(from);
        booking.setDropOff(to);
        booking.setDate(date);
        return booking;
    }

    public Booking newBookingBetweenLocations(Location pickup, Location dropoff) {
        if (!isPersistent(pickup) || !isPersistent(dropoff)) {
            throw new ApplicationException("Both locations must be persistent");
        }
        Booking booking = (Booking) newTransientInstance(Booking.class);
        booking.setDropOff(dropoff);
        booking.setPickUp(pickup);
        booking.setCity(pickup.getCity());
        persist(booking);

        Customer customer = pickup.getCustomer();
        if (customer != null) {
            booking.modifyCustomer(customer);
            booking.setPaymentMethod(customer.getPreferredPaymentMethod());
        }
        return booking;
    }

    public Booking newReturnBooking(Booking booking) {
        Booking returnBooking = (Booking) newTransientInstance(Booking.class);
        returnBooking.modifyCustomer(booking.getCustomer());
        returnBooking.setPickUp(booking.getDropOff());
        returnBooking.setDropOff(booking.getPickUp());
        returnBooking.setPaymentMethod(booking.getPaymentMethod());
        returnBooking.setContactTelephone(booking.getContactTelephone());
        persist(returnBooking);
        return returnBooking;
    }

    public Booking newBookingForCustomer(final Customer customer) {
        Booking booking = (Booking) newInstance(Booking.class, customer);
        booking.modifyCustomer(customer);
        booking.setPaymentMethod(customer.getPreferredPaymentMethod());
        return booking;
    }

    public Booking newBooking() {
        return (Booking) newTransientInstance(Booking.class);
    }
    
    public boolean hideCreateReference() {
        return true;
    }
    
    public String createReference() {
  /*      SimpleRepository repos = new SimpleRepository(ReferenceGenerator.class);
        repos.init();
        Object[] instances = repos.allInstances();
        ReferenceGenerator generator;
        if(instances.length == 0) {
            SimpleFactory fact = new SimpleFactory(ReferenceGenerator.class);
            fact.init();
            generator = (ReferenceGenerator) fact.newPersistentInstance();
        } else {
            generator = (ReferenceGenerator) instances[0];
        }
        
        return  "#" + generator.next();
        */
        return "#" + id++;
    }
    
    private int id = 1;
    
    public String iconName() {
        return "Booking";
    }
}

