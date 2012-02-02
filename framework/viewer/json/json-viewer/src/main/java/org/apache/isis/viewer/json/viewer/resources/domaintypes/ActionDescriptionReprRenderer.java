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
package org.apache.isis.viewer.json.viewer.resources.domaintypes;

import java.util.List;

import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.links.Rel;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.json.viewer.representations.LinkFollower;
import org.apache.isis.viewer.json.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererFactoryAbstract;

public class ActionDescriptionReprRenderer extends AbstractTypeMemberReprRenderer<ActionDescriptionReprRenderer, ObjectAction> {

    public static class Factory extends ReprRendererFactoryAbstract {

        public Factory() {
            super(RepresentationType.ACTION_DESCRIPTION);
        }

        @Override
        public ReprRenderer<?, ?> newRenderer(final ResourceContext resourceContext, final LinkFollower linkFollower, final JsonRepresentation representation) {
            return new ActionDescriptionReprRenderer(resourceContext, linkFollower, getRepresentationType(), representation);
        }
    }

    public static LinkBuilder newLinkToBuilder(final ResourceContext resourceContext, final Rel rel, final ObjectSpecification objectSpecification, final ObjectAction objectAction) {
        final String typeFullName = objectSpecification.getFullIdentifier();
        final String actionId = objectAction.getId();
        final String url = "domainTypes/" + typeFullName + "/actions/" + actionId;
        return LinkBuilder.newBuilder(resourceContext, rel, RepresentationType.ACTION_DESCRIPTION, url);
    }

    public ActionDescriptionReprRenderer(final ResourceContext resourceContext, final LinkFollower linkFollower, final RepresentationType representationType, final JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
    }

    @Override
    protected void addLinksSpecificToFeature() {
        addParameters();
        addLinkToReturnTypeIfAny();
        addLinkToElementTypeIfAny();
    }

    private void addParameters() {
        final JsonRepresentation parameterList = JsonRepresentation.newArray();
        final List<ObjectActionParameter> parameters = getObjectFeature().getParameters();
        for (final ObjectActionParameter parameter : parameters) {
            final LinkBuilder linkBuilder = ActionParameterDescriptionReprRenderer.newLinkToBuilder(getResourceContext(), Rel.ACTION_PARAM, objectSpecification, parameter);
            parameterList.arrayAdd(linkBuilder.build());
        }

        representation.mapPut("parameters", parameterList);
    }

    protected void addLinkToElementTypeIfAny() {
        final TypeOfFacet facet = getObjectFeature().getFacet(TypeOfFacet.class);
        if (facet == null) {
            return;
        }
        final ObjectSpecification typeOfSpec = facet.valueSpec();
        final LinkBuilder linkBuilder = DomainTypeReprRenderer.newLinkToBuilder(getResourceContext(), Rel.ELEMENT_TYPE, typeOfSpec);
        getLinks().arrayAdd(linkBuilder.build());
    }

    private void addLinkToReturnTypeIfAny() {
        final ObjectSpecification returnType = getObjectFeature().getReturnType();
        if (returnType == null) {
            return;
        }
        final LinkBuilder linkBuilder = DomainTypeReprRenderer.newLinkToBuilder(getResourceContext(), Rel.RETURN_TYPE, returnType);
        getLinks().arrayAdd(linkBuilder.build());
    }

    @Override
    protected void putExtensionsSpecificToFeature() {
        putExtensionsName();
        putExtensionsDescriptionIfAvailable();
    }

}