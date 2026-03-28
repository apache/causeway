package org.apache.causeway.core.config.observation;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import io.micrometer.observation.ObservationRegistry;

/**
 * Makes observation an opt-in choice based on profile 'observation' being active.
 * 
 * <p>see Spring's org.springframework.boot.micrometer.observation.autoconfigure.ObservationAutoConfiguration
 */
@AutoConfiguration
@ConditionalOnClass(ObservationRegistry.class) 
public class CausewayObservationAutoConfiguration {
	
	@Profile("!observation")
	@Bean 
	ObservationRegistry noopObservationRegistry() {
		return ObservationRegistry.NOOP;
	}
	
	@Profile("observation")
	@Bean
	@ConditionalOnMissingBean
	ObservationRegistry observationRegistry() {
		return ObservationRegistry.create();
	}

}
