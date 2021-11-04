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
package org.apache.isis.viewer.restfulobjects.rendering.domainobjects;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.databind.node.NullNode;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.collections.collection.defaultview.DefaultViewFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxFractionDigitsFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxTotalDigitsFacet;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.isis.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.isis.viewer.restfulobjects.rendering.domaintypes.PropertyDescriptionReprRenderer;

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
            addExtensionsIsisProprietaryChangedObjects();
        }

        return representation;
    }

    // ///////////////////////////////////////////////////
    // value
    // ///////////////////////////////////////////////////

    private Object addValue(final LinkFollowSpecs linkFollower) {
        val valueAdapterIfAny = objectMember.get(objectAdapter, getInteractionInitiatedBy());

        // use the runtime type if we have a value, otherwise the compile time type of the member
        val spec = valueAdapterIfAny != null
                ? valueAdapterIfAny.getSpecification()
                : objectMember.getElementType();

        val valueFacet = spec.getFacet(ValueFacet.class);
        if (valueFacet != null) {
            String format = null;
            final Class<?> valueType = spec.getCorrespondingClass();
            if(valueType == java.math.BigDecimal.class) {
                // look for facet on member, else on the value's spec

                val facetHolders = Can.<FacetHolder>of(
                        objectMember,
                        valueAdapterIfAny != null ? valueAdapterIfAny.getSpecification() : null);

                final int totalDigits = lookupFacet(MaxTotalDigitsFacet.class, facetHolders)
                        .map(MaxTotalDigitsFacet::maxTotalDigits)
                        .orElse(-1);

                final int scale = lookupFacet(MaxFractionDigitsFacet.class, facetHolders)
                        .map(MaxFractionDigitsFacet::getMaximumFractionDigits)
                        .orElse(-1);

                format = String.format("big-decimal(%d,%d)", totalDigits, scale);

            } else if(valueType == java.math.BigInteger.class) {
                // look for facet on member, else on the value's spec
                format = String.format("big-integer");
            }
            return jsonValueEncoder.appendValueAndFormat(valueAdapterIfAny, spec, representation, format, resourceContext.suppressMemberExtensions());
        }

        boolean eagerlyRender =
                (renderEagerly() && resourceContext.canEagerlyRender(valueAdapterIfAny))
                || (linkFollower != null && !linkFollower.isTerminated());

        if(valueAdapterIfAny == null) {
            final NullNode value = NullNode.getInstance();
            representation.mapPut("value", value);
            return value;
        }

        val valueAdapter = valueAdapterIfAny;

        final String title = valueAdapter.getTitle();

        final LinkBuilder valueLinkBuilder = DomainObjectReprRenderer
                .newLinkToBuilder(resourceContext, Rel.VALUE, valueAdapterIfAny).withTitle(title);
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
        representation.mapPut("value", valueJsonRepr);
        return valueJsonRepr;

    }

    private boolean renderEagerly() {
        final DefaultViewFacet defaultViewFacet = objectMember.getFacet(DefaultViewFacet.class);
        return defaultViewFacet != null
                && Objects.equals(defaultViewFacet.value(), "table");
    }

    private static <T extends Facet> Optional<T> lookupFacet(
            final Class<T> facetType,
            final Can<FacetHolder> holders) {
        for (FacetHolder holder : holders) {
            final T facet = holder.getFacet(facetType);
            if(facet != null) {
                return Optional.of(facet);
            }
        }
        return Optional.empty();
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
            list.add(DomainObjectReprRenderer.valueOrRef(resourceContext, super.getJsonValueEncoder(), choiceAdapter));
        }
        return list;
    }

    // ///////////////////////////////////////////////////
    // extensions and links
    // ///////////////////////////////////////////////////

    @Override
    protected void addLinksToFormalDomainModel() {
        if(resourceContext.suppressDescribedByLinks()) {
            return;
        }
        final JsonRepresentation link = PropertyDescriptionReprRenderer.newLinkToBuilder(getResourceContext(), Rel.DESCRIBEDBY, objectAdapter.getSpecification(), objectMember).build();
        getLinks().arrayAdd(link);
    }

    @Override
    protected void putExtensionsIsisProprietary() {
        // none
    }


}
