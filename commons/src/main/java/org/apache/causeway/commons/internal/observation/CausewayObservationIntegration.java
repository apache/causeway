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

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.experimental.Accessors;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.Observation.Scope;
import io.micrometer.observation.ObservationConvention;
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
public record CausewayObservationIntegration(
        ObservationRegistry observationRegistry,
        String module) {

    public CausewayObservationIntegration(
            final Optional<ObservationRegistry> observationRegistryOpt,
            final String module) {
        this(observationRegistryOpt.orElse(ObservationRegistry.NOOP), module);
    }

    public CausewayObservationIntegration {
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
                .lowCardinalityKeyValue("causeway.module", module)
                .lowCardinalityKeyValue("causeway.bean", bean.getSimpleName());
    }

    @FunctionalInterface
    public interface ObservationProvider {
        Observation get(String name);
    }

    public ObservationProvider provider(final Class<?> bean) {
        return name->createNotStarted(bean, name);
    }

    public ObservationProvider provider(final Class<?> bean, final UnaryOperator<Observation> customizer) {
        return name->customizer.apply(createNotStarted(bean, name));
    }

    /**
     * Helps if start and stop of an {@link Observation} happen in different code locations.
     */
    @Getter @Accessors(fluent = true)
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
            // scope lifecycle terminates before exception handling
            if(scope!=null) {
                this.scope.close();
                this.scope = null;
            }
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

    /**
     * TODO Has no effect. Cannot filter based on name, when {@link Observation} is already started.
     * With Micrometer Observation API, it seems there is no way to discard already started Observations.
     * Perhaps we can prevent those from being sent over the wire later.
     */
    public static void discard(@Nullable final Observation obs) {
        if(obs == null)
            return;
        obs.contextualName("denied");
    }

    public record ObservationWithTimeThreshold(Observation delegate, Duration threshold, Timer timer) implements Observation {
        private static class Timer {
            long startNanos;
            void start() { this.startNanos = System.nanoTime(); }
            long elapsedNanos() { return System.nanoTime() - startNanos; }
        }
        public ObservationWithTimeThreshold(final Observation delegate, final Duration threshold) {
            this(delegate, threshold, new Timer());
        }
        @Override public Observation contextualName(@Nullable final String contextualName) {
            return delegate.contextualName(contextualName);
        }
        @Override public Observation parentObservation(@Nullable final Observation parentObservation) {
            return delegate.parentObservation(parentObservation);
        }
        @Override public Observation lowCardinalityKeyValue(final KeyValue keyValue) {
            return delegate.lowCardinalityKeyValue(keyValue);
        }
        @Override public Observation lowCardinalityKeyValue(final String key, final String value) {
            return delegate.lowCardinalityKeyValue(key, value);
        }
        @Override public Observation highCardinalityKeyValue(final KeyValue keyValue) {
            return delegate.highCardinalityKeyValue(keyValue);
        }
        @Override public Observation highCardinalityKeyValue(final String key, final String value) {
            return delegate.highCardinalityKeyValue(key, value);
        }
        @Override public Observation observationConvention(final ObservationConvention<?> observationConvention) {
            return delegate.observationConvention(observationConvention);
        }
        @Override public Observation error(final Throwable error) {
            return delegate.error(error);
        }
        @Override public Observation event(final Event event) {
            return delegate.event(event);
        }
        @Override public Observation start() {
            timer.start();
            return delegate.start();
        }
        @Override public Context getContext() {
            return delegate.getContext();
        }
        @Override public void stop() {
            if(timer.elapsedNanos() < threshold.toNanos()) {
                discard(delegate);
            }
            delegate.stop();
        }
        @Override public Scope openScope() {
            return delegate.openScope();
        }
        @Override public String toString() {
            return delegate.toString();
        }
    }

}
