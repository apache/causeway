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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

/**
 * Model that holds the objects involved with the interaction.
 * That is a tuple of {regular-object, (same) regular-object}
 * or {mixee, mixin}, based on whether a regular object or a mixee/mixin pair
 * is represented.
 * @since 2.0
 */
public record InteractionHead(
        /**
         * The owning (domain) object of an interaction.
         */
        ManagedObject owner,
        /**
         * Typically equal to {@code owner}, except for mixins,
         * where {@code target} is the mixin instance.
         */
        ManagedObject target) {
    
    // -- FACTORIES
    
    /** Regular case, when owner equals target. (no mixin) */
    public static InteractionHead regular(final ManagedObject owner) {
        return new InteractionHead(owner, owner);
    }

    /** Mixin case, when target is a mixin for the owner. */
    public static InteractionHead mixin(final @NonNull ManagedObject owner, final @NonNull ManagedObject target) {
        return new InteractionHead(owner, target);
    }
    
    // canonical constructor with consistency checks
    public InteractionHead(
        final ManagedObject owner,
        final ManagedObject target) {
        if(ManagedObjects.isSpecified(owner)
            && owner.objSpec().getBeanSort().isMixin()) {
            throw _Exceptions.unrecoverable("unexpected: owner is a mixin %s", owner);
        }
        if(ManagedObjects.isSpecified(target)
            && target.objSpec().getBeanSort().isMixin()
            && target.getPojo()==null) {
            throw _Exceptions.unrecoverable("target not spec. %s", target);
        }
        this.owner = owner;
        this.target = target;
    }

    /**
     * Whether given command corresponds to given objectMember
     * by virtue of matching logical member identifiers.
     */
    public boolean isCommandForMember(
            final @Nullable Command command,
            final @Nullable ObjectMember objectMember) {
        return command!=null
                && objectMember!=null
                && logicalMemberIdentifierFor(objectMember)
                    .equals(command.getLogicalMemberIdentifier());
    }
    
    public String logicalMemberIdentifierFor(final ObjectMember objectMember) {
        if (!objectMember.isMixedIn()
                && objectMember instanceof ObjectAction objectAction
                && objectAction.isDeclaredOnMixin()) {
            // corner case when the objectMember is an ObjectActionDefault but corresponds to a mixin main  
            return logicalMemberIdentifierFor(owner().objSpec(), 
                objectMember.getProgrammingModel()
                    .mixinNamingStrategy()
                    .memberId(objectAction.getFeatureIdentifier().logicalType().correspondingClass()));
        }            
        if(objectMember instanceof ObjectAction act) {
            return logicalMemberIdentifierFor(act.getDeclaringType(), act.getFeatureIdentifier().memberLogicalName());
        }
        if(objectMember instanceof OneToOneAssociation prop) {
            return logicalMemberIdentifierFor(prop.getDeclaringType(), prop.getFeatureIdentifier().memberLogicalName());
        }
        throw new IllegalArgumentException(objectMember.getClass() + " is not supported");    
    }
    
    /**
     * Whether this head corresponds to a mixin.
     */
    public boolean isMixin() {
        return target.objSpec().isMixin();
    }
    
    // -- HELPER
    
    private String logicalMemberIdentifierFor(final ObjectSpecification onType, final String memberId) {
        return onType.logicalTypeName() + "#" + memberId;
    }

    
}