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

package org.apache.isis.runtime.services.publish;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
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
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.object.publishedobject.PublishedObjectFacet;
import org.apache.isis.metamodel.services.publishing.PublishingServiceInternal;
import org.apache.isis.runtime.system.transaction.ChangedObjectsServiceInternal;

/**
 * Wrapper around {@link PublisherService}.  Is a no-op if there is no injected service.
 */
@DomainService(
        nature = NatureOfService.DOMAIN
        )
@RequestScoped
public class PublishingServiceInternalDefault implements PublishingServiceInternal {


    @Override
    public void publishObjects() {

        if(suppress) {
            return;
        }

        // take a copy of enlisted adapters ... the JDO implementation of the PublishingService
        // creates further entities which would be enlisted; taking copy of the map avoids ConcurrentModificationException

        final Map<ObjectAdapter, PublishingChangeKind> changeKindByEnlistedAdapter = _Maps.newHashMap();
        changeKindByEnlistedAdapter.putAll(changedObjectsServiceInternal.getChangeKindByEnlistedAdapter());

        final Map<ObjectAdapter, PublishingChangeKind> changeKindByPublishedAdapter =
                _Maps.filterKeys(
                        changeKindByEnlistedAdapter,
                        isPublished(),
                        HashMap::new);

        if(changeKindByPublishedAdapter.isEmpty()) {
            return;
        }

        final int numberLoaded = metricsService.numberObjectsLoaded();
        final int numberObjectPropertiesModified = changedObjectsServiceInternal.numberObjectPropertiesModified();
        final PublishedObjects publishedObjects = newPublishedObjects(numberLoaded, numberObjectPropertiesModified,
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

        if(suppress) {
            return;
        }

        publishToPublisherServices(execution);
    }


    @Override
    public void publishProperty(final Interaction.Execution<?,?> execution) {

        if(suppress) {
            return;
        }

        publishToPublisherServices(execution);
    }


    private void publishToPublisherServices(final Interaction.Execution<?,?> execution) {

        if(publisherServices == null || publisherServices.isEmpty()) {
            return;
        }

        for (final PublisherService publisherService : publisherServices) {
            publisherService.publish(execution);
        }
    }


    boolean suppress;

    @Override
    public <T> T withPublishingSuppressed(final Block<T> block) {
        try {
            suppress = true;
            return block.exec();
        } finally {
            suppress = false;
        }
    }


    private static Predicate<ObjectAdapter> isPublished() {
        return (final ObjectAdapter objectAdapter) -> {
                final PublishedObjectFacet publishedObjectFacet =
                        objectAdapter.getSpecification().getFacet(PublishedObjectFacet.class);
                return publishedObjectFacet != null;
        };
    }

    // -- injected services
    @Inject List<PublisherService> publisherServices;
    @Inject ChangedObjectsServiceInternal changedObjectsServiceInternal;
    @Inject CommandContext commandContext;
    @Inject InteractionContext interactionContext;
    @Inject ClockService clockService;
    @Inject UserService userService;
    @Inject MetricsService metricsService;



}
