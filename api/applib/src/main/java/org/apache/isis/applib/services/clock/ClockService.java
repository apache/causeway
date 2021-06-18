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
package org.apache.isis.applib.services.clock;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.clock.VirtualClock;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactnlayer.InteractionLayerTracker;

import lombok.RequiredArgsConstructor;

/**
 * This service allows an application to be decoupled from the system time.
 * The most common use case is in support of testing scenarios, to &quot;mock the clock&quot;.
 * Use of this service also opens up the use of centralized
 * co-ordinated time management through a centralized time service.
 *
 * @since 1.x revised for 2.0 {@index}
 */
@Service
@Named("isis.applib.ClockService")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ClockService {

    private final Provider<InteractionLayerTracker> interactionLayerTrackerProvider;

    public VirtualClock getClock() {
        return interactionLayerTrackerProvider.get().currentInteractionContext()
                .map(InteractionContext::getClock)
                .orElseGet(VirtualClock::system);
    }

    // -- SHORTCUTS

    public long getEpochMillis() {
        return getClock().getEpochMillis();
    }

//    public java.time.LocalDate now() {
//        return Clock.getTimeAsLocalDate();
//    }
//
//    public java.time.LocalDateTime nowAsLocalDateTime() {
//        return Clock.getTimeAsLocalDateTime();
//    }
//
//    public java.time.OffsetDateTime nowAsOffsetDateTime() {
//        return Clock.getTimeAsOffsetDateTime();
//    }
//
//    public java.sql.Timestamp nowAsJavaSqlTimestamp() {
//        return Clock.getTimeAsJavaSqlTimestamp();
//    }
//
//    public XMLGregorianCalendar nowAsXMLGregorianCalendar() {
//        return JavaSqlXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(nowAsJavaSqlTimestamp());
//    }
//
//    public long nowAsMillis() {
//        return Clock.getEpochMillis();
//    }
//
//    public java.util.Date nowAsJavaUtilDate() {
//        return new java.util.Date(nowAsMillis());
//    }
//
//    public org.joda.time.DateTime nowAsJodaDateTime() {
//        return Clock.getTimeAsJodaDateTime();
//    }
//
//    public org.joda.time.LocalDate nowAsJodaLocalDate() {
//        final DateTimeZone timeZone = DateTimeZone.forTimeZone(TimeZone.getDefault());
//        return new org.joda.time.LocalDate(nowAsMillis(), timeZone);
//    }

}
