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

package org.apache.isis.metamodel.interactions;

import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.wrapper.events.ActionArgumentEvent;
import org.apache.isis.metamodel.consent.InteractionContextType;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.feature.ObjectAction;

import static org.apache.isis.metamodel.spec.ManagedObject.unwrapSingle;

import lombok.Getter;

/**
 * See {@link InteractionContext} for overview; analogous to
 * {@link ActionArgumentEvent}.
 */
public class ActionArgValidityContext extends ValidityContext<ActionArgumentEvent> implements ProposedHolder, ActionInteractionContext {

    @Getter(onMethod = @__(@Override)) private final ObjectAction objectAction;
    @Getter(onMethod = @__(@Override)) private final ManagedObject proposed;
    @Getter private final List<ManagedObject> args;
    @Getter private final int position;

    public ActionArgValidityContext(
            final ManagedObject targetAdapter,
            final ObjectAction objectAction,
            final Identifier id,
            final List<ManagedObject> args,
            final int position,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        super(InteractionContextType.ACTION_PROPOSED_ARGUMENT, targetAdapter, id, interactionInitiatedBy);
        this.objectAction = objectAction;

        this.args = args;
        this.position = position;
        this.proposed = args.get(position);
    }

    @Override
    public ActionArgumentEvent createInteractionEvent() {
        return new ActionArgumentEvent(
                unwrapSingle(getTarget()), getIdentifier(), ManagedObject.unwrapMultipleAsArray(getArgs()), getPosition());
    }

}
