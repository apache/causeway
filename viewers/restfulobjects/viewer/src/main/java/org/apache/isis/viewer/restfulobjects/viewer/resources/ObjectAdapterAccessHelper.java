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
import org.apache.isis.viewer.common.model.binding.interaction.InteractionResponse;
import org.apache.isis.viewer.common.model.binding.interaction.ObjectInteractor;
import org.apache.isis.viewer.common.model.binding.interaction.ObjectInteractor.AccessIntent;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.MemberType;

import lombok.RequiredArgsConstructor;
import lombok.val;

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
                ObjectInteractor.bind(managedObject),
                resourceContext.getWhere());
    }

    private final ObjectInteractor objectInteractor;
    private final Where where;

    public OneToOneAssociation getPropertyThatIsVisibleForIntent(
            final String propertyId, final AccessIntent intent) {

        val propertyInteractor = objectInteractor.getPropertyInteractor();

        val check = propertyInteractor.getPropertyThatIsVisibleForIntent(propertyId, where, intent);
        check.right()
        .ifPresent(failure->handleFailure(failure, propertyId, MemberType.PROPERTY));

        return check.leftIfAny();
    }

    public OneToManyAssociation getCollectionThatIsVisibleForIntent(
            final String collectionId, final AccessIntent intent) {

        val propertyInteractor = objectInteractor.getCollectionInteractor();

        val check = propertyInteractor.getPropertyThatIsVisibleForIntent(collectionId, where, intent);
        check.right()
        .ifPresent(failure->handleFailure(failure, collectionId, MemberType.COLLECTION));

        return check.leftIfAny();
    }

    public ObjectAction getObjectActionThatIsVisibleForIntent(
            final String actionId, final AccessIntent intent) {

        val actionInteractor = objectInteractor.getActionInteractor();

        val check = actionInteractor.getActionThatIsVisibleForIntent(actionId, where, intent);
        check.right()
        .ifPresent(failure->handleFailure(failure, actionId, MemberType.ACTION));

        return check.leftIfAny();
    }

    private void handleFailure(final InteractionResponse failure, String memberId, MemberType memberType) {
        switch(failure.getVeto()) {
        case NOT_FOUND:
        case HIDDEN:
            throw RestfulObjectsApplicationException
            .createWithMessage(RestfulResponse.HttpStatusCode.NOT_FOUND,
                    failure.getFailureMessage());
        case UNAUTHORIZED:
        case FORBIDDEN:
            throw RestfulObjectsApplicationException
            .createWithMessage(RestfulResponse.HttpStatusCode.FORBIDDEN,
                    failure.getFailureMessage());
        }
    }

}
