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

import org.apache.isis.core.commons.internal.base._Either;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.interaction.ManagedMember.MemberType;

import lombok.NonNull;
import lombok.val;

public final class CollectionInteraction extends MemberInteraction<ManagedCollection, CollectionInteraction> {

    public static final CollectionInteraction start(
            @NonNull final ManagedObject owner,
            @NonNull final String memberId) {
    
        val managedCollection = ManagedCollection.lookupCollection(owner, memberId);
        
        final _Either<ManagedCollection, InteractionVeto> chain = managedCollection.isPresent()
                ? _Either.left(managedCollection.get())
                : _Either.right(InteractionVeto.notFound(MemberType.COLLECTION, memberId));
                
        return new CollectionInteraction(chain);
    }
    
    CollectionInteraction(@NonNull _Either<ManagedCollection, InteractionVeto> chain) {
        super(chain);
    }

//    public PropertyHandle modifyProperty(
//            @NonNull final Function<ManagedProperty, ManagedObject> newProperyValueProvider) {
//
//        chain = chain.leftRemap(property->{
//            val validityVeto = property.modifyProperty(newProperyValueProvider.apply(property));
//            return validityVeto.isPresent()
//                ? _Either.right(validityVeto.get()) 
//                : _Either.left(property); 
//        });
//        return this;
//    }

    
}

