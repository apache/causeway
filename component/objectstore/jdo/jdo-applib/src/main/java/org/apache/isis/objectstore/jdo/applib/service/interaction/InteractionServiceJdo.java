/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.objectstore.jdo.applib.service.interaction;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.annotation.Bookmarkable;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.interaction.Interaction;
import org.apache.isis.applib.services.interaction.InteractionContext;
import org.apache.isis.applib.services.interaction.spi.InteractionFactory;

@Named("Interactions")
public class InteractionServiceJdo extends AbstractService implements InteractionFactory, InteractionRepository {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(InteractionServiceJdo.class);

    @Programmatic
    @Override
    public Interaction create() {
        InteractionJdo interaction = newTransientInstance(InteractionJdo.class);
        return interaction;
    }

    @Programmatic
    @Override
    public void startTransaction(Interaction interaction, UUID transactionId) {
        if(interaction instanceof InteractionJdo) {
            // should be the case, since this service created the object in the #create() method
            final InteractionJdo interactionJdo = (InteractionJdo) interaction;
            interactionJdo.setTransactionId(transactionId.toString());
        }
    }

    @Programmatic
    @Override
    public void complete(final Interaction interaction) {
        if(interaction.getActionIdentifier() == null) {
            // discard, no action occurred
            return;
        }
        if(interaction instanceof InteractionJdo) {
            // should be the case, since this service created the object in the #create() method
            final InteractionJdo interactionJdo = (InteractionJdo) interaction;
            
            interactionJdo.setCompletedAt(Clock.getTimeAsJavaSqlTimestamp());
            persistIfNotAlready(interaction);
        }
    }

    @Override
    public InteractionJdo findByGuid(UUID guid) {
        persistCurrentInteractionIfRequired();
        return firstMatch(
                new QueryDefault<InteractionJdo>(InteractionJdo.class, 
                        "findByGuid", 
                        "guid", guid.toString()));
    }

    private void persistCurrentInteractionIfRequired() {
        if(interactionContext != null) {
            // expect to be the case, given that this service has been configured
            Interaction interaction = interactionContext.getInteraction();
            if(interaction instanceof InteractionJdo) {
                // should be the case, since this service created the object in the #start() method
                persistIfNotAlready(interaction);
            }
        }
    }

    @Bookmarkable
    @Override
    public List<InteractionJdo> currentInteractions() {
        persistCurrentInteractionIfRequired();
        return allMatches(
                new QueryDefault<InteractionJdo>(InteractionJdo.class, 
                        "findCurrent"));
    }
    
    @Bookmarkable
    @Override
    public List<InteractionJdo> completedInteractions() {
        persistCurrentInteractionIfRequired();
        return allMatches(
                new QueryDefault<InteractionJdo>(InteractionJdo.class, 
                        "findCompleted"));
    }
    
    @Inject
    private InteractionContext interactionContext;

}
