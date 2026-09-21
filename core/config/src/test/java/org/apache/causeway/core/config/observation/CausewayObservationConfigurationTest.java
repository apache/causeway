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
package org.apache.causeway.core.config.observation;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CausewayObservationConfigurationTest {

    @Test
    void inactiveProfileUsesCausewayNoopRegistryAndIgnoresApplicationRegistry() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            final ObservationRegistry applicationRegistry = ObservationRegistry.create();
            context.registerBean(
                    "applicationObservationRegistry",
                    ObservationRegistry.class,
                    () -> applicationRegistry);
            context.register(CausewayObservationConfiguration.class);
            context.refresh();

            final CausewayObservationIntegration integration = context.getBean(
                    CausewayObservationIntegration.class);
            assertTrue(integration.isNoop());
            assertSame(ObservationRegistry.NOOP, integration.observationRegistry());
            assertNotSame(applicationRegistry, integration.observationRegistry());

            integration.createNotStarted(getClass(), "causeway.test.inactive")
                    .observe(() -> { });
            assertNull(integration.observationRegistry().getCurrentObservation());
        }
    }

    @Test
    void providerAddsStableCausewayMetadataAndAppliesCustomization() {
        final ObservationRegistry registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(new ObservationHandler<Observation.Context>() {
            @Override
            public boolean supportsContext(final Observation.Context context) {
                return true;
            }
        });
        final CausewayObservationIntegration integration = new CausewayObservationIntegration(registry);

        final Observation observation = integration.provider(
                getClass(),
                CausewayObservationIntegration.withModuleName("causeway-core-config"))
                .get("causeway.test.metadata");

        observation.start();
        try {
            assertEquals("causeway.test.metadata", observation.getContext().getName());
            assertEquals(
                    getClass().getSimpleName(),
                    observation.getContext().getLowCardinalityKeyValue("causeway.bean").getValue());
            assertEquals(
                    "core-config",
                    observation.getContext().getLowCardinalityKeyValue("causeway.module").getValue());
        } finally {
            observation.stop();
        }
    }

    @Test
    void activeProfileUsesRealRegistryAndStartsWithoutJavaAgent() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            final ObservationRegistry applicationRegistry = ObservationRegistry.create();
            context.getEnvironment().setActiveProfiles("observation");
            context.registerBean(
                    "applicationObservationRegistry",
                    ObservationRegistry.class,
                    () -> applicationRegistry);
            context.register(CausewayObservationConfiguration.class);
            context.refresh();

            final CausewayObservationIntegration integration = context.getBean(
                    CausewayObservationIntegration.class);
            assertFalse(integration.isNoop());
            assertNotSame(applicationRegistry, integration.observationRegistry());

            final Observation observation = integration.createNotStarted(
                    getClass(), "causeway.test.active");
            observation.start();
            try (Observation.Scope ignored = observation.openScope()) {
                assertSame(observation, integration.observationRegistry().getCurrentObservation());
            } finally {
                observation.stop();
            }
            assertNull(integration.observationRegistry().getCurrentObservation());
        }
    }
}
