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
package demoapp.dom.domain.actions.Action.domainEvent.subscribers;

import javax.inject.Inject;

import org.springframework.context.event.EventListener;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.services.registry.ServiceRegistry;

import demoapp.dom.domain.actions.Action.domainEvent.ActionDomainEventVm;
import demoapp.dom.domain.actions.Action.domainEvent.ActionDomainEventVm_mixinUpdateText;

// tag::class[]
@DomainService(objectType = "demo.ActionDomainEventControlService")
class ActionDomainEventControlService {

    ActionDomainEventControlStrategy controlStrategy = ActionDomainEventControlStrategy.DO_NOTHING;         // <.>

    @EventListener(ActionDomainEventVm.UpdateTextDomainEvent.class)       // <.>
    public void on(ActionDomainEventVm.UpdateTextDomainEvent ev) {
        controlStrategy.on(ev, serviceRegistry);
    }

    @EventListener(ActionDomainEventVm_mixinUpdateText.DomainEvent.class) // <.>
    public void on(ActionDomainEventVm_mixinUpdateText.DomainEvent ev) {
        controlStrategy.on(ev, serviceRegistry);
    }

    @Inject
    ServiceRegistry serviceRegistry;
}
// end::class[]
