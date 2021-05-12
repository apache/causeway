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

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.wrapper.events.ParseValueEvent;
import org.apache.isis.core.metamodel.consent.InteractionContextType;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects.UnwrapUtil;

/**
 * See {@link InteractionContext} for overview; analogous to
 * {@link ParseValueEvent}.
 */
public class ParseValueContext
extends ValidityContext
implements ProposedHolder {

    private final ManagedObject proposed;

    public ParseValueContext(
            final InteractionHead head,
            final Identifier identifier,
            final ManagedObject proposed,
            final InteractionInitiatedBy interactionInitiatedBy) {

        super(InteractionContextType.PARSE_VALUE, head, identifier, interactionInitiatedBy);
        this.proposed = proposed;
    }

    @Override
    public ManagedObject getProposed() {
        return proposed;
    }

    @Override
    public ParseValueEvent createInteractionEvent() {
        final String proposedPojo = (String) UnwrapUtil.single(getProposed());
        return new ParseValueEvent(UnwrapUtil.single(getTarget()), getIdentifier(), proposedPojo);
    }

}
