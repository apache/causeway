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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.applib.version.VersionResource;
import org.apache.causeway.viewer.restfulobjects.rendering.Caching;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.causeway.viewer.restfulobjects.viewer.context.ResourceContext;

import lombok.extern.slf4j.Slf4j;

@RestController
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
    public ResponseEntity<Object> version() {

        var resourceContext = createResourceContext(
                RepresentationType.VERSION, Where.NOWHERE, RepresentationService.Intent.NOT_APPLICABLE);
        fakeRuntimeExceptionIfXFail(resourceContext);

        final VersionReprRenderer renderer = new VersionReprRenderer(resourceContext, null, JsonRepresentation.newMap());
        renderer.includesSelf();

        return _EndpointLogging.response(log, "GET /version",
            responseFactory.ok(renderer, Caching.ONE_DAY));
    }

    @Override
    public ResponseEntity<Object> deleteVersionNotAllowed() {
        throw _EndpointLogging.error(log, "DELETE /version",
                RestfulObjectsApplicationException
                .createWithMessage(
                        HttpStatus.METHOD_NOT_ALLOWED,
                        "Deleting the version resource is not allowed."));
    }

    @Override
    public ResponseEntity<Object> putVersionNotAllowed() {
        throw _EndpointLogging.error(log, "PUT /version",
                RestfulObjectsApplicationException
                .createWithMessage(
                        HttpStatus.METHOD_NOT_ALLOWED,
                        "Putting to the version resource is not allowed."));
    }

    @Override
    public ResponseEntity<Object> postVersionNotAllowed() {
        throw _EndpointLogging.error(log, "POST /version",
                RestfulObjectsApplicationException
                .createWithMessage(
                        HttpStatus.METHOD_NOT_ALLOWED,
                        "Posting to the version resource is not allowed."));
    }

    // -- HELPER

    private void fakeRuntimeExceptionIfXFail(final ResourceContext resourceContext) {
        var httpHeaders = resourceContext.httpHeaders();
        final List<String> requestHeader = httpHeaders.getValuesAsList("X-Fail");
        if (requestHeader != null && !requestHeader.isEmpty()) {
            throw _EndpointLogging.error(log, "GET /version",
                    RestfulObjectsApplicationException.create(HttpStatus.PRECONDITION_FAILED));
        }
    }

}
