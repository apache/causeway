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

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public final class ManagedCollection extends ManagedMember {

    // -- FACTORIES
    
    public static final ManagedCollection of(
            final @NonNull ManagedObject owner, 
            final @NonNull OneToManyAssociation collection) {
        return new ManagedCollection(owner, collection);
    }
    
    public static final Optional<ManagedCollection> lookupCollection(
            @NonNull final ManagedObject owner,
            @NonNull final String memberId) {
        
        return ManagedMember.<OneToManyAssociation>lookup(owner, MemberType.COLLECTION, memberId)
        .map(objectAction -> of(owner, objectAction));
    }
    
    // -- IMPLEMENTATION
    
    @Getter private final OneToManyAssociation collection;
    
    private ManagedCollection(
            final @NonNull ManagedObject owner, 
            final @NonNull OneToManyAssociation collection) {
        
        super(owner);
        this.collection = collection;
    }

    @Override
    public OneToManyAssociation getMember() {
        return getCollection();
    }

    @Override
    public MemberType getMemberType() {
        return MemberType.COLLECTION;
    }

    public ManagedObject getCollectionValue() {
        val collection = getCollection();
        
        return Optional.ofNullable(collection.get(getOwner()))
        .orElse(ManagedObject.of(collection.getSpecification(), null));
    }

}
