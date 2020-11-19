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
package org.apache.isis.core.runtime.persistence.changetracking;

import java.util.HashMap;
import java.util.Map;

import org.apache.isis.applib.annotation.EntityChangeKind;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.publish.ChangingEntities;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.facets.object.publishedobject.PublishedObjectFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName = "of")
class ChangingEntitiesFactory {
    
    private final ClockService clockService;
    private final UserService userService;

    public ChangingEntities createChangingEntities(
            final EntityChangeTrackerDefault entityChangeTracker) {
        
        // take a copy of enlisted adapters ... the JDO implementation of the PublishingService
        // creates further entities which would be enlisted; 
        // taking copy of the map avoids ConcurrentModificationException

        val changeKindByPublishedAdapter =
                _Maps.filterKeys(
                        entityChangeTracker.getChangeKindByEnlistedAdapter(),
                        this::isPublishingEnabled,
                        HashMap::new);

        if(changeKindByPublishedAdapter.isEmpty()) {
            return null;
        }

        val changingEntities = newChangingEntities(
                        entityChangeTracker.currentInteraction(),
                        entityChangeTracker.numberEntitiesLoaded(), 
                        entityChangeTracker.numberAuditedEntityPropertiesModified(),
                        changeKindByPublishedAdapter);
        
        return changingEntities;
    }
    
    // -- HELPER
    
    private ChangingEntities newChangingEntities(
            final Interaction interaction,
            final int numberEntitiesLoaded,
            final int numberEntityPropertiesModified,
            final Map<ManagedObject, EntityChangeKind> changeKindByPublishedAdapter) {

        val uniqueId = interaction.getUniqueId();
        val userName = userService.getUser().getName();
        val timestamp = clockService.nowAsJavaSqlTimestamp();
        final int nextEventSequence = interaction.next(Interaction.Sequence.INTERACTION.id());

        return new SimpleChangingEntities(
                    uniqueId, nextEventSequence,
                    userName, timestamp,
                    numberEntitiesLoaded, 
                    numberEntityPropertiesModified, 
                    changeKindByPublishedAdapter);
    }

    private boolean isPublishingEnabled(ManagedObject objectAdapter) {
        return PublishedObjectFacet.isChangingEntitiesDispatchingEnabled(objectAdapter.getSpecification());
    }
    
}
