/*s
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
package org.apache.isis.viewer.restfulobjects.domainobjects;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.ClientResponseType;

import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;

@Path("/objects")
public interface DomainObjectResource {

    @POST
    @Path("/")
    @Consumes({ MediaType.WILDCARD })
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_DOMAIN_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR })
    @ClientResponseType(entityType = String.class)
    public Response persist(final InputStream object);

    // //////////////////////////////////////////////////////////
    // domain object
    // //////////////////////////////////////////////////////////

    @GET
    @Path("/{oid}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_DOMAIN_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR })
    @ClientResponseType(entityType = String.class)
    public Response object(@PathParam("oid") final String oidStr);

    @PUT
    @Path("/{oid}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({ RestfulMediaType.APPLICATION_JSON_DOMAIN_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR })
    @ClientResponseType(entityType = String.class)
    public Response object(@PathParam("oid") final String oidStr, final InputStream arguments);

    // //////////////////////////////////////////////////////////
    // domain object property
    // //////////////////////////////////////////////////////////

    @GET
    @Path("/{oid}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_JSON_ERROR })
    @ClientResponseType(entityType = String.class)
    public Response propertyDetails(@PathParam("oid") final String oidStr, @PathParam("propertyId") final String propertyId);

    @PUT
    @Path("/{oid}/properties/{propertyId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ERROR })
    @ClientResponseType(entityType = String.class)
    public Response modifyProperty(@PathParam("oid") final String oidStr, @PathParam("propertyId") final String propertyId, final InputStream arguments);

    @DELETE
    @Path("/{oid}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ERROR })
    @ClientResponseType(entityType = String.class)
    public Response clearProperty(@PathParam("oid") final String oidStr, @PathParam("propertyId") final String propertyId);

    // //////////////////////////////////////////////////////////
    // domain object collection
    // //////////////////////////////////////////////////////////

    @GET
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_JSON_ERROR })
    @ClientResponseType(entityType = String.class)
    public Response accessCollection(@PathParam("oid") final String oidStr, @PathParam("collectionId") final String collectionId);

    @PUT
    @Path("/{oid}/collections/{collectionId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response addToSet(@PathParam("oid") final String oidStr, @PathParam("collectionId") final String collectionId, final InputStream arguments);

    @POST
    @Path("/{oid}/collections/{collectionId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ERROR })
    @ClientResponseType(entityType = String.class)
    public Response addToList(@PathParam("oid") final String oidStr, @PathParam("collectionId") final String collectionId, final InputStream arguments);

    @DELETE
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ERROR })
    @ClientResponseType(entityType = String.class)
    public Response removeFromCollection(@PathParam("oid") final String oidStr, @PathParam("collectionId") final String collectionId);

    // //////////////////////////////////////////////////////////
    // domain object action
    // //////////////////////////////////////////////////////////

    @GET
    @Path("/{oid}/actions/{actionId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_ACTION, RestfulMediaType.APPLICATION_JSON_ERROR })
    @ClientResponseType(entityType = String.class)
    public Response actionPrompt(@PathParam("oid") final String oidStr, @PathParam("actionId") final String actionId);

    // //////////////////////////////////////////////////////////
    // domain object action invoke
    // //////////////////////////////////////////////////////////

    @GET
    @Path("/{oid}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR })
    @ClientResponseType(entityType = String.class)
    public Response invokeActionQueryOnly(@PathParam("oid") final String oidStr, @PathParam("actionId") final String actionId);

    @PUT
    @Path("/{oid}/actions/{actionId}/invoke")
    @Consumes({ MediaType.WILDCARD })
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_SCALAR_VALUE, RestfulMediaType.APPLICATION_JSON_ERROR })
    @ClientResponseType(entityType = String.class)
    public Response invokeActionIdempotent(@PathParam("oid") final String oidStr, @PathParam("actionId") final String actionId, final InputStream arguments);

    @POST
    @Path("/{oid}/actions/{actionId}/invoke")
    @Consumes({ MediaType.WILDCARD })
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR })
    @ClientResponseType(entityType = String.class)
    public Response invokeAction(@PathParam("oid") final String oidStr, @PathParam("actionId") final String actionId, final InputStream arguments);

}