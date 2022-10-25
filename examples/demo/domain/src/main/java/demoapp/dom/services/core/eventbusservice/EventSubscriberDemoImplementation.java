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

import static demoapp.dom._infra.utils.LogUtils.emphasize;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.wrapper.control.AsyncControl;

import demoapp.dom.services.core.eventbusservice.EventBusServiceDemoVm.UiButtonEvent;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named("demo.eventSubscriber")
@Qualifier("demo")
@Log4j2
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class EventSubscriberDemoImplementation {

    final WrapperFactory wrapper;
    final FactoryService factoryService;

    @EventListener(UiButtonEvent.class) // <-- listen on the event, triggered by button in the UI
    public void on(final UiButtonEvent event) {

        log.info(emphasize("UiButtonEvent")); // <-- log to the console

        val eventLogWriter = factoryService.get(EventLogWriter.class); // <-- get a new writer

        wrapper.asyncWrap(eventLogWriter, AsyncControl.returningVoid()).storeEvent(event);

    }

    @Named("demo.eventLogWriter")
    @DomainObject(
            nature = Nature.BEAN) // <-- have this Object's lifecycle managed by Spring
    public static class EventLogWriter {

        @Inject private EventLogEntryRepository<? extends EventLogEntry> eventLogEntryRepository;

        @Action // called asynchronously by above invocation
        public void storeEvent(final UiButtonEvent event) {

            eventLogEntryRepository.storeEvent(event);
        }

    }



}
