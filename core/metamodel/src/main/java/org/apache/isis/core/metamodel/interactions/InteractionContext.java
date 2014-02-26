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

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.events.InteractionEvent;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionContextType;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.Facet;

/**
 * Represents an interaction between the framework and (a {@link Facet} of) the
 * domain object.
 * 
 * <p>
 * There are two main responsibilities:
 * <ul>
 * <li>Wraps up a target object, parameters and a {@link AuthenticationSession}.
 * Defining this as a separate interface makes for a more stable API</li>
 * <li>Acts as a factory for the corresponding {@link InteractionEvent} (more on
 * this below).</li>
 * </ul>
 * 
 * <p>
 * The {@link InteractionContext} hierarchy is parallel to the
 * {@link InteractionEvent} hierarchy. Having parallel hierarchies is a bit of a
 * code-smell. However, it is required because the {@link InteractionContext
 * context} hierarchy is internal to the framework (with references to
 * {@link ObjectAdapter}s, {@link AuthenticationSession}s and so forth), whereas
 * the {@link InteractionEvent event} hierarchy is part of the corelib, that is
 * public API.
 * 
 * <p>
 * The class is genericized so that the {@link #createInteractionEvent() factory
 * method} can return the correct subclass without having to downcast.
 */
public abstract class InteractionContext<T extends InteractionEvent> {

    private final InteractionContextType interactionType;
    private final Identifier identifier;
    private final InteractionInvocationMethod invocation;
    private final AuthenticationSession session;
    private final ObjectAdapter target;
    private final DeploymentCategory deploymentCategory;
    
    private int contributeeParam = -1; // no contributee
    private ObjectAdapter contributee = null;

    public InteractionContext(final InteractionContextType interactionType, DeploymentCategory deploymentCategory, final AuthenticationSession session, final InteractionInvocationMethod invocationMethod, final Identifier identifier, final ObjectAdapter target) {
        this.interactionType = interactionType;
        this.invocation = invocationMethod;
        this.identifier = identifier;
        this.session = session;
        this.target = target;
        this.deploymentCategory = deploymentCategory;
    }

    public DeploymentCategory getDeploymentCategory() {
        return deploymentCategory;
    }

    /**
     * The type of interaction.
     * 
     * <p>
     * Available for use by {@link Facet}s that apply only in certain
     * conditions. For example, some facets for collections will care only when
     * an object is being added to the collection, but won't care when an object
     * is being removed from the collection.
     * 
     * <p>
     * Alternatively, {@link Facet}s can use <tt>instanceof</tt>.
     */
    public InteractionContextType getInteractionType() {
        return interactionType;
    }

    /**
     * The identifier of the object or member that is being identified with.
     * 
     * <p>
     * If the {@link #getInteractionType() type} is
     * {@link InteractionContextType#OBJECT_VALIDATE}, will be the identifier of
     * the {@link #getTarget() target} object's specification. Otherwise will be
     * the identifier of the member.
     */
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * The {@link AuthenticationSession user or role} that is performing this
     * interaction.
     */
    public AuthenticationSession getSession() {
        return session;
    }

    /**
     * How the interaction was initiated.
     */
    public InteractionInvocationMethod getInvocationMethod() {
        return invocation;
    }

    /**
     * Convenience method that indicates whether the
     * {@link #getInvocationMethod() interaction was invoked} programmatically.
     */
    public boolean isProgrammatic() {
        return invocation == InteractionInvocationMethod.PROGRAMMATIC;
    }

    /**
     * The target object that this interaction is with.
     */
    public ObjectAdapter getTarget() {
        return target;
    }

    // //////////////////////////////////////

    public void putContributee(int contributeeParam, ObjectAdapter contributee) {
        this.contributeeParam = contributeeParam;
        this.contributee = contributee;
    }
    
    public Map<Integer, ObjectAdapter> getContributeeAsMap() {
        return contributee != null
                ? ImmutableMap.<Integer, ObjectAdapter>of(contributeeParam, contributee)
                : ImmutableMap.<Integer, ObjectAdapter>of();
    }

    // //////////////////////////////////////

    
    
    /**
     * Factory method to create corresponding {@link InteractionEvent}.
     * 
     * @return
     */
    public abstract T createInteractionEvent();

}
