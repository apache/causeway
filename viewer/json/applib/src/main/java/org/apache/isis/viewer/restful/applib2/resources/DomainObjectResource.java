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
package org.apache.isis.viewer.restful.applib2.resources;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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

    @PUT
    @Path("/{oid}/properties{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String modifyProperty(
        @PathParam("oid") final String oidStr,
        @PathParam("propertyId") final String propertyId, 
        @FormParam("proposedValue") final String proposedValue);

    @DELETE
    @Path("/{oid}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String clearProperty(
        @PathParam("oid") final String oidStr, 
        @PathParam("propertyId") final String propertyId);

    @GET
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String accessCollection(
        @PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId);

    @PUT
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String addToCollection(
        @PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId,
        @FormParam("proposedValue") final String proposedValueOidStr);

    @DELETE
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String removeFromCollection(
        @PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId,
        @FormParam("proposedValue") final String proposedValueOidStr);

    @GET
    @Path("/{oid}/actions/{actionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String actionPrompt(
        @PathParam("oid") final String oidStr, 
        @PathParam("actionId") final String actionId);

    @POST
    @Path("/{oid}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON })
    public String invokeAction(
        @PathParam("oid") final String oidStr, 
        @PathParam("actionId") final String actionId,
        final InputStream body);

    @GET
    @Path("/{oid}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON })
    public String invokeActionIdempotent(
        @PathParam("oid") final String oidStr, 
        @PathParam("actionId") final String actionId,
        @QueryParam("argument") final List<String> arguments);

}