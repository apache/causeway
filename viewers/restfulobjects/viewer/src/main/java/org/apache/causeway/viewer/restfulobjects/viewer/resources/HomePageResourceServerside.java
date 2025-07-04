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

import jakarta.ws.rs.core.Response;

import org.springframework.web.bind.annotation.RestController;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;
import org.apache.causeway.viewer.restfulobjects.applib.homepage.HomePageResource;
import org.apache.causeway.viewer.restfulobjects.rendering.Caching;
import org.apache.causeway.viewer.restfulobjects.rendering.Responses;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class HomePageResourceServerside
extends ResourceAbstract
implements HomePageResource {

    public HomePageResourceServerside() {
        super();
        log.debug("<init>");
    }

    @Override
//    @Produces({
//        MediaType.APPLICATION_JSON,
//        RestfulMediaType.APPLICATION_JSON_HOME_PAGE })
    public Response homePage() {

        var resourceContext = createResourceContext(
                RepresentationType.HOME_PAGE, Where.NOWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        var homePageReprRenderer = new HomePageReprRenderer(resourceContext, null, JsonRepresentation.newMap());
        homePageReprRenderer.includesSelf();

        return _EndpointLogging.response(log, "GET /",
                Responses.ofOk(homePageReprRenderer, Caching.ONE_DAY).build());
    }

    @Override
    public Response deleteHomePageNotAllowed() {
        throw _EndpointLogging.error(log, "DELETE /",
                RestfulObjectsApplicationException
                .createWithMessage(
                        RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED,
                        "Deleting the home page resource is not allowed."));
    }

    @Override
    public Response putHomePageNotAllowed() {
        throw _EndpointLogging.error(log, "PUT /",
                RestfulObjectsApplicationException
                .createWithMessage(
                        RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED,
                        "Putting to the home page resource is not allowed."));
    }

    @Override
    public Response postHomePageNotAllowed() {
        throw _EndpointLogging.error(log, "POST /",
                RestfulObjectsApplicationException
                .createWithMessage(
                        RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED,
                        "Posting to the home page resource is not allowed."));
    }

    @Override
//    @GET
//    @Path("/notAuthenticated")
//    @Produces({ MediaType.APPLICATION_JSON })
    public Response notAuthenticated() {
        throw _EndpointLogging.error(log, "GET /notAuthenticated",
                RestfulObjectsApplicationException.create(HttpStatusCode.UNAUTHORIZED));
    }

}
