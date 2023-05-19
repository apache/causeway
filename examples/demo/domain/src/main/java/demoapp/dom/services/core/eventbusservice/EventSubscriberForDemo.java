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

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.wrapper.control.AsyncControl;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import demoapp.dom.services.core.eventbusservice.EventBusServiceDemoPage.UiButtonEvent;

import static demoapp.dom._infra.utils.LogUtils.emphasize;

//tag::class[]
@Service
@Named("demo.eventSubscriber")
@Qualifier("demo")
@Log4j2
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class EventSubscriberForDemo {

    final EventLogEntryRepository<? extends EventLogEntry> eventLogEntryRepository;

    @EventListener(UiButtonEvent.class)             // <.>
    public void on(final UiButtonEvent event) {
        eventLogEntryRepository.storeEvent(event);  // <.>
    }
}
//end::class[]
