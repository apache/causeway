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

package org.apache.isis.metamodel.interactions;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.wrapper.events.VisibilityEvent;
import org.apache.isis.metamodel.consent.InteractionContextType;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.spec.ManagedObject;

/**
 * See {@link InteractionContext} for overview; analogous to
 * {@link VisibilityEvent}.
 */
public abstract class VisibilityContext<T extends VisibilityEvent> extends InteractionContext<T> {

    private Where where;

    public VisibilityContext(
            final InteractionContextType interactionType,
            final ManagedObject targetAdapter,
            final Identifier identifier,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {
        super(interactionType, interactionInitiatedBy, identifier, targetAdapter);
        this.where = where;
    }

    /**
     * Where the element is to be rendered.
     */
    public Where getWhere() {
        return where;
    }

}
