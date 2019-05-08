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
package org.apache.isis.viewer.restfulobjects.server.resources;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.runtime.system.SystemConstants;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.user.UserResource;
import org.apache.isis.viewer.restfulobjects.rendering.Caching;
import org.apache.isis.viewer.restfulobjects.rendering.Responses;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;

public class UserResourceServerside extends ResourceAbstract implements UserResource {

    @Override
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_USER })
    public Response user() {
        init(RepresentationType.USER, Where.NOWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        final UserReprRenderer renderer = new UserReprRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.includesSelf().with(getResourceContext().getAuthenticationSession());

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
        init(RepresentationType.HOME_PAGE, Where.NOWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        final HomePageReprRenderer renderer = new HomePageReprRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.includesSelf();

        getResourceContext().logoutAuthenticationSession();

        // we also redirect to home page with special query string; this allows the session filter
        // to clear out any cookies/headers (eg if BASIC auth in use).
        try {
            final URI location = new URI("?" + SystemConstants.ISIS_SESSION_FILTER_QUERY_STRING_FORCE_LOGOUT);
            return Response.temporaryRedirect(location).build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
