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
package org.apache.causeway.core.metamodel.services.init;

import java.io.File;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.events.metamodel.MetamodelEvent;
import org.apache.causeway.applib.services.eventbus.EventBusService;
import org.apache.causeway.applib.util.schema.ChangesDtoUtils;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.applib.util.schema.InteractionDtoUtils;
import org.apache.causeway.applib.util.schema.InteractionsDtoUtils;
import org.apache.causeway.commons.internal.concurrent._ConcurrentContext;
import org.apache.causeway.commons.internal.concurrent._ConcurrentTaskList;
import org.apache.causeway.commons.internal.observation.CausewayObservationIntegration;
import org.apache.causeway.commons.internal.observation.CausewayObservationIntegration.ObservationProvider;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public record MetamodelInitializer(
        EventBusService eventBusService,
        Provider<SpecificationLoader> specificationLoaderProvider,
        ObservationProvider observationProvider) {

    @Inject
    public MetamodelInitializer(
            final EventBusService eventBusService,
            final Provider<SpecificationLoader> specificationLoaderProvider,
            @Qualifier("causeway-metamodel")
            final CausewayObservationIntegration observationIntegration) {
        this(eventBusService, specificationLoaderProvider, observationIntegration.provider(MetamodelInitializer.class));
    }

    @EventListener
    public void init(final ContextRefreshedEvent event) {
        log.info("Initialising Causeway System");
        log.info("working directory: {}", new File(".").getAbsolutePath());

        observationProvider.get("Initialising Causeway System").observe(() -> {
            observationProvider.get("Notify BEFORE_METAMODEL_LOADING Listeners").observe(() -> {
                eventBusService.post(MetamodelEvent.BEFORE_METAMODEL_LOADING);
            });

            observationProvider.get("Initialising Causeway Metamodel").observe(() -> {
                initMetamodel(specificationLoaderProvider.get());
            });

            observationProvider.get("Notify AFTER_METAMODEL_LOADED Listeners").observe(() -> {
                eventBusService.post(MetamodelEvent.AFTER_METAMODEL_LOADED);
            });
        });
    }

    private void initMetamodel(final SpecificationLoader specificationLoader) {

        var taskList = _ConcurrentTaskList.named("CausewayInteractionFactoryDefault Init")
                .addRunnable("SpecificationLoader::createMetaModel", specificationLoader::createMetaModel)
                .addRunnable("ChangesDtoUtils::init", ChangesDtoUtils::init)
                .addRunnable("InteractionDtoUtils::init", InteractionDtoUtils::init)
                .addRunnable("InteractionsDtoUtils::init", InteractionsDtoUtils::init)
                .addRunnable("CommandDtoUtils::init", CommandDtoUtils::init)
                ;

        taskList.submit(_ConcurrentContext.forkJoin());
        taskList.await();

        { // log any validation failures, experimental code however, not sure how to best propagate failures
            var validationResult = specificationLoader.getOrAssessValidationResult();
            if(validationResult.getNumberOfFailures()==0) {
                log.info("Validation PASSED");
            } else {
                log.error("### Validation FAILED, failure count: {}", validationResult.getNumberOfFailures());
                validationResult.forEach(failure->{
                    log.error("# " + failure.message());
                });
                //throw _Exceptions.unrecoverable("Validation FAILED");
            }
        }
    }

}
