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

package org.apache.isis.core.metamodel.interactions;

import static org.apache.isis.core.metamodel.adapter.ObjectAdapter.Util.unwrap;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.wrapper.events.CollectionRemoveFromEvent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionContextType;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;

/**
 * See {@link InteractionContext} for overview; analogous to
 * {@link CollectionRemoveFromEvent}.
 */
public class CollectionRemoveFromContext extends ValidityContext<CollectionRemoveFromEvent> implements ProposedHolder{

    private final ObjectAdapter proposed;

    public CollectionRemoveFromContext(
            final ObjectAdapter targetAdapter,
            final Identifier identifier,
            final ObjectAdapter proposed,
            final InteractionInitiatedBy interactionInitiatedBy) {
        super(InteractionContextType.COLLECTION_REMOVE_FROM, targetAdapter, identifier, interactionInitiatedBy);

        this.proposed = proposed;
    }

    @Override
    public ObjectAdapter getProposed() {
        return proposed;
    }

    @Override
    public CollectionRemoveFromEvent createInteractionEvent() {
        return new CollectionRemoveFromEvent(unwrap(getTarget()), getIdentifier(), unwrap(getProposed()));
    }

}
