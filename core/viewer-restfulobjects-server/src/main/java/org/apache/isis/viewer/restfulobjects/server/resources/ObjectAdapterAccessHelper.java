/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.restfulobjects.server.resources;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.rendering.RendererContext;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.MemberType;

/**
 * Utility class that encapsulates the logic for checking access to the specified
 * {@link org.apache.isis.core.metamodel.adapter.ObjectAdapter object}'s members.
 */
public class ObjectAdapterAccessHelper {

    public static void throwNotFoundException(final String memberId, final MemberType memberType) {
        final String memberTypeStr = memberType.name().toLowerCase();
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.NOT_FOUND, "%s '%s' either does not exist or is not visible", memberTypeStr, memberId);
    }

    static enum Intent {
        ACCESS, MUTATE;

        public boolean isMutate() {
            return this == MUTATE;
        }
    }

    private final ObjectAdapter objectAdapter;
    private final RendererContext rendererContext;

    public ObjectAdapterAccessHelper(RendererContext rendererContext, ObjectAdapter objectAdapter) {
        this.objectAdapter = objectAdapter;
        this.rendererContext = rendererContext;
    }

    public OneToOneAssociation getPropertyThatIsVisibleForIntent(
            final String propertyId, final Intent intent) {

        final Where where = rendererContext.getWhere();

        final ObjectAssociation association;
        try {
            final ObjectSpecification specification = objectAdapter.getSpecification();
            association = specification.getAssociation(propertyId);
        } catch(Exception ex) {
            // fall through
            throwNotFoundException(propertyId, MemberType.PROPERTY);
            return null; // to keep compiler happy.
        }

        if (association == null || !association.isOneToOneAssociation()) {
            throwNotFoundException(propertyId, MemberType.PROPERTY);
        }

        final OneToOneAssociation property = (OneToOneAssociation) association;
        return memberThatIsVisibleForIntent(property, MemberType.PROPERTY, intent);
    }

    public OneToManyAssociation getCollectionThatIsVisibleForIntent(
            final String collectionId, final Intent intent) {

        final Where where = rendererContext.getWhere();
        final ObjectAssociation association;
        try {
            final ObjectSpecification specification = objectAdapter.getSpecification();
            association = specification.getAssociation(collectionId);
        } catch(Exception ex) {
            // fall through
            throwNotFoundException(collectionId, MemberType.COLLECTION);
            return null; // to keep compiler happy.
        }
        if (association == null || !association.isOneToManyAssociation()) {
            throwNotFoundException(collectionId, MemberType.COLLECTION);
        }
        final OneToManyAssociation collection = (OneToManyAssociation) association;
        return memberThatIsVisibleForIntent(collection, MemberType.COLLECTION, intent);
    }

    public ObjectAction getObjectActionThatIsVisibleForIntent(
            final String actionId, final Intent intent) {

        final Where where = rendererContext.getWhere();

        final ObjectAction action;
        try {
            final ObjectSpecification specification = objectAdapter.getSpecification();
            action = specification.getObjectAction(actionId);
        } catch(Exception ex) {
            throwNotFoundException(actionId, MemberType.ACTION);
            return null; // to keep compiler happy.
        }
        if (action == null) {
            throwNotFoundException(actionId, MemberType.ACTION);
        }
        return memberThatIsVisibleForIntent(action, MemberType.ACTION, intent);
    }

    public <T extends ObjectMember> T memberThatIsVisibleForIntent(
            final T objectMember, final MemberType memberType, final Intent intent) {

        final Where where = rendererContext.getWhere();

        final String memberId = objectMember.getId();
        final Consent visibilityConsent =
                objectMember.isVisible(
                        objectAdapter, InteractionInitiatedBy.USER, where);
        if (visibilityConsent.isVetoed()) {
            throwNotFoundException(memberId, memberType);
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
