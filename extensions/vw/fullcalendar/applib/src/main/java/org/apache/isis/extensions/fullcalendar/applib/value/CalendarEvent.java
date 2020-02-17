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
package org.apache.isis.extensions.fullcalendar.applib.value;

import java.io.Serializable;

import org.joda.time.DateTime;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ObjectContracts.ObjectContract;

/**
 * Value type representing an event on a calendar.
 */
@Value(semanticsProviderClass=CalendarEventSemanticsProvider.class)
public class CalendarEvent implements Serializable {

    private static final long serialVersionUID = 1L;
    
	static final CalendarEvent DEFAULT_VALUE = null; // no default
	
    private final DateTime dateTime;
    private final String calendarName;
    private final String title;
    private final String notes;
	
	public CalendarEvent(final DateTime dateTime, final String calendarName, final String title) {
        this(dateTime, calendarName, title, null);
	}

    public CalendarEvent(final DateTime dateTime, final String calendarName, final String title, final String notes) {
        this.dateTime = dateTime;
        this.calendarName = calendarName;
        this.title = title;
        this.notes = notes;
    }

    public DateTime getDateTime() {
        return dateTime;
    }
    
    public String getCalendarName() {
        return calendarName;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getNotes() {
        return notes;
    }
   
	public CalendarEvent withDateTime(final DateTime date) {
		return new CalendarEvent(date, this.calendarName, this.title, this.notes);
	}
	
	public CalendarEvent withCalendarName(final String calendarName) {
	    return new CalendarEvent(this.dateTime, calendarName, this.title, this.notes);
	}
	
	public CalendarEvent withTitle(final String title) {
	    return new CalendarEvent(this.dateTime, this.calendarName, title, this.notes);
	}
	
	public CalendarEvent withNotes(final String notes) {
	    return new CalendarEvent(this.dateTime, this.calendarName, this.title, notes);
	}

	private static final ObjectContract<CalendarEvent> objectContract = 
	    ObjectContracts.contract(CalendarEvent.class)
	    .thenUse("dateTime", CalendarEvent::getDateTime)
	    .thenUse("calendarName", CalendarEvent::getCalendarName);
	
	
	@Override
    public int hashCode() {
	    return objectContract.hashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return objectContract.equals(this, obj);
    }

    @Override
    public String toString() {
        return objectContract.toString(this);
    }
    
    public static int typicalLength() {
		return 30;
	}
}
