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

import java.util.Objects;
import java.util.function.UnaryOperator;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

/**
 * Internal entry point for Causeway framework observations.
 *
 * <p>Framework components use this integration rather than depending on an
 * OpenTelemetry implementation or performing optional registry lookup.</p>
 *
 * @since 2.2
 */
public final class CausewayObservationIntegration {

    private final ObservationRegistry observationRegistry;

    public CausewayObservationIntegration(final ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry != null
                ? observationRegistry
                : ObservationRegistry.NOOP;
    }

    public ObservationRegistry observationRegistry() {
        return observationRegistry;
    }

    public boolean isNoop() {
        return observationRegistry.isNoop();
    }

    public Observation createNotStarted(final Class<?> beanType, final String name) {
        Objects.requireNonNull(beanType, "beanType");
        Objects.requireNonNull(name, "name");
        return Observation.createNotStarted(name, observationRegistry)
                .lowCardinalityKeyValue("causeway.bean", beanType.getSimpleName());
    }

    public ObservationProvider provider(final Class<?> beanType) {
        return name -> createNotStarted(beanType, name);
    }

    public ObservationProvider provider(
            final Class<?> beanType,
            final UnaryOperator<Observation> customizer) {
        Objects.requireNonNull(customizer, "customizer");
        return name -> customizer.apply(createNotStarted(beanType, name));
    }

    public static UnaryOperator<Observation> withModuleName(final String moduleName) {
        Objects.requireNonNull(moduleName, "moduleName");
        final String normalizedModuleName = moduleName.startsWith("causeway.")
                || moduleName.startsWith("causeway-")
                        ? moduleName.substring(9)
                        : moduleName;
        return observation -> observation.lowCardinalityKeyValue(
                "causeway.module", normalizedModuleName);
    }

    @FunctionalInterface
    public interface ObservationProvider {
        Observation get(String name);
    }
}
