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

import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "bind")
public class ObjectBinding {
    
    @Getter
    private final ManagedObject managedObject;
    
    public String getTitle() {
        return managedObject.getSpecification().getTitle(null, managedObject);
    }

//    @Deprecated
//    public Stream<OneToOneAssociation> streamVisisbleProperties() {
//        return managedObject.getSpecification()
//        .streamAssociations(Contributed.INCLUDED)
//        .filter(objMember->objMember.getFeatureType().isProperty())
//        //TODO filter visibility
//        .map(OneToOneAssociation.class::cast);
//    }
//    
//    @Deprecated
//    public Stream<OneToManyAssociation> streamVisisbleCollections() {
//        return managedObject.getSpecification()
//        .streamAssociations(Contributed.INCLUDED)
//        .filter(objMember->objMember.getFeatureType().isCollection())
//        //TODO filter visibility
//        .map(OneToManyAssociation.class::cast);
//    }
//    
//    @Deprecated
//    public Stream<ObjectAction> streamVisisbleActions() {
//        return managedObject.getSpecification()
//        .streamObjectActions(Contributed.INCLUDED)
//        .filter(objMember->objMember.getFeatureType().isAction())
//        //TODO filter visibility
//        .map(ObjectAction.class::cast);
//    }
//    
//    public Optional<ObjectAction> lookupAction(String actionId) {
//        return streamVisisbleActions()
//                .filter(action->Objects.equals(actionId, action.getId()))
//                .findFirst();
//    }
//    
//    public Optional<OneToOneAssociation> lookupVisibleProperty(String propertyId) {
//        return streamVisisbleProperties()
//                .filter(property->Objects.equals(propertyId, property.getId()))
//                .findFirst();
//    }
//
//    public Optional<OneToManyAssociation> lookupCollection(String collectionId) {
//        return streamVisisbleCollections()
//                .filter(collection->Objects.equals(collectionId, collection.getId()))
//                .findFirst();
//    }
    
}
