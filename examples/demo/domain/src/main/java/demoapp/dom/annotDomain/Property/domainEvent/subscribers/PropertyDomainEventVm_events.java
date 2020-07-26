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
package demoapp.dom.annotDomain.Property.domainEvent.subscribers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.context.event.EventListener;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Where;

import lombok.RequiredArgsConstructor;

import demoapp.dom.annotDomain.Property.domainEvent.PropertyDomainEventJdo;
import demoapp.dom.annotDomain.Property.domainEvent.PropertyDomainEventJdo.TextDomainEvent;


//tag::class[]
@Collection(
        // hiding for now, is called lots of times but  perhaps confuses more than explains
        hidden = Where.EVERYWHERE
)
@RequiredArgsConstructor
public class PropertyDomainEventVm_events {

    private final PropertyDomainEventJdo PropertyDomainEventJdo;

    public List<TextDomainEventAsVm> coll() {
        return eventCollectorService.events.stream()
                .map(TextDomainEventAsVm::new)
                .collect(Collectors.toList());
    }

    @Inject
    CollectorService eventCollectorService;

    @DomainService(objectType = "demo.PropertyDomainEventCollectorService")
    static class CollectorService {

        @EventListener(TextDomainEvent.class)
        public void on(TextDomainEvent ev) {
            events.add(ev);
        }

        List<TextDomainEvent> events = new ArrayList<>();
    }
}
//end::class[]
