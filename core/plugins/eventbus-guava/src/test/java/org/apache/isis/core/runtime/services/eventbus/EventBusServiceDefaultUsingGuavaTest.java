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

import com.google.common.eventbus.Subscribe;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.config.internal._Config;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class EventBusServiceDefaultUsingGuavaTest {

    EventBusServiceDefault eventBusService;

    @Before
    public void setUp() throws Exception {
        eventBusService = new EventBusServiceDefault() {
        	{
        		serviceRegistry = new ServiceRegistry() {
					@Override public <T> Optional<T> lookupService(Class<T> service) { return null; }
					@Override public <T> T injectServicesInto(T domainObject) {	return null; }
                    @Override public Stream<Object> streamServices() {return null;}
                    @Override public <T> Stream<T> streamServices(Class<T> serviceClass) {return null;}
				}; 
        	}
        };
    }

    public static class Post extends EventBusServiceDefaultTest {
    	
    	private final static String EVENTBUS_IMPL_NAME = "guava";
    	
    	public static class Type1 {
    		String name = "1";
    	}
    	
    	public static class Type3 {
    		String name = "3";
    	}

        public static class Subscriber1 {
        	Type1 obj;
            @Subscribe
            public void on1(Type1 obj) {
                this.obj = obj;
            }
        }
        
        public static class Subscriber2 {
        	Type1 obj;
            @Subscribe
            public void on2(Type1 obj) {
                this.obj = obj;
            }
        }
        
        public static class Subscriber3 {
        	Type3 obj;
            @Subscribe
            public void on3(Type3 obj) {
                this.obj = obj;
            }
        }
        

        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        Subscriber1 subscriber1;
        Subscriber2 subscriber2;
        Subscriber3 subscriber3;

        @Before
        public void setUp() throws Exception {
            super.setUp();
            subscriber1 = new Subscriber1();
            subscriber2 = new Subscriber2();
            subscriber3 = new Subscriber3();
        }

        @Test
        public void allow_late_registration_means_can_register_after_post() throws Exception {
            // given
            _Config.put(EventBusServiceDefault.KEY_ALLOW_LATE_REGISTRATION, true);
            _Config.put(EventBusServiceDefault.KEY_EVENT_BUS_IMPLEMENTATION, EVENTBUS_IMPL_NAME);
            eventBusService.init();
            
            assertThat(eventBusService.isAllowLateRegistration(), is(true));
            assertThat(eventBusService.getImplementation(), is(EVENTBUS_IMPL_NAME));

            eventBusService.post(new Object());

            // when
            eventBusService.register(subscriber1);

            // then
            assertThat(subscriber1.obj, is(nullValue()));
        }

        @Test
        public void disallow_late_registration_means_cannot_register_after_post() throws Exception {
            // given
            _Config.put(EventBusServiceDefault.KEY_ALLOW_LATE_REGISTRATION, false);
            _Config.put(EventBusServiceDefault.KEY_EVENT_BUS_IMPLEMENTATION, EVENTBUS_IMPL_NAME);
            eventBusService.init();
            
            assertThat(eventBusService.isAllowLateRegistration(), is(false));
            assertThat(eventBusService.getImplementation(), is(EVENTBUS_IMPL_NAME));

            eventBusService.post(new Object());

            // expect
            expectedException.expect(IllegalStateException.class);

            // when
            eventBusService.register(new Subscriber1());
        }

        @Test
        public void disallow_late_registration_means_can_register_before_post() throws Exception {
            // given
            _Config.put(EventBusServiceDefault.KEY_ALLOW_LATE_REGISTRATION, false);
            _Config.put(EventBusServiceDefault.KEY_EVENT_BUS_IMPLEMENTATION, EVENTBUS_IMPL_NAME);
            eventBusService.init();
            
            assertThat(eventBusService.isAllowLateRegistration(), is(false));
            assertThat(eventBusService.getImplementation(), is(EVENTBUS_IMPL_NAME));

            eventBusService.register(subscriber1);

            // when
            final Type1 event = new Type1();
            eventBusService.post(event);

            // then
            assertThat(subscriber1.obj, is(event));
        }
        
        @Test
        public void multiple_subscribers_receive_same_event_if_same_type() throws Exception {
            // given
            _Config.put(EventBusServiceDefault.KEY_ALLOW_LATE_REGISTRATION, false);
            _Config.put(EventBusServiceDefault.KEY_EVENT_BUS_IMPLEMENTATION, EVENTBUS_IMPL_NAME);
            eventBusService.init();
            
            assertThat(eventBusService.isAllowLateRegistration(), is(false));
            assertThat(eventBusService.getImplementation(), is(EVENTBUS_IMPL_NAME));

            eventBusService.register(subscriber1);
            eventBusService.register(subscriber2);
            eventBusService.register(subscriber3);

            // when
            final Type1 event1 = new Type1();
            eventBusService.post(event1);

            // then
            assertThat(subscriber1.obj, is(event1));
            assertThat(subscriber2.obj, is(event1));
            assertThat(subscriber3.obj, is(nullValue()));
            
            // when
            final Type3 event3 = new Type3();
            eventBusService.post(event3);
            
            // then
            assertThat(subscriber1.obj, is(event1));
            assertThat(subscriber2.obj, is(event1));
            assertThat(subscriber3.obj, is(event3));
        }
        
        @Test
        public void multiple_subscribers_eventlistener() throws Exception {
            // given
            _Config.put(EventBusServiceDefault.KEY_ALLOW_LATE_REGISTRATION, false);
            _Config.put(EventBusServiceDefault.KEY_EVENT_BUS_IMPLEMENTATION, EVENTBUS_IMPL_NAME);
            eventBusService.init();
            
            assertThat(eventBusService.isAllowLateRegistration(), is(false));
            assertThat(eventBusService.getImplementation(), is(EVENTBUS_IMPL_NAME));

            eventBusService.addEventListener(Type1.class, x->subscriber1.obj=x);
            eventBusService.addEventListener(Type1.class, x->subscriber2.obj=x);
            eventBusService.addEventListener(Type3.class, x->subscriber3.obj=x);
            
            // when
            final Type1 event1 = new Type1();
            eventBusService.post(event1);

            // then
            assertThat(subscriber1.obj, is(event1));
            assertThat(subscriber2.obj, is(event1));
            assertThat(subscriber3.obj, is(nullValue()));
            
            // when
            final Type3 event3 = new Type3();
            eventBusService.post(event3);
            
            // then
            assertThat(subscriber1.obj, is(event1));
            assertThat(subscriber2.obj, is(event1));
            assertThat(subscriber3.obj, is(event3));
        }

    }
}