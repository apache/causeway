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

import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.node.NullNode;

import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ActionResultRepresentation.ResultType;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.isis.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.isis.viewer.restfulobjects.rendering.ReprRendererAbstract;

public class ActionResultReprRenderer extends ReprRendererAbstract<ActionResultReprRenderer, ObjectAndActionInvocation> {

    private ObjectAdapterLinkTo adapterLinkTo = new DomainObjectLinkTo();

    private ManagedObject objectAdapter;
    private ObjectAction action;
    private JsonRepresentation arguments;
    private ManagedObject returnedAdapter;
    private final SelfLink selfLink;
    private ObjectAndActionInvocation objectAndActionInvocation;

    public enum SelfLink {
        INCLUDED, EXCLUDED
    }

    public ActionResultReprRenderer(
            final IResourceContext resourceContext,
            final LinkFollowSpecs linkFollower,
            final SelfLink selfLink,
            final JsonRepresentation representation) {
        super(resourceContext, linkFollower, RepresentationType.ACTION_RESULT, representation);
        this.selfLink = selfLink;
    }

    @Override
    public ActionResultReprRenderer with(final ObjectAndActionInvocation objectAndActionInvocation) {

        this.objectAndActionInvocation = objectAndActionInvocation;

        objectAdapter = objectAndActionInvocation.getObjectAdapter();
        action = objectAndActionInvocation.getAction();
        arguments = objectAndActionInvocation.getArguments();
        returnedAdapter = objectAndActionInvocation.getReturnedAdapter();

        adapterLinkTo.with(returnedAdapter);

        return this;
    }

    public void using(final ObjectAdapterLinkTo adapterLinkTo) {
        this.adapterLinkTo = adapterLinkTo.with(objectAdapter);
    }

    @Override
    public JsonRepresentation render() {

        if(representation == null) {
            return null;
        }

        representationWithSelfFor(action, arguments);

        addResult(representation);

        addExtensionsIsisProprietaryChangedObjects();

        return representation;
    }

    private void addResult(final JsonRepresentation representation) {

        final ResultType resultType = objectAndActionInvocation.determineResultType();
        final JsonRepresentation result = JsonRepresentation.newMap();
        addResultTo(resultType, result);

        putResultType(representation, resultType);
        if (!resultType.isVoid()) {
            if(returnedAdapter != null) {
                representation.mapPut("result", result);
            } else {
                representation.mapPut("result", NullNode.getInstance());
            }
        }
    }

    private void addResultTo(
            final ResultType resultType, final JsonRepresentation representation) {

        if(returnedAdapter == null) {
            return;
        }

        final ReprRendererAbstract<?, ?> renderer = buildResultRenderer(resultType, representation);
        if(renderer != null) {
            renderer.render();
        }
    }

    private ReprRendererAbstract<?, ?> buildResultRenderer(
            final ResultType resultType,
            final JsonRepresentation representation) {

        //final ObjectSpecification returnType = this.action.getReturnType();

        switch (resultType) {
        case VOID:
            return null;

        case LIST:

            final Stream<ManagedObject> collectionAdapters =
                CollectionFacet.streamAdapters(returnedAdapter);

            final ListReprRenderer listReprRenderer =
                    new ListReprRenderer(resourceContext, null, representation).withElementRel(Rel.ELEMENT);
            listReprRenderer.with(collectionAdapters)
            .withReturnType(action.getReturnType())
            .withElementType(returnedAdapter.getElementSpecification());

            return listReprRenderer;

        case SCALAR_VALUE:

            final ScalarValueReprRenderer scalarValueReprRenderer =
            new ScalarValueReprRenderer(resourceContext, null, representation);
            scalarValueReprRenderer.with(returnedAdapter)
            .withReturnType(action.getReturnType());

            return scalarValueReprRenderer;

        case DOMAIN_OBJECT:

            final DomainObjectReprRenderer objectReprRenderer =
            new DomainObjectReprRenderer(resourceContext, null, representation);

            objectReprRenderer.with(returnedAdapter).includesSelf();

            return objectReprRenderer;
        default:
            throw new IllegalStateException("All possible states of ResultType enumerated; resultType = " + resultType);
        }
    }

    private void putResultType(final JsonRepresentation representation, final ResultType resultType) {
        representation.mapPut("resulttype", resultType.getValue());
    }

    private void representationWithSelfFor(final ObjectAction action, final JsonRepresentation bodyArgs) {
        final JsonRepresentation links = JsonRepresentation.newArray();
        representation.mapPut("links", links);

        if(selfLink == SelfLink.EXCLUDED) {
            return;
        }

        final LinkBuilder selfLinkBuilder = adapterLinkTo.memberBuilder(Rel.SELF, MemberType.ACTION, action, RepresentationType.ACTION_RESULT, "invoke");

        // TODO: remove duplication with AbstractObjectMember#addLinkTo
        final MemberType memberType = MemberType.of(action);
        final Map<String, MutatorSpec> mutators = memberType.getMutators();

        final String mutator = InvokeKeys.getKeyFor(action.getSemantics());
        final MutatorSpec mutatorSpec = mutators.get(mutator);
        selfLinkBuilder.withHttpMethod(mutatorSpec.httpMethod);

        final JsonRepresentation selfLink = selfLinkBuilder.build();

        links.arrayAdd(selfLink);
        selfLink.mapPut("args", bodyArgs);

        final LinkBuilder upLinkBuilder = adapterLinkTo.memberBuilder(Rel.UP, MemberType.ACTION, action, RepresentationType.OBJECT_ACTION);
        upLinkBuilder.withHttpMethod(RestfulHttpMethod.GET);

        links.arrayAdd(upLinkBuilder.build());

    }

}
