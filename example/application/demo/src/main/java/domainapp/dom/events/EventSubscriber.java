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
package domainapp.dom.events;

import static domainapp.utils.DemoUtils.emphasize;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.events.domain.AbstractDomainEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;

import domainapp.dom.events.EventLogMenu.EventTestProgrammaticEvent;
import lombok.extern.java.Log;

@DomainService(nature=NatureOfService.DOMAIN)
@Log
public class EventSubscriber {

	@Inject private EventBusService eventBusService;
	@Inject private EventLog eventLog;

	public static class EventSubscriberEvent extends AbstractDomainEvent<Object> {
		private static final long serialVersionUID = 1L;
	}
	
	@PostConstruct
	public void init() {
		log.info(emphasize("init"));
		eventBusService.post(new EventSubscriberEvent());
	}

	@Programmatic
	//@com.google.common.eventbus.Subscribe
	@org.axonframework.eventhandling.EventHandler
	public void on(AbstractDomainEvent<?> ev) {
		if(ev.getEventPhase() != null && !ev.getEventPhase().isExecuted()) {
			return;
		}
		
		if(ev instanceof EventTestProgrammaticEvent) {
			log.info(emphasize("DomainEvent: "+ev.getClass().getName()));
			// store in event log
			eventLog.add(EventLogEntry.of(ev));	
		}
	}

}
