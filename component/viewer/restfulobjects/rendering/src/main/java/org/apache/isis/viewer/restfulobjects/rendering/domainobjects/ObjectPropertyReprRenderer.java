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

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.isis.viewer.restfulobjects.rendering.RendererContext;
import org.apache.isis.viewer.restfulobjects.rendering.domaintypes.PropertyDescriptionReprRenderer;
import org.codehaus.jackson.node.NullNode;

import com.google.common.collect.Lists;

public class ObjectPropertyReprRenderer extends AbstractObjectMemberReprRenderer<ObjectPropertyReprRenderer, OneToOneAssociation> {

    public ObjectPropertyReprRenderer(final RendererContext resourceContext, final LinkFollowSpecs linkFollower, final JsonRepresentation representation) {
        super(resourceContext, linkFollower, RepresentationType.OBJECT_PROPERTY, representation, Where.OBJECT_FORMS);
    }

    @Override
    public JsonRepresentation render() {
        // id and memberType are rendered eagerly

        renderMemberContent();
        addValue();

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

    private void addValue() {
        representation.mapPut("value", valueOrRefRepr());
    }

    private Object valueOrRefRepr() {
        final ObjectAdapter valueAdapter = objectMember.get(objectAdapter);
        if (valueAdapter == null) {
            return NullNode.getInstance();
        }
        // REVIEW: previously was using the spec of the member, but think instead it should be the spec of the adapter itself
        // final ObjectSpecification valueSpec = objectMember.getSpecification();
        ObjectSpecification valueSpec = valueAdapter.getSpecification();
        return DomainObjectReprRenderer.valueOrRef(rendererContext, valueAdapter, valueSpec);
    }

    // ///////////////////////////////////////////////////
    // details link
    // ///////////////////////////////////////////////////

    /**
     * Mandatory hook method to support x-ro-follow-links
     */
    @Override
    protected void followDetailsLink(final JsonRepresentation detailsLink) {
        final ObjectPropertyReprRenderer renderer = new ObjectPropertyReprRenderer(getRendererContext(), getLinkFollowSpecs(), JsonRepresentation.newMap());
        renderer.with(new ObjectAndProperty(objectAdapter, objectMember)).asFollowed();
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
        final Map<String, MutatorSpec> mutators = memberType.getMutators();
        for (final String mutator : mutators.keySet()) {
            final MutatorSpec mutatorSpec = mutators.get(mutator);
            addLinkFor(mutatorSpec);
        }
        return;
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
        final ObjectAdapter[] choiceAdapters = objectMember.getChoices(objectAdapter);
        if (choiceAdapters == null || choiceAdapters.length == 0) {
            return null;
        }
        final List<Object> list = Lists.newArrayList();
        for (final ObjectAdapter choiceAdapter : choiceAdapters) {
            // REVIEW: previously was using the spec of the member, but think instead it should be the spec of the adapter itself
            // final ObjectSpecification choiceSpec = objectMember.getSpecification();
            
            // REVIEW: check that it works for ToDoItem$Category, though...
            final ObjectSpecification choiceSpec = objectAdapter.getSpecification();
            list.add(DomainObjectReprRenderer.valueOrRef(rendererContext, choiceAdapter, choiceSpec));
        }
        return list;
    }

    // ///////////////////////////////////////////////////
    // extensions and links
    // ///////////////////////////////////////////////////

    @Override
    protected void addLinksToFormalDomainModel() {
        getLinks().arrayAdd(PropertyDescriptionReprRenderer.newLinkToBuilder(getRendererContext(), Rel.DESCRIBEDBY, objectAdapter.getSpecification(), objectMember).build());
    }

    @Override
    protected void addLinksIsisProprietary() {
        // none
    }

    @Override
    protected void putExtensionsIsisProprietary() {
        // none
    }

}