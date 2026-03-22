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
package org.apache.causeway.commons.internal.observation;

import java.util.Optional;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

import lombok.Data;
import lombok.experimental.Accessors;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.Observation.Scope;
import io.micrometer.observation.ObservationRegistry;

/**
 * Holder of {@link ObservationRegistry} which comes as a dependency of <i>spring-context</i>.
 *
 * @apiNote each Causeway module can have its own, using qualifiers and bean factory methods, e.g.:
 * <pre>
 * @Bean("causeway-metamodel")
 * public CausewayObservationInternal causewayObservationInternal(
 *   Optional<ObservationRegistry> observationRegistryOpt) {
 *   return new CausewayObservationInternal(observationRegistryOpt, "causeway-metamodel");
 * }
 *  </pre>
 */
public record CausewayObservationInternal(
        ObservationRegistry observationRegistry,
        String module) {

    public CausewayObservationInternal(
            final Optional<ObservationRegistry> observationRegistryOpt,
            final String module) {
        this(observationRegistryOpt.orElse(ObservationRegistry.NOOP), module);
    }

    public CausewayObservationInternal {
        observationRegistry = observationRegistry!=null
                ? observationRegistry
                : ObservationRegistry.NOOP;
        module = StringUtils.hasText(module) ? module : "unknown_module";
    }

    public boolean isNoop() {
        return observationRegistry.isNoop();
    }

    public Observation createNotStarted(final Class<?> bean, final String name) {
        return Observation.createNotStarted(name, observationRegistry)
                .lowCardinalityKeyValue("module", module)
                .lowCardinalityKeyValue("bean", bean.getSimpleName());
    }

    @FunctionalInterface
    public interface ObservationProvider {
        Observation get(String name);
    }

    public ObservationProvider provider(final Class<?> bean) {
        return name->createNotStarted(bean, name);
    }

    /**
     * Helps if start and stop of an {@link Observation} happen in different code locations.
     */
    @Data @Accessors(fluent = true)
    public static final class ObservationClosure implements AutoCloseable {

        private Observation observation;
        private Scope scope;

        public ObservationClosure startAndOpenScope(final Observation observation) {
            if(observation==null) return this;
            this.observation = observation.start();
            this.scope = observation.openScope();
            return this;
        }

        @Override
        public void close() {
            if(observation==null) return;
            if(scope!=null) {
                this.scope.close();
                this.scope = null;
            }
            observation.stop();
        }

        public void onError(final Exception ex) {
            if(observation==null) return;
            observation.error(ex);
        }

        public ObservationClosure tag(final String key, @Nullable final Supplier<Object> valueSupplier) {
            if(observation==null || valueSupplier == null) return this;
            try {
                observation.highCardinalityKeyValue(key, "" + valueSupplier.get());
            } catch (Exception e) {
                observation.highCardinalityKeyValue(key, "EXCEPTION: " + e.getMessage());
            }
            return this;
        }

    }

    public static KeyValue currentThreadId() {
        var ct = Thread.currentThread();
        return KeyValue.of("threadId", "%d [%s]".formatted(ct.getId(), ct.getName()));

    }

}
