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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import org.apache.causeway.commons.internal.observation.ObservationClosure;
import org.apache.causeway.core.config.observation.CausewayObservationAutoConfiguration.DiscardedSpanExportingPredicate;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.exporter.FinishedSpan;
import io.micrometer.tracing.exporter.SpanExportingPredicate;

/**
 * Makes observation an opt-in choice based on Spring Profile 'observation' being active.
 *
 * <p>see Spring's org.springframework.boot.micrometer.observation.autoconfigure.ObservationAutoConfiguration
 */
@AutoConfiguration
@ConditionalOnClass(ObservationRegistry.class)
@Import({
	DiscardedSpanExportingPredicate.class
})
public class CausewayObservationAutoConfiguration {

	/**
	 * Does not allow discarded spans to be exported. Register with Spring (before auto configuration is running).
	 */
	public record DiscardedSpanExportingPredicate() implements SpanExportingPredicate {
		@Override
		public boolean isExportable(final FinishedSpan span) {
			return !span.getTags().containsKey(ObservationClosure.DISCARD_KEY.getKey());
		}
	}

	/**
	 * Same as in org.springframework.boot.micrometer.observation.autoconfigure.ObservationAutoConfiguration,
	 * that is, acts as a fallback.
	 */
	@Profile("observation")
	@Bean
	@ConditionalOnMissingBean
	public ObservationRegistry observationRegistry() {
		return ObservationRegistry.create();
	}

	@Bean
    public CausewayObservationIntegration causewayObservationIntegration(
            final ObjectProvider<ObservationRegistry> registries,
            final Environment environment) {
        return new CausewayObservationIntegration(environment.acceptsProfiles(Profiles.of("observation"))
                ? registries.getIfAvailable(() -> ObservationRegistry.NOOP)
                : ObservationRegistry.NOOP,
                environment.getProperty("causeway.observation.duration-filtering-enabled", Boolean.class, true));
    }

}
