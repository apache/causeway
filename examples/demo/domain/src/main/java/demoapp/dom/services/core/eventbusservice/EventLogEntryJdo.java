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

import javax.inject.Named;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Property;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

import demoapp.dom.services.core.eventbusservice.EventBusServiceDemoPage.UiButtonEvent;

@Profile("demo-jdo")
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo" )
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@Named("demo.EventLogEntry")
@DomainObject
public class EventLogEntryJdo
extends EventLogEntry {

    // -- FACTORY

    public static EventLogEntryJdo of(final UiButtonEvent event) {
        val x = new EventLogEntryJdo();
        x.setEvent("Button clicked " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        return x;
    }

    @javax.jdo.annotations.Column(allowsNull = "false")
    @Property(editing = Editing.DISABLED)
    @Getter(onMethod_ = {@Override}) @Setter(onMethod_ = {@Override})
    private String event;

    @javax.jdo.annotations.Column(allowsNull = "true")
    @Property(editing = Editing.ENABLED)
    @Getter(onMethod_ = {@Override}) @Setter(onMethod_ = {@Override})
    private Acknowledge acknowledge;

    @Action
    @ActionLayout(associateWith = "acknowledge")
    public EventLogEntryJdo acknowledge(final Acknowledge acknowledge) {
        setAcknowledge(acknowledge);
        return this;
    }


}
