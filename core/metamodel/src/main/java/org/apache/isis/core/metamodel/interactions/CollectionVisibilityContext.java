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

import static org.apache.isis.core.metamodel.adapter.ObjectAdapter.Util.unwrapPojo;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.wrapper.events.CollectionVisibilityEvent;
import org.apache.isis.core.metamodel.consent.InteractionContextType;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;

/**
 * See {@link InteractionContext} for overview; analogous to
 * {@link CollectionVisibilityEvent}.
 */
public class CollectionVisibilityContext extends VisibilityContext<CollectionVisibilityEvent> {

    public CollectionVisibilityContext(
            final ManagedObject target,
            final Identifier identifierAdapter,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {
        super(InteractionContextType.COLLECTION_VISIBLE, target, identifierAdapter, interactionInitiatedBy, where);
    }

    @Override
    public CollectionVisibilityEvent createInteractionEvent() {
        return new CollectionVisibilityEvent(unwrapPojo(getTarget()), getIdentifier());
    }

}
