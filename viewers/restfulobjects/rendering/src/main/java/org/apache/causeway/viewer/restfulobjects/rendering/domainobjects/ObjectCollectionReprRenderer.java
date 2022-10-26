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
package org.apache.causeway.viewer.restfulobjects.rendering.domainobjects;

import java.util.List;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.metamodel.facets.collections.CollectionFacet;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.causeway.viewer.restfulobjects.rendering.domaintypes.CollectionDescriptionReprRenderer;

import lombok.val;

public class ObjectCollectionReprRenderer
extends AbstractObjectMemberReprRenderer<OneToManyAssociation> {

    public ObjectCollectionReprRenderer(
            final IResourceContext resourceContext,
            final LinkFollowSpecs linkFollowSpecs,
            final String collectionId,
            final JsonRepresentation representation) {

        super(resourceContext,
                linkFollowSpecs,
                collectionId,
                RepresentationType.OBJECT_COLLECTION,
                representation,
                Where.PARENTED_TABLES);
    }

    @Override
    public JsonRepresentation render() {

        if(representation == null) {
            return null;
        }

        renderMemberContent();

        final LinkFollowSpecs followValue = getLinkFollowSpecs().follow("value");
        final boolean eagerlyRender = !followValue.isTerminated()
                || (resourceContext.config().isHonorUiHints()
                        && Facets.defaultViewIsTable(objectMember));

        if ((mode.isInline() && eagerlyRender)
                || mode.isStandalone()
                || mode.isMutated()
                || mode.isEventSerialization()
                || !ManagedObjects.isIdentifiable(objectAdapter)) {
            addValue(followValue);
        }
        if(!mode.isEventSerialization()) {
            putDisabledReasonIfDisabled();
        }

        if (mode.isStandalone() || mode.isMutated()) {
            addExtensionsCausewayProprietaryChangedObjects();
        }

        return representation;
    }

    // ///////////////////////////////////////////////////
    // value
    // ///////////////////////////////////////////////////

    private void addValue(final LinkFollowSpecs linkFollower) {
        val valueAdapter = objectMember.get(objectAdapter, getInteractionInitiatedBy());
        if (valueAdapter == null) {
            return;
        }

        final LinkFollowSpecs followHref = linkFollower.follow("href");
        final boolean eagerlyRender = !followHref.isTerminated()
                || (resourceContext.config().isHonorUiHints()
                        && Facets.defaultViewIsTable(objectMember)
                        && resourceContext.canEagerlyRender(valueAdapter));

        final List<JsonRepresentation> list = _Lists.newArrayList();

        CollectionFacet.streamAdapters(valueAdapter)
        .forEach(elementAdapter->{
            final LinkBuilder valueLinkBuilder = DomainObjectReprRenderer
                    .newLinkToBuilder(resourceContext, Rel.VALUE, elementAdapter);
            if(eagerlyRender) {
                val domainObjectReprRenderer =
                        new DomainObjectReprRenderer(getResourceContext(), followHref, JsonRepresentation.newMap())
                        .with(elementAdapter);
                if(mode.isEventSerialization()) {
                    domainObjectReprRenderer.asEventSerialization();
                }

                valueLinkBuilder.withValue(domainObjectReprRenderer.render());
            }

            list.add(valueLinkBuilder.build());
        });

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
        val where = resourceContext.getWhere();
        val jsonRepresentation = JsonRepresentation.newMap();
        val objectCollectionReprRenderer =
                new ObjectCollectionReprRenderer(getResourceContext(), getLinkFollowSpecs(), null, jsonRepresentation)
                .with(ManagedCollection.of(objectAdapter, objectMember, where))
                .asFollowed();
        detailsLink.mapPutJsonRepresentation("value", objectCollectionReprRenderer.render());
    }

    // ///////////////////////////////////////////////////
    // mutators
    // ///////////////////////////////////////////////////

    @Override
    protected void addMutatorLinksIfEnabled() {
        // no-op
    }

    // ///////////////////////////////////////////////////
    // extensions and links
    // ///////////////////////////////////////////////////

    @Override
    protected void addLinksToFormalDomainModel() {
        if(resourceContext.config().isSuppressDescribedByLinks()) {
            return;
        }
        final JsonRepresentation link =
                CollectionDescriptionReprRenderer
                .newLinkToBuilder(resourceContext, Rel.DESCRIBEDBY, objectAdapter.getSpecification(), objectMember).build();
        getLinks().arrayAdd(link);
    }

    @Override
    protected void putExtensionsCausewayProprietary() {
        final CollectionSemantics semantics = CollectionSemantics.valueOf(objectMember);
        getExtensions().mapPutString("collectionSemantics", semantics.name().toLowerCase());
    }


}
