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
package org.apache.causeway.viewer.restfulobjects.viewer.resources;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.causeway.viewer.restfulobjects.applib.user.UserResource;
import org.apache.causeway.viewer.restfulobjects.rendering.Caching;
import org.apache.causeway.viewer.restfulobjects.rendering.Responses;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.causeway.viewer.restfulobjects.viewer.context.ResourceContext;
import org.apache.causeway.viewer.restfulobjects.viewer.webmodule.CausewayRestfulObjectsInteractionFilter;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class UserResourceServerside extends ResourceAbstract implements UserResource {

    @Inject
    public UserResourceServerside() {
        super();
        log.debug("<init>");
    }

    @Override
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_USER })
    public Response user() {

        val resourceContext = createResourceContext(
                RepresentationType.USER, Where.NOWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        final UserReprRenderer renderer = new UserReprRenderer(resourceContext, null, JsonRepresentation.newMap());
        renderer.includesSelf().with(resourceContext.getInteractionService().currentInteractionContextElseFail());

        return Responses.ofOk(renderer, Caching.ONE_HOUR).build();
    }

    @Override
    public Response deleteUserNotAllowed() {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Deleting the user resource is not allowed.");

    }

    @Override
    public Response putUserNotAllowed() {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Putting to the user resource is not allowed.");

    }

    @Override
    public Response postUserNotAllowed() {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Posting to the user resource is not allowed.");
    }

    /**
     * Not part of the Restful Objects spec.
     */
    @Override
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_HOME_PAGE })
    public Response logout() {

        val resourceContext = createResourceContext(
                RepresentationType.HOME_PAGE, Where.NOWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        final HomePageReprRenderer renderer = new HomePageReprRenderer(resourceContext, null, JsonRepresentation.newMap());
        renderer.includesSelf();

        logout(resourceContext);

        // we also redirect to home page with special query string; this allows the session filter
        // to clear out any cookies/headers (eg if BASIC auth in use).
        try {
            final URI location = new URI("?" + CausewayRestfulObjectsInteractionFilter.CAUSEWAY_SESSION_FILTER_QUERY_STRING_FORCE_LOGOUT);
            return Response.temporaryRedirect(location).build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private void logout(final ResourceContext resourceContext) {
        val interactionService = resourceContext.getInteractionService();
        val authenticationManager = resourceContext.getAuthenticationManager();

        interactionService
        .currentInteractionContext()
        .ifPresent(interactionContext->{
            authenticationManager.closeSession(interactionContext.getUser());
            interactionService.closeInteractionLayers();
        });
    }

}
