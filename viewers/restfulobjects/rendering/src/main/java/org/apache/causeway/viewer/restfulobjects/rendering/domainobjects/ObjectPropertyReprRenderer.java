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

import com.fasterxml.jackson.databind.node.NullNode;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.causeway.viewer.restfulobjects.rendering.domaintypes.PropertyDescriptionReprRenderer;
import org.apache.causeway.viewer.restfulobjects.rendering.service.valuerender.JsonValueConverter;

import lombok.val;

public class ObjectPropertyReprRenderer
extends AbstractObjectMemberReprRenderer<OneToOneAssociation> {

    public ObjectPropertyReprRenderer(final IResourceContext context) {
        this(context, null, null, JsonRepresentation.newMap());
    }

    public ObjectPropertyReprRenderer(
            final IResourceContext context,
            final LinkFollowSpecs linkFollower,
            final String propertyId,
            final JsonRepresentation representation) {
        super(context, linkFollower, propertyId, RepresentationType.OBJECT_PROPERTY, representation,
                Where.OBJECT_FORMS);
    }

    @Override
    public JsonRepresentation render() {

        renderMemberContent();

        final LinkFollowSpecs followValue = getLinkFollowSpecs().follow("value");

        addValue(followValue);

        putDisabledReasonIfDisabled();

        if (mode.isStandalone() || mode.isMutated()) {
            addChoices();
            addExtensionsCausewayProprietaryChangedObjects();
        }

        return representation;
    }

    // ///////////////////////////////////////////////////
    // value
    // ///////////////////////////////////////////////////

    private void addValue(final LinkFollowSpecs linkFollower) {
        val valueAdapterIfAny = objectMember.get(objectAdapter, getInteractionInitiatedBy());

        // use the runtime type if we have a value, otherwise fallback to the compile time type of the member
        val valueAdapter = ManagedObjects.isSpecified(valueAdapterIfAny)
                ? valueAdapterIfAny
                : ManagedObject.empty(objectMember.getElementType());

        val spec = valueAdapter.getSpecification();

        if (spec.isValue()) {
            jsonValueEncoder
                    .appendValueAndFormat(
                            valueAdapter,
                            representation,
                            JsonValueConverter.Context.of(
                                    objectMember,
                                    resourceContext.config().isSuppressMemberExtensions()));
            return;
        }

        if(valueAdapter.getPojo() == null) {
            final NullNode value = NullNode.getInstance();
            representation.mapPutJsonNode("value", value);
            return;
        }

        final boolean eagerlyRender =
                (Facets.defaultViewIsTable(objectMember)
                        && resourceContext.canEagerlyRender(valueAdapter))
                || (linkFollower != null
                        && !linkFollower.isTerminated());

        final String title = valueAdapter.getTitle();

        final LinkBuilder valueLinkBuilder = DomainObjectReprRenderer
                .newLinkToBuilder(resourceContext, Rel.VALUE, valueAdapter).withTitle(title);
        if(eagerlyRender) {
            final DomainObjectReprRenderer renderer =
                    new DomainObjectReprRenderer(resourceContext, linkFollower, JsonRepresentation.newMap());
            renderer.with(valueAdapter);
            if(mode.isEventSerialization()) {
                renderer.asEventSerialization();
            }

            valueLinkBuilder.withValue(renderer.render());
        }

        final JsonRepresentation valueJsonRepr = valueLinkBuilder.build();
        representation.mapPutJsonRepresentation("value", valueJsonRepr);

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
        final ObjectPropertyReprRenderer renderer = new ObjectPropertyReprRenderer(getResourceContext(), getLinkFollowSpecs(), null, representation);
        renderer.with(ManagedProperty.of(objectAdapter, objectMember, super.where)).asFollowed();
        detailsLink.mapPutJsonRepresentation("value", renderer.render());
    }

    // ///////////////////////////////////////////////////
    // mutators
    // ///////////////////////////////////////////////////

    @Override
    protected void addMutatorLinksIfEnabled() {
        if (usability().isVetoed()) {
            return;
        }
        objectMemberType.getMutators()
            .values()
            .forEach(this::addLinkFor);
    }

    // ///////////////////////////////////////////////////
    // choices
    // ///////////////////////////////////////////////////

    private ObjectPropertyReprRenderer addChoices() {
        final Object propertyChoices = propertyChoices();
        if (propertyChoices != null) {
            representation.mapPut("choices", propertyChoices);
        }
        return this;
    }

    private Object propertyChoices() {
        val choiceAdapters = objectMember
                .getChoices(objectAdapter, getInteractionInitiatedBy());

        if (choiceAdapters == null || choiceAdapters.isEmpty()) {
            return null;
        }
        final List<Object> list = _Lists.newArrayList();
        for (val choiceAdapter : choiceAdapters) {
            // REVIEW: previously was using the spec of the member, but think instead it should be the spec of the adapter itself
            // final ObjectSpecification choiceSpec = objectMember.getSpecification();

            // REVIEW: check that it works for ToDoItem$Category, though...
            list.add(DomainObjectReprRenderer.valueOrRef(resourceContext, objectMember, super.getJsonValueEncoder(), choiceAdapter));
        }
        return list;
    }

    // ///////////////////////////////////////////////////
    // extensions and links
    // ///////////////////////////////////////////////////

    @Override
    protected void addLinksToFormalDomainModel() {
        if(resourceContext.config().isSuppressDescribedByLinks()) {
            return;
        }
        final JsonRepresentation link = PropertyDescriptionReprRenderer.newLinkToBuilder(getResourceContext(), Rel.DESCRIBEDBY, objectAdapter.getSpecification(), objectMember).build();
        getLinks().arrayAdd(link);
    }

    @Override
    protected void putExtensionsCausewayProprietary() {
        // none
    }


}
