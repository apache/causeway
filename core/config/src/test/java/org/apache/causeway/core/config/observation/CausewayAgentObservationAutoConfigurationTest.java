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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;

class CausewayAgentObservationAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(CausewayObservationAutoConfiguration.class)
            .withConfiguration(AutoConfigurations.of(CausewayAgentObservationAutoConfiguration.class));

    @Test void bridgeCreatesOneRegistryAndTracer() {
        runner.withPropertyValues("spring.profiles.active=observation,agent").run(context -> {
            assertThat(context).hasSingleBean(Tracer.class).hasSingleBean(ObservationRegistry.class);
            assertThat(context.getBean(CausewayObservationIntegration.class).observationRegistry())
                    .isSameAs(context.getBean(ObservationRegistry.class));
            assertThat(context.getBean(ObservationRegistry.class).isNoop()).isFalse();
        });
    }

    @Test void customTracerIsSharedWithDefaultRegistryAndClassifier() {
        var tracer = mock(Tracer.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
        runner.withPropertyValues("spring.profiles.active=observation,agent")
                .withBean(Tracer.class, () -> tracer).run(context -> {
                    assertThat(context).hasSingleBean(Tracer.class).hasSingleBean(ObservationRegistry.class);
                    assertThat(context.getBean(Tracer.class)).isSameAs(tracer);
                    context.getBean(CausewayTraceClassifier.class)
                            .classifyCurrentSpan(CausewayTraceClassifier.ExecutionMode.FOREGROUND);
                    verify(tracer.currentSpan()).tag("causeway.execution.mode", "foreground");
                    context.getBean(CausewayObservationIntegration.class)
                            .createNotStarted(getClass(), "custom-tracer").observe(() -> {});
                    verify(tracer).nextSpan();
                });
    }

    @Test void customRegistryIsPreserved() {
        var custom = ObservationRegistry.create();
        runner.withPropertyValues("spring.profiles.active=observation,agent")
                .withBean(ObservationRegistry.class, () -> custom).run(context -> {
                    assertThat(context).hasSingleBean(ObservationRegistry.class);
                    assertThat(context.getBean(CausewayObservationIntegration.class).observationRegistry()).isSameAs(custom);
                    assertThat(custom.isNoop()).isTrue(); // no handler installed into application registry
                });
    }

    @Test void inactiveObservationDoesNotCreateAgentBridge() {
        runner.withPropertyValues("spring.profiles.active=agent").run(context -> {
            assertThat(context).doesNotHaveBean(Tracer.class).doesNotHaveBean(ObservationRegistry.class);
            assertThat(context.getBean(CausewayObservationIntegration.class).isNoop()).isTrue();
        });
    }

    @Test void bootModeStartsWithoutOptionalBridge() {
        runner.withClassLoader(new FilteredClassLoader("io.micrometer.tracing.otel", "io.opentelemetry"))
                .withPropertyValues("spring.profiles.active=observation").run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(ObservationRegistry.class);
                    assertThat(context).doesNotHaveBean(Tracer.class);
                });
    }

    @Test void ownershipExclusionsPreserveYamlListAndMetrics() {
        var environment = new StandardEnvironment();
        environment.setActiveProfiles("observation", "agent");
        environment.getPropertySources().addFirst(new MapPropertySource("app", Map.of(
                "spring.autoconfigure.exclude[0]", "example.CustomAutoConfiguration",
                "management.otlp.metrics.export.enabled", "true",
                "management.tracing.export.enabled", "true")));
        var processor = new CausewayAgentEnvironmentPostProcessor();
        processor.postProcessEnvironment(environment, new SpringApplication());
        processor.postProcessEnvironment(environment, new SpringApplication()); // repeat is harmless
        assertThat(environment.getProperty("spring.autoconfigure.exclude"))
                .contains("example.CustomAutoConfiguration", "OpenTelemetrySdkAutoConfiguration", "WebMvcObservationAutoConfiguration");
        assertThat(environment.getProperty("management.otlp.metrics.export.enabled")).isEqualTo("true");
        assertThat(environment.getProperty("management.opentelemetry.map-environment-variables")).isEqualTo("false");
        assertThat(environment.getProperty("management.tracing.export.enabled")).isEqualTo("false");
    }

    @Test void missingAgentDependencyExplainsRemedy() throws Exception {
        var environment = new StandardEnvironment();
        environment.setActiveProfiles("observation", "agent");
        try (var loader = new FilteredClassLoader("io.micrometer.tracing.otel")) {
            var application = new SpringApplication(new org.springframework.core.io.DefaultResourceLoader(loader));
            assertThatThrownBy(() -> new CausewayAgentEnvironmentPostProcessor()
                    .postProcessEnvironment(environment, application))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("micrometer-tracing-bridge-otel");
        }
    }

    @Test void bootEnvironmentRemainsUnchanged() {
        var environment = new StandardEnvironment();
        environment.setActiveProfiles("observation");
        new CausewayAgentEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication());
        assertThat(environment.getPropertySources().contains("causewayAgentTracing")).isFalse();
    }
}
