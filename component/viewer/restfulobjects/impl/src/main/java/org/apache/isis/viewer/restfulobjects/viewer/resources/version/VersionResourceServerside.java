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
package org.apache.isis.viewer.restfulobjects.viewer.resources.version;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.version.VersionResource;
import org.apache.isis.viewer.restfulobjects.viewer.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.viewer.representations.RendererFactory;
import org.apache.isis.viewer.restfulobjects.viewer.resources.ResourceAbstract;

/**
 * Implementation note: it seems to be necessary to annotate the implementation
 * with {@link Path} rather than the interface (at least under RestEasy 1.0.2
 * and 1.1-RC2).
 */
@Path("/version")
public class VersionResourceServerside extends ResourceAbstract implements VersionResource {

    @Override
    @GET
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_VERSION })
    public Response version() {
        final RepresentationType representationType = RepresentationType.VERSION;

        init(representationType, Where.NOWHERE);
        fakeRuntimeExceptionIfXFail();

        final RendererFactory factory = rendererFactoryRegistry.find(representationType);
        final VersionReprRenderer renderer = (VersionReprRenderer) factory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.includesSelf();

        return responseOfOk(renderer, Caching.ONE_DAY).build();
    }

    private void fakeRuntimeExceptionIfXFail() {
        final HttpHeaders httpHeaders = getResourceContext().getHttpHeaders();
        if (httpHeaders.getRequestHeader("X-Fail") != null) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.METHOD_FAILURE);
        }
    }

}