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

import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkFollowSpecs;

import lombok.val;

public class ActionParameterDescriptionReprRenderer
extends AbstractTypeFeatureReprRenderer<ObjectActionParameter> {

    public static LinkBuilder newLinkToBuilder(
            final IResourceContext resourceContext,
            final Rel rel,
            final ObjectSpecification objectSpecification,
            final ObjectActionParameter objectActionParameter) {
        final String domainType = objectSpecification.getLogicalTypeName();
        final ObjectAction objectAction = objectActionParameter.getAction();
        final String actionId = objectAction.getId();
        final String paramName = objectActionParameter
                .getStaticFriendlyName()
                .orElseThrow(_Exceptions::unexpectedCodeReach);;
        final String url = String.format("domain-types/%s/actions/%s/params/%s", domainType, actionId, paramName);
        return LinkBuilder.newBuilder(resourceContext, rel.andParam("id", deriveId(objectActionParameter)), RepresentationType.ACTION_PARAMETER_DESCRIPTION, url);
    }

    public ActionParameterDescriptionReprRenderer(
            final IResourceContext resourceContext,
            final LinkFollowSpecs linkFollower,
            final JsonRepresentation representation) {
        super(resourceContext, linkFollower, RepresentationType.ACTION_PARAMETER_DESCRIPTION, representation);
    }

    @Override
    public ActionParameterDescriptionReprRenderer with(final ParentSpecAndFeature<ObjectActionParameter> specAndFeature) {
        super.with(specAndFeature);

        // done eagerly so can use as criteria for x-ro-follow-links
        representation.mapPutString("id", deriveId());

        return this;
    }

    protected String deriveId() {
        return deriveId(getObjectFeature());
    }

    private static String deriveId(final ObjectActionParameter objectActionParameter) {
        val named = objectActionParameter
                .getStaticFriendlyName()
                .orElseThrow(_Exceptions::unexpectedCodeReach);
        return objectActionParameter.getAction().getId() + "-" + named;
    }

    @Override
    protected void addLinkSelfIfRequired() {
        if (!includesSelf) {
            return;
        }
        getLinks().arrayAdd(newLinkToBuilder(getResourceContext(), Rel.SELF, getParentSpecification(), getObjectFeature()).build());
    }

    @Override
    protected void addLinkUpToParent() {
        final ObjectAction parentAction = this.objectFeature.getAction();

        final LinkBuilder parentLinkBuilder = ActionDescriptionReprRenderer.newLinkToBuilder(resourceContext, Rel.UP, objectSpecification, parentAction);
        getLinks().arrayAdd(parentLinkBuilder.build());
    }

    @Override
    protected void addPropertiesSpecificToFeature() {
        representation.mapPutString("name", getObjectFeature()
                .getStaticFriendlyName()
                .orElseThrow(_Exceptions::unexpectedCodeReach));
        representation.mapPutInt("number", getObjectFeature().getParameterIndex());
        representation.mapPutBoolean("optional", getObjectFeature().isOptional());

        Facets.maxLength(getObjectFeature())
            .ifPresent(maxLength->representation.mapPutInt("maxLength", maxLength));
    }

    @Override
    protected void addLinksSpecificToFeature() {
        final LinkBuilder linkBuilder = DomainTypeReprRenderer.newLinkToBuilder(resourceContext, Rel.RETURN_TYPE, objectFeature.getElementType());
        getLinks().arrayAdd(linkBuilder.build());
    }

    @Override
    protected void putExtensionsSpecificToFeature() {
        putExtensionsName();
        putExtensionsDescriptionIfAvailable();
    }

}