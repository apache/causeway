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

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import org.apache.causeway.commons.internal.observation.ObservationClosure;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration.ObservationWithTimeThreshold;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

class ObservationWithTimeThresholdTest {
    @Test void compositionRetainsWrapperAndShortSuccessIsDiscarded() {
        var clock = new AtomicLong();
        var observation = observation(clock);
        assertSame(observation, observation.contextualName("context"));
        assertSame(observation, observation.lowCardinalityKeyValue("kind", "test"));
        assertSame(observation, observation.highCardinalityKeyValue("id", "test"));
        assertSame(observation, observation.parentObservation(null));
        try (var closure = new ObservationClosure().startAndOpenScope(observation)) {
            assertSame(observation, closure.observation());
            clock.set(9);
        }
        assertNotNull(observation.getContext().getLowCardinalityKeyValue("causeway.discard"));
    }
    @Test void atAndAboveThresholdAreRetained() {
        for (long duration : new long[]{10, 11}) {
            var clock = new AtomicLong();
            var observation = observation(clock);
            observation.start();
            clock.set(duration);
            observation.stop();
            assertNull(observation.getContext().getLowCardinalityKeyValue("causeway.discard"));
        }
    }
    @Test void shortFailureIsRetained() {
        var observation = observation(new AtomicLong());
        var failure = new AssertionError("work");
        assertSame(failure, assertThrows(AssertionError.class, () -> observation.observe(() -> { throw failure; })));
        assertSame(failure, observation.getContext().getError());
        assertNull(observation.getContext().getLowCardinalityKeyValue("causeway.discard"));
    }
    @Test void failureRetainsWrapperAndIsNotDiscarded() {
        var observation = observation(new AtomicLong());
        var failure = new AssertionError("work");
        assertSame(observation, observation.start());
        assertSame(observation, observation.error(failure));
        observation.stop();
        assertSame(failure, observation.getContext().getError());
        assertNull(observation.getContext().getLowCardinalityKeyValue("causeway.discard"));
    }
    @Test void providerPatternAndZeroThreshold() {
        var registry = registry();
        var integration = new CausewayObservationIntegration(registry);
        var provider = integration.provider(getClass(),
                CausewayObservationIntegration.withModuleName("causeway.jpa")
                .andThen(obs -> integration.withTimeThreshold(obs, Duration.ofDays(1))));
        var observation = provider.get("Persist");
        observation.observe(() -> {});
        assertNotNull(observation.getContext().getLowCardinalityKeyValue("causeway.discard"));
        var disabled = new CausewayObservationIntegration(registry);
        var delegate = disabled.createNotStarted(getClass(), "Persist");
        assertSame(delegate, disabled.withTimeThreshold(delegate, Duration.ZERO));
        delegate.observe(() -> {});
        assertNull(delegate.getContext().getLowCardinalityKeyValue("causeway.discard"));
    }
    @Test void zeroThresholdRetainsInstantSuccess() {
        var observation = new ObservationWithTimeThreshold(Observation.createNotStarted("test", registry()),
                Duration.ZERO, new ObservationWithTimeThreshold.Timer(() -> 0L));
        observation.observe(() -> {});
        assertNull(observation.getContext().getLowCardinalityKeyValue("causeway.discard"));
    }
    @Test void largeConfiguredThresholdDoesNotOverflowDuringCleanup() {
        var observation = new ObservationWithTimeThreshold(Observation.createNotStarted("test", registry()),
                Duration.ofDays(1000000), new ObservationWithTimeThreshold.Timer(() -> 0L));
        assertDoesNotThrow(() -> observation.observe(() -> {}));
        assertNotNull(observation.getContext().getLowCardinalityKeyValue("causeway.discard"));
    }
    @Test void defaultPolicyDoesNotWrapOrDiscard() {
        var integration = new CausewayObservationIntegration(registry());
        var observation = integration.createNotStarted(getClass(), "test");
        assertSame(observation, integration.withTimeThreshold(observation, integration.policy().jpaDurationThreshold()));
        observation.observe(() -> {});
        assertNull(observation.getContext().getLowCardinalityKeyValue("causeway.discard"));
    }
    private ObservationWithTimeThreshold observation(AtomicLong clock) {
        return new ObservationWithTimeThreshold(Observation.createNotStarted("test", registry()),
                Duration.ofNanos(10), new ObservationWithTimeThreshold.Timer(clock::get));
    }
    private ObservationRegistry registry() {
        var registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(new ObservationHandler<Observation.Context>() {
            public boolean supportsContext(Observation.Context context) { return true; }
        });
        return registry;
    }
}
