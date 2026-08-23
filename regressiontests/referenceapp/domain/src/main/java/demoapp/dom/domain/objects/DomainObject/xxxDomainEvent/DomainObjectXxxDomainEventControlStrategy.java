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
package demoapp.dom.domain.objects.DomainObject.xxxDomainEvent;

import java.util.List;

import org.apache.causeway.applib.events.domain.AbstractDomainEvent;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;

//tag::class[]
enum DomainObjectXxxDomainEventControlStrategy {

    DO_NOTHING{
        @Override
        void on(final DomainObjectXxxDomainEventPage.DomainObjectXxxDomainEventMarker ev, final ServiceRegistry serviceRegistry) {
        }
    },
    // ...
//end::class[]

//tag::hide[]
    HIDE {
        @Override
        void on(final DomainObjectXxxDomainEventPage.DomainObjectXxxDomainEventMarker ev, final ServiceRegistry serviceRegistry) {
            if (ev instanceof AbstractDomainEvent) {
                var domainEvent = (AbstractDomainEvent<?>) ev;
                switch (domainEvent.getEventPhase()) {
                    case HIDE:
                        domainEvent.hide();
                        break;
                }
            }
        }
    },
//end::hide[]
//tag::disable[]
    DISABLE {
        @Override
        void on(final DomainObjectXxxDomainEventPage.DomainObjectXxxDomainEventMarker ev, final ServiceRegistry serviceRegistry) {
            if (ev instanceof AbstractDomainEvent) {
                var domainEvent = (AbstractDomainEvent<?>) ev;
                switch (domainEvent.getEventPhase()) {
                    case DISABLE:
                        domainEvent.disable("ControlStrategy set to DISABLE");
                        break;
                }
            }
        }
    },
//end::disable[]
//tag::validate[]
    VALIDATE_MUST_BE_UPPER_CASE{
        @Override
        void on(final DomainObjectXxxDomainEventPage.DomainObjectXxxDomainEventMarker ev, final ServiceRegistry serviceRegistry) {
            if (ev instanceof DomainObjectXxxDomainEventPage.ActionEvent) {
                var actionEvent = (DomainObjectXxxDomainEventPage.ActionEvent) ev;
                switch (actionEvent.getEventPhase()) {
                    case VALIDATE:
                        String argument = (String) actionEvent.getArguments().get(0);
                        if (!argument.toUpperCase().equals(argument)) {
                            actionEvent.invalidate("must be upper case");
                        }
                        break;
                }
            }
            if (ev instanceof DomainObjectXxxDomainEventPage.PropertyEvent) {
                var propertyEvent = (DomainObjectXxxDomainEventPage.PropertyEvent) ev;
                switch (propertyEvent.getEventPhase()) {
                    case VALIDATE:
                        Object newValue = propertyEvent.getNewValue();
                        if(!newValue.toString().toUpperCase().equals(newValue)) {
                            propertyEvent.invalidate("must be upper case");
                        }
                        break;
                }
            }
        }
    },
//end::validate[]
//tag::executing[]
    EXECUTING_FORCE_UPPER_CASE{
        @Override
        void on(final DomainObjectXxxDomainEventPage.DomainObjectXxxDomainEventMarker ev, final ServiceRegistry serviceRegistry) {
            if (ev instanceof DomainObjectXxxDomainEventPage.ActionEvent) {
                var actionEvent = (DomainObjectXxxDomainEventPage.ActionEvent) ev;
                switch (actionEvent.getEventPhase()) {
                    case EXECUTING:
                        List<Object> arguments = actionEvent.getArguments();
                        String newValue = ((String) arguments.get(0)).toUpperCase();
                        arguments.set(0, newValue);
                        break;
                }
            }
            if (ev instanceof DomainObjectXxxDomainEventPage.PropertyEvent) {
                var propertyEvent = (DomainObjectXxxDomainEventPage.PropertyEvent) ev;
                switch (propertyEvent.getEventPhase()) {
                    case EXECUTING:
                        String newValue = propertyEvent.getNewValue().toString().toUpperCase();
                        propertyEvent.setNewValue(newValue);
                        break;
                }
            }
        }
    },
//end::executing[]
//tag::executed[]
    EXECUTED_ANNOUNCE{
        @Override
        void on(final DomainObjectXxxDomainEventPage.DomainObjectXxxDomainEventMarker ev, final ServiceRegistry serviceRegistry) {
            if (ev instanceof DomainObjectXxxDomainEventPage.ActionEvent) {
                var actionEvent = (DomainObjectXxxDomainEventPage.ActionEvent) ev;
                switch (actionEvent.getEventPhase()) {
                    case EXECUTED:
                        serviceRegistry
                            .lookupService(MessageService.class)
                            .ifPresent(ms ->
                                    ms.informUser("Changed using updateText('" + actionEvent.getArguments().get(0) + "')")
                            );
                        break;
                }
            }
            if (ev instanceof DomainObjectXxxDomainEventPage.PropertyEvent) {
                var propertyEvent = (DomainObjectXxxDomainEventPage.PropertyEvent) ev;
                switch (propertyEvent.getEventPhase()) {
                    case EXECUTED:
                        serviceRegistry
                            .lookupService(MessageService.class)
                            .ifPresent(ms ->
                                    ms.informUser(
                                            String.format("Changed from %s to %s"
                                                    , propertyEvent.getOldValue()
                                                    , propertyEvent.getNewValue()))

                            );
                        break;
                }
            }
        }
    }
//end::executed[]

//tag::class[]
    ;
    abstract void on(DomainObjectXxxDomainEventPage.DomainObjectXxxDomainEventMarker ev, ServiceRegistry serviceRegistry);
}
//end::class[]
