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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.LongAdder;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.annotation.PublishingChangeKind;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.applib.services.metrics.MetricsService;
import org.apache.isis.applib.services.publish.PublishedObjects;
import org.apache.isis.applib.services.publish.PublisherService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.publishedobject.PublishedObjectFacet;
import org.apache.isis.core.metamodel.services.publishing.PublisherDispatchService;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.persistence.transaction.ChangedObjectsService;

import lombok.val;

/**
 * Wrapper around {@link PublisherService}.  Is a no-op if there is no injected service.
 */
@Service
@Named("isisRuntimeServices.PublisherDispatchServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@RequestScoped
@Qualifier("Default")
public class PublisherDispatchServiceDefault implements PublisherDispatchService {

    @Inject private List<PublisherService> publisherServices;
    @Inject private ChangedObjectsService changedObjectsServiceInternal;
    @Inject private CommandContext commandContext;
    @Inject private InteractionContext interactionContext;
    @Inject private ClockService clockService;
    @Inject private UserService userService;
    @Inject private MetricsService metricsService;
    
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
                        changedObjectsServiceInternal.getChangeKindByEnlistedAdapter(),
                        this::isPublished,
                        HashMap::new);

        if(changeKindByPublishedAdapter.isEmpty()) {
            return;
        }

        val publishedObjects = newPublishedObjects(
                        metricsService.numberObjectsLoaded(), 
                        changedObjectsServiceInternal.numberObjectPropertiesModified(), 
                        changeKindByPublishedAdapter);

        for (PublisherService publisherService : publisherServices) {
            publisherService.publish(publishedObjects);
        }
    }

    private PublishedObjects newPublishedObjects(
            final int numberLoaded,
            final int numberObjectPropertiesModified,
            final Map<ObjectAdapter, PublishingChangeKind> changeKindByPublishedAdapter) {

        final Command command = commandContext.getCommand();
        final UUID transactionUuid = command.getUniqueId();

        final String userName = userService.getUser().getName();
        final Timestamp timestamp = clockService.nowAsJavaSqlTimestamp();

        final Interaction interaction = interactionContext.getInteraction();

        final int nextEventSequence = interaction.next(Interaction.Sequence.INTERACTION.id());

        return new PublishedObjectsDefault(transactionUuid, nextEventSequence, userName, timestamp, numberLoaded, numberObjectPropertiesModified, changeKindByPublishedAdapter);
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
