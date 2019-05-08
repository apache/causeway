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
package org.apache.isis.viewer.restfulobjects.rendering.domainobjects;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.collections.collection.defaultview.DefaultViewFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.isis.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.isis.viewer.restfulobjects.rendering.RendererContext;
import org.apache.isis.viewer.restfulobjects.rendering.domaintypes.CollectionDescriptionReprRenderer;

public class ObjectCollectionReprRenderer extends AbstractObjectMemberReprRenderer<ObjectCollectionReprRenderer, OneToManyAssociation> {

    public ObjectCollectionReprRenderer(
            final RendererContext rendererContext,
            final LinkFollowSpecs linkFollowSpecs,
            final String collectionId,
            final JsonRepresentation representation) {
        super(rendererContext, linkFollowSpecs, collectionId, RepresentationType.OBJECT_COLLECTION, representation,
                Where.PARENTED_TABLES);
    }

    @Override
    public JsonRepresentation render() {

        if(representation == null) {
            return null;
        }

        renderMemberContent();

        final LinkFollowSpecs followValue = getLinkFollowSpecs().follow("value");
        boolean eagerlyRender = rendererContext.honorUiHints() && renderEagerly() || !followValue.isTerminated();

        if ((mode.isInline() && eagerlyRender) || mode.isStandalone() || mode.isMutated() || mode.isEventSerialization() || !objectAdapter.isRepresentingPersistent()) {
            addValue(followValue);
        }
        if(!mode.isEventSerialization()) {
            putDisabledReasonIfDisabled();
        }

        if (mode.isStandalone() || mode.isMutated()) {
            addExtensionsIsisProprietaryChangedObjects();
        }

        return representation;
    }

    private boolean renderEagerly() {
        final DefaultViewFacet defaultViewFacet = objectMember.getFacet(DefaultViewFacet.class);
        return defaultViewFacet != null && Objects.equals(defaultViewFacet.value(), "table");
    }

    // ///////////////////////////////////////////////////
    // value
    // ///////////////////////////////////////////////////

    private void addValue(final LinkFollowSpecs linkFollower) {
        final ObjectAdapter valueAdapter = objectMember.get(objectAdapter, getInteractionInitiatedBy());
        if (valueAdapter == null) {
            return;
        }

        final LinkFollowSpecs followHref = linkFollower.follow("href");
        boolean eagerlyRender = rendererContext.honorUiHints() && renderEagerly(valueAdapter) || !followHref.isTerminated();

        final Stream<ObjectAdapter> elementAdapters = CollectionFacet.Utils.streamAdapters(valueAdapter);
        
        final List<JsonRepresentation> list = _Lists.newArrayList();
        
        elementAdapters.forEach(elementAdapter->{
            final LinkBuilder valueLinkBuilder = DomainObjectReprRenderer.newLinkToBuilder(rendererContext, Rel.VALUE, elementAdapter);
            if(eagerlyRender) {
                final DomainObjectReprRenderer renderer = new DomainObjectReprRenderer(getRendererContext(), followHref, JsonRepresentation.newMap()
                        );
                renderer.with(elementAdapter);
                if(mode.isEventSerialization()) {
                    renderer.asEventSerialization();
                }

                valueLinkBuilder.withValue(renderer.render());
            }

            list.add(valueLinkBuilder.build());
        });

        representation.mapPut("value", list);
    }

    private boolean renderEagerly(ObjectAdapter valueAdapter) {
        return renderEagerly() && rendererContext.canEagerlyRender(valueAdapter);
    }

    // ///////////////////////////////////////////////////
    // details link
    // ///////////////////////////////////////////////////

    /**
     * Mandatory hook method to support x-ro-follow-links
     */
    @Override
    protected void followDetailsLink(final JsonRepresentation detailsLink) {
        final JsonRepresentation representation = JsonRepresentation.newMap();
        final ObjectCollectionReprRenderer renderer = new ObjectCollectionReprRenderer(getRendererContext(), getLinkFollowSpecs(), null,
                representation);
        renderer.with(new ObjectAndCollection(objectAdapter, objectMember)).asFollowed();
        detailsLink.mapPut("value", renderer.render());
    }

    // ///////////////////////////////////////////////////
    // mutators
    // ///////////////////////////////////////////////////

    @Override
    protected void addMutatorLinksIfEnabled() {
        if (usability().isVetoed()) {
            return;
        }

        final CollectionSemantics semantics = CollectionSemantics.determine(objectMember);
        addMutatorLink(semantics.getAddToKey());
        addMutatorLink(semantics.getRemoveFromKey());

        return;
    }

    private void addMutatorLink(final String key) {
        final Map<String, MutatorSpec> mutators = objectMemberType.getMutators();
        final MutatorSpec mutatorSpec = mutators.get(key);
        addLinkFor(mutatorSpec);
    }

    // ///////////////////////////////////////////////////
    // extensions and links
    // ///////////////////////////////////////////////////

    @Override
    protected void addLinksToFormalDomainModel() {
        if(rendererContext.suppressDescribedByLinks()) {
            return;
        }
        final JsonRepresentation link = CollectionDescriptionReprRenderer.newLinkToBuilder(rendererContext, Rel.DESCRIBEDBY, objectAdapter.getSpecification(), objectMember).build();
        getLinks().arrayAdd(link);
    }

    @Override
    protected void addLinksIsisProprietary() {
        // none
    }

    @Override
    protected void putExtensionsIsisProprietary() {
        final CollectionSemantics semantics = CollectionSemantics.determine(objectMember);
        getExtensions().mapPut("collectionSemantics", semantics.name().toLowerCase());
    }


}