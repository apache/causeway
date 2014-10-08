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
package org.apache.isis.viewer.restfulobjects.server.resources;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.*;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ActionResultReprRenderer.SelfLink;
import org.apache.isis.viewer.restfulobjects.server.ResourceContext;
import org.apache.isis.viewer.restfulobjects.server.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.server.resources.ResourceAbstract.Caching;

@DomainService
public class ResponseGeneratorService {

    private static final DateFormat ETAG_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static interface ResponseContext {
        public ResourceContext getResourceContext();
        public ObjectAdapter getObjectAdapter();
        public ObjectAdapterLinkTo getAdapterLinkTo();
    }


    // //////////////////////////////////////////////////////////////
    // objectRepresentation
    // //////////////////////////////////////////////////////////////

    @Programmatic
    public Response objectRepresentation(final ResponseContext responseContext) {

        final ResourceContext resourceContext = responseContext.getResourceContext();
        final ObjectAdapter objectAdapter = responseContext.getObjectAdapter();

        final DomainObjectReprRenderer renderer = new DomainObjectReprRenderer(resourceContext, null, JsonRepresentation.newMap());
        renderer.with(objectAdapter).includesSelf();

        final ResponseBuilder responseBuilder = Responses.ofOk(renderer, Caching.NONE);

        final Version version = objectAdapter.getVersion();
        if (version != null && version.getTime() != null) {
            responseBuilder.tag(ETAG_FORMAT.format(version.getTime()));
        }
        return buildResponse(responseBuilder);
    }

    // //////////////////////////////////////////////////////////////
    // propertyDetails
    // //////////////////////////////////////////////////////////////

    public enum MemberMode {
        NOT_MUTATING {
            @Override
            public void apply(final AbstractObjectMemberReprRenderer<?, ?> renderer) {
                renderer.asStandalone();
            }
        },
        MUTATING {
            @Override
            public void apply(final AbstractObjectMemberReprRenderer<?, ?> renderer) {
                renderer.asMutated();
            }
        };

        public abstract void apply(AbstractObjectMemberReprRenderer<?, ?> renderer);
    }

    @Programmatic
    public Response propertyDetails(
            final ResponseContext responseContext,
            final String propertyId, final MemberMode memberMode, final Caching caching) {

        final ResourceContext resourceContext = responseContext.getResourceContext();
        final ObjectAdapter objectAdapter = responseContext.getObjectAdapter();
        final ObjectAdapterLinkTo adapterLinkTo = responseContext.getAdapterLinkTo();

        ObjectAdapterAccessHelper accessHelper = new ObjectAdapterAccessHelper(resourceContext, objectAdapter);

        final OneToOneAssociation property = accessHelper.getPropertyThatIsVisibleForIntent(propertyId, ObjectAdapterAccessHelper.Intent.ACCESS);

        final ObjectPropertyReprRenderer renderer = new ObjectPropertyReprRenderer(resourceContext, null, null, JsonRepresentation.newMap());

        renderer.with(new ObjectAndProperty(objectAdapter, property)).usingLinkTo(adapterLinkTo);

        memberMode.apply(renderer);

        return Responses.ofOk(renderer, caching).build();
    }

    // //////////////////////////////////////////////////////////////
    // collectionDetails
    // //////////////////////////////////////////////////////////////

    @Programmatic
    public Response collectionDetails(
            final ResponseContext responseContext,
            final String collectionId, final MemberMode memberMode, final Caching caching) {

        final ResourceContext resourceContext = responseContext.getResourceContext();
        final ObjectAdapter objectAdapter = responseContext.getObjectAdapter();
        final ObjectAdapterLinkTo adapterLinkTo = responseContext.getAdapterLinkTo();

        ObjectAdapterAccessHelper accessHelper = new ObjectAdapterAccessHelper(resourceContext, objectAdapter);

        final OneToManyAssociation collection = accessHelper.getCollectionThatIsVisibleForIntent(collectionId, ObjectAdapterAccessHelper.Intent.ACCESS);

        final ObjectCollectionReprRenderer renderer = new ObjectCollectionReprRenderer(resourceContext, null, null, JsonRepresentation.newMap());

        renderer.with(new ObjectAndCollection(objectAdapter, collection)).usingLinkTo(adapterLinkTo);

        memberMode.apply(renderer);

        final ResponseBuilder responseBuilder = Responses.ofOk(renderer, caching);
        return buildResponse(responseBuilder);
    }

    // //////////////////////////////////////////////////////////////
    // action Prompt
    // //////////////////////////////////////////////////////////////

    @Programmatic
    public Response actionPrompt(
            final ResponseContext responseContext,
            final String actionId) {

        final ResourceContext resourceContext = responseContext.getResourceContext();
        final ObjectAdapter objectAdapter = responseContext.getObjectAdapter();
        final ObjectAdapterLinkTo adapterLinkTo = responseContext.getAdapterLinkTo();

        ObjectAdapterAccessHelper accessHelper = new ObjectAdapterAccessHelper(resourceContext, objectAdapter);

        final ObjectAction action = accessHelper.getObjectActionThatIsVisibleForIntent(actionId, ObjectAdapterAccessHelper.Intent.ACCESS);

        final ObjectActionReprRenderer renderer = new ObjectActionReprRenderer(resourceContext, null, null, JsonRepresentation.newMap());

        renderer.with(new ObjectAndAction(objectAdapter, action)).usingLinkTo(adapterLinkTo).asStandalone();

        final ResponseBuilder responseBuilder = Responses.ofOk(renderer, Caching.NONE);
        return buildResponse(responseBuilder);
    }

    // //////////////////////////////////////////////////////////////
    // invoke action
    // //////////////////////////////////////////////////////////////

    @Programmatic
    public Response invokeActionQueryOnly(
            final ResponseContext responseContext,
            final String actionId, final JsonRepresentation arguments) {

        final ResourceContext resourceContext = responseContext.getResourceContext();
        final ObjectAdapter objectAdapter = responseContext.getObjectAdapter();
        final ObjectAdapterLinkTo adapterLinkTo = responseContext.getAdapterLinkTo();

        final ObjectAdapterAccessHelper accessHelper = new ObjectAdapterAccessHelper(resourceContext, objectAdapter);

        final ObjectAction action = accessHelper.getObjectActionThatIsVisibleForIntent(actionId, ObjectAdapterAccessHelper.Intent.MUTATE);

        final ActionSemantics.Of actionSemantics = action.getSemantics();
        if (actionSemantics != ActionSemantics.Of.SAFE) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.METHOD_NOT_ALLOWED, "Method not allowed; action '%s' is not query only", action.getId());
        }

        final ResponseBuilder responseBuilder = invokeActionUsingAdapters(responseContext, action, arguments, SelfLink.INCLUDED);
        return buildResponse(responseBuilder);
    }

    @Programmatic
    public Response invokeActionIdempotent(
            final ResponseContext responseContext,
            final String actionId, final JsonRepresentation arguments) {

        final ResourceContext resourceContext = responseContext.getResourceContext();
        final ObjectAdapter objectAdapter = responseContext.getObjectAdapter();

        final ObjectAdapterAccessHelper accessHelper = new ObjectAdapterAccessHelper(resourceContext, objectAdapter);

        final ObjectAction action = accessHelper.getObjectActionThatIsVisibleForIntent(actionId, ObjectAdapterAccessHelper.Intent.MUTATE);

        final ActionSemantics.Of actionSemantics = action.getSemantics();
        if (!actionSemantics.isIdempotentInNature()) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.METHOD_NOT_ALLOWED, "Method not allowed; action '%s' is not idempotent", action.getId());
        }
        final ResponseBuilder responseBuilder = invokeActionUsingAdapters(responseContext, action, arguments, SelfLink.EXCLUDED);
        return buildResponse(responseBuilder);
    }

    @Programmatic
    public Response invokeAction(
            final ResponseContext responseContext,
            final String actionId, final JsonRepresentation arguments) {

        final ResourceContext resourceContext = responseContext.getResourceContext();
        final ObjectAdapter objectAdapter = responseContext.getObjectAdapter();

        ObjectAdapterAccessHelper accessHelper = new ObjectAdapterAccessHelper(resourceContext, objectAdapter);

        final ObjectAction action = accessHelper.getObjectActionThatIsVisibleForIntent(actionId, ObjectAdapterAccessHelper.Intent.MUTATE);

        final ResponseBuilder responseBuilder = invokeActionUsingAdapters(responseContext, action, arguments, SelfLink.EXCLUDED);
        return buildResponse(responseBuilder);
    }

    /**
     * Overridable to allow further customization.
     */
    protected Response buildResponse(final ResponseBuilder responseBuilder) {
        return responseBuilder.build();
    }

    //region > helpers
    private ResponseBuilder invokeActionUsingAdapters(
            final ResponseContext responseContext,
            final ObjectAction action, final JsonRepresentation arguments, SelfLink selfLink) {

        final ResourceContext resourceContext = responseContext.getResourceContext();
        final ObjectAdapter objectAdapter = responseContext.getObjectAdapter();
        final ObjectAdapterLinkTo adapterLinkTo = responseContext.getAdapterLinkTo();

        final ObjectActionArgHelper argHelper = new ObjectActionArgHelper(resourceContext, objectAdapter, action);

        final List<ObjectAdapter> argAdapters = argHelper.parseAndValidateArguments(arguments);

        // invoke
        final ObjectAdapter[] argArray2 = argAdapters.toArray(new ObjectAdapter[0]);
        final ObjectAdapter returnedAdapter = action.execute(objectAdapter, argArray2);

        // response
        final ActionResultReprRenderer renderer = new ActionResultReprRenderer(resourceContext, null, selfLink, JsonRepresentation.newMap());

        renderer.with(new ObjectAndActionInvocation(objectAdapter, action, arguments, returnedAdapter)).using(adapterLinkTo);

        final ResponseBuilder respBuilder = Responses.ofOk(renderer, Caching.NONE);

        final Version version = objectAdapter.getVersion();
        Responses.addLastModifiedAndETagIfAvailable(respBuilder, version);

        return respBuilder;
    }
    //endregion

}
