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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.Entity;

import org.springframework.context.annotation.Profile;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.Editing;
import org.apache.isis.applib.annotations.ObjectSupport;
import org.apache.isis.applib.annotations.Property;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

import demoapp.dom.services.core.eventbusservice.EventBusServiceDemoVm.UiButtonEvent;

@Profile("demo-jpa")
@Entity
@DomainObject(logicalTypeName = "demo.EventLogEntryJpa")
public class EventLogEntryJpa {

    public static EventLogEntryJpa of(final UiButtonEvent even) {
        val x = new EventLogEntryJpa();
        x.setEvent("Button clicked " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        return x;
    }

    @ObjectSupport public String title() {
        return getEvent();
    }

    @javax.persistence.Column(nullable = true)
    @Property(editing = Editing.DISABLED)
    @Getter @Setter
    private String event;

    // demonstrating 2 methods of changing a property ...
    // - inline edit
    // - via action

    public static enum Acknowledge {
        IGNORE,
        CRITICAL
    }

    @javax.persistence.Column(nullable = true)
    @Property(editing = Editing.ENABLED)
    @Getter @Setter
    private Acknowledge acknowledge;

    @Action
    @ActionLayout(associateWith = "acknowledge")
    public EventLogEntryJpa acknowledge(final Acknowledge acknowledge) {
        setAcknowledge(acknowledge);
        return this;
    }


}
