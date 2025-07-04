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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.applib.user.UserResource;
import org.apache.causeway.viewer.restfulobjects.rendering.Caching;
import org.apache.causeway.viewer.restfulobjects.rendering.ResponseFactory;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.causeway.viewer.restfulobjects.viewer.context.ResourceContext;
import org.apache.causeway.viewer.restfulobjects.viewer.webmodule.CausewayRestfulObjectsInteractionFilter;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class UserResourceServerside extends ResourceAbstract
implements UserResource {

    public UserResourceServerside() {
        super();
        log.debug("<init>");
    }

    @Override
    public ResponseEntity<Object> user() {

        var resourceContext = createResourceContext(
                RepresentationType.USER, Where.NOWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        final UserReprRenderer renderer = new UserReprRenderer(resourceContext, null, JsonRepresentation.newMap());
        renderer.includesSelf().with(resourceContext.getInteractionService().currentInteractionContextElseFail());

        return _EndpointLogging.response(log, "GET /user", responseFactory.ok(renderer, Caching.ONE_HOUR));
    }

    @Override
    public ResponseEntity<Object> deleteUserNotAllowed() {
        throw RestfulObjectsApplicationException.createWithMessage(HttpStatus.METHOD_NOT_ALLOWED, "Deleting the user resource is not allowed.");

    }

    @Override
    public ResponseEntity<Object> putUserNotAllowed() {
        throw RestfulObjectsApplicationException.createWithMessage(HttpStatus.METHOD_NOT_ALLOWED, "Putting to the user resource is not allowed.");

    }

    @Override
    public ResponseEntity<Object> postUserNotAllowed() {
        throw RestfulObjectsApplicationException.createWithMessage(HttpStatus.METHOD_NOT_ALLOWED, "Posting to the user resource is not allowed.");
    }

    /**
     * Not part of the Restful Objects spec.
     */
    @Override
    @SneakyThrows
    public ResponseEntity<Object> logout() {

        var resourceContext = createResourceContext(
                RepresentationType.HOME_PAGE, Where.NOWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        final HomePageReprRenderer renderer = new HomePageReprRenderer(resourceContext, null, JsonRepresentation.newMap());
        renderer.includesSelf();

        logout(resourceContext);

        // we also redirect to home page with special query string; this allows the session filter
        // to clear out any cookies/headers (eg if BASIC auth in use).

        final URI location = new URI("?" + CausewayRestfulObjectsInteractionFilter.CAUSEWAY_SESSION_FILTER_QUERY_STRING_FORCE_LOGOUT);
        return _EndpointLogging.response(log, "GET /user/logout", ResponseFactory.temporaryRedirect(location));
    }

    private void logout(final ResourceContext resourceContext) {
        var interactionService = resourceContext.getInteractionService();
        var authenticationManager = resourceContext.getAuthenticationManager();

        interactionService
        .currentInteractionContext()
        .ifPresent(interactionContext->{
            authenticationManager.closeSession(interactionContext.getUser());
            interactionService.closeInteractionLayers();
        });
    }

}
