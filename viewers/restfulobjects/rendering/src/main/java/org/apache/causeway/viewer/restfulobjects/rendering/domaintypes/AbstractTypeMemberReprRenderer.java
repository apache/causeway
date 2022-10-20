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
package org.apache.causeway.viewer.restfulobjects.rendering.domaintypes;

import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.MemberType;

public abstract class AbstractTypeMemberReprRenderer<T extends ObjectMember>
extends AbstractTypeFeatureReprRenderer<T> {

    protected MemberType memberType;

    public AbstractTypeMemberReprRenderer(
            final IResourceContext resourceContext,
            final LinkFollowSpecs linkFollower,
            final RepresentationType representationType,
            final JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
    }

    /**
     * null if the feature is an object action param.
     */
    public MemberType getMemberType() {
        return memberType;
    }

    @Override
    public AbstractTypeMemberReprRenderer<T> with(final ParentSpecAndFeature<T> specAndFeature) {
        super.with(specAndFeature);
        memberType = MemberType.determineFrom(objectFeature);

        // done eagerly so can use as criteria for x-ro-follow-links
        representation.mapPutString("id", objectFeature.getId());
        representation.mapPutString("memberType", memberType.getName());

        return this;
    }

    @Override
    protected void addLinkUpToParent() {
        final LinkBuilder parentLinkBuilder = DomainTypeReprRenderer.newLinkToBuilder(resourceContext, Rel.UP, objectSpecification);
        getLinks().arrayAdd(parentLinkBuilder.build());
    }

    @Override
    protected void addLinkSelfIfRequired() {
        if (!includesSelf) {
            return;
        }

        final ObjectMember objectMember = getObjectFeature();
        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(
                getResourceContext(), Rel.SELF.getName(), getMediaType(),
                "domain-types/%s/%s%s", getParentSpecification().getLogicalTypeName(), getMemberType().getUrlPart(), objectMember.getId());
        getLinks().arrayAdd(linkBuilder.build());
    }

}