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
package org.apache.causeway.viewer.restfulobjects.applib.domaintypes;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import org.springframework.http.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.causeway.viewer.restfulobjects.applib.RestfulMediaType;

/**
 * @since 1.x {@index}
 */
@Path("/domain-types")
public interface DomainTypeResource {

    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_TYPE_LIST })
    public abstract Response domainTypes();

    @GET
    @Path("/{domainType}")
    @Produces({ MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_DOMAIN_TYPE })
    public abstract Response domainType(@PathParam("domainType") final String domainType);

    @GET
    @Path("/{domainType}/layout")
    @Produces({
        MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_LAYOUT_BS,
        MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_LAYOUT_BS
    })
    public abstract Response layout(@PathParam("domainType") final String domainType);

    @GET
    @Path("/{domainType}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_PROPERTY_DESCRIPTION })
    public abstract Response typeProperty(@PathParam("domainType") final String domainType, @PathParam("propertyId") final String propertyId);

    @GET
    @Path("/{domainType}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_COLLECTION_DESCRIPTION })
    public abstract Response typeCollection(@PathParam("domainType") final String domainType, @PathParam("collectionId") final String collectionId);

    @GET
    @Path("/{domainType}/actions/{actionId}")
    @Produces({ MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_ACTION_DESCRIPTION })
    public abstract Response typeAction(@PathParam("domainType") final String domainType, @PathParam("actionId") final String actionId);

    @GET
    @Path("/{domainType}/actions/{actionId}/params/{paramId}")
    @Produces({ MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_ACTION_PARAMETER_DESCRIPTION })
    public abstract Response typeActionParam(@PathParam("domainType") final String domainType, @PathParam("actionId") final String actionId, @PathParam("paramId") final String paramId);

    @GET
    @Path("/{domainType}/isSubtypeOf/invoke")
    @Produces({ MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_TYPE_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR })
    public abstract Response domainTypeIsSubtypeOf(
            @PathParam("domainType") final String domainType,
            @QueryParam("supertype") String superType, // simple style
            @QueryParam("args") final String argumentsQueryString // formal style
            );

    @GET
    @Path("/{domainType}/isSupertypeOf/invoke")
    @Produces({ MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_TYPE_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR })
    public abstract Response domainTypeIsSupertypeOf(
            @PathParam("domainType") final String domainType,
            @QueryParam("supertype") String superType, // simple style
            @QueryParam("args") final String argumentsQueryString // formal style
            );
}
