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
package org.apache.isis.viewer.json.viewer.resources.domaintypes;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.domaintypes.DomainTypeResource;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.json.viewer.resources.ResourceAbstract;

/**
 * Implementation note: it seems to be necessary to annotate the implementation with {@link Path} rather than the
 * interface (at least under RestEasy 1.0.2 and 1.1-RC2).
 */
@Path("/domainTypes")
public class DomainTypeResourceServerside extends ResourceAbstract implements DomainTypeResource {


    @GET
    @Path("/{representationTypeName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response representationType(@PathParam("representationTypeName") final String representationTypeName) {
        init();

        JsonRepresentation representation = JsonRepresentation.newMap();
        representation.mapPut("representationType", LinkBuilder.newBuilder(getResourceContext(), "representationType", "representationTypes/representationType").build());
        representation.mapPut("self", LinkBuilder.newBuilder(getResourceContext(), "self", "representationType/" + representationTypeName).build());
        
        return responseOfOk(jsonFor(representation));
    }


    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response domainTypes() {
        return null;
    }

    @GET
    @Path("/{domainType}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response domainType(@PathParam("domainType") final String domainType){
        return null;
    }

    @GET
    @Path("/{domainType}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response typeProperty(@PathParam("domainType") final String domainType,
        @PathParam("propertyId") final String propertyId){
        return null;
    }

    @GET
    @Path("/{domainType}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response typeCollection(@PathParam("domainType") final String domainType,
        @PathParam("collectionId") final String collectionId){
        return null;
    }

    @GET
    @Path("/{domainType}/actions/{actionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response typeAction(@PathParam("domainType") final String domainType,
        @PathParam("actionId") final String actionId){
        return null;
    }

    @GET
    @Path("/{domainType}/actions/{actionId}/params/{paramNum}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response typeActionParam(@PathParam("domainType") final String domainType,
        @PathParam("actionId") final String actionId, @PathParam("paramNum") final int paramNum){
        return null;
    }


}