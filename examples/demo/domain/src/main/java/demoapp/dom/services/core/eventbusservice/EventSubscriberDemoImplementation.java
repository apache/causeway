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
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.Nature;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.control.AsyncControl;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import demoapp.dom.services.core.eventbusservice.EventBusServiceDemoVm.UiButtonEvent;

import static demoapp.dom._infra.utils.LogUtils.emphasize;

@Service
@Named("demo.eventSubscriber")
@Qualifier("demo")
@Log4j2
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class EventSubscriberDemoImplementation {

    final WrapperFactory wrapper;
    final FactoryService factoryService;

    @EventListener(UiButtonEvent.class) // <-- listen on the event, triggered by button in the UI
    public void on(UiButtonEvent event) {

        log.info(emphasize("UiButtonEvent")); // <-- log to the console

        val eventLogWriter = factoryService.get(EventLogWriter.class); // <-- get a new writer

        wrapper.asyncWrap(eventLogWriter, AsyncControl.returningVoid()).storeEvent(event);

    }

    @DomainObject(
            nature = Nature.BEAN, // <-- have this Object's lifecycle managed by Spring
            logicalTypeName = "demo.eventLogWriter")
    public static class EventLogWriter {

        @Inject private EventLogEntryRepository<? extends Object> eventLogEntryRepository;

        @Action // called asynchronously by above invocation
        public void storeEvent(UiButtonEvent event) {

            eventLogEntryRepository.storeEvent(event);
        }

    }



}
