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

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.ioc.BeanAdapter;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.plugins.environment.IsisSystemEnvironment;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertThat;

public class EventBusServiceDefaultTest {

    EventBusServiceDefault eventBusService;

    @Before
    public void setUp() throws Exception {
        
        _Config.clear(); 
        IsisSystemEnvironment.setUnitTesting(true);
        
        eventBusService = new EventBusServiceDefault() {
        	{
        		serviceRegistry = new ServiceRegistry() {

                    @Override
                    public boolean isDomainServiceType(Class<?> cls) {
                        return false;
                    }

                    @Override
                    public Stream<BeanAdapter> streamRegisteredBeans() {
                        return null;
                    }

                    @Override
                    public Stream<Object> streamServices() {
                        return null;
                    }

                    @Override
                    public boolean isRegisteredBean(Class<?> cls) {
                        return false;
                    }

                    @Override
                    public void validateServices() {
                    }
					
				}; 
        	}
        };
    }

    public static class Init extends EventBusServiceDefaultTest {

        @Test
        public void emptyMap() throws Exception {
            eventBusService.init();
            assertThat(eventBusService.getImplementation(), isIn(new String[] {"auto", "plugin"}));
            assertThat(eventBusService.isAllowLateRegistration(), is(false));
        }

        @Test
        public void allowLateRegistration_setToFalse() throws Exception {
            _Config.put(EventBusServiceDefault.KEY_ALLOW_LATE_REGISTRATION, false);
            eventBusService.init();
            assertThat(eventBusService.isAllowLateRegistration(), is(false));
        }

        @Test
        public void allowLateRegistration_setToTrue() throws Exception {
            _Config.put(EventBusServiceDefault.KEY_ALLOW_LATE_REGISTRATION, true);
            eventBusService.init();
            assertThat(eventBusService.isAllowLateRegistration(), is(true));
        }

        @Test
        public void allowLateRegistration_setToTrueMixedCase() throws Exception {
            _Config.put(EventBusServiceDefault.KEY_ALLOW_LATE_REGISTRATION, "TrUe");
            eventBusService.init();
            assertThat(eventBusService.isAllowLateRegistration(), is(true));
        }

        @Test
        public void allowLateRegistration_setToEmptyString() throws Exception {
            _Config.put(EventBusServiceDefault.KEY_ALLOW_LATE_REGISTRATION, "");
            eventBusService.init();
            assertThat(eventBusService.isAllowLateRegistration(), is(false));
        }

        @Test @Ignore("why would we allow this?")
        public void allowLateRegistration_setToGarbage() throws Exception {
            _Config.put(EventBusServiceDefault.KEY_ALLOW_LATE_REGISTRATION, "SDF$%FDVDFG");
            eventBusService.init();
            assertThat(eventBusService.isAllowLateRegistration(), is(false));
        }

        @Test
        public void implementation_setToGuava() throws Exception {
            _Config.put(EventBusServiceDefault.KEY_EVENT_BUS_IMPLEMENTATION, "guava");
            eventBusService.init();
            assertThat(eventBusService.getImplementation(), is("guava"));
        }

        @Test
        public void implementation_setToGuavaMixedCaseRequiringTrimming() throws Exception {
            _Config.put(EventBusServiceDefault.KEY_EVENT_BUS_IMPLEMENTATION, "  GuAvA ");
            eventBusService.init();
            assertThat(eventBusService.getImplementation(), is("guava"));
        }

        @Test
        public void implementation_setToAxon() throws Exception {
            _Config.put(EventBusServiceDefault.KEY_EVENT_BUS_IMPLEMENTATION, "axon");
            eventBusService.init();
            assertThat(eventBusService.getImplementation(), is("axon"));
        }

        @Test
        public void implementation_setToAxonMixedCaseRequiringTrimming() throws Exception {
            _Config.put(EventBusServiceDefault.KEY_EVENT_BUS_IMPLEMENTATION, " AxOn   ");
            eventBusService.init();
            assertThat(eventBusService.getImplementation(), is("axon"));
        }

        @Test
        public void implementation_setToEmptyString() throws Exception {
            _Config.put(EventBusServiceDefault.KEY_EVENT_BUS_IMPLEMENTATION, "");
            eventBusService.init();
            assertThat(eventBusService.getImplementation(), isIn(new String[] {"auto", "plugin"}));
        }

        @Test
        public void implementation_setToAnythingElse() throws Exception {
            _Config.put(EventBusServiceDefault.KEY_EVENT_BUS_IMPLEMENTATION, 
                    "com.mycompany.my.event.bus.Implementation");
            eventBusService.init();
            assertThat(eventBusService.getImplementation(), is("com.mycompany.my.event.bus.Implementation"));
        }

    }
   
}