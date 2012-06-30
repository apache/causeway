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

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.links.Rel;
import org.apache.isis.viewer.restfulobjects.viewer.ResourceContext;
import org.apache.isis.viewer.restfulobjects.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.restfulobjects.viewer.representations.LinkFollower;
import org.apache.isis.viewer.restfulobjects.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.representations.ReprRendererFactoryAbstract;

public class CollectionDescriptionReprRenderer extends AbstractTypeMemberReprRenderer<CollectionDescriptionReprRenderer, OneToManyAssociation> {

    public static class Factory extends ReprRendererFactoryAbstract {

        public Factory() {
            super(RepresentationType.COLLECTION_DESCRIPTION);
        }

        @Override
        public ReprRenderer<?, ?> newRenderer(final ResourceContext resourceContext, final LinkFollower linkFollower, final JsonRepresentation representation) {
            return new CollectionDescriptionReprRenderer(resourceContext, linkFollower, getRepresentationType(), representation);
        }
    }

    public static LinkBuilder newLinkToBuilder(final ResourceContext resourceContext, final Rel rel, final ObjectSpecification objectSpecification, final OneToManyAssociation collection) {
        final String typeFullName = objectSpecification.getFullIdentifier();
        final String collectionId = collection.getId();
        final String url = "domainTypes/" + typeFullName + "/collections/" + collectionId;
        return LinkBuilder.newBuilder(resourceContext, rel, RepresentationType.COLLECTION_DESCRIPTION, url);
    }

    public CollectionDescriptionReprRenderer(final ResourceContext resourceContext, final LinkFollower linkFollower, final RepresentationType representationType, final JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
    }

    @Override
    protected void addLinksSpecificToFeature() {
        addLinkToElementTypeIfAny();
    }

    private void addLinkToElementTypeIfAny() {
        final ObjectSpecification elementType = getObjectFeature().getSpecification();
        if (elementType == null) {
            return;
        }
        final LinkBuilder linkBuilder = DomainTypeReprRenderer.newLinkToBuilder(getResourceContext(), Rel.ELEMENT_TYPE, elementType);
        getLinks().arrayAdd(linkBuilder.build());
    }

    @Override
    protected void putExtensionsSpecificToFeature() {
        putExtensionsName();
        putExtensionsDescriptionIfAvailable();
    }

}