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

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.id.FeatureIdentifier;
import org.apache.isis.applib.services.wrapper.events.InteractionEvent;
import org.apache.isis.core.metamodel.consent.InteractionContextType;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.security.authentication.Authentication;

import lombok.Getter;

/**
 * Represents an interaction between the framework and (a {@link Facet} of) the
 * domain object.
 *
 * <p>
 * There are two main responsibilities:
 * <ul>
 * <li>Wraps up a target object, parameters and a {@link Authentication}.
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
 * {@link ManagedObject}s, {@link Authentication}s and so forth), whereas
 * the {@link InteractionEvent event} hierarchy is part of AppLib, that is
 * public API.
 *
 * <p>
 * The class is generic so that the 
 * {@link InteractionEventSupplier#createInteractionEvent() factory method} 
 * can return the correct subclass without having to downcast.
 */
public abstract class InteractionContext {

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
    @Getter private final InteractionContextType interactionType;
    
    /**
     * How the interaction was initiated.
     */
    @Getter private final InteractionInitiatedBy initiatedBy;
    
    /**
     * The identifier of the object or member that this interaction is being 
     * identified with.
     *
     * <p>
     * If the {@link #getInteractionType() type} is
     * {@link InteractionContextType#OBJECT_VALIDATE}, will be the identifier of
     * the {@link #getTarget() target} object's specification. Otherwise will be
     * the identifier of the member.
     */
    @Getter private final FeatureIdentifier identifier;
    
    /**
     * Model that holds the object involved with the interaction.
     */
    @Getter private final InteractionHead head;
    
    /**
     * Where the element is to be rendered.
     */
    @Getter private final Where where;
    
    protected InteractionContext(
            final InteractionContextType interactionType,
            final InteractionInitiatedBy invocationMethod,
            final FeatureIdentifier identifier,
            final InteractionHead head,
            final Where where) {
        this.interactionType = interactionType;
        this.initiatedBy = invocationMethod;
        this.identifier = identifier;
        this.head = head;
        this.where = where;
    }

    /**
     * The target object that this interaction is associated with.
     */
    public ManagedObject getTarget() {
        return head.getTarget();
    }

    
    /**
     * Convenience method that indicates whether the
     * {@link #getInitiatedBy() interaction was invoked} by the framework.
     */
    public boolean isFrameworkInitiated() {
        return initiatedBy == InteractionInitiatedBy.FRAMEWORK;
    }
    

}
