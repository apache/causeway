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

import java.util.Optional;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkFollowSpecs;

import lombok.val;

public class ActionDescriptionReprRenderer
extends AbstractTypeMemberReprRenderer<ObjectAction> {

    public static LinkBuilder newLinkToBuilder(
            final IResourceContext resourceContext,
            final Rel rel,
            final ObjectSpecification objectSpecification,
            final ObjectAction objectAction) {
        final String domainType = objectSpecification.getLogicalTypeName();
        final String actionId = objectAction.getId();
        final String url = "domain-types/" + domainType + "/actions/" + actionId;
        return LinkBuilder.newBuilder(resourceContext, rel.getName(), RepresentationType.ACTION_DESCRIPTION, url);
    }

    public ActionDescriptionReprRenderer(
            final IResourceContext resourceContext,
            final LinkFollowSpecs linkFollower,
            final JsonRepresentation representation) {
        super(resourceContext, linkFollower, RepresentationType.ACTION_DESCRIPTION, representation);
    }

    @Override
    protected void addLinksSpecificToFeature() {
        addParameters();
        addLinkToReturnTypeIfAny();
        addLinkToElementTypeIfAny();
    }

    @Override
    protected void putExtensionsSpecificToFeature() {
        putExtensionsName();
        putExtensionsDescriptionIfAvailable();
    }

    // -- HELPER

    private void addParameters() {
        final JsonRepresentation parameterList = JsonRepresentation.newArray();
        val parameters = getObjectFeature().getParameters();
        for (final ObjectActionParameter parameter : parameters) {
            parameterList.arrayAdd(
                    ActionParameterDescriptionReprRenderer
                    .newLinkToBuilder(getResourceContext(), Rel.ACTION_PARAM, objectSpecification, parameter)
                    .build());
        }

        representation.mapPutJsonRepresentation("parameters", parameterList);
    }

    private void addLinkToElementTypeIfAny() {
        Optional.ofNullable(getObjectFeature().getElementType())
        .ifPresent(typeOfSpec->
            getLinks().arrayAdd(
                DomainTypeReprRenderer
                .newLinkToBuilder(getResourceContext(), Rel.ELEMENT_TYPE, typeOfSpec)
                .build()));
    }

    private void addLinkToReturnTypeIfAny() {
        val returnTypeSpec = getObjectFeature().getReturnType();
        if (returnTypeSpec == null) {
            return;
        }
        getLinks().arrayAdd(
                DomainTypeReprRenderer
                .newLinkToBuilder(getResourceContext(), Rel.RETURN_TYPE, returnTypeSpec)
                .build());
    }

}