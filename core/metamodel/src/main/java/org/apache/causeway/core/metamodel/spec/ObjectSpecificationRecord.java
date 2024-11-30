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
package org.apache.causeway.core.metamodel.spec;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.HasLogicalType;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionContainer;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociationContainer;
import org.apache.causeway.core.metamodel.spec.impl.ObjectMemberContainer;

//TODO[causeway-core-metamodel-CAUSEWAY-3834] WIP
public record ObjectSpecificationRecord(
        LogicalType logicalType, 
        FeatureType featureType, 
        FacetHolder facetHolder,
        Hierarchical hierarchical,
        ObjectActionContainer actionContainer,
        ObjectAssociationContainer associationContainer) 
implements
    HasLogicalType,
    Specification,
    ObjectMemberContainer
    //ObjectSpecification 
{
    
    // -- SPECIFICATION
    
    @Override public LogicalType getLogicalType() { return logicalType; }
    @Override public FeatureType getFeatureType() { return featureType; }
    @Override public FacetHolder getFacetHolder() { return facetHolder; }
    
    // -- HIERARCHICAL
    
    @Override public boolean hasSubclasses() {
        return hierarchical.hasSubclasses();
    }
    @Override public Can<ObjectSpecification> interfaces() {
        return hierarchical.interfaces();
    }
    @Override public boolean isOfType(ObjectSpecification other) {
        return hierarchical.isOfType(other);
    }
    @Override public boolean isOfTypeResolvePrimitive(ObjectSpecification other) {
        return hierarchical.isOfTypeResolvePrimitive(other);
    }
    @Override public Can<ObjectSpecification> subclasses(Depth depth) {
        return hierarchical.subclasses(depth);
    }
    @Override public ObjectSpecification superclass() {
        return hierarchical.superclass();
    }

    // -- ACTION CONTAINER
    
    @Override public Optional<ObjectAction> getAction(String id, ImmutableEnumSet<ActionScope> actionScopes, MixedIn mixedIn) {
        return actionContainer.getAction(id, actionScopes, mixedIn);
    }
    @Override public Optional<ObjectAction> getDeclaredAction(String id, ImmutableEnumSet<ActionScope> actionScopes, MixedIn mixedIn) {
        return actionContainer.getDeclaredAction(id, actionScopes, mixedIn);
    }
    @Override public Stream<ObjectAction> streamActions(ImmutableEnumSet<ActionScope> actionTypes, MixedIn mixedIn, Consumer<ObjectAction> onActionOverloaded) {
        return actionContainer.streamActions(actionTypes, mixedIn, onActionOverloaded);
    }
    @Override public Stream<ObjectAction> streamRuntimeActions(MixedIn mixedIn) {
        return actionContainer.streamRuntimeActions(mixedIn);
    }
    @Override public Stream<ObjectAction> streamActionsForColumnRendering(Identifier memberIdentifier) {
        return actionContainer.streamActionsForColumnRendering(memberIdentifier);
    }
    @Override public Stream<ObjectAction> streamDeclaredActions(ImmutableEnumSet<ActionScope> actionTypes, MixedIn mixedIn) {
        return actionContainer.streamDeclaredActions(actionTypes, mixedIn);
    }
    
    // -- ASSOCIATION CONTAINER
    
    @Override public Optional<ObjectAssociation> getAssociation(String id, MixedIn mixedIn) {
        return associationContainer.getAssociation(id, mixedIn);
    }
    @Override public Optional<ObjectAssociation> getDeclaredAssociation(String id, MixedIn mixedIn) {
        return associationContainer.getDeclaredAssociation(id, mixedIn);
    }
    @Override public Stream<ObjectAssociation> streamAssociations(MixedIn mixedIn) {
        return associationContainer.streamAssociations(mixedIn);
    }
    @Override public Stream<ObjectAssociation> streamAssociationsForColumnRendering(Identifier memberIdentifier, ManagedObject parentObject) {
        return associationContainer.streamAssociationsForColumnRendering(memberIdentifier, parentObject);
    }
    @Override public Stream<ObjectAssociation> streamDeclaredAssociations(MixedIn mixedIn) {
        return associationContainer.streamDeclaredAssociations(mixedIn);
    }

}
