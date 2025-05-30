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
package org.apache.causeway.extensions.fullcalendar.wkt.ui.viewer.calendareventable;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.extensions.fullcalendar.wkt.integration.fc.EventProvider;
import org.apache.causeway.extensions.fullcalendar.wkt.ui.viewer.CalendaredCollectionAbstract;
import org.apache.causeway.viewer.wicket.model.models.coll.CollectionModel;

public class CalendarEventableCollectionAsFullCalendar
extends CalendaredCollectionAbstract {

    private static final long serialVersionUID = 1L;

    public CalendarEventableCollectionAsFullCalendar(final String id, final CollectionModel model) {
        super(id, model);
    }

    @Override
    protected EventProvider newEventProvider(
            final CollectionModel model,
            final String calendarName) {
        return new CalendarEventableEventProvider(model, calendarName);
    }

    @Override
    protected Set<String> getCalendarNames(final Iterable<ManagedObject> entityList) {
        return _NullSafe.stream(entityList)
                .map(CalendarEventableEventProvider::getCalendarName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
