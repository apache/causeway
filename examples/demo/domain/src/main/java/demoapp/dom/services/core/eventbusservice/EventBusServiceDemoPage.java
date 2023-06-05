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
package demoapp.dom.services.core.eventbusservice;

import java.util.EventObject;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.services.eventbus.EventBusService;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.NoArgsConstructor;

//tag::class[]
@Named("demo.EventBusServiceDemoPage")
@DomainObject(nature=Nature.VIEW_MODEL)
@DomainObjectLayout(cssClassFa="fa-bolt")
@NoArgsConstructor
public class EventBusServiceDemoPage implements HasAsciiDocDescription {
    // ...
//end::class[]

    @ObjectSupport public String title() {
        return "Event Demo";
    }

//tag::eventClass[]
    public static class UiButtonEvent extends EventObject { // <.>
        public UiButtonEvent(final Object source) {
            super(source);
        }
    }
//end::eventClass[]

//tag::triggerEvent[]
    @Action
    public EventBusServiceDemoPage triggerEvent(){
        eventBusService.post(new UiButtonEvent(this));      // <.>
        return this;
    }

    @Inject private EventBusService eventBusService;        // <.>
//end::triggerEvent[]

    @Collection public List<? extends EventLogEntry> getAllEvents(){
        return eventLogEntryRepository.listAll();
    }

    @Inject private EventLogEntryRepository<? extends EventLogEntry> eventLogEntryRepository;

//tag::class[]
}
//end::class[]
