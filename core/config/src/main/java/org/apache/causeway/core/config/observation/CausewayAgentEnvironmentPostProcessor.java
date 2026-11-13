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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.Profiles;
import org.springframework.util.ClassUtils;

/** Selects agent trace ownership before Boot evaluates its auto-configurations. */
public class CausewayAgentEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final List<String> TRACE_AUTO_CONFIGURATIONS = List.of(
            "org.springframework.boot.opentelemetry.autoconfigure.OpenTelemetrySdkAutoConfiguration",
            "org.springframework.boot.micrometer.tracing.opentelemetry.autoconfigure.OpenTelemetryTracingAutoConfiguration",
            "org.springframework.boot.micrometer.tracing.opentelemetry.autoconfigure.otlp.OtlpTracingAutoConfiguration",
            "org.springframework.boot.micrometer.tracing.autoconfigure.MicrometerTracingAutoConfiguration",
            "org.springframework.boot.webmvc.autoconfigure.WebMvcObservationAutoConfiguration");

    @Override
    public int getOrder() {
        // Active profiles and application YAML must already have been loaded.
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }

    @Override
    public void postProcessEnvironment(final ConfigurableEnvironment environment,
            final SpringApplication application) {
        if (!environment.acceptsProfiles(Profiles.of("agent"))) {
            return;
        }
        if (environment.acceptsProfiles(Profiles.of("observation"))
                && !ClassUtils.isPresent("io.micrometer.tracing.otel.bridge.OtelTracer", application.getClassLoader())) {
            throw new IllegalStateException("The observation,agent profiles require "
                    + "io.micrometer:micrometer-tracing-bridge-otel. Add the BOM-managed dependency "
                    + "and attach the OpenTelemetry Java agent to the application JVM.");
        }
        var exclusions = new LinkedHashSet<>(Binder.get(environment)
                .bind("spring.autoconfigure.exclude", String[].class)
                .map(List::of).orElseGet(List::of));
        exclusions.addAll(TRACE_AUTO_CONFIGURATIONS);
        // Ownership is a profile contract, so these values override command-line
        // defaults too. Existing unrelated exclusions are retained above.
        // Avoid mapping OTEL_METRICS_EXPORTER=none onto Micrometer metrics.
        environment.getPropertySources().addFirst(new MapPropertySource("causewayAgentTracing", Map.of(
                "spring.autoconfigure.exclude", String.join(",", exclusions),
                "management.opentelemetry.map-environment-variables", "false",
                "management.tracing.export.enabled", "false")));
    }
}
