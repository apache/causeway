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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

class CausewayObservationAutoConfigurationTest {
    @Test void inactiveWithoutRegistry() {
        try (var context = context(false)) {
            assertTrue(context.getBean(CausewayObservationIntegration.class).isNoop());
            assertTrue(context.getBeansOfType(ObservationRegistry.class).isEmpty());
        }
    }
    @Test void inactivePreservesApplicationObservations() {
        try (var context = context(false, OneRegistry.class)) {
            var integration = context.getBean(CausewayObservationIntegration.class);
            assertSame(ObservationRegistry.NOOP, integration.observationRegistry());
            var registry = context.getBean(ObservationRegistry.class);
            assertFalse(registry.isNoop());
            var applicationObservation = Observation.start("application", registry);
            assertFalse(applicationObservation.isNoop());
            applicationObservation.stop();
            assertEquals(1, context.getBeansOfType(ObservationRegistry.class).size());
        }
    }
    @Test void activeFallbackWithoutExporter() {
        try (var context = context(true)) {
            var integration = context.getBean(CausewayObservationIntegration.class);
            assertSame(context.getBean(ObservationRegistry.class), integration.observationRegistry());
            assertDoesNotThrow(() -> integration.createNotStarted(getClass(), "test").observe(() -> {}));
        }
    }
    @Test void activeUsesApplicationRegistry() {
        try (var context = context(true, OneRegistry.class)) {
            assertSame(context.getBean("applicationRegistry"), context.getBean(CausewayObservationIntegration.class).observationRegistry());
            assertEquals(1, context.getBeansOfType(ObservationRegistry.class).size());
        }
    }
    @Test void activeUsesPrimaryRegistry() {
        try (var context = context(true, OneRegistry.class, PrimaryRegistry.class)) {
            assertSame(context.getBean("primaryRegistry"), context.getBean(CausewayObservationIntegration.class).observationRegistry());
            assertEquals(2, context.getBeansOfType(ObservationRegistry.class).size());
        }
    }
    @Test void inactiveDoesNotResolveAmbiguousApplicationRegistries() {
        try (var context = context(false, OneRegistry.class, OtherRegistry.class)) {
            assertTrue(context.getBean(CausewayObservationIntegration.class).isNoop());
        }
    }
    @Test void activeRejectsAmbiguousRegistries() {
        var error = assertThrows(Exception.class, () -> context(true, OneRegistry.class, OtherRegistry.class));
        Throwable cause = error;
        while (cause.getCause() != null) cause = cause.getCause();
        assertInstanceOf(NoUniqueBeanDefinitionException.class, cause);
    }
    @Test void policyDefaultsAndProgrammaticDefaultsAgree() {
        try (var context = context(true)) {
            var integration = context.getBean(CausewayObservationIntegration.class);
            assertEquals(CausewayObservationPolicy.DEFAULT, integration.policy());
            assertEquals(integration.policy(), new CausewayObservationIntegration(ObservationRegistry.NOOP).policy());
        }
    }
    @Test void bindsExplicitPolicyWithoutActivatingFrameworkObservations() {
        try (var context = configured(java.util.Map.of(
                "causeway.observation.include-user-name", "true",
                "causeway.observation.include-multi-tenancy-token", "true",
                "causeway.observation.duration-filtering-enabled", "true",
                "causeway.observation.jpa-duration-threshold", "7ms"))) {
            var integration = context.getBean(CausewayObservationIntegration.class);
            assertTrue(integration.isNoop());
            assertEquals(new CausewayObservationPolicy(true, true, true, java.time.Duration.ofMillis(7)), integration.policy());
        }
    }
    @Test void acceptsZeroThreshold() {
        try (var context = configured(java.util.Map.of("causeway.observation.jpa-duration-threshold", "0ms"))) {
            assertEquals(java.time.Duration.ZERO, context.getBean(CausewayObservationPolicy.class).jpaDurationThreshold());
        }
    }
    @Test void rejectsInvalidThresholdEvenWhenFilteringIsDisabled() {
        for (String value : new String[]{"-1ms", "not-a-duration"}) {
            var failure = assertThrows(Exception.class, () -> configured(
                    java.util.Map.of("causeway.observation.jpa-duration-threshold", value)));
            assertTrue(failure.toString().contains("causeway.observation"), failure.toString());
        }
    }
    private AnnotationConfigApplicationContext configured(java.util.Map<String, Object> properties) {
        var context = new AnnotationConfigApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(
                new org.springframework.core.env.MapPropertySource("test", properties));
        context.register(CausewayObservationAutoConfiguration.class);
        try { context.refresh(); return context; }
        catch (RuntimeException | Error ex) { context.close(); throw ex; }
    }
    private AnnotationConfigApplicationContext context(boolean active, Class<?>... userConfigs) {
        var context = new AnnotationConfigApplicationContext();
        if (active) context.getEnvironment().setActiveProfiles("observation");
        if (userConfigs.length > 0) context.register(userConfigs);
        context.register(CausewayObservationAutoConfiguration.class);
        try { context.refresh(); return context; }
        catch (RuntimeException | Error ex) { context.close(); throw ex; }
    }
    @Configuration(proxyBeanMethods = false) static class OneRegistry {
        @Bean ObservationRegistry applicationRegistry() {
            var registry = ObservationRegistry.create();
            registry.observationConfig().observationHandler(new ObservationHandler<Observation.Context>() {
                public boolean supportsContext(Observation.Context context) { return true; }
            });
            return registry;
        }
    }
    @Configuration(proxyBeanMethods = false) static class PrimaryRegistry {
        @Bean @Primary ObservationRegistry primaryRegistry() { return ObservationRegistry.create(); }
    }
    @Configuration(proxyBeanMethods = false) static class OtherRegistry {
        @Bean ObservationRegistry otherRegistry() { return ObservationRegistry.create(); }
    }
}
