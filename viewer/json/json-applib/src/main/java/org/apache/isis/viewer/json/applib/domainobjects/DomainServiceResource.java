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
package org.apache.isis.viewer.json.applib.domainobjects;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.ClientResponseType;

import org.apache.isis.viewer.json.applib.RestfulMediaType;

@Path("/services")
public interface DomainServiceResource {

    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_LIST, RestfulMediaType.APPLICATION_JSON_ERROR })
    @ClientResponseType(entityType = String.class)
    public Response services();

    // //////////////////////////////////////////////////////////
    // domain service
    // //////////////////////////////////////////////////////////

    @GET
    @Path("/{serviceId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_DOMAIN_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR })
    @ClientResponseType(entityType = String.class)
    public Response service(@PathParam("serviceId") final String serviceId);

    // //////////////////////////////////////////////////////////
    // domain service property
    // //////////////////////////////////////////////////////////

    @GET
    @Path("/{serviceId}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response propertyDetails(@PathParam("serviceId") final String serviceId, @PathParam("propertyId") final String propertyId);

    // //////////////////////////////////////////////////////////
    // domain service action
    // //////////////////////////////////////////////////////////

    @GET
    @Path("/{serviceId}/actions/{actionId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_ACTION, RestfulMediaType.APPLICATION_JSON_ERROR })
    @ClientResponseType(entityType = String.class)
    public Response actionPrompt(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId);

    // //////////////////////////////////////////////////////////
    // domain service action invoke
    // //////////////////////////////////////////////////////////

    @GET
    @Path("/{serviceId}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR })
    @ClientResponseType(entityType = String.class)
    public Response invokeActionQueryOnly(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId);

    @PUT
    @Path("/{serviceId}/actions/{actionId}/invoke")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR })
    @ClientResponseType(entityType = String.class)
    public Response invokeActionIdempotent(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId, final InputStream arguments);

    @POST
    @Path("/{serviceId}/actions/{actionId}/invoke")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR })
    @ClientResponseType(entityType = String.class)
    public Response invokeAction(@PathParam("serviceId") final String serviceId, @PathParam("actionId") final String actionId, final InputStream arguments);
}