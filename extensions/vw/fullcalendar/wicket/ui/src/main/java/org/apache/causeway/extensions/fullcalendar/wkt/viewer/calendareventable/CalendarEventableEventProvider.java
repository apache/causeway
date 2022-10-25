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
package org.apache.causeway.extensions.fullcalendar.wkt.viewer.calendareventable;

import java.util.Objects;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.extensions.fullcalendar.applib.CalendarEventable;
import org.apache.causeway.extensions.fullcalendar.applib.value.CalendarEvent;
import org.apache.causeway.extensions.fullcalendar.wkt.viewer.EventProviderAbstract;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel;

public class CalendarEventableEventProvider extends EventProviderAbstract {

    private static final long serialVersionUID = 1L;

    public CalendarEventableEventProvider(
            final EntityCollectionModel collectionModel,
            final String calendarName) {
        super(collectionModel, calendarName);
    }

    @Override
    protected CalendarEvent calendarEventFor(
            final Object domainObjectPojo,
            final String calendarName) {
        return _Casts.castTo(CalendarEventable.class, domainObjectPojo)
        .filter(calendarEventable->Objects.equals(calendarName, calendarEventable.getCalendarName()))
        .map(CalendarEventable::toCalendarEvent)
        .orElse(null);
    }

    static final String getCalendarName(final ManagedObject domainObject) {
        return _Casts.castTo(CalendarEventable.class, domainObject.getPojo())
        .map(CalendarEventable::getCalendarName)
        .orElse(null);
    }

}
