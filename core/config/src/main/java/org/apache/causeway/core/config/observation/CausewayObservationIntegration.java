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

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.observation.ObservationClosure;
import org.apache.causeway.core.config.observation.CausewayObservationAutoConfiguration.DiscardedSpanExportingPredicate;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.ObservationRegistry;

/**
 * Framework entry point for creating observations in the configured telemetry
 * context. Consumers use this integration instead of choosing registries or
 * constructing tracers themselves.
 *
 * @param observationRegistry registry selected by
 *        {@link CausewayObservationAutoConfiguration}: a single/primary
 *        application registry or the fallback when observation is enabled,
 *        otherwise {@link ObservationRegistry#NOOP}. Its handlers bridge
 *        observations to the chosen tracing pipeline and parent context; the
 *        registry is not itself an SDK or exporter. A null registry is treated
 *        as no-op for manually constructed instances.
 * @param policy metadata and duration settings, independent of activation and
 *        sampling. Duration filtering is opt-in and requires an exporter that
 *        consumes the Spring-managed discard predicate.
 */
public record CausewayObservationIntegration(
        ObservationRegistry observationRegistry, CausewayObservationPolicy policy) {

    public CausewayObservationIntegration(final ObservationRegistry observationRegistry) {
        this(observationRegistry, CausewayObservationPolicy.DEFAULT);
    }

    /** Compatibility constructor for callers explicitly selecting duration filtering. */
    public CausewayObservationIntegration(final ObservationRegistry observationRegistry,
            final boolean durationFilteringEnabled) {
        this(observationRegistry, new CausewayObservationPolicy(false, false,
                durationFilteringEnabled, CausewayObservationPolicy.DEFAULT.jpaDurationThreshold()));
    }

    public boolean durationFilteringEnabled() {
        return policy.durationFilteringEnabled();
    }

    public CausewayObservationIntegration(
            final Optional<ObservationRegistry> observationRegistryOpt) {
        this(observationRegistryOpt.orElse(ObservationRegistry.NOOP));
    }

    public CausewayObservationIntegration {
        java.util.Objects.requireNonNull(policy, "policy");
        observationRegistry = observationRegistry!=null
            ? observationRegistry
            : ObservationRegistry.NOOP;
    }

    public boolean isNoop() {
        return observationRegistry.isNoop();
    }

    public Observation createNotStarted(final Class<?> bean, final String name) {
        return Observation.createNotStarted(name, Context::new, observationRegistry)
                .lowCardinalityKeyValue("causeway.bean", bean.getSimpleName());
    }

    // -- OBSERVATION PROVIDER

    @FunctionalInterface
    public interface ObservationProvider {
        Observation get(String name);
    }

    public ObservationProvider provider(final Class<?> bean) {
        return name->createNotStarted(bean, name);
    }

    public ObservationProvider provider(final Class<?> bean, final Function<Observation, Observation> customizer) {
        return name->customizer.apply(createNotStarted(bean, name));
    }

    public static UnaryOperator<Observation> withModuleName(final String moduleName){
        return obs->StringUtils.hasText(moduleName)
                ? obs.lowCardinalityKeyValue(moduleName(moduleName))
                : obs;
    }

    /**
     * Applies the optional duration policy at the instrumentation boundary
     * (currently JPA operations, using the configured threshold). When disabled,
     * the original observation is returned unchanged.
     * The flag does not affect observations created without this method.
     */
    public Observation withTimeThreshold(final Observation observation, final Duration threshold) {
        return durationFilteringEnabled()
                ? new ObservationWithTimeThreshold(observation, threshold)
                : observation;
    }

    // -- COMMON KEY-VALUES

    public static KeyValue currentThreadId() {
        var ct = Thread.currentThread();
        return KeyValue.of("causeway.threadId", "%d [%s]".formatted(ct.getId(), ct.getName()));
    }

    public static KeyValue moduleName(final String moduleName) {
        return KeyValue.of("causeway.module",
            moduleName.startsWith("causeway.")
                || moduleName.startsWith("causeway-")
            ? moduleName.substring(9)
            : moduleName);
    }
    
    /**
     * UTC ISO format
     */
    public static KeyValue interactionClock(final Instant instant) {
        return KeyValue.of("causeway.interaction.clock", DateTimeFormatter.ISO_INSTANT.format(instant));
    }
    public static KeyValue interactionDepth(final int value) {
        return KeyValue.of("causeway.interaction.depth", "" + value);
    }
    public static KeyValue interactionLanguage(final Locale locale) {
        return KeyValue.of("causeway.interaction.language", locale.toString());
    }
//    public static KeyValue interactionMethod(@NonNull MethodFacade method) {
//		return KeyValue.of("causeway.interaction.method", method.getName());
//	}
    public static KeyValue interactionNumberFormat(final Locale locale) {
        return KeyValue.of("causeway.interaction.numberformat", locale.toString());
    }
    public static KeyValue interactionTimeFormat(final Locale locale) {
        return KeyValue.of("causeway.interaction.timeformat", locale.toString());
    }
    public static KeyValue interactionTimezone(final ZoneId zone) {
        return KeyValue.of("causeway.interaction.timezone", zone.getId());
    }
    public static KeyValue userName(final @Nullable String value) {
        return KeyValue.of("causeway.user.name", _Strings.nullToEmpty(value));
    }
    public static KeyValue userImpersonating(final boolean value) {
        return KeyValue.of("causeway.user.impersonating", "" + value);
    }
    public static KeyValue userMultiTenancyToken(final @Nullable String value) {
        return KeyValue.of("causeway.user.multiTenancyToken", _Strings.nullToEmpty(value));
    }

    // -- SPAN EXPORT DISCARDING SUPPORT

    /**
     * Marks an observation for suppression by the Spring-registered
     * {@link DiscardedSpanExportingPredicate}. Exporters that do not consume that
     * predicate, such as the Java agent's exporter, do not honor this marker.
     */
    public static void discard(@Nullable final Observation obs) {
    	ObservationClosure.discard(obs);
    }

    /**
     * Measures the observation from start to stop and marks successful work
     * below the threshold for discard. Failed work remains eligible for export
     * regardless of duration. This is an export filter, so it does not avoid the
     * cost of recording the observation or its children.
     *
     * <p>Fluent methods and {@code start()} return this wrapper, not the delegate:
     * callers such as ObservationClosure retain the returned observation and
     * must still reach this wrapper's {@code stop()} to apply the policy.
     */
    public record ObservationWithTimeThreshold(Observation delegate, Duration threshold, Timer timer) implements Observation {
        static class Timer {
            final LongSupplier clock;
            Timer() { this(System::nanoTime); }
            Timer(final LongSupplier clock) { this.clock = clock; }
            long startNanos;
            void start() { this.startNanos = clock.getAsLong(); }
            long elapsedNanos() { return clock.getAsLong() - startNanos; }
        }
        public ObservationWithTimeThreshold(final Observation delegate, final Duration threshold) {
            this(delegate, threshold, new Timer());
        }
        @Override public Observation contextualName(@Nullable final String contextualName) {
            delegate.contextualName(contextualName);
            return this;
        }
        @Override public Observation parentObservation(@Nullable final Observation parentObservation) {
            delegate.parentObservation(parentObservation);
            return this;
        }
        @Override public Observation lowCardinalityKeyValue(final KeyValue keyValue) {
            delegate.lowCardinalityKeyValue(keyValue);
            return this;
        }
        @Override public Observation lowCardinalityKeyValue(final String key, final String value) {
            delegate.lowCardinalityKeyValue(key, value);
            return this;
        }
        @Override public Observation highCardinalityKeyValue(final KeyValue keyValue) {
            delegate.highCardinalityKeyValue(keyValue);
            return this;
        }
        @Override public Observation highCardinalityKeyValue(final String key, final String value) {
            delegate.highCardinalityKeyValue(key, value);
            return this;
        }
        @Override public Observation observationConvention(final ObservationConvention<?> observationConvention) {
            delegate.observationConvention(observationConvention);
            return this;
        }
        @Override public Observation error(final Throwable error) {
            delegate.error(error);
            return this;
        }
        @Override public Observation event(final Event event) {
            delegate.event(event);
            return this;
        }
        @Override public Observation start() {
            timer.start();
            delegate.start();
            return this;
        }
        @Override public Context getContext() {
            return delegate.getContext();
        }
        @Override public void stop() {
            if(delegate.getContext().getError() == null && Duration.ofNanos(timer.elapsedNanos()).compareTo(threshold) < 0) {
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
