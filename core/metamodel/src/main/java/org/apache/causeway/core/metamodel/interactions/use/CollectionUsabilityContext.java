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
package org.apache.causeway.core.metamodel.interactions.use;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.wrapper.events.CollectionUsabilityEvent;
import org.apache.causeway.core.metamodel.consent.InteractionContextType;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.interactions.InteractionContext;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.interactions.RenderPolicy;
import org.apache.causeway.core.metamodel.interactions.vis.CollectionVisibilityContext;

/**
 * See {@link InteractionContext} for overview; analogous to
 * {@link CollectionUsabilityEvent}.
 */
public record CollectionUsabilityContext(
    InteractionContextType interactionType,
    InteractionHead head,
    Identifier identifier,
    InteractionInitiatedBy initiatedBy,
    Where where,
    RenderPolicy renderPolicy)
implements UsabilityContext {

    public CollectionUsabilityContext(
            final InteractionHead head,
            final Identifier identifier,
            final InteractionInitiatedBy initiatedBy,
            final Where where,
            final RenderPolicy renderPolicy) {
        this(InteractionContextType.COLLECTION_USABLE,
                head, identifier, initiatedBy, where, renderPolicy);
    }

    @Override
    public CollectionUsabilityEvent createInteractionEvent() {
        return new CollectionUsabilityEvent(target().getPojo(), identifier());
    }

    @Override
    public CollectionVisibilityContext asVisibilityContext() {
        return new CollectionVisibilityContext(head(), identifier(),
                initiatedBy(), where(), renderPolicy());
    }
}
