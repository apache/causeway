/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.runtime.services.eventbus;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.collections._Maps;

public class EventBusServiceDefaultTest {

    EventBusServiceDefault eventBusService;

    @Before
    public void setUp() throws Exception {
        eventBusService = new EventBusServiceDefault() {
        	{
        		serviceRegistry = new ServiceRegistry() {
					@Override public <T> Iterable<T> lookupServices(Class<T> service) { return null; }
					@Override public <T> T lookupService(Class<T> service) { return null; }
					@Override public <T> T injectServicesInto(T domainObject) {	return null; }
					@Override public List<Object> getRegisteredServices() { return null; }
				}; 
        	}
        };
    }

    public static class Init extends EventBusServiceDefaultTest {

        @Test
        public void emptyMap() throws Exception {
            eventBusService.init(Collections.<String, String>emptyMap());

            assertThat(eventBusService.getImplementation(), isIn(new String[] {"auto", "plugin"}));
            assertThat(eventBusService.isAllowLateRegistration(), is(false));
        }

        @Test
        public void allowLateRegistration_setToFalse() throws Exception {
            eventBusService.init(_Maps.unmodifiable(EventBusServiceDefault.KEY_ALLOW_LATE_REGISTRATION, "false"));
            assertThat(eventBusService.isAllowLateRegistration(), is(false));
        }

        @Test
        public void allowLateRegistration_setToTrue() throws Exception {
            eventBusService.init(_Maps.unmodifiable(EventBusServiceDefault.KEY_ALLOW_LATE_REGISTRATION, "true"));
            assertThat(eventBusService.isAllowLateRegistration(), is(true));
        }

        @Test
        public void allowLateRegistration_setToTrueMixedCase() throws Exception {
            eventBusService.init(_Maps.unmodifiable(EventBusServiceDefault.KEY_ALLOW_LATE_REGISTRATION, "TrUe"));
            assertThat(eventBusService.isAllowLateRegistration(), is(true));
        }

        @Test
        public void allowLateRegistration_setToEmptyString() throws Exception {
            eventBusService.init(_Maps.unmodifiable(EventBusServiceDefault.KEY_ALLOW_LATE_REGISTRATION, ""));
            assertThat(eventBusService.isAllowLateRegistration(), is(false));
        }

        @Test
        public void allowLateRegistration_setToGarbage() throws Exception {
            eventBusService.init(_Maps.unmodifiable(EventBusServiceDefault.KEY_ALLOW_LATE_REGISTRATION, "SDF$%FDVDFG"));
            assertThat(eventBusService.isAllowLateRegistration(), is(false));
        }

        @Test
        public void implementation_setToGuava() throws Exception {
            eventBusService.init(_Maps.unmodifiable(EventBusServiceDefault.KEY_EVENT_BUS_IMPLEMENTATION, "guava"));
            assertThat(eventBusService.getImplementation(), is("guava"));
        }

        @Test
        public void implementation_setToGuavaMixedCaseRequiringTrimming() throws Exception {
            eventBusService.init(_Maps.unmodifiable(EventBusServiceDefault.KEY_EVENT_BUS_IMPLEMENTATION, "  GuAvA "));
            assertThat(eventBusService.getImplementation(), is("guava"));
        }

        @Test
        public void implementation_setToAxon() throws Exception {
            eventBusService.init(_Maps.unmodifiable(EventBusServiceDefault.KEY_EVENT_BUS_IMPLEMENTATION, "axon"));
            assertThat(eventBusService.getImplementation(), is("axon"));
        }

        @Test
        public void implementation_setToAxonMixedCaseRequiringTrimming() throws Exception {
            eventBusService.init(_Maps.unmodifiable(EventBusServiceDefault.KEY_EVENT_BUS_IMPLEMENTATION, " AxOn   "));
            assertThat(eventBusService.getImplementation(), is("axon"));
        }

        @Test
        public void implementation_setToEmptyString() throws Exception {
            eventBusService.init(_Maps.unmodifiable(EventBusServiceDefault.KEY_EVENT_BUS_IMPLEMENTATION, ""));
            assertThat(eventBusService.getImplementation(), isIn(new String[] {"auto", "plugin"}));
        }

        @Test
        public void implementation_setToAnythingElse() throws Exception {
            eventBusService.init(_Maps.unmodifiable(EventBusServiceDefault.KEY_EVENT_BUS_IMPLEMENTATION,
                    "com.mycompany.my.event.bus.Implementation"));
            assertThat(eventBusService.getImplementation(), is("com.mycompany.my.event.bus.Implementation"));
        }

    }
   
}