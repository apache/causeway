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
import org.apache.isis.applib.services.interaction.spi.InteractionService;

public class InteractionServiceJdo extends AbstractService implements InteractionService {

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
    public void startTransaction(final Interaction interaction, final UUID transactionId) {
        if(interaction instanceof InteractionJdo) {
            // should be the case, since this service created the object in the #create() method
            final InteractionJdo interactionJdo = (InteractionJdo) interaction;
            final UUID currentTransactionId = interactionJdo.getTransactionId();
            if(currentTransactionId != null && !currentTransactionId.equals(transactionId)) {
                // the logic in IsisTransaction means that any subsequent transactions within a given interaction
                // should reuse the xactnId of the first transaction created within that interaction.
                throw new IllegalStateException("Attempting to set a different transactionId on interaction");
            }
            interactionJdo.setTransactionId(transactionId);
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
     * Not API, factored out from {@link InteractionServiceJdoRepository}.
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
