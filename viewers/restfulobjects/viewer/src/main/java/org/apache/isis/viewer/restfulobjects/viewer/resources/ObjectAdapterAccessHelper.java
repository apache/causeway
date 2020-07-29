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
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.CollectionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction.SemanticConstraint;
import org.apache.isis.core.metamodel.interactions.managed.MemberInteraction.AccessIntent;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Utility class that encapsulates the logic for checking access to the specified
 * {@link ManagedObject}'s members.
 */
@RequiredArgsConstructor
public class ObjectAdapterAccessHelper {
    
    public static ObjectAdapterAccessHelper of(
            final IResourceContext resourceContext,
            final ManagedObject managedObject) {
        return new ObjectAdapterAccessHelper(
                managedObject,
                resourceContext.getWhere());
    }

    private final ManagedObject managedObject;
    private final Where where;
    
    public ManagedAction getObjectActionThatIsVisibleForIntentAndSemanticConstraint(
            @NonNull final String actionId, 
            @NonNull final AccessIntent intent,
            @NonNull final SemanticConstraint semanticConstraint) {
        
        return ActionInteraction
                .start(managedObject, actionId)
                .checkVisibility(where)
                .checkUsability(where, AccessIntent.MUTATE)
                .checkSemanticConstraint(semanticConstraint)
                .getManagedActionElseThrow(InteractionFailureHandler::onFailure);
    }

    public ManagedProperty getPropertyThatIsVisibleForIntent(
            @NonNull final String propertyId, 
            @NonNull final AccessIntent intent) {
        
        return PropertyInteraction
                .start(managedObject, propertyId, where)
                .checkVisibility(where)
                .checkUsability(where, intent)
                .getManagedPropertyElseThrow(InteractionFailureHandler::onFailure);
        
    }

    public ManagedCollection getCollectionThatIsVisibleForIntent(
            @NonNull final String collectionId, 
            @NonNull final AccessIntent intent) {

        return CollectionInteraction
                .start(managedObject, collectionId)
                .checkVisibility(where)
                .checkUsability(where, intent)
                .getManagedCollectionElseThrow(InteractionFailureHandler::onFailure);

    }


}
