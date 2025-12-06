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
package org.apache.causeway.core.metamodel.interactions.vis;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.events.ActionArgumentVisibilityEvent;
import org.apache.causeway.applib.services.wrapper.events.ActionArgumentEvent;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionContextType;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.interactions.ActionInteractionContext;
import org.apache.causeway.core.metamodel.interactions.InteractionContext;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.interactions.RenderPolicy;
import org.apache.causeway.core.metamodel.interactions.VisibilityConstraint;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

/**
 * See {@link InteractionContext} for overview; analogous to
 * {@link ActionArgumentEvent}.
 */
public record ParamVisibilityContext(
		InteractionContextType interactionType,
		InteractionHead head,
		Identifier identifier,
		InteractionInitiatedBy initiatedBy,
		VisibilityConstraint visibilityConstraint,
		RenderPolicy renderPolicy,
		ObjectAction objectAction,
		Can<ManagedObject> args,
		int position)
implements VisibilityContext, ActionInteractionContext {

    public ParamVisibilityContext(
            final InteractionHead head,
            final ObjectAction objectAction,
            final Identifier id,
            final Can<ManagedObject> args,
            final int position,
            final InteractionInitiatedBy initiatedBy,
            final RenderPolicy renderPolicy) {

    	// assumption: param visibility is never directly constraint by WhatViewer or Where;
    	// instead those constraints apply only to their 'owning' actions
    	// in other words, if an action is visible honoring WhatViewer or Where, then no further
    	// visibility vetos for params are considered based on WhatViewer or Where.
        this(InteractionContextType.ACTION_PARAMETER_VISIBLE,
            head, id, initiatedBy, VisibilityConstraint.noViewer(Where.ANYWHERE), renderPolicy,
            objectAction, args, position);
    }

    @Override
    public ActionArgumentVisibilityEvent createInteractionEvent() {
        return new ActionArgumentVisibilityEvent(
                MmUnwrapUtils.single(target()),
                identifier(),
                MmUnwrapUtils.multipleAsArray(args().toList()),
                position());
    }

}
