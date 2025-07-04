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

import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.node.NullNode;

import org.apache.causeway.core.metamodel.facets.collections.CollectionFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.causeway.viewer.restfulobjects.applib.domainobjects.ActionResultRepresentation.ResultType;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.causeway.viewer.restfulobjects.rendering.ReprRendererAbstract;

public class ActionResultReprRenderer
extends ReprRendererAbstract<ObjectAndActionInvocation> {

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

    public ActionResultReprRenderer using(final ObjectAdapterLinkTo adapterLinkTo) {
        this.adapterLinkTo = adapterLinkTo.with(objectAdapter);
        return this;
    }

    @Override
    public JsonRepresentation render() {

        if(representation == null) {
            return null;
        }

        representationWithSelfFor(action, arguments);

        addResult(representation);

        addExtensionsCausewayProprietaryChangedObjects();

        return representation;
    }

    private void addResult(final JsonRepresentation representation) {

        final ResultType resultType = objectAndActionInvocation.determineResultType();
        final JsonRepresentation result = JsonRepresentation.newMap();
        addResultTo(resultType, result);

        putResultType(representation, resultType);
        if (!resultType.isVoid()) {
            if(returnedAdapter != null) {
                representation.mapPutJsonRepresentation("result", result);
            } else {
                representation.mapPutJsonNode("result", NullNode.getInstance());
            }
        }
    }

    private void addResultTo(
            final ResultType resultType,
            final JsonRepresentation representation) {

        if(returnedAdapter == null
                || !ManagedObjects.isSpecified(returnedAdapter)) {
            return;
        }

        // we have a returnedAdapter with a spec, but it might hold no pojo (null)

        final ReprRendererAbstract<?> renderer = buildResultRenderer(resultType, representation);
        if(renderer != null) {
            renderer.render();
        }
    }

    private ReprRendererAbstract<?> buildResultRenderer(
            final ResultType resultType,
            final JsonRepresentation representation) {

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
            .withElementType(returnedAdapter.getElementSpecification().orElse(null));

            return listReprRenderer;

        case SCALAR_VALUE:

            final ScalarValueReprRenderer scalarValueReprRenderer =
            new ScalarValueReprRenderer(resourceContext, action, null, representation);
            scalarValueReprRenderer.with(returnedAdapter)
            .withReturnType(action.getReturnType());

            return scalarValueReprRenderer;

        case DOMAIN_OBJECT:

            final DomainObjectReprRenderer objectReprRenderer =
            new DomainObjectReprRenderer(resourceContext, null, representation);

            objectReprRenderer.with(returnedAdapter).includesSelf();

            return objectReprRenderer;

        case SCALAR_VALUES:
            // Variant of 'list' representing a list of scalar values.
            // NOT supported by the RO spec v1.0, but allows for custom representations to
            // support this particular data structure.
            return null;

        default:
            throw new IllegalStateException("All possible states of ResultType enumerated; resultType = " + resultType);
        }
    }

    private void putResultType(final JsonRepresentation representation, final ResultType resultType) {
        representation.mapPutString("resulttype", resultType.getValue());
    }

    private void representationWithSelfFor(final ObjectAction action, final JsonRepresentation bodyArgs) {
        final JsonRepresentation links = JsonRepresentation.newArray();
        representation.mapPutJsonRepresentation("links", links);

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
        selfLink.mapPutJsonRepresentation("args", bodyArgs);

        final LinkBuilder upLinkBuilder = adapterLinkTo.memberBuilder(Rel.UP, MemberType.ACTION, action, RepresentationType.OBJECT_ACTION);
        upLinkBuilder.withHttpMethod(RestfulHttpMethod.GET);

        links.arrayAdd(upLinkBuilder.build());

    }

}
