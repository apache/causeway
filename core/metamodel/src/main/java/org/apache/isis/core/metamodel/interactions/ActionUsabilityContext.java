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

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.id.Identifier;
import org.apache.isis.applib.services.wrapper.events.ActionUsabilityEvent;
import org.apache.isis.core.metamodel.consent.InteractionContextType;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObjects.UnwrapUtil;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

/**
 * See {@link InteractionContext} for overview; analogous to
 * {@link ActionUsabilityEvent}.
 */
public class ActionUsabilityContext 
extends UsabilityContext 
implements ActionInteractionContext {

    private final ObjectAction objectAction;

    public ActionUsabilityContext(
            final InteractionHead head,
            final ObjectAction objectAction,
            final Identifier id,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {
        super(InteractionContextType.ACTION_USABLE, head, id, interactionInitiatedBy, where);
        this.objectAction = objectAction;
    }

    @Override
    public ObjectAction getObjectAction() {
        return objectAction;
    }

    @Override
    public ActionUsabilityEvent createInteractionEvent() {
        return new ActionUsabilityEvent(UnwrapUtil.single(getTarget()), getIdentifier());
    }

}
