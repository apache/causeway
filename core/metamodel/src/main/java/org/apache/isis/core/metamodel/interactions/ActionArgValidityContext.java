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
import org.apache.isis.applib.services.wrapper.events.ActionArgumentEvent;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.InteractionContextType;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects.UnwrapUtil;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;

/**
 * See {@link InteractionContext} for overview; analogous to
 * {@link ActionArgumentEvent}.
 */
public class ActionArgValidityContext 
extends ValidityContext 
implements ProposedHolder, ActionInteractionContext {

    @Getter(onMethod = @__(@Override)) private final ObjectAction objectAction;
    @Getter(onMethod = @__(@Override)) private final ManagedObject proposed; 
    @Getter private final Can<ManagedObject> args;
    @Getter private final int position;

    public ActionArgValidityContext(
            final InteractionHead head,
            final ObjectAction objectAction,
            final Identifier id,
            final Can<ManagedObject> args,
            final int position,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        super(InteractionContextType.ACTION_PROPOSED_ARGUMENT, 
                head, 
                id, 
                interactionInitiatedBy);
        this.objectAction = objectAction;

        this.args = args;
        this.position = position;
        this.proposed = args.getElseFail(position);
    }

    @Override
    public ActionArgumentEvent createInteractionEvent() {
        return new ActionArgumentEvent(
                UnwrapUtil.single(getTarget()), 
                getIdentifier(), 
                UnwrapUtil.multipleAsArray(getArgs().toList()), 
                getPosition());
    }

}
