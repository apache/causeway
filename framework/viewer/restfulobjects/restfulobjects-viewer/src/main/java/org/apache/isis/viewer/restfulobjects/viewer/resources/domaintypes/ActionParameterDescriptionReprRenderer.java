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
package org.apache.isis.viewer.restfulobjects.viewer.resources.domaintypes;

import org.apache.isis.core.metamodel.facets.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.links.Rel;
import org.apache.isis.viewer.restfulobjects.viewer.ResourceContext;
import org.apache.isis.viewer.restfulobjects.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.restfulobjects.viewer.representations.LinkFollower;
import org.apache.isis.viewer.restfulobjects.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.representations.ReprRendererFactoryAbstract;

public class ActionParameterDescriptionReprRenderer extends AbstractTypeFeatureReprRenderer<ActionParameterDescriptionReprRenderer, ObjectActionParameter> {

    public static class Factory extends ReprRendererFactoryAbstract {

        public Factory() {
            super(RepresentationType.ACTION_PARAMETER_DESCRIPTION);
        }

        @Override
        public ReprRenderer<?, ?> newRenderer(final ResourceContext resourceContext, final LinkFollower linkFollower, final JsonRepresentation representation) {
            return new ActionParameterDescriptionReprRenderer(resourceContext, linkFollower, getRepresentationType(), representation);
        }
    }

    public static LinkBuilder newLinkToBuilder(final ResourceContext resourceContext, final Rel rel, final ObjectSpecification objectSpecification, final ObjectActionParameter objectActionParameter) {
        final String typeFullName = objectSpecification.getFullIdentifier();
        final ObjectAction objectAction = objectActionParameter.getAction();
        final String actionId = objectAction.getId();
        final String paramName = objectActionParameter.getName();
        final String url = String.format("domainTypes/%s/actions/%s/params/%s", typeFullName, actionId, paramName);
        return LinkBuilder.newBuilder(resourceContext, rel, RepresentationType.ACTION_PARAMETER_DESCRIPTION, url).withId(deriveId(objectActionParameter));
    }

    public ActionParameterDescriptionReprRenderer(final ResourceContext resourceContext, final LinkFollower linkFollower, final RepresentationType representationType, final JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
    }

    @Override
    public ActionParameterDescriptionReprRenderer with(final ParentSpecAndFeature<ObjectActionParameter> specAndFeature) {
        super.with(specAndFeature);

        // done eagerly so can use as criteria for x-ro-follow-links
        representation.mapPut("id", deriveId());

        return this;
    }

    protected String deriveId() {
        return deriveId(getObjectFeature());
    }

    private static String deriveId(final ObjectActionParameter objectActionParameter) {
        return objectActionParameter.getAction().getId() + "-" + objectActionParameter.getName();
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
        representation.mapPut("name", getObjectFeature().getName());
        representation.mapPut("number", getObjectFeature().getNumber());
        representation.mapPut("optional", getObjectFeature().isOptional());
        final MaxLengthFacet maxLength = getObjectFeature().getFacet(MaxLengthFacet.class);
        if (maxLength != null && !maxLength.isNoop()) {
            representation.mapPut("maxLength", maxLength.value());
        }
    }

    @Override
    protected void addLinksSpecificToFeature() {
        final LinkBuilder linkBuilder = DomainTypeReprRenderer.newLinkToBuilder(resourceContext, Rel.RETURN_TYPE, objectFeature.getSpecification());
        getLinks().arrayAdd(linkBuilder.build());
    }

    @Override
    protected void putExtensionsSpecificToFeature() {
        putExtensionsName();
        putExtensionsDescriptionIfAvailable();
    }

}