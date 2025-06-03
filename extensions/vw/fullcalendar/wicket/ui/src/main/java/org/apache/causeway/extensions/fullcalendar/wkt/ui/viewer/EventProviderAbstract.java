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
package org.apache.causeway.extensions.fullcalendar.wkt.ui.viewer;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.extensions.fullcalendar.applib.spi.CalendarableDereferencingService;
import org.apache.causeway.extensions.fullcalendar.applib.value.CalendarEvent;
import org.apache.causeway.extensions.fullcalendar.wkt.integration.fc.Event;
import org.apache.causeway.extensions.fullcalendar.wkt.integration.fc.EventProvider;
import org.apache.causeway.viewer.wicket.model.models.coll.CollectionModel;

public abstract class EventProviderAbstract implements EventProvider {

    private static final long serialVersionUID = 1L;

    private final Map<String, Event> eventById = _Maps.newLinkedHashMap();

    // //////////////////////////////////////

    public EventProviderAbstract(final CollectionModel collectionModel, final String calendarName) {
        var commonContext = collectionModel.getMetaModelContext();

        collectionModel.getDataTableModel()
        .dataElementsObservable().getValue()
        .stream()
        .map(newEvent(commonContext, calendarName))
        .filter(Objects::nonNull)
        .forEach(event->eventById.put(event.getId(), event));
    }

    @Override
    public Collection<Event> getEvents(final ZonedDateTime start, final ZonedDateTime end) {

        var result = eventById.values().stream()
        .filter(event->!start.isAfter(event.getStart()))
        .filter(event->!end.isBefore(event.getEnd()))
        .collect(Collectors.toList());
        return result;

    }

    @Override
    public Event getEventForId(final String id) throws NoSuchElementException {
        return eventById.get(id);
    }

    protected abstract CalendarEvent calendarEventFor(final Object domainObject, final String calendarName);

    // -- HELPER

    private Object dereference(final MetaModelContext commonContext, final Object domainObject) {
        var serviceRegistry = commonContext.getServiceRegistry();
        var services = serviceRegistry.select(CalendarableDereferencingService.class);
        for (final CalendarableDereferencingService dereferencingService : services) {
            final Object dereferencedObject = dereferencingService.dereference(domainObject);
            if (dereferencedObject != null
                    && dereferencedObject != domainObject) {
                return dereferencedObject;
            }
        }
        return domainObject;
    }

    private Function<ManagedObject, Event> newEvent(
            final MetaModelContext commonContext,
            final String calendarName) {

        return domainObject -> {

            final Object domainObjectPojo = domainObject.getPojo();
            final CalendarEvent calendarEvent = calendarEventFor(domainObjectPojo, calendarName);
            if(calendarEvent == null) {
                return null;
            }

            var timeZone = commonContext.getInteractionService()
                    .currentInteractionContext()
                    .map(InteractionContext::getTimeZone)
                    .orElse(ZoneId.systemDefault());

            var start = calendarEvent.asDateTime(timeZone);
            var end = start;

            final Event event = new Event();
            event.setStart(start);
            event.setEnd(end);
            event.setAllDay(true);

            final Object dereferencedObject = dereference(commonContext, domainObjectPojo);

            var dereferencedManagedObject =
                    ManagedObject.adaptSingular(commonContext.getSpecificationLoader(), dereferencedObject);

            var oid = ManagedObjects.bookmark(dereferencedManagedObject).orElse(null);
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

}
