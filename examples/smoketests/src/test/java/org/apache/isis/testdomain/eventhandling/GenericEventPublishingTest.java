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
package org.apache.isis.testdomain.eventhandling;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.runtime.services.eventbus.EventBusServiceSpring;

import lombok.Getter;
import lombok.Value;
import lombok.val;

@SpringBootTest(
        classes = { 
                GenericEventPublishingTest.TestConfig.class, 
                GenericEventPublishingTest.TestPublisher.class,
                GenericEventPublishingTest.TestListener.class, 
                EventBusServiceSpring.class
        })
class GenericEventPublishingTest {

    @Inject private TestPublisher testPublisher;
    @Inject private TestListener testListener;

    @Test
    void firedEventShouldBeReceivedImmediately() {

        val history = testListener.getHistory();

        testPublisher.fireHelloWorld();

        assertEquals("Hello World!", history.toString());
    }

    // -- HELPER

    @Configuration
    static class TestConfig {
        // no specific config required
    }

    @Service
    public static class TestPublisher {

        @Inject EventBusService eventBusService;
        
        public void fireHelloWorld() {
            eventBusService.post(GenericEvent.of("Hello World!"));
        }

    }

    @Service 
    public static class TestListener {

        @Getter
        private final StringBuilder history = new StringBuilder();

        //XXX unfortunately Spring does not yet support the @Observes annotation
        // we are lucky if it does in the future
        @EventListener(GenericEvent.class)
        public void receiveHelloWorld(@Observes GenericEvent<String> event) {
            history.append(event.getWhat());
        }

    }

    @Value(staticConstructor = "of")
    static class GenericEvent<T> {
        private T what;
    }

}
