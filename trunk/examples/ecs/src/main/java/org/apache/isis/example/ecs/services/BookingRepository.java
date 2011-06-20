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


package org.apache.isis.example.ecs.services;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.example.ecs.Booking;

import java.util.List;


public class BookingRepository extends AbstractFactoryAndRepository {

    public static String[] namesFindByReference() {
        return new String[] { "Reference" };
    }

    public Booking findByReference(final String reference) {
        final Filter<Booking> filter = new Filter<Booking>() {
            public boolean accept(final Booking booking) {
                return booking.getReference().equalsIgnoreCase(reference);
            }
        };
        
        return (Booking) uniqueMatch(Booking.class, filter);
    }

    public List<Booking> explorationAllBookings() {
        return allInstances(Booking.class);
    }

    public String iconName() {
        return "Booking";
    }

}

