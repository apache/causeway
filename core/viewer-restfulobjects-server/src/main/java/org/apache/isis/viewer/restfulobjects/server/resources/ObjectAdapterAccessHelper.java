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
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.*;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.MemberType;
import org.apache.isis.viewer.restfulobjects.server.ResourceContext;
import org.apache.isis.viewer.restfulobjects.server.RestfulObjectsApplicationException;

/**
 * Utility class that encapsulates the logic for checking access to the specified
 * {@link org.apache.isis.core.metamodel.adapter.ObjectAdapter object}'s members.
 */
public class ObjectAdapterAccessHelper {

    static enum Intent {
        ACCESS, MUTATE;

        public boolean isMutate() {
            return this == MUTATE;
        }
    }

    private final ObjectAdapter objectAdapter;
    private final ResourceContext resourceContext;

    public ObjectAdapterAccessHelper(ResourceContext resourceContext, ObjectAdapter objectAdapter) {
        this.objectAdapter = objectAdapter;
        this.resourceContext = resourceContext;
    }

    public OneToOneAssociation getPropertyThatIsVisibleForIntent(
            final String propertyId, final Intent intent) {

        final Where where = resourceContext.getWhere();

        final ObjectAssociation association;
        try {
            final ObjectSpecification specification = objectAdapter.getSpecification();
            association = specification.getAssociation(propertyId);
        } catch(Exception ex) {
            // fall through
            Util.throwNotFoundException(propertyId, MemberType.PROPERTY);
            return null; // to keep compiler happy.
        }

        if (association == null || !association.isOneToOneAssociation()) {
            Util.throwNotFoundException(propertyId, MemberType.PROPERTY);
        }

        final OneToOneAssociation property = (OneToOneAssociation) association;
        return memberThatIsVisibleForIntent(property, MemberType.PROPERTY, intent);
    }

    public OneToManyAssociation getCollectionThatIsVisibleForIntent(
            final String collectionId, final Intent intent) {

        final Where where = resourceContext.getWhere();
        final ObjectAssociation association;
        try {
            final ObjectSpecification specification = objectAdapter.getSpecification();
            association = specification.getAssociation(collectionId);
        } catch(Exception ex) {
            // fall through
            Util.throwNotFoundException(collectionId, MemberType.COLLECTION);
            return null; // to keep compiler happy.
        }
        if (association == null || !association.isOneToManyAssociation()) {
            Util.throwNotFoundException(collectionId, MemberType.COLLECTION);
        }
        final OneToManyAssociation collection = (OneToManyAssociation) association;
        return memberThatIsVisibleForIntent(collection, MemberType.COLLECTION, intent);
    }

    public ObjectAction getObjectActionThatIsVisibleForIntent(
            final String actionId, final Intent intent) {

        final Where where = resourceContext.getWhere();

        final ObjectAction action;
        try {
            final ObjectSpecification specification = objectAdapter.getSpecification();
            action = specification.getObjectAction(actionId);
        } catch(Exception ex) {
            Util.throwNotFoundException(actionId, MemberType.ACTION);
            return null; // to keep compiler happy.
        }
        if (action == null) {
            Util.throwNotFoundException(actionId, MemberType.ACTION);
        }
        return memberThatIsVisibleForIntent(action, MemberType.ACTION, intent);
    }

    public <T extends ObjectMember> T memberThatIsVisibleForIntent(
            final T objectMember, final MemberType memberType, final Intent intent) {

        final Where where = resourceContext.getWhere();

        final String memberId = objectMember.getId();
        final AuthenticationSession authenticationSession = resourceContext.getAuthenticationSession();
        if (objectMember.isVisible(authenticationSession, objectAdapter, where).isVetoed()) {
            Util.throwNotFoundException(memberId, memberType);
        }
        if (intent.isMutate()) {
            final Consent usable = objectMember.isUsable(authenticationSession, objectAdapter, where);
            if (usable.isVetoed()) {
                throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.FORBIDDEN, usable.getReason());
            }
        }
        return objectMember;
    }


}
