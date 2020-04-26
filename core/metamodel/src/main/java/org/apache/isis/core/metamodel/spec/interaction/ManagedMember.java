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
package org.apache.isis.core.metamodel.spec.interaction;

import java.util.Optional;
import java.util.function.BiFunction;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public abstract class ManagedMember {

    // only used to create failure messages
    @RequiredArgsConstructor
    public static enum MemberType {
        PROPERTY(OneToOneAssociation.class, (spec, propertyId)->
        spec.getAssociation(propertyId)
        .map(property->property.isOneToOneAssociation()?property:null)),
        
        COLLECTION(OneToManyAssociation.class, (spec, collectionId)->
        spec.getAssociation(collectionId)
        .map(collection->collection.isOneToManyAssociation()?collection:null)),
        
        ACTION(ObjectAction.class, (spec, actionId)->
        spec.getObjectAction(actionId));
        
        @Getter private final Class<? extends ObjectMember> memberType;
        private final BiFunction<
                ObjectSpecification, String, 
                Optional<? extends ObjectMember>
            > memberProvider;
    
        public <T extends ObjectMember> Optional<T> lookup(
                @NonNull final ManagedObject owner,
                @NonNull final String memberId) {
            val onwerSpec = owner.getSpecification();
            val member = memberProvider.apply(onwerSpec, memberId);
            return _Casts.uncheckedCast(member);
        }
        
    }
    
    @Getter
    private final ManagedObject owner;
    
    public abstract ObjectMember getMember();
    
    public abstract MemberType getMemberType();
    
    public ObjectSpecification getSpecification() {
        return getMember().getSpecification();
    }
    
    public String getName() {
        return getMember().getName();
    }
    
    public Identifier getIdentifier() {
        return getMember().getIdentifier();
    }
    
    /**
     * @param where
     * @return non-empty if hidden
     */
    public Optional<InteractionVeto> checkVisibility(
            @NonNull final Where where) {

        val visibilityConsent = 
                getMember()
                .isVisible(getOwner(), InteractionInitiatedBy.USER, where);
        
        return visibilityConsent.isVetoed()
                ? Optional.of(InteractionVeto.hidden(visibilityConsent)) 
                : Optional.empty();
    }

    /**
     * @param where
     * @return non-empty if not usable/editable (meaning if read-only)
     */
    public Optional<InteractionVeto> checkUsability(
            @NonNull final Where where) {
        
        val usabilityConsent = 
                getMember()
                .isUsable(getOwner(), InteractionInitiatedBy.USER, where);
        
        return usabilityConsent.isVetoed()
                ? Optional.of(InteractionVeto.readonly(usabilityConsent))
                : Optional.empty();
    }
    
    protected static <T extends ObjectMember> Optional<T> lookup(
            @NonNull final ManagedObject owner,
            @NonNull final MemberType memberType,
            @NonNull final String memberId) {
        return memberType.lookup(owner, memberId);
    }
    
}
