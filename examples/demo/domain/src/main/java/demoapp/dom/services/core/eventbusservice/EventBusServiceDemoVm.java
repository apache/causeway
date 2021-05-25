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

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.ActionLayout.Position;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.services.eventbus.EventBusService;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

@DomainObject(nature=Nature.VIEW_MODEL, logicalTypeName = "demo.EventBusServiceDemoVm")
public class EventBusServiceDemoVm implements HasAsciiDocDescription {

    @Inject private EventLogEntryJdoRepository repository;
    @Inject private EventBusService eventBusService;

    public String title() {
        return "Event Demo";
    }

    @Collection
    public List<EventLogEntryJdo> getAllEvents(){
        return repository.listAll();
    }

    public static class UiButtonEvent {}

    @ActionLayout(
            describedAs = "Writes a new EventLog entry to the persistent eventlog.",
            cssClassFa="fa-bolt",
            position = Position.PANEL)
    @Action
    public EventBusServiceDemoVm triggerEvent(){
        eventBusService.post(new UiButtonEvent());
        return this;
    }


}
