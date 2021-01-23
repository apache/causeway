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
package org.apache.isis.core.metamodel.specloader.specimpl;

import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.Hierarchical;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionContainer;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationContainer;

import lombok.val;

/**
 * Responsibility: member lookup and streaming with support for inheritance, 
 * based on access to declared members, super-classes and interfaces.
 */
public abstract class ObjectMemberContainer
extends FacetHolderImpl 
implements 
    ObjectActionContainer,
    ObjectAssociationContainer, 
    Hierarchical {

    // -- ACTIONS
    
    @Override
    public Optional<ObjectAction> getAction(String id, @Nullable ActionType type) {

        if(isTypeHierarchyRoot()) {
            return Optional.empty(); // stop search as we reached the Object class, which does not contribute actions 
        }
        
        val declaredAction = getDeclaredAction(id); // no inheritance nor type considered
                
        if(declaredAction.isPresent()) {
            // action found but if its not the right type, stop searching
            if(type!=null
                    && declaredAction.get().getType() != type) {
                return Optional.empty();
            }
            return declaredAction; 
        }
        
        if(superclass()==null) {
            // guard against unexpected reach of type hierarchy root
            return Optional.empty();
        }
        
        return superclass().getAction(id, type);
        
        //XXX future extensions should also search the interfaces, 
        // but avoid doing redundant work when walking the type-hierarchy;
        // (this elegant recursive solution will then need some tweaks to be efficient)
    }
    
    @Override
    public Stream<ObjectAction> streamActions(
            final ImmutableEnumSet<ActionType> types, 
            final MixedIn contributed) {
        //FIXME poorly implemented, inheritance not supported
        return streamDeclaredActions(contributed);
    }
    
    // -- ASSOCIATIONS
    
    @Override
    public Optional<ObjectAssociation> getAssociation(String id) {

        if(isTypeHierarchyRoot()) {
            return Optional.empty(); // stop search as we reached the Object class, which does not contribute actions 
        }
        
        val declaredAssociation = getDeclaredAssociation(id); // no inheritance considered
                
        if(declaredAssociation.isPresent()) {
            return declaredAssociation; 
        }
        
        if(superclass()==null) {
            // guard against unexpected reach of type hierarchy root
            return Optional.empty();
        }
        
        return superclass().getAssociation(id);
        
        //XXX future extensions should also search the interfaces, 
        // but avoid doing redundant work when walking the type-hierarchy;
        // (this elegant recursive solution will then need some tweaks to be efficient)
    }
    
    @Override
    public Stream<ObjectAssociation> streamAssociations(MixedIn contributed) {
        //FIXME poorly implemented, inheritance not supported
        return streamDeclaredAssociations(contributed);
    }
    
}
