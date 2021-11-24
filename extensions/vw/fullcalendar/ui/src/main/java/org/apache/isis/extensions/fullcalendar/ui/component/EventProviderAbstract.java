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
package org.apache.isis.extensions.fullcalendar.ui.component;

import java.time.ZoneId;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.extensions.fullcalendar.applib.spi.CalendarableDereferencingService;
import org.apache.isis.extensions.fullcalendar.applib.value.CalendarEvent;
import org.apache.isis.valuetypes.jodatime.applib.value.JodaTimeConverters;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;

import lombok.val;

import net.ftlines.wicket.fullcalendar.Event;
import net.ftlines.wicket.fullcalendar.EventNotFoundException;
import net.ftlines.wicket.fullcalendar.EventProvider;

public abstract class EventProviderAbstract implements EventProvider {

    private static final long serialVersionUID = 1L;

    private final Map<String, Event> eventById = _Maps.newLinkedHashMap();

    // //////////////////////////////////////

    public EventProviderAbstract(final EntityCollectionModel collectionModel, final String calendarName) {
        createEvents(collectionModel, calendarName);
    }

    private void createEvents(final EntityCollectionModel model, final String calendarName) {
        val commonContext = model.getCommonContext();

        model.getDataTableModel()
        .getDataElements().getValue()
        .stream()
        .map(newEvent(commonContext, calendarName))
        .filter(NOT_NULL)
        .forEach(event->eventById.put(event.getId(), event));
    }

    private Object dereference(final IsisAppCommonContext commonContext, final Object domainObject) {
        val serviceRegistry = commonContext.getServiceRegistry();
        val services = serviceRegistry.select(CalendarableDereferencingService.class);
        for (final CalendarableDereferencingService dereferencingService : services) {
            final Object dereferencedObject = dereferencingService.dereference(domainObject);
            if (dereferencedObject != null && dereferencedObject != domainObject) {
                return dereferencedObject;
            }
        }
        return domainObject;
    }

    protected abstract CalendarEvent calendarEventFor(final Object domainObject, final String calendarName);

    private Function<ManagedObject, Event> newEvent(
            final IsisAppCommonContext commonContext,
            final String calendarName) {

        return input -> {

            final Object domainObject = input.getPojo();
            final CalendarEvent calendarEvent = calendarEventFor(domainObject, calendarName);
            if(calendarEvent == null) {
                return null;
            }

            val timeZone = commonContext.getInteractionProvider()
                    .currentInteractionContext()
                    .map(InteractionContext::getTimeZone)
                    .orElse(ZoneId.systemDefault());

            val start = JodaTimeConverters.toJoda(calendarEvent.asDateTime(timeZone));
            val end = start;

            final Event event = new Event();
            event.setStart(start);
            event.setEnd(end);
            event.setAllDay(true);

            final Object dereferencedObject = dereference(commonContext, domainObject);

            val dereferencedManagedObject =
                    ManagedObject.lazy(commonContext.getSpecificationLoader(), dereferencedObject);

            val oid = ManagedObjects.bookmark(dereferencedManagedObject).orElse(null);
            if(oid!=null) {

                final String oidStr = oid.stringify();
                event.setId(oidStr + "-" + calendarName);

                event.setClassName("fullCalendar2-event-" + calendarName);
                event.setEditable(false);
                event.setPayload(oidStr);
                event.setTitle(calendarEvent.getTitle());

                //event.setBackgroundColor(backgroundColor)
                //event.setBorderColor(borderColor)
                //event.setColor(color)
                //event.setTextColor(textColor)
                //event.setUrl(url)

                return event;

            } else {
                return null;
            }

        };
    }

    static final Predicate<Event> NOT_NULL = Objects::nonNull;

    // //////////////////////////////////////

    @Override
    public Collection<Event> getEvents(final DateTime start, final DateTime end) {
        final Interval interval = new Interval(start, end);
        final Predicate<Event> withinInterval = input -> interval.contains(input.getStart());
        return eventById.values().stream()
        .filter(withinInterval)
        .collect(Collectors.toList());
    }

    @Override
    public Event getEventForId(final String id) throws EventNotFoundException {
        return eventById.get(id);
    }

}
