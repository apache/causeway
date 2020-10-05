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

package org.apache.isis.core.runtimeservices.publish;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.IsisInteractionScope;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.annotation.PublishingChangeKind;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.applib.services.metrics.MetricsService;
import org.apache.isis.applib.services.publish.PublishedObjects;
import org.apache.isis.applib.services.publish.PublisherService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.facets.object.publishedobject.PublishedObjectFacet;
import org.apache.isis.core.metamodel.services.publishing.PublisherDispatchService;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.persistence.changetracking.HasEnlistedForPublishing;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Wrapper around {@link PublisherService}.  Is a no-op if there is no injected service.
 */
@Service
@Named("isisRuntimeServices.PublisherDispatchServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@IsisInteractionScope
@RequiredArgsConstructor
//@Log4j2
public class PublisherDispatchServiceDefault implements PublisherDispatchService {

    @Inject final List<PublisherService> publisherServices;
    @Inject final ClockService clockService;
    @Inject final UserService userService;
    @Inject final Provider<HasEnlistedForPublishing> changedObjectsProvider;
    @Inject final Provider<InteractionContext> interactionContextProvider;
    @Inject final Provider<MetricsService> metricsServiceProvider;
    
    @Override
    public void publishObjects() {

        if(isSuppressed()) {
            return;
        }

        // take a copy of enlisted adapters ... the JDO implementation of the PublishingService
        // creates further entities which would be enlisted; 
        // taking copy of the map avoids ConcurrentModificationException

        val changeKindByPublishedAdapter =
                _Maps.filterKeys(
                        changedObjectsProvider.get().getChangeKindByEnlistedAdapter(),
                        this::isPublished,
                        HashMap::new);

        if(changeKindByPublishedAdapter.isEmpty()) {
            return;
        }

        val publishedObjects = newPublishedObjects(
                        metricsServiceProvider.get().numberObjectsLoaded(), 
                        changedObjectsProvider.get().numberObjectPropertiesModified(),
                        changeKindByPublishedAdapter);

        if(publishedObjects == null) {
            return;
        }
        for (val publisherService : publisherServices) {
            publisherService.publish(publishedObjects);
        }
    }

    private PublishedObjects newPublishedObjects(
            final int numberLoaded,
            final int numberObjectPropertiesModified,
            final Map<ManagedObject, PublishingChangeKind> changeKindByPublishedAdapter) {

        val interaction = interactionContextProvider.get().getInteraction();
        val uniqueId = interaction.getUniqueId();

        if(uniqueId == null) {
            // there was no interaction... eg fixture scripts
            return null;
        }

        final int nextEventSequence = interaction.next(Interaction.Sequence.INTERACTION.id());
        val userName = userService.getUser().getName();
        val timestamp = clockService.nowAsJavaSqlTimestamp();

        return new PublishedObjectsDefault(
                    uniqueId, nextEventSequence,
                    userName, timestamp,
                    numberLoaded, numberObjectPropertiesModified, changeKindByPublishedAdapter);
    }


    @Override
    public void publishAction(final Interaction.Execution<?,?> execution) {
        publishToPublisherServices(execution);
    }


    @Override
    public void publishProperty(final Interaction.Execution<?,?> execution) {
        publishToPublisherServices(execution);
    }


    private void publishToPublisherServices(final Interaction.Execution<?,?> execution) {

        if(isSuppressed()) {
            return;
        }

        for (final PublisherService publisherService : publisherServices) {
            publisherService.publish(execution);
        }
    }

    private final LongAdder suppressionRequestCounter = new LongAdder();
    
    private boolean isSuppressed() {
        return publisherServices == null 
                || publisherServices.isEmpty() 
                || suppressionRequestCounter.intValue() > 0;
    }
    
    @Override
    public <T> T withPublishingSuppressed(final Block<T> block) {
        try {
            suppressionRequestCounter.increment();
            return block.exec();
        } finally {
            suppressionRequestCounter.decrement();
        }
    }

    private boolean isPublished(ManagedObject objectAdapter) {
        val publishedObjectFacet = objectAdapter.getSpecification().getFacet(PublishedObjectFacet.class);
        return publishedObjectFacet != null;
    }


}
