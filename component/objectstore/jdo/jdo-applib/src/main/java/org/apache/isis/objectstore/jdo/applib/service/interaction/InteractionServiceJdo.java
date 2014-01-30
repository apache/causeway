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

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.services.interaction.Interaction;
import org.apache.isis.applib.services.interaction.spi.InteractionFactory;

public class InteractionServiceJdo extends AbstractService implements InteractionFactory {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(InteractionServiceJdo.class);

    /**
     * Creates an {@link InteractionJdo}, initializing its 
     * {@link Interaction#setNature(Interaction.Nature) nature} to be
     * {@link Interaction.Nature#RENDERING rendering}.
     */
    @Programmatic
    @Override
    public Interaction create() {
        InteractionJdo interaction = newTransientInstance(InteractionJdo.class);
        interaction.setNature(Interaction.Nature.RENDERING);
        return interaction;
    }

    @Programmatic
    @Override
    public void startTransaction(Interaction interaction, UUID transactionId) {
        if(interaction instanceof InteractionJdo) {
            // should be the case, since this service created the object in the #create() method
            InteractionJdo interactionJdo = (InteractionJdo) interaction;
            if(interactionJdo.getTransactionId() == null) {
                interactionJdo.setTransactionId(transactionId);
            }
        }
    }

    @Programmatic
    @Override
    public void complete(final Interaction interaction) {
        InteractionJdo interactionJdo = asPersistableInteractionJdo(interaction);
        if(interactionJdo == null) {
            return;
        }
            
        interactionJdo.setCompletedAt(Clock.getTimeAsJavaSqlTimestamp());
        
        persistIfNotAlready(interactionJdo);
    }

    /**
     * Not API, factored out from {@link InteractionRepository}.
     */
    InteractionJdo asPersistableInteractionJdo(final Interaction interaction) {
        if(interaction.getNature() == Interaction.Nature.ACTION_INVOCATION) {
            if(interaction instanceof InteractionJdo) {
                // should be the case, since this service created the object in the #create() method
                return (InteractionJdo)interaction;
            }
        }
        // else, don't care
        return null;
    }
}
