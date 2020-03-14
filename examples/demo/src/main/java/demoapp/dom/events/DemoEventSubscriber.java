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
package demoapp.dom.events;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import demoapp.dom.events.EventsDemo.UiButtonEvent;

import static demoapp.utils.DemoUtils.emphasize;

@Service
@Named("demoapp.eventSubscriber")
@Qualifier("demo")
@Log4j2
public class DemoEventSubscriber {

    @Inject private WrapperFactory wrapper;
    @Inject private FactoryService factoryService;
    
    @EventListener(UiButtonEvent.class) // <-- listen on the event, triggered by button in the UI 
    public void on(UiButtonEvent event) {

        log.info(emphasize("UiButtonEvent")); // <-- log to the console
        
        val eventLogWriter = factoryService.get(EventLogWriter.class); // <-- get a new writer
        
        wrapper.async(eventLogWriter).storeEvent(event);

    }

    @DomainObject(
            nature = Nature.BEAN, // <-- have this Object's lifecycle managed by Spring
            objectType = "demoapp.eventLogWriter")
    public static class EventLogWriter {

        @Inject private EventLogRepository eventLogRepository;
        
        @Action // called asynchronously by above invocation 
        public void storeEvent(UiButtonEvent event) {
            
            val entry = EventLogEntry.of(event);
            eventLogRepository.add(entry);
            
        }
        
    }
    
    

}
