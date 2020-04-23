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
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.common.model.binding.interaction.ObjectInteractor.AccessIntent;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.MemberType;

import lombok.val;

/**
 * Utility class that encapsulates the logic for checking access to the specified
 * {@link ManagedObject}'s members.
 */
public class ObjectAdapterAccessHelper {

    public static RestfulObjectsApplicationException notFoundException(final String memberId, final MemberType memberType) {
        final String memberTypeStr = memberType.name().toLowerCase();
        return RestfulObjectsApplicationException.createWithMessage(
                RestfulResponse.HttpStatusCode.NOT_FOUND, 
                "%s '%s' either does not exist, is disabled or is not visible", 
                memberTypeStr,
                memberId);
    }

    private final ManagedObject objectAdapter;
    private final IResourceContext resourceContext;

    public ObjectAdapterAccessHelper(IResourceContext resourceContext, ManagedObject objectAdapter) {
        this.objectAdapter = objectAdapter;
        this.resourceContext = resourceContext;
    }

    public OneToOneAssociation getPropertyThatIsVisibleForIntent(
            final String propertyId, final AccessIntent intent) {

        val spec = objectAdapter.getSpecification();
        val association = spec.getAssociation(propertyId)
                .orElseThrow(()->notFoundException(propertyId, MemberType.PROPERTY));

        if (!association.isOneToOneAssociation()) {
            throw notFoundException(propertyId, MemberType.PROPERTY);
        }

        final OneToOneAssociation property = (OneToOneAssociation) association;
        return memberThatIsVisibleForIntent(property, MemberType.PROPERTY, intent);
    }

    public OneToManyAssociation getCollectionThatIsVisibleForIntent(
            final String collectionId, final AccessIntent intent) {

        val spec = objectAdapter.getSpecification();
        val association = spec.getAssociation(collectionId)
                .orElseThrow(()->notFoundException(collectionId, MemberType.COLLECTION));
        
        if (!association.isOneToManyAssociation()) {
            throw notFoundException(collectionId, MemberType.COLLECTION);
        }
        
        final OneToManyAssociation collection = (OneToManyAssociation) association;
        return memberThatIsVisibleForIntent(collection, MemberType.COLLECTION, intent);
    }

    public ObjectAction getObjectActionThatIsVisibleForIntent(
            final String actionId, final AccessIntent intent) {

        val spec = objectAdapter.getSpecification();
        val action = spec.getObjectAction(actionId)
                .orElseThrow(()->notFoundException(actionId, MemberType.ACTION));

        return memberThatIsVisibleForIntent(action, MemberType.ACTION, intent);
    }

    public <T extends ObjectMember> T memberThatIsVisibleForIntent(
            final T objectMember, final MemberType memberType, final AccessIntent intent) {

        final Where where = resourceContext.getWhere();

        final String memberId = objectMember.getId();
        final Consent visibilityConsent =
                objectMember.isVisible(
                        objectAdapter, InteractionInitiatedBy.USER, where);
        if (visibilityConsent.isVetoed()) {
            throw notFoundException(memberId, memberType);
        }
        if (intent.isMutate()) {
            final Consent usabilityConsent = objectMember.isUsable(
                    objectAdapter, InteractionInitiatedBy.USER, where
                    );
            if (usabilityConsent.isVetoed()) {
                throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.FORBIDDEN,
                        usabilityConsent.getReason());
            }
        }
        return objectMember;
    }


}
