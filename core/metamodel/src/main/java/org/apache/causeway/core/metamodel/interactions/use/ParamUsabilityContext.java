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
import org.apache.causeway.applib.events.ActionArgumentUsabilityEvent;
import org.apache.causeway.applib.services.wrapper.events.ActionArgumentEvent;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionContextType;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.interactions.ActionInteractionContext;
import org.apache.causeway.core.metamodel.interactions.InteractionContext;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.interactions.RenderPolicy;
import org.apache.causeway.core.metamodel.interactions.vis.ParamVisibilityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

/**
 * See {@link InteractionContext} for overview; analogous to
 * {@link ActionArgumentEvent}.
 */
public record ParamUsabilityContext(
    InteractionContextType interactionType,
    InteractionHead head,
    Identifier identifier,
    InteractionInitiatedBy initiatedBy,
    Where where,
    RenderPolicy renderPolicy,
    ObjectAction objectAction,
    Can<ManagedObject> args,
    int position)
implements UsabilityContext, ActionInteractionContext {

    public ParamUsabilityContext(
            final InteractionHead head,
            final ObjectAction objectAction,
            final Identifier id,
            final Can<ManagedObject> args,
            final int position,
            final InteractionInitiatedBy initiatedBy,
            final RenderPolicy renderPolicy) {

        this(InteractionContextType.ACTION_PARAMETER_USABLE,
            head, id, initiatedBy, Where.OBJECT_FORMS, renderPolicy,
            objectAction, args, position);
    }

    @Override
    public ActionArgumentUsabilityEvent createInteractionEvent() {
        return new ActionArgumentUsabilityEvent(
                MmUnwrapUtils.single(target()),
                identifier(),
                MmUnwrapUtils.multipleAsArray(args().toList()),
                position());
    }

    @Override
    public ParamVisibilityContext asVisibilityContext() {
        return new ParamVisibilityContext(head(), objectAction(), identifier(),
                args, position, initiatedBy(), renderPolicy());
    }

}
