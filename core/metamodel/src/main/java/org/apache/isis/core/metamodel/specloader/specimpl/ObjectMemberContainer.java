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
import org.apache.isis.commons.internal.collections._Sets;
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
 * <p>
 * TODO performance: add memoization
 * <p>
 * TODO future extensions should also search the interfaces, 
 * but avoid doing redundant work when walking the type-hierarchy;
 * (current elegant recursive solution will then need some tweaks to be efficient)
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

        val declaredAction = getDeclaredAction(id); // no inheritance nor type considered
                
        if(declaredAction.isPresent()) {
            // action found but if its not the right type, stop searching
            if(type!=null
                    && declaredAction.get().getType() != type) {
                return Optional.empty();
            }
            return declaredAction; 
        }

        return isTypeHierarchyRoot()
                ? Optional.empty() // stop searching
                : superclass().getAction(id, type);
    }
    
    @Override
    public Stream<ObjectAction> streamActions(
            final ImmutableEnumSet<ActionType> types, 
            final MixedIn contributed) {
        
        if(isTypeHierarchyRoot()) {
            return streamDeclaredActions(contributed); // stop going deeper
        }
        
        val ids = _Sets.<String>newHashSet();
        
        return Stream.concat(
            streamDeclaredActions(contributed), 
            superclass().streamActions(contributed)
        )
        .filter(action->ids.add(action.getId())); // ensure we don't emit duplicates
    }
    
    // -- ASSOCIATIONS
    
    @Override
    public Optional<ObjectAssociation> getAssociation(String id) {

        val declaredAssociation = getDeclaredAssociation(id); // no inheritance considered
                
        if(declaredAssociation.isPresent()) {
            return declaredAssociation; 
        }
        
        return isTypeHierarchyRoot()
               ? Optional.empty() // stop searching 
               : superclass().getAssociation(id);
    }
    
    @Override
    public Stream<ObjectAssociation> streamAssociations(MixedIn contributed) {
        
        if(isTypeHierarchyRoot()) {
            return streamDeclaredAssociations(contributed); // stop going deeper
        }
        
        val ids = _Sets.<String>newHashSet();
        
        return Stream.concat(
                streamDeclaredAssociations(contributed), 
                superclass().streamAssociations(contributed)
        )
        .filter(association->ids.add(association.getId())); // ensure we don't emit duplicates
    }
    
}
