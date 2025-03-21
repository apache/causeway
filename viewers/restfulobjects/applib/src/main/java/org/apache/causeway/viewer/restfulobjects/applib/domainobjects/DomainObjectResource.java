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
package org.apache.causeway.viewer.restfulobjects.applib.domainobjects;

import java.io.InputStream;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.causeway.viewer.restfulobjects.applib.RestfulMediaType;

/**
 * @since 1.x {@index}
 */
@Path("/objects")
public interface DomainObjectResource {

    @POST
    @Path("/{domainType}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response persist(@PathParam("domainType") String domainType, final InputStream object);

    // //////////////////////////////////////////////////////////
    // domain object
    // //////////////////////////////////////////////////////////

    @GET
    @Path("/{domainType}/{instanceId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response object(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId);

    @PUT
    @Path("/{domainType}/{instanceId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response object(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, final InputStream arguments);

    @DELETE
    @Path("/{domainType}/{instanceId}")
    public Response deleteMethodNotSupported(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId);

    @POST
    @Path("/{domainType}/{instanceId}")
    public Response postMethodNotAllowed(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId);

    // //////////////////////////////////////////////////////////
    // domain object image
    // //////////////////////////////////////////////////////////

    @GET
    @Path("/{domainType}/{instanceId}/image")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        "image/png"
    })
    public Response image(
            @PathParam("domainType")
            final String domainType,
            @PathParam("instanceId")
            final String instanceId);

    // //////////////////////////////////////////////////////////
    // domain object layout
    // //////////////////////////////////////////////////////////

    @GET
    @Path("/{domainType}/{instanceId}/object-layout")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_LAYOUT_BS,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_LAYOUT_BS
    })
    public Response layout(
            @PathParam("domainType")
            final String domainType,
            @PathParam("instanceId")
            final String instanceId);

    // //////////////////////////////////////////////////////////
    // domain object property
    // //////////////////////////////////////////////////////////

    @GET
    @Path("/{domainType}/{instanceId}/properties/{propertyId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response propertyDetails(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("propertyId") final String propertyId);

    @PUT
    @Path("/{domainType}/{instanceId}/properties/{propertyId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response modifyProperty(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("propertyId") final String propertyId, final InputStream arguments);

    @DELETE
    @Path("/{domainType}/{instanceId}/properties/{propertyId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response clearProperty(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("propertyId") final String propertyId);

    @POST
    @Path("/{domainType}/{instanceId}/properties/{propertyId}")
    public Response postPropertyNotAllowed(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("propertyId") final String propertyId);

    // //////////////////////////////////////////////////////////
    // domain object collection
    // //////////////////////////////////////////////////////////

    @GET
    @Path("/{domainType}/{instanceId}/collections/{collectionId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response accessCollection(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("collectionId") final String collectionId);

//XXX[CAUSEWAY-3084] - removal of (direct) collection modification - business logic should handle that via actions instead
//    @PUT
//    @Path("/{domainType}/{instanceId}/collections/{collectionId}")
//    @Consumes({ MediaType.WILDCARD })
//    @Produces({
//        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_JSON_ERROR,
//        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_XML_ERROR
//    })
//    public Response addToSet(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("collectionId") final String collectionId, final InputStream arguments);
//
//    @POST
//    @Path("/{domainType}/{instanceId}/collections/{collectionId}")
//    @Consumes({ MediaType.WILDCARD })
//    @Produces({
//        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_JSON_ERROR,
//        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_XML_ERROR
//    })
//    public Response addToList(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("collectionId") final String collectionId, final InputStream arguments);
//
//    @DELETE
//    @Path("/{domainType}/{instanceId}/collections/{collectionId}")
//    @Consumes({ MediaType.WILDCARD })
//    @Produces({
//        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_JSON_ERROR,
//        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_XML_ERROR
//    })
//    public Response removeFromCollection(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("collectionId") final String collectionId);

    // //////////////////////////////////////////////////////////
    // domain object action
    // //////////////////////////////////////////////////////////

    @GET
    @Path("/{domainType}/{instanceId}/actions/{actionId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_ACTION, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_ACTION, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response actionPrompt(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("actionId") final String actionId);

    @DELETE
    @Path("/{domainType}/{instanceId}/actions/{actionId}")
    public Response deleteActionPromptNotAllowed(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("actionId") final String actionId);

    @PUT
    @Path("/{domainType}/{instanceId}/actions/{actionId}")
    public Response putActionPromptNotAllowed(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("actionId") final String actionId);

    @POST
    @Path("/{domainType}/{instanceId}/actions/{actionId}")
    public Response postActionPromptNotAllowed(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("actionId") final String actionId);

    // //////////////////////////////////////////////////////////
    // domain object action invoke
    // //////////////////////////////////////////////////////////

    @GET
    @Path("/{domainType}/{instanceId}/actions/{actionId}/invoke")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response invokeActionQueryOnly(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("actionId") final String actionId, @QueryParam("x-causeway-querystring") final String xCausewayQueryString);

    @PUT
    @Path("/{domainType}/{instanceId}/actions/{actionId}/invoke")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response invokeActionIdempotent(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("actionId") final String actionId, final InputStream arguments);

    @POST
    @Path("/{domainType}/{instanceId}/actions/{actionId}/invoke")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response invokeAction(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("actionId") final String actionId, final InputStream arguments);

    @DELETE
    @Path("/{domainType}/{instanceId}/actions/{actionId}/invoke")
    public Response deleteInvokeActionNotAllowed(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("actionId") final String actionId);

}
