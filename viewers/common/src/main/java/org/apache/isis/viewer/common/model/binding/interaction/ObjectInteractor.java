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
import java.util.stream.Stream;

import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.common.model.binding.UiComponentFactory;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName = "bind")
public class ObjectInteractor {

    private final ManagedObject managedObject;
    
    public String getTitle() {
        return managedObject.getSpecification().getTitle(null, managedObject);
    }

    public Stream<OneToOneAssociation> streamVisisbleProperties() {
        return managedObject.getSpecification()
        .streamAssociations(Contributed.INCLUDED)
        .filter(objMember->objMember.getFeatureType().isProperty())
        //TODO filter visibility
        .map(OneToOneAssociation.class::cast);
    }
    
    public Stream<OneToManyAssociation> streamVisisbleCollections() {
        return managedObject.getSpecification()
        .streamAssociations(Contributed.INCLUDED)
        .filter(objMember->objMember.getFeatureType().isCollection())
        //TODO filter visibility
        .map(OneToManyAssociation.class::cast);
    }
    
    public Stream<ObjectAction> streamVisisbleActions() {
        return managedObject.getSpecification()
        .streamObjectActions(Contributed.INCLUDED)
        .filter(objMember->objMember.getFeatureType().isAction())
        //TODO filter visibility
        .map(ObjectAction.class::cast);
    }
    
    public InteractionResponse modifyProperty(
            final OneToOneAssociation property, 
            final ManagedObject proposedNewValue) {
        
        val consent = property.isAssociationValid(managedObject, proposedNewValue, InteractionInitiatedBy.USER);
        if (consent.isVetoed()) {
            return InteractionResponse.failed(consent.getReason());
        }
        
        property.set(managedObject, proposedNewValue, InteractionInitiatedBy.USER);
        
        return InteractionResponse.success();
    }

    public UiComponentFactory.Request newUiComponentCreateRequest(
            final OneToOneAssociation property) {
        
        val propertyValue = Optional.ofNullable(property.get(managedObject))
                .orElse(ManagedObject.of(property.getSpecification(), null));
        
        return UiComponentFactory.Request
        .of(propertyValue, property, proposedNewValue -> {
            
            val iResponse = modifyProperty(property, proposedNewValue);
            if (iResponse.isFailure()) {
                return iResponse.getFailureMessage(); // validation result if any
            }
            return null;
            
        });
        
    }

    public Optional<ObjectAction> lookupAction(String id) {
        return streamVisisbleActions()
                .filter(action->Objects.equals(id, action.getId()))
                .findFirst();
    }
    
    public Optional<OneToOneAssociation> lookupProperty(String id) {
        return streamVisisbleProperties()
                .filter(property->Objects.equals(id, property.getId()))
                .findFirst();
    }

    public Optional<OneToManyAssociation> lookupCollection(String id) {
        return streamVisisbleCollections()
                .filter(collection->Objects.equals(id, collection.getId()))
                .findFirst();
    }
    
    
}
