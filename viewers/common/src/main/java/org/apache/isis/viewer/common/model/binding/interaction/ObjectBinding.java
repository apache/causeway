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
package org.apache.isis.viewer.common.model.binding.interaction;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.internal.base._Either;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.common.model.binding.interaction.InteractionResponse.Veto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName = "bind")
public class ObjectBinding {
    
    // only used to create failure messages
    @RequiredArgsConstructor
    private static enum MemberType {
        PROPERTY(OneToOneAssociation.class, (spec, propertyId)->
        spec.getAssociation(propertyId)
        .map(property->property.isOneToOneAssociation()?property:null)),
        
        COLLECTION(OneToManyAssociation.class, (spec, collectionId)->
        spec.getAssociation(collectionId)
        .map(collection->collection.isOneToManyAssociation()?collection:null)),
        
        ACTION(ObjectAction.class, (spec, actionId)->
        spec.getObjectAction(actionId));
        
        @Getter private final Class<? extends ObjectMember> memberType;
        @Getter private final BiFunction<
                ObjectSpecification, String, 
                Optional<? extends ObjectMember>
            > memberProvider;
        
    }
    
    public static enum AccessIntent {
        ACCESS, MUTATE;

        public boolean isMutate() {
            return this == MUTATE;
        }
    }

    @Getter
    private final ManagedObject managedObject;
    
    public String getTitle() {
        return managedObject.getSpecification().getTitle(null, managedObject);
    }

    @Deprecated
    public Stream<OneToOneAssociation> streamVisisbleProperties() {
        return managedObject.getSpecification()
        .streamAssociations(Contributed.INCLUDED)
        .filter(objMember->objMember.getFeatureType().isProperty())
        //TODO filter visibility
        .map(OneToOneAssociation.class::cast);
    }
    
    @Deprecated
    public Stream<OneToManyAssociation> streamVisisbleCollections() {
        return managedObject.getSpecification()
        .streamAssociations(Contributed.INCLUDED)
        .filter(objMember->objMember.getFeatureType().isCollection())
        //TODO filter visibility
        .map(OneToManyAssociation.class::cast);
    }
    
    @Deprecated
    public Stream<ObjectAction> streamVisisbleActions() {
        return managedObject.getSpecification()
        .streamObjectActions(Contributed.INCLUDED)
        .filter(objMember->objMember.getFeatureType().isAction())
        //TODO filter visibility
        .map(ObjectAction.class::cast);
    }
    
    public Optional<ObjectAction> lookupAction(String actionId) {
        return streamVisisbleActions()
                .filter(action->Objects.equals(actionId, action.getId()))
                .findFirst();
    }
    
    public Optional<OneToOneAssociation> lookupVisibleProperty(String propertyId) {
        return streamVisisbleProperties()
                .filter(property->Objects.equals(propertyId, property.getId()))
                .findFirst();
    }

    public Optional<OneToManyAssociation> lookupCollection(String collectionId) {
        return streamVisisbleCollections()
                .filter(collection->Objects.equals(collectionId, collection.getId()))
                .findFirst();
    }

    /**
     * @param actionId
     * @param where
     * @return ActionAccessChain with either an editable or readonly ActionBinding, 
     * based on visibility and usability
     */
    public ActionAccessChain getActionBinding(String actionId, Where where) {
        val either = getActionThatIsVisible(actionId, where)
        .map(
                action->
                    checkUsability(action, where).isSuccess()
                       ? ActionBindingUsable.of(managedObject, action)
                       : ActionBindingReadonly.of(managedObject, action), 
                UnaryOperator.identity());
        return ActionAccessChain.of(either);
    }
    
    /**
     * 
     * @param actionId
     * @param where
     * @param accessIntent
     * @return ActionAccessChain with either an editable or readonly PropertyBinding, 
     * based on visibility and accessIntent
     */
    public ActionAccessChain getActionBinding(
            final String actionId, 
            final Where where, 
            final AccessIntent accessIntent) {
        
        val either = getActionThatIsVisibleForIntent(actionId, where, accessIntent)
        .map(
                action->{
                    switch(accessIntent) {
                    case ACCESS:
                        return ActionBindingReadonly.of(managedObject, action);
                    case MUTATE:
                        return ActionBindingUsable.of(managedObject, action);
                    default:
                        throw _Exceptions.unmatchedCase(accessIntent);
                    }
                },
                UnaryOperator.identity());
        return ActionAccessChain.of(either);
    }
    
    /**
     * @param propertyId
     * @param where
     * @return PropertyAccessChain with either an editable or readonly PropertyBinding, 
     * based on visibility and usability
     */
    public PropertyAccessChain getPropertyBinding(String propertyId, Where where) {
        val either = getPropertyThatIsVisible(propertyId, where)
        .map(
                property->
                    checkUsability(property, where).isSuccess()
                       ? PropertyBindingEditable.of(managedObject, property)
                       : PropertyBindingReadonly.of(managedObject, property), 
                UnaryOperator.identity());
        return PropertyAccessChain.of(either);
    }
    
    /**
     * 
     * @param propertyId
     * @param where
     * @param accessIntent
     * @return PropertyAccessChain with either an editable or readonly PropertyBinding, 
     * based on visibility and accessIntent
     */
    public PropertyAccessChain getPropertyBinding(
            final String propertyId, 
            final Where where, 
            final AccessIntent accessIntent) {
        
        val either = getPropertyThatIsVisibleForIntent(propertyId, where, accessIntent)
        .map(
                property->{
                    switch(accessIntent) {
                    case ACCESS:
                        return PropertyBindingReadonly.of(managedObject, property);
                    case MUTATE:
                        return PropertyBindingEditable.of(managedObject, property);
                    default:
                        throw _Exceptions.unmatchedCase(accessIntent);
                    }
                },
                UnaryOperator.identity());
        return PropertyAccessChain.of(either);
    }
    
    /**
     * @param collectionId
     * @param where
     * @return CollectionAccessChain with either an editable or readonly CollectionBinding, 
     * based on visibility and usability
     */
    public CollectionAccessChain getCollectionBinding(String collectionId, Where where) {
        val either = getCollectionThatIsVisible(collectionId, where)
        .map(
                coll->
                    checkUsability(coll, where).isSuccess()
                       ? CollectionBindingEditable.of(managedObject, coll)
                       : CollectionBindingReadonly.of(managedObject, coll), 
                UnaryOperator.identity());
        
        return CollectionAccessChain.of(either);
    }
    
    /**
     * 
     * @param collectionId
     * @param where
     * @param accessIntent
     * @return CollectionAccessChain with either an editable or readonly CollectionBinding, 
     * based on visibility and accessIntent
     */
    public CollectionAccessChain getCollectionBinding(
            final String collectionId, 
            final Where where, 
            final AccessIntent accessIntent) {
        
        val either = getCollectionThatIsVisibleForIntent(collectionId, where, accessIntent)
        .map(
                coll->{
                    switch(accessIntent) {
                    case ACCESS:
                        return CollectionBindingReadonly.of(managedObject, coll);
                    case MUTATE:
                        return CollectionBindingEditable.of(managedObject, coll);
                    default:
                        throw _Exceptions.unmatchedCase(accessIntent);
                    }
                },
                UnaryOperator.identity());
        
        return CollectionAccessChain.of(either);

    }
    
    // -- HELPER - TOP LEVEL

    private _Either<ObjectAction, InteractionResponse> 
    getActionThatIsVisible(String actionId, Where where) {
        val chain = this.<ObjectAction>startChain(MemberType.ACTION, actionId);
        return checkVisibility(chain, MemberType.ACTION, where);
    }
    
    private _Either<OneToOneAssociation, InteractionResponse> 
    getPropertyThatIsVisible(String propertyId, Where where) {
        val chain = this.<OneToOneAssociation>startChain(MemberType.PROPERTY, propertyId);
        return checkVisibility(chain, MemberType.PROPERTY, where);
    }
    
    private _Either<OneToManyAssociation, InteractionResponse> 
    getCollectionThatIsVisible(String collectionId, Where where) {
        val chain = this.<OneToManyAssociation>startChain(MemberType.COLLECTION, collectionId);
        return checkVisibility(chain, MemberType.COLLECTION, where);
    }
    
    private _Either<ObjectAction, InteractionResponse> getActionThatIsVisibleForIntent(
            final String actionId, 
            final Where where, 
            final AccessIntent accessIntent) {
        return checkUsability(getActionThatIsVisible(actionId, where), where, accessIntent);
    }
    
    private _Either<OneToOneAssociation, InteractionResponse> getPropertyThatIsVisibleForIntent(
            final String propertyId, 
            final Where where, 
            final AccessIntent accessIntent) {
        return checkUsability(getPropertyThatIsVisible(propertyId, where), where, accessIntent);
    }
    
    private _Either<OneToManyAssociation, InteractionResponse> getCollectionThatIsVisibleForIntent(
            final String propertyId, 
            final Where where, 
            final AccessIntent accessIntent) {
        return checkUsability(getCollectionThatIsVisible(propertyId, where), where, accessIntent);
    }
    
    // HELPER - MID LEVEL
    
    private <T extends ObjectMember> _Either<T, InteractionResponse> startChain(
            final MemberType memberType,
            final String memberId) {
        
        val spec = managedObject.getSpecification();
        val member = memberType.getMemberProvider().apply(spec, memberId);
        
        if(!member.isPresent()) {
            return _Either.right(InteractionResponse.failed(
                    Veto.NOT_FOUND,
                    notFound(memberType, memberId)));
        }
        return _Either.<T, InteractionResponse>left((T)member.get());
    }
    
    private <T extends ObjectMember> _Either<T, InteractionResponse> checkVisibility(
            final _Either<T, InteractionResponse> chain,
            final MemberType memberType,
            final Where where) {
        
        return chain.leftRemap(objectMember->{
            val visibility = checkVisibility(memberType, objectMember, where);
            if(visibility.isFailure()) {
                return _Either.right(visibility);
            }
            return _Either.left(objectMember);
            
        });
    }
    
    private <T extends ObjectMember> _Either<T, InteractionResponse> checkUsability(
            final _Either<T, InteractionResponse> chain, 
            final Where where, 
            final AccessIntent accessIntent) {
        
        return chain.leftRemap(objectMember->{
            if(accessIntent.isMutate()) {
                val usability = checkUsability(objectMember, where);
                if(usability.isFailure()) {
                    return _Either.right(usability);
                }
            }
            return _Either.left(objectMember);
            
        });
    }

    // HELPER - LOWEST LEVEL
    
    private <T extends ObjectMember> InteractionResponse checkVisibility(
            final MemberType memberType,
            final T objectMember, 
            final Where where) {

        val visibilityConsent =
                objectMember.isVisible(
                        managedObject, InteractionInitiatedBy.USER, where);
        return visibilityConsent.isVetoed()
                ? InteractionResponse.failed(
                    Veto.HIDDEN,
                    notFound(memberType, objectMember.getId()))
                : InteractionResponse.success();
    }
    
    private <T extends ObjectMember> InteractionResponse checkUsability(
            final T objectMember, 
            final Where where) {
        
        val usabilityConsent = 
                objectMember.isUsable(
                    managedObject, InteractionInitiatedBy.USER, where);
        return usabilityConsent.isVetoed()
                ? InteractionResponse.failed(
                    Veto.FORBIDDEN,
                    usabilityConsent.getReason())
                : InteractionResponse.success();
    }
   
    private <T extends ObjectMember> String notFound(
            MemberType memberType, 
            String memberId) {
        return String.format("%s '%s' either does not exist, is disabled or is not visible", 
                        memberId, 
                        memberType.name().toLowerCase());
    }
    
}
