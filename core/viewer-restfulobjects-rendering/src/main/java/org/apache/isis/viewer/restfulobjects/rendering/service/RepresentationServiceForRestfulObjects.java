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
package org.apache.isis.viewer.restfulobjects.rendering.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.rendering.Caching;
import org.apache.isis.viewer.restfulobjects.rendering.Responses;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.*;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ActionResultReprRenderer.SelfLink;

@DomainService
public class RepresentationServiceForRestfulObjects implements RepresentationService {

    private static final DateFormat ETAG_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    // //////////////////////////////////////////////////////////////
    // objectRepresentation
    // //////////////////////////////////////////////////////////////

    @Override
    @Programmatic
    public Response objectRepresentation(
            final Context resourceContext,
            final ObjectAdapter objectAdapter) {
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


    @Override
    @Programmatic
    public Response propertyDetails(
            final Context rendererContext,
            final ObjectAndProperty objectAndProperty,
            final MemberReprMode memberReprMode) {

        final ObjectPropertyReprRenderer renderer = new ObjectPropertyReprRenderer(rendererContext);
        renderer.with(objectAndProperty)
                .usingLinkTo(rendererContext.getAdapterLinkTo())
                .withMemberMode(memberReprMode);

        final ResponseBuilder responseBuilder = Responses.ofOk(renderer, Caching.NONE);
        return buildResponse(responseBuilder);
    }

    // //////////////////////////////////////////////////////////////
    // collectionDetails
    // //////////////////////////////////////////////////////////////

    @Override
    @Programmatic
    public Response collectionDetails(
            final Context rendererContext,
            final ObjectAndCollection objectAndCollection,
            final MemberReprMode memberReprMode) {

        final ObjectCollectionReprRenderer renderer = new ObjectCollectionReprRenderer(rendererContext);
        renderer.with(objectAndCollection)
                .usingLinkTo(rendererContext.getAdapterLinkTo())
                .withMemberMode(memberReprMode);

        final ResponseBuilder responseBuilder = Responses.ofOk(renderer, Caching.NONE);
        return buildResponse(responseBuilder);
    }

    // //////////////////////////////////////////////////////////////
    // action Prompt
    // //////////////////////////////////////////////////////////////

    @Override
    @Programmatic
    public Response actionPrompt(
            final Context rendererContext,
            final ObjectAndAction objectAndAction) {

        final ObjectActionReprRenderer renderer = new ObjectActionReprRenderer(rendererContext);
        renderer.with(objectAndAction)
                .usingLinkTo(rendererContext.getAdapterLinkTo())
                .asStandalone();

        final ResponseBuilder responseBuilder = Responses.ofOk(renderer, Caching.NONE);
        return buildResponse(responseBuilder);
    }

    // //////////////////////////////////////////////////////////////
    // action Result
    // //////////////////////////////////////////////////////////////

    @Override
    @Programmatic
    public Response actionResult(
            final Context rendererContext,
            final ObjectAndActionInvocation objectAndActionInvocation,
            final SelfLink selfLink) {

        final ActionResultReprRenderer renderer = new ActionResultReprRenderer(rendererContext, selfLink);
        renderer.with(objectAndActionInvocation)
                .using(rendererContext.getAdapterLinkTo());

        final ResponseBuilder respBuilder = Responses.ofOk(renderer, Caching.NONE);
        Responses.addLastModifiedAndETagIfAvailable(respBuilder, objectAndActionInvocation.getObjectAdapter().getVersion());
        return buildResponse(respBuilder);
    }

    /**
     * Overridable to allow further customization.
     */
    protected Response buildResponse(final ResponseBuilder responseBuilder) {
        return responseBuilder.build();
    }

}
