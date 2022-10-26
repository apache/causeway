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
package org.apache.causeway.core.metamodel.interactions;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.events.ActionArgumentVisibilityEvent;
import org.apache.causeway.applib.services.wrapper.events.ActionArgumentEvent;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionContextType;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtil;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;

/**
 * See {@link InteractionContext} for overview; analogous to
 * {@link ActionArgumentEvent}.
 */
public class ActionArgVisibilityContext
extends VisibilityContext
implements ActionInteractionContext {

    @Getter(onMethod = @__(@Override)) private final ObjectAction objectAction;
    @Getter private final Can<ManagedObject> args;
    @Getter private final int position;

    public ActionArgVisibilityContext(
            final InteractionHead head,
            final ObjectAction objectAction,
            final Identifier id,
            final Can<ManagedObject> args,
            final int position,
            final InteractionInitiatedBy interactionInitiatedBy) {

        super(InteractionContextType.ACTION_PARAMETER_VISIBLE,
                head,
                id,
                interactionInitiatedBy,
                Where.OBJECT_FORMS);

        this.objectAction = objectAction;
        this.args = args;
        this.position = position;
    }

    @Override
    public ActionArgumentVisibilityEvent createInteractionEvent() {
        return new ActionArgumentVisibilityEvent(
                MmUnwrapUtil.single(getTarget()),
                getIdentifier(),
                MmUnwrapUtil.multipleAsArray(getArgs().toList()),
                getPosition());
    }

}
