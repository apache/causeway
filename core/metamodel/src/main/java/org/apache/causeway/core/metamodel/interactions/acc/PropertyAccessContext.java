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
package org.apache.causeway.core.metamodel.interactions.acc;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.wrapper.events.PropertyAccessEvent;
import org.apache.causeway.core.metamodel.consent.InteractionContextType;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.interactions.InteractionContext;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;

/**
 * See {@link InteractionContext} for overview; analogous to
 * {@link PropertyAccessEvent}.
 */
public record PropertyAccessContext(
    InteractionContextType interactionType,
    Identifier identifier,
    InteractionHead head,
    InteractionInitiatedBy initiatedBy,
    Where where,
    /**
     * The current value for a property.
     */
    ManagedObject value)
implements AccessContext {

    public PropertyAccessContext(
            final InteractionHead head,
            final Identifier id,
            final ManagedObject value,
            final InteractionInitiatedBy initiatedBy) {
        this(InteractionContextType.PROPERTY_READ, id, head, initiatedBy, Where.NOT_SPECIFIED,
            value);
    }

    @Override
    public PropertyAccessEvent createInteractionEvent() {
        return new PropertyAccessEvent(
                MmUnwrapUtils.single(target()),
                identifier(),
                MmUnwrapUtils.single(value()));
    }

}
