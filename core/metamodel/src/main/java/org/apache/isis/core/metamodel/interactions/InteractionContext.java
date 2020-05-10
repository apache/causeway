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

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.wrapper.events.InteractionEvent;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.consent.InteractionContextType;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.security.authentication.AuthenticationSession;

import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

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
 * {@link ManagedObject}s, {@link AuthenticationSession}s and so forth), whereas
 * the {@link InteractionEvent event} hierarchy is part of AppLib, that is
 * public API.
 *
 * <p>
 * The class is generic so that the {@link #createInteractionEvent() factory
 * method} can return the correct subclass without having to downcast.
 */
public abstract class InteractionContext {

    /**
     * Model that holds the objects involved with the interaction.
     * @since 2.0
     */
    @Value(staticConstructor = "unchecked")
    public static class Head {
        /**
         * The owning object that this interaction is associated with.
         */
        @NonNull private final ManagedObject owner;
        
        /**
         * The target object that this interaction is associated with.
         */
        @NonNull private final ManagedObject target;
        
        /** in support of legacy code */
        public static Head of(@NonNull ManagedObject owner, @NonNull ManagedObject target) {
            if(ManagedObject.isSpecified(owner) 
                    && owner.getSpecification().getBeanSort().isMixin()) {
                throw _Exceptions.unrecoverableFormatted("unexpected: owner is a mixin %s", owner);
            }
            if(ManagedObject.isSpecified(target)                    
                    && target.getSpecification().getBeanSort().isMixin()
                    && target.getPojo()==null) {
                throw _Exceptions.unrecoverableFormatted("target not spec. %s", target);
            }
            return unchecked(owner, target);
        }
        
        /** when owner equals target (no mixin) */
        public static Head simple(ManagedObject owner) {
            return Head.of(owner, owner);
        }
        
        /** 
         * as used by the domain event subsystem
         * @return optionally the owner, if the target is a mixin 
         */
        public Optional<ManagedObject> getMixedIn() {
            return Objects.equals(getOwner(), getTarget()) 
                    ? Optional.empty()
                    : Optional.of(getOwner());
        }

        /** in support of legacy code */
        public static Head mixedIn(@NonNull ManagedObject target, @Nullable ManagedObject mixedIn) {
            return mixedIn==null
                    ? of(target, target)
                    : of(mixedIn, target);
        }
        
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
    @Getter private final Identifier identifier;
    
    /**
     * Model that holds the object involved with the interaction.
     */
    @Getter private final Head head;
    
    /**
     * Where the element is to be rendered.
     */
    @Getter private final Where where;
    
    protected InteractionContext(
            final InteractionContextType interactionType,
            final InteractionInitiatedBy invocationMethod,
            final Identifier identifier,
            final Head head,
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
