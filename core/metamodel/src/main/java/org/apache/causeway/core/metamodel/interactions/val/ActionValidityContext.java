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
package org.apache.causeway.core.metamodel.interactions.val;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.services.wrapper.events.ActionInvocationEvent;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionContextType;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.interactions.ActionInteractionContext;
import org.apache.causeway.core.metamodel.interactions.InteractionContext;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

/**
 * See {@link InteractionContext} for overview; analogous to
 * {@link ActionInvocationEvent}.
 */
public record ActionValidityContext(
    ValidityContextRecord validityContext,
    ObjectAction objectAction,
    Can<ManagedObject> args
    ) implements ActionInteractionContext, ValidityContextHolder {

    public ActionValidityContext(
            final InteractionHead head,
            final ObjectAction objectAction,
            final Identifier id,
            final Can<ManagedObject> args,
            final InteractionInitiatedBy interactionInitiatedBy) {

        this(
            new ValidityContextRecord(InteractionContextType.ACTION_INVOKE,
                head,
                id,
                ()->objectAction.getFriendlyName(head::target),
                interactionInitiatedBy),
            objectAction,
            args);
    }

    @Override
    public ActionInvocationEvent createInteractionEvent() {
        return new ActionInvocationEvent(
                MmUnwrapUtils.single(target()), identifier(), MmUnwrapUtils.multipleAsArray(args().toList()));
    }

}
