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
import org.apache.isis.applib.events.VisibilityEvent;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionContextType;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;

/**
 * See {@link InteractionContext} for overview; analogous to
 * {@link VisibilityEvent}.
 */
public abstract class VisibilityContext<T extends VisibilityEvent> extends InteractionContext<T> {

    private Where where;

    public VisibilityContext(
            final InteractionContextType interactionType,
            final DeploymentCategory deploymentCategory,
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final Identifier identifier,
            final ObjectAdapter target,
            final Where where) {
        super(interactionType, deploymentCategory, session, invocationMethod, identifier, target);
        this.where = where;
    }

    /**
     * Where the element is to be rendered.
     */
    public Where getWhere() {
        return where;
    }

}
