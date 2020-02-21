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

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.context.session.RuntimeContextBase;
import org.apache.isis.extensions.fullcalendar.applib.spi.CalendarableDereferencingService;
import org.apache.isis.extensions.fullcalendar.applib.value.CalendarEvent;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;

import lombok.val;

import net.ftlines.wicket.fullcalendar.Event;
import net.ftlines.wicket.fullcalendar.EventNotFoundException;
import net.ftlines.wicket.fullcalendar.EventProvider;

public abstract class EventProviderAbstract implements EventProvider {

    private static final long serialVersionUID = 1L;

    private final Map<String, Event> eventById = Maps.newLinkedHashMap();

    // //////////////////////////////////////

    public EventProviderAbstract(final EntityCollectionModel collectionModel, final String calendarName) {
        createEvents(collectionModel, calendarName);
    }

    private void createEvents(final EntityCollectionModel model, final String calendarName) {
        final Collection<ManagedObject> entityList = model.getObject();
        final Iterable<Event> events = Iterables.filter(
                Iterables.transform(entityList, newEvent(calendarName)), NOT_NULL);
        for (final Event event : events) {
            eventById.put(event.getId(), event);
        }
    }

    private Object dereference(final Object domainObject) {
        return getServiceRegistry().map(serviceRegistry -> {
            val services = serviceRegistry.select(CalendarableDereferencingService.class);
            for (final CalendarableDereferencingService dereferencingService : services) {
                final Object dereferencedObject = dereferencingService.dereference(domainObject);
                if (dereferencedObject != null && dereferencedObject != domainObject) {
                    return dereferencedObject;
                }
            }
            return domainObject;
        }).orElse(null);
    }

    protected abstract CalendarEvent calendarEventFor(final Object domainObject, final String calendarName);

    private Function<ManagedObject, Event> newEvent(final String calendarName) {
        return input -> {

            final Object domainObject = input.getPojo();
            final CalendarEvent calendarEvent = calendarEventFor(domainObject, calendarName);
            if(calendarEvent == null) {
                return null;
            }

            final Event event = new Event();

            final DateTime start = calendarEvent.getDateTime();
            final DateTime end = start;
            event.setStart(start);
            event.setEnd(end);
            event.setAllDay(true);

            final Object dereferencedObject = dereference(domainObject);

            return IsisContext.getCurrentIsisSession()
                    .map(isisSession -> {
                        final SpecificationLoader specificationLoader = isisSession.getSpecificationLoader();
                        final ObjectSpecification spec = specificationLoader.loadSpecification(dereferencedObject.getClass());
                        final ManagedObject dereferencedManagedObject = ManagedObject.of(spec, dereferencedObject);

                        final RootOid rootOid = ManagedObject.identify(dereferencedManagedObject).orElse(null);
                        if(rootOid!=null) {

                            final String oidStr = rootOid.enString();
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
                    })
                    .orElse(null);
        };
    }

    static final Predicate<Event> NOT_NULL = Objects::nonNull;

    // //////////////////////////////////////

    public Collection<Event> getEvents(final DateTime start, final DateTime end) {
        final Interval interval = new Interval(start, end);
        final Predicate<Event> withinInterval = input -> interval.contains(input.getStart());
        final Collection<Event> values = eventById.values();
        return Collections2.filter(values, withinInterval);
    }

    public Event getEventForId(String id) throws EventNotFoundException {
        return eventById.get(id);
    }

    Optional<ServiceRegistry> getServiceRegistry() {
        return IsisContext.getCurrentIsisSession().map(RuntimeContextBase::getServiceRegistry);
    }

}
