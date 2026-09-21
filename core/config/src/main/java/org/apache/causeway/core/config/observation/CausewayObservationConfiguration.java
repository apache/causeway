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

import java.util.Collections;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import io.micrometer.tracing.otel.bridge.OtelBaggageManager;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.api.GlobalOpenTelemetry;

import org.apache.causeway.OrgApacheCauseway;

/**
 * Boot 2.7-specific observation wiring for Causeway framework instrumentation.
 *
 * <p>The {@code observation} Spring profile opts into a registry backed by the
 * OpenTelemetry Java agent's {@link GlobalOpenTelemetry} context. Without the
 * profile, framework observations use {@link ObservationRegistry#NOOP}.</p>
 *
 * @since 2.2
 */
@Configuration(proxyBeanMethods = false)
public class CausewayObservationConfiguration {

    public static final String REGISTRY_BEAN_NAME = "causewayObservationRegistry";

    @Bean
    public CausewayObservationIntegration causewayObservationIntegration(
            @Qualifier(REGISTRY_BEAN_NAME) final ObservationRegistry observationRegistry) {
        return new CausewayObservationIntegration(observationRegistry);
    }

    @Bean(name = REGISTRY_BEAN_NAME)
    @Profile("!observation")
    public ObservationRegistry inactiveCausewayObservationRegistry() {
        return ObservationRegistry.NOOP;
    }

    @Bean(name = REGISTRY_BEAN_NAME)
    @Profile("observation")
    public ObservationRegistry activeCausewayObservationRegistry() {
        final OtelCurrentTraceContext currentTraceContext = new OtelCurrentTraceContext();
        final OtelBaggageManager baggageManager = new OtelBaggageManager(
                currentTraceContext,
                Collections.emptyList(),
                Collections.emptyList());
        final Tracer tracer = new OtelTracer(
                GlobalOpenTelemetry.getTracer(OrgApacheCauseway.class.getPackageName()),
                currentTraceContext,
                event -> { },
                baggageManager);

        final ObservationRegistry observationRegistry = ObservationRegistry.create();
        observationRegistry.observationConfig()
                .observationHandler(new DefaultTracingObservationHandler(tracer));
        return observationRegistry;
    }
}
