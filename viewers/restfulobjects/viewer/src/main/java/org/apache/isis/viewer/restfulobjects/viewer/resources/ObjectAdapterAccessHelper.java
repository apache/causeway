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
package org.apache.isis.viewer.restfulobjects.viewer.resources;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.spec.interaction.ManagedAction;
import org.apache.isis.core.metamodel.spec.interaction.ManagedCollection;
import org.apache.isis.core.metamodel.spec.interaction.ManagedProperty;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Utility class that encapsulates the logic for checking access to the specified
 * {@link ManagedObject}'s members.
 */
@RequiredArgsConstructor
public class ObjectAdapterAccessHelper {
    
    public static enum AccessIntent {
        ACCESS, MUTATE;

        public boolean isMutate() {
            return this == MUTATE;
        }
    }

    public static ObjectAdapterAccessHelper of(
            final IResourceContext resourceContext,
            final ManagedObject managedObject) {
        return new ObjectAdapterAccessHelper(
                managedObject,
                resourceContext.getWhere());
    }

    private final ManagedObject managedObject;
    private final Where where;
    
    public ObjectAction getObjectActionThatIsVisibleForIntent(
            final String actionId, final AccessIntent intent) {
        
        val actionHandle = ManagedAction
                .getActionHandle(managedObject, actionId)
                .checkVisibility(where);
        
        if(intent.isMutate()) {
            actionHandle.checkUsability(where);
        }
        
        return actionHandle
                .getOrElseThrow(InteractionFailureHandler::onFailure)
                .getAction();
    }

    public OneToOneAssociation getPropertyThatIsVisibleForIntent(
            final String propertyId, final AccessIntent intent) {
        
        val propertyHandle = ManagedProperty
                .getPropertyHandle(managedObject, propertyId)
                .checkVisibility(where);
        
        if(intent.isMutate()) {
            propertyHandle.checkUsability(where);
        }
        
        return propertyHandle
                .getOrElseThrow(InteractionFailureHandler::onFailure)
                .getProperty();
        
    }

    public OneToManyAssociation getCollectionThatIsVisibleForIntent(
            final String collectionId, final AccessIntent intent) {
        
        val collectionHandle = ManagedCollection
                .getCollectionHandle(managedObject, collectionId)
                .checkVisibility(where);
        
        if(intent.isMutate()) {
            collectionHandle.checkUsability(where);
        }
        
        return collectionHandle
                .getOrElseThrow(InteractionFailureHandler::onFailure)
                .getCollection();
    }


}
