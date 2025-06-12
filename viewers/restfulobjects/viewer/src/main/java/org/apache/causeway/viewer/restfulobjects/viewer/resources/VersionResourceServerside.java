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

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;
import org.apache.causeway.viewer.restfulobjects.applib.version.VersionResource;
import org.apache.causeway.viewer.restfulobjects.rendering.Caching;
import org.apache.causeway.viewer.restfulobjects.rendering.Responses;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.causeway.viewer.restfulobjects.viewer.context.ResourceContext;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation note: it seems to be necessary to annotate the implementation
 * with {@link Path} rather than the interface (at least under RestEasy 1.0.2
 * and 1.1-RC2).
 */
@Component
@Path("/version")
@Slf4j
public class VersionResourceServerside
extends ResourceAbstract
implements VersionResource {

    @Inject
    public VersionResourceServerside() {
        super();
        log.debug("<init>");
    }

    @Override
    @GET
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_VERSION })
    public Response version() {

        var resourceContext = createResourceContext(
                RepresentationType.VERSION, Where.NOWHERE, RepresentationService.Intent.NOT_APPLICABLE);
        fakeRuntimeExceptionIfXFail(resourceContext);

        final VersionReprRenderer renderer = new VersionReprRenderer(resourceContext, null, JsonRepresentation.newMap());
        renderer.includesSelf();

        return _EndpointLogging.response(log, "GET /version",
                Responses.ofOk(renderer, Caching.ONE_DAY).build());
    }

    @Override
    public Response deleteVersionNotAllowed() {
        throw _EndpointLogging.error(log, "DELETE /version",
                RestfulObjectsApplicationException
                .createWithMessage(
                        RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED,
                        "Deleting the version resource is not allowed."));
    }

    @Override
    public Response putVersionNotAllowed() {
        throw _EndpointLogging.error(log, "PUT /version",
                RestfulObjectsApplicationException
                .createWithMessage(
                        RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED,
                        "Putting to the version resource is not allowed."));
    }

    @Override
    public Response postVersionNotAllowed() {
        throw _EndpointLogging.error(log, "POST /version",
                RestfulObjectsApplicationException
                .createWithMessage(
                        RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED,
                        "Posting to the version resource is not allowed."));
    }

    private void fakeRuntimeExceptionIfXFail(final ResourceContext resourceContext) {
        final HttpHeaders httpHeaders = resourceContext.httpHeaders();
        final List<String> requestHeader = httpHeaders.getRequestHeader("X-Fail");
        if (requestHeader != null && !requestHeader.isEmpty()) {
            throw _EndpointLogging.error(log, "GET /version",
                    RestfulObjectsApplicationException.create(HttpStatusCode.METHOD_FAILURE));
        }
    }

}
