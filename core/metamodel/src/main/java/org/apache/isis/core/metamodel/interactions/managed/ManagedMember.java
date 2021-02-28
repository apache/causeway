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
package org.apache.isis.core.metamodel.interactions.managed;

import java.util.Optional;
import java.util.function.BiFunction;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.id.FeatureIdentifier;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects.EntityUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;

@RequiredArgsConstructor
public abstract class ManagedMember implements ManagedFeature {

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
        spec.getAction(actionId));
        
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
    
    /**
     * Some representations may vary according to whether the member is to be represented for read
     * (render the state of the property or collection) or for write (render additional hypermedia controls to allow
     * the property to be modified/cleared, or the collection to be added to/removed from).
     */
    public enum RepresentationMode {
        AUTO,
        READ,
        WRITE;
        public boolean isAuto() {return this == AUTO;}
        public boolean isRead() {return this == READ;}
        public boolean isWrite() {return this == WRITE;}
        public boolean isExplicit() {return !isAuto();}
    }
    
    @NonNull private ManagedObject owner;
    public ManagedObject getOwner() {
        return owner = EntityUtil.reattach(owner);
    }
    
    @Getter @NonNull private final Where where;

    /**
     * Allows a managed property of a view model to replace its owner with a clone.
     *
     * <p>
     *     This is because view models are intrinsically immutable.
     * </p>
     *
     * @param managedObject
     */
    protected void setOwner(@NonNull ManagedObject managedObject) {
        this.owner = managedObject;
    }


    public abstract ObjectMember getMetaModel();
    
    public abstract MemberType getMemberType();
    
    @Override
    public ObjectSpecification getSpecification() {
        return getMetaModel().getSpecification();
    }
    
    public String getId() {
        return getMetaModel().getId();
    }
    
    public String getName() {
        return getMetaModel().getName();
    }
    
    @Override
    public FeatureIdentifier getIdentifier() {
        return getMetaModel().getIdentifier();
    }
    
    @Override
    public String getDisplayLabel() {
        return getMetaModel().getName();
    }
    
    @Getter @Setter @NonNull 
    private RepresentationMode representationMode = RepresentationMode.AUTO;

    
    /**
     * @return non-empty if hidden
     */
    public Optional<InteractionVeto> checkVisibility() {

        try {
            val visibilityConsent = 
                    getMetaModel()
                    .isVisible(getOwner(), InteractionInitiatedBy.USER, where);
            
            return visibilityConsent.isVetoed()
                    ? Optional.of(InteractionVeto.hidden(visibilityConsent)) 
                    : Optional.empty();
            
        } catch (final Exception ex) {
            
            return Optional.of(InteractionVeto.hidden(new Veto("failure during visibility evaluation")));
            
        }
        
    }

    /**
     * @return non-empty if not usable/editable (meaning if read-only)
     */
    public Optional<InteractionVeto> checkUsability() {
        
        try {
            
            val usabilityConsent = 
                    getMetaModel()
                    .isUsable(getOwner(), InteractionInitiatedBy.USER, where);
            
            return usabilityConsent.isVetoed()
                    ? Optional.of(InteractionVeto.readonly(usabilityConsent))
                    : Optional.empty();
            
        } catch (final Exception ex) {
            
            return Optional.of(InteractionVeto
                    .readonly(
                            new Veto(ex.getLocalizedMessage())));
            
        }
        
    }
    
    protected static <T extends ObjectMember> Optional<T> lookup(
            @NonNull final ManagedObject owner,
            @NonNull final MemberType memberType,
            @NonNull final String memberId) {
        return memberType.lookup(owner, memberId);
    }


    
}
