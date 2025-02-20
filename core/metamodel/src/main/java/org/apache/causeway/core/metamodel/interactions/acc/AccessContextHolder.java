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
package org.apache.causeway.core.metamodel.interactions.acc;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.consent.InteractionContextType;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;

/**
 * Helper interface that allows composition on {@link AccessContextRecord}.
 */
sealed interface AccessContextHolder
extends AccessContext
permits CollectionAccessContext, ObjectTitleContext, PropertyAccessContext {

    record AccessContextRecord(
        InteractionContextType interactionType,
        InteractionInitiatedBy initiatedBy,
        Identifier identifier,
        InteractionHead head,
        Where where) {

        public AccessContextRecord(
                final InteractionContextType interactionType,
                final Identifier identifier,
                final InteractionHead head,
                final InteractionInitiatedBy initiatedBy) {
            this(interactionType, initiatedBy, identifier, head, Where.NOT_SPECIFIED);
        }

    }

    AccessContextRecord accessContext();

    @Override
    default InteractionContextType interactionType() {
        return accessContext().interactionType();
    }

    @Override
    default InteractionInitiatedBy initiatedBy() {
        return accessContext().initiatedBy();
    }

    @Override
    default Identifier identifier() {
        return accessContext().identifier();
    }

    @Override
    default InteractionHead head() {
        return accessContext().head();
    }

    @Override
    default Where where() {
        return accessContext().where();
    }

}
