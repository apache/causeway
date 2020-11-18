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

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.annotation.PublishingChangeKind;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.publish.ChangingEntities;
import org.apache.isis.applib.services.publish.ChangingEntitiesListener;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.facets.object.publishedobject.PublishedObjectFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.persistence.changetracking.ChangingEntitiesDispatcher;
import org.apache.isis.core.runtime.persistence.changetracking.HasEnlistedChangingEntities;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Wrapper around {@link org.apache.isis.applib.services.audit.EntityAuditListener}.
 */
@Service
@Named("isisRuntime.ChangingEntitiesDispatcher")
@Order(OrderPrecedence.EARLY)
@Primary
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
//@Log4j2
public class ChangingEntitiesDispatcherDefault implements ChangingEntitiesDispatcher {
    
    private final List<ChangingEntitiesListener> changingEntitiesListenersNullable;
    private final ClockService clockService;
    private final UserService userService;
    
    private Can<ChangingEntitiesListener> changingEntitiesListeners;
    
    @PostConstruct
    public void init() {
        changingEntitiesListeners = Can.ofCollection(changingEntitiesListenersNullable);
    }

    public void dispatchChangingEntities(HasEnlistedChangingEntities hasEnlistedChangingEntities) {

        if(!canDispatch()) {
            return;
        }
        
        hasEnlistedChangingEntities.preparePublishing();

        // take a copy of enlisted adapters ... the JDO implementation of the PublishingService
        // creates further entities which would be enlisted; 
        // taking copy of the map avoids ConcurrentModificationException

        val changeKindByPublishedAdapter =
                _Maps.filterKeys(
                        hasEnlistedChangingEntities.getChangeKindByEnlistedAdapter(),
                        this::isPublishingEnabled,
                        HashMap::new);

        if(changeKindByPublishedAdapter.isEmpty()) {
            return;
        }

        val changingEntities = newChangingEntities(
                        hasEnlistedChangingEntities.currentInteraction(),
                        hasEnlistedChangingEntities.numberObjectsLoaded(), 
                        hasEnlistedChangingEntities.numberObjectPropertiesModified(),
                        changeKindByPublishedAdapter);

        if(changingEntities == null) {
            return;
        }
        for (val changingEntitiesListener : changingEntitiesListeners) {
            changingEntitiesListener.onEntitiesChanging(changingEntities);
        }
    }
    
    // -- HELPER
    
    private boolean canDispatch() {
        return changingEntitiesListeners.isNotEmpty();
    }

    private ChangingEntities newChangingEntities(
            final Interaction interaction,
            final int numberLoaded,
            final int numberObjectPropertiesModified,
            final Map<ManagedObject, PublishingChangeKind> changeKindByPublishedAdapter) {

        val uniqueId = interaction.getUniqueId();

        if(uniqueId == null) {
            // there was no interaction... eg fixture scripts
            return null;
        }

        final int nextEventSequence = interaction.next(Interaction.Sequence.INTERACTION.id());
        val userName = userService.getUser().getName();
        val timestamp = clockService.nowAsJavaSqlTimestamp();

        return new SimpleChangingEntities(
                    uniqueId, nextEventSequence,
                    userName, timestamp,
                    numberLoaded, numberObjectPropertiesModified, changeKindByPublishedAdapter);
    }

    private boolean isPublishingEnabled(ManagedObject objectAdapter) {
        val publishedObjectFacet = objectAdapter.getSpecification().getFacet(PublishedObjectFacet.class);
        return publishedObjectFacet != null;
    }


}
