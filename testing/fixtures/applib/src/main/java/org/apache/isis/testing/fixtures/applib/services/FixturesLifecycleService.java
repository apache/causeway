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

import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.commons.internal.environment.IsisSystemEnvironment;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.runtime.context.session.AppLifecycleEvent;
import org.apache.isis.core.runtime.iactn.IsisInteractionFactory;
import org.apache.isis.core.runtime.iactn.template.AbstractIsisInteractionTemplate;
import org.apache.isis.core.security.authentication.standard.SimpleSession;
import org.apache.isis.testing.fixtures.applib.clock.FixtureClock;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

import lombok.val;
import lombok.var;
import lombok.extern.log4j.Log4j2;

@Service
@Named("isisTstFixtures.FixturesLifecycleService")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Log4j2
public class FixturesLifecycleService {

    @SuppressWarnings("unused")

    @Inject
    private IsisInteractionFactory isisInteractionFactory; // depends on relationship
    @Inject
    private IsisSystemEnvironment isisSystemEnvironment;
    @Inject
    private IsisConfiguration isisConfiguration;
    @Inject
    private ServiceInjector serviceInjector;
    @Inject
    private FixtureScripts fixtureScripts;

    private FixtureScript initialFixtureScript;


    @PostConstruct
    public void postConstruct() {

        // a bit of a workaround, but required if anything in the metamodel (for example, a
        // ValueSemanticsProvider for a date value type) needs to use the Clock singleton
        // we do this after loading the services to allow a service to prime a different clock
        // implementation (eg to use an NTP time service).
        if (isisSystemEnvironment.isPrototyping() && !Clock.isInitialized()) {
            FixtureClock.initialize();
        }

        final Class<?> initialScript = isisConfiguration.getTesting().getFixtures().getInitialScript();
        if (initialScript != null && FixtureScript.class.isAssignableFrom(initialScript)) {
            try {
                initialFixtureScript = (FixtureScript) initialScript.newInstance();
                serviceInjector.injectServicesInto(initialFixtureScript);
            } catch (InstantiationException | IllegalAccessException e) {
                initialFixtureScript = null;
            }
        }
    }

    @EventListener(AppLifecycleEvent.class)
    public void onAppLifecycleEvent(final AppLifecycleEvent event) {

        val eventType = event.getEventType();

        log.debug("received app lifecycle event {}", eventType);

        switch (eventType) {
            case appPreMetamodel:
                break;
            case appPostMetamodel:

                log.info("SEED");

                if(initialFixtureScript != null) {
                        fixtureScripts.run(initialFixtureScript);
                }
                break;

            default:
                throw _Exceptions.unmatchedCase(eventType);
        }
    }


    @PreDestroy
    public void preDestroy() {

    }

}
