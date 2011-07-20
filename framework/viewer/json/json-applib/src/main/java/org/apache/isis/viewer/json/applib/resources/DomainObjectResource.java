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
package org.apache.isis.viewer.json.applib.resources;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// under /objects
public interface DomainObjectResource {

    @GET
    @Path("/{oid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String object(@PathParam("oid") final String oidStr);

    @GET
    @Path("/{oid}/properties{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String propertyDetails(
        @PathParam("oid") final String oidStr,
        @PathParam("propertyId") final String propertyId);

    @GET
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String accessCollection(
        @PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId);

    @GET
    @Path("/{oid}/actions/{actionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String actionPrompt(
        @PathParam("oid") final String oidStr, 
        @PathParam("actionId") final String actionId);

    @GET
    @Path("/{oid}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON })
    public Object invokeActionIdempotent(
        @PathParam("oid") final String oidStr, 
        @PathParam("actionId") final String actionId,
        @QueryParam("arg") final List<String> arguments);

    @PUT
    @Path("/{oid}/properties{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response modifyProperty(
        @PathParam("oid") final String oidStr,
        @PathParam("propertyId") final String propertyId, 
        final InputStream body);

    @PUT
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response addToSet(
        @PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId,
        final InputStream body);

    @DELETE
    @Path("/{oid}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response clearProperty(
        @PathParam("oid") final String oidStr, 
        @PathParam("propertyId") final String propertyId);

    @DELETE
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response removeFromCollection(
        @PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId,
        final InputStream body);

    @POST
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response addToList(
        @PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId,
        final InputStream body);

    @POST
    @Path("/{oid}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON })
    public Object invokeAction(
        @PathParam("oid") final String oidStr, 
        @PathParam("actionId") final String actionId,
        final InputStream body);
}