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
package org.apache.isis.extensions.fixtures;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import org.apache.isis.commons.internal.ioc.spring._Spring;
import org.apache.isis.extensions.fixtures.events.FixturesInstalledEvent;
import org.apache.isis.extensions.fixtures.events.FixturesInstallingEvent;

/**
 * @since 2.0
 */
@Service
public class FixturesEventService {

    @Inject Event<FixturesInstallingEvent> fixturesInstallingEvents;
    @Inject Event<FixturesInstalledEvent> fixturesInstalledEvents;

    // -- FIXTURES

    public void fireFixturesInstalling(FixturesInstallingEvent fixturesInstallingEvent) {
        fixturesInstallingEvents.fire(fixturesInstallingEvent);
    }

    public void fireFixturesInstalled(FixturesInstalledEvent fixturesInstalledEvent) {
        fixturesInstalledEvents.fire(fixturesInstalledEvent);
    }

    // -- TODO[2133] IS THIS STILL REQUIRED?

    @Bean
    public Event<FixturesInstallingEvent> fixturesInstallingEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }

    @Bean
    public Event<FixturesInstalledEvent> fixturesInstalledEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }

}
