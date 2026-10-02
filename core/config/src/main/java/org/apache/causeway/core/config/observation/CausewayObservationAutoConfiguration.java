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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
 * Connects Causeway instrumentation to Micrometer when the {@code observation}
 * Spring profile is active. The registry's handlers determine what observations
 * produce (for example, tracing spans); this configuration does not create an SDK
 * or exporter.
 *
 * <p>Normally Boot configures the tracing handlers and export pipeline. An
 * application can instead supply a registry bridged to the OpenTelemetry Java
 * agent's context, with competing Boot tracing auto-configuration excluded.
 * Both arrangements use the same Causeway integration below.
 *
 * <p>With the profile inactive, only Causeway's integration is disabled. We do
 * not register a no-op registry bean: that could interfere with the application's
 * own observations or make registry injection ambiguous.
 */
@AutoConfiguration
@EnableConfigurationProperties(CausewayObservationPolicy.class)
@ConditionalOnClass(ObservationRegistry.class)
@Import({
	DiscardedSpanExportingPredicate.class
})
public class CausewayObservationAutoConfiguration {

	/**
	 * Lets the Spring-managed tracing pipeline drop spans marked by Causeway's
	 * duration filter or an explicit discard. The marker alone does not suppress
	 * export: the pipeline must consume this predicate. In particular, an
	 * agent-owned exporter does not discover Spring beans, so duration filtering
	 * is explicitly disabled in the documented agent configuration.
	 */
	public record DiscardedSpanExportingPredicate() implements SpanExportingPredicate {
		@Override
		public boolean isExportable(final FinishedSpan span) {
			return !span.getTags().containsKey(ObservationClosure.DISCARD_KEY.getKey());
		}
	}

	/**
	 * Supplies an active-profile fallback only when no registry bean is already
	 * available. As with Boot's fallback, creating a registry does not itself
	 * enable export; observation handlers must be registered by the tracing setup.
	 */
	@Profile("observation")
	@Bean
	@ConditionalOnMissingBean
	public ObservationRegistry observationRegistry() {
		return ObservationRegistry.create();
	}

    /**
     * Always supplies the integration used by framework consumers, including
     * when Causeway observations are disabled.
     *
     * <p>The provider defers registry resolution until after checking the
     * profile. When active, Spring selects the single or primary registry;
     * ambiguous candidates deliberately fail configuration rather than selecting
     * an arbitrary telemetry pipeline. When inactive, application registries are
     * not resolved or modified.
     */
	@Bean
    public CausewayObservationIntegration causewayObservationIntegration(
            final ObjectProvider<ObservationRegistry> registries,
            final Environment environment,
            final CausewayObservationPolicy policy) {
        // Bind policy even with the profile inactive, so invalid configuration
        // fails at startup. This does not resolve or modify application registries.
        return new CausewayObservationIntegration(environment.acceptsProfiles(Profiles.of("observation"))
                ? registries.getIfAvailable(() -> ObservationRegistry.NOOP)
                : ObservationRegistry.NOOP,
                policy);
    }

}
