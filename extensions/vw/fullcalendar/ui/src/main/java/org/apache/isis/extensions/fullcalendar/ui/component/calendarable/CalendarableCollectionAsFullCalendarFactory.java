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
package org.apache.isis.extensions.fullcalendar.ui.component.calendarable;

import org.apache.wicket.Component;

import org.apache.isis.applib.annotations.Domain;
import org.apache.isis.extensions.fullcalendar.applib.Calendarable;
import org.apache.isis.extensions.fullcalendar.ui.component.CalendaredCollectionFactoryAbstract;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;

/**
 * {@link ComponentFactory} for {@link CalendarableCollectionAsFullCalendar}.
 */
@org.springframework.stereotype.Component
@Domain.Exclude
public class CalendarableCollectionAsFullCalendarFactory
extends CalendaredCollectionFactoryAbstract {

    private static final long serialVersionUID = 1L;

    public CalendarableCollectionAsFullCalendarFactory() {
        super(Calendarable.class);
    }

    @Override
    protected Component newComponent(final String id, final EntityCollectionModel collectionModel) {
        return new CalendarableCollectionAsFullCalendar(id, collectionModel);
    }
}
