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
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedParameter;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.isis.viewer.restfulobjects.rendering.domaintypes.ActionDescriptionReprRenderer;

import lombok.val;

public class ObjectActionReprRenderer
extends AbstractObjectMemberReprRenderer<ObjectAction> {

    public ObjectActionReprRenderer(final IResourceContext resourceContext) {
        this(resourceContext, null, null, JsonRepresentation.newMap());
    }

    public ObjectActionReprRenderer(
            final IResourceContext resourceContext,
            final LinkFollowSpecs linkFollowSpecs,
            final String actionId,
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
        val where = resourceContext.getWhere();
        final ObjectActionReprRenderer renderer = new ObjectActionReprRenderer(getResourceContext(), getLinkFollowSpecs(), null, JsonRepresentation.newMap());
        renderer.with(ManagedAction.of(objectAdapter, objectMember, where)).usingLinkTo(linkTo).asFollowed();
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
        if(objectMember.getParameterCount()>0) {
            val act = ManagedAction.of(objectAdapter, objectMember, Where.ANYWHERE);
            val paramNeg = act.startParameterNegotiation();
            for(val paramMod : paramNeg.getParamModels()) {
                val paramMeta = paramMod.getMetaModel();
                final Object paramDetails = paramDetails(paramMod, paramNeg);
                parameters.put(paramMeta.getId(), paramDetails);
            }
        }
        representation.mapPut("parameters", parameters);
        return this;
    }

    private Object paramDetails(final ManagedParameter paramMod, final ParameterNegotiationModel paramNeg) {
        val paramMeta = paramMod.getMetaModel();
        final JsonRepresentation paramRep = JsonRepresentation.newMap();
        paramRep.mapPut("num", paramMeta.getParameterIndex());
        paramRep.mapPut("id", paramMeta.getId());
        paramRep.mapPut("name", paramMeta.getFriendlyName(objectAdapter.asProvider()));
        paramRep.mapPut("description", paramMeta.getDescription(objectAdapter.asProvider()));
        final Object paramChoices = choicesFor(paramMod, paramNeg);
        if (paramChoices != null) {
            paramRep.mapPut("choices", paramChoices);
        }
        final Object paramDefault = defaultFor(paramMod);
        if (paramDefault != null) {
            paramRep.mapPut("default", paramDefault);
        }
        return paramRep;
    }

    private Object choicesFor(
            final ManagedParameter paramMod,
            final ParameterNegotiationModel paramNeg) {
        val paramMeta = paramMod.getMetaModel();
        val choiceAdapters = paramMeta.getChoices(paramNeg, getInteractionInitiatedBy());
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

    private Object defaultFor(final ManagedParameter paramMod) {
        val defaultAdapter = paramMod.getValue().getValue();
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
        getExtensions().mapPut("actionScope", objectMember.getScope().name().toLowerCase());

        final SemanticsOf semantics = objectMember.getSemantics();
        getExtensions().mapPut("actionSemantics", semantics.getCamelCaseName());
    }

}
