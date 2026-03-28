package org.apache.causeway.core.config.observation;

import java.util.Optional;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

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
	
	@Profile("!observation")
	@Bean 
	ObservationRegistry noopObservationRegistry() {
		return ObservationRegistry.NOOP;
	}
	
	/**
	 * Same as in org.springframework.boot.micrometer.observation.autoconfigure.ObservationAutoConfiguration,
	 * that is, acts as a fallback. 
	 */
	@Profile("observation")
	@Bean
	@ConditionalOnMissingBean
	ObservationRegistry observationRegistry() {
		return ObservationRegistry.create();
	}
	
	@Bean
    CausewayObservationIntegration causewayObservationIntegration(
            final Optional<ObservationRegistry> observationRegistryOpt) {
        return new CausewayObservationIntegration(observationRegistryOpt);
    }

}
