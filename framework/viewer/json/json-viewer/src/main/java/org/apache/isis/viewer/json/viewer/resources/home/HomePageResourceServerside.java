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
package org.apache.isis.viewer.json.viewer.resources.home;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.homepage.HomePageResource;
import org.apache.isis.viewer.json.viewer.JsonApplicationException;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.json.viewer.resources.ResourceAbstract;

/**
 * Implementation note: it seems to be necessary to annotate the implementation with {@link Path} rather than the
 * interface (at least under RestEasy 1.0.2 and 1.1-RC2).
 */
public class HomePageResourceServerside extends ResourceAbstract implements HomePageResource {


    @Override
    @Produces(MediaType.APPLICATION_JSON)
    public Response resources() {
        init();
        
        fakeRuntimeExceptionIfXFail();
        
        JsonRepresentation representation = JsonRepresentation.newMap();
        
        representation.mapPut("self", LinkBuilder.newBuilder(getResourceContext(), "self", "").build());
        representation.mapPut("user", LinkBuilder.newBuilder(getResourceContext(), "user", "user").build());
        representation.mapPut("services", LinkBuilder.newBuilder(getResourceContext(), "services", "services").build());
        representation.mapPut("capabilities", LinkBuilder.newBuilder(getResourceContext(), "capabilities", "capabilities").build());

        representation.mapPut("links", JsonRepresentation.newArray());
        representation.mapPut("extensions", JsonRepresentation.newMap());

        return Response.ok()
                .entity(jsonFor(representation))
                .cacheControl(CACHE_ONE_DAY)
                .header(RestfulResponse.Header.X_REPRESENTATION_TYPE.getName(), RepresentationType.HOME_PAGE.getName())
                .type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    private void fakeRuntimeExceptionIfXFail() {
        HttpHeaders httpHeaders = getResourceContext().getHttpHeaders();
        if(httpHeaders.getRequestHeader("X-Fail") != null) {
            throw JsonApplicationException.create(HttpStatusCode.METHOD_FAILURE);
        }
    }


}