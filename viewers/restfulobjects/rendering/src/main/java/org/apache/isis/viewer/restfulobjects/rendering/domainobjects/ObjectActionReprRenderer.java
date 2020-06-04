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
import java.util.Map;

import com.fasterxml.jackson.databind.node.NullNode;

import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.isis.viewer.restfulobjects.rendering.domaintypes.ActionDescriptionReprRenderer;

import lombok.val;

public class ObjectActionReprRenderer extends AbstractObjectMemberReprRenderer<ObjectActionReprRenderer, ObjectAction> {

    public ObjectActionReprRenderer(IResourceContext resourceContext) {
        this(resourceContext, null, null, JsonRepresentation.newMap());
    }

    public ObjectActionReprRenderer(
            final IResourceContext resourceContext,
            final LinkFollowSpecs linkFollowSpecs,
            String actionId,
            final JsonRepresentation representation) {
        super(resourceContext, linkFollowSpecs, actionId, RepresentationType.OBJECT_ACTION, representation,
                Where.OBJECT_FORMS);
    }

    @Override
    public JsonRepresentation render() {

        renderMemberContent();
        putDisabledReasonIfDisabled();

        if (mode.isStandalone() || mode.isMutated()) {
            addParameterDetails();
        }

        return representation;
    }

    // ///////////////////////////////////////////////////
    // details link
    // ///////////////////////////////////////////////////

    /**
     * Mandatory hook method to support x-ro-follow-links
     */
    @Override
    protected void followDetailsLink(final JsonRepresentation detailsLink) {
        final ObjectActionReprRenderer renderer = new ObjectActionReprRenderer(getResourceContext(), getLinkFollowSpecs(), null, JsonRepresentation.newMap());
        renderer.with(ManagedAction.of(objectAdapter, objectMember)).usingLinkTo(linkTo).asFollowed();
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
        final Map<String, MutatorSpec> mutators = objectMemberType.getMutators();

        final SemanticsOf actionSemantics = objectMember.getSemantics();
        final String mutator = InvokeKeys.getKeyFor(actionSemantics);
        final MutatorSpec mutatorSpec = mutators.get(mutator);

        addLinkFor(mutatorSpec);
    }

    @Override
    protected ObjectAdapterLinkTo linkToForMutatorInvoke() {
        return super.linkToForMutatorInvoke();
    }

    @Override
    protected JsonRepresentation mutatorArgs(final MutatorSpec mutatorSpec) {
        final JsonRepresentation argMap = JsonRepresentation.newMap();
        val parameters = objectMember.getParameters();
        for (int i = 0; i < objectMember.getParameterCount(); i++) {
            argMap.mapPut(parameters.getElseFail(i).getId() + ".value", argValueFor(i));
        }
        return argMap;
    }

    private Object argValueFor(final int i) {
        // force a null into the map
        return NullNode.getInstance();
    }

    // ///////////////////////////////////////////////////
    // parameter details
    // ///////////////////////////////////////////////////

    private ObjectActionReprRenderer addParameterDetails() {
        final Map<String,Object> parameters = _Maps.newLinkedHashMap();
        for (int i = 0; i < objectMember.getParameterCount(); i++) {
            final ObjectActionParameter param = objectMember.getParameters().getElseFail(i);
            final Object paramDetails = paramDetails(param, getInteractionInitiatedBy());
            parameters.put(param.getId(), paramDetails);
        }
        representation.mapPut("parameters", parameters);
        return this;
    }

    private Object paramDetails(final ObjectActionParameter param, final InteractionInitiatedBy interactionInitiatedBy) {
        final JsonRepresentation paramRep = JsonRepresentation.newMap();
        paramRep.mapPut("num", param.getNumber());
        paramRep.mapPut("id", param.getId());
        paramRep.mapPut("name", param.getName());
        paramRep.mapPut("description", param.getDescription());
        final Object paramChoices = choicesFor(param, interactionInitiatedBy);
        if (paramChoices != null) {
            paramRep.mapPut("choices", paramChoices);
        }
        final Object paramDefault = defaultFor(param);
        if (paramDefault != null) {
            paramRep.mapPut("default", paramDefault);
        }
        return paramRep;
    }

    private Object choicesFor(
            final ObjectActionParameter param,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        val pendingArgs = param.getAction()
                .interactionHead(objectAdapter)
                .emptyModel();
        
        val choiceAdapters = param.getChoices(pendingArgs, interactionInitiatedBy);
        if (choiceAdapters == null || choiceAdapters.isEmpty()) {
            return null;
        }
        final List<Object> list = _Lists.newArrayList();
        for (val choiceAdapter : choiceAdapters) {
            // REVIEW: previously was using the spec of the parameter, but think instead it should be the spec of the adapter itself
            // final ObjectSpecification choiceSpec = param.getSpecification();
            list.add(DomainObjectReprRenderer.valueOrRef(resourceContext, super.getJsonValueEncoder(), choiceAdapter));
        }
        return list;
    }

    private Object defaultFor(final ObjectActionParameter param) {
        
        val emptyPpm = param.getAction()
                .interactionHead(objectAdapter)
                .emptyModel();
        
        val defaultAdapter = param.getDefault(emptyPpm);
        if (ManagedObjects.isNullOrUnspecifiedOrEmpty(defaultAdapter)) {
            return null;
        }
        // REVIEW: previously was using the spec of the parameter, but think instead it should be the spec of the adapter itself
        // final ObjectSpecification defaultSpec = param.getSpecification();
        return DomainObjectReprRenderer.valueOrRef(resourceContext, super.getJsonValueEncoder(), defaultAdapter);
    }

    // ///////////////////////////////////////////////////
    // extensions and links
    // ///////////////////////////////////////////////////

    @Override
    protected void addLinksToFormalDomainModel() {
        if(resourceContext.suppressDescribedByLinks()) {
            return;
        }
        final JsonRepresentation link = ActionDescriptionReprRenderer.newLinkToBuilder(resourceContext, Rel.DESCRIBEDBY, objectAdapter.getSpecification(), objectMember).build();
        getLinks().arrayAdd(link);
    }

    @Override
    protected void putExtensionsIsisProprietary() {
        getExtensions().mapPut("actionType", objectMember.getType().name().toLowerCase());

        final SemanticsOf semantics = objectMember.getSemantics();
        getExtensions().mapPut("actionSemantics", semantics.getCamelCaseName());
    }

}
