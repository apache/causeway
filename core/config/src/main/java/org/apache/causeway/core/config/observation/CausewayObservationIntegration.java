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
 * Holder of {@link ObservationRegistry} which comes as a dependency of <i>spring-context</i>.
 */
public record CausewayObservationIntegration(
        ObservationRegistry observationRegistry, boolean durationFilteringEnabled) {

    public CausewayObservationIntegration(final ObservationRegistry observationRegistry) {
        this(observationRegistry, true);
    }

    public CausewayObservationIntegration(
            final Optional<ObservationRegistry> observationRegistryOpt) {
        this(observationRegistryOpt.orElse(ObservationRegistry.NOOP));
    }

    public CausewayObservationIntegration {
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

    public Observation withTimeThreshold(final Observation observation, final Duration threshold) {
        return durationFilteringEnabled
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
     * Denies span export, in collaboration with a Spring registered {@link DiscardedSpanExportingPredicate}.
     */
    public static void discard(@Nullable final Observation obs) {
    	ObservationClosure.discard(obs);
    }

    //TODO perhaps threshold should not be hardcoded at call site; what we really want is to report Observations
    // that are way off a base-line; this would require some profiling to establish base-lines
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
            if(delegate.getContext().getError() == null && timer.elapsedNanos() < threshold.toNanos()) {
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
