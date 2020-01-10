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
package org.apache.isis.viewer.restfulobjects.rendering.domaintypes;

import org.apache.isis.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.isis.viewer.restfulobjects.rendering.LinkFollowSpecs;

public class PropertyDescriptionReprRenderer extends AbstractTypeMemberReprRenderer<PropertyDescriptionReprRenderer, OneToOneAssociation> {

    public static LinkBuilder newLinkToBuilder(final IResourceContext resourceContext, final Rel rel, final ObjectSpecification objectSpecification, final OneToOneAssociation property) {
        final String domainType = objectSpecification.getSpecId().asString();
        final String propertyId = property.getId();
        final String url = "domain-types/" + domainType + "/properties/" + propertyId;
        return LinkBuilder.newBuilder(resourceContext, rel.getName(), RepresentationType.PROPERTY_DESCRIPTION, url);
    }

    public PropertyDescriptionReprRenderer(final IResourceContext resourceContext, final LinkFollowSpecs linkFollower, final JsonRepresentation representation) {
        super(resourceContext, linkFollower, RepresentationType.PROPERTY_DESCRIPTION, representation);
    }

    @Override
    protected void addLinksSpecificToFeature() {
        addLinkToReturnTypeIfAny();
    }

    @Override
    protected void addPropertiesSpecificToFeature() {
        representation.mapPut("optional", !getObjectFeature().isMandatory());
        final MaxLengthFacet maxLength = getObjectFeature().getFacet(MaxLengthFacet.class);
        if (maxLength != null && !maxLength.isFallback()) {
            representation.mapPut("maxLength", maxLength.value());
        }
    }

    private void addLinkToReturnTypeIfAny() {
        final ObjectSpecification returnType = getObjectFeature().getSpecification();
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