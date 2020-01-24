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
package org.apache.isis.extensions.fullcalendar.ui.component.calendareventable;

import com.google.common.base.Function;
import com.google.common.base.Objects;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.extensions.fullcalendar.applib.CalendarEventable;
import org.apache.isis.extensions.fullcalendar.applib.value.CalendarEvent;
import org.apache.isis.extensions.fullcalendar.ui.component.EventProviderAbstract;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;

public class CalendarEventableEventProvider extends EventProviderAbstract {

    private static final long serialVersionUID = 1L;

    public CalendarEventableEventProvider(
            final EntityCollectionModel collectionModel,
            final String calendarName) {
        super(collectionModel, calendarName);
    }

    @Override
    protected CalendarEvent calendarEventFor(
            final Object domainObject,
            final String calendarName) {
        if(!(domainObject instanceof CalendarEventable)) {
            return null;
        }
        final CalendarEventable calendarEventable = (CalendarEventable)domainObject;
        return Objects.equal(calendarName, calendarEventable.getCalendarName())
                ? calendarEventable.toCalendarEvent()
                : null;
    }

    static final Function<ManagedObject, String> GET_CALENDAR_NAME = new Function<ManagedObject, String>() {
        @Override
        public String apply(final ManagedObject input) {
            final Object domainObject = input.getPojo();
            if(!(domainObject instanceof CalendarEventable)) {
                return null;
            }
            final CalendarEventable calendarEventable = (CalendarEventable) domainObject;
            return calendarEventable.getCalendarName();
        }
    };


}
