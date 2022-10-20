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

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkFollowSpecs;

import lombok.val;

public class PropertyDescriptionReprRenderer
extends AbstractTypeMemberReprRenderer<OneToOneAssociation> {

    public static LinkBuilder newLinkToBuilder(
            final IResourceContext resourceContext,
            final Rel rel,
            final ObjectSpecification objectSpecification,
            final OneToOneAssociation property) {
        final String domainType = objectSpecification.getLogicalTypeName();
        final String propertyId = property.getId();
        final String url = "domain-types/" + domainType + "/properties/" + propertyId;
        return LinkBuilder.newBuilder(resourceContext, rel.getName(), RepresentationType.PROPERTY_DESCRIPTION, url);
    }

    public PropertyDescriptionReprRenderer(
            final IResourceContext resourceContext,
            final LinkFollowSpecs linkFollower,
            final JsonRepresentation representation) {
        super(resourceContext, linkFollower, RepresentationType.PROPERTY_DESCRIPTION, representation);
    }

    @Override
    protected void addLinksSpecificToFeature() {
        addLinkToReturnTypeIfAny();
    }

    @Override
    protected void addPropertiesSpecificToFeature() {
        representation.mapPutBoolean("optional", !getObjectFeature().isMandatory());
        Facets.maxLength(getObjectFeature())
            .ifPresent(maxLength->representation.mapPutInt("maxLength", maxLength));
    }


    @Override
    protected void putExtensionsSpecificToFeature() {
        putExtensionsName();
        putExtensionsDescriptionIfAvailable();
    }

    // -- HELPER

    private void addLinkToReturnTypeIfAny() {
        val returnTypeSpec = getObjectFeature().getElementType();
        if (returnTypeSpec == null) {
            return;
        }
        getLinks().arrayAdd(
                DomainTypeReprRenderer
                .newLinkToBuilder(getResourceContext(), Rel.RETURN_TYPE, returnTypeSpec)
                .build());
    }


}