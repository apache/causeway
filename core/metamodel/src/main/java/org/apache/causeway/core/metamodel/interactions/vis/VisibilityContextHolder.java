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
import org.apache.causeway.core.metamodel.consent.InteractionContextType;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.interactions.RenderPolicy;
import org.apache.causeway.core.metamodel.object.ManagedObject;

/**
 * Helper interface that allows composition on {@link VisibilityContextRecord}.
 */
sealed interface VisibilityContextHolder
extends VisibilityContext
permits ParamVisibilityContext, ActionVisibilityContext, CollectionVisibilityContext,
    ObjectVisibilityContext, PropertyVisibilityContext {

    record VisibilityContextRecord(
        InteractionContextType interactionType,
        InteractionHead head,
        Identifier identifier,
        InteractionInitiatedBy initiatedBy,
        Where where,
        RenderPolicy renderPolicy) {
    }

    VisibilityContextRecord visibilityContext();

    @Override
    default RenderPolicy renderPolicy() {
        return visibilityContext().renderPolicy();
    }

    @Override
    default InteractionContextType interactionType() {
        return visibilityContext().interactionType();
    }

    @Override
    default InteractionInitiatedBy initiatedBy() {
        return visibilityContext().initiatedBy();
    }

    @Override
    default Identifier identifier() {
        return visibilityContext().identifier();
    }

    @Override
    default InteractionHead head() {
        return visibilityContext().head();
    }

    @Override
    default Where where() {
        return visibilityContext().where();
    }

    @Override
    default ManagedObject target() {
        return head().target();
    }

}
