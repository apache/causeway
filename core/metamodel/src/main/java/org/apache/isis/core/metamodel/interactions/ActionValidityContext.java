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

import static org.apache.isis.core.metamodel.spec.ManagedObject.unwrapSingle;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.wrapper.events.ActionInvocationEvent;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.InteractionContextType;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;

/**
 * See {@link InteractionContext} for overview; analogous to
 * {@link ActionInvocationEvent}.
 */
public class ActionValidityContext
extends ValidityContext 
implements ActionInteractionContext {

    @Getter(onMethod = @__(@Override)) private final ObjectAction objectAction;
    @Getter private final Can<ManagedObject> args;

    public ActionValidityContext(
            final ManagedObject targetAdapter,
            final ObjectAction objectAction,
            final Identifier id,
            final Can<ManagedObject> args,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        super(InteractionContextType.ACTION_INVOKE, targetAdapter, id, interactionInitiatedBy);
        this.objectAction = objectAction;
        this.args = args;
    }

    @Override
    public ActionInvocationEvent createInteractionEvent() {
        return new ActionInvocationEvent(
                unwrapSingle(getTarget()), getIdentifier(), ManagedObject.unwrapMultipleAsArray(getArgs().toList()));
    }

}
