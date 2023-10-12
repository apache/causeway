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

import org.apache.wicket.Component;
import org.springframework.stereotype.Service;

import org.apache.causeway.extensions.fullcalendar.applib.CalendarEventable;
import org.apache.causeway.extensions.fullcalendar.wkt.ui.viewer.CalendaredCollectionFactoryAbstract;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;

/**
 * {@link ComponentFactory} for {@link CalendarEventableCollectionAsFullCalendar}.
 */
@Service
public class CalendarEventableCollectionAsFullCalendarFactory
extends CalendaredCollectionFactoryAbstract<CalendarEventable> {

    public CalendarEventableCollectionAsFullCalendarFactory() {
        super(CalendarEventable.class);
    }

    @Override
    protected Component newComponent(final String id, final EntityCollectionModel collectionModel) {
        return new CalendarEventableCollectionAsFullCalendar(id, collectionModel);
    }
}
