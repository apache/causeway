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
import org.apache.causeway.core.metamodel.consent.InteractionContextType;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent=true)
@Deprecated
public abstract class InteractionContextAbstract implements InteractionContext {

    @Getter private final InteractionContextType interactionType;
    @Getter private final InteractionInitiatedBy initiatedBy;
    @Getter private final Identifier identifier;
    @Getter private final InteractionHead head;
    @Getter private final Where where;

    protected InteractionContextAbstract(
            final InteractionContextType interactionType,
            final InteractionInitiatedBy invocationMethod,
            final Identifier identifier,
            final InteractionHead head,
            final Where where) {
        this.interactionType = interactionType;
        this.initiatedBy = invocationMethod;
        this.identifier = identifier;
        this.head = head;
        this.where = where;
    }

    @Override
    public ManagedObject target() {
        return head.target();
    }

}
