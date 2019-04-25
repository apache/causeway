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
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.events.ActionArgumentEvent;
import org.apache.isis.applib.events.ActionArgumentUsabilityEvent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionContextType;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import static org.apache.isis.core.metamodel.adapter.ObjectAdapter.Util.unwrap;

/**
 * See {@link InteractionContext} for overview; analogous to
 * {@link ActionArgumentEvent}.
 */
public class ActionArgUsabilityContext extends UsabilityContext<ActionArgumentUsabilityEvent> implements ActionInteractionContext {

    private final ObjectAction objectAction;
    private final ObjectAdapter[] args;
    private final int position;

    public ActionArgUsabilityContext(
            final ObjectAdapter targetAdapter,
            final ObjectAction objectAction,
            final Identifier id,
            final ObjectAdapter[] args,
            final int position,
            final InteractionInitiatedBy interactionInitiatedBy) {
        super(InteractionContextType.ACTION_PARAMETER_USABLE, targetAdapter, id, interactionInitiatedBy, Where.OBJECT_FORMS);
        this.objectAction = objectAction;

        this.args = args;
        this.position = position;
    }

    @Override
    public ObjectAction getObjectAction() {
        return objectAction;
    }

    public ObjectAdapter[] getArgs() {
        return args;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public ActionArgumentUsabilityEvent createInteractionEvent() {
        return new ActionArgumentUsabilityEvent(unwrap(getTarget()), getIdentifier(), unwrap(getArgs()), getPosition());
    }

}
