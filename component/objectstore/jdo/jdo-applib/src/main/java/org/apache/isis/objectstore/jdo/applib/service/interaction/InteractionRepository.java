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

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.interaction.Interaction;
import org.apache.isis.applib.services.interaction.InteractionContext;


public class InteractionRepository extends AbstractFactoryAndRepository {

    @Programmatic
    public InteractionJdo findByTransactionId(final UUID transactionId) {
        persistCurrentInteractionIfRequired();
        return firstMatch(
                new QueryDefault<InteractionJdo>(InteractionJdo.class, 
                        "findByTransactionId", 
                        "transactionId", transactionId.toString()));
    }

    private void persistCurrentInteractionIfRequired() {
        if(interactionContext != null && interactionService != null) {
            Interaction interaction = interactionContext.getInteraction();
            final InteractionJdo interactionJdo = interactionService.asPersistableInteractionJdo(interaction);
            persistIfNotAlready(interactionJdo);
        }
    }

    
    @Programmatic
    public List<InteractionJdo> findCurrent() {
        persistCurrentInteractionIfRequired();
        return allMatches(
                new QueryDefault<InteractionJdo>(InteractionJdo.class, "findCurrent"));
    }
    
    @Programmatic
    public List<InteractionJdo> findCompleted() {
        persistCurrentInteractionIfRequired();
        return allMatches(
                new QueryDefault<InteractionJdo>(InteractionJdo.class, "findCompleted"));
    }

    // //////////////////////////////////////

    
    @javax.inject.Inject
    private InteractionServiceJdo interactionService;
    
    @javax.inject.Inject
    private InteractionContext interactionContext;

}
