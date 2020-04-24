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

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.internal.base._Either;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.common.model.binding.interaction.InteractionResponse.Veto;
import org.apache.isis.viewer.common.model.binding.interaction.ObjectInteractor.AccessIntent;

import lombok.val;

public class CollectionInteractor extends MemberInteractor {

    public CollectionInteractor(ObjectInteractor objectInteractor) {
        super(objectInteractor);
    }

    public _Either<OneToManyAssociation, InteractionResponse> getPropertyThatIsVisibleForIntent(
            final String collectionId,
            final Where where,
            final AccessIntent intent) {

        val managedObject = objectInteractor.getManagedObject();
        
        val spec = managedObject.getSpecification();
        val collection = spec.getAssociation(collectionId).orElse(null);
        if(collection==null || !collection.isOneToOneAssociation()) {
            return super.notFound(MemberType.COLLECTION, collectionId, Veto.NOT_FOUND);
        }
        
        return super.memberThatIsVisibleForIntent(
                MemberType.COLLECTION,
                (OneToManyAssociation) collection, where, intent);
    }

}
