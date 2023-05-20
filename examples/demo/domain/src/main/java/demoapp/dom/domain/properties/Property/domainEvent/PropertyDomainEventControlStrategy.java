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
package demoapp.dom.domain.properties.Property.domainEvent;

import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;

//tag::class[]
enum PropertyDomainEventControlStrategy {

    DO_NOTHING{
        @Override
        void on(PropertyDomainEventPage.TextDomainEvent ev, ServiceRegistry serviceRegistry) {
        }
    },
    // ...
//end::class[]

//tag::hide[]
    HIDE {
        @Override
        void on(PropertyDomainEventPage.TextDomainEvent ev, ServiceRegistry serviceRegistry) {
            switch (ev.getEventPhase()) {
                case HIDE:
                    ev.hide();
                    break;
            }
        }
    },
//end::hide[]
//tag::disable[]
    DISABLE{
        @Override
        void on(PropertyDomainEventPage.TextDomainEvent ev, ServiceRegistry serviceRegistry) {
            switch (ev.getEventPhase()) {
                case DISABLE:
                    ev.disable("ControlStrategy set to DISABLE");
                    break;
            }

        }
    },
//end::disable[]
//tag::validate[]
    VALIDATE_MUST_BE_UPPER_CASE{
        @Override
        void on(PropertyDomainEventPage.TextDomainEvent ev, ServiceRegistry serviceRegistry) {
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
//end::validate[]
//tag::executing[]
    EXECUTING_FORCE_UPPER_CASE{
        @Override
        void on(PropertyDomainEventPage.TextDomainEvent ev, ServiceRegistry serviceRegistry) {

            switch (ev.getEventPhase()) {
                case EXECUTING:
                    String newValue = ev.getNewValue().toUpperCase();
                    ev.setNewValue(newValue);
                    break;
            }
        }
    },
//end::executing[]
//tag::executed[]
    EXECUTED_ANNOUNCE{
        @Override
        void on(PropertyDomainEventPage.TextDomainEvent ev, ServiceRegistry serviceRegistry) {
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
    }
//end::executed[]

//tag::class[]
    ;
    abstract void on(PropertyDomainEventPage.TextDomainEvent ev, ServiceRegistry serviceRegistry);
}
//end::class[]
