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
package org.apache.isis.viewer.restfulobjects.viewer.resources.domainobjects;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.links.Rel;
import org.apache.isis.viewer.restfulobjects.viewer.ResourceContext;
import org.apache.isis.viewer.restfulobjects.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.restfulobjects.viewer.representations.LinkFollower;
import org.apache.isis.viewer.restfulobjects.viewer.representations.RendererFactory;
import org.apache.isis.viewer.restfulobjects.viewer.representations.RendererFactoryRegistry;
import org.apache.isis.viewer.restfulobjects.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.representations.ReprRendererFactoryAbstract;
import org.apache.isis.viewer.restfulobjects.viewer.resources.domaintypes.CollectionDescriptionReprRenderer;

public class ObjectCollectionReprRenderer extends AbstractObjectMemberReprRenderer<ObjectCollectionReprRenderer, OneToManyAssociation> {

    public static class Factory extends ReprRendererFactoryAbstract {

        public Factory() {
            super(RepresentationType.OBJECT_COLLECTION);
        }

        @Override
        public ReprRenderer<?, ?> newRenderer(final ResourceContext resourceContext, final LinkFollower linkFollower, final JsonRepresentation representation) {
            return new ObjectCollectionReprRenderer(resourceContext, linkFollower, getRepresentationType(), representation);
        }
    }

    private ObjectCollectionReprRenderer(final ResourceContext resourceContext, final LinkFollower linkFollower, final RepresentationType representationType, final JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation, Where.PARENTED_TABLE);
    }

    @Override
    public JsonRepresentation render() {
        // id and memberType are rendered eagerly

        renderMemberContent();
        if (mode.isStandalone() || mode.isMutated() || !objectAdapter.representsPersistent()) {
            addValue();
        }
        putDisabledReasonIfDisabled();

        if (mode.isStandalone() || mode.isMutated()) {
            addExtensionsIsisProprietaryChangedObjects();
        }

        return representation;
    }

    // ///////////////////////////////////////////////////
    // value
    // ///////////////////////////////////////////////////

    private void addValue() {
        final ObjectAdapter valueAdapter = objectMember.get(objectAdapter);
        if (valueAdapter == null) {
            return;
        }

        final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(valueAdapter);
        final List<JsonRepresentation> list = Lists.newArrayList();
        for (final ObjectAdapter elementAdapter : facet.iterable(valueAdapter)) {

            final LinkBuilder newBuilder = DomainObjectReprRenderer.newLinkToBuilder(resourceContext, Rel.OBJECT, elementAdapter);

            list.add(newBuilder.build());
        }

        representation.mapPut("value", list);
    }

    // ///////////////////////////////////////////////////
    // details link
    // ///////////////////////////////////////////////////

    /**
     * Mandatory hook method to support x-ro-follow-links
     */
    @Override
    protected void followDetailsLink(final JsonRepresentation detailsLink) {
        final RendererFactory factory = RendererFactoryRegistry.instance.find(RepresentationType.OBJECT_COLLECTION);
        final ObjectCollectionReprRenderer renderer = (ObjectCollectionReprRenderer) factory.newRenderer(getResourceContext(), getLinkFollower(), JsonRepresentation.newMap());
        renderer.with(new ObjectAndCollection(objectAdapter, objectMember)).asFollowed();
        detailsLink.mapPut("value", renderer.render());
    }

    // ///////////////////////////////////////////////////
    // mutators
    // ///////////////////////////////////////////////////

    @Override
    protected void addMutatorsIfEnabled() {
        if (usability().isVetoed()) {
            return;
        }

        final CollectionSemantics semantics = CollectionSemantics.determine(this.resourceContext, objectMember);
        addMutatorLink(semantics.getAddToKey());
        addMutatorLink(semantics.getRemoveFromKey());

        return;
    }

    private void addMutatorLink(final String key) {
        final Map<String, MutatorSpec> mutators = memberType.getMutators();
        final MutatorSpec mutatorSpec = mutators.get(key);
        addLinkFor(mutatorSpec);
    }

    // ///////////////////////////////////////////////////
    // extensions and links
    // ///////////////////////////////////////////////////

    @Override
    protected void addLinksToFormalDomainModel() {
        final LinkBuilder linkBuilder = CollectionDescriptionReprRenderer.newLinkToBuilder(resourceContext, Rel.DESCRIBEDBY, objectAdapter.getSpecification(), objectMember);
        getLinks().arrayAdd(linkBuilder.build());
    }

    @Override
    protected void addLinksIsisProprietary() {
        // none
    }

    @Override
    protected void putExtensionsIsisProprietary() {
        final CollectionSemantics semantics = CollectionSemantics.determine(resourceContext, objectMember);
        getExtensions().mapPut("collectionSemantics", semantics.name().toLowerCase());
    }

}