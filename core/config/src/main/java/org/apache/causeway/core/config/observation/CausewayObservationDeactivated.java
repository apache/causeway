package org.apache.causeway.core.config.observation;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile("!observation")
public class CausewayObservationDeactivated {
	
	//TODO disable the entire metric stack, that is, make observation an opt-in choice based on profile 'observation' being active  
	//watch out for Spring's org.springframework.boot.micrometer.observation.autoconfigure.ObservationRegistryPostProcessor

}
