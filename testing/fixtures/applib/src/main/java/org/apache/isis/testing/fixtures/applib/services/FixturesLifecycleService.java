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
package org.apache.isis.testing.fixtures.applib.services;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.commons.internal.environment.IsisSystemEnvironment;
import org.apache.isis.testing.fixtures.applib.legacy.FixtureClock;
import org.apache.isis.runtime.session.IsisSessionFactory;

@Service
@Named("isisTstFixtures.FixturesLifecycleService")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class FixturesLifecycleService {

    @SuppressWarnings("unused")
    @Inject private IsisSessionFactory isisSessionFactory; // depends on relationship  
    @Inject private IsisSystemEnvironment isisSystemEnvironment;

    @PostConstruct
    public void postConstruct() {

        // a bit of a workaround, but required if anything in the metamodel (for example, a
        // ValueSemanticsProvider for a date value type) needs to use the Clock singleton
        // we do this after loading the services to allow a service to prime a different clock
        // implementation (eg to use an NTP time service).
        if (isisSystemEnvironment.isPrototyping() && !Clock.isInitialized()) {
            FixtureClock.initialize();
        }

        //
        // installFixturesIfRequired
        //
        //        final FixturesInstallerFromConfiguration fixtureInstaller =
        //                new FixturesInstallerFromConfiguration();
        //        fixtureInstaller.installFixtures();  


    }

    @PreDestroy
    public void preDestroy() {

    }

}
