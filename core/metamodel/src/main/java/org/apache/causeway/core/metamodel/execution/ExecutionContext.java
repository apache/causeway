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
package org.apache.causeway.core.metamodel.execution;

import jakarta.inject.Provider;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.metrics.MetricsService;
import org.apache.causeway.applib.services.queryresultscache.QueryResultsCache;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration.ObservationProvider;
import org.apache.causeway.core.metamodel.facets.DomainEventHelper;
import org.apache.causeway.core.metamodel.services.deadlock.DeadlockRecognizer;
import org.apache.causeway.core.metamodel.services.events.MetamodelEventService;
import org.apache.causeway.core.metamodel.services.ixn.InteractionDtoFactory;
import org.apache.causeway.core.metamodel.services.publishing.CommandPublisher;

import io.micrometer.observation.Observation;

@Service
public record ExecutionContext(
        ClockService clockService,
        Provider<MetricsService> metricsServiceProvider,
        Provider<CommandPublisher> commandPublisherProvider,
        DeadlockRecognizer deadlockRecognizer,
		InteractionIdGenerator idGenerator,
		CausewayObservationIntegration observationIntegration,
		
		Provider<InteractionDtoFactory> interactionDtoFactoryProvider,
		MetamodelEventService metamodelEventService,
		QueryResultsCache queryResultsCache) {

    public CommandPublisher commandPublisher() {
        return commandPublisherProvider.get();
    }
    
    public DomainEventHelper domainEventHelper() {
    	return new DomainEventHelper(metamodelEventService);
    }
    
    public InteractionDtoFactory interactionDtoFactory() {
    	return interactionDtoFactoryProvider.get();
    }
    
    public MetricsService metricsService() {
    	return metricsServiceProvider.get();
    }
    
    public ObservationProvider observationProvider(Class<?> participant, String moduleName) {
    	return observationIntegration!=null
    		? observationIntegration.provider(participant,
                        CausewayObservationIntegration.withModuleName(moduleName))
			: __->Observation.NOOP;
	}

}