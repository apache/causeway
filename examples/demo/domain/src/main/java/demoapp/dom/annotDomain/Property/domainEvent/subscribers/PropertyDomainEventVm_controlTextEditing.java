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

import javax.inject.Inject;

import org.springframework.context.event.EventListener;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.registry.ServiceRegistry;

import lombok.RequiredArgsConstructor;

import demoapp.dom.annotDomain.Property.domainEvent.PropertyDomainEventJdo;


//tag::class[]
@Action()
@ActionLayout(promptStyle = PromptStyle.DIALOG_SIDEBAR)
@RequiredArgsConstructor
public class PropertyDomainEventVm_controlTextEditing {

    private final PropertyDomainEventJdo propertyDomainEventJdo;

    public PropertyDomainEventJdo act(final ControlStrategy controlStrategy) {
        eventControlService.controlStrategy = controlStrategy;
        return propertyDomainEventJdo;
    }
    public ControlStrategy default0Act() {
        return eventControlService.controlStrategy;
    }

    @Inject
    ControlService eventControlService;

    enum ControlStrategy {
        HIDE {
            @Override
            void on(PropertyDomainEventJdo.TextDomainEvent ev
                    , ServiceRegistry serviceRegistry) {
                switch (ev.getEventPhase()) {
                    case HIDE:
                        ev.hide();
                        break;
                }
            }
        },
        DISABLE{
            @Override
            void on(PropertyDomainEventJdo.TextDomainEvent ev
                    , ServiceRegistry serviceRegistry) {
                switch (ev.getEventPhase()) {
                    case DISABLE:
                        ev.disable("ControlStrategy set to DISABLE");
                        break;
                }

            }
        },
        VALIDATE_MUST_BE_UPPER_CASE{
            @Override
            void on(PropertyDomainEventJdo.TextDomainEvent ev
                    , ServiceRegistry serviceRegistry) {
                switch (ev.getEventPhase()) {
                    case VALIDATE:
                        String newValue = ev.getNewValue();
                        if(!newValue.toUpperCase().equals(newValue)) {
                            ev.invalidate("must be upper case");
                        }
                        break;
                }

            }
        },
        VALIDATE_MUST_BE_LOWER_CASE{
            @Override
            void on(PropertyDomainEventJdo.TextDomainEvent ev
                    , ServiceRegistry serviceRegistry) {

                switch (ev.getEventPhase()) {
                    case VALIDATE:
                        String newValue = ev.getNewValue();
                        if (!newValue.toLowerCase().equals(newValue)) {
                            ev.invalidate("must be lower case");
                        }
                        break;
                }
            }
        },
        EXECUTING_FORCE_UPPER_CASE{
            @Override
            void on(PropertyDomainEventJdo.TextDomainEvent ev
                    , ServiceRegistry serviceRegistry) {

                switch (ev.getEventPhase()) {
                    case EXECUTING:
                        String newValue = ev.getNewValue().toUpperCase();
                        ev.setNewValue(newValue);
                        break;
                }
            }
        },
        EXECUTING_FORCE_LOWER_CASE{
            @Override
            void on(PropertyDomainEventJdo.TextDomainEvent ev
                    , ServiceRegistry serviceRegistry) {

                switch (ev.getEventPhase()) {
                    case EXECUTING:
                        String newValue = ev.getNewValue().toLowerCase();
                        ev.setNewValue(newValue);
                        break;
                }
            }
        },
        EXECUTED_ANNOUNCE{
            @Override
            void on(PropertyDomainEventJdo.TextDomainEvent ev
                    , ServiceRegistry serviceRegistry) {
                switch (ev.getEventPhase()) {
                    case EXECUTED:
                        serviceRegistry
                            .lookupService(MessageService.class)
                            .ifPresent(ms ->
                                    ms.informUser(
                                        String.format("Changed from %s to %s"
                                                , ev.getOldValue()
                                                , ev.getNewValue()))
                            );
                        break;
                }
            }
        },
        DO_NOTHING{
            @Override
            void on(PropertyDomainEventJdo.TextDomainEvent ev
                    , ServiceRegistry serviceRegistry) {

            }
        };

        abstract void on(PropertyDomainEventJdo.TextDomainEvent ev
                , ServiceRegistry serviceRegistry);
    }
    @DomainService(objectType = "demo.PropertyDomainEventControlService")
    static class ControlService {

        ControlStrategy controlStrategy = ControlStrategy.DO_NOTHING;

        @EventListener(PropertyDomainEventJdo.TextDomainEvent.class)
        public void on(PropertyDomainEventJdo.TextDomainEvent ev) {
            controlStrategy.on(ev, serviceRegistry);
        }

        @Inject
        ServiceRegistry serviceRegistry;
    }
}
//end::class[]
