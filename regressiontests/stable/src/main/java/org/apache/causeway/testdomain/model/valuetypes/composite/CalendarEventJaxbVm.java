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
package org.apache.causeway.testdomain.model.valuetypes.composite;

import java.util.List;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.extensions.fullcalendar.applib.value.CalendarEvent;
import org.apache.causeway.extensions.fullcalendar.applib.value.CalendarEventSemantics;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

@XmlRootElement(name = "root")
@XmlType(
        propOrder = {"nextEvent", "events"}
)
@XmlAccessorType(XmlAccessType.FIELD)
@Named("testdomain.val.CalendarEventJaxbVm")
@DomainObject(
        nature=Nature.VIEW_MODEL)
public class CalendarEventJaxbVm {

    @Property(editing = Editing.ENABLED)
    @PropertyLayout(promptStyle = PromptStyle.INLINE)
    @Getter @Setter
    private CalendarEvent nextEvent;

    @Collection
    @Getter @Setter
    private List<CalendarEvent> events;

    @Action
    @ActionLayout(associateWith = "nextEvent")
    public CalendarEventJaxbVm updateNextEvent(
            @Parameter(optionality = Optionality.OPTIONAL)
            final CalendarEvent nextEvent) {
        this.nextEvent = nextEvent;
        return this;
    }

    @MemberSupport
    public List<CalendarEvent> choices0UpdateNextEvent() {
        return events;
    }

    // -- FACTORY

    public static CalendarEventJaxbVm setUpViewmodelWith3CalendarEvents(final FactoryService factoryService) {
        val sampleVm = factoryService.viewModel(new CalendarEventJaxbVm());

        val eventSamples = new CalendarEventSemantics().getExamples();
        val a = eventSamples.getElseFail(0);
        val b = eventSamples.getElseFail(1);
        val c = eventSamples.getElseFail(2);

        sampleVm.setNextEvent(a);
        sampleVm.setEvents(List.of(a, b, c));

        return sampleVm;
    }

}
