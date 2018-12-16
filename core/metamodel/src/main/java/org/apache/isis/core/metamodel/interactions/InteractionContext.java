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

import javax.annotation.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.wrapper.events.InteractionEvent;
import org.apache.isis.commons.internal.base._Tuples;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionContextType;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.security.authentication.AuthenticationSession;

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
 * The class is generic so that the {@link #createInteractionEvent() factory
 * method} can return the correct subclass without having to downcast.
 */
public abstract class InteractionContext<T extends InteractionEvent> {

    private final InteractionContextType interactionType;
    private final Identifier identifier;
    private final InteractionInitiatedBy interactionInitiatedBy;
    private final ManagedObject target;

    private int contributeeParam = -1; // no contributee
    private ManagedObject contributee = null;

    private ManagedObject mixedInAdapter = null; // for mixin members only, obviously

    public InteractionContext(
            final InteractionContextType interactionType,
            final InteractionInitiatedBy invocationMethod,
            final Identifier identifier,
            final ManagedObject target) {
        this.interactionType = interactionType;
        this.interactionInitiatedBy = invocationMethod;
        this.identifier = identifier;
        this.target = target;
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
     * How the interaction was initiated.
     */
    public InteractionInitiatedBy getInitiatedBy() {
        return interactionInitiatedBy;
    }

    /**
     * Convenience method that indicates whether the
     * {@link #getInitiatedBy() interaction was invoked} by the framework.
     */
    public boolean isFrameworkInitiated() {
        return interactionInitiatedBy == InteractionInitiatedBy.FRAMEWORK;
    }

    /**
     * The target object that this interaction is with.
     */
    public ManagedObject getTarget() {
        return target;
    }

    // //////////////////////////////////////

    public void putContributee(int contributeeParam, ManagedObject contributee) {
        this.contributeeParam = contributeeParam;
        this.contributee = contributee;
    }

    public @Nullable _Tuples.Tuple2<Integer, ManagedObject> getContributeeWithParamIndex() {
        if(contributee==null) {
            return null;
        }
        return _Tuples.pair(contributeeParam, contributee);
    }

    // //////////////////////////////////////

    public void setMixedIn(final ManagedObject mixedInAdapter) {
        this.mixedInAdapter = mixedInAdapter;
    }

    public ManagedObject getMixedIn() {
        return mixedInAdapter;
    }

    // //////////////////////////////////////



    /**
     * Factory method to create corresponding {@link InteractionEvent}.
     *
     * @return
     */
    public abstract T createInteractionEvent();

}
